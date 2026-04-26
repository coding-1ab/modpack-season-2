package foundry.veil.forge;

import foundry.veil.Veil;
import foundry.veil.VeilClient;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.forge.event.ForgeVeilRegisterFixedBuffersEvent;
import foundry.veil.forge.event.ForgeVeilRendererAvailableEvent;
import foundry.veil.forge.impl.ForgeRenderTypeStageHandler;
import foundry.veil.impl.VeilBuiltinPacks;
import foundry.veil.impl.VeilReloadListeners;
import foundry.veil.impl.client.imgui.VeilImGuiCompat;
import foundry.veil.impl.client.render.shader.VeilVanillaShaders;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@Mod(value = Veil.MODID, dist = Dist.CLIENT)
public class VeilForgeClient {

    public VeilForgeClient(IEventBus modEventBus) {
        VeilClient.init();

        modEventBus.addListener(VeilForgeClient::registerKeys);
        modEventBus.addListener(VeilForgeClient::registerListeners);
        modEventBus.addListener(VeilForgeClient::registerShaders);
        modEventBus.addListener(VeilForgeClient::addPackFinders);
    }

    private static void registerListeners(RegisterClientReloadListenersEvent event) {
        VeilRenderSystem.init();
        VeilReloadListeners.registerListeners((type, id, listener) -> event.registerReloadListener(listener));
        ModLoader.postEvent(new ForgeVeilRendererAvailableEvent(VeilRenderSystem.renderer()));
        ModLoader.postEvent(new ForgeVeilRegisterFixedBuffersEvent(ForgeRenderTypeStageHandler::register));
    }

    private static void registerKeys(RegisterKeyMappingsEvent event) {
        if (VeilClient.IMGUIMC_LOADED) {
            event.register(VeilImGuiCompat.EDITOR_KEY);
        }
    }

    private static void registerShaders(RegisterShadersEvent event) {
        try {
            VeilVanillaShaders.registerShaders((id, vertexFormat, loadCallback) -> event.registerShader(new ShaderInstance(event.getResourceProvider(), id, vertexFormat), loadCallback));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // TODO allow pack enabled by default
    private static void addPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            VeilBuiltinPacks.registerPacks((id, defaultEnabled) -> event.addPackFinders(
                    ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "resourcepacks/" + id.getPath()),
                    PackType.CLIENT_RESOURCES,
                    Component.literal(id.getNamespace() + "/" + id.getPath()),
                    PackSource.DEFAULT,
                    false,
                    Pack.Position.TOP
            ));
        }
    }
}
