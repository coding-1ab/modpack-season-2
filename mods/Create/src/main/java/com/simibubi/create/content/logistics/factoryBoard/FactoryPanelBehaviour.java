package com.simibubi.create.content.logistics.factoryBoard;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.logistics.filter.FilterItem;
import com.simibubi.create.content.logistics.filter.FilterItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.stockTicker.StockCheckingBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;

import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.utility.VecHelper;
import net.createmod.catnip.utility.lang.Components;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class FactoryPanelBehaviour extends FilteringBehaviour {

	int lastReportedLevelInStorage;

	public FactoryPanelBehaviour(FactoryPanelBlockEntity be) {
		super(be, new FactoryPanelSlotPositioning(be));
		showCount();
		count = 0;
		diamondShape = true;
	}

	@Override
	public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
		int maxAmount = 100;
		return new ValueSettingsBoard(CreateLang.temporaryText("Target Amount in Storage")
			.component(), maxAmount, 10,
			List.of(CreateLang.temporaryText("Items")
				.component(),
				CreateLang.temporaryText("Stacks")
					.component()),
			new ValueSettingsFormatter(this::formatValue));
	}

	@Override
	public MutableComponent getLabel() {
		return CreateLang.temporaryText("Target Amount")
			.component();
	}

	@Override
	public ValueSettings getValueSettings() {
		return new ValueSettings(upTo ? 0 : 1, count);
	}

	@Override
	public MutableComponent getCountLabelForValueBox() {
		if (filter.isEmpty())
			return Components.empty();

		int inStorage = getLevelInStorage();
		String stacks = upTo ? "" : "\u25A4";

		if (count == 0) {
			return CreateLang.text(inStorage + stacks)
				.color(0xF1EFE8)
				.component();
		}

		return CreateLang.text("   " + inStorage + stacks)
			.color(inStorage >= count ? 0xD7FFA8 : 0xFFBFA8)
			.add(CreateLang.text("/")
				.style(ChatFormatting.WHITE))
			.add(CreateLang.text(count + stacks + "  ")
				.color(0xF1EFE8))
			.component();
	}

	@Override
	public void tick() {
		super.tick();
		if (getWorld().isClientSide())
			return;
		int levelInStorage = getLevelInStorage();
		if (lastReportedLevelInStorage != levelInStorage) {
			lastReportedLevelInStorage = levelInStorage;
			blockEntity.sendData();
		}
	}

	private int getLevelInStorage() {
		if (getWorld().isClientSide())
			return lastReportedLevelInStorage;
		InventorySummary summary = ((StockCheckingBlockEntity) blockEntity).getRecentSummary();
		return summary.getCountOf(getFilter()) / (upTo ? 1 : getFilter().getMaxStackSize());
	}

	@Override
	public void write(CompoundTag nbt, boolean clientPacket) {
		super.write(nbt, clientPacket);
		nbt.putInt("LastLevel", lastReportedLevelInStorage);
	}

	@Override
	public void read(CompoundTag nbt, boolean clientPacket) {
		filter = FilterItemStack.of(nbt.getCompound("Filter"));
		count = nbt.getInt("FilterAmount");
		upTo = nbt.getBoolean("UpTo");
		lastReportedLevelInStorage = nbt.getInt("LastLevel");
	}
	
	@Override
	public float getRenderDistance() {
		return 64;
	}

	@Override
	public MutableComponent formatValue(ValueSettings value) {
		return value.value() == 0 ? Components.literal("*")
			: Components.literal(Math.max(0, value.value()) + ((value.row() == 0) ? "" : "\u25A4"));
	}

	@Override
	public boolean setFilter(ItemStack stack) {
		ItemStack filter = stack.copy();
		if (stack.getItem() instanceof FilterItem)
			return false;
		this.filter = FilterItemStack.of(filter);
		blockEntity.setChanged();
		blockEntity.sendData();
		return true;
	}

	@Override
	public void setValueSettings(Player player, ValueSettings settings, boolean ctrlDown) {
		if (getValueSettings().equals(settings))
			return;
		count = Math.max(0, settings.value());
		upTo = settings.row() == 0;
		blockEntity.setChanged();
		blockEntity.sendData();
		playFeedbackSound(this);
	}

	private static class FactoryPanelSlotPositioning extends ValueBoxTransform {

		private FactoryPanelBlockEntity be;

		public FactoryPanelSlotPositioning(FactoryPanelBlockEntity be) {
			this.be = be;
		}

		@Override
		public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
			Vec3 vec = VecHelper.voxelSpace(8, 8, 2.5f);
			vec = VecHelper.rotateCentered(vec, Mth.RAD_TO_DEG * be.getXRot(), Axis.X);
			vec = VecHelper.rotateCentered(vec, Mth.RAD_TO_DEG * be.getYRot(), Axis.Y);
			return vec;
		}

		@Override
		public boolean testHit(LevelAccessor level, BlockPos pos, BlockState state, Vec3 localHit) {
			Vec3 offset = getLocalOffset(level, pos, state);
			if (offset == null)
				return false;
			return localHit.distanceTo(offset) < scale / 3;
		}

		@Override
		public float getScale() {
			return super.getScale() * 1.125f;
		}

		@Override
		public void rotate(LevelAccessor level, BlockPos pos, BlockState state, PoseStack ms) {
			TransformStack.of(ms)
				.rotate(be.getYRot() + Mth.PI, Direction.UP)
				.rotate(-be.getXRot(), Direction.EAST);
		}

	}

}
