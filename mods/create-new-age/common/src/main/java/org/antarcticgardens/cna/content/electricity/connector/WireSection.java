package org.antarcticgardens.cna.content.electricity.connector;

import com.jozufozu.flywheel.core.model.Model;
import dev.engine_room.flywheel.api.vertex.VertexList;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class WireSection implements Model {
    private static final Map<String, WireSection> cache = new HashMap<>();
    
    private final VertexList list;
    
    private final float thickness;
    private final float length;
    private final float endYOffset;
    private final float vOffset;
    
    private boolean deleted = false;
    
    public WireSection(float length, float thickness, float endYOffset, float vOffset) {
        this.thickness = thickness;
        this.length = length;
        this.endYOffset = endYOffset;
        this.vOffset = vOffset;
        
        ByteBuffer buf = MemoryUtil.memAlloc(size());
        PosTexNormalWriterUnsafe writer = new PosTexNormalWriterUnsafe(Formats.POS_TEX_NORMAL, buf);

        float ht = thickness / 2;
        
        for (int i = -1; i <= 1; i += 2) {
            writer.putVertex(ht, ht * i, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.5f + vOffset);
            writer.putVertex(-ht, -ht * i, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f + vOffset);
            writer.putVertex(-ht, -ht * i + endYOffset, length * 1.01f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f + vOffset);
            writer.putVertex(ht, ht * i + endYOffset, length * 1.01f, 0.0f, 1.0f, 0.0f, 1.0f, 0.5f + vOffset);
        }
        
        list = writer.intoReader();
        MemoryUtil.memFree(buf);
    }
    
    @Override
    public String name() {
        return thickness + "," + length + "," + endYOffset + "," + vOffset;
    }

    @Override
    public VertexList getReader() {
        return list;
    }

    @Override
    public int vertexCount() {
        return 4 * 2;
    }

    @Override
    public VertexType getType() {
        return Formats.POS_TEX_NORMAL;
    }

    @Override
    public void delete() {
        if (!deleted) {
            cache.remove(name());
            list.delete();
            deleted = true;
        }
    }
    
    public static WireSection getOrCreate(float length, float thickness, float endYOffset, float vOffset) {
        return cache.computeIfAbsent(thickness + "," + length + "," + endYOffset + "," + vOffset, 
                s -> new WireSection(length, thickness, endYOffset, vOffset));
    }
}
