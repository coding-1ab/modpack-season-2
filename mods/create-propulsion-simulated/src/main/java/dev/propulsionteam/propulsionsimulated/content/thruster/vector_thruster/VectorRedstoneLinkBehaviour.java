package dev.propulsionteam.propulsionsimulated.content.thruster.vector_thruster;

import com.simibubi.create.Create;
import com.simibubi.create.content.redstone.link.IRedstoneLinkable;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler.Frequency;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import net.createmod.catnip.data.Couple;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;

public class VectorRedstoneLinkBehaviour extends BlockEntityBehaviour implements IRedstoneLinkable {

    public static final BehaviourType<VectorRedstoneLinkBehaviour> TYPE = new BehaviourType<>();

    private enum Mode {
        TRANSMIT, RECEIVE
    }

    private Frequency frequencyFirst;
    private Frequency frequencyLast;
    private final ValueBoxTransform firstSlot;
    private final ValueBoxTransform secondSlot;

    public boolean newPosition;
    private Mode mode;
    private IntSupplier transmission;
    private IntConsumer signalCallback;

    protected VectorRedstoneLinkBehaviour(SmartBlockEntity be, Pair<ValueBoxTransform, ValueBoxTransform> slots) {
        super(be);
        frequencyFirst = Frequency.EMPTY;
        frequencyLast = Frequency.EMPTY;
        firstSlot = slots.getLeft();
        secondSlot = slots.getRight();
        newPosition = true;
    }

    public static VectorRedstoneLinkBehaviour receiver(SmartBlockEntity be,
            Pair<ValueBoxTransform, ValueBoxTransform> slots, IntConsumer signalCallback) {
        VectorRedstoneLinkBehaviour behaviour = new VectorRedstoneLinkBehaviour(be, slots);
        behaviour.signalCallback = signalCallback;
        behaviour.mode = Mode.RECEIVE;
        return behaviour;
    }

    public static VectorRedstoneLinkBehaviour transmitter(SmartBlockEntity be,
            Pair<ValueBoxTransform, ValueBoxTransform> slots, IntSupplier transmission) {
        VectorRedstoneLinkBehaviour behaviour = new VectorRedstoneLinkBehaviour(be, slots);
        behaviour.transmission = transmission;
        behaviour.mode = Mode.TRANSMIT;
        return behaviour;
    }

    @Override
    public boolean isListening() {
        return mode == Mode.RECEIVE;
    }

    @Override
    public int getTransmittedStrength() {
        return mode == Mode.TRANSMIT ? transmission.getAsInt() : 0;
    }

    @Override
    public void setReceivedStrength(int networkPower) {
        if (!newPosition)
            return;
        signalCallback.accept(networkPower);
    }

    public void notifySignalChange() {
        Create.REDSTONE_LINK_NETWORK_HANDLER.updateNetworkOf(getWorld(), this);
    }

    @Override
    public void initialize() {
        super.initialize();
        if (getWorld().isClientSide)
            return;
        Create.REDSTONE_LINK_NETWORK_HANDLER.addToNetwork(getWorld(), this);
        newPosition = true;
    }

    @Override
    public Couple<Frequency> getNetworkKey() {
        return Couple.create(frequencyFirst, frequencyLast);
    }

    @Override
    public void unload() {
        super.unload();
        if (getWorld().isClientSide)
            return;
        Create.REDSTONE_LINK_NETWORK_HANDLER.removeFromNetwork(getWorld(), this);
    }

    @Override
    public boolean isSafeNBT() {
        return true;
    }

    @Override
    public void write(CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(nbt, registries, clientPacket);
        nbt.put("VectorLink2FrequencyFirst", frequencyFirst.getStack().saveOptional(registries));
        nbt.put("VectorLink2FrequencyLast", frequencyLast.getStack().saveOptional(registries));
        nbt.putLong("VectorLink2LastKnownPosition", blockEntity.getBlockPos().asLong());
    }

    @Override
    public void read(CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket) {
        long positionInTag = blockEntity.getBlockPos().asLong();
        long positionKey = nbt.getLong("VectorLink2LastKnownPosition");
        newPosition = positionInTag != positionKey;

        super.read(nbt, registries, clientPacket);
        frequencyFirst = Frequency.of(ItemStack.parseOptional(registries, nbt.getCompound("VectorLink2FrequencyFirst")));
        frequencyLast = Frequency.of(ItemStack.parseOptional(registries, nbt.getCompound("VectorLink2FrequencyLast")));
    }

    public void setFrequency(boolean first, ItemStack stack) {
        stack = stack.copy();
        stack.setCount(1);
        ItemStack toCompare = first ? frequencyFirst.getStack() : frequencyLast.getStack();
        boolean changed = !ItemStack.isSameItemSameComponents(stack, toCompare);

        if (changed)
            Create.REDSTONE_LINK_NETWORK_HANDLER.removeFromNetwork(getWorld(), this);

        if (first)
            frequencyFirst = Frequency.of(stack);
        else
            frequencyLast = Frequency.of(stack);

        if (!changed)
            return;

        blockEntity.sendData();
        Create.REDSTONE_LINK_NETWORK_HANDLER.addToNetwork(getWorld(), this);
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    public boolean testHit(Boolean first, Vec3 hit) {
        BlockState state = blockEntity.getBlockState();
        Vec3 localHit = hit.subtract(Vec3.atLowerCornerOf(blockEntity.getBlockPos()));
        return (first ? firstSlot : secondSlot).testHit(getWorld(), getPos(), state, localHit);
    }

    @Override
    public boolean isAlive() {
        Level level = getWorld();
        BlockPos pos = getPos();
        if (blockEntity.isChunkUnloaded())
            return false;
        if (blockEntity.isRemoved())
            return false;
        if (!level.isLoaded(pos))
            return false;
        return level.getBlockEntity(pos) == blockEntity;
    }

    @Override
    public BlockPos getLocation() {
        return getPos();
    }

    public ValueBoxTransform getFirstSlot() {
        return firstSlot;
    }

    public ValueBoxTransform getSecondSlot() {
        return secondSlot;
    }

    public Frequency getFrequency(boolean first) {
        return first ? frequencyFirst : frequencyLast;
    }
}
