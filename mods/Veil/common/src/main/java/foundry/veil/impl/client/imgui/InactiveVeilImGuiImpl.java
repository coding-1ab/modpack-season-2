package foundry.veil.impl.client.imgui;

import org.jetbrains.annotations.ApiStatus;

@Deprecated
@ApiStatus.Internal
public enum InactiveVeilImGuiImpl implements VeilImGui {

    INSTANCE;

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void beginFrame() {
    }

    @Override
    public void endFrame() {
    }

    @Override
    public void updateFonts() {
    }
}
