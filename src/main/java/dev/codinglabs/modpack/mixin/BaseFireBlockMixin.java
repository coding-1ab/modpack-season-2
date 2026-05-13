package dev.codinglabs.modpack.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.codinglabs.modpack.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BaseFireBlock.class)
public class BaseFireBlockMixin {
    @WrapMethod(method = "getState(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;")
    private static BlockState igniteEnderFire(BlockGetter reader, BlockPos pos, Operation<BlockState> original) {
        BlockState originalResult = original.call(reader, pos);

        if (!(reader instanceof Level level)) {
            return originalResult;
        }

        if (level.dimension() != Level.END) {
            return originalResult;
        }

        BlockPos blockpos = pos.below();
        BlockState blockstate = reader.getBlockState(blockpos);
        if (!blockstate.is(Tags.Blocks.END_STONES)) {
            return originalResult;
        }

        return Blocks.INSTANCE.getENDER_FIRE().defaultBlockState();
    }
}
