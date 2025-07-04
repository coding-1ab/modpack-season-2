package com.kipti.bnb.content.chair;

import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.registry.BnbPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.model.BakedQuadHelper;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class ChairModelBuilder extends BakedModelWrapper<BakedModel> {

    private static final ModelProperty<ChairModelData> CHAIR_PROPERTY = new ModelProperty<>();

    public ChairModelBuilder(BakedModel originalModel) {
        super(originalModel);
    }

    @Override
    public @NotNull ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
        ChairModelData data = new ChairModelData();

        if (!(state.getBlock() instanceof ChairBlock chairBlock)) {
            return super.getModelData(level, pos, state, modelData);
        }
        data.setData(state.getValue(ChairBlock.LEFT_ARM), state.getValue(ChairBlock.RIGHT_ARM), chairBlock.getColor());

        return ModelData.builder()
            .with(CHAIR_PROPERTY, data)
            .build();
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData data, RenderType renderType) {
        if (data.has(CHAIR_PROPERTY)) {
            List<BakedQuad> model = new ArrayList<>();

            ChairModelData modelData = data.get(CHAIR_PROPERTY);
            assert modelData != null;

            PoseStack transform = new PoseStack();

            TransformStack.of(transform)
                .center()
                .rotateY((float) ((state.getValue(ChairBlock.FACING).get2DDataValue() - 2 * 90) * (Math.PI / 180f)))
                .uncenter();

            addModelQuads(super.getQuads(state, side, rand, data, renderType), state, side, rand, data, renderType, model, transform, modelData);
            if (modelData.isLeftArm())
                addModelQuads(BnbPartialModels.CHAIR_LEFT_ARM, state, side, rand, data, renderType, model, transform, modelData);
            if (modelData.isRightArm())
                addModelQuads(BnbPartialModels.CHAIR_RIGHT_ARM, state, side, rand, data, renderType, model, transform, modelData);

            return model;
        }
        return Collections.emptyList();
    }

    private void addModelQuads(PartialModel partialModel, BlockState state, Direction side, RandomSource rand, ModelData data, RenderType renderType, List<BakedQuad> model, PoseStack transform, ChairModelData modelData) {
        addModelQuads(partialModel.get().getQuads(state, side, rand, data, renderType), state, side, rand, data, renderType, model, transform, modelData);
    }

    private static void addModelQuads(List<BakedQuad> quads, BlockState state, Direction side, RandomSource rand, ModelData data, RenderType renderType, List<BakedQuad> model, PoseStack transform, ChairModelData modelData) {
        model.addAll(transformQuadsWithDyeTextureTransform(
            quads,
            transform, modelData.getDyeColor(),
            CreateBitsnBobs.asResource("block/chair/chair_red"),
            c -> CreateBitsnBobs.asResource("block/chair/chair_" + c)
        ));
    }

    public static List<BakedQuad> transformQuadsWithDyeTextureTransform(List<BakedQuad> quads, PoseStack poseStack, DyeColor color, ResourceLocation dyeReplaceTexture, Function<String, ResourceLocation> dyeTextureProvider) {
        Matrix4f pose = poseStack.last().pose();
        List<BakedQuad> transformedQuads = new ArrayList<>();
        for (BakedQuad quad : quads) {
            int[] vertices = quad.getVertices();
            int[] transformedVertices = Arrays.copyOf(vertices, vertices.length);

            TextureAtlasSprite oldSprite = quad.getSprite();
            boolean oldSpriteIsBlockTexture = !oldSprite.contents().name().equals(dyeReplaceTexture);
            TextureAtlasSprite newSprite = oldSpriteIsBlockTexture || color == null ? oldSprite :
                Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(dyeTextureProvider.apply(color.getName()));

            Vec3 quadNormal = Vec3.atLowerCornerOf(quad.getDirection()
                .getNormal());
            Vector3f quadNormalJoml = pose.transformDirection((float) quadNormal.x, (float) quadNormal.y, (float) quadNormal.z, new Vector3f());

            for (int i = 0; i < vertices.length / BakedQuadHelper.VERTEX_STRIDE; i++) {
                Vec3 vertex = BakedQuadHelper.getXYZ(vertices, i);
                Vec3 normal = BakedQuadHelper.getNormalXYZ(vertices, i);
                float uvX = BakedQuadHelper.getU(vertices, i);
                float uvY = BakedQuadHelper.getV(vertices, i);

                if (!oldSprite.equals(newSprite)) {
                    uvX = (uvX - oldSprite.getU0()) / (oldSprite.getU1() - oldSprite.getU0()) * (newSprite.getU1() - newSprite.getU0()) + newSprite.getU0();
                    uvY = (uvY - oldSprite.getV0()) / (oldSprite.getV1() - oldSprite.getV0()) * (newSprite.getV1() - newSprite.getV0()) + newSprite.getV0();
                }

                Vector3f vertexJoml = pose.transformPosition((float) vertex.x, (float) vertex.y, (float) vertex.z, new Vector3f());
                Vector3f normalJoml = pose.transformDirection((float) normal.x, (float) normal.y, (float) normal.z, new Vector3f());

                BakedQuadHelper.setXYZ(transformedVertices, i, new Vec3(vertexJoml.x, vertexJoml.y, vertexJoml.z));
                BakedQuadHelper.setNormalXYZ(transformedVertices, i, new Vec3(normalJoml.x, normalJoml.y, normalJoml.z));
                BakedQuadHelper.setU(transformedVertices, i, uvX);
                BakedQuadHelper.setV(transformedVertices, i, uvY);
            }

            Direction newNormal = Direction.fromDelta(Math.round(quadNormalJoml.x), Math.round(quadNormalJoml.y), Math.round(quadNormalJoml.z));
            assert newNormal != null;
            transformedQuads.add(new BakedQuad(transformedVertices,
                quad.getTintIndex(),
                newNormal,
                newSprite,
                true
            ));
        }
        return transformedQuads;
    }

    private static class ChairModelData {

        boolean leftArm;
        boolean rightArm;

        DyeColor dyeColor;

        public void setData(boolean leftArm, boolean rightArm, DyeColor dyeColor) {
            this.leftArm = leftArm;
            this.rightArm = rightArm;
            this.dyeColor = dyeColor;
        }

        public boolean isLeftArm() {
            return leftArm;
        }

        public boolean isRightArm() {
            return rightArm;
        }

        public DyeColor getDyeColor() {
            return dyeColor;
        }

    }

}
