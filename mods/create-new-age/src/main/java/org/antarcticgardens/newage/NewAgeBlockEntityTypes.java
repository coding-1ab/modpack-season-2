package org.antarcticgardens.newage;

import com.simibubi.create.content.kinetics.base.CutoutRotatingInstance;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.base.ShaftInstance;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import org.antarcticgardens.newage.content.energiser.EnergiserBlockEntity;
import org.antarcticgardens.newage.content.energiser.EnergiserRenderer;
import org.antarcticgardens.newage.content.generation.carbonbrushes.CarbonBrushesBlockEntity;
import org.antarcticgardens.newage.content.generation.carbonbrushes.CarbonBrushesRenderer;
import org.antarcticgardens.newage.content.generation.generatorcoil.GeneratorCoilBlockEntity;
import org.antarcticgardens.newage.content.heat.heater.HeaterBlockEntity;
import org.antarcticgardens.newage.content.heat.heatpipe.HeatPipeBlockEntity;
import org.antarcticgardens.newage.content.heat.heatpump.HeatPumpBlockEntity;
import org.antarcticgardens.newage.content.heat.solarheatingplate.SolarHeatingPlateBlockEntity;
import org.antarcticgardens.newage.content.heat.stirlingengine.StirlingEngineBlockEntity;
import org.antarcticgardens.newage.content.heat.stirlingengine.StirlingEngineInstance;
import org.antarcticgardens.newage.content.heat.stirlingengine.StirlingEngineRenderer;
import org.antarcticgardens.newage.content.reactor.reactorcore.ReactorCoreBlockEntity;

import static org.antarcticgardens.newage.CreateNewAge.REGISTRATE;

public class NewAgeBlockEntityTypes {
    public static final BlockEntityEntry<EnergiserBlockEntity> ENERGISER_T1 = REGISTRATE
            .blockEntity("energiser_t1", EnergiserBlockEntity::newTier1)
            .instance(() -> ShaftInstance::new)
            .validBlocks(NewAgeBlocks.ENERGISER_T1)
            .renderer(() -> EnergiserRenderer::new)
            .register();

    public static final BlockEntityEntry<CarbonBrushesBlockEntity> CARBON_BRUSHES = REGISTRATE
            .blockEntity("carbon_brushes", CarbonBrushesBlockEntity::new)
            .instance(() -> ShaftInstance::new)
            .validBlocks(NewAgeBlocks.CARBON_BRUSHES)
            .renderer(() -> CarbonBrushesRenderer::new)
            .register();

    public static final BlockEntityEntry<HeatPipeBlockEntity> HEAT_PIPE = REGISTRATE
            .blockEntity("heat_pipe", HeatPipeBlockEntity::new)
            .validBlocks(NewAgeBlocks.HEAT_PIPE)
            .register();

    public static final BlockEntityEntry<HeatPumpBlockEntity> HEAT_PUMP = REGISTRATE
            .blockEntity("heat_pump", HeatPumpBlockEntity::new)
            .validBlocks(NewAgeBlocks.HEAT_PUMP)
            .register();


    public static final BlockEntityEntry<SolarHeatingPlateBlockEntity> BASIC_SOLAR_HEATING_PLATE = REGISTRATE
            .blockEntity("basic_solar_heating_plate", SolarHeatingPlateBlockEntity::createBasic)
            .validBlocks(NewAgeBlocks.BASIC_SOLAR_HEATING_PLATE)
            .register();

    public static final BlockEntityEntry<SolarHeatingPlateBlockEntity> ADVANCED_SOLAR_HEATING_PLATE = REGISTRATE
            .blockEntity("advanced_solar_heating_plate", SolarHeatingPlateBlockEntity::createAdvanced)
            .validBlocks(NewAgeBlocks.ADVANCED_SOLAR_HEATING_PLATE)
            .register();


    public static final BlockEntityEntry<HeaterBlockEntity> HEATER = REGISTRATE
            .blockEntity("heater", HeaterBlockEntity::new)
            .validBlocks(NewAgeBlocks.HEATER)
            .register();

    public static final BlockEntityEntry<ReactorCoreBlockEntity> REACTOR_CORE = REGISTRATE
            .blockEntity("reactor_core", ReactorCoreBlockEntity::new)
            .validBlocks(NewAgeBlocks.REACTOR_CORE)
            .register();

    public static final BlockEntityEntry<GeneratorCoilBlockEntity> GENERATOR_COIL = REGISTRATE
            .blockEntity("generator_coil", GeneratorCoilBlockEntity::new)
            .instance(() -> CutoutRotatingInstance::new)
            .validBlocks(NewAgeBlocks.GENERATOR_COIL)
            .renderer(() -> KineticBlockEntityRenderer::new)
            .register();

    public static final BlockEntityEntry<StirlingEngineBlockEntity> STIRLING_ENGINE = REGISTRATE
            .blockEntity("stirling_engine", StirlingEngineBlockEntity::new)
            .instance(() -> StirlingEngineInstance::new)
            .validBlocks(NewAgeBlocks.STIRLING_ENGINE)
            .renderer(() -> StirlingEngineRenderer::new)
            .register();

    public static void load() {  }
}
