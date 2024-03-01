package org.antarcticgardens.cna.rendering;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class HalfShaftRenderer extends KineticBlockEntityRenderer<KineticBlockEntity> {
    public HalfShaftRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(KineticBlockEntity blockEntity, BlockState blockState) {
        return CachedBufferer.partialFacing(AllPartialModels.SHAFT_HALF, blockState);
    }
}
