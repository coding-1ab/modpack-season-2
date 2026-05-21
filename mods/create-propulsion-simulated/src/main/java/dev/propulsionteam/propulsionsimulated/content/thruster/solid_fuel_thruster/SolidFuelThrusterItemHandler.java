package dev.propulsionteam.propulsionsimulated.content.thruster.solid_fuel_thruster;

import org.jetbrains.annotations.NotNull;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

public class SolidFuelThrusterItemHandler implements IItemHandlerModifiable {
    public static final int QUEUE_SLOT = 0;

    private final SolidFuelThrusterBlockEntity blockEntity;

    public SolidFuelThrusterItemHandler(SolidFuelThrusterBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    @NotNull
    public ItemStack getStackInSlot(int slot) {
        return slot == QUEUE_SLOT ? blockEntity.getQueuedFuel() : ItemStack.EMPTY;
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        if (slot == QUEUE_SLOT) {
            blockEntity.setQueuedFuel(stack);
            blockEntity.markFuelChanged();
        }
    }

    @Override
    @NotNull
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (slot != QUEUE_SLOT || stack.isEmpty()) {
            return stack;
        }
        if (!blockEntity.canAcceptFuel(stack)) {
            return stack;
        }

        if (!blockEntity.getQueuedFuel().isEmpty()) {
            return stack;
        }

        ItemStack toInsert = stack.copyWithCount(1);
        if (!simulate) {
            blockEntity.setQueuedFuel(toInsert);
            blockEntity.markFuelChanged();
        }
        if (stack.getCount() <= 1) {
            return ItemStack.EMPTY;
        }
        ItemStack remainder = stack.copy();
        remainder.shrink(1);
        return remainder;
    }

    @Override
    @NotNull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot != QUEUE_SLOT || amount <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack queued = blockEntity.getQueuedFuel();
        if (queued.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack extracted = queued.copy();
        if (!simulate) {
            blockEntity.setQueuedFuel(ItemStack.EMPTY);
            blockEntity.markFuelChanged();
        }
        return extracted;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return slot == QUEUE_SLOT && blockEntity.canAcceptFuel(stack);
    }
}
