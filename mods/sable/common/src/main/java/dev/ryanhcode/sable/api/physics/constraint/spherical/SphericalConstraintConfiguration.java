package dev.ryanhcode.sable.api.physics.constraint.spherical;

import dev.ryanhcode.sable.api.physics.constraint.PhysicsConstraintConfiguration;
import org.joml.Vector3dc;

/***
 * A configuration for a spherical constraint, with three DOF.
 * It's commonly used for rag-doll.
 * @param pos1 the position in world space assumed to be inside the plot of the first sub-level (ex. a block position).
 * @param pos2 the position in world space assumed to be inside the plot of the second sub-level (ex. a block position).
 * @param contacts whether or not these two bodies should collide physically.
 */
public record SphericalConstraintConfiguration(Vector3dc pos1, Vector3dc pos2, boolean contacts) implements PhysicsConstraintConfiguration<SphericalConstraintHandle> {
}
