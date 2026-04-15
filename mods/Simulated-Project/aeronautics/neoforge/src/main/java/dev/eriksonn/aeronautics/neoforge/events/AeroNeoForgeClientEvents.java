package dev.eriksonn.aeronautics.neoforge.events;

import dev.eriksonn.aeronautics.Aeronautics;
import dev.eriksonn.aeronautics.events.AeronauticsClientEvents;
import dev.eriksonn.aeronautics.index.AeroBlocks;
import dev.eriksonn.aeronautics.index.client.AeroRenderTypes;
import dev.eriksonn.aeronautics.neoforge.content.fluids.AeroFluidType;
import dev.eriksonn.aeronautics.neoforge.index.AeroFluidsNeoForge;
import foundry.veil.forge.event.ForgeVeilRegisterBlockLayersEvent;
import foundry.veil.forge.event.ForgeVeilRegisterFixedBuffersEvent;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = Aeronautics.MOD_ID, value = Dist.CLIENT)
public class AeroNeoForgeClientEvents {

    @SubscribeEvent
    public static void preClientTick(final ClientTickEvent.Pre event) {
        AeronauticsClientEvents.clientLevelTick(false);
    }

    @SubscribeEvent
    public static void postClientTick(final ClientTickEvent.Post event) {
        AeronauticsClientEvents.clientLevelTick(true);
    }


    @EventBusSubscriber(modid = Aeronautics.MOD_ID, value = Dist.CLIENT)
    public static class ModBusEvents {

        @SubscribeEvent
        public static void registerClientExtensions(final RegisterClientExtensionsEvent event) {
            final AeroFluidType type = (AeroFluidType) AeroFluidsNeoForge.LEVITITE_BLEND.getType();
            event.registerFluidType(type, type);
        }

        @SubscribeEvent
        public static void clientSetup(final FMLClientSetupEvent event) {
            final ChunkRenderTypeSet set = ChunkRenderTypeSet.of(RenderType.SOLID, AeroRenderTypes.levitite(), AeroRenderTypes.levititeGhosts());
            ItemBlockRenderTypes.setRenderLayer(AeroBlocks.LEVITITE.get(), set);
            ItemBlockRenderTypes.setRenderLayer(AeroBlocks.PEARLESCENT_LEVITITE.get(), set);
        }

        @SubscribeEvent
        public static void registerBlockLayersEvent(final ForgeVeilRegisterBlockLayersEvent event) {
            event.registerBlockLayer(AeroRenderTypes.levitite());
            event.registerBlockLayer(AeroRenderTypes.levititeGhosts());
        }

        @SubscribeEvent
        public static void registerFixedBuffersEvent(final ForgeVeilRegisterFixedBuffersEvent event) {
            event.register(RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES, AeroRenderTypes.levitite());
            event.register(RenderLevelStageEvent.Stage.AFTER_WEATHER, AeroRenderTypes.levititeGhosts());
        }

        @SubscribeEvent
        public static void registerRegisterStageEvent(final RenderLevelStageEvent.RegisterStageEvent event) {
            event.register(Aeronautics.path("levitite"), AeroRenderTypes.levitite());
            event.register(Aeronautics.path("levitite_ghosts"), AeroRenderTypes.levititeGhosts());
        }
    }
}
