package foundry.veil.api.client.render.shader.program;

/**
 * Provides write access to all uniform variables in a shader program.
 *
 * @author Ocelot
 */
public interface MutableUniformAccess extends UniformAccess {

    /**
     * Sets the binding to use for the specified uniform block.
     *
     * @param name    The name of the block to set
     * @param binding The binding to use for that block
     */
    void setUniformBlock(CharSequence name, int binding);

    /**
     * Sets the binding to use for the specified storage block.
     *
     * @param name    The name of the block to set
     * @param binding The binding to use for that block
     */
    void setStorageBlock(CharSequence name, int binding);
}
