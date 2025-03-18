package foundry.veil.impl.client.imgui;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.ObjIntConsumer;

/**
 * Manages the internal ImGui state.
 *
 * @author Ocelot
 */
@ApiStatus.Internal
public interface VeilImGui {

    void start();

    void stop();

    void beginFrame();

    void endFrame();

    void toggle();

    void updateFonts();

    void addImguiShaders(ObjIntConsumer<ResourceLocation> registry);
}
