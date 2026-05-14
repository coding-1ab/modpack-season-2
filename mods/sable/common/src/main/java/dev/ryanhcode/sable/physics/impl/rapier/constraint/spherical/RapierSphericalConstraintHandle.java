package dev.ryanhcode.sable.physics.impl.rapier.constraint.spherical;

import dev.ryanhcode.sable.api.physics.constraint.spherical.SphericalConstraintConfiguration;
import dev.ryanhcode.sable.physics.impl.rapier.Rapier3D;
import dev.ryanhcode.sable.physics.impl.rapier.constraint.RapierConstraintHandle;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

public class RapierSphericalConstraintHandle extends RapierConstraintHandle {
    public static RapierSphericalConstraintHandle create(
            final ServerLevel serverLevel,
            @Nullable final ServerSubLevel sublevelA,
            @Nullable final ServerSubLevel sublevelB,
            final SphericalConstraintConfiguration config
    ) {
        final int sceneID = Rapier3D.getID(serverLevel);

        final long handle = Rapier3D.addSphericalConstraint(
                sceneID,
                sublevelA == null ? -1 :  Rapier3D.getID(sublevelA),
                sublevelB == null ? -1 :  Rapier3D.getID(sublevelB),
                config.pos1().x(),
                config.pos1().y(),
                config.pos1().z(),
                config.pos2().x(),
                config.pos2().y(),
                config.pos2().z(),
                config.contacts()
        );

        return new RapierSphericalConstraintHandle(sceneID, handle);
    }


    /**
     * Creates a new constraint handle
     *
     * @param sceneID the scene ID that this constraint is in
     * @param handle  the handle from the physics engine
     */
    protected RapierSphericalConstraintHandle(int sceneID, long handle) {
        super(sceneID, handle);
    }
}
