package com.simibubi.create.content.logistics.factoryBoard;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Math;

import com.simibubi.create.content.logistics.stockTicker.StockCheckingBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.createmod.catnip.utility.NBTHelper;
import net.createmod.catnip.utility.Pointing;
import net.createmod.catnip.utility.math.AngleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;

public class FactoryPanelBlockEntity extends StockCheckingBlockEntity {

	public FactoryPanelBehaviour panelBehaviour;

	public EnumMap<Pointing, Map<BlockPos, FactoryPanelConnection>> inboundConnections = new EnumMap<>(Pointing.class);
	public EnumMap<Pointing, FactoryPanelConnectionBehaviour> connections;

	public boolean satisfied;
	public boolean promisedSatisfied;

	public FactoryPanelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		satisfied = false;
		promisedSatisfied = false;
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		behaviours.add(panelBehaviour = new FactoryPanelBehaviour(this));
		connections = new EnumMap<>(Pointing.class);
		for (Pointing side : Pointing.values()) {
			FactoryPanelConnectionBehaviour e = new FactoryPanelConnectionBehaviour(this, side);
			connections.put(side, e);
			behaviours.add(e);
		}
	}

	public void addConnection(BlockPos fromPos, Pointing fromSide, Pointing toSide) {
		if (level.getBlockEntity(fromPos) instanceof FactoryPanelBlockEntity fpbe)
			fpbe.activateInput(fromSide);
		inboundConnections.computeIfAbsent(toSide, $ -> new HashMap<>())
			.put(fromPos.subtract(worldPosition), new FactoryPanelConnection(fromSide));
		connections.get(toSide)
			.setValue(Math.max(connections.get(toSide)
				.getValue(), 200));
		notifyUpdate();
	}

	public void activateInput(Pointing onSide) {
		connections.get(onSide).inputModeActive = true;
		notifyUpdate();
	}

	public boolean isInput(Pointing onSide) {
		return connections.get(onSide).inputModeActive;
	}

	public boolean hasInboundConnections(Pointing onSide) {
		return inboundConnections.containsKey(onSide);
	}

	@Override
	public void tick() {
		super.tick();
		if (level.isClientSide)
			return;

		ItemStack filter = panelBehaviour.getFilter();
		if (filter.isEmpty() || panelBehaviour.getAmount() == 0) {
			setSatisfied(true);
			return;
		}

		int inStorage = panelBehaviour.getLevelInStorage();
		int promised = panelBehaviour.getPromised();
		int demand = panelBehaviour.getAmount() * (panelBehaviour.upTo ? 1 : filter.getMaxStackSize());
		
		setSatisfied(inStorage >= demand);
		setPromisedSatisfied(inStorage + promised >= demand);
	}

	public void setPromisedSatisfied(boolean promisedSatisfied) {
		if (this.promisedSatisfied == promisedSatisfied)
			return;
		this.promisedSatisfied = promisedSatisfied;
		notifyUpdate();
	}

	public void setSatisfied(boolean satisfied) {
		if (this.satisfied == satisfied)
			return;
		this.satisfied = satisfied;
		notifyUpdate();
	}

	@Override
	protected void write(CompoundTag tag, boolean clientPacket) {
		super.write(tag, clientPacket);
		tag.putBoolean("Satisfied", satisfied);
		tag.putBoolean("PromisedSatisfied", promisedSatisfied);

		tag.put("Sides", NBTHelper.writeCompoundList(inboundConnections.entrySet(), e -> {
			CompoundTag nbt = new CompoundTag();
			nbt.put("Connections", NBTHelper.writeCompoundList(e.getValue()
				.entrySet(), e2 -> {
					CompoundTag nbt2 = e2.getValue()
						.write();
					nbt2.put("Pos", NbtUtils.writeBlockPos(e2.getKey()));
					return nbt2;
				}));
			nbt.putString("Side", e.getKey()
				.name());
			return nbt;
		}));
	}

	@Override
	protected void read(CompoundTag tag, boolean clientPacket) {
		super.read(tag, clientPacket);
		satisfied = tag.getBoolean("Satisfied");
		promisedSatisfied = tag.getBoolean("PromisedSatisfied");

		inboundConnections.clear();
		NBTHelper.iterateCompoundList(tag.getList("Sides", Tag.TAG_COMPOUND), nbt -> {
			HashMap<BlockPos, FactoryPanelConnection> map = new HashMap<>();
			NBTHelper.iterateCompoundList(nbt.getList("Connections", Tag.TAG_COMPOUND),
				nbt2 -> map.put(NbtUtils.readBlockPos(nbt2.getCompound("Pos")), FactoryPanelConnection.read(nbt2)));
			inboundConnections.put(Pointing.valueOf(nbt.getString("Side")), map);
		});
	}

	public float getYRot() {
		Direction facing = getBlockState().getValue(FactoryPanelBlock.FACING);
		AttachFace face = getBlockState().getValue(FactoryPanelBlock.FACE);
		return (face == AttachFace.CEILING ? Mth.PI : 0) + AngleHelper.rad(AngleHelper.horizontalAngle(facing));
	}

	public float getXRot() {
		AttachFace face = getBlockState().getValue(FactoryPanelBlock.FACE);
		return face == AttachFace.CEILING ? Mth.PI / 2 : face == AttachFace.FLOOR ? -Mth.PI / 2 : 0;
	}

}
