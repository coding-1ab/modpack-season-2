package com.kipti.bnb.content.flywheel_bearing;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class FlywheelBearingBlockEntityRenderer extends KineticBlockEntityRenderer<FlywheelBearingBlockEntity> {
    public FlywheelBearingBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(final FlywheelBearingBlockEntity be, final BlockState state) {
        return CachedBuffers.partialFacing(AllPartialModels.SHAFTLESS_LARGE_COGWHEEL, state);
    }
}
