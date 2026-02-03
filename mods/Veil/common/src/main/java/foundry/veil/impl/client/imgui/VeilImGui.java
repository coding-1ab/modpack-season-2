package foundry.veil.impl.client.imgui;

import org.jetbrains.annotations.ApiStatus;

/**
 * Manages the internal ImGui state.
 *
 * @author Ocelot
 */
@Deprecated
@ApiStatus.Internal
public interface VeilImGui {

    void start();

    void stop();

    void beginFrame();

    void endFrame();

    void updateFonts();
}
