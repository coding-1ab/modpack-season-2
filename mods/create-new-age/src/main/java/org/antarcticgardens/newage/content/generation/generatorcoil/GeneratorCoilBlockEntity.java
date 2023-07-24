package org.antarcticgardens.newage.content.generation.generatorcoil;

import com.google.common.collect.Lists;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.antarcticgardens.newage.content.generation.magnets.IMagneticBlock;
import org.antarcticgardens.newage.tools.RelativeBlockPos;

import java.util.List;

public class GeneratorCoilBlockEntity extends KineticBlockEntity {
    private final List<BlockPos> magnetPositions;

    private float lastStress = 0.0f;
    private int generatedEnergy = 0;

    public GeneratorCoilBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);

        RelativeBlockPos relativePosition = new RelativeBlockPos(worldPosition,
                state.getValue(RotatedPillarKineticBlock.AXIS));

        magnetPositions = Lists.newArrayList(
                relativePosition.up(2).getPos(),
                relativePosition.up(2).right(1).getPos(),
                relativePosition.up(2).left(1).getPos(),

                relativePosition.down(2).getPos(),
                relativePosition.down(2).right(1).getPos(),
                relativePosition.down(2).left(1).getPos(),

                relativePosition.right(2).getPos(),
                relativePosition.right(2).up(1).getPos(),
                relativePosition.right(2).down(1).getPos(),

                relativePosition.left(2).getPos(),
                relativePosition.left(2).up(1).getPos(),
                relativePosition.left(2).down(1).getPos()
        );

        setLazyTickRate(20);
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return new AABB(worldPosition).inflate(1);
    }

    @Override
    public float calculateStressApplied() {
        float stress = super.calculateStressApplied();

        for (BlockPos pos : magnetPositions) {
            if (level.getBlockState(pos).getBlock() instanceof IMagneticBlock magneticBlock)
                stress += magneticBlock.getStrength();
        }

        return stress;
    }

    @Override
    public void lazyTick() {
        float stress = calculateStressApplied();
        generatedEnergy = (int) ((stress - super.calculateStressApplied()) * Math.abs(this.getTheoreticalSpeed()));

        if (getOrCreateNetwork() != null && lastStress != stress) {
            getOrCreateNetwork().remove(this);
            getOrCreateNetwork().addSilently(this, lastCapacityProvided, lastStress);

            lastStress = stress;
        }
    }

    public int takeGeneratedEnergy() {
        int energy = generatedEnergy;
        generatedEnergy = 0;
        return energy;
    }
}
