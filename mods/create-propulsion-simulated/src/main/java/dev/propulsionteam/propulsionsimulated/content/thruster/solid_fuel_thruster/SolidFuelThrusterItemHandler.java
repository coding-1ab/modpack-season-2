package dev.propulsionteam.propulsionsimulated.content.thruster.solid_fuel_thruster;

import org.jetbrains.annotations.NotNull;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

public class SolidFuelThrusterItemHandler extends ItemStackHandler {
    public static final int FUEL_SLOT = 0;
    public static final int SLOT_COUNT = 1;

    private final SolidFuelThrusterBlockEntity blockEntity;

    public SolidFuelThrusterItemHandler(SolidFuelThrusterBlockEntity blockEntity) {
        super(SLOT_COUNT);
        this.blockEntity = blockEntity;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return slot == FUEL_SLOT && blockEntity.canAcceptFuel(stack);
    }

    @Override
    @NotNull
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (slot != FUEL_SLOT || stack.isEmpty() || blockEntity.getBurnTime() > 0) {
            return stack;
        }
        if (!getStackInSlot(FUEL_SLOT).isEmpty()) {
            return stack;
        }
        return super.insertItem(slot, stack, simulate);
    }

    @Override
    @NotNull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot != FUEL_SLOT || amount <= 0 || blockEntity.getBurnTime() > 0) {
            return ItemStack.EMPTY;
        }
        return super.extractItem(slot, amount, simulate);
    }

    @Override
    protected void onContentsChanged(int slot) {
        blockEntity.onInventoryChanged(slot);
    }
}
