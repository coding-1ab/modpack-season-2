package dev.propulsionteam.propulsionsimulated.content.tilt_adapter;

import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsInputHandler;

import dev.propulsionteam.propulsionsimulated.CreatePropulsion;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags.Items;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Create's default handler picks the first behaviour whose {@code testHit} passes.
 * With two scroll behaviours that shared {@code netId == 0}, the right box always updated the left limit.
 */
@EventBusSubscriber(modid = CreatePropulsion.ID)
public class AdvancedTiltAdapterValueSettingsHandler {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onBlockActivated(PlayerInteractEvent.RightClickBlock event) {
        Level world = event.getLevel();
        BlockPos pos = event.getPos();
        Player player = event.getEntity();
        InteractionHand hand = event.getHand();

        if (!(world.getBlockEntity(pos) instanceof AdvancedTiltAdapterBlockEntity advanced)) {
            return;
        }
        if (!ValueSettingsInputHandler.canInteract(player)) {
            return;
        }
        if ("create:clipboard".equals(BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem()).toString())) {
            return;
        }
        SmartBlockEntity sbe = advanced;

        if (event.getSide() == LogicalSide.CLIENT) {
            CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> CreateClient.VALUE_SETTINGS_HANDLER.cancelIfWarmupAlreadyStarted(event));
        }
        if (event.isCanceled()) {
            return;
        }

        BlockHitResult ray = event.getHitVec();
        if (ray == null) {
            return;
        }

        AdvancedTiltAdapterAngleScrollBehaviour target = pickBehaviour(sbe, ray.getLocation());
        if (target == null) {
            return;
        }
        if (target.bypassesInput(player.getMainHandItem())) {
            return;
        }
        if (!target.mayInteract(player)) {
            return;
        }
        if (!target.isActive()) {
            return;
        }
        if (target.onlyVisibleWithWrench() && !player.getItemInHand(hand).is(Items.TOOLS_WRENCH)) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        if (!target.acceptsValueSettings()) {
            target.onShortInteract(player, hand, ray.getDirection(), ray);
            return;
        }

        if (event.getSide() == LogicalSide.CLIENT) {
            BehaviourType<?> type = target.getType();
            CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> CreateClient.VALUE_SETTINGS_HANDLER
                .startInteractionWith(pos, type, hand, ray.getDirection()));
        }
    }

    private static AdvancedTiltAdapterAngleScrollBehaviour pickBehaviour(SmartBlockEntity sbe, Vec3 hit) {
        AdvancedTiltAdapterAngleScrollBehaviour left = sbe.getBehaviour(AdvancedTiltAdapterAngleScrollBehaviour.LEFT_TYPE);
        AdvancedTiltAdapterAngleScrollBehaviour right = sbe.getBehaviour(AdvancedTiltAdapterAngleScrollBehaviour.RIGHT_TYPE);
        if (left == null && right == null) {
            return null;
        }

        Vec3 local = hit.subtract(Vec3.atLowerCornerOf(sbe.getBlockPos()));
        boolean hitLeft = left != null && left.testHit(hit);
        boolean hitRight = right != null && right.testHit(hit);

        if (hitLeft && !hitRight) {
            return left;
        }
        if (hitRight && !hitLeft) {
            return right;
        }
        if (!hitLeft && !hitRight) {
            return null;
        }

        // Rare corner overlap: choose the closer value box.
        double distLeft = left.getSlotPositioning().getLocalOffset(sbe.getLevel(), sbe.getBlockPos(), sbe.getBlockState())
            .distanceTo(local);
        double distRight = right.getSlotPositioning().getLocalOffset(sbe.getLevel(), sbe.getBlockPos(), sbe.getBlockState())
            .distanceTo(local);
        return distLeft <= distRight ? left : right;
    }
}
