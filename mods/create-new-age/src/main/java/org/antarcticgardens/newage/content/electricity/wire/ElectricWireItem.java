package org.antarcticgardens.newage.content.electricity.wire;

import com.simibubi.create.foundation.utility.Lang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.antarcticgardens.newage.content.electricity.connector.ElectricalConnectorBlockEntity;
import org.antarcticgardens.newage.tools.StringFormattingTool;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ElectricWireItem extends Item {
    public static final double MAX_DISTANCE = 16.0;

    private final WireType wireType;

    public ElectricWireItem(Properties properties, WireType wireType) {
        super(properties);
        this.wireType = wireType;
    }

    public static ElectricWireItem newCopperWire(Properties properties) {
        return new ElectricWireItem(properties, WireType.COPPER);
    }

    public static ElectricWireItem newIronWire(Properties properties) {
        return new ElectricWireItem(properties, WireType.IRON);
    }

    public static ElectricWireItem newGoldenWire(Properties properties) {
        return new ElectricWireItem(properties, WireType.GOLD);
    }

    public static ElectricWireItem newDiamondWire(Properties properties) {
        return new ElectricWireItem(properties, WireType.DIAMOND);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, InteractionHand usedHand) {
        ItemStack item = player.getItemInHand(usedHand);
        BlockPos boundToPos = getBoundConnector(item);

        if (boundToPos != null && player.isShiftKeyDown()) {
            player.displayClientMessage(Component.translatable("item.create_new_age.wire.message.unbound"), true);
            item.removeTagKey("boundTo");
            return InteractionResultHolder.success(item);
        }
        return InteractionResultHolder.pass(item);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (isSelected) {
            BlockPos boundToPos = getBoundConnector(stack);
            if (boundToPos == null)
                return;
            if (!(level.getBlockEntity(boundToPos) instanceof ElectricalConnectorBlockEntity)) {
                stack.removeTagKey("boundTo");
            }
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockEntity clickedEntity = context.getLevel().getBlockEntity(context.getClickedPos());
        BlockPos boundToPos = getBoundConnector(context.getItemInHand());

        if (clickedEntity instanceof ElectricalConnectorBlockEntity clickedConnector) {
            if (boundToPos == null) {
                setBoundConnector(context.getItemInHand(), clickedConnector);
                return InteractionResult.SUCCESS;
            } else {
                BlockPos clickedPos = clickedConnector.getBlockPos();

                if (boundToPos.equals(clickedPos)) {
                    context.getPlayer().displayClientMessage(Component.translatable("item.create_new_age.wire.message.self_connect"), true);
                    context.getItemInHand().removeTagKey("boundTo");
                    return InteractionResult.FAIL;
                } else if (!clickedPos.closerThan(boundToPos, MAX_DISTANCE)) {
                    context.getPlayer().displayClientMessage(Component.translatable("item.create_new_age.wire.message.too_far", MAX_DISTANCE), true);
                    return InteractionResult.FAIL;
                } else if (clickedConnector.isConnected(boundToPos)) {
                    context.getPlayer().displayClientMessage(Component.translatable("item.create_new_age.wire.message.already_connected"), true);
                    context.getItemInHand().removeTagKey("boundTo");
                    return InteractionResult.FAIL;
                }

                BlockEntity boundToEntity = context.getLevel().getBlockEntity(boundToPos);

                if (boundToEntity instanceof ElectricalConnectorBlockEntity boundToConnector) {
                    context.getItemInHand().removeTagKey("boundTo");
                    boundToConnector.connect(clickedConnector, wireType);

                    if (!context.getPlayer().isCreative())
                        context.getItemInHand().shrink(1);

                    return InteractionResult.CONSUME;
                } else
                    return InteractionResult.FAIL;
            }
        }

        return InteractionResult.PASS;
    }

    private BlockPos getBoundConnector(ItemStack stack) {
        CompoundTag tag = stack.getTagElement("boundTo");

        if (tag == null)
            return null;

        return NbtUtils.readBlockPos(tag);
    }

    private void setBoundConnector(ItemStack stack, ElectricalConnectorBlockEntity connector) {
        stack.addTagElement("boundTo", NbtUtils.writeBlockPos(connector.getBlockPos()));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Lang.translate("tooltip.create_new_age.transfers").style(ChatFormatting.GRAY)
                .component());
        tooltip.add(Lang.text(" ").translate("tooltip.create_new_age.energy_per_second", StringFormattingTool.formatLong(wireType.getConductivity() * 20)).style(ChatFormatting.AQUA).component());
    }

}
