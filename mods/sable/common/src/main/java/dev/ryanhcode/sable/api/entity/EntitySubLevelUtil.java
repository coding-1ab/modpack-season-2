package dev.ryanhcode.sable.api.entity;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.SubLevelHelper;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.index.SableTags;
import dev.ryanhcode.sable.mixinterface.entity.entity_sublevel_collision.EntityMovementExtension;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaterniondc;
import org.joml.Vector3d;

import java.util.UUID;

/**
 * Utility for operations regarding entities and sub-levels
 */
public class EntitySubLevelUtil {

    /**
     * Queries the sub-level an entity is currently "tracking", if any.
     * Entities move with their tracking sub-levels, are networked in the local frame of them,
     * and log out with tracking points located in them.
     *
     * @param entity the entity to query the tracking sub-level of
     * @return the sub-level that the entity is tracking, if any
     */
    public static @Nullable SubLevel getTrackingSubLevel(final Entity entity) {
        return ((EntityMovementExtension) entity).sable$getTrackingSubLevel();
    }

    public static @Nullable SubLevel getLastTrackingSubLevel(final Entity entity) {
        final UUID uuid = ((EntityMovementExtension) entity).sable$getLastTrackingSubLevelID();
        if(uuid != null) {
            final SubLevelContainer container = SubLevelContainer.getContainer(entity.level());
            return container.getSubLevel(uuid);
        }
        return null;
    }

    /**
     * Queries the sub-level an entity is currently tracking, or the vehicle sub-level.
     *
     * @param entity the entity to query
     * @return the sub-level that the entity is tracking or the passenger of, if any
     */
    public static @Nullable SubLevel getTrackingOrVehicleSubLevel(final Entity entity) {
        SubLevel trackingSubLevel = getTrackingSubLevel(entity);

        if (trackingSubLevel == null) {
            trackingSubLevel = getVehicleSubLevel(entity);
        }

        return trackingSubLevel;
    }

    /**
     * @param entity the entity to query
     * @return the sub-level that the entity is passenger of, if any
     */
    public static @Nullable SubLevel getVehicleSubLevel(final Entity entity) {
        if (entity.getVehicle() != null) {
            return Sable.HELPER.getContaining(entity.getVehicle());
        }

        return null;
    }

    /**
     * Sets the old pos of an entity for no apparent movement, taking their tracking sub-level
     * into account.
     *
     * @param entity the entity to set the old pos of
     */
    public static void setOldPosNoMovement(final Entity entity) {
        final SubLevel trackingSubLevel = getTrackingSubLevel(entity);

        if (trackingSubLevel != null) {
            final Vec3 entityPos = entity.position();
            final Vec3 oldPos = trackingSubLevel.lastPose().transformPosition(trackingSubLevel.logicalPose().transformPositionInverse(entityPos));

            entity.xOld = oldPos.x;
            entity.xo = oldPos.x;
            entity.yOld = oldPos.y;
            entity.yo = oldPos.y;
            entity.zOld = oldPos.z;
            entity.zo = oldPos.z;
        } else {
            entity.xOld = entity.getX();
            entity.xo = entity.getX();
            entity.yOld = entity.getY();
            entity.yo = entity.getY();
            entity.zOld = entity.getZ();
            entity.zo = entity.getZ();
        }
    }

    /**
     * Gets the interpolated eye position of an entity, taking their tracking sub-level into account.
     */
    public static Vec3 getEyePositionInterpolated(final Entity entity, final float partialTicks) {
        final SubLevel trackingSubLevel = getTrackingOrVehicleSubLevel(entity);

        if (trackingSubLevel instanceof final ClientSubLevel clientSubLevel) {
            final Vector3d startPos = new Vector3d(entity.xo, entity.yo + entity.getEyeHeight(), entity.zo);
            final Vector3d endPos = new Vector3d(entity.getX(), entity.getY() + entity.getEyeHeight(), entity.getZ());

            final Pose3dc renderPose = clientSubLevel.renderPose(partialTicks);
            clientSubLevel.lastPose().transformPositionInverse(startPos);
            clientSubLevel.logicalPose().transformPositionInverse(endPos);

            startPos.lerp(endPos, partialTicks);
            renderPose.transformPosition(startPos);

            return new Vec3(startPos.x, startPos.y, startPos.z);
        } else {
            return entity.getEyePosition(partialTicks);
        }
    }

    /**
     * Kicks an entity out of a sub-level, including velocity and position.
     *
     * @param subLevel The sub-level to kick the entity out of
     * @param entity   The entity to kick
     */
    public static void kickEntity(final SubLevel subLevel, final Entity entity) {
        final Vector3d subLevelGainedVelo = new Vector3d();
		if (entity instanceof final AbstractHurtingProjectile ahp && ahp.accelerationPower == 0) {
			Sable.HELPER.getVelocity(entity.level(), JOMLConversion.toJOML(entity.position()), subLevelGainedVelo);
		}

	    // convert from m/s to m/t
        subLevelGainedVelo.mul(1.0 / 20.0);

        final Vec3 pos = entity.position();
        Vec3 anchor = Vec3.ZERO;

        if (entity instanceof FallingBlockEntity) {
            anchor = new Vec3(0.0, entity.getBbHeight() / 2.0, 0.0);
        }

        entity.moveTo(subLevel.logicalPose().transformPosition(pos.add(anchor)).subtract(anchor));
        entity.setDeltaMovement(subLevel.logicalPose().transformNormal(entity.getDeltaMovement()).add(subLevelGainedVelo.x, subLevelGainedVelo.y, subLevelGainedVelo.z));
        entity.lookAt(EntityAnchorArgument.Anchor.FEET, subLevel.logicalPose().transformNormal(entity.getLookAngle()).add(entity.position()));

        // Arrows use an incorrect Y and X rotation
        if (entity instanceof AbstractArrow) {
            final Vec3 deltaMovement = entity.getDeltaMovement();
            final double horizontal = deltaMovement.horizontalDistance();
            entity.setYRot((float) (Mth.atan2(deltaMovement.x, deltaMovement.z) * 180.0 / (float) Math.PI));
            entity.setXRot((float) (Mth.atan2(deltaMovement.y, horizontal) * 180.0 / (float) Math.PI));
        }
    }

    public static boolean shouldKick(final Entity entity) {
        return !entity.getType().is(SableTags.RETAIN_IN_SUB_LEVEL);
    }

    @Nullable
    public static Quaterniondc getCustomEntityOrientation(final Entity entity, final float partialTicks) {
        return null;
    }

    public static boolean hasCustomEntityOrientation(final Entity entity) {
        return false;
    }

    public static @NotNull Vec3 getFeetPos(final Entity entity, final float distanceDown) {
        final Quaterniondc orientation = getCustomEntityOrientation(entity, 1.0f);

        return getFeetPos(entity, distanceDown, orientation);
    }

    public static @NotNull Vec3 getFeetPos(final Entity entity, final float distanceDown, final Quaterniondc orientation) {
        final Vec3 feetPos;

        if (orientation == null) {
            feetPos = entity.position().subtract(0.0, distanceDown, 0.0);
        } else {
            feetPos = entity.getEyePosition().subtract(JOMLConversion.toMojang(
                    orientation.transform(new Vector3d(0.0, distanceDown + entity.getEyeHeight(), 0.0))
            ));
        }
        return feetPos;
    }
}
