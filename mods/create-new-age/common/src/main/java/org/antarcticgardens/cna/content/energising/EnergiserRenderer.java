package org.antarcticgardens.cna.content.energising;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.render.CachedBufferer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.Blocks;

public class EnergiserRenderer implements BlockEntityRenderer<EnergiserBlockEntity> {
    public EnergiserRenderer(BlockEntityRendererProvider.Context context) {
        super();
    }
    
    @Override
    public void render(EnergiserBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (blockEntity.size > 0f && blockEntity.getLevel() != null) {
            var consumer = buffer.getBuffer(RenderType.lightning());
            float scalar = (1 - blockEntity.size * 0.12f) * 0.5f;
            var buf = CachedBufferer.block(Blocks.WHITE_CONCRETE.defaultBlockState());

            buf.color(100, 150, 200, 200)
                    .translate(scalar, -1.2, scalar)
                    .scale(blockEntity.size * 0.12f, 1.3f, blockEntity.size * 0.12f)
                    .renderInto(poseStack, consumer);
        }
    }
}
