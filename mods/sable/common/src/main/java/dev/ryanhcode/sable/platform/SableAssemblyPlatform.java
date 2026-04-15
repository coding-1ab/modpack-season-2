package dev.ryanhcode.sable.platform;

import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;

import java.util.ServiceLoader;

@ApiStatus.Internal
public interface SableAssemblyPlatform {

    SableAssemblyPlatform INSTANCE = ServiceLoader.load(SableAssemblyPlatform.class).findFirst().orElseThrow(() -> new RuntimeException("Failed to find sable assembly platform"));

    void setIgnoreOnPlace(final Level level, final boolean ignore);
}
