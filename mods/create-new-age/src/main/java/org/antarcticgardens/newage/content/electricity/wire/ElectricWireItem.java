package org.antarcticgardens.newage.content.electricity.wire;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.antarcticgardens.newage.content.electricity.connector.ElectricalConnectorBlockEntity;

public class ElectricWireItem extends Item {
    public static final double MAX_DISTANCE = 10.0;

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
    public InteractionResult useOn(UseOnContext context) {
        BlockEntity clickedEntity = context.getLevel().getBlockEntity(context.getClickedPos());
        BlockPos boundToPos = getBoundConnector(context.getItemInHand());

        if (boundToPos != null && context.getPlayer().isShiftKeyDown()) {
            context.getPlayer().displayClientMessage(Component.translatable("item.create_new_age.wire.message.unbound"), true);
            context.getItemInHand().removeTagKey("boundTo");
            return InteractionResult.SUCCESS;
        }

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
                    context.getPlayer().displayClientMessage(Component.translatable("item.create_new_age.wire.message.too_far"), true);
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
}
