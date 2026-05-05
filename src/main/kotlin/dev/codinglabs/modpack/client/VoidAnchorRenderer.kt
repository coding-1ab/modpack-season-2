package dev.codinglabs.modpack.client

import com.mojang.blaze3d.vertex.PoseStack
import dev.codinglabs.modpack.VoidAnchorBlockEntity
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider

class VoidAnchorRenderer(@Suppress("UNUSED_PARAMETER") context: BlockEntityRendererProvider.Context) : BlockEntityRenderer<VoidAnchorBlockEntity> {

    override fun render(
        blockEntity: VoidAnchorBlockEntity,
        partialTick: Float,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLight: Int,
        packedOverlay: Int,
    ) {

        val consumer = bufferSource.getBuffer(RenderType.endPortal())
        val pose = poseStack.last().pose()

        val min = 0.0f
        val max = 1.0f
        val y = 1.002f

        // Top face
        consumer.addVertex(pose, min, y, min)
        consumer.addVertex(pose, min, y, max)
        consumer.addVertex(pose, max, y, max)
        consumer.addVertex(pose, max, y, min)

        // Back face so it is still visible from lower camera angles.
        consumer.addVertex(pose, max, y, min)
        consumer.addVertex(pose, max, y, max)
        consumer.addVertex(pose, min, y, max)
        consumer.addVertex(pose, min, y, min)
    }
}

