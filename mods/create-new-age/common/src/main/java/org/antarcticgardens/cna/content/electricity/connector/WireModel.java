package org.antarcticgardens.cna.content.electricity.connector;

import com.jozufozu.flywheel.api.vertex.VertexList;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.core.Formats;
import com.jozufozu.flywheel.core.model.Model;
import com.jozufozu.flywheel.core.vertex.PosTexNormalWriterUnsafe;
import org.antarcticgardens.cna.config.CNAConfig;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class WireModel implements Model {
    public static final float SAG_FACTOR = 0.92f;

    private final int vertexCount;
    private final VertexList list;

    public WireModel(Vector3f direction, int sections, float sectionLength, float thickness) {
        Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);

        if (isVertical(direction)) {
            up = new Vector3f(1.0f, 0.0f, 0.0f);
        }

        Vector3f right = new Vector3f(direction).cross(up);

        Vector3f thicknessRight = new Vector3f(right).mul(thickness / 2);
        Vector3f thicknessUp = new Vector3f(up).mul(thickness / 2);

        vertexCount = sections * 4 * 2;

        ByteBuffer buf = MemoryUtil.memAlloc(size());
        PosTexNormalWriterUnsafe writer = new PosTexNormalWriterUnsafe(Formats.POS_TEX_NORMAL, buf);

        Vector3f last = new Vector3f(0.0f);
        float lastCat = 0.0f;

        for (int i = 1; i <= sections; i++) {
            Vector3f to = new Vector3f(last).add(new Vector3f(direction).mul(sectionLength));

            float cat = catenary(i, sectionLength * sections, sections);

            float vOffset = (i % 2 == 0) ? 0.5f : 0.0f;
            addQuad(writer, last, to, thicknessRight, thicknessUp, lastCat, cat, vOffset);
            addQuad(writer, last, to, thicknessRight, new Vector3f(thicknessUp).mul(-1), lastCat, cat, vOffset);

            last = to;
            lastCat = cat;
        }

        list = writer.intoReader();
        MemoryUtil.memFree(buf);
    }

    private void addQuad(PosTexNormalWriterUnsafe writer, Vector3f from, Vector3f to, Vector3f thicknessRight, Vector3f thicknessUp, float fromCat, float toCat, float vOffset) {
        Vector3f a = new Vector3f(from).sub(thicknessRight).sub(thicknessUp).add(0.0f, fromCat, 0.0f);
        Vector3f b = new Vector3f(from).add(thicknessRight).add(thicknessUp).add(0.0f, fromCat, 0.0f);
        Vector3f c = new Vector3f(to).add(thicknessRight).add(thicknessUp).add(0.0f, toCat, 0.0f);
        Vector3f d = new Vector3f(to).sub(thicknessRight).sub(thicknessUp).add(0.0f, toCat, 0.0f);

        // I'm not gonna try to find out why, but with batching backend it only works when normal Y is 1.0f
        writer.putVertex(a.x, a.y, a.z, 0.0f, 1.0f, 0.0f, 0.0f, 0.5f + vOffset);
        writer.putVertex(b.x, b.y, b.z, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f + vOffset);
        writer.putVertex(c.x, c.y, c.z, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f + vOffset);
        writer.putVertex(d.x, d.y, d.z, 0.0f, 1.0f, 0.0f, 1.0f, 0.5f + vOffset);
    }

    private float catenary(double x, double length, int sections) {
        double a = length / CNAConfig.getCommon().maxWireLength.get() * SAG_FACTOR;
        x = (x / sections * 2 - 1);
        return (float) ((Math.cosh(x) - Math.cosh(1.0f)) * a);
    }

    private boolean isVertical(Vector3f vec) {
        Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
        return vec.equals(up, 0.001f) || vec.equals(up.mul(-1), 0.001f);
    }

    @Override
    public String name() {
        return "Diane"; // Definitely a name
    }

    @Override
    public VertexList getReader() {
        return list;
    }

    @Override
    public int vertexCount() {
        return vertexCount;
    }

    @Override
    public VertexType getType() {
        return Formats.POS_TEX_NORMAL;
    }

    @Override
    public void delete() {
        list.delete();
    }
}
