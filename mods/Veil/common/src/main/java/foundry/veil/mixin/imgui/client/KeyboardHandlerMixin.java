package foundry.veil.mixin.imgui.client;

import foundry.veil.Veil;
import imgui.ImGui;
import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {

    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    public void keyCallback(long window, int key, int scancode, int action, int mods, CallbackInfo ci) {
        Veil.withImGui(() -> {
            if (ImGui.getIO().getWantCaptureKeyboard()) {
                ci.cancel();
            }
        });
    }

    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    public void charCallback(long window, int codepoint, int mods, CallbackInfo ci) {
        Veil.withImGui(() -> {
            if (ImGui.getIO().getWantCaptureKeyboard()) {
                ci.cancel();
            }
        });
    }
}
