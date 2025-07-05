package com.kipti.bnb.content.nixie.foundation;

import com.kipti.bnb.content.nixie.nixie_board.NixieBoardBlock;
import com.kipti.bnb.mixin_accessor.FontAccess;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class GenericNixieDisplayBoardRenderer extends SmartBlockEntityRenderer<GenericNixieDisplayBlockEntity> {

    public GenericNixieDisplayBoardRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(GenericNixieDisplayBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

        Font fontRenderer = Minecraft.getInstance().font;
        FontSet fontSet = ((FontAccess) fontRenderer).bits_n_bobs$getFontSet(ResourceLocation.withDefaultNamespace("default"));
        if (fontSet == null) {
            return; // No font set available, nothing to render
        }

        float scale = 1 / 16f;

        ms.pushPose();
        Direction facing = be.getBlockState().getValue(NixieBoardBlock.FACING);
        Direction orientation = be.getBlockState().getValue(NixieBoardBlock.ORIENTATION);

        TransformStack.of(ms)
            .center()
            .rotate(DoubleOrientedBlockModel.getRotation(facing, orientation))
            .rotate(facing.getRotation())
            .uncenter();

        ms.translate(0.5, 1 - 1/16f, 0.5);
        ms.scale(scale, scale, scale);
        ms.scale(-1, -1, 1);

        float r = 1f, g = 1f, b = 1f;

        Matrix4f pose = ms.last().pose();

        int glyph = be.getCurrentChar();

        if (TextBlockSubAtlas.NIXIE_TEXT_SUB_ATLAS.isInCharacterSet(glyph))
            renderGlyphUsingSpecialFont(ms, buffer, overlay, glyph, pose, r, g, b);
        else
            renderUsingNormalFont(ms, buffer, fontSet, glyph, pose, r, g, b);

        ms.popPose();
    }

    private static void renderGlyphUsingSpecialFont(PoseStack ms, MultiBufferSource buffer, int overlay, int glyph, Matrix4f pose, float r, float g, float b) {
        VertexConsumer cutoutBuffer = buffer.getBuffer(RenderType.cutout());

        ms.translate(-6, -3, 0);
        TextBlockSubAtlas.Uv characterUv = TextBlockSubAtlas.NIXIE_TEXT_SUB_ATLAS.getUvForCharacter(glyph);
        float u0 = characterUv.getU0(), u1 = characterUv.getU1(),
            v0 = characterUv.getV0(), v1 = characterUv.getV1();

        addVerticesForChar(overlay, cutoutBuffer, pose, u0, v1, v0, u1, r, g, b);
        pose = pose.translate(0.5f, 0.5f, 0.1f);
        addVerticesForChar(overlay, cutoutBuffer, pose, u0, v1, v0, u1,
            Math.clamp(r * 0.65f, 0, 1),
            Math.clamp(g * 0.65f, 0, 1),
            Math.clamp(b * 0.65f, 0, 1));
    }

    private static void addVerticesForChar(int overlay, VertexConsumer cutoutBuffer, Matrix4f pose, float u0, float v1, float v0, float u1, float r, float g, float b) {
        cutoutBuffer
            .addVertex(pose, 0, 12, 0)
            .setColor(r, g, b, 1)
            .setUv(u0, v1)
            .setOverlay(overlay)
            .setLight(LightTexture.FULL_BRIGHT)
            .setNormal(1, 0, 0);
        cutoutBuffer
            .addVertex(pose, 0, 0, 0)
            .setColor(r, g, b, 1)
            .setUv(u0, v0)
            .setOverlay(overlay)
            .setLight(LightTexture.FULL_BRIGHT)
            .setNormal(1, 0, 0);
        cutoutBuffer
            .addVertex(pose, 12, 0, 0)
            .setColor(r, g, b, 1)
            .setUv(u1, v0)
            .setOverlay(overlay)
            .setLight(LightTexture.FULL_BRIGHT)
            .setNormal(1, 0, 0);
        cutoutBuffer
            .addVertex(pose, 12, 12, 0)
            .setColor(r, g, b, 1)
            .setUv(u1, v1)
            .setOverlay(overlay)
            .setLight(LightTexture.FULL_BRIGHT)
            .setNormal(1, 0, 0);

        cutoutBuffer
            .addVertex(pose, 12, 12, 0)
            .setColor(r, g, b, 1)
            .setUv(u1, v1)
            .setOverlay(overlay)
            .setLight(LightTexture.FULL_BRIGHT)
            .setNormal(1, 0, 0);
        cutoutBuffer
            .addVertex(pose, 12, 0, 0)
            .setColor(r, g, b, 1)
            .setUv(u1, v0)
            .setOverlay(overlay)
            .setLight(LightTexture.FULL_BRIGHT)
            .setNormal(1, 0, 0);
        cutoutBuffer
            .addVertex(pose, 0, 0, 0)
            .setColor(r, g, b, 1)
            .setUv(u0, v0)
            .setOverlay(overlay)
            .setLight(LightTexture.FULL_BRIGHT)
            .setNormal(1, 0, 0);
        cutoutBuffer
            .addVertex(pose, 0, 12, 0)
            .setColor(r, g, b, 1)
            .setUv(u0, v1)
            .setOverlay(overlay)
            .setLight(LightTexture.FULL_BRIGHT)
            .setNormal(1, 0, 0);
    }

    private static void renderUsingNormalFont(PoseStack ms, MultiBufferSource buffer, FontSet fontSet, int glyph, Matrix4f pose, float r, float g, float b) {
        BakedGlyph bakedGlyph = fontSet.getGlyph(glyph);
        VertexConsumer vertexconsumer = buffer.getBuffer(bakedGlyph.renderType(Font.DisplayMode.NORMAL));
        float width = fontSet.getGlyphInfo(glyph, true).getAdvance(false) - 1;

        RenderSystem.disableCull();

        bakedGlyph.render(false, -width / 2, 0, pose, vertexconsumer, r, g, b, 1, LightTexture.FULL_BRIGHT);

        ms.pushPose();
        ms.translate(0, 0, 0.1f);
        Matrix4f backPose = ms.last().pose();
        bakedGlyph.render(false, -width / 2 + 0.5f, 0.5f, backPose, vertexconsumer,
            Math.clamp(r * 0.65f, 0, 1),
            Math.clamp(g * 0.65f, 0, 1),
            Math.clamp(b * 0.65f, 0, 1),
            1, LightTexture.FULL_BRIGHT);
        ms.popPose();

        if (buffer instanceof MultiBufferSource.BufferSource bs) {
            bs.endBatch(bakedGlyph.renderType(Font.DisplayMode.NORMAL));
        }
        RenderSystem.enableCull();
    }
}
