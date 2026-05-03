package com.tmvkrpxl0.modpack.updator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import cpw.mods.jarhandling.JarContents;
import cpw.mods.jarhandling.JarContentsBuilder;
import net.neoforged.fml.ModLoadingException;
import net.neoforged.fml.ModLoadingIssue;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.progress.ProgressMeter;
import net.neoforged.fml.loading.progress.StartupNotificationManager;
import net.neoforged.neoforgespi.locating.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nullable;
import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.zip.CRC32;

// TODO UpdatingLocator는 Self 업데이트 불가능. 해결할 것.
public class UpdatingLocator implements IDependencyLocator {
    private static final String errorKey = "fml.modloadingissue.technical_error";
    private static final String baseUrl = "https://kedete-file.tmvkrpxl0.org/";

    @Override
    public void scanMods(List<IModFile> loadedMods, IDiscoveryPipeline pipeline) {
        if (!FMLEnvironment.production) {
            StartupNotificationManager.addModMessage("DEV: Ignoring Remote Mod Registry");
            return;
        }

        HttpClient client = HttpClient.newHttpClient();
        var discoveryAttributes = ModFileDiscoveryAttributes.DEFAULT.withDependencyLocator(this);

        ProgressMeter registryProgress = StartupNotificationManager.addProgressBar("Reading CodingLab Mod Registry", 1);

        Registry registry;
        try {
            Reader reader = new BufferedReader(new InputStreamReader(download("registry.json", client).body()));
            JsonElement json = JsonParser.parseReader(reader);
            reader.close();
            registry = Registry.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
        } catch (Exception exception) {
            StartupNotificationManager.addModMessage("FAILED TO READ MOD REGISTRY");
            throw new ModLoadingException(ModLoadingIssue.error(
                    errorKey,
                    "Failed to read Codinglab Mod Registry"
            ).withCause(exception));
        }
        registryProgress.complete();
        ProgressMeter modCheckProgress = StartupNotificationManager.addProgressBar(
                "Checking for mods",
                registry.builtMods().size()
        );

        Path cacheDir = FMLPaths.MODSDIR.get().resolve("codinglab");
        Path builtDir = cacheDir.resolve("built");
        for (Registry.ModEntry mod : registry.builtMods()) {
            StartupNotificationManager.addModMessage(String.format("Checking %s", mod.fileName()));

            Path modPath = builtDir.resolve(mod.fileName());
            File modFile = modPath.toFile();

            byte[][] fileContentContainer = new byte[][]{null};
            int fileSize;
            if (modFile.exists()) {
                try {
                    fileSize = (int) modFile.length();
                    fileContentContainer[0] = Files.readAllBytes(modPath);
                } catch (IOException exception) {
                    StartupNotificationManager.addModMessage("FAILED TO READ MOD FILE FOR CHECKSUM");
                    throw new ModLoadingException(
                            ModLoadingIssue.error(
                                    errorKey,
                                    "Failed to read mod file for checksum"
                            ).withCause(exception)
                    );
                }
            } else {
                fileSize = downloadMod(modFile, client, fileContentContainer);
            }

            long checksum = getChecksum(fileContentContainer[0], fileSize);
            if (checksum != mod.crc()) {
                fileSize = downloadMod(modFile, client, fileContentContainer);
            }
            checksum = getChecksum(fileContentContainer[0], fileSize);
            if (checksum != mod.crc()) {
                StartupNotificationManager.addModMessage(String.format("DOWNLOADED FILE %s FAILED CRC CHECK", modFile.getName()));
                throw new ModLoadingException(
                        ModLoadingIssue.error(
                                errorKey,
                                "Downloaded file %s failed crc check!".formatted(modFile.getName())
                        )
                );
            }

            switch(mod.assetSource()) {
                case Registry.ModSource.CurseForge ignored -> {
                    throw new RuntimeException("Curseforge download is not implemented!");
                }
                case Registry.ModSource.Inline ignored -> pipeline.addPath(modPath, discoveryAttributes, IncompatibleFileReporting.ERROR);
                case Registry.ModSource.Modrinth modrinth -> {
                    Path downloaded = downloadModrinth(modrinth.version(), client);
                    JarContents jar = new JarContentsBuilder()
                            .paths(modPath, downloaded)
                            .pathFilter((relative, source) -> {
                                boolean isAssets = relative.startsWith("assets") || relative.startsWith("/assets");

                                if (isAssets) {
                                    return source.equals(downloaded);
                                } else {
                                    return source.equals(modPath);
                                }
                            })
                            .build();
                    pipeline.addJarContent(jar, discoveryAttributes, IncompatibleFileReporting.ERROR);
                }
            }

            modCheckProgress.increment();
        }

        for(Registry.ModSource.ExternalSource extra: registry.extraMods()) {
            switch(extra) {
                case Registry.ModSource.CurseForge ignored -> {
                    throw new RuntimeException("Curseforge download is not implemented!");
                }
                case Registry.ModSource.Modrinth modrinth -> {
                    Path downloaded = downloadModrinth(modrinth.version(), client);
                    pipeline.addPath(downloaded, discoveryAttributes, IncompatibleFileReporting.ERROR);
                }
            }
        }

        modCheckProgress.complete();
    }

    private static long getChecksum(byte[] contents, int size) {
        long checksum;
        CRC32 crc = new CRC32();
        crc.update(contents, 0, size);
        checksum = crc.getValue();

        return checksum;
    }

    private static Path downloadModrinth(String version, HttpClient client) throws ModLoadingException {
        URI infoUri = URI.create("https://api.modrinth.com/v3/version/").resolve(version);
        var infoRequest = HttpRequest.newBuilder(infoUri).GET().build();
        ModrinthVersionInfo info;
        try {
            var response = client.send(infoRequest, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() != 200) {
                throwStatusException(response.statusCode(), infoUri);
            }

            JsonObject infoJson = JsonParser.parseReader(
                    new BufferedReader(
                            new InputStreamReader(response.body())
                    )
            ).getAsJsonObject();
            info = ModrinthVersionInfo.CODEC.parse(JsonOps.INSTANCE, infoJson).getOrThrow();
        } catch (IOException | InterruptedException | JsonSyntaxException | IllegalStateException e) {
            StartupNotificationManager.addModMessage("FAILED TO GET VERSION DATA FROM MODRINTH");
            throw new ModLoadingException(ModLoadingIssue
                    .error(errorKey, "Failed to get version data from modrinth for %s".formatted(version))
                    .withCause(e)
            );
        }

        ModrinthVersionInfo.ModrinthFile primary = info
                .files()
                .stream()
                .filter(ModrinthVersionInfo.ModrinthFile::primary)
                .findAny()
                .orElse(info.files().getFirst());

        boolean downloaded = false;
        byte[] jarContents;
        Path path = FMLPaths.MODSDIR.get().resolve("codinglab").resolve("modrinth").resolve(primary.filename());
        if (!path.toFile().exists()) {
            URI jarUri = URI.create(primary.url());
            var jarRequest = HttpRequest.newBuilder(jarUri).GET().build();
            try {
                var response = client.send(jarRequest, HttpResponse.BodyHandlers.ofByteArray());

                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    throwStatusException(response.statusCode(), jarUri);
                }
                jarContents = response.body();
                downloaded = true;
            } catch (IOException | InterruptedException e) {
                StartupNotificationManager.addModMessage("FAILED TO DOWNLOAD MOD FROM MODRINTH");
                throw new ModLoadingException(ModLoadingIssue
                        .error(errorKey, "Failed to download mod %s from modrinth".formatted(info.name()))
                        .withCause(e)
                );
            }
        } else {
            try {
                jarContents = Files.readAllBytes(path);
            } catch (IOException e) {
                StartupNotificationManager.addModMessage("FAILED TO READ MOD FROM MODRINTH FOR HASH CHECK");
                throw new ModLoadingException(ModLoadingIssue
                        .error(errorKey, "Failed to read mod jar file %s from modrinth".formatted(primary.filename()))
                        .withCause(e)
                );
            }
        }

        String sha1 = DigestUtils.sha1Hex(jarContents).toLowerCase();
        if (!primary.hashes().sha1().equals(sha1)) {
            StartupNotificationManager.addModMessage("DOWNLOADED FILE FAILED SHA1 HASH CHECK");
            throw new ModLoadingException(ModLoadingIssue.error(errorKey, "Mod %s from Modrinth has failed SHA1 Hash Check".formatted(info.name())));
        }

        if (downloaded) {
            try {
                boolean ignored = path.getParent().toFile().mkdirs();
                Files.createFile(path);
                Files.write(path, jarContents, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException e) {
                StartupNotificationManager.addModMessage("FAILED TO SAVE MOD FILE %s FROM MODRINTH".formatted(info.name()));
                throw new ModLoadingException(ModLoadingIssue.error(errorKey, "Failed to save mod file %s from Modrinth".formatted(primary.filename())));
            }
        }

        return path;
    }

    private static void throwStatusException(int statusCode, URI uri) {
        StartupNotificationManager.addModMessage("HTTP REQUEST FAILED WITH CODE %d".formatted(statusCode));
        throw new ModLoadingException(
                ModLoadingIssue.error(errorKey, "HTTP Request to %s failed with status code %d".formatted(uri, statusCode))
        );
    }

    private static int downloadMod(File modFile, HttpClient client, @Nullable byte[][] writeTo) throws ModLoadingException {
        StartupNotificationManager.addModMessage(String.format("Downloading %s", modFile.getName()));
        try {
            byte[] buffer = new byte[1024 * 1024 * 32]; // 32MiB buffer
            int size = IOUtils.read(
                    download(modFile.getName(), client).body(),
                    buffer
            );

            boolean ignored = modFile.getParentFile().mkdirs();
            Files.createFile(modFile.toPath());
            try (FileOutputStream output = new FileOutputStream(modFile)) {
                output.write(buffer, 0, size);
                if (writeTo != null) {
                    writeTo[0] = Arrays.copyOfRange(buffer, 0, size);
                }
            }
            return size;
        } catch (IOException e) {
            StartupNotificationManager.addModMessage("FAILED TO SAVE MOD FILE %s".formatted(modFile.getName()));
            throw new ModLoadingException(ModLoadingIssue
                    .error(errorKey, "Failed to download %s".formatted(modFile.getName()))
                    .withCause(e)
            );
        }
    }

    private static HttpResponse<InputStream> download(String fileName, HttpClient client) throws ModLoadingException {
        var uri = URI.create(baseUrl).resolve(fileName);
        var request = HttpRequest.newBuilder(uri).GET().build();
        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throwStatusException(response.statusCode(), uri);
            }
            return response;
        } catch (IOException | InterruptedException exception) {
            StartupNotificationManager.addModMessage("FAILED TO DOWNLOAD MOD FILE %s".formatted(fileName));
            throw new ModLoadingException(
                    ModLoadingIssue.error(errorKey, "Failed to download %s".formatted(fileName)).withCause(exception)
            );
        }
    }
}
