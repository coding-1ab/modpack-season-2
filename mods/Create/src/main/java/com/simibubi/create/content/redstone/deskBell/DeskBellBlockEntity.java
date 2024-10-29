package com.simibubi.create.content.redstone.deskBell;

import java.util.List;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.createmod.catnip.utility.NBTHelper;
import net.createmod.catnip.utility.animation.LerpedFloat;
import net.createmod.catnip.utility.animation.LerpedFloat.Chaser;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class DeskBellBlockEntity extends SmartBlockEntity {

	public LerpedFloat animation = LerpedFloat.linear()
		.startWithValue(0);
	
	public boolean ding;

	public DeskBellBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void tick() {
		super.tick();
		animation.tickChaser();
	}
	
	@Override
	protected void write(CompoundTag tag, boolean clientPacket) {
		super.write(tag, clientPacket);
		if (clientPacket && ding)
			NBTHelper.putMarker(tag, "Ding");
		ding = false;
	}
	
	@Override
	protected void read(CompoundTag tag, boolean clientPacket) {
		super.read(tag, clientPacket);
		if (clientPacket && tag.contains("Ding"))
			ding();
	}

	public void ding() {
		if (!level.isClientSide) {
			ding = true;
			sendData();
			return;
		}
		
		animation.startWithValue(1)
			.chase(0, 0.05, Chaser.LINEAR);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

}
