package org.antarcticgardens.newage.content.generation.magnets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.antarcticgardens.newage.content.generation.generatorcoil.GeneratorCoilBlockEntity;

public class MagnetBlockItem extends BlockItem {
    public MagnetBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        BlockPos placedOnPos = context.getClickedPos()
                .relative(context.getClickedFace()
                        .getOpposite());

        if (context.getLevel().getBlockEntity(placedOnPos) instanceof GeneratorCoilBlockEntity coil) {
            BlockPos pos = null;
            for (BlockPos bp: coil.magnetPositions) {
                if (context.getLevel().getBlockState(bp).canBeReplaced()) {
                    pos = bp;
                    break;
                }
            }

            if (pos == null) {
                return InteractionResult.FAIL;
            }
            context = new BlockPlaceContext(context.getLevel(), context.getPlayer(), context.getHand(), context.getItemInHand(), new BlockHitResult(new Vec3((double)pos.getX() + 0.5D + (double) Direction.UP.getStepX() * 0.5D, (double)pos.getY() + 0.5D + (double) Direction.UP.getStepY() * 0.5D, (double)pos.getZ() + 0.5D + (double) Direction.UP.getStepZ() * 0.5D), Direction.UP, pos, false));
        }

        return super.place(context);
    }
}
