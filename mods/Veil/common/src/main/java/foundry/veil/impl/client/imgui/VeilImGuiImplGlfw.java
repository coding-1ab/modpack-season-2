package foundry.veil.impl.client.imgui;

import imgui.*;
import imgui.callback.*;
import imgui.flag.*;
import imgui.glfw.ImGuiImplGlfw;
import imgui.lwjgl3.glfw.ImGuiImplGlfwNative;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.*;
import org.lwjgl.system.NativeResource;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

@ApiStatus.Internal
public class VeilImGuiImplGlfw implements NativeResource {

    private static final boolean IS_WINDOWS = Util.getPlatform() == Util.OS.WINDOWS;

    // Pointer of the current GLFW window
    private long windowPtr;

    // Some features may be available only from a specific version
    private boolean glfwHawWindowTopmost;
    private boolean glfwHasWindowAlpha;
    private boolean glfwHasPerMonitorDpi;
    private boolean glfwHasFocusWindow;
    private boolean glfwHasFocusOnShow;
    private boolean glfwHasMonitorWorkArea;
    private boolean glfwHasOsxWindowPosFix;

    // For application window properties
    private final int[] winWidth = new int[1];
    private final int[] winHeight = new int[1];
    private final int[] fbWidth = new int[1];
    private final int[] fbHeight = new int[1];

    // Mouse cursors provided by GLFW
    private final long[] mouseCursors = new long[ImGuiMouseCursor.COUNT];
    private final long[] keyOwnerWindows = new long[512];

    // Empty array to fill ImGuiIO.NavInputs with zeroes
    private final float[] emptyNavInputs = new float[ImGuiNavInput.COUNT];

    // For mouse tracking
    private final boolean[] mouseJustPressed = new boolean[ImGuiMouseButton.COUNT];
    private final ImVec2 mousePosBackup = new ImVec2();
    private final double[] mouseX = new double[1];
    private final double[] mouseY = new double[1];

    private final int[] windowX = new int[1];
    private final int[] windowY = new int[1];

    // Monitor properties
    private final int[] monitorX = new int[1];
    private final int[] monitorY = new int[1];
    private final int[] monitorWorkAreaX = new int[1];
    private final int[] monitorWorkAreaY = new int[1];
    private final int[] monitorWorkAreaWidth = new int[1];
    private final int[] monitorWorkAreaHeight = new int[1];
    private final float[] monitorContentScaleX = new float[1];
    private final float[] monitorContentScaleY = new float[1];

    // GLFW callbacks
    private GLFWWindowFocusCallback prevUserCallbackWindowFocus = null;
    private GLFWMouseButtonCallback prevUserCallbackMouseButton = null;
    private GLFWScrollCallback prevUserCallbackScroll = null;
    private GLFWKeyCallback prevUserCallbackKey = null;
    private GLFWCharCallback prevUserCallbackChar = null;
    private GLFWMonitorCallback prevUserCallbackMonitor = null;
    private GLFWCursorEnterCallback prevUserCallbackCursorEnter = null;

    // Internal data
    private boolean callbacksInstalled = false;
    private boolean wantUpdateMonitors = true;
    private double time = 0.0;
    private long mouseWindowPtr;

    private final VeilImGuiImpl impl;

    public VeilImGuiImplGlfw(VeilImGuiImpl impl) {
        this.impl = impl;
    }

    /**
     * Method to set the {@link GLFWMouseButtonCallback}.
     *
     * @param windowId pointer to the window
     * @param button   clicked mouse button
     * @param action   click action type
     * @param mods     click modifiers
     */
    public void mouseButtonCallback(final long windowId, final int button, final int action, final int mods) {
        if (this.prevUserCallbackMouseButton != null && windowId == this.windowPtr) {
            this.prevUserCallbackMouseButton.invoke(windowId, button, action, mods);
        }

        if (action == GLFW_PRESS && button >= 0 && button < this.mouseJustPressed.length) {
            this.mouseJustPressed[button] = true;
        }
    }

    /**
     * Method to set the {@link GLFWScrollCallback}.
     *
     * @param windowId pointer to the window
     * @param xOffset  scroll offset by x-axis
     * @param yOffset  scroll offset by y-axis
     */
    public void scrollCallback(final long windowId, final double xOffset, final double yOffset) {
        if (this.prevUserCallbackScroll != null && windowId == this.windowPtr) {
            this.prevUserCallbackScroll.invoke(windowId, xOffset, yOffset);
        }

        try {
            this.impl.start();
            final ImGuiIO io = ImGui.getIO();
            io.setMouseWheelH(io.getMouseWheelH() + (float) xOffset);
            io.setMouseWheel(io.getMouseWheel() + (float) yOffset);
        } finally {
            this.impl.stop();
        }
    }

    /**
     * Method to set the {@link GLFWKeyCallback}.
     *
     * @param windowId pointer to the window
     * @param key      pressed key
     * @param scancode key scancode
     * @param action   press action
     * @param mods     press modifiers
     */
    public void keyCallback(final long windowId, final int key, final int scancode, final int action, final int mods) {
        if (this.prevUserCallbackKey != null && windowId == this.windowPtr) {
            this.prevUserCallbackKey.invoke(windowId, key, scancode, action, mods);
        }

        try {
            this.impl.start();
            final ImGuiIO io = ImGui.getIO();

            if (key >= 0 && key < this.keyOwnerWindows.length) {
                if (action == GLFW_PRESS) {
                    io.setKeysDown(key, true);
                    this.keyOwnerWindows[key] = windowId;
                } else if (action == GLFW_RELEASE) {
                    io.setKeysDown(key, false);
                    this.keyOwnerWindows[key] = 0;
                }
            }

            io.setKeyCtrl(io.getKeysDown(GLFW_KEY_LEFT_CONTROL) || io.getKeysDown(GLFW_KEY_RIGHT_CONTROL));
            io.setKeyShift(io.getKeysDown(GLFW_KEY_LEFT_SHIFT) || io.getKeysDown(GLFW_KEY_RIGHT_SHIFT));
            io.setKeyAlt(io.getKeysDown(GLFW_KEY_LEFT_ALT) || io.getKeysDown(GLFW_KEY_RIGHT_ALT));
            io.setKeySuper(io.getKeysDown(GLFW_KEY_LEFT_SUPER) || io.getKeysDown(GLFW_KEY_RIGHT_SUPER));
        } finally {
            this.impl.stop();
        }
    }

    /**
     * Method to set the {@link GLFWWindowFocusCallback}.
     *
     * @param windowId pointer to the window
     * @param focused  is window focused
     */
    public void windowFocusCallback(final long windowId, final boolean focused) {
        if (this.prevUserCallbackWindowFocus != null && windowId == this.windowPtr) {
            this.prevUserCallbackWindowFocus.invoke(windowId, focused);
        }

        try {
            this.impl.start();
            ImGui.getIO().addFocusEvent(focused);
        } finally {
            this.impl.stop();
        }
    }

    /**
     * Method to set the {@link GLFWCursorEnterCallback}.
     *
     * @param windowId pointer to the window
     * @param entered  is cursor entered
     */
    public void cursorEnterCallback(final long windowId, final boolean entered) {
        if (this.prevUserCallbackCursorEnter != null && windowId == this.windowPtr) {
            this.prevUserCallbackCursorEnter.invoke(windowId, entered);
        }

        if (entered) {
            this.mouseWindowPtr = windowId;
        }
        if (!entered && this.mouseWindowPtr == windowId) {
            this.mouseWindowPtr = 0;
        }
    }

    /**
     * Method to set the {@link GLFWCharCallback}.
     *
     * @param windowId pointer to the window
     * @param c        pressed char
     */
    public void charCallback(final long windowId, final int c) {
        if (this.prevUserCallbackChar != null && windowId == this.windowPtr) {
            this.prevUserCallbackChar.invoke(windowId, c);
        }

        try {
            this.impl.start();
            final ImGuiIO io = ImGui.getIO();
            io.addInputCharacter(c);
        } finally {
            this.impl.stop();
        }
    }

    /**
     * Method to set the {@link GLFWMonitorCallback}.
     *
     * @param windowId pointer to the window
     * @param event    monitor event type (ignored)
     */
    public void monitorCallback(final long windowId, final int event) {
        this.wantUpdateMonitors = true;
    }

    /**
     * Method to do an initialization of the {@link ImGuiImplGlfw} state. It SHOULD be called before calling the {@link ImGuiImplGlfw#newFrame()} method.
     * <p>
     * Method takes two arguments, which should be a valid GLFW window pointer and a boolean indicating whether or not to install callbacks.
     *
     * @param windowId         pointer to the window
     * @param installCallbacks should window callbacks be installed
     * @return true if everything initialized
     */
    public boolean init(final long windowId, final boolean installCallbacks) {
        this.windowPtr = windowId;

        this.detectGlfwVersionAndEnabledFeatures();

        final ImGuiIO io = ImGui.getIO();

        io.addBackendFlags(ImGuiBackendFlags.HasMouseCursors | ImGuiBackendFlags.HasSetMousePos | ImGuiBackendFlags.PlatformHasViewports);
        io.setBackendPlatformName("imgui_java_impl_glfw");

        // Keyboard mapping. ImGui will use those indices to peek into the io.KeysDown[] array.
        final int[] keyMap = new int[ImGuiKey.COUNT];
        keyMap[ImGuiKey.Tab] = GLFW_KEY_TAB;
        keyMap[ImGuiKey.LeftArrow] = GLFW_KEY_LEFT;
        keyMap[ImGuiKey.RightArrow] = GLFW_KEY_RIGHT;
        keyMap[ImGuiKey.UpArrow] = GLFW_KEY_UP;
        keyMap[ImGuiKey.DownArrow] = GLFW_KEY_DOWN;
        keyMap[ImGuiKey.PageUp] = GLFW_KEY_PAGE_UP;
        keyMap[ImGuiKey.PageDown] = GLFW_KEY_PAGE_DOWN;
        keyMap[ImGuiKey.Home] = GLFW_KEY_HOME;
        keyMap[ImGuiKey.End] = GLFW_KEY_END;
        keyMap[ImGuiKey.Insert] = GLFW_KEY_INSERT;
        keyMap[ImGuiKey.Delete] = GLFW_KEY_DELETE;
        keyMap[ImGuiKey.Backspace] = GLFW_KEY_BACKSPACE;
        keyMap[ImGuiKey.Space] = GLFW_KEY_SPACE;
        keyMap[ImGuiKey.Enter] = GLFW_KEY_ENTER;
        keyMap[ImGuiKey.Escape] = GLFW_KEY_ESCAPE;
        keyMap[ImGuiKey.KeyPadEnter] = GLFW_KEY_KP_ENTER;
        keyMap[ImGuiKey.A] = GLFW_KEY_A;
        keyMap[ImGuiKey.C] = GLFW_KEY_C;
        keyMap[ImGuiKey.V] = GLFW_KEY_V;
        keyMap[ImGuiKey.X] = GLFW_KEY_X;
        keyMap[ImGuiKey.Y] = GLFW_KEY_Y;
        keyMap[ImGuiKey.Z] = GLFW_KEY_Z;

        io.setKeyMap(keyMap);

        io.setGetClipboardTextFn(new ImStrSupplier() {
            @Override
            public String get() {
                final String clipboardString = glfwGetClipboardString(windowId);
                return clipboardString != null ? clipboardString : "";
            }
        });

        io.setSetClipboardTextFn(new ImStrConsumer() {
            @Override
            public void accept(final String str) {
                glfwSetClipboardString(windowId, str);
            }
        });

        // Mouse cursors mapping. Disable errors whilst setting due to X11.
        final GLFWErrorCallback prevErrorCallback = glfwSetErrorCallback(null);
        this.mouseCursors[ImGuiMouseCursor.Arrow] = glfwCreateStandardCursor(GLFW_ARROW_CURSOR);
        this.mouseCursors[ImGuiMouseCursor.TextInput] = glfwCreateStandardCursor(GLFW_IBEAM_CURSOR);
        this.mouseCursors[ImGuiMouseCursor.ResizeAll] = glfwCreateStandardCursor(GLFW_RESIZABLE);
        this.mouseCursors[ImGuiMouseCursor.ResizeNS] = glfwCreateStandardCursor(GLFW_RESIZE_NS_CURSOR);
        this.mouseCursors[ImGuiMouseCursor.ResizeEW] = glfwCreateStandardCursor(GLFW_RESIZE_EW_CURSOR);
        this.mouseCursors[ImGuiMouseCursor.ResizeNESW] = glfwCreateStandardCursor(GLFW_RESIZE_NESW_CURSOR);
        this.mouseCursors[ImGuiMouseCursor.ResizeNWSE] = glfwCreateStandardCursor(GLFW_RESIZE_NWSE_CURSOR);
        this.mouseCursors[ImGuiMouseCursor.Hand] = glfwCreateStandardCursor(GLFW_HAND_CURSOR);
        this.mouseCursors[ImGuiMouseCursor.NotAllowed] = glfwCreateStandardCursor(GLFW_ARROW_CURSOR);
        glfwSetErrorCallback(prevErrorCallback);

        if (installCallbacks) {
            this.callbacksInstalled = true;
            this.prevUserCallbackWindowFocus = glfwSetWindowFocusCallback(windowId, this::windowFocusCallback);
            this.prevUserCallbackCursorEnter = glfwSetCursorEnterCallback(windowId, this::cursorEnterCallback);
            this.prevUserCallbackMouseButton = glfwSetMouseButtonCallback(windowId, this::mouseButtonCallback);
            this.prevUserCallbackScroll = glfwSetScrollCallback(windowId, this::scrollCallback);
            this.prevUserCallbackKey = glfwSetKeyCallback(windowId, this::keyCallback);
            this.prevUserCallbackChar = glfwSetCharCallback(windowId, this::charCallback);
        }
        // Update monitors the first time (note: monitor callback are broken in GLFW 3.2 and earlier, see github.com/glfw/glfw/issues/784)
        this.updateMonitors();
        this.prevUserCallbackMonitor = glfwSetMonitorCallback(this::monitorCallback);


        // Our mouse update function expect PlatformHandle to be filled for the main viewport
        final ImGuiViewport mainViewport = ImGui.getMainViewport();
        mainViewport.setPlatformHandle(this.windowPtr);

        if (IS_WINDOWS) {
            mainViewport.setPlatformHandleRaw(GLFWNativeWin32.glfwGetWin32Window(windowId));
        }

        if (io.hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
            this.initPlatformInterface();
        }

        return true;
    }

    /**
     * Updates {@link ImGuiIO} and {@link org.lwjgl.glfw.GLFW} state.
     */
    public void newFrame() {
        final ImGuiIO io = ImGui.getIO();

        glfwGetWindowSize(this.windowPtr, this.winWidth, this.winHeight);
        glfwGetFramebufferSize(this.windowPtr, this.fbWidth, this.fbHeight);

        io.setDisplaySize((float) this.winWidth[0], (float) this.winHeight[0]);
        if (this.winWidth[0] > 0 && this.winHeight[0] > 0) {
            final float scaleX = (float) this.fbWidth[0] / this.winWidth[0];
            final float scaleY = (float) this.fbHeight[0] / this.winHeight[0];
            io.setDisplayFramebufferScale(scaleX, scaleY);
        }
        if (this.wantUpdateMonitors) {
            this.updateMonitors();
        }

        final double currentTime = glfwGetTime();
        io.setDeltaTime(this.time > 0.0 ? (float) (currentTime - this.time) : 1.0f / 60.0f);
        this.time = currentTime;

        this.updateMousePosAndButtons();
        this.updateMouseCursor();
        this.updateGamepads();
    }

    /**
     * Method to restore {@link org.lwjgl.glfw.GLFW} to it's state prior to calling method {@link ImGuiImplGlfw#init(long, boolean)}.
     */
    public void free() {
        this.shutdownPlatformInterface();

        try {
            if (this.callbacksInstalled) {
                glfwSetWindowFocusCallback(this.windowPtr, this.prevUserCallbackWindowFocus).free();
                glfwSetCursorEnterCallback(this.windowPtr, this.prevUserCallbackCursorEnter).free();
                glfwSetMouseButtonCallback(this.windowPtr, this.prevUserCallbackMouseButton).free();
                glfwSetScrollCallback(this.windowPtr, this.prevUserCallbackScroll).free();
                glfwSetKeyCallback(this.windowPtr, this.prevUserCallbackKey).free();
                glfwSetCharCallback(this.windowPtr, this.prevUserCallbackChar).free();
                this.callbacksInstalled = false;
            }
            glfwSetMonitorCallback(this.prevUserCallbackMonitor).free();
        } catch (NullPointerException ignored) {
            // ignored
        }

        for (int i = 0; i < ImGuiMouseCursor.COUNT; i++) {
            if (this.mouseCursors[i] != 0L) {
                glfwDestroyCursor(this.mouseCursors[i]);
            }
        }
    }

    private void detectGlfwVersionAndEnabledFeatures() {
        final int[] major = new int[1];
        final int[] minor = new int[1];
        final int[] rev = new int[1];
        glfwGetVersion(major, minor, rev);

        final int version = major[0] * 1000 + minor[0] * 100 + rev[0] * 10;
        this.glfwHawWindowTopmost = version >= 3200;
        this.glfwHasWindowAlpha = version >= 3300;
        this.glfwHasPerMonitorDpi = version >= 3300;
        this.glfwHasFocusWindow = version >= 3200;
        this.glfwHasFocusOnShow = version >= 3300;
        this.glfwHasMonitorWorkArea = version >= 3300;
    }

    private void updateMousePosAndButtons() {
        final ImGuiIO io = ImGui.getIO();

        for (int i = 0; i < ImGuiMouseButton.COUNT; i++) {
            // If a mouse press event came, always pass it as "mouse held this frame", so we don't miss click-release events that are shorter than 1 frame.
            io.setMouseDown(i, this.mouseJustPressed[i] || glfwGetMouseButton(this.windowPtr, i) != 0);
            this.mouseJustPressed[i] = false;
        }

        io.getMousePos(this.mousePosBackup);
        io.setMousePos(-Float.MAX_VALUE, -Float.MAX_VALUE);
        io.setMouseHoveredViewport(0);

        final ImGuiPlatformIO platformIO = ImGui.getPlatformIO();

        for (int n = 0; n < platformIO.getViewportsSize(); n++) {
            final ImGuiViewport viewport = platformIO.getViewports(n);
            final long windowPtr = viewport.getPlatformHandle();

            final boolean focused = glfwGetWindowAttrib(windowPtr, GLFW_FOCUSED) != 0;

            final long mouseWindowPtr = (this.mouseWindowPtr == windowPtr || focused) ? windowPtr : 0;

            // Update mouse buttons
            if (focused) {
                for (int i = 0; i < ImGuiMouseButton.COUNT; i++) {
                    io.setMouseDown(i, glfwGetMouseButton(windowPtr, i) != 0);
                }
            }

            // Set OS mouse position from Dear ImGui if requested (rarely used, only when ImGuiConfigFlags_NavEnableSetMousePos is enabled by user)
            // (When multi-viewports are enabled, all Dear ImGui positions are same as OS positions)
            if (io.getWantSetMousePos() && focused) {
                glfwSetCursorPos(windowPtr, this.mousePosBackup.x - viewport.getPosX(), this.mousePosBackup.y - viewport.getPosY());
            }

            // Set Dear ImGui mouse position from OS position
            if (mouseWindowPtr != 0) {
                glfwGetCursorPos(mouseWindowPtr, this.mouseX, this.mouseY);

                if (io.hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
                    // Multi-viewport mode: mouse position in OS absolute coordinates (io.MousePos is (0,0) when the mouse is on the upper-left of the primary monitor)
                    glfwGetWindowPos(windowPtr, this.windowX, this.windowY);
                    io.setMousePos((float) this.mouseX[0] + this.windowX[0], (float) this.mouseY[0] + this.windowY[0]);
                } else {
                    // Single viewport mode: mouse position in client window coordinates (io.MousePos is (0,0) when the mouse is on the upper-left corner of the app window)
                    io.setMousePos((float) this.mouseX[0], (float) this.mouseY[0]);
                }
            }
        }
    }

    private void updateMouseCursor() {
        final ImGuiIO io = ImGui.getIO();

        final boolean noCursorChange = io.hasConfigFlags(ImGuiConfigFlags.NoMouseCursorChange);
        final boolean cursorDisabled = glfwGetInputMode(this.windowPtr, GLFW_CURSOR) == GLFW_CURSOR_DISABLED;

        if (noCursorChange || cursorDisabled) {
            return;
        }

        final int imguiCursor = ImGui.getMouseCursor();
        final ImGuiPlatformIO platformIO = ImGui.getPlatformIO();

        for (int n = 0; n < platformIO.getViewportsSize(); n++) {
            final long windowPtr = platformIO.getViewports(n).getPlatformHandle();

            if (imguiCursor == ImGuiMouseCursor.None || io.getMouseDrawCursor()) {
                // Hide OS mouse cursor if imgui is drawing it or if it wants no cursor
                glfwSetInputMode(windowPtr, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
            } else {
                // Show OS mouse cursor
                // FIXME-PLATFORM: Unfocused windows seems to fail changing the mouse cursor with GLFW 3.2, but 3.3 works here.
                glfwSetCursor(windowPtr, this.mouseCursors[imguiCursor] != 0 ? this.mouseCursors[imguiCursor] : this.mouseCursors[ImGuiMouseCursor.Arrow]);
                glfwSetInputMode(windowPtr, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
            }
        }
    }

    private void updateGamepads() {
        final ImGuiIO io = ImGui.getIO();

        if (!io.hasConfigFlags(ImGuiConfigFlags.NavEnableGamepad)) {
            return;
        }

        io.setNavInputs(this.emptyNavInputs);

        final ByteBuffer buttons = glfwGetJoystickButtons(GLFW_JOYSTICK_1);
        final int buttonsCount = buttons.limit();

        final FloatBuffer axis = glfwGetJoystickAxes(GLFW_JOYSTICK_1);
        final int axisCount = axis.limit();

        this.mapButton(ImGuiNavInput.Activate, 0, buttons, buttonsCount, io);   // Cross / A
        this.mapButton(ImGuiNavInput.Cancel, 1, buttons, buttonsCount, io);     // Circle / B
        this.mapButton(ImGuiNavInput.Menu, 2, buttons, buttonsCount, io);       // Square / X
        this.mapButton(ImGuiNavInput.Input, 3, buttons, buttonsCount, io);      // Triangle / Y
        this.mapButton(ImGuiNavInput.DpadLeft, 13, buttons, buttonsCount, io);  // D-Pad Left
        this.mapButton(ImGuiNavInput.DpadRight, 11, buttons, buttonsCount, io); // D-Pad Right
        this.mapButton(ImGuiNavInput.DpadUp, 10, buttons, buttonsCount, io);    // D-Pad Up
        this.mapButton(ImGuiNavInput.DpadDown, 12, buttons, buttonsCount, io);  // D-Pad Down
        this.mapButton(ImGuiNavInput.FocusPrev, 4, buttons, buttonsCount, io);  // L1 / LB
        this.mapButton(ImGuiNavInput.FocusNext, 5, buttons, buttonsCount, io);  // R1 / RB
        this.mapButton(ImGuiNavInput.TweakSlow, 4, buttons, buttonsCount, io);  // L1 / LB
        this.mapButton(ImGuiNavInput.TweakFast, 5, buttons, buttonsCount, io);  // R1 / RB
        this.mapAnalog(ImGuiNavInput.LStickLeft, 0, -0.3f, -0.9f, axis, axisCount, io);
        this.mapAnalog(ImGuiNavInput.LStickRight, 0, +0.3f, +0.9f, axis, axisCount, io);
        this.mapAnalog(ImGuiNavInput.LStickUp, 1, +0.3f, +0.9f, axis, axisCount, io);
        this.mapAnalog(ImGuiNavInput.LStickDown, 1, -0.3f, -0.9f, axis, axisCount, io);

        if (axisCount > 0 && buttonsCount > 0) {
            io.addBackendFlags(ImGuiBackendFlags.HasGamepad);
        } else {
            io.removeBackendFlags(ImGuiBackendFlags.HasGamepad);
        }
    }

    private void mapButton(final int navNo, final int buttonNo, final ByteBuffer buttons, final int buttonsCount, final ImGuiIO io) {
        if (buttonsCount > buttonNo && buttons.get(buttonNo) == GLFW_PRESS) {
            io.setNavInputs(navNo, 1.0f);
        }
    }

    private void mapAnalog(
            final int navNo,
            final int axisNo,
            final float v0,
            final float v1,
            final FloatBuffer axis,
            final int axisCount,
            final ImGuiIO io
    ) {
        float v = axisCount > axisNo ? axis.get(axisNo) : v0;
        v = (v - v0) / (v1 - v0);
        if (v > 1.0f) {
            v = 1.0f;
        }
        if (io.getNavInputs(navNo) < v) {
            io.setNavInputs(navNo, v);
        }
    }

    private void updateMonitors() {
        final ImGuiPlatformIO platformIO = ImGui.getPlatformIO();
        final PointerBuffer monitors = glfwGetMonitors();

        platformIO.resizeMonitors(0);

        for (int n = 0; n < monitors.limit(); n++) {
            final long monitor = monitors.get(n);

            glfwGetMonitorPos(monitor, this.monitorX, this.monitorY);
            final GLFWVidMode vidMode = glfwGetVideoMode(monitor);
            final float mainPosX = this.monitorX[0];
            final float mainPosY = this.monitorY[0];
            final float mainSizeX = vidMode.width();
            final float mainSizeY = vidMode.height();

            if (this.glfwHasMonitorWorkArea) {
                glfwGetMonitorWorkarea(monitor, this.monitorWorkAreaX, this.monitorWorkAreaY, this.monitorWorkAreaWidth, this.monitorWorkAreaHeight);
            }

            float workPosX = 0;
            float workPosY = 0;
            float workSizeX = 0;
            float workSizeY = 0;

            // Workaround a small GLFW issue reporting zero on monitor changes: https://github.com/glfw/glfw/pull/1761
            if (this.glfwHasMonitorWorkArea && this.monitorWorkAreaWidth[0] > 0 && this.monitorWorkAreaHeight[0] > 0) {
                workPosX = this.monitorWorkAreaX[0];
                workPosY = this.monitorWorkAreaY[0];
                workSizeX = this.monitorWorkAreaWidth[0];
                workSizeY = this.monitorWorkAreaHeight[0];
            }

            // Warning: the validity of monitor DPI information on Windows depends on the application DPI awareness settings,
            // which generally needs to be set in the manifest or at runtime.
            if (this.glfwHasPerMonitorDpi) {
                glfwGetMonitorContentScale(monitor, this.monitorContentScaleX, this.monitorContentScaleY);
            }
            final float dpiScale = this.monitorContentScaleX[0];

            platformIO.pushMonitors(mainPosX, mainPosY, mainSizeX, mainSizeY, workPosX, workPosY, workSizeX, workSizeY, dpiScale);
        }

        this.wantUpdateMonitors = false;
    }

    //--------------------------------------------------------------------------------------------------------
    // MULTI-VIEWPORT / PLATFORM INTERFACE SUPPORT
    // This is an _advanced_ and _optional_ feature, allowing the back-end to create and handle multiple viewports simultaneously.
    // If you are new to dear imgui or creating a new binding for dear imgui, it is recommended that you completely ignore this section first..
    //--------------------------------------------------------------------------------------------------------

    private void windowCloseCallback(final long windowId) {
        final ImGuiViewport vp = ImGui.findViewportByPlatformHandle(windowId);
        vp.setPlatformRequestClose(true);
    }

    // GLFW may dispatch window pos/size events after calling glfwSetWindowPos()/glfwSetWindowSize().
    // However: depending on the platform the callback may be invoked at different time:
    // - on Windows it appears to be called within the glfwSetWindowPos()/glfwSetWindowSize() call
    // - on Linux it is queued and invoked during glfwPollEvents()
    // Because the event doesn't always fire on glfwSetWindowXXX() we use a frame counter tag to only
    // ignore recent glfwSetWindowXXX() calls.
    private void windowPosCallback(final long windowId, final int xPos, final int yPos) {
        final ImGuiViewport vp = ImGui.findViewportByPlatformHandle(windowId);
        final ImGuiViewportDataGlfw data = (ImGuiViewportDataGlfw) vp.getPlatformUserData();
        final boolean ignoreEvent = (ImGui.getFrameCount() <= data.ignoreWindowPosEventFrame + 1);

        if (ignoreEvent) {
            return;
        }

        vp.setPlatformRequestMove(true);
    }

    private void windowSizeCallback(final long windowId, final int width, final int height) {
        final ImGuiViewport vp = ImGui.findViewportByPlatformHandle(windowId);
        final ImGuiViewportDataGlfw data = (ImGuiViewportDataGlfw) vp.getPlatformUserData();
        final boolean ignoreEvent = (ImGui.getFrameCount() <= data.ignoreWindowSizeEventFrame + 1);

        if (ignoreEvent) {
            return;
        }

        vp.setPlatformRequestResize(true);
    }

    private final class CreateWindowFunction extends ImPlatformFuncViewport {
        @Override
        public void accept(final ImGuiViewport vp) {
            final ImGuiViewportDataGlfw data = new ImGuiViewportDataGlfw();

            vp.setPlatformUserData(data);

            // GLFW 3.2 unfortunately always set focus on glfwCreateWindow() if GLFW_VISIBLE is set, regardless of GLFW_FOCUSED
            // With GLFW 3.3, the hint GLFW_FOCUS_ON_SHOW fixes this problem
            glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
            glfwWindowHint(GLFW_FOCUSED, GLFW_FALSE);
            if (VeilImGuiImplGlfw.this.glfwHasFocusOnShow) {
                glfwWindowHint(GLFW_FOCUS_ON_SHOW, GLFW_FALSE);
            }
            glfwWindowHint(GLFW_DECORATED, vp.hasFlags(ImGuiViewportFlags.NoDecoration) ? GLFW_FALSE : GLFW_TRUE);
            if (VeilImGuiImplGlfw.this.glfwHawWindowTopmost) {
                glfwWindowHint(GLFW_FLOATING, vp.hasFlags(ImGuiViewportFlags.TopMost) ? GLFW_TRUE : GLFW_FALSE);
            }

            data.window = glfwCreateWindow((int) vp.getSizeX(), (int) vp.getSizeY(), "No Title Yet", NULL, VeilImGuiImplGlfw.this.windowPtr);
            data.windowOwned = true;

            vp.setPlatformHandle(data.window);

            if (IS_WINDOWS) {
                vp.setPlatformHandleRaw(GLFWNativeWin32.glfwGetWin32Window(data.window));
            }

            glfwSetWindowPos(data.window, (int) vp.getPosX(), (int) vp.getPosY());

            // Install GLFW callbacks for secondary viewports
            glfwSetMouseButtonCallback(data.window, VeilImGuiImplGlfw.this::mouseButtonCallback);
            glfwSetScrollCallback(data.window, VeilImGuiImplGlfw.this::scrollCallback);
            glfwSetKeyCallback(data.window, VeilImGuiImplGlfw.this::keyCallback);
            glfwSetCharCallback(data.window, VeilImGuiImplGlfw.this::charCallback);
            glfwSetWindowCloseCallback(data.window, VeilImGuiImplGlfw.this::windowCloseCallback);
            glfwSetWindowPosCallback(data.window, VeilImGuiImplGlfw.this::windowPosCallback);
            glfwSetWindowSizeCallback(data.window, VeilImGuiImplGlfw.this::windowSizeCallback);

            glfwMakeContextCurrent(data.window);
            glfwSwapInterval(0);
        }
    }

    private final class DestroyWindowFunction extends ImPlatformFuncViewport {
        @Override
        public void accept(final ImGuiViewport vp) {
            final ImGuiViewportDataGlfw data = (ImGuiViewportDataGlfw) vp.getPlatformUserData();

            if (data != null && data.windowOwned) {
                // Release any keys that were pressed in the window being destroyed and are still held down,
                // because we will not receive any release events after window is destroyed.
                for (int i = 0; i < VeilImGuiImplGlfw.this.keyOwnerWindows.length; i++) {
                    if (VeilImGuiImplGlfw.this.keyOwnerWindows[i] == data.window) {
                        VeilImGuiImplGlfw.this.keyCallback(data.window, i, 0, GLFW_RELEASE, 0); // Later params are only used for main viewport, on which this function is never called.
                    }
                }

                Callbacks.glfwFreeCallbacks(data.window);
                glfwDestroyWindow(data.window);
            }

            vp.setPlatformUserData(null);
            vp.setPlatformHandle(0);
        }
    }

    private static final class ShowWindowFunction extends ImPlatformFuncViewport {
        @Override
        public void accept(final ImGuiViewport vp) {
            final ImGuiViewportDataGlfw data = (ImGuiViewportDataGlfw) vp.getPlatformUserData();

            if (IS_WINDOWS && vp.hasFlags(ImGuiViewportFlags.NoTaskBarIcon)) {
                ImGuiImplGlfwNative.win32hideFromTaskBar(vp.getPlatformHandleRaw());
            }

            glfwShowWindow(data.window);
        }
    }

    private static final class GetWindowPosFunction extends ImPlatformFuncViewportSuppImVec2 {
        private final int[] posX = new int[1];
        private final int[] posY = new int[1];

        @Override
        public void get(final ImGuiViewport vp, final ImVec2 dstImVec2) {
            final ImGuiViewportDataGlfw data = (ImGuiViewportDataGlfw) vp.getPlatformUserData();
            glfwGetWindowPos(data.window, this.posX, this.posY);
            dstImVec2.x = this.posX[0];
            dstImVec2.y = this.posY[0];
        }
    }

    private static final class SetWindowPosFunction extends ImPlatformFuncViewportImVec2 {
        @Override
        public void accept(final ImGuiViewport vp, final ImVec2 imVec2) {
            final ImGuiViewportDataGlfw data = (ImGuiViewportDataGlfw) vp.getPlatformUserData();
            data.ignoreWindowPosEventFrame = ImGui.getFrameCount();
            glfwSetWindowPos(data.window, (int) imVec2.x, (int) imVec2.y);
        }
    }

    private static final class GetWindowSizeFunction extends ImPlatformFuncViewportSuppImVec2 {
        private final int[] width = new int[1];
        private final int[] height = new int[1];

        @Override
        public void get(final ImGuiViewport vp, final ImVec2 dstImVec2) {
            final ImGuiViewportDataGlfw data = (ImGuiViewportDataGlfw) vp.getPlatformUserData();
            glfwGetWindowSize(data.window, this.width, this.height);
            dstImVec2.x = this.width[0];
            dstImVec2.y = this.height[0];
        }
    }

    private final class SetWindowSizeFunction extends ImPlatformFuncViewportImVec2 {
        private final int[] x = new int[1];
        private final int[] y = new int[1];
        private final int[] width = new int[1];
        private final int[] height = new int[1];

        @Override
        public void accept(final ImGuiViewport vp, final ImVec2 imVec2) {
            final ImGuiViewportDataGlfw data = (ImGuiViewportDataGlfw) vp.getPlatformUserData();
            // Native OS windows are positioned from the bottom-left corner on macOS, whereas on other platforms they are
            // positioned from the upper-left corner. GLFW makes an effort to convert macOS style coordinates, however it
            // doesn't handle it when changing size. We are manually moving the window in order for changes of size to be based
            // on the upper-left corner.
            if (Minecraft.ON_OSX && !VeilImGuiImplGlfw.this.glfwHasOsxWindowPosFix) {
                glfwGetWindowPos(data.window, this.x, this.y);
                glfwGetWindowSize(data.window, this.width, this.height);
                glfwSetWindowPos(data.window, this.x[0], this.y[0] - this.height[0] + (int) imVec2.y);
            }
            data.ignoreWindowSizeEventFrame = ImGui.getFrameCount();
            glfwSetWindowSize(data.window, (int) imVec2.x, (int) imVec2.y);
        }
    }

    private static final class SetWindowTitleFunction extends ImPlatformFuncViewportString {
        @Override
        public void accept(final ImGuiViewport vp, final String str) {
            final ImGuiViewportDataGlfw data = (ImGuiViewportDataGlfw) vp.getPlatformUserData();
            glfwSetWindowTitle(data.window, str);
        }
    }

    private final class SetWindowFocusFunction extends ImPlatformFuncViewport {
        @Override
        public void accept(final ImGuiViewport vp) {
            if (VeilImGuiImplGlfw.this.glfwHasFocusWindow) {
                final ImGuiViewportDataGlfw data = (ImGuiViewportDataGlfw) vp.getPlatformUserData();
                glfwFocusWindow(data.window);
            }
        }
    }

    private static final class GetWindowFocusFunction extends ImPlatformFuncViewportSuppBoolean {
        @Override
        public boolean get(final ImGuiViewport vp) {
            final ImGuiViewportDataGlfw data = (ImGuiViewportDataGlfw) vp.getPlatformUserData();
            return glfwGetWindowAttrib(data.window, GLFW_FOCUSED) != 0;
        }
    }

    private static final class GetWindowMinimizedFunction extends ImPlatformFuncViewportSuppBoolean {
        @Override
        public boolean get(final ImGuiViewport vp) {
            final ImGuiViewportDataGlfw data = (ImGuiViewportDataGlfw) vp.getPlatformUserData();
            return glfwGetWindowAttrib(data.window, GLFW_ICONIFIED) != 0;
        }
    }

    private final class SetWindowAlphaFunction extends ImPlatformFuncViewportFloat {
        @Override
        public void accept(final ImGuiViewport vp, final float f) {
            if (VeilImGuiImplGlfw.this.glfwHasWindowAlpha) {
                final ImGuiViewportDataGlfw data = (ImGuiViewportDataGlfw) vp.getPlatformUserData();
                glfwSetWindowOpacity(data.window, f);
            }
        }
    }

    private static final class RenderWindowFunction extends ImPlatformFuncViewport {
        @Override
        public void accept(final ImGuiViewport vp) {
            final ImGuiViewportDataGlfw data = (ImGuiViewportDataGlfw) vp.getPlatformUserData();
            glfwMakeContextCurrent(data.window);
        }
    }

    private static final class SwapBuffersFunction extends ImPlatformFuncViewport {
        @Override
        public void accept(final ImGuiViewport vp) {
            final ImGuiViewportDataGlfw data = (ImGuiViewportDataGlfw) vp.getPlatformUserData();
            glfwMakeContextCurrent(data.window);
            glfwSwapBuffers(data.window);
        }
    }

    private void initPlatformInterface() {
        final ImGuiPlatformIO platformIO = ImGui.getPlatformIO();

        // Register platform interface (will be coupled with a renderer interface)
        platformIO.setPlatformCreateWindow(new VeilImGuiImplGlfw.CreateWindowFunction());
        platformIO.setPlatformDestroyWindow(new VeilImGuiImplGlfw.DestroyWindowFunction());
        platformIO.setPlatformShowWindow(new VeilImGuiImplGlfw.ShowWindowFunction());
        platformIO.setPlatformGetWindowPos(new VeilImGuiImplGlfw.GetWindowPosFunction());
        platformIO.setPlatformSetWindowPos(new VeilImGuiImplGlfw.SetWindowPosFunction());
        platformIO.setPlatformGetWindowSize(new VeilImGuiImplGlfw.GetWindowSizeFunction());
        platformIO.setPlatformSetWindowSize(new VeilImGuiImplGlfw.SetWindowSizeFunction());
        platformIO.setPlatformSetWindowTitle(new VeilImGuiImplGlfw.SetWindowTitleFunction());
        platformIO.setPlatformSetWindowFocus(new VeilImGuiImplGlfw.SetWindowFocusFunction());
        platformIO.setPlatformGetWindowFocus(new VeilImGuiImplGlfw.GetWindowFocusFunction());
        platformIO.setPlatformGetWindowMinimized(new VeilImGuiImplGlfw.GetWindowMinimizedFunction());
        platformIO.setPlatformSetWindowAlpha(new VeilImGuiImplGlfw.SetWindowAlphaFunction());
        platformIO.setPlatformRenderWindow(new VeilImGuiImplGlfw.RenderWindowFunction());
        platformIO.setPlatformSwapBuffers(new VeilImGuiImplGlfw.SwapBuffersFunction());

        // Register main window handle (which is owned by the main application, not by us)
        // This is mostly for simplicity and consistency, so that our code (e.g. mouse handling etc.) can use same logic for main and secondary viewports.
        final ImGuiViewport mainViewport = ImGui.getMainViewport();
        final ImGuiViewportDataGlfw data = new ImGuiViewportDataGlfw();
        data.window = this.windowPtr;
        data.windowOwned = false;
        mainViewport.setPlatformUserData(data);
    }

    private void shutdownPlatformInterface() {
    }

    private static final class ImGuiViewportDataGlfw {
        long window;
        boolean windowOwned = false;
        int ignoreWindowPosEventFrame = -1;
        int ignoreWindowSizeEventFrame = -1;
    }
}
