package org.antarcticgardens.cna.rendering.fallbackInstance;

import com.jozufozu.flywheel.api.InstanceData;
import com.jozufozu.flywheel.api.struct.Batched;
import com.jozufozu.flywheel.api.vertex.VertexList;
import com.jozufozu.flywheel.backend.instancing.AbstractInstancer;
import com.jozufozu.flywheel.backend.instancing.TaskEngine;
import com.jozufozu.flywheel.backend.model.DirectVertexConsumer;
import com.jozufozu.flywheel.core.model.Model;
import com.jozufozu.flywheel.core.model.ModelTransformer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

public class FallbackInstancer<D extends InstanceData> extends AbstractInstancer<D> {
    private final Batched<D> type;
    
    public FallbackInstancer(Batched<D> type, Model modelData) {
        super(type::create, modelData);
        this.type = type;
    }

    @Override
    public void notifyDirty() {
        
    }

    public void submitTask(PoseStack poseStack, TaskEngine taskEngine, DirectVertexConsumer consumer) {
        DirectVertexConsumer s = consumer.split(getVertexCount());
        taskEngine.submit(() -> render(poseStack, s));
    }

    public void delete() {
        modelData.delete();
    }
    
    public void render(PoseStack poseStack, VertexConsumer consumer) {
        ModelTransformer.Params params = new ModelTransformer.Params();
        VertexList reader = modelData.getReader();

        for (D d : data) {
            params.loadDefault();
            params.model.set(poseStack.last().pose());
            type.transform(d, params);
            
            for (int i = 0; i < reader.getVertexCount(); i++) {
                consumer.vertex(params.model, reader.getX(i), reader.getY(i), reader.getZ(i))
                        .color(reader.getR(i), reader.getG(i), reader.getB(i), reader.getA(i))
                        .uv(reader.getU(i), reader.getV(i))
                        .uv2(params.packedLightCoords)
                        .normal(params.normal, reader.getNX(i), reader.getNY(i), reader.getNZ(i))
                        .endVertex();
            }
        }
    }

    void setup() {
        if (anyToRemove) {
            data.removeIf(InstanceData::isRemoved);
            anyToRemove = false;
        }
    }
}
