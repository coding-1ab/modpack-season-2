package com.mrh0.createaddition.blocks.connector;

import com.mrh0.createaddition.CreateAddition;
import com.mrh0.createaddition.blocks.connector.base.AbstractConnectorBlock;
import com.mrh0.createaddition.blocks.connector.base.ConnectorMode;
import com.mrh0.createaddition.blocks.connector.base.ConnectorVariant;
import com.mrh0.createaddition.index.CABlockEntities;
import com.mrh0.createaddition.shapes.CAShapes;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import net.createmod.catnip.math.VoxelShaper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.model.generators.BlockModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.MultiPartBlockStateBuilder;

public class SmallConnectorBlock extends AbstractConnectorBlock<SmallConnectorBlockEntity> {
    public static final VoxelShaper CONNECTOR_SHAPE = CAShapes.shape(6, 0, 6, 10, 5, 10).forDirectional();
    public SmallConnectorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Class<SmallConnectorBlockEntity> getBlockEntityClass() {
        return SmallConnectorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends SmallConnectorBlockEntity> getBlockEntityType() {
        return CABlockEntities.SMALL_CONNECTOR.get();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return CABlockEntities.SMALL_CONNECTOR.create(pos, state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return CONNECTOR_SHAPE.get(state.getValue(FACING).getOpposite());
    }

    public static void makeBlockState(DataGenContext<Block, SmallConnectorBlock> ctx, RegistrateBlockstateProvider provider) {
        BlockModelProvider models = provider.models();
        String basePath = "block/connector/";
        String basePathSmall = basePath + "small/";
        MultiPartBlockStateBuilder builder = provider.getMultipartBuilder(ctx.get());

        ModelFile.ExistingModelFile noneModel = models.getExistingFile(ResourceLocation.fromNamespaceAndPath(CreateAddition.MODID, basePathSmall + "none"));
        ModelFile.ExistingModelFile pushModel = models.getExistingFile(ResourceLocation.fromNamespaceAndPath(CreateAddition.MODID, basePathSmall + "push"));
        ModelFile.ExistingModelFile pullModel = models.getExistingFile(ResourceLocation.fromNamespaceAndPath(CreateAddition.MODID, basePathSmall + "pull"));
        ModelFile.ExistingModelFile girderBaseModel = models.getExistingFile(ResourceLocation.fromNamespaceAndPath(CreateAddition.MODID, basePath + "girder_base"));

        for(Direction direction: Direction.values()) {
            int verticalAngle = direction == Direction.UP ? 90 : direction == Direction.DOWN ? -90 : 0;

            for (ConnectorMode connectorMode: ConnectorMode.values()) {
                builder.part()
                        .modelFile(noneModel)
                        .rotationX(verticalAngle)
                        .rotationY((int) (direction.toYRot() + (direction.getAxis()
                                .isVertical() ? 90 : 0)) % 360)
                        .addModel()
                        .condition(SmallConnectorBlock.FACING, direction)
                        .condition(SmallConnectorBlock.MODE, connectorMode)
                        .end()
                        .part()
                        .modelFile(pushModel)
                        .rotationX(verticalAngle)
                        .rotationY((int) (direction.toYRot() + (direction.getAxis()
                                .isVertical() ? 90 : 0)) % 360)
                        .addModel()
                        .condition(SmallConnectorBlock.FACING, direction)
                        .condition(SmallConnectorBlock.MODE, connectorMode)
                        .end()
                        .part()
                        .modelFile(pullModel)
                        .rotationX(verticalAngle)
                        .rotationY((int) (direction.toYRot() + (direction.getAxis()
                                .isVertical() ? 90 : 0)) % 360)
                        .addModel()
                        .condition(SmallConnectorBlock.FACING, direction)
                        .condition(SmallConnectorBlock.MODE, connectorMode)
                        .end();
            }

            builder.part()
                    .modelFile(girderBaseModel)
                    .rotationX(verticalAngle)
                    .rotationY((int) (direction.toYRot() + (direction.getAxis()
                            .isVertical() ? 90 : 0)) % 360)
                    .addModel()
                    .condition(SmallConnectorBlock.FACING, direction)
                    .condition(SmallConnectorBlock.VARIANT, ConnectorVariant.Girder)
                    .end();

        }


    }
}
