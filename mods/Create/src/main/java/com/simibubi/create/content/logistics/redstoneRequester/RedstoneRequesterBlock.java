package com.simibubi.create.content.logistics.redstoneRequester;

import java.util.List;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class RedstoneRequesterBlock extends Block implements IBE<RedstoneRequesterBlockEntity> {

	public RedstoneRequesterBlock(Properties pProperties) {
		super(pProperties);
	}

	@Override
	public boolean shouldCheckWeakPower(BlockState state, SignalGetter level, BlockPos pos, Direction side) {
		return false;
	}

	public static void programRequester(ServerPlayer player, StockTickerBlockEntity be, PackageOrder order,
		String address) {
		ItemStack stack = player.getMainHandItem();
		if (!AllBlocks.REDSTONE_REQUESTER.isIn(stack) && !AllItems.DISPLAY_CLOTH.isIn(stack))
			return;

		AutoRequestData autoRequestData = new AutoRequestData();
		autoRequestData.encodedRequest = order;
		autoRequestData.encodedTargetAdress = address;
		autoRequestData.targetOffset = be.getBlockPos();
		autoRequestData.targetDim = player.level()
			.dimension()
			.location()
			.toString();

		autoRequestData.writeToItem(BlockPos.ZERO, stack);
		player.setItemInHand(InteractionHand.MAIN_HAND, stack);
	}

	@Override
	public void appendHoverText(ItemStack pStack, BlockGetter pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
		super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
		appendRequesterTooltip(pStack, pTooltip);
	}

	public static void appendRequesterTooltip(ItemStack pStack, List<Component> pTooltip) {
		if (!pStack.hasTag())
			return;

		CompoundTag compoundnbt = pStack.getTag();
//		if (compoundnbt.contains("EncodedAddress", Tag.TAG_STRING))
//			pTooltip.add(Components.literal("-> " + compoundnbt.getString("EncodedAddress"))
//				.withStyle(ChatFormatting.GOLD));

		if (!compoundnbt.contains("EncodedRequest", Tag.TAG_COMPOUND))
			return;

		PackageOrder contents = PackageOrder.read(compoundnbt.getCompound("EncodedRequest"));
		for (BigItemStack entry : contents.stacks()) {
			pTooltip.add(entry.stack.getHoverName()
				.copy()
				.append(" x")
				.append(String.valueOf(entry.count))
				.withStyle(ChatFormatting.GRAY));
		}

		CreateLang.translate("logistically_linked.tooltip_clear")
			.style(ChatFormatting.DARK_GRAY)
			.addTo(pTooltip);
	}

	@Override
	public void setPlacedBy(Level pLevel, BlockPos requesterPos, BlockState pState, LivingEntity pPlacer,
		ItemStack pStack) {
		Player player = pPlacer instanceof Player ? (Player) pPlacer : null;
		withBlockEntityDo(pLevel, requesterPos,
			rrbe -> rrbe.requestData = AutoRequestData.readFromItem(pLevel, player, requesterPos, pStack));
	}

	@Override
	public List<ItemStack> getDrops(BlockState pState, Builder pParams) {
		@SuppressWarnings("deprecation")
		List<ItemStack> drops = super.getDrops(pState, pParams);
		BlockEntity blockEntity = pParams.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
		if (!(blockEntity instanceof RedstoneRequesterBlockEntity rrbe))
			return drops;

		for (ItemStack itemStack : drops)
			if (itemStack.is(this.asItem()))
				rrbe.requestData.writeToItem(rrbe.getBlockPos(), itemStack);

		return drops;
	}

	@Override
	public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pNeighborBlock,
		BlockPos pNeighborPos, boolean pMovedByPiston) {
		if (pLevel.isClientSide())
			return;
		withBlockEntityDo(pLevel, pPos, RedstoneRequesterBlockEntity::onRedstonePowerChanged);
	}

	@Override
	public Class<RedstoneRequesterBlockEntity> getBlockEntityClass() {
		return RedstoneRequesterBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends RedstoneRequesterBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.REDSTONE_REQUESTER.get();
	}

}
