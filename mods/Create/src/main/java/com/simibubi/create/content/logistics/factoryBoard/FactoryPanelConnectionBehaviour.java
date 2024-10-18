package com.simibubi.create.content.logistics.factoryBoard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllPackets;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;

import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.gui.ScreenOpener;
import net.createmod.catnip.utility.IntAttached;
import net.createmod.catnip.utility.NBTHelper;
import net.createmod.catnip.utility.Pointing;
import net.createmod.catnip.utility.VecHelper;
import net.createmod.catnip.utility.lang.Components;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

public class FactoryPanelConnectionBehaviour extends ScrollValueBehaviour {

	public static final BehaviourType<FactoryPanelBehaviour> UP = new BehaviourType<>();
	public static final BehaviourType<FactoryPanelBehaviour> DOWN = new BehaviourType<>();
	public static final BehaviourType<FactoryPanelBehaviour> LEFT = new BehaviourType<>();
	public static final BehaviourType<FactoryPanelBehaviour> RIGHT = new BehaviourType<>();

	public boolean inputModeActive;
	public String address;
	public Pointing side;

	private int timer;

	public FactoryPanelConnectionBehaviour(FactoryPanelBlockEntity be, Pointing side) {
		super(Components.empty(), be, new FactoryPanelConnectionSlotPositioning(be, side));
		this.side = side;
		onlyActiveWhen(() -> inputModeActive || be.hasInboundConnections(side));
		withFormatter(value -> inputModeActive ? formatInputSlot(value) : formatTimerSlot(value));
		inputModeActive = false;
		setValue(1);
		address = "";
	}

	@Override
	public void tick() {
		super.tick();
		if (getWorld().isClientSide)
			return;
		if (!(blockEntity instanceof FactoryPanelBlockEntity fpbe))
			return;
		if (!fpbe.hasInboundConnections(side) || fpbe.satisfied)
			return;
		if (value < 20)
			return;
		if (timer > 0) {
			timer = Math.min(timer, value);
			timer--;
			return;
		}

		timer = value;

		InventorySummary summary = fpbe.getAccurateSummary();
		boolean failed = false;

		ArrayList<IntAttached<ItemStack>> toRequest = new ArrayList<>();
		for (Entry<BlockPos, FactoryPanelConnection> entry : fpbe.inboundConnections.get(side)
			.entrySet()) {
			BlockPos targetPos = getPos().offset(entry.getKey());
			if (!getWorld().isLoaded(targetPos))
				return;
			if (!(getWorld().getBlockEntity(targetPos) instanceof FactoryPanelBlockEntity fpbe2))
				continue;
			Pointing fromSide = entry.getValue()
				.fromSide();
			FactoryPanelConnectionBehaviour connectionBehaviour = fpbe2.connections.get(fromSide);
			ItemStack item = fpbe2.behaviour.getFilter();
			if (connectionBehaviour.value == 0 || item.isEmpty())
				continue;
			if (summary.getCountOf(item) < connectionBehaviour.value) {
				sendEffect(targetPos, fromSide, getPos(), side, false);
				failed = true;
				continue;
			}

			toRequest.add(IntAttached.with(connectionBehaviour.value, item));
			sendEffect(targetPos, fromSide, getPos(), side, true);
		}

		if (failed)
			return;

		PackageOrder order = new PackageOrder(toRequest);
		fpbe.broadcastPackageRequest(order, null, address);
	}

	private void sendEffect(BlockPos fromPos, Pointing fromSide, BlockPos toPos, Pointing toSide, boolean success) {
		AllPackets.sendToNear(getWorld(), getPos(), 64,
			new FactoryPanelEffectPacket(fromPos, fromSide, toPos, toSide, success));
	}

	@Override
	public BehaviourType<?> getType() {
		return getTypeForSide(side);
	}

	public static BehaviourType<?> getTypeForSide(Pointing side) {
		return switch (side) {
		case DOWN -> DOWN;
		case LEFT -> LEFT;
		case RIGHT -> RIGHT;
		case UP -> UP;
		};
	}

	//

	@Override
	public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
		if (inputModeActive) {
			return new ValueSettingsBoard(CreateLang.temporaryText("Amount per Shipment")
				.component(), 64, 16,
				List.of(CreateLang.temporaryText("Items")
					.component()),
				new ValueSettingsFormatter(this::formatInputSettings));
		}
		return new ValueSettingsBoard(CreateLang.temporaryText("Request inputs every...")
			.component(), 60, 10, CreateLang.translatedOptions("generic.unit", "seconds", "minutes"),
			new ValueSettingsFormatter(this::formatTimerSettings));
	}

	@Override
	public void onShortInteract(Player player, InteractionHand hand, Direction side, BlockHitResult hitResult) {
		ItemStack itemInHand = player.getItemInHand(hand);
		if (AllItems.FACTORY_PANEL_CONNECTOR.isIn(itemInHand))
			itemInHand.useOn(new UseOnContext(player, hand, hitResult));
		else if (!inputModeActive)
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> displayScreen(player));
	}

	@Override
	public void setValueSettings(Player player, ValueSettings valueSetting, boolean ctrlHeld) {
		int value = valueSetting.value();
		int multiplier = inputModeActive ? 1 : switch (valueSetting.row()) {
		case 0 -> 20;
		default -> 60 * 20;
		};
		if (!valueSetting.equals(getValueSettings()))
			playFeedbackSound(this);
		setValue(Math.max(1, Math.max(1, value) * multiplier));
	}

	@Override
	public ValueSettings getValueSettings() {
		int row = 0;
		int value = this.value;

		if (value > 60 * 20) {
			value = value / (60 * 20);
			row = 1;
		} else if (value > 60) {
			value = value / 20;
			row = 0;
		}

		return new ValueSettings(row, value);
	}

	@Override
	public void setValue(int value) {
		if (value == this.value)
			return;
		this.value = value;
		blockEntity.setChanged();
		blockEntity.sendData();
	}

	public MutableComponent formatTimerSettings(ValueSettings settings) {
		int value = Math.max(1, settings.value());
		return Components.literal(switch (settings.row()) {
		case 0 -> "0:" + (value < 10 ? "0" : "") + value;
		default -> value + ":00";
		});
	}

	public MutableComponent formatInputSettings(ValueSettings settings) {
		int value = Math.max(1, settings.value());
		return Components.literal(value + "");
	}

	private String formatTimerSlot(int value) {
		if (value < 20 * 60)
			return (value / 20) + "s";
		return (value / 20 / 60) + "m";
	}

	private String formatInputSlot(int value) {
		return String.valueOf(value);
	}

	@Override
	public String getClipboardKey() {
		return "FactoryConnections";
	}

	//

	@Override
	public void write(CompoundTag nbt, boolean clientPacket) {
		String sideName = side.getSerializedName();
		if (inputModeActive)
			NBTHelper.putMarker(nbt, sideName + "Input");
		else {
			nbt.putInt(sideName + "Timer", timer);
			nbt.putString(sideName + "Address", address);
		}
		nbt.putInt(sideName + "ScrollValue", value);
	}

	@Override
	public void read(CompoundTag nbt, boolean clientPacket) {
		String sideName = side.getSerializedName();
		inputModeActive = nbt.getBoolean(sideName + "Input");
		value = nbt.getInt(sideName + "ScrollValue");
		address = nbt.getString(sideName + "Address");
		timer = nbt.getInt(sideName + "Timer");

		if (!clientPacket)
			return;
		label = inputModeActive ? CreateLang.temporaryText("Amount per Shipment")
			.component()
			: CreateLang.temporaryText("Request rate")
				.component();
	}

	@Override
	public int netId() {
		return 2 + side.ordinal();
	}

	@OnlyIn(value = Dist.CLIENT)
	protected void displayScreen(Player player) {
		if (player instanceof LocalPlayer)
			ScreenOpener.open(new FactoryPanelAddressInputScreen(this));
	}

	private static class FactoryPanelConnectionSlotPositioning extends ValueBoxTransform {

		private FactoryPanelBlockEntity be;
		private Pointing side;

		public FactoryPanelConnectionSlotPositioning(FactoryPanelBlockEntity be, Pointing side) {
			this.be = be;
			this.side = side;
		}

		@Override
		public boolean testHit(LevelAccessor level, BlockPos pos, BlockState state, Vec3 localHit) {
			Vec3 offset = getLocalOffset(level, pos, state);
			if (offset == null)
				return false;
			return localHit.distanceTo(offset) < scale / 4;
		}

		@Override
		public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
			Vec3 vec = VecHelper.voxelSpace(8, 14, 2.5f);
			vec = VecHelper.rotateCentered(vec, -side.getXRotation(), Axis.Z);
			vec = VecHelper.rotateCentered(vec, Mth.RAD_TO_DEG * be.getXRot(), Axis.X);
			vec = VecHelper.rotateCentered(vec, Mth.RAD_TO_DEG * be.getYRot(), Axis.Y);
			return vec;
		}

		@Override
		public void rotate(LevelAccessor level, BlockPos pos, BlockState state, PoseStack ms) {
			TransformStack.of(ms)
				.rotate(be.getYRot() + Mth.PI, Direction.UP)
				.rotate(-be.getXRot(), Direction.EAST);
		}

	}

}
