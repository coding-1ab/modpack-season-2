package dev.ryanhcode.sable.platform;

import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.ApiStatus;

import java.util.ServiceLoader;

@ApiStatus.Internal
public interface SableChunkEventPlatform {

    SableChunkEventPlatform INSTANCE = ServiceLoader.load(SableChunkEventPlatform.class).findFirst().orElseThrow(() -> new RuntimeException("Failed to find sable chunk event platform"));

    void onChunkPacketReplaced(final LevelChunk chunk);

    void onOldChunkInvalid(final LevelChunk chunk);
}
