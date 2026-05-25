package dev.codinglabs.modpack.rapier_entity.dragon

import com.mojang.blaze3d.vertex.PoseStack
import dev.codinglabs.modpack.ID
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.resources.ResourceLocation

class Renderer(context: EntityRendererProvider.Context) : EntityRenderer<EnderDragonPrototype>(context) {
    override fun getTextureLocation(entity: EnderDragonPrototype): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(ID, "dummy")
    }

    /*override fun render(
        p_entity: EnderDragonPrototype,
        entityYaw: Float,
        partialTick: Float,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLight: Int
    ) {
        super.render(p_entity, entityYaw, partialTick, poseStack, bufferSource, packedLight)

        // LevelRenderer.renderLineBox()
    }*/
}
