package dev.ryanhcode.sable.mixin.block_decal_render;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Changes the distance block damage is rendered from, transforms block damage rendering for sublevels, and renders block hover outlines for sublevels.
 */
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    // Storage vectors to avoid repeated allocation
    private final @Unique Vector3d sable$localTranslationStorage = new Vector3d();
    private final @Unique Vector3d sable$globalTranslationStorage = new Vector3d();
    private final @Unique Quaternionf sable$orientationStorage = new Quaternionf();

    @Shadow
    @Nullable
    private ClientLevel level;

    @Shadow
    protected static void renderShape(final PoseStack arg, final VertexConsumer arg2, final VoxelShape arg3, final double d, final double e, final double f, final float g, final float h, final float i, final float j) {
    }

    @Inject(method = "renderHitOutline", at = @At("HEAD"), cancellable = true)
    private void sable$preRenderHitOutline(final PoseStack ps, final VertexConsumer pConsumer, final Entity pEntity, final double pCamX, final double pCamY, final double pCamZ, final BlockPos blockPos, final BlockState blockState, final CallbackInfo ci) {
        final ClientSubLevel subLevel = (ClientSubLevel) Sable.HELPER.getContaining(this.level, blockPos);

        if (subLevel == null) {
            return;
        }

        ps.pushPose();

        final Pose3dc pose = subLevel.renderPose();

        final Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

        final Vector3d globalTranslation = pose.position().sub(cameraPos.x, cameraPos.y, cameraPos.z, this.sable$globalTranslationStorage);
        final Vector3d localTranslation = this.sable$localTranslationStorage.set(blockPos.getX(), blockPos.getY(), blockPos.getZ()).sub(pose.rotationPoint());

        // apply transforms
        ps.translate(globalTranslation.x, globalTranslation.y, globalTranslation.z);
        ps.mulPose(this.sable$orientationStorage.set(pose.orientation()));
        ps.translate(localTranslation.x, localTranslation.y, localTranslation.z);

        renderShape(ps, pConsumer, blockState.getShape(this.level, blockPos, CollisionContext.of(pEntity)), 0, 0, 0, 0.0F, 0.0F, 0.0F, 0.4F);

        ps.popPose();
        ci.cancel();
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;last()Lcom/mojang/blaze3d/vertex/PoseStack$Pose;", shift = At.Shift.BEFORE))
    private void sable$preRenderBlockDamage(final DeltaTracker deltaTracker, final boolean bl, final Camera camera, final GameRenderer gameRenderer, final LightTexture lightTexture, final Matrix4f matrix4f, final Matrix4f matrix4f2, final CallbackInfo ci, @Local(ordinal = 0) final PoseStack ps, @Local(ordinal = 0) final BlockPos pos) {

        final Vec3 plotPos = new Vec3(pos.getX(), pos.getY(), pos.getZ());
        final ClientSubLevel subLevel = (ClientSubLevel) Sable.HELPER.getContaining(this.level, plotPos);

        if (subLevel == null) {
            return;
        }

        final Pose3dc renderPose = subLevel.renderPose();
        final Vec3 cameraPos = camera.getPosition();
        final Vec3 projectedPos = renderPose.transformPosition(plotPos);

        ps.popPose();
        ps.pushPose();

        ps.translate(projectedPos.x - cameraPos.x, projectedPos.y - cameraPos.y, projectedPos.z - cameraPos.z);
        ps.mulPose(this.sable$orientationStorage.set(renderPose.orientation()));
    }

    @ModifyConstant(method = "renderLevel", constant = @Constant(doubleValue = 1024.0, ordinal = 0))
    private double sable$blockDamageDistance(final double originalBlockDamageDistanceConstant) {
        return Double.MAX_VALUE;
    }
}
