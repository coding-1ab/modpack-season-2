package org.example.com.tmvkrpxl0.modpack

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.util.RandomSource
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.level.material.FluidState
import net.neoforged.neoforge.fluids.BaseFlowingFluid

abstract class EnderFuel(properties: Properties) : BaseFlowingFluid(properties) {
    override fun getBucket() = Items.ENDER_FUEL_BUCKET

    override fun canBeReplacedWith(
        state: FluidState,
        level: BlockGetter,
        pos: BlockPos,
        fluid: Fluid,
        direction: Direction
    ) = false

    public override fun animateTick(level: Level, pos: BlockPos, state: FluidState, random: RandomSource) {
        val above = pos.above()
        if (level.getBlockState(above).isAir && !level.getBlockState(above).isSolidRender(level, above)) {
            if (random.nextInt(100) == 0) {
                val x = pos.x.toDouble() + random.nextDouble()
                val y = pos.y.toDouble() + 1.0
                val z = pos.z.toDouble() + random.nextDouble()
                level.addParticle(ParticleTypes.PORTAL, x, y, z, 0.0, 0.0, 0.0)
            }
        }
    }

    public override fun getDripParticle(): ParticleOptions = ParticleTypes.PORTAL

    override fun beforeDestroyingBlock(level: LevelAccessor, pos: BlockPos, state: BlockState) {
        val blockEntity = if (state.hasBlockEntity()) level.getBlockEntity(pos) else null
        Block.dropResources(state, level, pos, blockEntity)
    }

    class Source(properties: Properties): EnderFuel(properties) {
        override fun getAmount(state: FluidState): Int = 8

        override fun isSource(state: FluidState): Boolean = true
    }

    class Flowing(properties: Properties): EnderFuel(properties) {
        init {
            registerDefaultState(getStateDefinition().any().setValue(LEVEL, 7))
        }

        override fun createFluidStateDefinition(builder: StateDefinition.Builder<Fluid, FluidState>) {
            super.createFluidStateDefinition(builder)
            builder.add(LEVEL)
        }

        override fun getAmount(state: FluidState): Int = state.getValue(LEVEL)

        override fun isSource(state: FluidState): Boolean = false
    }
}
