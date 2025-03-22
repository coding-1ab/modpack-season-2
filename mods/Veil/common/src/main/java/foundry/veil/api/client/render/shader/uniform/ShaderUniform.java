package foundry.veil.api.client.render.shader.uniform;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.shader.program.ShaderUniformCache;
import org.jetbrains.annotations.ApiStatus;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.ARBGPUShaderFP64.*;
import static org.lwjgl.opengl.ARBSeparateShaderObjects.*;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL30C.*;

/**
 * A single uniform in a shader program.
 */
@ApiStatus.Experimental
public interface ShaderUniform extends ShaderUniformAccess {

    /**
     * Invalidates the uniform cache.
     */
    void invalidate();

    @Override
    default boolean isValid() {
        return this.getLocation() != -1;
    }

    /**
     * @return The name assigned to this uniform in the shader
     */
    String getName();

    /**
     * @return The location of the uniform in the shader
     */
    int getLocation();

    /**
     * @return The data type of this uniform in the shader
     */
    Type getType();

    // TODO getters

    enum Type {
        FLOAT(Float.BYTES) {
            @Override
            public void upload(int program, int location, ByteBuffer buffer) {
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniform1fv(program, location, buffer.asFloatBuffer());
                } else {
                    glUniform1fv(location, buffer.asFloatBuffer());
                }
            }
        },
        FLOAT_VEC2(Float.BYTES * 2) {
            @Override
            public void upload(int program, int location, ByteBuffer buffer) {
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniform2fv(program, location, buffer.asFloatBuffer());
                } else {
                    glUniform2fv(location, buffer.asFloatBuffer());
                }
            }
        },
        FLOAT_VEC3(Float.BYTES * 3) {
            @Override
            public void upload(int program, int location, ByteBuffer buffer) {
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniform3fv(program, location, buffer.asFloatBuffer());
                } else {
                    glUniform3fv(location, buffer.asFloatBuffer());
                }
            }
        },
        FLOAT_VEC4(Float.BYTES * 4) {
            @Override
            public void upload(int program, int location, ByteBuffer buffer) {
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniform4fv(program, location, buffer.asFloatBuffer());
                } else {
                    glUniform4fv(location, buffer.asFloatBuffer());
                }
            }
        },
        INT(Integer.BYTES) {
            @Override
            public void upload(int program, int location, ByteBuffer buffer) {
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniform1iv(program, location, buffer.asIntBuffer());
                } else {
                    glUniform1iv(location, buffer.asIntBuffer());
                }
            }
        },
        INT_VEC2(Integer.BYTES * 2) {
            @Override
            public void upload(int program, int location, ByteBuffer buffer) {
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniform2iv(program, location, buffer.asIntBuffer());
                } else {
                    glUniform2iv(location, buffer.asIntBuffer());
                }
            }
        },
        INT_VEC3(Integer.BYTES * 3) {
            @Override
            public void upload(int program, int location, ByteBuffer buffer) {
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniform3iv(program, location, buffer.asIntBuffer());
                } else {
                    glUniform3iv(location, buffer.asIntBuffer());
                }
            }
        },
        INT_VEC4(Integer.BYTES * 4) {
            @Override
            public void upload(int program, int location, ByteBuffer buffer) {
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniform4iv(program, location, buffer.asIntBuffer());
                } else {
                    glUniform4iv(location, buffer.asIntBuffer());
                }
            }
        },
        UNSIGNED_INT(Integer.BYTES) {
            @Override
            public void upload(int program, int location, ByteBuffer buffer) {
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniform1uiv(program, location, buffer.asIntBuffer());
                } else {
                    glUniform1uiv(location, buffer.asIntBuffer());
                }
            }
        },
        UNSIGNED_INT_VEC2(Integer.BYTES * 2) {
            @Override
            public void upload(int program, int location, ByteBuffer buffer) {
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniform2uiv(program, location, buffer.asIntBuffer());
                } else {
                    glUniform2uiv(location, buffer.asIntBuffer());
                }
            }
        },
        UNSIGNED_INT_VEC3(Integer.BYTES * 3) {
            @Override
            public void upload(int program, int location, ByteBuffer buffer) {
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniform3uiv(program, location, buffer.asIntBuffer());
                } else {
                    glUniform3uiv(location, buffer.asIntBuffer());
                }
            }
        },
        UNSIGNED_INT_VEC4(Integer.BYTES * 4) {
            @Override
            public void upload(int program, int location, ByteBuffer buffer) {
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniform4uiv(program, location, buffer.asIntBuffer());
                } else {
                    glUniform4uiv(location, buffer.asIntBuffer());
                }
            }
        },
        MATRIX2x2(Float.BYTES * 2 * 2) {
            @Override
            public void uploadAsMatrix(int program, int location, boolean transpose, ByteBuffer buffer) {
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniformMatrix2fv(program, location, transpose, buffer.asFloatBuffer());
                } else {
                    glUniformMatrix2fv(location, transpose, buffer.asFloatBuffer());
                }
            }
        },
        MATRIX3x3(Float.BYTES * 3 * 3) {
            @Override
            public void uploadAsMatrix(int program, int location, boolean transpose, ByteBuffer buffer) {
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniformMatrix3fv(program, location, transpose, buffer.asFloatBuffer());
                } else {
                    glUniformMatrix3fv(location, transpose, buffer.asFloatBuffer());
                }
            }
        },
        MATRIX4x4(Float.BYTES * 4 * 4) {
            @Override
            public void uploadAsMatrix(int program, int location, boolean transpose, ByteBuffer buffer) {
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniformMatrix4fv(program, location, transpose, buffer.asFloatBuffer());
                } else {
                    glUniformMatrix4fv(location, transpose, buffer.asFloatBuffer());
                }
            }
        },
        MATRIX2x3(Float.BYTES * 2 * 3) {
            @Override
            public void uploadAsMatrix(int program, int location, boolean transpose, ByteBuffer buffer) {
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniformMatrix2x3fv(program, location, transpose, buffer.asFloatBuffer());
                } else {
                    glUniformMatrix2x3fv(location, transpose, buffer.asFloatBuffer());
                }
            }
        },
        MATRIX3x2(Float.BYTES * 3 * 2) {
            @Override
            public void uploadAsMatrix(int program, int location, boolean transpose, ByteBuffer buffer) {
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniformMatrix3x2fv(program, location, transpose, buffer.asFloatBuffer());
                } else {
                    glUniformMatrix3x2fv(location, transpose, buffer.asFloatBuffer());
                }
            }
        },
        MATRIX2x4(Float.BYTES * 2 * 4) {
            @Override
            public void uploadAsMatrix(int program, int location, boolean transpose, ByteBuffer buffer) {
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniformMatrix2x4fv(program, location, transpose, buffer.asFloatBuffer());
                } else {
                    glUniformMatrix2x4fv(location, transpose, buffer.asFloatBuffer());
                }
            }
        },
        MATRIX4x2(Float.BYTES * 4 * 2) {
            @Override
            public void uploadAsMatrix(int program, int location, boolean transpose, ByteBuffer buffer) {
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniformMatrix4x2fv(program, location, transpose, buffer.asFloatBuffer());
                } else {
                    glUniformMatrix4x2fv(location, transpose, buffer.asFloatBuffer());
                }
            }
        },
        MATRIX3x4(Float.BYTES * 3 * 4) {
            @Override
            public void uploadAsMatrix(int program, int location, boolean transpose, ByteBuffer buffer) {
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniformMatrix3x4fv(program, location, transpose, buffer.asFloatBuffer());
                } else {
                    glUniformMatrix3x4fv(location, transpose, buffer.asFloatBuffer());
                }
            }
        },
        MATRIX4x3(Float.BYTES * 4 * 3) {
            @Override
            public void uploadAsMatrix(int program, int location, boolean transpose, ByteBuffer buffer) {
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniformMatrix4x3fv(program, location, transpose, buffer.asFloatBuffer());
                } else {
                    glUniformMatrix4x3fv(location, transpose, buffer.asFloatBuffer());
                }
            }
        },

        DOUBLE(Double.BYTES) {
            @Override
            public void upload(int program, int location, ByteBuffer buffer) {
                if (!VeilRenderSystem.gpuShaderFloat64BitSupported()) {
                    throw new UnsupportedOperationException("64-Bit Floats are not supported");
                }
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniform1dv(program, location, buffer.asDoubleBuffer());
                } else {
                    glUniform1dv(location, buffer.asDoubleBuffer());
                }
            }
        },
        DOUBLE_VEC2(Double.BYTES * 2) {
            @Override
            public void upload(int program, int location, ByteBuffer buffer) {
                if (!VeilRenderSystem.gpuShaderFloat64BitSupported()) {
                    throw new UnsupportedOperationException("64-Bit Floats are not supported");
                }
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniform2dv(program, location, buffer.asDoubleBuffer());
                } else {
                    glUniform2dv(location, buffer.asDoubleBuffer());
                }
            }
        },
        DOUBLE_VEC3(Double.BYTES * 3) {
            @Override
            public void upload(int program, int location, ByteBuffer buffer) {
                if (!VeilRenderSystem.gpuShaderFloat64BitSupported()) {
                    throw new UnsupportedOperationException("64-Bit Floats are not supported");
                }
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniform3dv(program, location, buffer.asDoubleBuffer());
                } else {
                    glUniform3dv(location, buffer.asDoubleBuffer());
                }
            }
        },
        DOUBLE_VEC4(Double.BYTES * 4) {
            @Override
            public void upload(int program, int location, ByteBuffer buffer) {
                if (!VeilRenderSystem.gpuShaderFloat64BitSupported()) {
                    throw new UnsupportedOperationException("64-Bit Floats are not supported");
                }
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniform4dv(program, location, buffer.asDoubleBuffer());
                } else {
                    glUniform4dv(location, buffer.asDoubleBuffer());
                }
            }
        },
        DOUBLE_MATRIX2x2(Double.BYTES * 2 * 2) {
            @Override
            public void uploadAsMatrix(int program, int location, boolean transpose, ByteBuffer buffer) {
                if (!VeilRenderSystem.gpuShaderFloat64BitSupported()) {
                    throw new UnsupportedOperationException("64-Bit Floats are not supported");
                }
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniformMatrix2dv(program, location, transpose, buffer.asDoubleBuffer());
                } else {
                    glUniformMatrix2dv(location, transpose, buffer.asDoubleBuffer());
                }
            }
        },
        DOUBLE_MATRIX3x3(Double.BYTES * 3 * 3) {
            @Override
            public void uploadAsMatrix(int program, int location, boolean transpose, ByteBuffer buffer) {
                if (!VeilRenderSystem.gpuShaderFloat64BitSupported()) {
                    throw new UnsupportedOperationException("64-Bit Floats are not supported");
                }
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniformMatrix3dv(program, location, transpose, buffer.asDoubleBuffer());
                } else {
                    glUniformMatrix3dv(location, transpose, buffer.asDoubleBuffer());
                }
            }
        },
        DOUBLE_MATRIX4x4(Double.BYTES * 4 * 4) {
            @Override
            public void uploadAsMatrix(int program, int location, boolean transpose, ByteBuffer buffer) {
                if (!VeilRenderSystem.gpuShaderFloat64BitSupported()) {
                    throw new UnsupportedOperationException("64-Bit Floats are not supported");
                }
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniformMatrix4dv(program, location, transpose, buffer.asDoubleBuffer());
                } else {
                    glUniformMatrix4dv(location, transpose, buffer.asDoubleBuffer());
                }
            }
        },
        DOUBLE_MATRIX2x3(Double.BYTES * 2 * 3) {
            @Override
            public void uploadAsMatrix(int program, int location, boolean transpose, ByteBuffer buffer) {
                if (!VeilRenderSystem.gpuShaderFloat64BitSupported()) {
                    throw new UnsupportedOperationException("64-Bit Floats are not supported");
                }
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniformMatrix2x3dv(program, location, transpose, buffer.asDoubleBuffer());
                } else {
                    glUniformMatrix2x3dv(location, transpose, buffer.asDoubleBuffer());
                }
            }
        },
        DOUBLE_MATRIX3x2(Double.BYTES * 3 * 2) {
            @Override
            public void uploadAsMatrix(int program, int location, boolean transpose, ByteBuffer buffer) {
                if (!VeilRenderSystem.gpuShaderFloat64BitSupported()) {
                    throw new UnsupportedOperationException("64-Bit Floats are not supported");
                }
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniformMatrix3x2dv(program, location, transpose, buffer.asDoubleBuffer());
                } else {
                    glUniformMatrix3x2dv(location, transpose, buffer.asDoubleBuffer());
                }
            }
        },
        DOUBLE_MATRIX2x4(Double.BYTES * 2 * 4) {
            @Override
            public void uploadAsMatrix(int program, int location, boolean transpose, ByteBuffer buffer) {
                if (!VeilRenderSystem.gpuShaderFloat64BitSupported()) {
                    throw new UnsupportedOperationException("64-Bit Floats are not supported");
                }
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniformMatrix2x4dv(program, location, transpose, buffer.asDoubleBuffer());
                } else {
                    glUniformMatrix2x4dv(location, transpose, buffer.asDoubleBuffer());
                }
            }
        },
        DOUBLE_MATRIX4x2(Double.BYTES * 4 * 2) {
            @Override
            public void uploadAsMatrix(int program, int location, boolean transpose, ByteBuffer buffer) {
                if (!VeilRenderSystem.gpuShaderFloat64BitSupported()) {
                    throw new UnsupportedOperationException("64-Bit Floats are not supported");
                }
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniformMatrix4x2dv(program, location, transpose, buffer.asDoubleBuffer());
                } else {
                    glUniformMatrix4x2dv(location, transpose, buffer.asDoubleBuffer());
                }
            }
        },
        DOUBLE_MATRIX3x4(Double.BYTES * 3 * 4) {
            @Override
            public void uploadAsMatrix(int program, int location, boolean transpose, ByteBuffer buffer) {
                if (!VeilRenderSystem.gpuShaderFloat64BitSupported()) {
                    throw new UnsupportedOperationException("64-Bit Floats are not supported");
                }
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniformMatrix3x4dv(program, location, transpose, buffer.asDoubleBuffer());
                } else {
                    glUniformMatrix3x4dv(location, transpose, buffer.asDoubleBuffer());
                }
            }
        },
        DOUBLE_MATRIX4x3(Double.BYTES * 4 * 3) {
            @Override
            public void uploadAsMatrix(int program, int location, boolean transpose, ByteBuffer buffer) {
                if (!VeilRenderSystem.gpuShaderFloat64BitSupported()) {
                    throw new UnsupportedOperationException("64-Bit Floats are not supported");
                }
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniformMatrix4x3dv(program, location, transpose, buffer.asDoubleBuffer());
                } else {
                    glUniformMatrix4x3dv(location, transpose, buffer.asDoubleBuffer());
                }
            }
        };

        private final int bytes;

        Type(int bytes) {
            this.bytes = bytes;
        }

        public static Type byId(int type) {
            if (ShaderUniformCache.isSampler(type)) {
                return INT;
            }
            return switch (type) {
                case GL_FLOAT -> FLOAT;
                case GL_FLOAT_VEC2 -> FLOAT_VEC2;
                case GL_FLOAT_VEC3 -> FLOAT_VEC3;
                case GL_FLOAT_VEC4 -> FLOAT_VEC4;
                case GL_INT -> INT;
                case GL_INT_VEC2 -> INT_VEC2;
                case GL_INT_VEC3 -> INT_VEC3;
                case GL_INT_VEC4 -> INT_VEC4;
                case GL_UNSIGNED_INT -> UNSIGNED_INT;
                case GL_UNSIGNED_INT_VEC2 -> UNSIGNED_INT_VEC2;
                case GL_UNSIGNED_INT_VEC3 -> UNSIGNED_INT_VEC3;
                case GL_UNSIGNED_INT_VEC4 -> UNSIGNED_INT_VEC4;
                case GL_FLOAT_MAT2 -> MATRIX2x2;
                case GL_FLOAT_MAT3 -> MATRIX3x3;
                case GL_FLOAT_MAT4 -> MATRIX4x4;
                case GL_FLOAT_MAT2x3 -> MATRIX2x3;
                case GL_FLOAT_MAT3x4 -> MATRIX3x4;
                default -> throw new AssertionError("Invalid Uniform Type: " + ShaderUniformCache.getName(type));
            };
        }

        public void upload(int program, int location, ByteBuffer buffer) {
            throw new UnsupportedOperationException();
        }

        public void uploadAsMatrix(int program, int location, boolean transpose, ByteBuffer buffer) {
            throw new UnsupportedOperationException();
        }

        public int getBytes() {
            return this.bytes;
        }
    }
}
