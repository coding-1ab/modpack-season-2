package org.antarcticgardens.cna.content.nuclear.reactor;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;

public class ReactorBlock extends Block implements IWrenchable {
    public ReactorBlock(Properties properties) {
        super(properties.strength(6.0f));
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity != null) {
            super.onRemove(state, level, pos, newState, movedByPiston);
        } else {
            IBE.onRemove(state, level, pos, newState);
        }
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        boolean canRotate;
        {
            Property<?> direction = this.stateDefinition.getProperty(BlockStateProperties.FACING.getName());
            Property<?> axis = this.stateDefinition.getProperty(BlockStateProperties.AXIS.getName());

            canRotate = direction != null || axis != null;
        }

        if (canRotate) {
            return IWrenchable.super.onWrenched(state, context);
        } else {
            return InteractionResult.FAIL;
        }
    }
}
