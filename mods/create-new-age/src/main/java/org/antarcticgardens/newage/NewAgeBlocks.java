package org.antarcticgardens.newage;

import com.simibubi.create.content.kinetics.BlockStressDefaults;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.antarcticgardens.newage.content.generation.carbonbrushes.CarbonBrushesBlock;
import org.antarcticgardens.newage.content.energiser.EnergiserBlock;
import org.antarcticgardens.newage.content.generation.generatorcoil.GeneratorCoilBlock;
import org.antarcticgardens.newage.content.generation.magnets.MagnetiteBlock;
import org.antarcticgardens.newage.content.heat.heater.HeaterBlock;
import org.antarcticgardens.newage.content.heat.heatpipe.HeatPipeBlock;
import org.antarcticgardens.newage.content.heat.heatpump.HeatPumpBlock;
import org.antarcticgardens.newage.content.heat.solarheatingplate.SolarHeatingPlateBlock;

import static org.antarcticgardens.newage.CreateNewAge.REGISTRATE;

public class NewAgeBlocks {
    static {
        REGISTRATE.defaultCreativeTab(CreateNewAge.CREATIVE_TAB_KEY);
    }

    public static final BlockEntry<EnergiserBlock> ENERGISER_T1 =
            REGISTRATE.block("energiser_t1", EnergiserBlock::new)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .transform(BlockStressDefaults.setImpact(2.0))
                    .simpleItem()
                    .register();

    public static final BlockEntry<CarbonBrushesBlock> CARBON_BRUSHES =
            REGISTRATE.block("carbon_brushes", CarbonBrushesBlock::new)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .simpleItem()
                    .register();

    public static final BlockEntry<HeatPipeBlock> HEAT_PIPE =
            REGISTRATE.block("heat_pipe", HeatPipeBlock::new)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .simpleItem()
                    .register();

    public static final BlockEntry<HeatPumpBlock> HEAT_PUMP =
            REGISTRATE.block("heat_pump", HeatPumpBlock::new)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .simpleItem()
                    .register();

    public static final BlockEntry<HeaterBlock> HEATER =
            REGISTRATE.block("heater", HeaterBlock::new)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .simpleItem()
                    .register();

    public static final BlockEntry<GeneratorCoilBlock> GENERATOR_COIL =
            REGISTRATE.block("generator_coil", GeneratorCoilBlock::new)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .transform(BlockStressDefaults.setImpact(12.0f))
                    .simpleItem()
                    .register();

    public static final BlockEntry<MagnetiteBlock> MAGNETITE_BLOCK =
            REGISTRATE.block("magnetite_block", MagnetiteBlock::new)
                    .simpleItem()
                    .register();

    public static final BlockEntry<SolarHeatingPlateBlock> BASIC_SOLAR_HEATING_PLATE =
            REGISTRATE.block("basic_solar_heating_plate", SolarHeatingPlateBlock::new)
                    .simpleItem()
                    .register();

    public static void load() {  }
}
