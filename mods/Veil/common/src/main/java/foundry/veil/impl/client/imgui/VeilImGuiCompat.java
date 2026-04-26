package foundry.veil.impl.client.imgui;

import com.mojang.blaze3d.platform.InputConstants;
import foundry.imgui.api.ImGuiMCEvents;
import foundry.veil.Veil;
import foundry.veil.api.client.editor.EditorManager;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.impl.client.editor.*;
import net.minecraft.client.KeyMapping;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class VeilImGuiCompat {

    public static final KeyMapping EDITOR_KEY = new KeyMapping("key.veil.editor", InputConstants.KEY_F6, "key.categories.veil");

    private VeilImGuiCompat() {
    }

    public static void load() {
        ImGuiMCEvents.INSTANCE.onImGuiLoad(() -> {
            ImGuiMCEvents.INSTANCE.preRenderImGuiEvents(() -> {
                VeilImGuiStylesheet.initStyles();
                AdvancedFboImGuiAreaImpl.begin();
                VeilRenderSystem.renderer().getEditorManager().render();
            });
            ImGuiMCEvents.INSTANCE.postRenderImGuiEvents(() -> {
                VeilImGuiStylesheet.initStyles();
                VeilRenderSystem.renderer().getEditorManager().renderLast();
                AdvancedFboImGuiAreaImpl.end();
            });

            EditorManager editorManager = VeilRenderSystem.renderer().getEditorManager();

            // Example for devs
            if (Veil.platform().isDevelopmentEnvironment()) {
                editorManager.add(new DemoInspector());
            }

            // Debug editors
            editorManager.add(new DeviceInfoViewer());
            editorManager.add(new PipelineStatisticsViewer());
            editorManager.add(new PostInspector());
            editorManager.add(new ShaderInspector());
            editorManager.add(new TextureInspector());
            editorManager.add(new LightInspector());
            editorManager.add(new FramebufferInspector());
            editorManager.add(new ResourceManagerInspector());
        });
    }
}
