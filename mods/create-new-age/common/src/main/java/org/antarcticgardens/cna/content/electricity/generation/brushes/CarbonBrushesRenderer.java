package org.antarcticgardens.cna.content.electricity.generation.brushes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.antarcticgardens.cna.CNAPartialModels;

public class CarbonBrushesRenderer implements BlockEntityRenderer<CarbonBrushesBlockEntity> {
    public CarbonBrushesRenderer(BlockEntityRendererProvider.Context context) {
        super();
    }

    @Override
    public void render(CarbonBrushesBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light, int packedOverlay) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.solid());
        BlockState state = blockEntity.getBlockState();
        Direction dir = state.getValue(BlockStateProperties.FACING);

        SuperByteBuffer coil = CachedBufferer.partial(CNAPartialModels.COIL, state);
        KineticBlockEntityRenderer.standardKineticRotationTransform(coil, blockEntity, light);
        rotateToAxis(coil, dir.getAxis());
        coil.renderInto(poseStack, consumer);
    }
    
    private void rotateToAxis(SuperByteBuffer buffer, Direction.Axis axis) {
        buffer.centre();
        
        switch (axis) {
            case X -> buffer.rotate(90, Direction.Axis.Z);
            case Z -> buffer.rotate(90, Direction.Axis.X);
        }
        
        buffer.unCentre();
    }
}
