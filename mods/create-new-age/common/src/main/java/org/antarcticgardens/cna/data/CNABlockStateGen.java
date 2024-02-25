package org.antarcticgardens.cna.data;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.antarcticgardens.cna.content.energising.EnergiserBlock;
import org.antarcticgardens.cna.content.heat.heater.HeaterBlock;
import org.antarcticgardens.cna.content.heat.pipe.HeatPipeBlock;
import org.antarcticgardens.cna.content.heat.pump.HeatPumpBlock;
import org.antarcticgardens.cna.content.nuclear.reactor.rod.ReactorRodBlock;
import org.joml.Vector3f;

#if CNA_FABRIC
import io.github.fabricators_of_create.porting_lib.models.generators.*;
import io.github.fabricators_of_create.porting_lib.models.generators.block.*;
#else
import net.minecraftforge.client.model.generators.*;
#endif

public class CNABlockStateGen {
    public static <P extends EnergiserBlock> NonNullBiConsumer<DataGenContext<Block, P>, RegistrateBlockstateProvider> energiser() {
        return (c, p) -> p.horizontalBlock(c.get(), p.models().withExistingParent(c.getName(), p.modLoc("block/energiser"))
                        .texture("all", "block/" + c.getName()));
    }
    
    public static <P extends HeatPipeBlock> NonNullBiConsumer<DataGenContext<Block, P>, RegistrateBlockstateProvider> heatPipe() {
        return (c, p) -> {
            MultiPartBlockStateBuilder builder = p.getMultipartBuilder(c.get());
            ModelFile.ExistingModelFile center = p.models().getExistingFile(p.modLoc("block/" + c.getName() + "/center"));
            ModelFile.ExistingModelFile side = p.models().getExistingFile(p.modLoc("block/" + c.getName() + "/side"));
            
            builder.part()
                    .modelFile(center)
                    .addModel()
                    .end();
            
            for (Direction dir : Direction.values()) {
                Vector3f euler = new Vector3f();
                dir.getRotation().getEulerAnglesXYZ(euler);
                
                if (dir.getAxis().isHorizontal()) {
                    dir = dir.getOpposite();
                }
                
                builder.part()
                        .modelFile(side)
                        .rotationX((int) Math.round(Math.toDegrees(euler.x)))
                        .rotationY((int) Math.round(Math.toDegrees(euler.z)))
                        .addModel()
                        .condition(PipeBlock.PROPERTY_BY_DIRECTION.get(dir), true)
                        .end();
            }
        };
    }

    public static <P extends HeatPumpBlock> NonNullBiConsumer<DataGenContext<Block, P>, RegistrateBlockstateProvider> heatPump() {
        return (c, p) -> {
            MultiPartBlockStateBuilder builder = p.getMultipartBuilder(c.get());
            ModelFile.ExistingModelFile center = p.models().getExistingFile(p.modLoc("block/" + c.getName() + "/center"));
            ModelFile.ExistingModelFile side = p.models().getExistingFile(p.modLoc("block/" + c.getName() + "/side"));
            
            for (Direction dir : Direction.values()) {
                Vector3f euler = new Vector3f();
                dir.getRotation().getEulerAnglesXYZ(euler);

                if (dir.getAxis().isHorizontal()) {
                    dir = dir.getOpposite();
                }
                
                int rotX = (int) Math.round(Math.toDegrees(euler.x));
                int rotY = (int) Math.round(Math.toDegrees(euler.z));

                builder.part()
                        .modelFile(center)
                        .rotationX(rotX)
                        .rotationY(rotY)
                        .addModel()
                        .condition(BlockStateProperties.FACING, dir)
                        .end();

                builder.part()
                        .modelFile(side)
                        .rotationX(rotX)
                        .rotationY(rotY)
                        .addModel()
                        .condition(PipeBlock.PROPERTY_BY_DIRECTION.get(dir), true)
                        .end();
            }
        };
    }
    
    public static <P extends HeaterBlock> NonNullBiConsumer<DataGenContext<Block, P>, RegistrateBlockstateProvider> heater() {
        return (c, p) -> {
            VariantBlockStateBuilder builder = p.getVariantBuilder(c.get());
            
            for (BlazeBurnerBlock.HeatLevel level : BlazeBurnerBlock.HeatLevel.values()) {
                String id = "block/" + c.getName() + "_top_" + level.ordinal();
                
                ModelBuilder<?> model = p.models()
                        .withExistingParent(id, p.modLoc("block/" + c.getName()))
                        .texture("top", id);
                
                builder.addModels(builder.partialState().with(BlazeBurnerBlock.HEAT_LEVEL, level), new ConfiguredModel(model));
            }
        };
    }

    public static <P extends ReactorRodBlock> NonNullBiConsumer<DataGenContext<Block, P>, RegistrateBlockstateProvider> reactorRod() {
        return (c, p) -> {
            VariantBlockStateBuilder builder = p.getVariantBuilder(c.get());
            
            ModelBuilder<?> off = p.models().withExistingParent(c.getName() + "_off", p.modLoc("block/" + c.getName()))
                    .texture("all", p.modLoc("block/" + c.getName() + "_off"));
            ModelBuilder<?> on = p.models().withExistingParent(c.getName() + "_on", p.modLoc("block/" + c.getName()))
                    .texture("all", p.modLoc("block/" + c.getName() + "_on"));
            
            builder.forAllStates(state -> {
                Direction.Axis axis = state.getValue(BlockStateProperties.AXIS);
                boolean lit = state.getValue(BlockStateProperties.LIT);
                
                return ConfiguredModel.builder()
                        .modelFile(lit ? on : off)
                        .rotationX(axis == Direction.Axis.Y ? 0 : 90)
                        .rotationY(axis == Direction.Axis.X ? 90 : axis == Direction.Axis.Z ? 180 : 0)
                        .build();
            });
        };
    }
}
