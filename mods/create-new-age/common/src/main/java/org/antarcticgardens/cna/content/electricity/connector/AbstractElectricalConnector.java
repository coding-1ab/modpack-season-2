package org.antarcticgardens.cna.content.electricity.connector;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.antarcticgardens.cna.content.electricity.network.ElectricalNetwork;
import org.antarcticgardens.cna.content.electricity.network.EnergyBlockEntity;
import org.antarcticgardens.cna.content.electricity.network.NetworkTicker;
import org.antarcticgardens.cna.content.electricity.wire.Wire;
import org.antarcticgardens.cna.content.electricity.wire.WireType;
import org.antarcticgardens.esl.energy.EnergyStorage;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.stream.Stream;

public abstract class AbstractElectricalConnector extends SmartBlockEntity implements IElectricityNode {
    protected ElectricalNetwork network = new ElectricalNetwork();
    private final List<Pair<BlockPos, WireType>> toVerify = new ArrayList<>();
    private boolean sendToClient = false;

    public AbstractElectricalConnector(BlockEntityType<? extends AbstractElectricalConnector> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public void setLevel(@NonNull Level level) {
        super.setLevel(level);
        if (level instanceof ServerLevel serverLevel) {
            network.addNode(this);
            NetworkTicker.addNetwork(serverLevel, network);
        }
    }

    @Override
    public boolean isValid() {
        return !this.isRemoved();
    }

    public void sendToClient() {
        sendToClient = true;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        Objects.requireNonNull(network);
        ListTag list = new ListTag();

        network.getNeighbours(this).forEach(neighbour -> {
            if (!(neighbour.getFirst() instanceof AbstractElectricalConnector connector)) {
                return;
            }
            Wire wire = neighbour.getSecond();

            CompoundTag compound = new CompoundTag();
            compound.put("position", NBTHelper.writeVec3i(connector.getBlockPos()));
            compound.put("wire", StringTag.valueOf(wire.type().name()));

            list.add(compound);
        });

        tag.put("connections", list);
        super.write(tag, registries, clientPacket);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        ListTag list = tag.getList("connections", Tag.TAG_COMPOUND);
        ElectricalNetwork localNetwork = new ElectricalNetwork();
        localNetwork.addNode(this);

        for (Tag listTag : list.toArray(new Tag[0])) {
            if (listTag instanceof CompoundTag ct && ct.contains("position") && ct.contains("wire")) {
                BlockPos pos = new BlockPos(NBTHelper.readVec3i((ListTag) ct.get("position")));
                WireType wireType = WireType.valueOf(ct.getString("wire"));

                if (level == null) {
                    toVerify.add(Pair.of(pos, wireType));
                } else {
                    BlockEntity entity = level.getBlockEntity(pos);
                    if (!(entity instanceof AbstractElectricalConnector connector)) {
                        continue;
                    }

                    localNetwork.addNode(connector);
                    localNetwork.addConnection(this, connector, wireType);
                }
            }
        }

        this.network = localNetwork;

        super.read(tag, registries, clientPacket);
    }

    @Override
    public @NonNull CompoundTag getUpdateTag(HolderLookup.@NonNull Provider registries) {
        return saveWithoutMetadata(registries);
    }

    public BlockPos getSupportingBlockPos() {
        return this.getBlockPos();
    }

    public abstract Direction getFacing();

    protected void serverTick() {
        Objects.requireNonNull(level);

        if (!toVerify.isEmpty()) {
            toVerify.forEach(neighbour -> {
                BlockPos pos = neighbour.getFirst();
                WireType wireType = neighbour.getSecond();

                BlockEntity entity = level.getBlockEntity(pos);
                if (!(entity instanceof AbstractElectricalConnector connector)) {
                    return;
                }

                network.addNode(connector);
                network.addConnection(this, connector, wireType);
            });
            toVerify.clear();
        }

        Direction dir = getFacing();
        BlockPos support = getSupportingBlockPos();
        BlockEntity entity = level.getBlockEntity(support);
        BlockState state = getBlockState();

        if (entity != null && (!(entity instanceof AbstractElectricalConnector) || entity == this)) {
            EnergyStorage storage = EnergyStorage.findForBlock(level, support, dir);

            if (storage != null) {
                if (storage.supportsInsertion()) {
                    network.notifySink(this, new EnergyBlockEntity(entity, storage));
                }

                ElectricalConnectorMode mode = getBlockState().getValue(ElectricalConnectorBlock.MODE);
                if (storage.supportsExtraction() && mode.pull) {
                    network.notifySource(this, new EnergyBlockEntity(entity, storage));
                }
            }
        }

        if (sendToClient) {
            sendToClient = false;
            notifyUpdate();
        }
    }

    public void neighborChanged() {}

    @Override
    public void onNearConnectionChanged() {
        invalidateRenderBoundingBox();
    }

    public void remove(ServerLevel level) {
        if (level.isClientSide()) {
            return;
        }

        Objects.requireNonNull(network);
        network.getNeighbours(this).forEach(neighbour -> {
            if (!(neighbour.getFirst() instanceof AbstractElectricalConnector)) {
                return;
            }

            Containers.dropContents(level, getBlockPos(), NonNullList.of(ItemStack.EMPTY, neighbour.getSecond().type().getDroppedItem()));
        });

        network.removeNode(this);
    }

    public void connect(AbstractElectricalConnector entity, WireType wireType) {
        Objects.requireNonNull(network);
        ElectricalNetwork otherNetwork = entity.getNetwork();
        Objects.requireNonNull(otherNetwork);

        network.mergeOther(otherNetwork);
        network.addConnection(this, entity, wireType);
    }

    public void disconnect(AbstractElectricalConnector entity) {
        Objects.requireNonNull(network);
        network.disconnect(this, entity);
    }

    public boolean isConnected(AbstractElectricalConnector connector) {
        Objects.requireNonNull(network);
        return network.getEdge(this, connector) != null;
    }

    @Override
    public void setNetwork(@NonNull ElectricalNetwork network) {
        this.network = network;
    }

    @Override
    public ElectricalNetwork getNetwork() {
        return network;
    }

    public Vec3 getConnectionPoint() {
        return new Vec3(0.5f, 0.5f, 0.5f);
    }

    @Override
    protected AABB createRenderBoundingBox() {
        Objects.requireNonNull(network);

        double[] coordinates = Stream.concat(network.getNeighbours(this).map(pair -> {
                    if (!(pair.getFirst() instanceof AbstractElectricalConnector connector)) {
                        return null;
                    }
                    return connector;
                }), Stream.of(this)).filter(Objects::nonNull)
                .map(AbstractElectricalConnector::getConnectionPoint)
                .collect(() -> new double[]{
                        Double.POSITIVE_INFINITY, // minX
                        Double.POSITIVE_INFINITY, // minY
                        Double.POSITIVE_INFINITY, // minZ
                        Double.NEGATIVE_INFINITY, // maxX
                        Double.NEGATIVE_INFINITY, // maxY
                        Double.NEGATIVE_INFINITY, // maxZ
                }, (acc, point) -> {
                    acc[0] = Math.min(acc[0], point.x);
                    acc[1] = Math.min(acc[1], point.y);
                    acc[2] = Math.min(acc[2], point.z);
                    acc[3] = Math.max(acc[3], point.x);
                    acc[4] = Math.max(acc[4], point.y);
                    acc[5] = Math.max(acc[5], point.z);
                }, (a, b) -> {
                });

        AABB box = new AABB(coordinates[0], coordinates[1], coordinates[2], coordinates[3], coordinates[4], coordinates[5]);
        return box.inflate(1.0);
    }
}
