package dev.propulsionteam.propulsionsimulated.content.thruster;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import dev.propulsionteam.propulsionsimulated.content.thruster.vector_thruster.VectorRedstoneLinkRenderer;
import dev.propulsionteam.propulsionsimulated.content.thruster.vector_thruster.VectorThrusterDebugRenderer;
import dev.propulsionteam.propulsionsimulated.content.thruster.vector_thruster.VectorThrusterRenderer;
import dev.propulsionteam.propulsionsimulated.content.thruster.vector_thruster.VectorThrusterBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class IonThrusterRenderer extends SmartBlockEntityRenderer<IonThrusterBlockEntity> {

    public IonThrusterRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(IonThrusterBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
            int light, int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
        VectorThrusterDebugRenderer.render(be);
        if (be instanceof VectorThrusterBlockEntity vector) {
            VectorThrusterRenderer.render(vector, partialTicks, ms, buffer, light, overlay);
            VectorRedstoneLinkRenderer.renderOnBlockEntity(vector, partialTicks, ms, buffer, light, overlay);
        }
    }
}
