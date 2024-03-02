package org.antarcticgardens.cna;

import com.simibubi.create.content.kinetics.base.CutoutRotatingInstance;
import com.simibubi.create.content.kinetics.base.HalfShaftInstance;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.base.ShaftInstance;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import org.antarcticgardens.cna.content.electricity.battery.BatteryBlockEntity;
import org.antarcticgardens.cna.content.electricity.connector.ElectricalConnectorBlockEntity;
import org.antarcticgardens.cna.content.electricity.connector.ElectricalConnectorInstance;
import org.antarcticgardens.cna.content.electricity.connector.ElectricalConnectorRenderer;
import org.antarcticgardens.cna.content.electricity.generation.brushes.CarbonBrushesBlockEntity;
import org.antarcticgardens.cna.content.electricity.generation.brushes.CarbonBrushesRenderer;
import org.antarcticgardens.cna.content.electricity.generation.coil.GeneratorCoilBlockEntity;
import org.antarcticgardens.cna.content.energising.EnergiserBlockEntity;
import org.antarcticgardens.cna.content.energising.EnergiserRenderer;
import org.antarcticgardens.cna.content.heat.heater.HeaterBlockEntity;
import org.antarcticgardens.cna.content.heat.pipe.HeatPipeBlockEntity;
import org.antarcticgardens.cna.content.heat.plate.SolarHeatingPlateBlockEntity;
import org.antarcticgardens.cna.content.heat.pump.HeatPumpBlockEntity;
import org.antarcticgardens.cna.content.heat.stirling.StirlingEngineBlockEntity;
import org.antarcticgardens.cna.content.heat.stirling.StirlingEngineInstance;
import org.antarcticgardens.cna.content.heat.stirling.StirlingEngineRenderer;
import org.antarcticgardens.cna.content.motor.MotorBlockEntity;
import org.antarcticgardens.cna.content.motor.extension.MotorExtensionBlockEntity;
import org.antarcticgardens.cna.content.motor.extension.variants.BasicMotorExtensionVariant;
import org.antarcticgardens.cna.content.motor.variants.BasicMotorVariant;
import org.antarcticgardens.cna.content.nuclear.reactor.fuelacceptor.ReactorFuelAcceptorBlockEntity;
import org.antarcticgardens.cna.content.nuclear.reactor.rod.ReactorRodBlockEntity;
import org.antarcticgardens.cna.content.nuclear.reactor.vent.ReactorHeatVentBlockEntity;
import org.antarcticgardens.cna.rendering.HalfShaftRenderer;

import static org.antarcticgardens.cna.CreateNewAge.REGISTRATE;

public class CNABlockEntityTypes {
    public static final BlockEntityEntry<EnergiserBlockEntity> ENERGISER = REGISTRATE
            .blockEntity("energiser", EnergiserBlockEntity::new)
            .instance(() -> ShaftInstance::new)
            .validBlocks(CNABlocks.BASIC_ENERGISER, CNABlocks.ADVANCED_ENERGISER, CNABlocks.REINFORCED_ENERGISER)
            .renderer(() -> EnergiserRenderer::new)
            .register();

    public static final BlockEntityEntry<ElectricalConnectorBlockEntity> ELECTRICAL_CONNECTOR = REGISTRATE
            .blockEntity("electrical_connector", ElectricalConnectorBlockEntity::new)
            .instance(() -> ElectricalConnectorInstance::new)
            .validBlocks(CNABlocks.ELECTRICAL_CONNECTOR)
            .renderer(() -> ElectricalConnectorRenderer::new)
            .register();

    public static final BlockEntityEntry<GeneratorCoilBlockEntity> GENERATOR_COIL = REGISTRATE
            .blockEntity("generator_coil", GeneratorCoilBlockEntity::new)
            .instance(() -> CutoutRotatingInstance::new)
            .validBlocks(CNABlocks.GENERATOR_COIL)
            .renderer(() -> KineticBlockEntityRenderer::new)
            .register();

    public static final BlockEntityEntry<CarbonBrushesBlockEntity> CARBON_BRUSHES = REGISTRATE
            .blockEntity("carbon_brushes", CarbonBrushesBlockEntity::new)
            .instance(() -> ShaftInstance::new)
            .validBlocks(CNABlocks.CARBON_BRUSHES)
            .renderer(() -> CarbonBrushesRenderer::new)
            .register();

    public static final BlockEntityEntry<HeatPipeBlockEntity> HEAT_PIPE = REGISTRATE
            .blockEntity("heat_pipe", HeatPipeBlockEntity::new)
            .validBlocks(CNABlocks.HEAT_PIPE)
            .register();

    public static final BlockEntityEntry<HeatPumpBlockEntity> HEAT_PUMP = REGISTRATE
            .blockEntity("heat_pump", HeatPumpBlockEntity::new)
            .validBlocks(CNABlocks.HEAT_PUMP)
            .register();

    public static final BlockEntityEntry<HeaterBlockEntity> HEATER = REGISTRATE
            .blockEntity("heater", HeaterBlockEntity::new)
            .validBlocks(CNABlocks.HEATER)
            .register();


    public static final BlockEntityEntry<SolarHeatingPlateBlockEntity> BASIC_SOLAR_HEATING_PLATE = REGISTRATE
            .blockEntity("basic_solar_heating_plate", SolarHeatingPlateBlockEntity::createBasic)
            .validBlocks(CNABlocks.BASIC_SOLAR_HEATING_PLATE)
            .register();

    public static final BlockEntityEntry<SolarHeatingPlateBlockEntity> ADVANCED_SOLAR_HEATING_PLATE = REGISTRATE
            .blockEntity("advanced_solar_heating_plate", SolarHeatingPlateBlockEntity::createAdvanced)
            .validBlocks(CNABlocks.ADVANCED_SOLAR_HEATING_PLATE)
            .register();
    

    public static final BlockEntityEntry<ReactorRodBlockEntity> REACTOR_ROD = REGISTRATE
            .blockEntity("reactor_rod", ReactorRodBlockEntity::new)
            .validBlocks(CNABlocks.REACTOR_ROD)
            .register();

    public static final BlockEntityEntry<ReactorFuelAcceptorBlockEntity> REACTOR_FUEL_ACCEPTOR = REGISTRATE
            .blockEntity("reactor_fuel_acceptor", ReactorFuelAcceptorBlockEntity::new)
            .validBlocks(CNABlocks.REACTOR_FUEL_ACCEPTOR)
            .register();

    public static final BlockEntityEntry<ReactorHeatVentBlockEntity> REACTOR_HEAT_VENT = REGISTRATE
            .blockEntity("reactor_heat_vent", ReactorHeatVentBlockEntity::new)
            .validBlocks(CNABlocks.REACTOR_HEAT_VENT)
            .register();


    public static final BlockEntityEntry<StirlingEngineBlockEntity> STIRLING_ENGINE = REGISTRATE
            .blockEntity("stirling_engine", StirlingEngineBlockEntity::new)
            .instance(() -> StirlingEngineInstance::new)
            .validBlocks(CNABlocks.STIRLING_ENGINE)
            .renderer(() -> StirlingEngineRenderer::new)
            .register();


    public static final BlockEntityEntry<MotorBlockEntity> MOTOR = REGISTRATE 
            .blockEntity("motor", MotorBlockEntity.create(new BasicMotorVariant()))
            .instance(() -> HalfShaftInstance::new)
            .validBlocks(CNABlocks.BASIC_MOTOR, CNABlocks.ADVANCED_MOTOR, CNABlocks.REINFORCED_MOTOR)
            .renderer(() -> HalfShaftRenderer::new)
            .register();


    public static final BlockEntityEntry<MotorExtensionBlockEntity> MOTOR_EXTENSION = REGISTRATE
            .blockEntity("motor_extension", MotorExtensionBlockEntity.create(new BasicMotorExtensionVariant()))
            .validBlocks(CNABlocks.BASIC_MOTOR_EXTENSION, CNABlocks.ADVANCED_MOTOR_EXTENSION)
            .register();


    public static final BlockEntityEntry<BatteryBlockEntity> BATTERY = REGISTRATE
            .blockEntity("battery", BatteryBlockEntity::new)
            .validBlocks(CNABlocks.BATTERY)
            .register();

    public static void load() {  }
}
