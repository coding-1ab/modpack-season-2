package foundry.veil.api.client.render.shader.program;

import org.jetbrains.annotations.ApiStatus;

/**
 * Provides write access to all uniform variables in a shader program.
 *
 * @author Ocelot
 * @deprecated Use {@link UniformAccess} instead
 */
@ApiStatus.ScheduledForRemoval(inVersion = "4.0.0")
@Deprecated
public interface MutableUniformAccess extends UniformAccess {
}
