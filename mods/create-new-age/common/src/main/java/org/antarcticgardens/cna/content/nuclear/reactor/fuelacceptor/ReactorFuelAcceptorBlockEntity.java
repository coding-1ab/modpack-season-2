package org.antarcticgardens.cna.content.nuclear.reactor.fuelacceptor;

import com.simibubi.create.AllBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.antarcticgardens.cna.CNABlockEntityTypes;
import org.antarcticgardens.cna.CreateNewAge;
import org.antarcticgardens.cna.CNATags;
import org.antarcticgardens.cna.content.nuclear.reactor.RodFindingReactorBlockEntity;
import org.antarcticgardens.cna.content.nuclear.reactor.rod.ReactorRodBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.neoforged.neoforge.items.IItemHandler;
// TODO: Make this fabric compatible
// import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ReactorFuelAcceptorBlockEntity extends RodFindingReactorBlockEntity {
    public IItemHandler capability;
    public SimpleContainer container;

    public ReactorFuelAcceptorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        container = new FuelAcceptorContainer(3);
        capability = new FuelAcceptorInventoryHandler();
    }

    // TODO: register this is ModBusEvents.registerCapabilities
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                CNABlockEntityTypes.REACTOR_FUEL_ACCEPTOR.get(),
                (be, context) -> be.capability
        );
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        compound.put("contents", container.createTag(registries));
        super.write(compound, registries, clientPacket);
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        container.fromTag(compound.getList("contents", compound.TAG_COMPOUND), registries);
        super.read(compound, registries, clientPacket);
    }

    @Override
    public void destroy() {
        super.destroy();
        for (int i = 0 ; i < container.getContainerSize() ; i++) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), container.getItem(i));
        }
    }

    int ticks = 0;

    public void tick(BlockPos pos, Level world, BlockState state) {
        ticks--;
        if (ticks <= 0) {
            ticks = 20;
            List<ReactorRodBlockEntity> rods = new LinkedList<>();
            for (Direction dir : Direction.values()) {
                findRods(rods, dir);
            }
            if (rods.isEmpty()) {
                return;
            }
            AtomicInteger have = new AtomicInteger();
            int hadBefore = 0;
            for (ReactorRodBlockEntity rod : rods) {
                hadBefore += rod.fuel;
            }

            AtomicInteger totalNeeded = new AtomicInteger(345600 * rods.size() - hadBefore);
            for (int i = 0 ; i < container.getContainerSize() ; i++) {
                ItemStack stack = container.getItem(i);
                if (stack.is(CNATags.Item.NUCLEAR_FUEL.tag)) {
                    stack.getTags().forEach(itemTagKey -> {
                        if (itemTagKey.location().getNamespace().equals(CreateNewAge.MOD_ID)) {
                            String path = itemTagKey.location().getPath();
                            if (path.startsWith("nuclear/energy_")) {
                                try {
                                    int energy = Integer.parseInt(path.substring(15)); //  assert "nuclear/energy_".length() == 15;
                                    int total = (int) Math.min(stack.getCount(), (double) (totalNeeded.get() / energy));
                                    totalNeeded.addAndGet(-energy*total);
                                    have.addAndGet(energy*total);
                                    stack.shrink(total);
                                } catch (NumberFormatException e) {
                                    CreateNewAge.LOGGER.error("BAD TAG " + itemTagKey + " on item " + stack.getDisplayName().getString(), e);
                                }
                            }
                        }
                    });
                }
            }
            setChanged();
            int target = (hadBefore + have.get()) / rods.size(); // doesn't matter even if we loose a bit or gain a few.
            for (ReactorRodBlockEntity rod : rods) {
                rod.fuel = target;
            }
        }
    }

    @Override
    public void invalidate() {
        if (capability != null)
            invalidateCapabilities();
        super.invalidate();
    }

    private class FuelAcceptorContainer extends SimpleContainer {

        FuelAcceptorContainer(int size) {
            super(size);
        }

        @Override
        public boolean canPlaceItem(int index, ItemStack stack) {
            return stack.is(CNATags.Item.NUCLEAR_FUEL.tag);
        }
    }

    private class FuelAcceptorInventoryHandler extends InvWrapper {

        public FuelAcceptorInventoryHandler() {
            super(container);
        }
    }
}
