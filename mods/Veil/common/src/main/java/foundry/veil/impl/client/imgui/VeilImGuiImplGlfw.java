package foundry.veil.impl.client.imgui;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImVec2;
import imgui.callback.ImStrConsumer;
import imgui.callback.ImStrSupplier;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiMouseButton;
import imgui.glfw.ImGuiImplGlfw;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.*;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;

public class VeilImGuiImplGlfw extends ImGuiImplGlfw {

    private final VeilImGuiImpl impl;

    public VeilImGuiImplGlfw(VeilImGuiImpl impl) {
        this.impl = impl;
    }

    protected static class VeilData extends Data {
        public long getWindow() {
            return this.window;
        }

        public long getMouseWindow() {
            return this.mouseWindow;
        }

        public ImVec2 getLastValidMousePos() {
            return this.lastValidMousePos;
        }

        public long[] getKeyOwnerWindows() {
            return this.keyOwnerWindows;
        }

        public GLFWWindowFocusCallback getPrevUserCallbackWindowFocus() {
            return this.prevUserCallbackWindowFocus;
        }

        public GLFWCursorPosCallback getPrevUserCallbackCursorPos() {
            return this.prevUserCallbackCursorPos;
        }

        public GLFWCursorEnterCallback getPrevUserCallbackCursorEnter() {
            return this.prevUserCallbackCursorEnter;
        }

        public GLFWMouseButtonCallback getPrevUserCallbackMousebutton() {
            return this.prevUserCallbackMousebutton;
        }

        public GLFWScrollCallback getPrevUserCallbackScroll() {
            return this.prevUserCallbackScroll;
        }

        public GLFWKeyCallback getPrevUserCallbackKey() {
            return this.prevUserCallbackKey;
        }

        public GLFWCharCallback getPrevUserCallbackChar() {
            return this.prevUserCallbackChar;
        }

        public GLFWMonitorCallback getPrevUserCallbackMonitor() {
            return this.prevUserCallbackMonitor;
        }

        public void setMouseWindow(long mouseWindow) {
            this.mouseWindow = mouseWindow;
        }
    }

    private VeilData getData() {
        return (VeilData) this.data;
    }

    @Override
    protected ImStrSupplier getClipboardTextFn() {
        return new ImStrSupplier() {
            @Override
            public String get() {
                long window = VeilImGuiImplGlfw.this.getData().getWindow();
                if (Minecraft.getInstance().getWindow().getWindow() == window) {
                    return Minecraft.getInstance().keyboardHandler.getClipboard();
                }

                final String clipboardString = glfwGetClipboardString(window);
                return clipboardString != null ? clipboardString : "";
            }
        };
    }

    @Override
    protected ImStrConsumer setClipboardTextFn() {
        return new ImStrConsumer() {
            @Override
            public void accept(final String text) {
                long window = VeilImGuiImplGlfw.this.getData().getWindow();
                if (Minecraft.getInstance().getWindow().getWindow() == window) {
                    Minecraft.getInstance().keyboardHandler.setClipboard(text);
                } else {
                    glfwSetClipboardString(window, text);
                }
            }
        };
    }

    // All these callbacks are overridden so no other mod can break the ImGui state with their callbacks

    @Override
    public void mouseButtonCallback(long window, int button, int action, int mods) {
        VeilData data = this.getData();
        if (data.getPrevUserCallbackMousebutton() != null && window == data.getWindow()) {
            data.getPrevUserCallbackMousebutton().invoke(window, button, action, mods);
        }

        try {
            this.impl.start();

            this.updateKeyModifiers(mods);

            final ImGuiIO io = ImGui.getIO();
            if (button >= 0 && button < ImGuiMouseButton.COUNT) {
                io.addMouseButtonEvent(button, action == GLFW_PRESS);
            }
        } finally {
            this.impl.stop();
        }
    }

    @Override
    public void scrollCallback(long window, double xOffset, double yOffset) {
        VeilData data = this.getData();
        if (data.getPrevUserCallbackScroll() != null && window == data.getWindow()) {
            data.getPrevUserCallbackScroll().invoke(window, xOffset, yOffset);
        }

        try {
            this.impl.start();

            final ImGuiIO io = ImGui.getIO();
            io.addMouseWheelEvent((float) xOffset, (float) yOffset);
        } finally {
            this.impl.stop();
        }
    }

    @Override
    public void keyCallback(long window, int keycode, int scancode, int action, int mods) {
        VeilData data = this.getData();
        if (data.getPrevUserCallbackKey() != null && window == data.getWindow()) {
            data.getPrevUserCallbackKey().invoke(window, keycode, scancode, action, mods);
        }

        try {
            this.impl.start();

            if (action != GLFW_PRESS && action != GLFW_RELEASE) {
                return;
            }

            {
                int keyModifiers = mods;

                // Workaround: X11 does not include current pressed/released modifier key in 'mods' flags. https://github.com/glfw/glfw/issues/1630
                final int keycodeToMod = this.keyToModifier(keycode);
                if (keycodeToMod != 0) {
                    keyModifiers = (action == GLFW_PRESS) ? (mods | keycodeToMod) : (mods & ~keycodeToMod);
                }

                this.updateKeyModifiers(keyModifiers);
            }

            if (keycode >= 0 && keycode < data.getKeyOwnerWindows().length) {
                data.getKeyOwnerWindows()[keycode] = (action == GLFW_PRESS) ? window : -1;
            }

            final int key = this.translateUntranslatedKey(keycode, scancode);

            final ImGuiIO io = ImGui.getIO();
            final int imguiKey = this.glfwKeyToImGuiKey(key);
            io.addKeyEvent(imguiKey, (action == GLFW_PRESS));
            io.setKeyEventNativeData(imguiKey, key, scancode); // To support legacy indexing (<1.87 user code)
        } finally {
            this.impl.stop();
        }
    }

    @Override
    public void windowFocusCallback(long window, boolean focused) {
        VeilData data = this.getData();
        if (data.getPrevUserCallbackWindowFocus() != null && window == data.getWindow()) {
            data.getPrevUserCallbackWindowFocus().invoke(window, focused);
        }

        try {
            this.impl.start();

            ImGui.getIO().addFocusEvent(focused);
        } finally {
            this.impl.stop();
        }
    }

    @Override
    public void cursorPosCallback(long window, double x, double y) {
        VeilData data = this.getData();
        if (data.getPrevUserCallbackCursorPos() != null && window == data.getWindow()) {
            data.getPrevUserCallbackCursorPos().invoke(window, x, y);
        }

        try {
            this.impl.start();

            float posX = (float) x;
            float posY = (float) y;

            final ImGuiIO io = ImGui.getIO();

            if (io.hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
                try (MemoryStack stack = MemoryStack.stackPush()) {
                    IntBuffer windowX = stack.mallocInt(1);
                    IntBuffer windowY = stack.mallocInt(1);
                    glfwGetWindowPos(window, windowX, windowY);
                    posX += windowX.get(0);
                    posY += windowY.get(0);
                }
            }

            io.addMousePosEvent(posX, posY);
            data.getLastValidMousePos().set(posX, posY);
        } finally {
            this.impl.stop();
        }
    }

    @Override
    public void cursorEnterCallback(long window, boolean entered) {
        VeilData data = this.getData();
        if (data.getPrevUserCallbackCursorEnter() != null && window == data.getWindow()) {
            data.getPrevUserCallbackCursorEnter().invoke(window, entered);
        }

        try {
            this.impl.start();

            final ImGuiIO io = ImGui.getIO();

            if (entered) {
                data.setMouseWindow(window);
                io.addMousePosEvent(data.getLastValidMousePos().x, data.getLastValidMousePos().y);
            } else if (data.getMouseWindow() == window) {
                io.getMousePos(data.getLastValidMousePos());
                data.setMouseWindow(-1);
                io.addMousePosEvent(Float.MIN_VALUE, Float.MIN_VALUE);
            }
        } finally {
            this.impl.stop();
        }
    }

    @Override
    public void charCallback(long window, int c) {
        VeilData data = this.getData();
        if (data.getPrevUserCallbackChar() != null && window == data.getWindow()) {
            data.getPrevUserCallbackChar().invoke(window, c);
        }

        try {
            this.impl.start();
            ImGui.getIO().addInputCharacter(c);
        } finally {
            this.impl.stop();
        }
    }

    @Override
    public void monitorCallback(long window, int event) {
        VeilData data = this.getData();
        if (data.getPrevUserCallbackMonitor() != null && window == data.getWindow()) {
            data.getPrevUserCallbackMonitor().invoke(window, event);
        }

        super.monitorCallback(window, event);
    }

    @Override
    protected Data newData() {
        return new VeilData();
    }
}