package com.simibubi.create.content.logistics.displayCloth;

import java.util.List;

import com.simibubi.create.content.logistics.redstoneRequester.AutoRequestData;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class DisplayClothItem extends Item {

	public DisplayClothItem(Properties p_i48487_1_) {
		super(p_i48487_1_);
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Direction face = ctx.getClickedFace();
		Player player = ctx.getPlayer();
		ItemStack stack = ctx.getItemInHand();
		BlockPos pos = ctx.getClickedPos()
			.relative(face);

		if (player != null && !player.mayUseItemAt(pos, face, stack))
			return InteractionResult.FAIL;

		Level world = ctx.getLevel();
		DisplayClothEntity hangingentity = new DisplayClothEntity(world, pos, ctx.getHorizontalDirection());
		CompoundTag compoundnbt = stack.getTag();

		hangingentity.setPos(pos.getX() + 0.5, Math.min(ctx.getClickLocation()
			.y(), pos.getY() + 1), pos.getZ() + 0.5);

		if (compoundnbt != null)
			EntityType.updateCustomEntityTag(world, player, hangingentity, compoundnbt);
		if (!hangingentity.survives())
			return InteractionResult.CONSUME;

		hangingentity.requestData = AutoRequestData.readFromItem(world, player, hangingentity.blockPosition(), stack);
		hangingentity.owner = player.getUUID();

		if (!world.isClientSide) {
			hangingentity.playPlacementSound();
			world.addFreshEntity(hangingentity);
		}

		stack.shrink(1);

		if (hangingentity.requestData.isValid)
			hangingentity.interactAsOwner(player);

		hangingentity.sendData();
		return InteractionResult.sidedSuccess(world.isClientSide);
	}

	@Override
	public void appendHoverText(ItemStack pStack, Level pLevel, List<Component> pTooltipComponents,
		TooltipFlag pIsAdvanced) {
		super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
		RedstoneRequesterBlock.appendRequesterTooltip(pStack, pTooltipComponents);
	}

	protected boolean canPlace(Player p_200127_1_, Direction p_200127_2_, ItemStack p_200127_3_, BlockPos p_200127_4_) {
		return p_200127_1_.mayUseItemAt(p_200127_4_, p_200127_2_, p_200127_3_);
	}

}
