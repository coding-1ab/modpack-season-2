package com.kipti.bnb.content.cogwheel_chain.block;

import com.kipti.bnb.content.cogwheel_chain.graph.CogwheelChainNode;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class CogwheelChainBlockEntityRenderer extends KineticBlockEntityRenderer<CogwheelChainBlockEntity> {

    public CogwheelChainBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(CogwheelChainBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
        //For now, if controller, render an outliner between each node
        if (be.isController && be.chain != null)
            for (int i = 0; i < be.chain.getNodes().size(); i++) {
                CogwheelChainNode nodeA = be.chain.getNodes().get(i);
                CogwheelChainNode nodeB = be.chain.getNodes().get((i + 1) % be.chain.getNodes().size());

                Outliner.getInstance()
                    .showLine(nodeA, nodeA.getPosition(), nodeB.getPosition())
                    .colored(0xff00ff00)
                    .lineWidth(0.2f);
            }
    }
}
