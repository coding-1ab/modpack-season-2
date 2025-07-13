package com.kipti.bnb.content.nixie.foundation;

import com.kipti.bnb.content.nixie.large_nixie_tube.LargeNixieTubeBlock;
import com.kipti.bnb.content.nixie.nixie_board.NixieBoardBlock;
import com.kipti.bnb.mixin_accessor.FontAccess;
import com.kipti.bnb.registry.BnbBlocks;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.simibubi.create.foundation.utility.DyeHelper;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.data.Couple;
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
import net.minecraft.util.FastColor;
import net.minecraft.world.level.block.Block;
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

        //TODO remove these so many BnbBlocks.NIXIE_BOARD.is(be.getBlockState().getBlock()) || BnbBlocks.DYED_NIXIE_BOARD.contains(be.getBlockState().getBlock())
        float scale = (BnbBlocks.NIXIE_BOARD.is(be.getBlockState().getBlock()) || BnbBlocks.DYED_NIXIE_BOARD.contains(be.getBlockState().getBlock())) ? 1f / 16f : 1f / 20f;
        float offset = (BnbBlocks.NIXIE_BOARD.is(be.getBlockState().getBlock()) || BnbBlocks.DYED_NIXIE_BOARD.contains(be.getBlockState().getBlock())) ? 0 : 1f / 8f;

        ms.pushPose();
        Direction facing = be.getBlockState().getValue(NixieBoardBlock.FACING);
        Direction orientation = be.getBlockState().getValue(NixieBoardBlock.ORIENTATION);

        float orientationOffset = 0;
        boolean isLargeNixieTube = be.getBlockState().getBlock() instanceof LargeNixieTubeBlock;
        if (be.currentDisplayOption != GenericNixieDisplayBlockEntity.ConfigurableDisplayOptions.ALWAYS_UP) {
            TransformStack.of(ms)
                .center()
                .mulPose(DoubleOrientedBlockModel.getRotation(facing, orientation))
                .uncenter();
            orientationOffset = isLargeNixieTube ? - 2/16f : 0;
        } else {
            TransformStack.of(ms)
                .center()
                .mulPose(DoubleOrientedBlockModel.getRotation(Direction.UP, orientation))
                .uncenter();
            orientationOffset = - 2/16f;
        }

        ms.translate(0.5, 1f + orientationOffset, 0.5);
        ms.scale(1/16f, 1/16f, 1/16f);
        if (isLargeNixieTube) {
            ms.scale(0.85f, 0.85f, 0.85f); // Scale down for large nixie tubes
        }
        ms.scale(-1, -1, +1);

        Couple<Integer> couple = DyeHelper.getDyeColors(((DyeProviderBlock) be.getBlockState().getBlock()).getDyeColor());

        ConfigurableDisplayOptionTransform transform = be.getCurrentDisplayOption().renderTransform.get();

        transform.render(ms, be, glyph -> {
            if (glyph == ' ') {
                return; // Skip rendering spaces
            }
            int charCode = glyph;
            if (TextBlockSubAtlas.NIXIE_TEXT_SUB_ATLAS.isInCharacterSet(charCode)) {
                renderGlyphUsingSpecialFont(ms, buffer, overlay, charCode, ms.last().pose(), couple);
            } else {
                renderUsingNormalFont(ms, buffer, fontSet, charCode, ms.last().pose(), couple);
            }
        });
        ms.popPose();
    }

    private static int getTextColor(GenericNixieDisplayBlockEntity be) {
        int col = 0xffffffff;
        Block block = be.getBlockState().getBlock();
        if (block instanceof NixieBoardBlock nbb) {
            if (nbb.getDyeColor() != null) {
                col = nbb.getDyeColor().getTextureDiffuseColor() | 0x000000ff; // Ensure alpha is set
            }
        }
        if (block instanceof LargeNixieTubeBlock lnb) {
            if (lnb.getDyeColor() != null) {
                col = lnb.getDyeColor().getTextureDiffuseColor() | 0x000000ff;
            }
        }
        return col;
    }

    private static void renderGlyphUsingSpecialFont(PoseStack ms, MultiBufferSource buffer, int overlay, int glyph, Matrix4f pose, Couple<Integer> col) {
        VertexConsumer cutoutBuffer = buffer.getBuffer(RenderType.cutout());

        ms.translate(-6, -3, 0);
        TextBlockSubAtlas.Uv characterUv = TextBlockSubAtlas.NIXIE_TEXT_SUB_ATLAS.getUvForCharacter(glyph);
        float u0 = characterUv.getU0(), u1 = characterUv.getU1(),
            v0 = characterUv.getV0(), v1 = characterUv.getV1();

        int primary = FastColor.ARGB32.opaque(col.get(true));
        addVerticesForChar(overlay, cutoutBuffer, pose, u0, v1, v0, u1, primary);
        pose = pose.translate(0.5f, 0.5f, 0.1f);
        int secondary = FastColor.ARGB32.opaque(col.get(false));
        addVerticesForChar(overlay, cutoutBuffer, pose, u0, v1, v0, u1, secondary);
    }

    private static void addVerticesForChar(int overlay, VertexConsumer cutoutBuffer, Matrix4f pose, float u0, float v1, float v0, float u1, int col) {
        cutoutBuffer
            .addVertex(pose, 0, 12, 0)
            .setColor(col)
            .setUv(u0, v1)
            .setOverlay(overlay)
            .setLight(LightTexture.FULL_BRIGHT)
            .setNormal(1, 0, 0);
        cutoutBuffer
            .addVertex(pose, 0, 0, 0)
            .setColor(col)
            .setUv(u0, v0)
            .setOverlay(overlay)
            .setLight(LightTexture.FULL_BRIGHT)
            .setNormal(1, 0, 0);
        cutoutBuffer
            .addVertex(pose, 12, 0, 0)
            .setColor(col)
            .setUv(u1, v0)
            .setOverlay(overlay)
            .setLight(LightTexture.FULL_BRIGHT)
            .setNormal(1, 0, 0);
        cutoutBuffer
            .addVertex(pose, 12, 12, 0)
            .setColor(col)
            .setUv(u1, v1)
            .setOverlay(overlay)
            .setLight(LightTexture.FULL_BRIGHT)
            .setNormal(1, 0, 0);

        cutoutBuffer
            .addVertex(pose, 12, 12, 0)
            .setColor(col)
            .setUv(u1, v1)
            .setOverlay(overlay)
            .setLight(LightTexture.FULL_BRIGHT)
            .setNormal(1, 0, 0);
        cutoutBuffer
            .addVertex(pose, 12, 0, 0)
            .setColor(col)
            .setUv(u1, v0)
            .setOverlay(overlay)
            .setLight(LightTexture.FULL_BRIGHT)
            .setNormal(1, 0, 0);
        cutoutBuffer
            .addVertex(pose, 0, 0, 0)
            .setColor(col)
            .setUv(u0, v0)
            .setOverlay(overlay)
            .setLight(LightTexture.FULL_BRIGHT)
            .setNormal(1, 0, 0);
        cutoutBuffer
            .addVertex(pose, 0, 12, 0)
            .setColor(col)
            .setUv(u0, v1)
            .setOverlay(overlay)
            .setLight(LightTexture.FULL_BRIGHT)
            .setNormal(1, 0, 0);
    }

    private static void renderUsingNormalFont(PoseStack ms, MultiBufferSource buffer, FontSet fontSet, int glyph, Matrix4f pose, Couple<Integer> colours) {
        BakedGlyph bakedGlyph = fontSet.getGlyph(glyph);
        VertexConsumer vertexconsumer = buffer.getBuffer(bakedGlyph.renderType(Font.DisplayMode.NORMAL));
        float width = fontSet.getGlyphInfo(glyph, true).getAdvance(false) - 1;

        RenderSystem.disableCull();

        float r = (colours.get(true) >> 16 & 0xFF) / 255.0f;
        float g = (colours.get(true) >> 8 & 0xFF) / 255.0f;
        float b = (colours.get(true) & 0xFF) / 255.0f;
        float rSecondary = (colours.get(false) >> 16 & 0xFF) / 255.0f;
        float gSecondary = (colours.get(false) >> 8 & 0xFF) / 255.0f;
        float bSecondary = (colours.get(false) & 0xFF) / 255.0f;

        bakedGlyph.render(false, -width / 2, 0, pose, vertexconsumer, r, g, b, 1, LightTexture.FULL_BRIGHT);

        ms.pushPose();
        ms.translate(0, 0, 0.1f);
        Matrix4f backPose = ms.last().pose();
        bakedGlyph.render(false, -width / 2 + 0.5f, 0.5f, backPose, vertexconsumer,
            rSecondary, gSecondary, bSecondary,
            1, LightTexture.FULL_BRIGHT);
        ms.popPose();

        if (buffer instanceof MultiBufferSource.BufferSource bs) {
            bs.endBatch(bakedGlyph.renderType(Font.DisplayMode.NORMAL));
        }
        RenderSystem.enableCull();
    }
}
