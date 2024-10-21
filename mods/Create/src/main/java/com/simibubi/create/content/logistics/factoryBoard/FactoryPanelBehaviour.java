package com.simibubi.create.content.logistics.factoryBoard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import org.joml.Math;

import com.simibubi.create.AllPackets;
import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock.PanelSlot;
import com.simibubi.create.content.logistics.filter.FilterItem;
import com.simibubi.create.content.logistics.filter.FilterItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packagerLink.RequestPromise;
import com.simibubi.create.content.logistics.packagerLink.RequestPromiseQueue;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
import com.simibubi.create.content.logistics.stockTicker.StockCheckingBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.gui.ScreenOpener;
import net.createmod.catnip.utility.NBTHelper;
import net.createmod.catnip.utility.animation.LerpedFloat;
import net.createmod.catnip.utility.animation.LerpedFloat.Chaser;
import net.createmod.catnip.utility.lang.Components;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

public class FactoryPanelBehaviour extends FilteringBehaviour {

	public static final BehaviourType<FactoryPanelBehaviour> TOP_LEFT = new BehaviourType<>();
	public static final BehaviourType<FactoryPanelBehaviour> TOP_RIGHT = new BehaviourType<>();
	public static final BehaviourType<FactoryPanelBehaviour> BOTTOM_LEFT = new BehaviourType<>();
	public static final BehaviourType<FactoryPanelBehaviour> BOTTOM_RIGHT = new BehaviourType<>();
	public static final int REQUEST_INTERVAL = 100;

	public Map<FactoryPanelPosition, FactoryPanelConnection> targetedBy;
	public Set<FactoryPanelPosition> targeting;

	public boolean satisfied;
	public boolean promisedSatisfied;
	public String recipeAddress;
	public int recipeOutput;
	public LerpedFloat bulb;
	public PanelSlot slot;
	public int promiseClearingInterval;
	public boolean forceClearPromises;

	private boolean active;
	private int lastReportedLevelInStorage;
	private int lastReportedPromises;
	private int timer;

	public FactoryPanelBehaviour(FactoryPanelBlockEntity be, PanelSlot slot) {
		super(be, new FactoryPanelSlotPositioning(slot));
		this.slot = slot;
		this.targetedBy = new HashMap<>();
		this.targeting = new HashSet<>();
		this.count = 0;
		this.satisfied = false;
		this.promisedSatisfied = false;
		this.recipeAddress = "";
		this.recipeOutput = 1;
		this.active = false;
		this.forceClearPromises = false;
		this.promiseClearingInterval = -1;
		this.bulb = LerpedFloat.linear()
			.startWithValue(0)
			.chase(0, 0.45, Chaser.EXP);
	}

	@Nullable
	public static FactoryPanelBehaviour at(BlockAndTintGetter world, FactoryPanelPosition pos) {
		if (world instanceof Level l && !l.isLoaded(pos.pos()))
			return null;
		if (!(world.getBlockEntity(pos.pos()) instanceof FactoryPanelBlockEntity fpbe))
			return null;
		FactoryPanelBehaviour behaviour = fpbe.panels.get(pos.slot());
		if (!behaviour.active)
			return null;
		return behaviour;
	}

	@Override
	public void tick() {
		super.tick();
		if (getWorld().isClientSide()) {
			bulb.updateChaseTarget(satisfied ? 1 : 0);
			bulb.tickChaser();
			return;
		}
		tickStorageMonitor();
		tickRequests();
	}

	private void tickStorageMonitor() {
		ItemStack filter = getFilter();
		int inStorage = getLevelInStorage();
		int promised = getPromised();
		int demand = getAmount() * (upTo ? 1 : filter.getMaxStackSize());
		boolean shouldSatisfy = filter.isEmpty() || inStorage >= demand;
		boolean shouldPromiseSatisfy = filter.isEmpty() || inStorage + promised >= demand;

		if (lastReportedLevelInStorage == inStorage && lastReportedPromises == promised && satisfied == shouldSatisfy
			&& promisedSatisfied == shouldPromiseSatisfy)
			return;

		lastReportedLevelInStorage = inStorage;
		satisfied = shouldSatisfy;
		lastReportedPromises = promised;
		promisedSatisfied = shouldPromiseSatisfy;
		blockEntity.sendData();
	}

	private void tickRequests() {
		if (targetedBy.isEmpty())
			return;
		if (!(blockEntity instanceof FactoryPanelBlockEntity fpbe))
			return;
		if (satisfied || promisedSatisfied)
			return;
		if (timer > 0) {
			timer = Math.min(timer, REQUEST_INTERVAL);
			timer--;
			return;
		}

		timer = REQUEST_INTERVAL;

		InventorySummary summary = fpbe.getAccurateSummary();
		boolean failed = false;

		List<BigItemStack> toRequest = new ArrayList<>();
		for (FactoryPanelConnection connection : targetedBy.values()) {
			FactoryPanelBehaviour source = at(getWorld(), connection.from());
			if (source == null)
				return;
			ItemStack item = source.getFilter();
			int amount = connection.amount();
			if (amount == 0 || item.isEmpty() || summary.getCountOf(item) < amount) {
				sendEffect(connection.from(), false);
				failed = true;
				continue;
			}

			toRequest.add(new BigItemStack(item, amount));
			sendEffect(connection.from(), true);
		}

		if (failed)
			return;

		PackageOrder order = new PackageOrder(toRequest);
		fpbe.broadcastPackageRequest(order, null, recipeAddress);

		RequestPromiseQueue promises = Create.LOGISTICS.getQueuedPromises(fpbe.behaviour.freqId);
		if (promises != null)
			promises.add(new RequestPromise(new BigItemStack(getFilter(), recipeOutput)));
	}

	private void sendEffect(FactoryPanelPosition fromPos, boolean success) {
		AllPackets.sendToNear(getWorld(), getPos(), 64,
			new FactoryPanelEffectPacket(fromPos, getPanelPosition(), success));
	}

	public void addConnection(FactoryPanelPosition fromPos) {
		if (targetedBy.size() >= 9)
			return;
		FactoryPanelBehaviour source = at(getWorld(), fromPos);
		if (source == null)
			return;
		source.targeting.add(getPanelPosition());
		targetedBy.putIfAbsent(fromPos, new FactoryPanelConnection(fromPos, 1));
		blockEntity.notifyUpdate();
	}

	public FactoryPanelPosition getPanelPosition() {
		return new FactoryPanelPosition(getPos(), slot);
	}

	@Override
	public void onShortInteract(Player player, InteractionHand hand, Direction side, BlockHitResult hitResult) {
		if (getFilter().isEmpty()) {
			super.onShortInteract(player, hand, side, hitResult);
			return;
		}

		if (!player.level().isClientSide)
			return;
		if (FactoryPanelConnectionHandler.panelClicked(getWorld(), player, getPanelPosition()))
			return;

		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> displayScreen(player));
	}

	public void enable() {
		active = true;
		blockEntity.notifyUpdate();
	}

	public void disable() {
		destroy();
		active = false;
		targetedBy = new HashMap<>();
		targeting = new HashSet<>();
		count = 0;
		satisfied = false;
		promisedSatisfied = false;
		recipeAddress = "";
		recipeOutput = 1;
		setFilter(ItemStack.EMPTY);
		blockEntity.notifyUpdate();
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public void destroy() {
		FactoryPanelPosition panelPosition = getPanelPosition();
		for (FactoryPanelPosition position : targetedBy.keySet()) {
			FactoryPanelBehaviour source = at(getWorld(), position);
			if (source != null) {
				source.targeting.remove(panelPosition);
				source.blockEntity.sendData();
			}
		}
		for (FactoryPanelPosition position : targeting) {
			FactoryPanelBehaviour target = at(getWorld(), position);
			if (target != null) {
				target.targetedBy.remove(panelPosition);
				target.blockEntity.sendData();
			}
		}

		super.destroy();
	}

	public int getLevelInStorage() {
		if (getWorld().isClientSide())
			return lastReportedLevelInStorage;
		if (getFilter().isEmpty())
			return 0;

		InventorySummary summary = ((StockCheckingBlockEntity) blockEntity).getRecentSummary();
		return summary.getCountOf(getFilter()) / (upTo ? 1 : getFilter().getMaxStackSize());
	}

	public int getPromised() {
		if (getWorld().isClientSide())
			return lastReportedPromises;
		if (getFilter().isEmpty())
			return 0;

		UUID freqId = ((StockCheckingBlockEntity) blockEntity).behaviour.freqId;
		RequestPromiseQueue promises = Create.LOGISTICS.getQueuedPromises(freqId);
		if (forceClearPromises)
			promises.forceClear(getFilter());

		return promises == null ? 0
			: promises.getTotalPromisedAndRemoveExpired(getFilter(), getPromiseExpiryTimeInTicks());
	}

	private int getPromiseExpiryTimeInTicks() {
		if (promiseClearingInterval == -1)
			return -1;
		if (promiseClearingInterval == 0)
			return 20 * 30;

		return promiseClearingInterval * 20 * 60;
	}

	@Override
	public void write(CompoundTag nbt, boolean clientPacket) {
		if (!active)
			return;

		CompoundTag panelTag = new CompoundTag();
		super.write(panelTag, clientPacket);

		panelTag.putInt("LastLevel", lastReportedLevelInStorage);
		panelTag.putInt("LastPromised", lastReportedPromises);
		panelTag.putBoolean("Satisfied", satisfied);
		panelTag.putBoolean("PromisedSatisfied", promisedSatisfied);
		panelTag.put("Targeting", NBTHelper.writeCompoundList(targeting, FactoryPanelPosition::write));
		panelTag.put("TargetedBy", NBTHelper.writeCompoundList(targetedBy.values(), FactoryPanelConnection::write));
		panelTag.putString("RecipeAddress", recipeAddress);
		panelTag.putInt("RecipeOutput", recipeOutput);
		panelTag.putInt("PromiseClearingInterval", promiseClearingInterval);

		nbt.put(CreateLang.asId(slot.name()), panelTag);
	}

	@Override
	public void read(CompoundTag nbt, boolean clientPacket) {
		CompoundTag panelTag = nbt.getCompound(CreateLang.asId(slot.name()));
		if (panelTag.isEmpty()) {
			active = false;
			return;
		}

		active = true;
		filter = FilterItemStack.of(panelTag.getCompound("Filter"));
		count = panelTag.getInt("FilterAmount");
		upTo = panelTag.getBoolean("UpTo");
		lastReportedLevelInStorage = panelTag.getInt("LastLevel");
		lastReportedPromises = panelTag.getInt("LastPromised");
		satisfied = panelTag.getBoolean("Satisfied");
		promisedSatisfied = panelTag.getBoolean("PromisedSatisfied");
		promiseClearingInterval = panelTag.getInt("PromiseClearingInterval");

		targeting.clear();
		NBTHelper.iterateCompoundList(panelTag.getList("Targeting", Tag.TAG_COMPOUND),
			c -> targeting.add(FactoryPanelPosition.read(c)));

		targetedBy.clear();
		NBTHelper.iterateCompoundList(panelTag.getList("TargetedBy", Tag.TAG_COMPOUND),
			c -> targetedBy.put(FactoryPanelPosition.read(c), FactoryPanelConnection.read(c)));

		recipeAddress = panelTag.getString("RecipeAddress");
		recipeOutput = panelTag.getInt("RecipeOutput");
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
		if (blockEntity instanceof FactoryPanelBlockEntity fpbe)
			fpbe.redraw = true;
		blockEntity.setChanged();
		blockEntity.sendData();
		playFeedbackSound(this);
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
		String key = "Target Amount";

		if (getFilter().isEmpty())
			key = "New Factory task";
		else if (getAmount() == 0 || targetedBy.isEmpty())
			key = getFilter().getHoverName()
				.getString();
		else {
			String stacks = upTo ? "" : "\u25A4";
			key = getFilter().getHoverName()
				.getString() + " -> " + getAmount() + stacks;
			if (!satisfied)
				key += " (In Progress)";
		}

		return CreateLang.temporaryText(key)
			.component();
	}

	@Override
	public ValueSettings getValueSettings() {
		return new ValueSettings(upTo ? 0 : 1, count);
	}

	@Override
	public MutableComponent getTip() {
		return CreateLang.translateDirect(
			filter.isEmpty() ? "logistics.filter.click_to_set" : "logistics.factory_panel.click_to_configure");
	}

	@Override
	public MutableComponent getCountLabelForValueBox() {
		if (filter.isEmpty())
			return Components.empty();

		int inStorage = getLevelInStorage();
		int promised = getPromised();
		String stacks = upTo ? "" : "\u25A4";

		if (count == 0) {
			return CreateLang.text(inStorage + stacks)
				.color(0xF1EFE8)
				.component();
		}

		return CreateLang.text("   " + inStorage + stacks)
			.color(inStorage >= count ? 0xD7FFA8 : 0xFFBFA8)
			.add(CreateLang.text(promised == 0 ? "" : "+" + promised))
			.add(CreateLang.text("/")
				.style(ChatFormatting.WHITE))
			.add(CreateLang.text(count + stacks + "  ")
				.color(0xF1EFE8))
			.component();
	}

	@Override
	public int netId() {
		return 2 + slot.ordinal();
	}

	@Override
	public boolean isCountVisible() {
		return !getFilter().isEmpty();
	}

	@Override
	public BehaviourType<?> getType() {
		return getTypeForSlot(slot);
	}

	public static BehaviourType<?> getTypeForSlot(PanelSlot slot) {
		return switch (slot) {
		case BOTTOM_LEFT -> BOTTOM_LEFT;
		case TOP_LEFT -> TOP_LEFT;
		case TOP_RIGHT -> TOP_RIGHT;
		case BOTTOM_RIGHT -> BOTTOM_RIGHT;
		};
	}

	@OnlyIn(value = Dist.CLIENT)
	public void displayScreen(Player player) {
		if (player instanceof LocalPlayer)
			ScreenOpener.open(new FactoryPanelScreen(this));
	}

}
