package dev.propulsionteam.propulsionsimulated.registries;

import dev.propulsionteam.propulsionsimulated.CreatePropulsion;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FlowingFluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Consumer;

public class PropulsionFluids {
    private static final ResourceLocation TURPENTINE_STILL = ResourceLocation.parse("minecraft:block/water_still");
    private static final ResourceLocation TURPENTINE_FLOW = ResourceLocation.parse("minecraft:block/water_flow");
    private static final String TURPENTINE_DESCRIPTION = "fluid." + CreatePropulsion.ID + ".turpentine";

    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, CreatePropulsion.ID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, CreatePropulsion.ID);

    public static final DeferredHolder<FluidType, FluidType> TURPENTINE_TYPE = FLUID_TYPES.register("turpentine_type",
            () -> new TurpentineFluidType(FluidType.Properties.create().density(500).viscosity(1000)));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> TURPENTINE = FLUIDS.register("turpentine",
            () -> new BaseFlowingFluid.Source(turpentineProperties()));
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> FLOWING_TURPENTINE = FLUIDS.register("flowing_turpentine",
            () -> new BaseFlowingFluid.Flowing(turpentineProperties()));

    public static final DeferredBlock<LiquidBlock> TURPENTINE_BLOCK = PropulsionBlocks.BLOCKS.register("turpentine",
            () -> new LiquidBlock((FlowingFluid) TURPENTINE.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));

    public static void register(IEventBus modBus) {
        FLUID_TYPES.register(modBus);
        FLUIDS.register(modBus);
    }

    private static BaseFlowingFluid.Properties turpentineProperties() {
        return new BaseFlowingFluid.Properties(
                TURPENTINE_TYPE,
                TURPENTINE,
                FLOWING_TURPENTINE
        ).bucket(PropulsionItems.TURPENTINE_BUCKET)
                .block(TURPENTINE_BLOCK)
                .levelDecreasePerBlock(1)
                .tickRate(7)
                .slopeFindDistance(3)
                .explosionResistance(100f);
    }

    private static final class TurpentineFluidType extends FluidType {
        private TurpentineFluidType(final Properties properties) {
            super(properties);
        }

        @Override
        public String getDescriptionId() {
            return TURPENTINE_DESCRIPTION;
        }

        @Override
        public Component getDescription() {
            return Component.translatable(TURPENTINE_DESCRIPTION);
        }

        @Override
        public void initializeClient(final Consumer<IClientFluidTypeExtensions> consumer) {
            consumer.accept(new IClientFluidTypeExtensions() {
                @Override
                public ResourceLocation getStillTexture() {
                    return TURPENTINE_STILL;
                }

                @Override
                public ResourceLocation getFlowingTexture() {
                    return TURPENTINE_FLOW;
                }

                @Override
                public int getTintColor() {
                    return 0xFFD69E49;
                }
            });
        }
    }
}
