package dev.simulated_team.simulated.content.blocks.docking_connector;

import dev.simulated_team.simulated.multiloader.inventory.ContainerSlot;
import dev.simulated_team.simulated.multiloader.inventory.ItemInfoWrapper;
import dev.simulated_team.simulated.multiloader.inventory.SingleSlotContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class DockingConnectorInventory extends SingleSlotContainer {

    private final DockingConnectorBlockEntity blockEntity;
    private BlockPos connectedPos;
    private DockingConnectorInventory connectedInventory;

    public DockingConnectorInventory(final DockingConnectorBlockEntity blockEntity) {
        super(64);
        this.blockEntity = blockEntity;
    }

    public void connect(final BlockPos connectedPos, final DockingConnectorInventory other) {
        this.connectedPos = connectedPos;
        this.connectedInventory = other;
    }

    public void disconnect() {
        this.connectedPos = null;
        this.connectedInventory = null;
    }

    @Override
    public int commonInsert(final ItemInfoWrapper item, final ContainerSlot slot, final int amountToInsert, final boolean simulate) {
        return this.canInsertItem(item) ? this.connectedInventory.slot.insertStack(item, amountToInsert, simulate) : 0;
    }

    @Override
    public boolean canInsertItem(final ItemInfoWrapper info) {
        final Level level = this.blockEntity.getLevel();
        if (level == null) {
            return false;
        }

        if (this.connectedPos == null || !(level.getBlockEntity(this.connectedPos) instanceof final DockingConnectorBlockEntity other)) {
            return false;
        }

        return this.connectedInventory == other.inventory;
    }
}
