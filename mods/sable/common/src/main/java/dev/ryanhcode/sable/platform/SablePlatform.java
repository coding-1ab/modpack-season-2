package dev.ryanhcode.sable.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ServiceLoader;

@ApiStatus.Internal
public interface SablePlatform {

    SablePlatform INSTANCE = ServiceLoader.load(SablePlatform.class).findFirst().orElseThrow(() -> new RuntimeException("Failed to find sable platform"));

    /**
     * Checks if the specified level is a wrapped level from Create.
     *
     * @param level The level to check
     * @return If the level is wrapped
     */
    boolean isWrappedLevel(@Nullable final Level level);

    boolean isBlockstateLadder(BlockState state, Level level, BlockPos pos, LivingEntity entity);
}
