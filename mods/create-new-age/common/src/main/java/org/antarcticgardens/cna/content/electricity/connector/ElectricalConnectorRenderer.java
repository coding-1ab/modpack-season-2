package org.antarcticgardens.cna.content.electricity.connector;

import com.google.common.collect.Streams;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.antarcticgardens.cna.CNARenderTypes;
import org.antarcticgardens.cna.CreateNewAge;
import org.antarcticgardens.cna.config.CNAConfig;
import org.antarcticgardens.cna.content.electricity.wire.ElectricWireItem;
import org.antarcticgardens.cna.content.electricity.wire.WireType;

import java.util.stream.Stream;

public class ElectricalConnectorRenderer implements BlockEntityRenderer<AbstractElectricalConnector> {
    @Override
    public void render(AbstractElectricalConnector blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        renderAllConnections(blockEntity, poseStack, buffer);
        renderHand(blockEntity, partialTick, poseStack, buffer);
    }

    public void renderAllConnections(AbstractElectricalConnector blockEntity, PoseStack poseStack, MultiBufferSource buffer) {
        blockEntity.getConnectorPositions().forEach((key, value) ->
                renderConnection(blockEntity.getBlockPos(), key, value, poseStack, buffer, blockEntity.getLevel())
        );
    }

    //Makes sure that only one of the two connectors renders the wire
    public boolean shouldRenderConnection(BlockPos pos, BlockPos endPos) {
        return pos.compareTo(endPos) < 0;
    }

    public void renderConnection(BlockPos pos, BlockPos endPos, WireType wireType, PoseStack poseStack, MultiBufferSource buffer, Level level) {
        if (!shouldRenderConnection(pos, endPos)) {
            return;
        }

        ResourceLocation texture = wireType.getTextureLocation();

        var originConnectorPreCast = level.getBlockEntity(pos);
        var endConnectorPreCast = level.getBlockEntity(endPos);
        if (!(originConnectorPreCast instanceof AbstractElectricalConnector originConnector) ||
                !(endConnectorPreCast instanceof AbstractElectricalConnector endConnector)) {
            return;
        }

        var originPoint = originConnector.getConnectionPoint().add(Vec3.atLowerCornerOf(pos));
        var endPoint = endConnector.getConnectionPoint().add(Vec3.atLowerCornerOf(endPos));

        Wire wire = new Wire(
                originPoint.toVector3f(),
                endPoint.toVector3f(),
                CNAConfig.getClient().wireSectionsPerMeter.get(),
                CNAConfig.getClient().wireThickness.get().floatValue()
        );
        VertexConsumer consumer = buffer.getBuffer(CNARenderTypes.wire(texture));

        poseStack.pushPose();

        wire.render(consumer, poseStack, level);

        poseStack.popPose();
    }

    public void renderHand(AbstractElectricalConnector blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }

        Level level = player.level();
        if (!minecraft.options.getCameraType().isFirstPerson()) {
            return;
        }

        ItemStack itemStack = player.getMainHandItem();

        if (!(itemStack.getItem() instanceof ElectricWireItem)) {
            itemStack = player.getOffhandItem();
        }

        if (!(itemStack.getItem() instanceof ElectricWireItem wireItem)) {
            return;
        }

        BlockPos bound = wireItem.getBoundConnector(itemStack);

        if (bound == null || !bound.equals(blockEntity.getBlockPos())) {
            return;
        }

        Vec3 eyePos = minecraft.gameRenderer.getMainCamera().getPosition();
        Vec3 viewVector = player.getViewVector(partialTick);
        Vec3 wireEnd = eyePos.add(viewVector.scale(player.blockInteractionRange()));
        var wireStart = blockEntity.getConnectionPoint().add(Vec3.atLowerCornerOf(bound));

        HitResult lookingAt = minecraft.hitResult;

        if (lookingAt instanceof BlockHitResult blockHit) {
            BlockPos lookedAtPos = blockHit.getBlockPos();
            BlockEntity lookedBlockEntity = level.getBlockEntity(lookedAtPos);
            boolean snapped = false;

            if (lookedBlockEntity != null) {
                if (lookedBlockEntity == blockEntity) {
                    return;
                }
                if (lookedBlockEntity instanceof AbstractElectricalConnector otherConnector) {
                    if (otherConnector.isConnected(blockEntity.getBlockPos())) {
                        return;
                    }

                    wireEnd = otherConnector.getConnectionPoint().add(Vec3.atLowerCornerOf(lookedAtPos));
                    snapped = true;
                }
            }

            if (!snapped) {
                Vec3 vec = blockHit.getLocation().subtract(viewVector.scale(0.1));

                if (eyePos.distanceToSqr(wireEnd) > eyePos.distanceToSqr(vec))
                    wireEnd = vec;
            }
        }

        ResourceLocation texture = wireItem.getWireType().getTextureLocation();
        double distanceSqr = wireEnd.distanceToSqr(wireStart);
        int maxDistance = CNAConfig.getServer().maxWireLength.get();

        if (distanceSqr >= (maxDistance * maxDistance)) {
            if (distanceSqr > (maxDistance * maxDistance) * 4) {
                return;
            }

            texture = ResourceLocation.fromNamespaceAndPath(CreateNewAge.MOD_ID, "textures/wire/red.png");

        }

        Wire wire = new Wire(
                wireStart.toVector3f(),
                wireEnd.toVector3f(),
                CNAConfig.getClient().wireSectionsPerMeter.get(),
                CNAConfig.getClient().wireThickness.get().floatValue()
        );
        VertexConsumer consumer = buffer.getBuffer(CNARenderTypes.wire(texture));

        poseStack.pushPose();
        wire.render(consumer, poseStack, level);

        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(AbstractElectricalConnector blockEntity) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(AbstractElectricalConnector blockEntity) {
        Stream<AbstractElectricalConnector> connectors = blockEntity.connectors.keySet().stream();
        double[] coordinates = Streams.concat(connectors, Stream.of(blockEntity))
                .map(AbstractElectricalConnector::getConnectionPoint)
                .collect(() -> new double[]{
                        Double.POSITIVE_INFINITY, // minX
                        Double.POSITIVE_INFINITY, // minY
                        Double.POSITIVE_INFINITY, // minZ
                        Double.NEGATIVE_INFINITY, // maxX
                        Double.NEGATIVE_INFINITY, // maxY
                        Double.NEGATIVE_INFINITY, // maxZ
                }, (acc, point) -> {
                    acc[0] = Math.min(acc[0], point.x);
                    acc[1] = Math.min(acc[1], point.y);
                    acc[2] = Math.min(acc[2], point.z);
                    acc[3] = Math.max(acc[3], point.x);
                    acc[4] = Math.max(acc[4], point.y);
                    acc[5] = Math.max(acc[5], point.z);
                }, (a, b) -> {
                });

        AABB box = new AABB(coordinates[0], coordinates[1], coordinates[2], coordinates[3], coordinates[4], coordinates[5]);
        return box.inflate(1.0);
    }
}
