package org.antarcticgardens.cna.content.electricity.connector;

import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.vertex.VertexList;
import dev.engine_room.flywheel.lib.vertex.PosTexNormalVertexView;
import org.joml.Vector4fc;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WireSection implements Model {
    private static final Map<String, WireSection> cache = new HashMap<>();
    
    private final VertexList list;

    private final float thickness;
    private final float length;
    private final float endYOffset;
    private final float vOffset;
//
    private boolean deleted = false;
    
    public WireSection(float length, float thickness, float endYOffset, float vOffset) {
        this.thickness = thickness;
        this.length = length;
        this.endYOffset = endYOffset;
        this.vOffset = vOffset;

//        ByteBuffer buf = MemoryUtil.memAlloc();
        PosTexNormalVertexView writer = new PosTexNormalVertexView();

        float ht = thickness / 2;

//        for (int i = -1; i <= 1; i += 2) {
//            writer.putVertex(ht, ht * i, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.5f + vOffset);
//            writer.putVertex(-ht, -ht * i, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f + vOffset);
//            writer.putVertex(-ht, -ht * i + endYOffset, length * 1.01f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f + vOffset);
//            writer.putVertex(ht, ht * i + endYOffset, length * 1.01f, 0.0f, 1.0f, 0.0f, 1.0f, 0.5f + vOffset);
//        }

        list = writer;
//        MemoryUtil.memFree(buf);
    }


    public String name() {
        return thickness + "," + length + "," + endYOffset + "," + vOffset;
    }


    public VertexList getReader() {
        return list;
    }

    public static WireSection getOrCreate(float length, float thickness, float endYOffset, float vOffset) {
        return cache.computeIfAbsent(thickness + "," + length + "," + endYOffset + "," + vOffset, 
                s -> new WireSection(length, thickness, endYOffset, vOffset));
    }

    @Override
    public List<ConfiguredMesh> meshes() {
        return List.of();
    }

    @Override
    public Vector4fc boundingSphere() {
        return null;
    }
}
