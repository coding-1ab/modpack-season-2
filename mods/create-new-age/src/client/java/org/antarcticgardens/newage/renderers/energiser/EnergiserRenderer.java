package org.antarcticgardens.newage.renderers.energiser;

import com.jozufozu.flywheel.backend.Backend;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import org.antarcticgardens.newage.content.energiser.EnergiserBlockEntity;

public class EnergiserRenderer extends KineticBlockEntityRenderer<EnergiserBlockEntity> {
    public EnergiserRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(EnergiserBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

        if (Backend.canUseInstancing(be.getLevel()))
            return;


    }

    @Override
    protected BlockState getRenderedBlockState(EnergiserBlockEntity be) {
        return shaft(getRotationAxisOf(be));
    }
}
