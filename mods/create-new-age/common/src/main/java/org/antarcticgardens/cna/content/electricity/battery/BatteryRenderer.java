package org.antarcticgardens.cna.content.electricity.battery;

import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.utility.Iterate;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class BatteryRenderer extends SafeBlockEntityRenderer<BatteryBlockEntity> {
    public BatteryRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    protected void renderSafe(BatteryBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        if (be.isController()) {
            BlockState blockState = be.getBlockState();
            VertexConsumer vb = buffer.getBuffer(RenderType.solid());
            ms.pushPose();
            TransformStack msr = TransformStack.cast(ms);
            msr.translate(be.getWidth() / 2f, 0.5, be.getWidth() / 2f);

            float dialPivot = 5.75f / 16;
            float progress = be.gauge.getValue(partialTicks);

            for (Direction d : Iterate.horizontalDirections) {
                ms.pushPose();
                CachedBufferer.partial(AllPartialModels.BOILER_GAUGE, blockState)
                        .rotateY(d.toYRot())
                        .unCentre()
                        .translate(be.getWidth() / 2f - 6 / 16f, 0, 0)
                        .light(light)
                        .renderInto(ms, vb);
                CachedBufferer.partial(AllPartialModels.BOILER_GAUGE_DIAL, blockState)
                        .rotateY(d.toYRot())
                        .unCentre()
                        .translate(be.getWidth() / 2f - 6 / 16f, 0, 0)
                        .translate(0, dialPivot, dialPivot)
                        .rotateX(-90 * progress)
                        .translate(0, -dialPivot, -dialPivot)
                        .light(light)
                        .renderInto(ms, vb);
                ms.popPose();
            }

            ms.popPose();
        }
    }
}
