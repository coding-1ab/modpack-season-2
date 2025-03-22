package foundry.veil.mixin.pipeline.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import foundry.veil.ext.AutoStorageIndexBufferExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import static org.lwjgl.opengl.ARBDirectStateAccess.glVertexArrayElementBuffer;
import static org.lwjgl.opengl.GL15C.GL_ELEMENT_ARRAY_BUFFER;

@Mixin(RenderSystem.AutoStorageIndexBuffer.class)
public abstract class PipelineAutoStorageIndexBufferMixin implements AutoStorageIndexBufferExtension {

    @Shadow
    private int name;

    @Shadow
    protected abstract void ensureStorage(int neededIndexCount);

    @Shadow
    public abstract boolean hasStorage(int index);

    @Override
    public void veil$bind(int vao, int indexCount) {
        if (this.name == 0) {
            this.name = GlStateManager._glGenBuffers();
        }

        if (!this.hasStorage(indexCount)) {
            GlStateManager._glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.name);
            this.ensureStorage(indexCount);
        } else {
            glVertexArrayElementBuffer(vao, this.name);
        }
    }
}
