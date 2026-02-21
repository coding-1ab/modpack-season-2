package vectorwing.farmersdelight.common;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockShapes {

    public static final VoxelShape[] ROAST_CHICKEN_SHAPES = new VoxelShape[] {
            Block.box(4, 2, 10, 12, 9, 12),
            Block.box(4, 2, 8, 12, 9, 12),
            Block.box(4, 2, 6, 12, 9, 12),
            Block.box(4, 2, 4, 12, 9, 12)
    };
    public static final VoxelShape[] HONEY_GLAZED_HAM_SHAPES = new VoxelShape[] {
            Block.box(4, 2, 4, 12, 4, 12),
            Block.box(4, 2, 8, 12, 10, 12),
            Block.box(4, 2, 6, 12, 10, 12),
            Block.box(4, 2, 4, 12, 10, 12)
    };
    public static final VoxelShape[] SHEPHERDS_PIE_BLOCK = new VoxelShape[] {
            Block.box(2, 2, 8, 8, 8, 14),
            Block.box(2, 2, 8, 14, 8, 14),
            Shapes.join(Block.box(8, 2, 2, 14, 8, 8), Block.box(2, 2, 8, 14, 8, 14), BooleanOp.OR),
            Block.box(2, 2, 2, 14, 8, 14)
    };
}
