package org.antarcticgardens.newage.content.generation.magnets;

import com.simibubi.create.foundation.placement.IPlacementHelper;
import com.simibubi.create.foundation.placement.PlacementOffset;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.antarcticgardens.newage.NewAgeBlocks;
import org.antarcticgardens.newage.NewAgeTags;
import org.antarcticgardens.newage.content.generation.generatorcoil.GeneratorCoilBlockEntity;

import java.util.function.Predicate;

@SuppressWarnings("NullableProblems")
public class MagnetPlacementHelper implements IPlacementHelper {
    @Override
    public Predicate<ItemStack> getItemPredicate() {
        return is -> is.is(NewAgeTags.MAGNET_TAG);
    }

    @Override
    public Predicate<BlockState> getStatePredicate() {
        return bs -> bs.is(NewAgeBlocks.GENERATOR_COIL.get());
    }

    @Override
    public PlacementOffset getOffset(Player player, Level world, BlockState state, BlockPos pos, BlockHitResult ray) {
        if (world.getBlockEntity(ray.getBlockPos()) instanceof GeneratorCoilBlockEntity coil) {
            for (BlockPos magnetPos : coil.magnetPositions) {
                if (world.getBlockState(magnetPos).is(Blocks.AIR)) 
                    return PlacementOffset.success(magnetPos);
            }
        }
        
        return PlacementOffset.fail();
    }
}
