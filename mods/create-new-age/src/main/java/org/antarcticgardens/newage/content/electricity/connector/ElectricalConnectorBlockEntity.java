package org.antarcticgardens.newage.content.electricity.connector;

import com.simibubi.create.foundation.utility.NBTHelper;
import earth.terrarium.botarium.common.energy.base.BotariumEnergyBlock;
import earth.terrarium.botarium.common.energy.impl.WrappedBlockEnergyContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.antarcticgardens.newage.content.electricity.network.ElectricalNetwork;
import org.antarcticgardens.newage.content.electricity.network.NetworkEnergyContainer;
import org.antarcticgardens.newage.content.electricity.wire.WireType;

import java.util.*;

public class ElectricalConnectorBlockEntity extends BlockEntity implements BotariumEnergyBlock<WrappedBlockEnergyContainer> {
    private final Map<ElectricalConnectorBlockEntity, WireType> connectors = new HashMap<>();
    private final Map<BlockPos, WireType> connectorPositions = new HashMap<>();

    private ElectricalNetwork network;
    private WrappedBlockEnergyContainer energyContainer;

    protected boolean tickedBefore = false;

    public ElectricalConnectorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        setNetwork(new ElectricalNetwork(this));
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        ListTag list = new ListTag();

        for (Map.Entry<BlockPos, WireType> e : connectorPositions.entrySet()) {
            CompoundTag compound = new CompoundTag();
            compound.put("position", NBTHelper.writeVec3i(e.getKey()));
            compound.put("wire", StringTag.valueOf(e.getValue().name()));

            list.add(compound);
        }

        nbt.put("connections", list);
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        ListTag list = nbt.getList("connections", Tag.TAG_COMPOUND);
        connectorPositions.clear();

        for (Tag listTag : list.toArray(new Tag[0])) {
            if (listTag instanceof CompoundTag ct && ct.contains("position") && ct.contains("wire")) {
                BlockPos pos = new BlockPos(NBTHelper.readVec3i((ListTag) ct.get("position")));
                WireType wire = WireType.valueOf(ct.getString("wire"));

                connectorPositions.put(pos, wire);
            }
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    public Map<BlockPos, WireType> getConnectorPositions() {
        return Collections.unmodifiableMap(connectorPositions);
    }

    public BlockPos getSupportingBlockPos() {
        return getBlockPos().relative(getBlockState().getValue(BlockStateProperties.FACING).getOpposite());
    }

    protected void tick() {
        if (!tickedBefore) {
            updateNetwork();
            tickedBefore = true;
        }
    }

    protected void neighborChanged() {
        network.updateConsumers();
    }

    private void updateNetwork() {
        for (Map.Entry<BlockPos, WireType> e : connectorPositions.entrySet()) {
            if (getLevel().getBlockEntity(e.getKey()) instanceof ElectricalConnectorBlockEntity connector)
                connect(connector, e.getValue());
        }
    }

    protected void remove(Level level) {
        if (!level.isClientSide())
            network.destroy();

        for (Map.Entry<ElectricalConnectorBlockEntity, WireType> e : connectors.entrySet()) {
            e.getKey().disconnect(this);
            e.getKey().updateNetwork();

            e.getKey().setChanged();

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.getChunkSource().blockChanged(e.getKey().getBlockPos());

                Containers.dropContents(level, getBlockPos(), NonNullList.of(ItemStack.EMPTY, e.getValue().getDroppedItem()));
            }
        }
    }

    public void connect(ElectricalConnectorBlockEntity entity, WireType wireType) {
        if (!connectors.containsKey(entity)) {
            connectors.put(entity, wireType);
            entity.connect(this, wireType);
        }

        network.addNode(entity);

        if (!connectorPositions.containsKey(entity.getBlockPos()))
            connectorPositions.put(entity.getBlockPos(), wireType);

        entity.setChanged();
        setChanged();

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.getChunkSource().blockChanged(entity.getBlockPos());
            serverLevel.getChunkSource().blockChanged(getBlockPos());
        }
    }

    public boolean isConnected(BlockPos pos) {
        return connectorPositions.containsKey(pos);
    }

    public void disconnect(ElectricalConnectorBlockEntity entity) {
        connectors.remove(entity);
        connectorPositions.remove(entity.getBlockPos());
    }

    public Map<ElectricalConnectorBlockEntity, WireType> getConnectors() {
        return Collections.unmodifiableMap(connectors);
    }

    public void setNetwork(ElectricalNetwork network) {
        this.network = network;
        energyContainer = new WrappedBlockEnergyContainer(this, new NetworkEnergyContainer(this, this.network));
    }

    public ElectricalNetwork getNetwork() {
        return network;
    }

    @Override
    public WrappedBlockEnergyContainer getEnergyStorage() {
        return energyContainer;
    }
}
