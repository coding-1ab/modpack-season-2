package foundry.veil.impl.client.render.vertex;

import com.mojang.blaze3d.systems.RenderSystem;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.vertex.VertexArray;
import foundry.veil.api.client.render.vertex.VertexArrayBuilder;
import org.jetbrains.annotations.ApiStatus;

import java.util.Arrays;

import static org.lwjgl.opengl.ARBVertexAttrib64Bit.glVertexAttribLPointer;
import static org.lwjgl.opengl.GL33C.*;

@ApiStatus.Internal
public class LegacyVertexAttribBindingBuilder implements VertexArrayBuilder {

    private final VertexArray vertexArray;
    private final VertexBufferRegion[] vertexBuffers;
    private int boundIndex = -1;

    public LegacyVertexAttribBindingBuilder(VertexArray vertexArray) {
        this.vertexArray = vertexArray;
        this.vertexBuffers = new VertexBufferRegion[VeilRenderSystem.maxVertexAttributes()];
    }

    private void bindIndex(int index) {
        if (index < 0 || index >= this.vertexBuffers.length) {
            throw new IllegalArgumentException("Invalid vertex attribute index. Must be between 0 and " + (this.vertexBuffers.length - 1) + ": " + index);
        }

        if (this.boundIndex != index) {
            if (this.vertexBuffers[index] == null) {
                throw new IllegalArgumentException("No vertex buffer defined for index: " + index);
            }

            RenderSystem.glBindBuffer(GL_ARRAY_BUFFER, this.vertexBuffers[index].buffer);
            this.boundIndex = index;
        }
    }

    @Override
    public VertexArray vertexArray() {
        return this.vertexArray;
    }

    @Override
    public VertexArrayBuilder defineVertexBuffer(int index, int buffer, int offset, int stride, int divisor) {
        if (index < 0 || index >= this.vertexBuffers.length) {
            throw new IllegalArgumentException("Invalid vertex attribute index. Must be between 0 and " + (this.vertexBuffers.length - 1) + ": " + index);
        }
        this.vertexBuffers[index] = new VertexBufferRegion(buffer, offset, stride, divisor);
        return this;
    }

    @Override
    public VertexArrayBuilder setVertexAttribute(int index, int bufferIndex, int size, DataType type, boolean normalized, int relativeOffset) {
        VertexArrayBuilder.validateRelativeOffset(relativeOffset);
        this.bindIndex(bufferIndex);
        glEnableVertexAttribArray(index);
        glVertexAttribPointer(index, size, type.getGlType(), normalized, this.vertexBuffers[this.boundIndex].stride, this.vertexBuffers[this.boundIndex].offset + relativeOffset);
        glVertexAttribDivisor(index, this.vertexBuffers[this.boundIndex].divisor);
        return this;
    }

    @Override
    public VertexArrayBuilder setVertexIAttribute(int index, int bufferIndex, int size, DataType type, int relativeOffset) {
        VertexArrayBuilder.validateRelativeOffset(relativeOffset);
        this.bindIndex(bufferIndex);
        glEnableVertexAttribArray(index);
        glVertexAttribIPointer(index, size, type.getGlType(), this.vertexBuffers[this.boundIndex].stride, this.vertexBuffers[this.boundIndex].offset + relativeOffset);
        glVertexAttribDivisor(index, this.vertexBuffers[this.boundIndex].divisor);
        return this;
    }

    @Override
    public VertexArrayBuilder setVertexLAttribute(int index, int bufferIndex, int size, DataType type, int relativeOffset) {
        if (!VeilRenderSystem.vertexAttribute64BitSupported()) {
            throw new UnsupportedOperationException("Long attributes not supported");
        }
        this.bindIndex(bufferIndex);
        glEnableVertexAttribArray(index);
        glVertexAttribLPointer(index, size, type.getGlType(), this.vertexBuffers[this.boundIndex].stride, this.vertexBuffers[this.boundIndex].offset + relativeOffset);
        glVertexAttribDivisor(index, this.vertexBuffers[this.boundIndex].divisor);
        return this;
    }

    @Override
    public VertexArrayBuilder removeVertexBuffer(int index) {
        if (index < 0 || index >= this.vertexBuffers.length) {
            throw new IllegalArgumentException("Invalid vertex attribute index. Must be between 0 and " + (this.vertexBuffers.length - 1) + ": " + index);
        }
        this.vertexBuffers[index] = null;
        return this;
    }

    @Override
    public VertexArrayBuilder removeAttribute(int index) {
        glDisableVertexAttribArray(index);
        return this;
    }

    @Override
    public VertexArrayBuilder clearVertexBuffers() {
        Arrays.fill(this.vertexBuffers, null);
        return this;
    }

    @Override
    public VertexArrayBuilder clearVertexAttributes() {
        for (int i = 0; i < VeilRenderSystem.maxVertexAttributes(); i++) {
            glDisableVertexAttribArray(i);
        }
        return this;
    }

    private record VertexBufferRegion(int buffer, int offset, int stride, int divisor) {
    }
}
