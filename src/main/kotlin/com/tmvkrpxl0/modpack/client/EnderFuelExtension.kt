package com.tmvkrpxl0.modpack.client

import com.mojang.blaze3d.shaders.FogShape
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.*
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.renderer.FogRenderer
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.FastColor
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions
import org.joml.Matrix4f
import org.joml.Vector3f

object EnderFuelExtension: IClientFluidTypeExtensions {
    private val stillTexture = ResourceLocation.parse("minecraft:block/water_still")
    private val flowingTexture = ResourceLocation.parse("minecraft:block/water_flow")
    private val overlayTexture = ResourceLocation.parse("minecraft:block/water_overlay")
    private val portalTexture = ResourceLocation.parse("minecraft:textures/entity/end_portal.png")

    override fun getTintColor(): Int {
        return FastColor.ARGB32.color(0xEF, 0x00, 0x08, 0x30)
    }

    override fun modifyFogColor(
        camera: Camera,
        partialTick: Float,
        level: ClientLevel,
        renderDistance: Int,
        darkenWorldAmount: Float,
        fluidFogColor: Vector3f
    ): Vector3f {
        return Vector3f(0.0F, 0.03F, 0.2F).mul(super.modifyFogColor(camera, partialTick, level, renderDistance, darkenWorldAmount, fluidFogColor))
    }

    override fun modifyFogRender(
        camera: Camera,
        mode: FogRenderer.FogMode,
        renderDistance: Float,
        partialTick: Float,
        nearDistance: Float,
        farDistance: Float,
        shape: FogShape
    ) {
        val fogStart = -8.0f
        var fogEnd = 16.0f

        if (fogEnd > farDistance) {
            fogEnd = farDistance
        }

        RenderSystem.setShaderFogStart(fogStart)
        RenderSystem.setShaderFogEnd(fogEnd)
        RenderSystem.setShaderFogShape(shape)
    }

    override fun getStillTexture(): ResourceLocation = stillTexture
    override fun getFlowingTexture(): ResourceLocation = flowingTexture
    override fun getOverlayTexture(): ResourceLocation = overlayTexture
    override fun renderOverlay(mc: Minecraft, poseStack: PoseStack) {
        val player = mc.player ?: return
        RenderSystem.setShader(GameRenderer::getPositionTexShader)
        RenderSystem.setShaderTexture(0, portalTexture)
        RenderSystem.enableBlend()
        RenderSystem.setShaderColor(0.0F, 0.03F, 0.2F, 0.7f)
        val f7: Float = -player.yRot / 64.0f
        val f8: Float = player.xRot / 64.0f
        val matrix4f: Matrix4f = poseStack.last().pose()
        val buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX)
        buffer.addVertex(matrix4f, -1.0f, -1.0f, -0.5f).setUv(4.0f + f7, 4.0f + f8)
        buffer.addVertex(matrix4f, 1.0f, -1.0f, -0.5f).setUv(0.0f + f7, 4.0f + f8)
        buffer.addVertex(matrix4f, 1.0f, 1.0f, -0.5f).setUv(0.0f + f7, 0.0f + f8)
        buffer.addVertex(matrix4f, -1.0f, 1.0f, -0.5f).setUv(4.0f + f7, 0.0f + f8)
        BufferUploader.drawWithShader(buffer.buildOrThrow())
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
        RenderSystem.disableBlend()
    }
}
