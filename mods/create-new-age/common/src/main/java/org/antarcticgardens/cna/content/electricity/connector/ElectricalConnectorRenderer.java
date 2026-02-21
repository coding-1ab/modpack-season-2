package org.antarcticgardens.cna.content.electricity.connector;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.createmod.catnip.data.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.antarcticgardens.cna.CNARenderTypes;
import org.antarcticgardens.cna.CreateNewAge;
import org.antarcticgardens.cna.config.CNAConfig;
import org.antarcticgardens.cna.content.electricity.wire.ElectricWireItem;
import org.antarcticgardens.cna.content.electricity.wire.WireType;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class ElectricalConnectorRenderer implements BlockEntityRenderer<AbstractElectricalConnector> {
    public ElectricalConnectorRenderer(BlockEntityRendererProvider.Context context) {
        super();
    }

    public static HitResult pickBlockFromPos(Level world, Vec3 pos, Vec3 dir, double distance) {
        Vec3 vec33 = pos.add(dir.x * distance, dir.y * distance, dir.z * distance);
        return world.clip(new ClipContext(pos, vec33, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, Minecraft.getInstance().player));
    }

    @Override
    public void render(AbstractElectricalConnector blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        renderAllConnections(blockEntity, poseStack, buffer);
        renderHand(blockEntity, partialTick, poseStack, buffer);
    }

    public void renderAllConnections(AbstractElectricalConnector blockEntity, PoseStack poseStack, MultiBufferSource buffer) {
        blockEntity.getConnectorPositions().entrySet().stream().forEach(e ->
                renderConnection(blockEntity.getBlockPos(), e.getKey(), e.getValue(), poseStack, buffer, blockEntity.getLevel()));
    }

    //Makes sure that only one of the two connectors renders the wire
    public boolean shouldRenderConnection(BlockPos pos, BlockPos endPos){
        if(pos.getX() < endPos.getX()){
            return true;
        }else if(pos.getX() == endPos.getX()) {
            if(pos.getY() < endPos.getY()){
                return true;
            }else if(pos.getY() == endPos.getY() && pos.getZ() < endPos.getZ()) {
                return true;
            }
        }
        return false;
    }

    public void renderConnection(BlockPos pos, BlockPos endPos, WireType wireType, PoseStack poseStack, MultiBufferSource buffer, Level level){
        if(!shouldRenderConnection(pos, endPos)){
            return;
        }

        ResourceLocation texture = wireType.getTextureLocation();

        var originConnectorPreCast = level.getBlockEntity(pos);
        var endConnectorPreCast = level.getBlockEntity(endPos);
        if (!(originConnectorPreCast instanceof AbstractElectricalConnector originConnector) ||
                !(endConnectorPreCast instanceof AbstractElectricalConnector endConnector)) {return;}

        var originPoint = originConnector.getConnectionPoint().add(Vec3.atLowerCornerOf(pos));
        var endPoint = endConnector.getConnectionPoint().add(Vec3.atLowerCornerOf(endPos));

        double distance = pos.getCenter().distanceTo(endPoint);
        int sections = (int) Math.ceil(distance * CNAConfig.getClient().wireSectionsPerMeter.get());
        Vector3f direction = endPoint.subtract(originPoint).normalize().toVector3f();

        Wire wire = new Wire(direction, (float) distance, sections);
        VertexConsumer consumer = buffer.getBuffer(CNARenderTypes.wire(texture));

        poseStack.pushPose();
        var midPoint = originConnector.getConnectionPoint();
        poseStack.translate(midPoint.x(), midPoint.y(), midPoint.z());
        poseStack.mulPose(new Matrix4f().rotateTowards(wire.getDirection(), wire.getUp()));

        for (int i = 0; i < wire.getSections().size(); i++) {
            Pair<WireSection, Float> pair = wire.getSections().get(i);

            float sectionOffset = wire.getSectionLength() * i;
            Vector3f lightPos = pos.getCenter().toVector3f()
                    .add(wire.getDirection().mul(sectionOffset))
                    .add(wire.getUp().mul(pair.getSecond()));
            BlockPos lightBlockPos = BlockPos.containing(new Vec3(lightPos));
            int block = level.getBrightness(LightLayer.BLOCK, lightBlockPos);
            int sky = level.getBrightness(LightLayer.SKY, lightBlockPos);

            pair.getFirst().render(consumer, poseStack, LightTexture.pack(block, sky), pair.getSecond());

            poseStack.translate(0.0f, 0.0f, wire.getSectionLength());
        }

        poseStack.popPose();
    }

    public void renderHand(AbstractElectricalConnector blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            ItemStack itemInHand = player.getMainHandItem();

            if (!(itemInHand.getItem() instanceof ElectricWireItem))
                itemInHand = player.getOffhandItem();

            if (itemInHand.getItem() instanceof ElectricWireItem wireItem) {
                var midPoint = blockEntity.getConnectionPoint();
                BlockPos bound = wireItem.getBoundConnector(itemInHand);

                if (bound != null && bound.equals(blockEntity.getBlockPos())) {
                    Vec3 eyePos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
                    Vec3 endPos = eyePos.add(player.getViewVector(partialTick).normalize().scale(2.0f));

                    HitResult hit = pickBlockFromPos(blockEntity.getLevel(), eyePos,
                            player.getViewVector(partialTick), Minecraft.getInstance().player.blockInteractionRange());

                    if (hit instanceof BlockHitResult blockHit) {
                        Vec3 vec = eyePos.add(blockHit.getLocation().subtract(eyePos).scale(0.9f));

                        if (eyePos.distanceTo(endPos) > eyePos.distanceTo(vec))
                            endPos = vec;
                    }

                    BlockPos pos = blockEntity.getBlockPos();

                    Vector3f to = new Vector3f(
                            (float) (endPos.x - pos.getX() - midPoint.x),
                            (float) (endPos.y - pos.getY() - midPoint.y),
                            (float) (endPos.z - pos.getZ() - midPoint.z)
                    );

                    var originPoint = blockEntity.getConnectionPoint().add(Vec3.atLowerCornerOf(bound));

                    double distance = endPos.distanceTo(originPoint);
                    int maxDistance = CNAConfig.getCommon().maxWireLength.get();

                    if (distance > maxDistance * 2)
                        return;

                    if (Minecraft.getInstance().gameMode != null && hit instanceof BlockHitResult blockHit) {
                        if (blockEntity.getLevel().getBlockEntity(blockHit.getBlockPos()) instanceof AbstractElectricalConnector connector) {
                            if (connector.isConnected(blockEntity.getBlockPos()))
                                return;

                            var point = connector.getConnectionPoint();

                            to = new Vector3f(
                                    blockHit.getBlockPos().getX() - pos.getX() + (float)point.x() - (float)midPoint.x(),
                                    blockHit.getBlockPos().getY() - pos.getY() + (float)point.y() - (float)midPoint.y(),
                                    blockHit.getBlockPos().getZ() - pos.getZ() + (float)point.z() - (float)midPoint.z()
                            );

                            distance = connector.getBlockPos().getCenter().distanceTo(blockEntity.getBlockPos().getCenter());
                        }
                    }

                    ResourceLocation texture = wireItem.getWireType().getTextureLocation();

                    if (distance >= maxDistance) {
                        texture = ResourceLocation.fromNamespaceAndPath(CreateNewAge.MOD_ID, "textures/wire/red.png");
                    }

                    int sections = (int) Math.ceil(distance * CNAConfig.getClient().wireSectionsPerMeter.get());

                    Vector3f direction = new Vector3f(to).normalize();

                    Wire wire = new Wire(direction, (float) distance, sections);
                    VertexConsumer consumer = buffer.getBuffer(CNARenderTypes.wire(texture));

                    poseStack.pushPose();
                    poseStack.translate(midPoint.x(), midPoint.y(), midPoint.z());
                    poseStack.mulPose(new Matrix4f().rotateTowards(wire.getDirection(), wire.getUp()));

                    for (int i = 0; i < wire.getSections().size(); i++) {
                        Pair<WireSection, Float> pair = wire.getSections().get(i);

                        float sectionOffset = wire.getSectionLength() * i;
                        Vector3f lightPos = blockEntity.getBlockPos().getCenter().toVector3f()
                                .add(wire.getDirection().mul(sectionOffset))
                                .add(wire.getUp().mul(pair.getSecond()));
                        BlockPos lightBlockPos = BlockPos.containing(new Vec3(lightPos));
                        int block = blockEntity.getLevel().getBrightness(LightLayer.BLOCK, lightBlockPos);
                        int sky = blockEntity.getLevel().getBrightness(LightLayer.SKY, lightBlockPos);

                        pair.getFirst().render(consumer, poseStack, LightTexture.pack(block, sky), pair.getSecond());

                        poseStack.translate(0.0f, 0.0f, wire.getSectionLength());
                    }

                    poseStack.popPose();
                }
            }
        }
    }

    @Override
    public boolean shouldRenderOffScreen(AbstractElectricalConnector blockEntity) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(AbstractElectricalConnector blockEntity) {
        return AABB.INFINITE;
    }
}