package com.tmvkrpxl0.modpack.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import com.tmvkrpxl0.modpack.Blocks;
import com.tmvkrpxl0.modpack.Fluids;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FireBlock.class)
public class FireBlockMixin {
    @SuppressWarnings({"unchecked", "rawtypes"})
    @WrapMethod(method = "getStateForPlacement(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;")
    private BlockState placeEnderFire(BlockGetter level, BlockPos pos, Operation<BlockState> original) {
        BlockState originalState = original.call(level, pos);
        FluidState fluidState = level.getFluidState(pos);

        if (!fluidState.isSource()) {
            return originalState;
        }

        if (!fluidState.getFluidType().equals(Fluids.INSTANCE.getENDER_FUEL_TYPE())) {
            return originalState;
        }

        BlockState enderFire = Blocks.INSTANCE.getENDER_FIRE().defaultBlockState();
        for(Property property: enderFire.getProperties()) {
            if (originalState.hasProperty(property)) {
                enderFire = enderFire.setValue(property, originalState.getValue(property));
            }
        }

        return enderFire;
    }
}
