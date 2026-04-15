package dev.ryanhcode.sable.mixin.climbing_sub_levels;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.SubLevelHelper;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.platform.SablePlatform;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Allows living entities to climb ladders on sub-levels
 * <p>
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(final EntityType<?> entityType, final Level level) {
        super(entityType, level);
    }

    @Redirect(method = "onClimbable", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;blockPosition()Lnet/minecraft/core/BlockPos;"))
    private BlockPos sable$redirectPos(final LivingEntity instance) {
        final Level level = this.level();
        final BlockPos defaultPos = instance.blockPosition();

        final LivingEntity self = (LivingEntity) (Object) this;
        final BlockState defaultState = level.getBlockState(defaultPos);
        if (defaultState.is(BlockTags.CLIMBABLE) && SablePlatform.INSTANCE.isBlockstateLadder(defaultState, level, defaultPos, self)) {
            return defaultPos;
        }

        final Vector3d position = new Vector3d();
        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (final SubLevel subLevel : Sable.HELPER.getAllIntersecting(level, new BoundingBox3d(this.getBoundingBox()))) {
            subLevel.logicalPose().transformPositionInverse(JOMLConversion.toJOML(this.position(), position));
            pos.set(position.x, position.y, position.z);
            final BlockState state = level.getBlockState(pos);

            if (state.is(BlockTags.CLIMBABLE) && SablePlatform.INSTANCE.isBlockstateLadder(state, level, pos, self)) {
                return pos.immutable();
            }
        }

        return defaultPos;
    }
}
