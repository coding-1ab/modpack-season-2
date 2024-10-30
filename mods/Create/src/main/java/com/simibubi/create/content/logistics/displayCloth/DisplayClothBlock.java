package com.simibubi.create.content.logistics.displayCloth;

import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.logistics.redstoneRequester.AutoRequestData;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.IHaveBigOutline;

import net.createmod.catnip.utility.placement.IPlacementHelper;
import net.createmod.catnip.utility.placement.PlacementHelpers;
import net.createmod.catnip.utility.placement.PlacementOffset;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DisplayClothBlock extends Block implements IHaveBigOutline, IWrenchable, IBE<DisplayClothBlockEntity> {

	public static final BooleanProperty HAS_BE = BooleanProperty.create("entity");

	private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

	private DyeColor colour;

	public DisplayClothBlock(Properties pProperties, DyeColor colour) {
		super(pProperties);
		this.colour = colour;
		registerDefaultState(defaultBlockState().setValue(HAS_BE, false));
	}

	public DisplayClothBlock(Properties pProperties, String type) {
		super(pProperties);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
		super.createBlockStateDefinition(pBuilder.add(HAS_BE));
	}

	@Override
	public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
		super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
		if (!(pPlacer instanceof Player player))
			return;

		AutoRequestData requestData = AutoRequestData.readFromItem(pLevel, player, pPos, pStack);
		if (requestData == null)
			return;

		pLevel.setBlockAndUpdate(pPos, pState.setValue(HAS_BE, true));
		withBlockEntityDo(pLevel, pPos, dcbe -> {
			dcbe.requestData = requestData;
			dcbe.owner = player.getUUID();
			if (dcbe.requestData.isValid)
				dcbe.interactAsOwner(player);
		});
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
		BlockHitResult ray) {
		if (ray.getDirection() == Direction.DOWN)
			return InteractionResult.PASS;
		
		ItemStack heldItem = player.getItemInHand(hand);
		boolean shiftKeyDown = player.isShiftKeyDown();
		if (!player.mayBuild())
			return InteractionResult.PASS;

		IPlacementHelper placementHelper = PlacementHelpers.get(placementHelperId);
		if (placementHelper.matchesItem(heldItem)) {
			if (shiftKeyDown)
				return InteractionResult.PASS;
			placementHelper.getOffset(player, world, state, pos, ray)
				.placeInWorld(world, (BlockItem) heldItem.getItem(), player, hand, ray);
			return InteractionResult.SUCCESS;
		}

		if ((shiftKeyDown || heldItem.isEmpty()) && !state.getValue(HAS_BE))
			return InteractionResult.PASS;

		if (!world.isClientSide() && !state.getValue(HAS_BE))
			world.setBlockAndUpdate(pos, state.cycle(HAS_BE));
		if (world.isClientSide())
			return InteractionResult.SUCCESS;

		return onBlockEntityUse(world, pos, dcbe -> dcbe.use(player));
	}

	@SuppressWarnings("deprecation")
	@Override
	public List<ItemStack> getDrops(BlockState pState,
		net.minecraft.world.level.storage.loot.LootParams.Builder pParams) {
		List<ItemStack> drops = super.getDrops(pState, pParams);

		if (!(pParams.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof DisplayClothBlockEntity dcbe))
			return drops;
		if (!dcbe.isShop())
			return drops;

		for (ItemStack stack : drops) {
			if (AllItemTags.DISPLAY_CLOTHS.matches(stack)) {
				ItemStack drop = new ItemStack(this);
				dcbe.requestData.writeToItem(dcbe.getBlockPos(), drop);
				return List.of(drop);
			}
		}

		return drops;
	}

	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		return colour == null ? AllShapes.DISPLAY_CLOTH_OCCLUSION : AllShapes.DISPLAY_CLOTH;
	}

	@Override
	public VoxelShape getInteractionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
		return colour == null ? AllShapes.DISPLAY_CLOTH_OCCLUSION : AllShapes.DISPLAY_CLOTH;
	}

	@Override
	public VoxelShape getOcclusionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
		return AllShapes.DISPLAY_CLOTH_OCCLUSION;
	}

	@Override
	public boolean canSurvive(BlockState p_152922_, LevelReader p_152923_, BlockPos p_152924_) {
		return true;
	}

	@Nullable
	public DyeColor getColor() {
		return colour;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return state.getValue(HAS_BE) ? IBE.super.newBlockEntity(pos, state) : null;
	}

	@Override
	public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pMovedByPiston) {
		if (!pNewState.getOptionalValue(HAS_BE)
			.orElse(false))
			pNewState = Blocks.AIR.defaultBlockState();

		IBE.onRemove(pState, pLevel, pPos, pNewState);
	}

	@Override
	public Class<DisplayClothBlockEntity> getBlockEntityClass() {
		return DisplayClothBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends DisplayClothBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.DISPLAY_CLOTH.get();
	}

	private static class PlacementHelper implements IPlacementHelper {

		@Override
		public Predicate<ItemStack> getItemPredicate() {
			return i -> AllItemTags.DISPLAY_CLOTHS.matches(i.getItem());
		}

		@Override
		public Predicate<BlockState> getStatePredicate() {
			return s -> s.getBlock() instanceof DisplayClothBlock;
		}

		@Override
		public PlacementOffset getOffset(Player player, Level world, BlockState state, BlockPos pos,
			BlockHitResult ray) {
			List<Direction> directions = IPlacementHelper.orderedByDistanceExceptAxis(pos, ray.getLocation(), Axis.Y,
				dir -> world.getBlockState(pos.relative(dir))
					.canBeReplaced());

			if (directions.isEmpty())
				return PlacementOffset.fail();
			else
				return PlacementOffset.success(pos.relative(directions.get(0)), s -> s);
		}
	}
}
