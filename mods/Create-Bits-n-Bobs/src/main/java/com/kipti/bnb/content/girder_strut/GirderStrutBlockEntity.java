package com.kipti.bnb.content.girder_strut;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GirderStrutBlockEntity extends SmartBlockEntity {

    private final Set<BlockPos> connections = new HashSet<>();

    public GirderStrutBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void addConnection(BlockPos other) {
        if (!other.equals(getBlockPos())) {
            connections.add(other.immutable());
            setChanged();
            sendData();
        }
    }

    public void removeConnection(BlockPos pos) {
        connections.remove(pos);
        setChanged();
        sendData();
    }

    public boolean hasConnectionTo(BlockPos pos) {
        return connections.contains(pos);
    }

    public int connectionCount() {
        return connections.size();
    }

    public Set<BlockPos> getConnectionsCopy() {
        return Set.copyOf(connections);
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        ListTag list = new ListTag();
        for (BlockPos p : connections) {
            CompoundTag ct = new CompoundTag();
            ct.putInt("X", p.getX());
            ct.putInt("Y", p.getY());
            ct.putInt("Z", p.getZ());
            list.add(ct);
        }
        tag.put("Connections", list);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        connections.clear();
        if (tag.contains("Connections", Tag.TAG_LIST)) {
            ListTag list = tag.getList("Connections", Tag.TAG_COMPOUND);
            for (Tag t : list) {
                if (t instanceof CompoundTag ct) {
                    connections.add(new BlockPos(ct.getInt("X"), ct.getInt("Y"), ct.getInt("Z")));
                }
            }
        }
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

}
