package org.antarcticgardens.cna.content.electricity.connector;

import com.jozufozu.flywheel.api.vertex.VertexList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.antarcticgardens.cna.CNARenderTypes;
import org.antarcticgardens.cna.CreateNewAge;
import org.antarcticgardens.cna.config.CNAConfig;
import org.antarcticgardens.cna.content.electricity.wire.ElectricWireItem;
import org.antarcticgardens.cna.util.RaycastUtil;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class ElectricalConnectorRenderer implements BlockEntityRenderer<ElectricalConnectorBlockEntity> {
    public ElectricalConnectorRenderer(BlockEntityRendererProvider.Context context) {
        super();
    }

    @Override
    public void render(ElectricalConnectorBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            ItemStack itemInHand = player.getMainHandItem();

            if (!(itemInHand.getItem() instanceof ElectricWireItem))
                itemInHand = player.getOffhandItem();

            if (itemInHand.getItem() instanceof ElectricWireItem wire) {
                BlockPos bound = wire.getBoundConnector(itemInHand);

                if (bound != null && bound.equals(blockEntity.getBlockPos())) {
                    Vec3 eyePos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
                    Vec3 endPos = eyePos.add(player.getViewVector(partialTick).normalize().scale(2.0f));

                    HitResult hit = RaycastUtil.pickBlockFromPos(blockEntity.getLevel(), eyePos,
                            player.getViewVector(partialTick), Minecraft.getInstance().gameMode.getPickRange());

                    if (hit instanceof BlockHitResult blockHit) {
                        Vec3 vec = eyePos.add(blockHit.getLocation().subtract(eyePos).scale(0.9f));

                        if (eyePos.distanceTo(endPos) > eyePos.distanceTo(vec))
                            endPos = vec;
                    }

                    BlockPos pos = blockEntity.getBlockPos();

                    Vector3f to = new Vector3f(
                            (float) (endPos.x - pos.getX() - 0.5),
                            (float) (endPos.y - pos.getY() - 0.5),
                            (float) (endPos.z - pos.getZ() - 0.5)
                    );

                    double distance = endPos.distanceTo(bound.getCenter());
                    int maxDistance = CNAConfig.getCommon().maxWireLength.get();

                    if (distance > maxDistance * 2)
                        return;

                    if (Minecraft.getInstance().gameMode != null && hit instanceof BlockHitResult blockHit) {
                        if (blockEntity.getLevel().getBlockEntity(blockHit.getBlockPos()) instanceof ElectricalConnectorBlockEntity connector) {
                            if (connector.isConnected(blockEntity.getBlockPos()))
                                return;

                            to = new Vector3f(
                                    blockHit.getBlockPos().getX() - pos.getX(),
                                    blockHit.getBlockPos().getY() - pos.getY(),
                                    blockHit.getBlockPos().getZ() - pos.getZ()
                            );

                            distance = connector.getBlockPos().getCenter().distanceTo(blockEntity.getBlockPos().getCenter());
                        }
                    }

                    ResourceLocation texture = wire.getWireType().getTextureLocation();

                    if (distance >= maxDistance) {
                        texture = new ResourceLocation(CreateNewAge.MOD_ID, "textures/wire/red.png");
                    }

                    poseStack.pushPose();
                    poseStack.translate(0.5f, 0.5f, 0.5f);

                    int sections = (int) Math.ceil(distance * CNAConfig.getClient().wireSectionsPerMeter.get());

                    Vector3f direction = new Vector3f(to).normalize();
                    WireModel model = new WireModel(direction, sections, (float) (distance / sections),
                            CNAConfig.getClient().wireThickness.get().floatValue());

                    VertexConsumer consumer = buffer.getBuffer(CNARenderTypes.wire(texture));
                    VertexList reader = model.getReader();
                    Matrix4f pose = poseStack.last().pose();

                    for (int i = 0; i < reader.getVertexCount(); i++) {
                        consumer.vertex(pose, reader.getX(i), reader.getY(i), reader.getZ(i))
                                .color(1.0f, 1.0f, 1.0f, 1.0f)
                                .uv(reader.getU(i), reader.getV(i))
                                .uv2(packedLight) // TODO proper lighting
                                .normal(0.0f, 1.0f, 0.0f)
                                .endVertex();
                    }

                    poseStack.popPose();
                }
            }
        }
    }

    @Override
    public boolean shouldRenderOffScreen(ElectricalConnectorBlockEntity blockEntity) {
        return true;
    }
}
