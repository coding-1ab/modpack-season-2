package com.tmvkrpxl0.modpack.updator;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.neoforged.fml.ModLoadingException;
import net.neoforged.fml.ModLoadingIssue;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.progress.ProgressMeter;
import net.neoforged.fml.loading.progress.StartupNotificationManager;
import net.neoforged.neoforgespi.locating.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.CRC32;

public class UpdatingLocator implements IDependencyLocator {
    @Override
    public void scanMods(List<IModFile> loadedMods, IDiscoveryPipeline pipeline) {
        ProgressMeter registryProgress = StartupNotificationManager.addProgressBar("Reading CodingLab Mod Registry", 1);
        Path registryPath = FMLPaths.MODSDIR.get().resolve("codinglab").resolve("registry.json");
        if (!registryPath.toFile().exists()) {
            StartupNotificationManager.addModMessage("DEV: Ignoring Missing Mod Registry");
            // throw new RuntimeException("Registry download unimplemented");
            return;
        }

        Registry registry;
        try {
            Reader reader = new BufferedReader(new FileReader(registryPath.toFile()));
            JsonElement json = JsonParser.parseReader(reader);
            reader.close();
            registry = Registry.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
        } catch (Exception exception) {
            StartupNotificationManager.addModMessage("FAILED TO READ MOD REGISTRY");
            throw new ModLoadingException(ModLoadingIssue.error(
                    "fml.modloadingissue.technical_error",
                    "Failed to read Codinglab Mod Registry"
            ).withCause(exception));
        }
        registryProgress.complete();
        ProgressMeter modCheckProgress = StartupNotificationManager.addProgressBar(
                "Checking for mods",
                registry.mods().size()
        );

        Path builtPath = FMLPaths.MODSDIR.get().resolve("codinglab").resolve("built");
        Path cursePath = FMLPaths.MODSDIR.get().resolve("codinglab").resolve("curseforge");
        for (Registry.ModEntry mod : registry.mods()) {
            boolean hasAssetStep = !(mod.assetSource() instanceof Registry.AssetSource.Inline);

            StartupNotificationManager.addModMessage(String.format("Checking %s", mod.fileName()));

            Path modPath = builtPath.resolve(mod.fileName());
            File modFile = modPath.toFile();

            if (!modFile.exists()) {
                StartupNotificationManager.addModMessage("MOD DOWNLOAD NOT IMPLEMENTED");
                throw new RuntimeException("Download isn't implemented");
            }

            CRC32 crc = new CRC32();
            byte[] fileContents;
            try {
                fileContents = Files.readAllBytes(modPath);
            } catch (Exception exception) {
                StartupNotificationManager.addModMessage("FAILED TO READ MOD FILE FOR CHECKSUM");
                throw new ModLoadingException(
                        ModLoadingIssue.error(
                                "fml.modloadingissue.technical_error",
                                "Failed to read mod file for checksum"
                        ).withCause(exception)
                );
            }
            crc.update(fileContents);
            long checksum = crc.getValue();
            if (checksum != mod.crc()) {
                StartupNotificationManager.addModMessage("MOD DOWNLOAD NOT IMPLEMENTED");
                throw new RuntimeException("Checksum mismatch");
            }

            if (hasAssetStep) {
                throw new RuntimeException("Asset downloading unimplemented");
            }

            pipeline.addPath(modPath, ModFileDiscoveryAttributes.DEFAULT, IncompatibleFileReporting.ERROR);
            modCheckProgress.increment();
        }
        modCheckProgress.complete();
    }
}
