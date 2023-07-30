package org.antarcticgardens.newage;

import com.simibubi.create.content.kinetics.BlockStressDefaults;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.antarcticgardens.newage.content.energiser.EnergiserBlock;
import org.antarcticgardens.newage.content.generation.carbonbrushes.CarbonBrushesBlock;
import org.antarcticgardens.newage.content.generation.connector.ElectricalConnectorBlock;
import org.antarcticgardens.newage.content.generation.generatorcoil.GeneratorCoilBlock;
import org.antarcticgardens.newage.content.generation.magnets.MagnetiteBlock;
import org.antarcticgardens.newage.content.heat.heater.HeaterBlock;
import org.antarcticgardens.newage.content.heat.heatpipe.HeatPipeBlock;
import org.antarcticgardens.newage.content.heat.heatpump.HeatPumpBlock;
import org.antarcticgardens.newage.content.heat.solarheatingplate.SolarHeatingPlateBlock;
import org.antarcticgardens.newage.content.heat.stirlingengine.StirlingEngineBlock;
import org.antarcticgardens.newage.content.reactor.CoriumBlock;
import org.antarcticgardens.newage.content.reactor.ReactorBlock;
import org.antarcticgardens.newage.content.reactor.ReactorTransparentBlock;
import org.antarcticgardens.newage.content.reactor.SolidCoriumBlock;
import org.antarcticgardens.newage.content.reactor.reactorfuelacceptor.ReactorFuelAcceptorBlock;
import org.antarcticgardens.newage.content.reactor.reactorheatvent.ReactorHeatVentBlock;
import org.antarcticgardens.newage.content.reactor.reactorrod.ReactorRodBlock;

import static org.antarcticgardens.newage.CreateNewAge.REGISTRATE;

public class NewAgeBlocks {
    static {
        REGISTRATE.defaultCreativeTab(CreateNewAge.CREATIVE_TAB_KEY);
    }

    public static final BlockEntry<EnergiserBlock> ENERGISER_T1 =
            REGISTRATE.block("energiser_t1", EnergiserBlock::new)
                    .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .transform(BlockStressDefaults.setImpact(2.0))
                    .simpleItem()
                    .register();

    public static final BlockEntry<CarbonBrushesBlock> CARBON_BRUSHES =
            REGISTRATE.block("carbon_brushes", CarbonBrushesBlock::new)
                    .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .addLayer(() -> RenderType::cutout)
                    .simpleItem()
                    .register();

    public static final BlockEntry<HeatPipeBlock> HEAT_PIPE =
            REGISTRATE.block("heat_pipe", HeatPipeBlock::new)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .simpleItem()
                    .register();

    public static final BlockEntry<HeatPumpBlock> HEAT_PUMP =
            REGISTRATE.block("heat_pump", HeatPumpBlock::new)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .simpleItem()
                    .register();

    public static final BlockEntry<HeaterBlock> HEATER =
            REGISTRATE.block("heater", HeaterBlock::new)
                    .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .simpleItem()
                    .register();

    public static final BlockEntry<GeneratorCoilBlock> GENERATOR_COIL =
            REGISTRATE.block("generator_coil", GeneratorCoilBlock::new)
                    .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .transform(BlockStressDefaults.setImpact(12.0f))
                    .simpleItem()
                    .register();


    public static final BlockEntry<StirlingEngineBlock> STIRLING_ENGINE =
            REGISTRATE.block("stirling_engine", properties -> new StirlingEngineBlock(properties, NewAgeBlockEntityTypes.STIRLING_ENGINE))
                    .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .transform(BlockStressDefaults.setCapacity(32.0))
                    .simpleItem()
                    .register();

    public static final BlockEntry<MagnetiteBlock> MAGNETITE_BLOCK =
            REGISTRATE.block("magnetite_block", MagnetiteBlock::new)
                    .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .simpleItem()
                    .register();

    public static final BlockEntry<SolidCoriumBlock> SOLID_CORIUM =
            REGISTRATE.block("solid_corium", SolidCoriumBlock::new)
                    .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .simpleItem()
                    .register();

    public static final BlockEntry<CoriumBlock> CORIUM =
            REGISTRATE.block("corium", CoriumBlock::new)
                    .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .simpleItem()
                    .register();

    public static final BlockEntry<ReactorBlock> REACTOR_CASING =
            REGISTRATE.block("reactor_casing", ReactorBlock::new)
                    .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .simpleItem()
                    .register();

    public static final BlockEntry<ReactorRodBlock> REACTOR_ROD =
            REGISTRATE.block("reactor_rod", ReactorRodBlock::new)
                    .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .addLayer(() -> RenderType::cutout)
                    .simpleItem()
                    .register();

    public static final BlockEntry<ReactorTransparentBlock> REACTOR_GLASS =
            REGISTRATE.block("reactor_glass", ReactorTransparentBlock::new)
                    .properties(p -> p.isViewBlocking((blockState, blockGetter, blockPos) -> false))
                    .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .addLayer(() -> RenderType::cutout)
                    .simpleItem()
                    .register();

    public static final BlockEntry<ReactorFuelAcceptorBlock> REACTOR_FUEL_ACCEPTOR =
            REGISTRATE.block("reactor_fuel_acceptor", ReactorFuelAcceptorBlock::new)
                    .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .simpleItem()
                    .register();


    public static final BlockEntry<ReactorHeatVentBlock> REACTOR_HEAT_VENT =
            REGISTRATE.block("reactor_heat_vent", ReactorHeatVentBlock::new)
                    .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .simpleItem()
                    .register();


    public static final BlockEntry<Block> BASIC_SOLAR_HEATING_PLATE =
            REGISTRATE.block("basic_solar_heating_plate", SolarHeatingPlateBlock::createBasic)
                    .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .simpleItem()
                    .register();

    public static final BlockEntry<Block> ADVANCED_SOLAR_HEATING_PLATE =
            REGISTRATE.block("advanced_solar_heating_plate", SolarHeatingPlateBlock::createAdvanced)
                    .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .simpleItem()
                    .register();

    public static final BlockEntry<ElectricalConnectorBlock> ELECTRICAL_CONNECTOR =
            REGISTRATE.block("electrical_connector", ElectricalConnectorBlock::new)
                    .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .simpleItem()
                    .register();

    public static void load() {  }
}
