package vectorwing.farmersdelight.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import vectorwing.farmersdelight.common.utility.ShapeUtils;

import java.util.function.Supplier;

public class PlatedFeastBlock extends FeastBlock {

    private static final VoxelShape DEFAULT_PLATE_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 2.0D, 15.0D);

    private final VoxelShape[][] combinedShapes;

    public PlatedFeastBlock(Properties properties, Supplier<Item> servingItem, boolean hasLeftovers, VoxelShape[] dishShapes, VoxelShape plateShape) {
        super(properties, servingItem, hasLeftovers);
        this.combinedShapes = ShapeUtils.buildPlatedFoodShapes(dishShapes, plateShape);
    }

    public PlatedFeastBlock(Properties properties, Supplier<Item> servingItem, boolean hasLeftovers, VoxelShape[] dishShapes) {
        this(properties, servingItem, hasLeftovers, dishShapes, DEFAULT_PLATE_SHAPE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return combinedShapes[state.getValue(SERVINGS)][state.getValue(FACING).get2DDataValue()];
    }
}
