package org.example.com.tmvkrpxl0.modpack

import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.BiomeTags
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.*
import net.minecraft.world.level.block.BaseFireBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.FireBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape
import kotlin.math.min

class EnderFire(blockProperties: Properties) : BaseFireBlock(blockProperties, 2.0F) {
    companion object {
        val CODEC: MapCodec<EnderFire> = simpleCodec(::EnderFire)
        val AGE: IntegerProperty = BlockStateProperties.AGE_15
        val NORTH: BooleanProperty = BlockStateProperties.NORTH
        val EAST: BooleanProperty = BlockStateProperties.EAST
        val SOUTH: BooleanProperty = BlockStateProperties.SOUTH
        val WEST: BooleanProperty = BlockStateProperties.WEST
        val UP: BooleanProperty = BlockStateProperties.UP

        private val UP_AABB: VoxelShape = box(0.0, 15.0, 0.0, 16.0, 16.0, 16.0)
        private val WEST_AABB: VoxelShape = box(0.0, 0.0, 0.0, 1.0, 16.0, 16.0)
        private val EAST_AABB: VoxelShape = box(15.0, 0.0, 0.0, 16.0, 16.0, 16.0)
        private val NORTH_AABB: VoxelShape = box(0.0, 0.0, 0.0, 16.0, 16.0, 1.0)
        private val SOUTH_AABB: VoxelShape = box(0.0, 0.0, 15.0, 16.0, 16.0, 16.0)
    }

    private val shapesCache: Map<BlockState, VoxelShape> =
        stateDefinition.getPossibleStates().filter { state -> state.getValue(AGE) == 0 }
            .associateBy({ it }, {
                val entries = arrayOf(
                    UP to UP_AABB,
                    WEST to WEST_AABB,
                    EAST to EAST_AABB,
                    NORTH to NORTH_AABB,
                    SOUTH to SOUTH_AABB,
                )

                var voxelShape = Shapes.empty()
                for ((direction, shape) in entries) {
                    if (it.getValue(direction)) {
                        voxelShape = Shapes.or(voxelShape, shape)
                    }
                }

                if (voxelShape.isEmpty) {
                    voxelShape = DOWN_AABB
                }

                voxelShape
            })

    init {
        registerDefaultState(
            stateDefinition
                .any()
                .setValue(AGE, 0)
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(UP, false)
        )
    }


    override fun codec() = CODEC

    override fun canBurn(state: BlockState) = true

    override fun tick(state: BlockState, level: ServerLevel, pos: BlockPos, random: RandomSource) {
        level.scheduleTick(pos, this, getFireTickDelay(level.random))
        if (!level.gameRules.getBoolean(GameRules.RULE_DOFIRETICK)) {
            return
        }

        val currentAge = state.getValue(AGE)
        var shouldExtinguish = !state.canSurvive(level, pos)
        shouldExtinguish = shouldExtinguish || level.isRaining && isRainingNearby(level, pos)
        shouldExtinguish = shouldExtinguish || (currentAge == 15 && random.nextInt(4) == 0)

        if (shouldExtinguish) {
            level.removeBlock(pos, false)
            return
        }

        if (!level.getBiome(pos).`is`(BiomeTags.IS_END)) {
            val nextAge = min(15, currentAge + random.nextInt(3) / 2)
            if (nextAge != currentAge) {
                val newState = state.setValue(AGE, nextAge)
                level.setBlock(pos, newState, 4)
            }
        }

    }

    override fun animateTick(state: BlockState, level: Level, pos: BlockPos, random: RandomSource) {
        super.animateTick(state, level, pos, random)
    }

    override fun entityInside(state: BlockState, level: Level, pos: BlockPos, entity: Entity) {
        if (entity is LivingEntity && level is ServerLevel) {
            if (level.random.nextInt(100) == 0) {
                randomTeleport(entity, level)
            }
        }
        super.entityInside(state, level, pos, entity)
    }

    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape {
        return shapesCache[state.setValue(AGE, 0)]!!
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FireBlock.AGE, FireBlock.NORTH, FireBlock.EAST, FireBlock.SOUTH, FireBlock.WEST, FireBlock.UP)
    }
    
    override fun onPlace(state: BlockState, level: Level, pos: BlockPos, oldState: BlockState, isMoving: Boolean) {
        super.onPlace(state, level, pos, oldState, isMoving)
        level.scheduleTick(pos, this, getFireTickDelay(level.random))
    }
    
    fun getFireTickDelay(random: RandomSource): Int {
        return 30 + random.nextInt(10)
    }

    override fun updateShape(
        currentState: BlockState,
        direction: Direction,
        neighborState: BlockState,
        level: LevelAccessor,
        currentPos: BlockPos,
        neighborPos: BlockPos
    ): BlockState {
        return if (canSurvive(currentState, level, currentPos)) {
            currentState
        } else {
            Blocks.AIR.defaultBlockState()
        }
    }

    override fun canSurvive(state: BlockState, level: LevelReader, pos: BlockPos): Boolean {
        val below = pos.below()
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)
    }

    fun isRainingNearby(level: Level, pos: BlockPos): Boolean {
        return level.isRainingAt(pos)
            || level.isRainingAt(pos.west())
            || level.isRainingAt(pos.east())
            || level.isRainingAt(pos.north())
            || level.isRainingAt(pos.south())
    }
}
