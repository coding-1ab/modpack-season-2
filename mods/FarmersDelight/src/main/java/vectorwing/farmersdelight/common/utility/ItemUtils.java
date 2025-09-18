package vectorwing.farmersdelight.common.utility;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.items.IItemHandler;

/**
 * Util for handling ItemStacks and inventories containing them.
 */
public class ItemUtils
{
	/**
	 * Places a block without the need for a BlockItem. The placement won't account for NBT tags or block entities.
	 */
	public static InteractionResult placeSimpleBlock(Block block, BlockPlaceContext context) {
		if (!block.isEnabled(context.getLevel().enabledFeatures())) {
			return InteractionResult.FAIL;
		} else if (!context.canPlace()) {
			return InteractionResult.FAIL;
		}

		BlockState state = block.getStateForPlacement(context);
		if (state == null) {
			return InteractionResult.FAIL;
		} else if (!context.getLevel().setBlock(context.getClickedPos(), state, 11)) {
			return InteractionResult.FAIL;
		}

		BlockPos pos = context.getClickedPos();
		Level level = context.getLevel();
		Player player = context.getPlayer();
		ItemStack stack = context.getItemInHand();
		BlockState targetState = level.getBlockState(pos);
		if (targetState.is(state.getBlock())) {
			if (player instanceof ServerPlayer) {
				CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer) player, pos, stack);
			}
		}

		SoundType soundtype = targetState.getSoundType(level, pos, context.getPlayer());
		level.playSound(player, pos, targetState.getSoundType(level, pos, context.getPlayer()).getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
		level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(player, targetState));
		if (player == null || !player.getAbilities().instabuild) {
			stack.shrink(1);
		}

		return InteractionResult.sidedSuccess(level.isClientSide);
	}

	public static void dropItems(Level level, BlockPos pos, IItemHandler inventory) {
		for (int slot = 0; slot < inventory.getSlots(); slot++)
			Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), inventory.getStackInSlot(slot));
	}

	public static boolean isInventoryEmpty(IItemHandler inventory) {
		for (int i = 0; i < inventory.getSlots(); i++) {
			if (!inventory.getStackInSlot(i).isEmpty()) {
				return false;
			}
		}
		return true;
	}

	public static void spawnItemEntity(Level level, ItemStack stack, double x, double y, double z, double xMotion, double yMotion, double zMotion) {
		ItemEntity entity = new ItemEntity(level, x, y, z, stack);
		entity.setDeltaMovement(xMotion, yMotion, zMotion);
		level.addFreshEntity(entity);
	}
}
