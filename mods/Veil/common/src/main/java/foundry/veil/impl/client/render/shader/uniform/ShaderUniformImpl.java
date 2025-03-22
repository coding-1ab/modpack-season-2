package foundry.veil.impl.client.render.shader.uniform;

import foundry.veil.api.client.render.shader.program.ShaderUniformCache;
import foundry.veil.api.client.render.shader.uniform.ShaderUniform;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.*;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeResource;

import java.lang.Math;
import java.lang.ref.Cleaner;
import java.nio.ByteBuffer;
import java.util.function.IntSupplier;

@ApiStatus.Internal
public class ShaderUniformImpl implements ShaderUniform, NativeResource {

    private final IntSupplier program;
    private final String name;

    private Type type;
    private int length;
    private ByteBuffer value;
    private int location;

    public ShaderUniformImpl(IntSupplier program, String name) {
        this.program = program;
        this.name = name;
        this.set(null);
    }

    private void upload() {
        this.type.upload(this.program.getAsInt(), this.location, this.value);
    }

    private void uploadMatrix(boolean transpose) {
        this.type.uploadAsMatrix(this.program.getAsInt(), this.location, transpose, this.value);
    }

    public void set(@Nullable ShaderUniformCache.Uniform uniform) {
        if (uniform != null) {
            this.type = Type.byId(uniform.type());
            this.length = uniform.arrayLength();
            if (this.value == null || this.value.capacity() != this.type.getBytes() * this.length) {
                this.value = MemoryUtil.memRealloc(this.value, this.type.getBytes() * this.length);
            }
            this.location = uniform.location();
            this.invalidate();
        } else {
            this.type = null;
            this.length = 0;
            this.free();
            this.location = -1;
        }
    }

    @Override
    public void invalidate() {
        if (this.value != null) {
            MemoryUtil.memSet(this.value, 0);
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getLocation() {
        return this.location;
    }

    @Override
    public Type getType() {
        return this.type;
    }

    @Override
    public void free() {
        if (this.value != null) {
            MemoryUtil.memFree(this.value);
            this.value = null;
        }
    }

    @Override
    public void setFloat(float value) {
        if (this.type != Type.FLOAT ||
            Float.floatToIntBits(this.value.getFloat(0)) == Float.floatToIntBits(value)) {
            return;
        }

        this.value.putFloat(0, value);
        this.upload();
    }

    @Override
    public void setVector(float x, float y) {
        if (this.type != Type.FLOAT_VEC2 ||
            (Float.floatToIntBits(this.value.getFloat(0)) == Float.floatToIntBits(x) &&
             Float.floatToIntBits(this.value.getFloat(4)) == Float.floatToIntBits(y))) {
            return;
        }

        this.value.putFloat(0, x);
        this.value.putFloat(4, y);
        this.upload();
    }

    @Override
    public void setVector(float x, float y, float z) {
        if (this.type != Type.FLOAT_VEC3 ||
            (Float.floatToIntBits(this.value.getFloat(0)) == Float.floatToIntBits(x) &&
             Float.floatToIntBits(this.value.getFloat(4)) == Float.floatToIntBits(y) &&
             Float.floatToIntBits(this.value.getFloat(8)) == Float.floatToIntBits(z))) {
            return;
        }

        this.value.putFloat(0, x);
        this.value.putFloat(4, y);
        this.value.putFloat(8, z);
        this.upload();
    }

    @Override
    public void setVector(float x, float y, float z, float w) {
        if (this.type != Type.FLOAT_VEC4 ||
            (Float.floatToIntBits(this.value.getFloat(0)) == Float.floatToIntBits(x) &&
             Float.floatToIntBits(this.value.getFloat(4)) == Float.floatToIntBits(y) &&
             Float.floatToIntBits(this.value.getFloat(8)) == Float.floatToIntBits(z) &&
             Float.floatToIntBits(this.value.getFloat(2)) == Float.floatToIntBits(w))) {
            return;
        }

        this.value.putFloat(0, x);
        this.value.putFloat(4, y);
        this.value.putFloat(8, z);
        this.value.putFloat(12, w);
        this.upload();
    }

    @Override
    public void setInt(int value) {
        if ((this.type != Type.INT && this.type != Type.UNSIGNED_INT) ||
            this.value.getInt(0) == value) {
            return;
        }

        this.value.putInt(0, value);
        this.upload();
    }

    @Override
    public void setVectorI(int x, int y) {
        if ((this.type != Type.INT_VEC2 && this.type != Type.UNSIGNED_INT_VEC2) ||
            (this.value.getInt(0) == x &&
             this.value.getInt(4) == y)) {
            return;
        }

        this.value.putInt(0, x);
        this.value.putInt(4, y);
        this.upload();
    }

    @Override
    public void setVectorI(int x, int y, int z) {
        if ((this.type != Type.INT_VEC3 && this.type != Type.UNSIGNED_INT_VEC3) ||
            (this.value.getInt(0) == x &&
             this.value.getInt(4) == y &&
             this.value.getInt(8) == z)) {
            return;
        }

        this.value.putInt(0, x);
        this.value.putInt(4, y);
        this.value.putInt(8, z);
        this.upload();
    }

    @Override
    public void setVectorI(int x, int y, int z, int w) {
        if ((this.type != Type.INT_VEC4 && this.type != Type.UNSIGNED_INT_VEC4) ||
            (this.value.getInt(0) == x &&
             this.value.getInt(4) == y &&
             this.value.getInt(8) == z &&
             this.value.getInt(12) == w)) {
            return;
        }

        this.value.putInt(0, x);
        this.value.putInt(4, y);
        this.value.putInt(8, z);
        this.value.putInt(12, w);
        this.upload();
    }

    @Override
    public void setFloats(float... values) {
        if (this.type != Type.FLOAT) {
            return;
        }

        int length = Math.min(this.length, values.length);
        for (int i = 0; i < length; i++) {
            if (this.value.getInt(i * 4) != Float.floatToIntBits(values[i])) {
                this.value.asFloatBuffer().put(0, values, 0, length);
                this.upload();
                break;
            }
        }
    }

    @Override
    public void setVectors(Vector2fc... values) {
        if (this.type != Type.FLOAT_VEC2) {
            return;
        }

        int length = Math.min(this.length, values.length);
        boolean changed = false;
        for (int i = 0; i < length; i++) {
            Vector2fc vector = values[i];
            if (vector.equals(this.value.getFloat(i * 8), this.value.getFloat(i * 8 + 4))) {
                changed = true;
            }
            vector.get(i * 8, this.value);
        }
        if (changed) {
            this.upload();
        }
    }

    @Override
    public void setVectors(Vector3fc... values) {
        if (this.type != Type.FLOAT_VEC3) {
            return;
        }

        int length = Math.min(this.length, values.length);
        boolean changed = false;
        for (int i = 0; i < length; i++) {
            Vector3fc vector = values[i];
            if (vector.equals(this.value.getFloat(i * 12), this.value.getFloat(i * 12 + 4), this.value.getFloat(i * 12 + 8))) {
                changed = true;
            }
            vector.get(i * 12, this.value);
        }
        if (changed) {
            this.upload();
        }
    }

    @Override
    public void setVectors(Vector4fc... values) {
        if (this.type != Type.FLOAT_VEC4) {
            return;
        }

        int length = Math.min(this.length, values.length);
        boolean changed = false;
        for (int i = 0; i < length; i++) {
            Vector4fc vector = values[i];
            if (vector.equals(this.value.getFloat(i * 16), this.value.getFloat(i * 16 + 4), this.value.getFloat(i * 16 + 8), this.value.getFloat(i * 16 + 12))) {
                changed = true;
            }
            vector.get(i * 16, this.value);
        }
        if (changed) {
            this.upload();
        }
    }

    @Override
    public void setInts(int... values) {
        if (this.type != Type.INT && this.type != Type.UNSIGNED_INT) {
            return;
        }

        int length = Math.min(this.length, values.length);
        for (int i = 0; i < length; i++) {
            if (this.value.getInt(i * 4) != values[i]) {
                this.value.asIntBuffer().put(0, values, 0, length);
                this.upload();
                break;
            }
        }
    }

    @Override
    public void setIVectors(Vector2ic... values) {
        if (this.type != Type.INT_VEC2 && this.type != Type.UNSIGNED_INT_VEC2) {
            return;
        }

        int length = Math.min(this.length, values.length);
        boolean changed = false;
        for (int i = 0; i < length; i++) {
            Vector2ic vector = values[i];
            if (vector.equals(this.value.getInt(i * 8), this.value.getInt(i * 8 + 4))) {
                changed = true;
            }
            vector.get(i * 8, this.value);
        }
        if (changed) {
            this.upload();
        }
    }

    @Override
    public void setIVectors(Vector3ic... values) {
        if (this.type != Type.INT_VEC3 && this.type != Type.UNSIGNED_INT_VEC3) {
            return;
        }

        int length = Math.min(this.length, values.length);
        boolean changed = false;
        for (int i = 0; i < length; i++) {
            Vector3ic vector = values[i];
            if (vector.equals(this.value.getInt(i * 12), this.value.getInt(i * 12 + 4), this.value.getInt(i * 12 + 8))) {
                changed = true;
            }
            vector.get(i * 12, this.value);
        }
        if (changed) {
            this.upload();
        }
    }

    @Override
    public void setIVectors(Vector4ic... values) {
        if (this.type != Type.INT_VEC4 && this.type != Type.UNSIGNED_INT_VEC4) {
            return;
        }

        int length = Math.min(this.length, values.length);
        boolean changed = false;
        for (int i = 0; i < length; i++) {
            Vector4ic vector = values[i];
            if (vector.equals(this.value.getInt(i * 16), this.value.getInt(i * 16 + 4), this.value.getInt(i * 16 + 8), this.value.getInt(i * 16 + 12))) {
                changed = true;
            }
            vector.get(i * 16, this.value);
        }
        if (changed) {
            this.upload();
        }
    }

    @Override
    public void setMatrix(Matrix2fc value, boolean transpose) {
        if (this.type != Type.MATRIX2x2) {
            return;
        }

        if (Float.floatToIntBits(this.value.getFloat(0)) == Float.floatToIntBits(value.m00()) &&
            Float.floatToIntBits(this.value.getFloat(4)) == Float.floatToIntBits(value.m01()) &&
            Float.floatToIntBits(this.value.getFloat(8)) == Float.floatToIntBits(value.m10()) &&
            Float.floatToIntBits(this.value.getFloat(12)) == Float.floatToIntBits(value.m11())) {
            return;
        }

        value.get(this.value);
        this.uploadMatrix(transpose);
    }

    @Override
    public void setMatrix(Matrix3fc value, boolean transpose) {
        if (this.type != Type.MATRIX3x3) {
            return;
        }

        if (Float.floatToIntBits(this.value.getFloat(0)) == Float.floatToIntBits(value.m00()) &&
            Float.floatToIntBits(this.value.getFloat(4)) == Float.floatToIntBits(value.m01()) &&
            Float.floatToIntBits(this.value.getFloat(8)) == Float.floatToIntBits(value.m02()) &&
            Float.floatToIntBits(this.value.getFloat(12)) == Float.floatToIntBits(value.m10()) &&
            Float.floatToIntBits(this.value.getFloat(16)) == Float.floatToIntBits(value.m11()) &&
            Float.floatToIntBits(this.value.getFloat(20)) == Float.floatToIntBits(value.m12()) &&
            Float.floatToIntBits(this.value.getFloat(24)) == Float.floatToIntBits(value.m20()) &&
            Float.floatToIntBits(this.value.getFloat(28)) == Float.floatToIntBits(value.m21()) &&
            Float.floatToIntBits(this.value.getFloat(32)) == Float.floatToIntBits(value.m22())) {
            return;
        }

        value.get(this.value);
        this.uploadMatrix(transpose);
    }

    @Override
    public void setMatrix(Matrix4fc value, boolean transpose) {
        if (this.type != Type.MATRIX4x4) {
            return;
        }

        if (Float.floatToIntBits(this.value.getFloat(0)) == Float.floatToIntBits(value.m00()) &&
            Float.floatToIntBits(this.value.getFloat(4)) == Float.floatToIntBits(value.m01()) &&
            Float.floatToIntBits(this.value.getFloat(8)) == Float.floatToIntBits(value.m02()) &&
            Float.floatToIntBits(this.value.getFloat(12)) == Float.floatToIntBits(value.m03()) &&
            Float.floatToIntBits(this.value.getFloat(16)) == Float.floatToIntBits(value.m10()) &&
            Float.floatToIntBits(this.value.getFloat(20)) == Float.floatToIntBits(value.m11()) &&
            Float.floatToIntBits(this.value.getFloat(24)) == Float.floatToIntBits(value.m12()) &&
            Float.floatToIntBits(this.value.getFloat(28)) == Float.floatToIntBits(value.m13()) &&
            Float.floatToIntBits(this.value.getFloat(32)) == Float.floatToIntBits(value.m20()) &&
            Float.floatToIntBits(this.value.getFloat(36)) == Float.floatToIntBits(value.m21()) &&
            Float.floatToIntBits(this.value.getFloat(40)) == Float.floatToIntBits(value.m22()) &&
            Float.floatToIntBits(this.value.getFloat(44)) == Float.floatToIntBits(value.m23()) &&
            Float.floatToIntBits(this.value.getFloat(48)) == Float.floatToIntBits(value.m30()) &&
            Float.floatToIntBits(this.value.getFloat(52)) == Float.floatToIntBits(value.m31()) &&
            Float.floatToIntBits(this.value.getFloat(56)) == Float.floatToIntBits(value.m32()) &&
            Float.floatToIntBits(this.value.getFloat(60)) == Float.floatToIntBits(value.m33())) {
            return;
        }

        value.get(this.value);
        this.uploadMatrix(transpose);
    }

    // TODO finish these

    @Override
    public void setMatrix(Matrix3x2fc value, boolean transpose) {
    }

    @Override
    public void setMatrix(Matrix4x3fc value, boolean transpose) {
    }

    @Override
    public void setMatrix(Matrix2dc value, boolean transpose) {
        if (this.type != Type.MATRIX2x2) {
            return;
        }

        if (Double.doubleToLongBits(this.value.getDouble(0)) == Double.doubleToLongBits(value.m00()) &&
            Double.doubleToLongBits(this.value.getDouble(8)) == Double.doubleToLongBits(value.m01()) &&
            Double.doubleToLongBits(this.value.getDouble(16)) == Double.doubleToLongBits(value.m10()) &&
            Double.doubleToLongBits(this.value.getDouble(24)) == Double.doubleToLongBits(value.m11())) {
            return;
        }

        value.get(this.value);
        this.uploadMatrix(transpose);
    }

    @Override
    public void setMatrix(Matrix3dc value, boolean transpose) {
        if (this.type != Type.MATRIX3x3) {
            return;
        }

        if (Double.doubleToLongBits(this.value.getDouble(0)) == Double.doubleToLongBits(value.m00()) &&
            Double.doubleToLongBits(this.value.getDouble(8)) == Double.doubleToLongBits(value.m01()) &&
            Double.doubleToLongBits(this.value.getDouble(16)) == Double.doubleToLongBits(value.m02()) &&
            Double.doubleToLongBits(this.value.getDouble(24)) == Double.doubleToLongBits(value.m10()) &&
            Double.doubleToLongBits(this.value.getDouble(32)) == Double.doubleToLongBits(value.m11()) &&
            Double.doubleToLongBits(this.value.getDouble(40)) == Double.doubleToLongBits(value.m12()) &&
            Double.doubleToLongBits(this.value.getDouble(48)) == Double.doubleToLongBits(value.m20()) &&
            Double.doubleToLongBits(this.value.getDouble(56)) == Double.doubleToLongBits(value.m21()) &&
            Double.doubleToLongBits(this.value.getDouble(64)) == Double.doubleToLongBits(value.m22())) {
            return;
        }

        value.get(this.value);
        this.uploadMatrix(transpose);
    }

    @Override
    public void setMatrix(Matrix4dc value, boolean transpose) {
        if (this.type != Type.DOUBLE_MATRIX4x4) {
            return;
        }

        if (Double.doubleToLongBits(this.value.getDouble(0)) == Double.doubleToLongBits(value.m00()) &&
            Double.doubleToLongBits(this.value.getDouble(8)) == Double.doubleToLongBits(value.m01()) &&
            Double.doubleToLongBits(this.value.getDouble(16)) == Double.doubleToLongBits(value.m02()) &&
            Double.doubleToLongBits(this.value.getDouble(24)) == Double.doubleToLongBits(value.m03()) &&
            Double.doubleToLongBits(this.value.getDouble(32)) == Double.doubleToLongBits(value.m10()) &&
            Double.doubleToLongBits(this.value.getDouble(40)) == Double.doubleToLongBits(value.m11()) &&
            Double.doubleToLongBits(this.value.getDouble(48)) == Double.doubleToLongBits(value.m12()) &&
            Double.doubleToLongBits(this.value.getDouble(56)) == Double.doubleToLongBits(value.m13()) &&
            Double.doubleToLongBits(this.value.getDouble(64)) == Double.doubleToLongBits(value.m20()) &&
            Double.doubleToLongBits(this.value.getDouble(72)) == Double.doubleToLongBits(value.m21()) &&
            Double.doubleToLongBits(this.value.getDouble(80)) == Double.doubleToLongBits(value.m22()) &&
            Double.doubleToLongBits(this.value.getDouble(88)) == Double.doubleToLongBits(value.m23()) &&
            Double.doubleToLongBits(this.value.getDouble(96)) == Double.doubleToLongBits(value.m30()) &&
            Double.doubleToLongBits(this.value.getDouble(104)) == Double.doubleToLongBits(value.m31()) &&
            Double.doubleToLongBits(this.value.getDouble(112)) == Double.doubleToLongBits(value.m32()) &&
            Double.doubleToLongBits(this.value.getDouble(120)) == Double.doubleToLongBits(value.m33())) {
            return;
        }

        value.get(this.value);
        this.uploadMatrix(transpose);
    }

    @Override
    public void setMatrix(Matrix3x2dc value, boolean transpose) {
    }

    @Override
    public void setMatrix(Matrix4x3dc value, boolean transpose) {

    }
}
