package org.antarcticgardens.newage.content.generatorcoil;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class GeneratorCoilBlockEntity extends KineticBlockEntity {
    public GeneratorCoilBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return new AABB(worldPosition).inflate(1);
    }

    @Override
    public float calculateStressApplied() {
        float stress = super.calculateStressApplied();

        // TODO: Increase stress when magnets are nearby

        return stress;
    }
}
