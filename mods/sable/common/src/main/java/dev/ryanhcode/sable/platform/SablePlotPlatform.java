package dev.ryanhcode.sable.platform;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ServiceLoader;

public interface SablePlotPlatform {

    SablePlotPlatform INSTANCE = ServiceLoader.load(SablePlotPlatform.class).findFirst().orElseThrow(() -> new RuntimeException("Failed to find sable plot platform"));

    void readLightData(final CompoundTag tag, final RegistryAccess registryAccess, final LevelChunk chunk);

    void readChunkAttachments(final CompoundTag tag, final RegistryAccess registryAccess, final LevelChunk chunk);

    void postLoad(final CompoundTag tag, final LevelChunk chunk);

    void writeLightData(final CompoundTag tag, final RegistryAccess registryAccess, final LevelChunk chunk);

    void writeChunkAttachments(final CompoundTag tag, final RegistryAccess registryAccess, final LevelChunk chunk);
}
