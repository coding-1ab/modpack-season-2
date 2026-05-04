package dev.codinglabs.updater;

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
import org.apache.commons.codec.binary.Hex;
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        var discoveryAttributes = ModFileDiscoveryAttributes.DEFAULT.withDependencyLocator(this);

        ProgressMeter registryProgress = StartupNotificationManager.addProgressBar("Reading CodingLab Mod Registry", 1);

        Registry registry;
        try {
            Reader reader = new BufferedReader(
                    new InputStreamReader(
                            requestDownload(
                                    URI.create(baseUrl).resolve("registry.json"),
                                    client
                            ).body()
                    )
            );
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
                    throw checksumIoException(exception);
                }
            } else {
                fileSize = downloadMod(builtMod(modFile.getName()), modFile, client, fileContentContainer);
            }

            long checksum = getChecksum(fileContentContainer[0], fileSize);
            if (checksum != mod.crc()) {
                fileSize = downloadMod(builtMod(modFile.getName()), modFile, client, fileContentContainer);
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

            switch (mod.assetSource()) {
                case Registry.ModSource.CurseForge curseForge -> {
                    Path downloaded = downloadCurseforge(curseForge.projectId(), curseForge.fileId(), client);
                    JarContents jar = buildJarContents(modPath, downloaded);
                    pipeline.addJarContent(jar, discoveryAttributes, IncompatibleFileReporting.ERROR);
                }
                case Registry.ModSource.Inline ignored ->
                        pipeline.addPath(modPath, discoveryAttributes, IncompatibleFileReporting.ERROR);
                case Registry.ModSource.Modrinth modrinth -> {
                    Path downloaded = downloadModrinth(modrinth.version(), client);
                    JarContents jar = buildJarContents(modPath, downloaded);
                    pipeline.addJarContent(jar, discoveryAttributes, IncompatibleFileReporting.ERROR);
                }
            }

            modCheckProgress.increment();
        }

        for (Registry.ModSource.ExternalSource extra : registry.extraMods()) {
            switch (extra) {
                case Registry.ModSource.CurseForge curseForge -> {
                    Path downloaded = downloadCurseforge(curseForge.projectId(), curseForge.fileId(), client);
                    pipeline.addPath(downloaded, discoveryAttributes, IncompatibleFileReporting.ERROR);
                }
                case Registry.ModSource.Modrinth modrinth -> {
                    Path downloaded = downloadModrinth(modrinth.version(), client);
                    pipeline.addPath(downloaded, discoveryAttributes, IncompatibleFileReporting.ERROR);
                }
            }
        }

        modCheckProgress.complete();
    }

    private static URI builtMod(String fileName) {
        return URI.create(baseUrl).resolve(fileName);
    }

    private static JarContents buildJarContents(Path built, Path downloaded) {
        return new JarContentsBuilder()
                .paths(built, downloaded)
                .pathFilter((relative, source) -> {
                    boolean isAssets = relative.startsWith("assets") || relative.startsWith("/assets");

                    if (isAssets) {
                        return source.equals(downloaded);
                    } else {
                        return source.equals(built);
                    }
                })
                .build();
    }

    private static ModLoadingException checksumIoException(@Nullable Exception exception) {
        StartupNotificationManager.addModMessage("FAILED TO READ MOD FILE FOR CHECKSUM");
        return new ModLoadingException(
                ModLoadingIssue.error(
                        errorKey,
                        "Failed to read mod file for checksum"
                ).withCause(exception)
        );
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
                throw statusException(response.statusCode(), infoUri);
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

        byte[][] jarContents = new byte[][]{null};
        int jarSize;
        Path modPath = FMLPaths.MODSDIR.get().resolve("codinglab").resolve("modrinth").resolve(primary.filename());
        File modFile = modPath.toFile();
        if (!modFile.exists()) {
            jarSize = downloadMod(URI.create(primary.url()), modFile, client, jarContents);
        } else {
            try {
                jarSize = (int) modFile.length();
                jarContents[0] = Files.readAllBytes(modPath);
            } catch (IOException e) {
                StartupNotificationManager.addModMessage("FAILED TO READ MOD FROM MODRINTH FOR HASH CHECK");
                throw new ModLoadingException(ModLoadingIssue
                        .error(errorKey, "Failed to read mod jar file %s from modrinth".formatted(primary.filename()))
                        .withCause(e)
                );
            }
        }

        String sha1 = sha1Hash(jarContents[0], jarSize);
        if (!primary.hashes().sha1().equalsIgnoreCase(sha1)) {
            throw hashFailException(primary.filename());
        }

        return modPath;
    }

    private static Path downloadCurseforge(long projectId, long fileId, HttpClient client) throws ModLoadingException {
        URI uri = URI.create(baseUrl + "curseforge/" + projectId + "/" + fileId);
        var curseUrlRequest = HttpRequest.newBuilder(uri)
                .GET()
                .build();

        InputStream response;
        try {
            response = client.send(curseUrlRequest, HttpResponse.BodyHandlers.ofInputStream()).body();
        } catch (InterruptedException | IOException | IllegalStateException e) {
            StartupNotificationManager.addModMessage("FAILED TO FETCH CURSEFORGE API");
            throw new ModLoadingException(ModLoadingIssue.error(
                    errorKey,
                    "Failed to fetch CurseForge API to download mod. Project Id: %d File Id: %d".formatted(projectId, fileId)
            ).withCause(e));
        }

        CurseDownloadInfo downloadInfo;
        try {
            JsonObject json = JsonParser.parseReader(new InputStreamReader(response)).getAsJsonObject();
            downloadInfo = CurseDownloadInfo.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
        } catch (JsonSyntaxException | IllegalStateException e) {
            StartupNotificationManager.addModMessage("FAILED TO PARSE DOWNLOAD URL");
            throw new ModLoadingException(ModLoadingIssue.error(
                    errorKey,
                    "Failed to parse JSON from download server. Json contents"
            ).withCause(e));
        }

        Path basePath = FMLPaths.MODSDIR.get().resolve("codinglab").resolve("curseforge");
        Path modPath = basePath.resolve(downloadInfo.fileName());
        File modFile = modPath.toFile();

        boolean shouldDownload;
        if (modFile.exists()) {
            String hash;
            try {
                hash = DigestUtils.sha1Hex(Files.newInputStream(modPath, StandardOpenOption.READ));
            } catch (IOException e) {
                throw checksumIoException(e);
            }

            shouldDownload = !hash.equalsIgnoreCase(downloadInfo.sha1Hash());
        } else {
            shouldDownload = true;
        }

        if (!shouldDownload) {
            return modPath;
        }

        byte[][] fileContents = new byte[][]{null};
        int size = downloadMod(URI.create(downloadInfo.url()), modFile, client, fileContents);
        if (!sha1Hash(fileContents[0], size).equalsIgnoreCase(downloadInfo.sha1Hash())) {
            throw hashFailException(modFile.getName());
        }
        return modPath;
    }

    private static String sha1Hash(byte[] contents, int size) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException impossible) {
            throw new RuntimeException(impossible);
        }
        md.update(contents, 0, size);

        return Hex.encodeHexString(md.digest());
    }

    private static ModLoadingException hashFailException(String fileName) {
        StartupNotificationManager.addModMessage("DOWNLOADED FILE FAILED SHA1 HASH CHECK");
        throw new ModLoadingException(ModLoadingIssue.error(errorKey, "Mod %s has failed SHA1 Hash Check".formatted(fileName)));
    }

    private static ModLoadingException statusException(int statusCode, URI uri) {
        StartupNotificationManager.addModMessage("HTTP REQUEST FAILED WITH CODE %d".formatted(statusCode));
        throw new ModLoadingException(
                ModLoadingIssue.error(errorKey, "HTTP Request to %s failed with status code %d".formatted(uri, statusCode))
        );
    }

    private static int downloadMod(URI uri, File modFile, HttpClient client, @Nullable byte[][] writeTo) throws ModLoadingException {
        StartupNotificationManager.addModMessage(String.format("Downloading %s", modFile.getName()));
        try {
            byte[] buffer = new byte[1024 * 1024 * 32]; // 32MiB buffer
            int size = IOUtils.read(
                    requestDownload(uri, client).body(),
                    buffer
            );

            boolean ignored = modFile.getParentFile().mkdirs();
            try (OutputStream output = Files.newOutputStream(
                    modFile.toPath(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING
            )) {
                output.write(buffer, 0, size);
                if (writeTo != null) {
                    writeTo[0] = buffer;
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

    private static HttpResponse<InputStream> requestDownload(URI uri, HttpClient client) throws ModLoadingException {
        var request = HttpRequest.newBuilder(uri).GET().build();
        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw statusException(response.statusCode(), uri);
            }
            return response;
        } catch (IOException | InterruptedException exception) {
            StartupNotificationManager.addModMessage("FAILED TO DOWNLOAD from %s".formatted(uri));
            throw new ModLoadingException(
                    ModLoadingIssue.error(errorKey, "Failed to download from %s".formatted(uri)).withCause(exception)
            );
        }
    }
}
