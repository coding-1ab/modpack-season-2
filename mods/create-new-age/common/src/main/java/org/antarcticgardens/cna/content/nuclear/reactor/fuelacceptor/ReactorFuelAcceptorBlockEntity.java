package org.antarcticgardens.cna.content.nuclear.reactor.fuelacceptor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.antarcticgardens.cna.CreateNewAge;
import org.antarcticgardens.cna.CNATags;
import org.antarcticgardens.cna.content.nuclear.reactor.RodFindingReactorBlockEntity;
import org.antarcticgardens.cna.content.nuclear.reactor.rod.ReactorRodBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ReactorFuelAcceptorBlockEntity extends RodFindingReactorBlockEntity implements Container {
    public ReactorFuelAcceptorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        container = new SimpleContainer(3);
    }

    public SimpleContainer container;

    @Override
    public int getContainerSize() {
        return container.getContainerSize();
    }

    @Override
    public boolean isEmpty() {
        return container.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return container.getItem(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return container.removeItem(slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return container.removeItemNoUpdate(slot);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void setItem(int slot, @NotNull ItemStack stack) {
        container.setItem(slot, stack);
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public void clearContent() {
        container.clearContent();
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return stack.is(CNATags.Item.NUCLEAR_FUEL.tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        container.fromTag(tag.getList("contents", Tag.TAG_COMPOUND));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("contents", container.createTag());
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
}
