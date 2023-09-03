package org.antarcticgardens.newage;

import com.tterrag.registrate.fabric.SimpleFlowableFluid;
import com.tterrag.registrate.util.entry.FluidEntry;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributeHandler;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import static org.antarcticgardens.newage.CreateNewAge.REGISTRATE;

@SuppressWarnings("UnstableApiUsage")
public class NewAgeFluids {
    public static final FluidEntry<SimpleFlowableFluid.Flowing> GLOWSTONE_ELECTROLYTE;
    
    static {
        var builder = REGISTRATE
                .fluid("glowstone_electrolyte",
                        new ResourceLocation(CreateNewAge.MOD_ID, "fluid/glowstone_electrolyte_still"),
                        new ResourceLocation(CreateNewAge.MOD_ID, "fluid/glowstone_electrolyte_flow"))
                .fluidAttributes(() -> new AttributeHandler("fluid.create_new_age.glowstone_electrolyte", 2000, 8))
                .source(SimpleFlowableFluid.Source::new);
        
        builder.block().properties(p -> p.lightLevel(state -> 8)).register();
        GLOWSTONE_ELECTROLYTE = builder.register();
    }

    public static void load() { }
    
    public static void clientSideLoad() {
        BlockRenderLayerMap.INSTANCE.putFluids(RenderType.translucent(), GLOWSTONE_ELECTROLYTE.get(), GLOWSTONE_ELECTROLYTE.getSource());
    }
    
    private record AttributeHandler(String key, int viscosity, int luminance) implements FluidVariantAttributeHandler {
        @Override
        public Component getName(FluidVariant fluidVariant) {
            return Component.translatable(key);
        }

        @Override
        public int getViscosity(FluidVariant variant, @Nullable Level world) {
            return viscosity;
        }

        @Override
        public int getLuminance(FluidVariant variant) {
            return luminance;
        }
    }
}
