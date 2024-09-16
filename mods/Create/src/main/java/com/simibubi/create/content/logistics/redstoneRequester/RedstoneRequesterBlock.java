package com.simibubi.create.content.logistics.redstoneRequester;

import java.util.List;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.utility.IntAttached;
import net.createmod.catnip.utility.lang.Components;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
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
		if (!AllBlocks.REDSTONE_REQUESTER.isIn(stack))
			return;

		CompoundTag tag = stack.getOrCreateTag();
		addBEtag(order, address, tag);

		tag.put("StockTickerPos", NbtUtils.writeBlockPos(be.getBlockPos()));
		tag.putString("StockTickerDim", player.level()
			.dimension()
			.location()
			.toString());

		player.setItemInHand(InteractionHand.MAIN_HAND, stack);
	}

	@Override
	public void appendHoverText(ItemStack pStack, BlockGetter pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
		super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
		CompoundTag compoundnbt = pStack.getOrCreateTag();
		if (!compoundnbt.contains("BlockEntityTag", Tag.TAG_COMPOUND))
			return;
		compoundnbt = compoundnbt.getCompound("BlockEntityTag");

		if (compoundnbt.contains("EncodedAddress", Tag.TAG_STRING))
			pTooltip.add(Components.literal("-> " + compoundnbt.getString("EncodedAddress"))
				.withStyle(ChatFormatting.GOLD));

		if (!compoundnbt.contains("EncodedRequest", Tag.TAG_COMPOUND))
			return;

		PackageOrder contents = PackageOrder.read(compoundnbt.getCompound("EncodedRequest"));
		for (IntAttached<ItemStack> entry : contents.stacks()) {
			pTooltip.add(entry.getSecond()
				.getHoverName()
				.copy()
				.append(" x")
				.append(String.valueOf(entry.getFirst()))
				.withStyle(ChatFormatting.GRAY));
		}
	}

	private static void addBEtag(PackageOrder order, String address, CompoundTag tag) {
		CompoundTag teTag = new CompoundTag();
		teTag.put("EncodedRequest", order.write());
		teTag.putString("EncodedAddress", address);
		tag.put("BlockEntityTag", teTag);
	}

	@Override
	public void setPlacedBy(Level pLevel, BlockPos requesterPos, BlockState pState, LivingEntity pPlacer,
		ItemStack pStack) {
		super.setPlacedBy(pLevel, requesterPos, pState, pPlacer, pStack);

		withBlockEntityDo(pLevel, requesterPos, rrbe -> {
			CompoundTag tag = pStack.getTag();
			BlockPos tickerPos = NbtUtils.readBlockPos(tag.getCompound("StockTickerPos"));
			String tickerDim = tag.getString("StockTickerDim");

			rrbe.targetOffset = tickerPos.subtract(requesterPos);
			rrbe.targetDim = tickerDim;
			rrbe.isValid = tickerPos.closerThan(requesterPos, 128) && tickerDim.equals(pLevel.dimension()
				.location()
				.toString());

			if (pPlacer instanceof Player player)
				CreateLang
					.translate(
						rrbe.isValid ? "redstone_requester.keeper_connected" : "redstone_requester.keeper_too_far_away")
					.style(rrbe.isValid ? ChatFormatting.WHITE : ChatFormatting.RED)
					.sendStatus(player);
		});

	}

	@Override
	public List<ItemStack> getDrops(BlockState pState, Builder pParams) {
		@SuppressWarnings("deprecation")
		List<ItemStack> drops = super.getDrops(pState, pParams);
		BlockEntity blockEntity = pParams.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
		if (!(blockEntity instanceof RedstoneRequesterBlockEntity rrbe))
			return drops;

		for (ItemStack itemStack : drops) {
			if (!itemStack.is(this.asItem()))
				continue;

			CompoundTag tag = itemStack.getOrCreateTag();

			addBEtag(rrbe.encodedRequest, rrbe.encodedTargetAdress, tag);
			tag.put("StockTickerPos", NbtUtils.writeBlockPos(rrbe.getBlockPos()
				.offset(rrbe.targetOffset)));
			tag.putString("StockTickerDim", rrbe.targetDim);
			itemStack.setTag(tag);
		}

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
