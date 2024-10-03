package com.simibubi.create.content.logistics.displayCloth;

import java.util.ArrayList;
import java.util.UUID;

import javax.annotation.Nullable;

import com.simibubi.create.AllEntityTypes;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllPackets;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.logistics.displayCloth.ShoppingListItem.ShoppingList;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.redstoneRequester.AutoRequestData;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.simibubi.create.content.schematics.requirement.ISpecialEntityItemRequirement;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.content.schematics.requirement.ItemRequirement.ItemUseType;
import com.simibubi.create.foundation.networking.ISyncPersistentData;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.foundation.utility.IInteractionChecker;

import net.createmod.catnip.utility.IntAttached;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;

public class DisplayClothEntity extends HangingEntity implements IEntityAdditionalSpawnData,
	ISpecialEntityItemRequirement, ISyncPersistentData, IInteractionChecker, MenuProvider {

	public AutoRequestData requestData = new AutoRequestData();

	public ItemStack paymentItem = ItemStack.EMPTY;
	public int paymentAmount = 1;
	public UUID owner;

	public DisplayClothEntity(EntityType<? extends HangingEntity> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
	}

	public DisplayClothEntity(Level world, BlockPos pos, Direction direction) {
		super(AllEntityTypes.DISPLAY_CLOTH.get(), world, pos);
		setDirection(direction);
	}

	public static EntityType.Builder<?> build(EntityType.Builder<?> builder) {
		@SuppressWarnings("unchecked")
		EntityType.Builder<DisplayClothEntity> entityBuilder = (EntityType.Builder<DisplayClothEntity>) builder;
		return entityBuilder;
	}

	@Override
	public Packet<ClientGamePacketListener> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	public void addAdditionalSaveData(CompoundTag pCompound) {
		pCompound.putByte("Facing", (byte) this.direction.get2DDataValue());
		requestData.write(pCompound);
		pCompound.put("Payment", paymentItem.serializeNBT());
		pCompound.putInt("PaymentAmount", paymentAmount);
		pCompound.putUUID("OwnerUUID", owner);
		super.addAdditionalSaveData(pCompound);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag pCompound) {
		direction = Direction.from2DDataValue(pCompound.getByte("Facing"));
		requestData = AutoRequestData.read(pCompound);
		paymentItem = ItemStack.of(pCompound.getCompound("Payment"));
		paymentAmount = pCompound.getInt("PaymentAmount");
		owner = pCompound.getUUID("OwnerUUID");
		super.readAdditionalSaveData(pCompound);
		setDirection(direction);
	}

	@Override
	public InteractionResult interact(Player player, InteractionHand pHand) {
		if (level().isClientSide())
			return InteractionResult.SUCCESS;
		if (pHand != InteractionHand.MAIN_HAND)
			return InteractionResult.PASS;
		if (!owner.equals(player.getUUID()) || (!paymentItem.isEmpty() && !player.isShiftKeyDown()))
			return interactAsCustomer(player);

		return interactAsOwner(player);
	}

	public InteractionResult interactAsOwner(Player player) {
		if (player instanceof ServerPlayer sp)
			NetworkHooks.openScreen(sp, this, buf -> {
				buf.writeVarInt(getId());
				requestData.encodedRequest.write(buf);
			});
		return InteractionResult.SUCCESS;
	}

	public InteractionResult interactAsCustomer(Player player) {
		ItemStack itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND);
		ItemStack prevListItem = null;
		boolean addOntoList = false;

		// Remove other lists from inventory
		for (int i = 0; i < 9; i++) {
			ItemStack item = player.getInventory()
				.getItem(i);
			if (!AllItems.SHOPPING_LIST.isIn(item))
				continue;
			prevListItem = item;
			addOntoList = true;
			player.getInventory()
				.setItem(i, ItemStack.EMPTY);
		}

		// add onto existing list if in hand
		if (AllItems.SHOPPING_LIST.isIn(itemInHand)) {
			prevListItem = itemInHand;
			addOntoList = true;
		}

		if (!itemInHand.isEmpty() && !addOntoList) {
			CreateLang.text("Empty hand required to start a shopping list")
				.sendStatus(player);
			AllSoundEvents.DENY.playOnServer(level(), blockPosition());
			return InteractionResult.SUCCESS;
		}

		if (paymentItem.isEmpty()) {
			CreateLang.text("Shop owner must set a price first")
				.sendStatus(player);
			AllSoundEvents.DENY.playOnServer(level(), blockPosition());
			return InteractionResult.SUCCESS;
		}

		UUID tickerID = null;
		BlockPos tickerPos = requestData.targetOffset.offset(blockPosition());
		if (level().getBlockEntity(tickerPos) instanceof StockTickerBlockEntity stbe && stbe.isKeeperPresent())
			tickerID = stbe.behaviour.freqId;

		int stockLevel = getStockLevelForTrade();

		if (tickerID == null) {
			CreateLang.text("Stock keeper missing")
				.style(ChatFormatting.RED)
				.sendStatus(player);
			AllSoundEvents.DENY.playOnServer(level(), blockPosition());
			return InteractionResult.SUCCESS;
		}

		if (stockLevel == 0) {
			CreateLang.text("Out of Stock")
				.style(ChatFormatting.RED)
				.sendStatus(player);
			AllSoundEvents.DENY.playOnServer(level(), blockPosition());
			return InteractionResult.SUCCESS;
		}

		ShoppingList list = new ShoppingList(new ArrayList<>(), owner, tickerID);

		if (addOntoList) {
			ShoppingList prevList = ShoppingListItem.getList(prevListItem);
			if (owner.equals(prevList.shopOwner()) && tickerID.equals(prevList.shopNetwork()))
				list = prevList;
			else
				addOntoList = false;
		}

		BlockPos posWithPixelY = getPosWithPixelY();

		if (list.getPurchases(posWithPixelY) == stockLevel) {
			CreateLang.text("Limited stock available")
				.style(ChatFormatting.RED)
				.sendStatus(player);
			AllSoundEvents.DENY.playOnServer(level(), blockPosition());

		} else {
			list.addPurchases(posWithPixelY, 1);
			if (!addOntoList)
				CreateLang.text("Use this list to add more to your purchase")
					.color(0xeeeeee)
					.sendStatus(player);
			if (!addOntoList)
				level().playSound(null, blockPosition(), SoundEvents.BOOK_PAGE_TURN, getSoundSource(), 1, 1.5f);
			AllSoundEvents.CONFIRM.playOnServer(level(), blockPosition());
		}

		ItemStack newListItem =
			ShoppingListItem.saveList(AllItems.SHOPPING_LIST.asStack(), list, requestData.encodedTargetAdress);

		if (player.getItemInHand(InteractionHand.MAIN_HAND)
			.isEmpty())
			player.setItemInHand(InteractionHand.MAIN_HAND, newListItem);
		else
			player.getInventory()
				.placeItemBackInInventory(newListItem);

		return InteractionResult.SUCCESS;
	}

	public int getStockLevelForTrade() {
		BlockPos tickerPos = requestData.targetOffset.offset(blockPosition());
		if (!(level().getBlockEntity(tickerPos) instanceof StockTickerBlockEntity stbe))
			return 0;

		InventorySummary recentSummary = null;

		if (level().isClientSide()) {
			if (stbe.getTicksSinceLastUpdate() > 15)
				stbe.refreshClientStockSnapshot();
			recentSummary = stbe.getLastClientsideStockSnapshotAsSummary();
		} else
			recentSummary = stbe.getRecentSummary();

		if (recentSummary == null)
			return 0;

		int smallestQuotient = Integer.MAX_VALUE;
		for (IntAttached<ItemStack> entry : requestData.encodedRequest.stacks())
			smallestQuotient =
				Math.min(smallestQuotient, recentSummary.getCountOf(entry.getValue()) / entry.getFirst());

		return smallestQuotient;
	}

	public BlockPos getPosWithPixelY() {
		return new BlockPos(blockPosition().getX(), Mth.floor(getY() * 16), blockPosition().getZ());
	}

	public static DisplayClothEntity getAtPosWithPixelY(EntityGetter level, BlockPos pos) {
		for (DisplayClothEntity entity : level.getEntitiesOfClass(DisplayClothEntity.class,
			new AABB(new BlockPos(pos.getX(), pos.getY() / 16, pos.getZ())).inflate(1)))
			if (pos.equals(entity.getPosWithPixelY()))
				return entity;
		return null;
	}

	@Override
	protected float getEyeHeight(Pose pPose, EntityDimensions pDimensions) {
		return 0;
	}

	@Override
	protected void recalculateBoundingBox() {
		setBoundingBox(new AABB(position(), position()).inflate(.5, 0, .5)
			.expandTowards(0, 1 / 16f, 0));
	}

	@Override
	public void setPos(double pX, double pY, double pZ) {
		setPosRaw(pX, pY, pZ);
		super.setPos(pX, pY, pZ);
	}

	@Override
	public void dropItem(@Nullable Entity pBrokenEntity) {
		if (!this.level()
			.getGameRules()
			.getBoolean(GameRules.RULE_DOENTITYDROPS))
			return;

		playSound(SoundEvents.WOOL_BREAK, 1.0F, 1.0F);
		if (pBrokenEntity instanceof Player) {
			Player player = (Player) pBrokenEntity;
			if (player.getAbilities().instabuild)
				return;
		}

		ItemStack item = AllItems.DISPLAY_CLOTH.asStack();
		if (requestData != null)
			requestData.writeToItem(pos, item);

		spawnAtLocation(item);
	}

	@Override
	public boolean survives() {
		BlockPos blockpos = BlockPos.containing(position().relative(Direction.DOWN, 1 / 16f));
		BlockState blockstate = level().getBlockState(blockpos);

		boolean fenceLikeBelow = blockstate.getBlock() instanceof FenceBlock
			|| blockstate.getBlock() instanceof FenceGateBlock || blockstate.getBlock() instanceof WallBlock;

		AABB boundingBox = getBoundingBox();
		if (fenceLikeBelow)
			if (blockstate.getShape(level(), blockpos)
				.bounds()
				.move(blockpos)
				.intersects(boundingBox))
				return false;

		if (!fenceLikeBelow)
			if (!level().noCollision(this))
				return false;

		if (blockstate.getShape(level(), blockpos)
			.isEmpty())
			return false;

		return level().getEntities(this, boundingBox, HANGING_ENTITY)
			.isEmpty();
	}

	@Override
	public int getWidth() {
		return 16;
	}

	@Override
	public int getHeight() {
		return 16;
	}

	@Override
	public ItemStack getPickedResult(HitResult target) {
		return AllItems.DISPLAY_CLOTH.asStack();
	}

	@Override
	public ItemRequirement getRequiredItems() {
		return new ItemRequirement(ItemUseType.CONSUME, AllItems.DISPLAY_CLOTH.get());
	}

	@Override
	public void playPlacementSound() {
		this.playSound(SoundEvents.WOOL_PLACE, 1.0F, 1.0F);
	}

	@Override
	public void moveTo(double p_70012_1_, double p_70012_3_, double p_70012_5_, float p_70012_7_, float p_70012_8_) {
		this.setPos(p_70012_1_, p_70012_3_, p_70012_5_);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void lerpTo(double p_180426_1_, double p_180426_3_, double p_180426_5_, float p_180426_7_, float p_180426_8_,
		int p_180426_9_, boolean p_180426_10_) {
		BlockPos blockpos = this.pos.offset(
			BlockPos.containing(p_180426_1_ - this.getX(), p_180426_3_ - this.getY(), p_180426_5_ - this.getZ()));
		this.setPos((double) blockpos.getX(), (double) blockpos.getY(), (double) blockpos.getZ());
	}

	@Override
	public void onPersistentDataUpdated() {}

	@Override
	public void writeSpawnData(FriendlyByteBuf buffer) {
		CompoundTag compound = new CompoundTag();
		addAdditionalSaveData(compound);
		buffer.writeNbt(compound);
		buffer.writeNbt(getPersistentData());
	}

	@Override
	public void readSpawnData(FriendlyByteBuf additionalData) {
		readAdditionalSaveData(additionalData.readNbt());
		getPersistentData().merge(additionalData.readNbt());
	}

	@Override
	public boolean canPlayerUse(Player player) {
		return isAlive() && player.closerThan(this, 10);
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return DisplayClothPricingMenu.create(id, inv, this);
	}

	@Override
	public Component getDisplayName() {
		return AllItems.DISPLAY_CLOTH.get()
			.getDescription();
	}

	public void sendData() {
		if (level().isClientSide())
			return;
		AllPackets.getChannel()
			.send(PacketDistributor.TRACKING_ENTITY.with(() -> this),
				new DisplayClothPacketToClient(getId(), requestData.encodedRequest, paymentItem, paymentAmount));
	}

}
