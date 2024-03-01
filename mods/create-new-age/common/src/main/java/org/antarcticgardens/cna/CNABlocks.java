package org.antarcticgardens.cna;

import com.mojang.math.Axis;
import com.simibubi.create.content.processing.AssemblyOperatorBlockItem;
import com.simibubi.create.content.kinetics.BlockStressDefaults;
import com.simibubi.create.foundation.block.connected.SimpleCTBehaviour;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.ModelGen;
import com.tterrag.registrate.providers.loot.RegistrateBlockLootTables;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import org.antarcticgardens.cna.content.electricity.connector.ElectricalConnectorBlock;
import org.antarcticgardens.cna.content.electricity.generation.brushes.CarbonBrushesBlock;
import org.antarcticgardens.cna.content.electricity.generation.coil.GeneratorCoilBlock;
import org.antarcticgardens.cna.content.electricity.generation.magnet.ImplementedMagnetBlock;
import org.antarcticgardens.cna.content.energising.EnergiserBlock;
import org.antarcticgardens.cna.content.heat.heater.HeaterBlock;
import org.antarcticgardens.cna.content.heat.pipe.HeatPipeBlock;
import org.antarcticgardens.cna.content.heat.plate.SolarHeatingPlateBlock;
import org.antarcticgardens.cna.content.heat.pump.HeatPumpBlock;
import org.antarcticgardens.cna.content.heat.stirling.StirlingEngineBlock;
import org.antarcticgardens.cna.content.motor.MotorBlock;
import org.antarcticgardens.cna.content.motor.MotorBlockStateGen;
import org.antarcticgardens.cna.content.motor.extension.MotorExtensionBlock;
import org.antarcticgardens.cna.content.motor.extension.variants.AdvancedMotorExtensionVariant;
import org.antarcticgardens.cna.content.motor.extension.variants.BasicMotorExtensionVariant;
import org.antarcticgardens.cna.content.motor.variants.AdvancedMotorVariant;
import org.antarcticgardens.cna.content.motor.variants.BasicMotorVariant;
import org.antarcticgardens.cna.content.motor.variants.ReinforcedMotorVariant;
import org.antarcticgardens.cna.content.nuclear.CoriumBlock;
import org.antarcticgardens.cna.content.nuclear.SolidCoriumBlock;
import org.antarcticgardens.cna.content.nuclear.reactor.ReactorBlock;
import org.antarcticgardens.cna.content.nuclear.reactor.ReactorTransparentBlock;
import org.antarcticgardens.cna.content.nuclear.reactor.fuelacceptor.ReactorFuelAcceptorBlock;
import org.antarcticgardens.cna.content.nuclear.reactor.rod.ReactorRodBlock;
import org.antarcticgardens.cna.content.nuclear.reactor.vent.ReactorHeatVentBlock;
import org.antarcticgardens.cna.data.CNABlockStateGen;
import org.antarcticgardens.cna.rendering.ItemShaftRenderer;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static org.antarcticgardens.cna.CreateNewAge.REGISTRATE;

public class CNABlocks {
    static {
        REGISTRATE.defaultCreativeTab(CreateNewAge.CREATIVE_TAB_KEY);
    }

    public static final BlockEntry<EnergiserBlock> BASIC_ENERGISER =
            REGISTRATE.block("basic_energiser", EnergiserBlock::newBasic)
                    .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .blockstate(CNABlockStateGen.energiser())
                    .transform(BlockStressDefaults.setImpact(4.0))
                    .tag(BlockTags.MINEABLE_WITH_AXE)
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .item(AssemblyOperatorBlockItem::new)
                    .transform(ItemShaftRenderer.itemTransformer(new Vector3f(0.5f, 0.0f, 0.0f), Axis.XP.rotationDegrees(90.0f)))
                    .build()
                    .register();

    public static final BlockEntry<EnergiserBlock> ADVANCED_ENERGISER =
            REGISTRATE.block("advanced_energiser", EnergiserBlock::newAdvanced)
                    .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .blockstate(CNABlockStateGen.energiser())
                    .transform(BlockStressDefaults.setImpact(8.0))
                    .tag(BlockTags.MINEABLE_WITH_AXE)
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .item(AssemblyOperatorBlockItem::new)
                    .transform(ItemShaftRenderer.itemTransformer(new Vector3f(0.5f, 0.0f, 0.0f), Axis.XP.rotationDegrees(90.0f)))
                    .build()
                    .register();

    public static final BlockEntry<EnergiserBlock> REINFORCED_ENERGISER =
            REGISTRATE.block("reinforced_energiser", EnergiserBlock::newReinforced)
                    .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .blockstate(CNABlockStateGen.energiser())
                    .transform(BlockStressDefaults.setImpact(32.0))
                    .tag(BlockTags.MINEABLE_WITH_AXE)
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .item(AssemblyOperatorBlockItem::new)
                    .transform(ItemShaftRenderer.itemTransformer(new Vector3f(0.5f, 0.0f, 0.0f), Axis.XP.rotationDegrees(90.0f)))
                    .build()
                    .register();

    public static final BlockEntry<ElectricalConnectorBlock> ELECTRICAL_CONNECTOR =
            REGISTRATE.block("electrical_connector", ElectricalConnectorBlock::new)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .properties(properties -> properties.strength(0.4f))
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .blockstate((c, p) -> p.directionalBlock(c.get(), p.models().getExistingFile(p.modLoc(c.getName()))))
                    .simpleItem()
                    .register();

    public static final BlockEntry<GeneratorCoilBlock> GENERATOR_COIL =
            REGISTRATE.block("generator_coil", GeneratorCoilBlock::new)
                    .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .blockstate((c, p) -> BlockStateGen.axisBlock(c, p, (s) -> p.models().getExistingFile(p.modLoc("block/generator_coil/block"))))
                    .transform(BlockStressDefaults.setImpact(24.0f))
                    .item()
                    .transform(ModelGen.customItemModel())
                    .register();

    
    public static final BlockEntry<ImplementedMagnetBlock> MAGNETITE_BLOCK =
            REGISTRATE.block("magnetite_block", ImplementedMagnetBlock.simple(1))
                    .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .tag(CNATags.Block.MAGNET.blockTag)
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .simpleItem()
                    .register();

    public static final BlockEntry<ImplementedMagnetBlock> REDSTONE_MAGNET =
            REGISTRATE.block("redstone_magnet", ImplementedMagnetBlock.simple(2))
                    .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .transform(b -> b.onRegister(CreateRegistrate.connectedTextures(() -> new SimpleCTBehaviour(CNASpriteShifts.REDSTONE_MAGNET))))
                    .tag(CNATags.Block.MAGNET.blockTag)
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .simpleItem()
                    .register();

    public static final BlockEntry<ImplementedMagnetBlock> LAYERED_MAGNET =
            REGISTRATE.block("layered_magnet", ImplementedMagnetBlock.simple(4))
                    .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .tag(CNATags.Block.MAGNET.blockTag)
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .simpleItem()
                    .register();

    public static final BlockEntry<ImplementedMagnetBlock> FLUXUATED_MAGNETITE =
            REGISTRATE.block("fluxuated_magnetite", ImplementedMagnetBlock.simple(8))
                    .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .tag(CNATags.Block.MAGNET.blockTag)
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .simpleItem()
                    .register();

    public static final BlockEntry<ImplementedMagnetBlock> NETHERITE_MAGNET =
            REGISTRATE.block("netherite_magnet", ImplementedMagnetBlock.simple(24))
                    .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .tag(CNATags.Block.MAGNET.blockTag)
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .simpleItem()
                    .register();
    
    
    public static final BlockEntry<CarbonBrushesBlock> CARBON_BRUSHES =
            REGISTRATE.block("carbon_brushes", CarbonBrushesBlock::new)
                    .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .blockstate((c, p) -> p.directionalBlock(c.get(), p.models().getExistingFile(p.modLoc("block/carbon_brushes/base"))))
                    .item()
                    .transform(ItemShaftRenderer.itemTransformer(new Vector3f(0.0f), new Quaternionf()))
                    .build()
                    .register();
    
    public static final BlockEntry<MotorBlock> BASIC_MOTOR =
            REGISTRATE.block("basic_motor", (p) -> new MotorBlock(p, new BasicMotorVariant()))
                    .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .blockstate(new MotorBlockStateGen("motor")::generate)
                    .properties(properties -> properties.strength(3.0f))
                    .item()
                    .transform(ItemShaftRenderer.itemTransformer(new Vector3f(0.0f), Axis.XP.rotationDegrees(90.0f)))
                    .transform(b -> b.model((c, p) -> 
                            p.withExistingParent(c.getName(), p.modLoc("block/" + c.getName() + "/horizontal"))).build())
                    .register();

    public static final BlockEntry<MotorBlock> ADVANCED_MOTOR =
            REGISTRATE.block("advanced_motor", (p) -> new MotorBlock(p, new AdvancedMotorVariant()))
                    .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .properties(properties -> properties.strength(3.5f))
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .blockstate(new MotorBlockStateGen("motor")::generate)
                    .item()
                    .transform(ItemShaftRenderer.itemTransformer(new Vector3f(0.0f), Axis.XP.rotationDegrees(90.0f)))
                    .transform(b -> b.model((c, p) ->
                            p.withExistingParent(c.getName(), p.modLoc("block/" + c.getName() + "/horizontal"))).build())
                    .register();


    public static final BlockEntry<MotorBlock> REINFORCED_MOTOR =
            REGISTRATE.block("reinforced_motor", (p) -> new MotorBlock(p, new ReinforcedMotorVariant()))
                    .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .properties(properties -> properties.strength(4.0f))
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .blockstate(new MotorBlockStateGen("motor")::generate)
                    .item()
                    .transform(ItemShaftRenderer.itemTransformer(new Vector3f(0.0f), Axis.XP.rotationDegrees(90.0f)))
                    .transform(b -> b.model((c, p) ->
                            p.withExistingParent(c.getName(), p.modLoc("block/" + c.getName() + "/horizontal"))).build())
                    .register();

    public static final BlockEntry<MotorExtensionBlock> BASIC_MOTOR_EXTENSION =
            REGISTRATE.block("basic_motor_extension", (p) -> new MotorExtensionBlock(p, new BasicMotorExtensionVariant()))
                    .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .properties(properties -> properties.strength(4.0f))
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .blockstate(new MotorBlockStateGen("motor_extension")::generate)
                    .item()
                    .transform(b -> b.model((c, p) ->
                            p.withExistingParent(c.getName(), p.modLoc("block/" + c.getName() + "/horizontal"))).build())
                    .register();

    public static final BlockEntry<MotorExtensionBlock> ADVANCED_MOTOR_EXTENSION =
            REGISTRATE.block("advanced_motor_extension", (p) -> new MotorExtensionBlock(p, new AdvancedMotorExtensionVariant()))
                    .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .properties(properties -> properties.strength(4.0f))
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .blockstate(new MotorBlockStateGen("motor_extension")::generate)
                    .item()
                    .transform(b -> b.model((c, p) ->
                            p.withExistingParent(c.getName(), p.modLoc("block/" + c.getName() + "/horizontal"))).build())
                    .register();



    public static final BlockEntry<HeatPipeBlock> HEAT_PIPE =
            REGISTRATE.block("heat_pipe", HeatPipeBlock::new)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .properties(properties -> properties.strength(1.6f))
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .blockstate(CNABlockStateGen.heatPipe())
                    .item()
                    .transform(ModelGen.customItemModel())
                    .register();

    public static final BlockEntry<HeatPumpBlock> HEAT_PUMP =
            REGISTRATE.block("heat_pump", HeatPumpBlock::new)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .properties(properties -> properties.strength(1.6f))
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .blockstate(CNABlockStateGen.heatPump())
                    .item()
                    .transform(ModelGen.customItemModel())
                    .register();

    public static final BlockEntry<HeaterBlock> HEATER =
            REGISTRATE.block("heater", HeaterBlock::new)
                    .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .properties(properties -> properties.strength(2.0f))
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .blockstate(CNABlockStateGen.heater())
                    .item()
                    .transform(b -> b.model((c, p) -> p.withExistingParent(c.getName(), p.modLoc("block/" + c.getName()))
                            .texture("top", p.modLoc("block/heater_top_0"))).build())
                    .register();


    public static final BlockEntry<StirlingEngineBlock> STIRLING_ENGINE =
            REGISTRATE.block("stirling_engine", properties -> new StirlingEngineBlock(properties, CNABlockEntityTypes.STIRLING_ENGINE))
                    .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .properties(properties -> properties.strength(2.0f))
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .blockstate((c, p) -> p.simpleBlock(c.get(), p.models().getExistingFile(p.modLoc(c.getName()))))
                    .transform(BlockStressDefaults.setCapacity(32.0))
                    .simpleItem()
                    .register();

    public static final BlockEntry<SolidCoriumBlock> SOLID_CORIUM =
            REGISTRATE.block("solid_corium", SolidCoriumBlock::new)
                    .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .properties(properties -> properties.strength(50.0f))
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .simpleItem()
                    .register();

    public static final BlockEntry<CoriumBlock> CORIUM =
            REGISTRATE.block("corium", CoriumBlock::new)
                    .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .properties(properties -> properties.strength(70.0f))
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .simpleItem()
                    .register();

    public static final BlockEntry<ReactorBlock> REACTOR_CASING =
            REGISTRATE.block("reactor_casing", ReactorBlock::new)
                    .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .transform(b -> b.onRegister(CreateRegistrate.connectedTextures(() -> new SimpleCTBehaviour(CNASpriteShifts.REACTOR_CASING))))
                    .tag(CNATags.Block.STOPS_RADIATION.blockTag)
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .simpleItem()
                    .register();

    public static final BlockEntry<ReactorRodBlock> REACTOR_ROD =
            REGISTRATE.block("reactor_rod", ReactorRodBlock::new)
                    .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .blockstate(CNABlockStateGen.reactorRod())
                    .tag(CNATags.Block.STOPS_RADIATION.blockTag)
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .simpleItem()
                    .register();

    public static final BlockEntry<ReactorTransparentBlock> REACTOR_GLASS =
            REGISTRATE.block("reactor_glass", ReactorTransparentBlock::new)
                    .initialProperties(() -> Blocks.GLASS)
                    .properties(p -> p.isViewBlocking((blockState, blockGetter, blockPos) -> false))
                    .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .properties(p -> p.isViewBlocking((a,b,c) -> false))
                    .transform(b -> b.onRegister(CreateRegistrate.connectedTextures(() -> new SimpleCTBehaviour(CNASpriteShifts.REACTOR_GLASS))))
                    .tag(CNATags.Block.STOPS_RADIATION.blockTag)
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .simpleItem()
                    .register();

    public static final BlockEntry<ReactorFuelAcceptorBlock> REACTOR_FUEL_ACCEPTOR =
            REGISTRATE.block("reactor_fuel_acceptor", ReactorFuelAcceptorBlock::new)
                    .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .blockstate((c, p) -> p.directionalBlock(c.get(), p.models().getExistingFile(p.modLoc(c.getName()))))
                    .tag(CNATags.Block.STOPS_RADIATION.blockTag)
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .simpleItem()
                    .register();


    public static final BlockEntry<ReactorHeatVentBlock> REACTOR_HEAT_VENT =
            REGISTRATE.block("reactor_heat_vent", ReactorHeatVentBlock::new)
                    .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .blockstate((c, p) -> p.directionalBlock(c.get(), p.models().getExistingFile(p.modLoc(c.getName()))))
                    .tag(CNATags.Block.STOPS_RADIATION.blockTag)
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .simpleItem()
                    .register();


    public static final BlockEntry<SolarHeatingPlateBlock> BASIC_SOLAR_HEATING_PLATE =
            REGISTRATE.block("basic_solar_heating_plate", SolarHeatingPlateBlock::createBasic)
                    .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .blockstate((c, p) -> p.simpleBlock(c.get(), p.models().getExistingFile(p.modLoc(c.getName()))))
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .simpleItem()
                    .register();

    public static final BlockEntry<SolarHeatingPlateBlock> ADVANCED_SOLAR_HEATING_PLATE =
            REGISTRATE.block("advanced_solar_heating_plate", SolarHeatingPlateBlock::createAdvanced)
                    .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .blockstate((c, p) -> p.simpleBlock(c.get(), p.models().getExistingFile(p.modLoc(c.getName()))))
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .simpleItem()
                    .register();


    public static final BlockEntry<Block> THORIUM_ORE =
            REGISTRATE.block("thorium_ore", Block::new)
                    .properties((p) -> p.strength(3.5f).requiresCorrectToolForDrops())
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .tag(BlockTags.NEEDS_IRON_TOOL)
                    .loot((lt, b) -> lt.add(b,
                            RegistrateBlockLootTables.createSilkTouchDispatchTable(b,
                                    lt.applyExplosionDecay(b, LootItem.lootTableItem(CNAItems.THORIUM)
                                            .apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))))))
                    .item(AssemblyOperatorBlockItem::new)
                    .build()
                    .register();

    public static void load() {  }
}
