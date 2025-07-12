package com.kipti.bnb.content.nixie.foundation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.model.BakedQuadHelper;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.*;

import java.lang.Math;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Used for a block that can be placed on any surface (FACING), but can have a 0-3 ORIENTATION property
 */
public class DoubleOrientedBlockModel extends BakedModelWrapper<BakedModel> {

    private static final ModelProperty<DoubleOrientedModelData> DOUBLE_ORIENTED_PROPERTY = new ModelProperty<>();

    public DoubleOrientedBlockModel(BakedModel originalModel) {
        super(originalModel);
    }

    @Override
    public @NotNull ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
        DoubleOrientedModelData data = new DoubleOrientedModelData();

        Direction up = state.getValue(DoubleOrientedBlock.FACING);
        Direction front = state.getValue(DoubleOrientedBlock.ORIENTATION);
        Matrix4f rotation = getRotation(up, front);
        data.setRotation(rotation);

        return ModelData.builder()
            .with(DOUBLE_ORIENTED_PROPERTY, data)
            .build();
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData data, @Nullable RenderType renderType) {
        if (data.has(DOUBLE_ORIENTED_PROPERTY)) {
            DoubleOrientedModelData doubleOrientedModelData = data.get(DOUBLE_ORIENTED_PROPERTY);

            assert doubleOrientedModelData != null;

            PoseStack transformStack = new PoseStack();
            transformStack.translate(0.5, 0.5, 0.5);
            transformStack.mulPose(doubleOrientedModelData.getRotation());
            transformStack.translate(-0.5, -0.5, -0.5);
            return new ArrayList<>(transformQuads(super.getQuads(state, side, rand, data, renderType), transformStack));
        }
        return Collections.emptyList();
    }

    public static Direction getLeft(BlockState state) {
        Direction up = state.getValue(DoubleOrientedBlock.FACING);
        Direction front = state.getValue(DoubleOrientedBlock.ORIENTATION);
        return getLeft(up, front);
    }

    public static Direction getLeft(Direction up, Direction front) {
        return getDirectionByNormal(up.getNormal().cross(front.getNormal())).getOpposite();
    }

    private static Direction getDirectionByNormal(Vec3i cross) {
        for (Direction direction : Direction.values()) {
            if (direction.getNormal().equals(cross)) {
                return direction;
            }
        }
        throw new IllegalArgumentException("No Direction found for normal: " + cross);
    }

    public static Direction getFront(Direction up, Direction front) {
        if (up.getAxis() == front.getAxis()) {
            return front;
        }
        return up.getAxis() == Direction.Axis.Y ? Direction.EAST : up.getAxis() != Direction.Axis.Z ? Direction.NORTH : Direction.EAST;
    }

    /**
     * Gets the rotation given the direction at the top of the block and the direction in front
     */
    public static Matrix4f getRotation(Direction upDir, Direction frontDir) {
        Direction leftDir = getLeft(upDir, frontDir);
        return new Matrix4f(
            leftDir.getStepX(), leftDir.getStepY(), leftDir.getStepZ(), 0,
            upDir.getStepX(), upDir.getStepY(), upDir.getStepZ(), 0,
            -frontDir.getStepX(), -frontDir.getStepY(), -frontDir.getStepZ(), 0,
            0, 0, 0, 1
        );
    }


    private List<BakedQuad> transformQuads(List<BakedQuad> quads, PoseStack poseStack) {
        Matrix4f pose = poseStack.last().pose();
        List<BakedQuad> transformedQuads = new ArrayList<>();
        for (BakedQuad quad : quads) {
            int[] vertices = quad.getVertices();
            int[] transformedVertices = Arrays.copyOf(vertices, vertices.length);

            Vec3 quadNormal = Vec3.atLowerCornerOf(quad.getDirection()
                .getNormal());
            Vector3f quadNormalJoml = pose.transformDirection((float) quadNormal.x, (float) quadNormal.y, (float) quadNormal.z, new Vector3f());

            for (int i = 0; i < vertices.length / BakedQuadHelper.VERTEX_STRIDE; i++) {
                Vec3 vertex = BakedQuadHelper.getXYZ(vertices, i);
                Vec3 normal = BakedQuadHelper.getNormalXYZ(vertices, i);

                Vector3f vertexJoml = pose.transformPosition((float) vertex.x, (float) vertex.y, (float) vertex.z, new Vector3f());
                Vector3f normalJoml = pose.transformDirection((float) normal.x, (float) normal.y, (float) normal.z, new Vector3f());

                BakedQuadHelper.setXYZ(transformedVertices, i, new Vec3(vertexJoml.x, vertexJoml.y, vertexJoml.z));
                BakedQuadHelper.setNormalXYZ(transformedVertices, i, new Vec3(normalJoml.x, normalJoml.y, normalJoml.z));
            }


            Direction newNormal = Direction.getNearest(quadNormalJoml.x, quadNormalJoml.y, quadNormalJoml.z).getOpposite();//TODO: from direint
            transformedQuads.add(new BakedQuad(transformedVertices,
                quad.getTintIndex(),
                newNormal,
                quad.getSprite(),
                false
            ));
        }
        return transformedQuads;
    }

    private static class DoubleOrientedModelData {

        Matrix4f rotation;

        public Matrix4f getRotation() {
            return rotation;
        }

        public void setRotation(Matrix4f rotation) {
            this.rotation = rotation;
        }

    }

}
