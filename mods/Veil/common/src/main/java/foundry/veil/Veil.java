package foundry.veil;

import com.mojang.blaze3d.systems.RenderSystem;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.molang.VeilMolang;
import foundry.veil.impl.client.imgui.VeilImGui;
import foundry.veil.impl.client.imgui.VeilImGuiImpl;
import foundry.veil.platform.VeilPlatform;
import gg.moonflower.molangcompiler.api.MolangCompiler;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ServiceLoader;

public class Veil {

    public static final String MODID = "veil";
    public static final Logger LOGGER = LoggerFactory.getLogger("Veil");
    public static final boolean DEBUG;
    public static final boolean IMGUI;
    public static final boolean VERBOSE_SHADER_ERRORS;
    public static boolean RENDERDOC;

    private static final VeilPlatform PLATFORM = ServiceLoader.load(VeilPlatform.class).findFirst().orElseThrow(() -> new RuntimeException("Veil expected platform implementation"));

    public static final boolean SODIUM = PLATFORM.isModLoaded("sodium");
    public static final boolean IRIS = PLATFORM.isModLoaded("iris");

    static {
        DEBUG = System.getProperty("veil.debug") != null;
        IMGUI = System.getProperty("veil.disableImgui") == null;
        VERBOSE_SHADER_ERRORS = System.getProperty("veil.verboseShaderErrors") != null;
    }

    @ApiStatus.Internal
    public static void init() {
        LOGGER.info("Veil is initializing.");
        if (DEBUG) {
            LOGGER.info("Veil Debug Enabled");
        }
        if (!IMGUI) {
            LOGGER.info("ImGui Disabled");
        }
        VeilMolang.set(MolangCompiler.create(MolangCompiler.DEFAULT_FLAGS, Veil.class.getClassLoader()));
    }

    /**
     * Runs the specified code with the correct ImGui context.
     *
     * @param task The ImGui task to run
     */
    public static void withImGui(Runnable task) {
        if (!RenderSystem.isOnRenderThreadOrInit()) {
            LOGGER.error("Called Veil#withImGui() on another thread");
            return;
        }

        if (VeilRenderSystem.hasImGui()) {
            VeilImGui imGui = VeilImGuiImpl.get();
            imGui.start();
            task.run();
            imGui.stop();
        }
    }

    public static ResourceLocation veilPath(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public static VeilPlatform platform() {
        return PLATFORM;
    }
}
