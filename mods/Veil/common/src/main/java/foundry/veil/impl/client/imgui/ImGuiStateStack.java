package foundry.veil.impl.client.imgui;

import foundry.veil.Veil;
import imgui.ImGui;
import imgui.extension.implot.ImPlot;
import imgui.extension.implot.ImPlotContext;
import imgui.internal.ImGuiContext;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

@ApiStatus.Internal
public class ImGuiStateStack {

    private static final List<State> STATE_STACK = new ArrayList<>(1);

    public static void push() {
        long imGuiContext = ImGui.getCurrentContext().ptr;
        long imPlotContext = ImPlot.getCurrentContext().ptr;
        STATE_STACK.add(new State(imGuiContext, imPlotContext));
    }

    public static void pop() {
        if (STATE_STACK.isEmpty()) {
            return;
        }

        State state = STATE_STACK.removeLast();
        ImGui.setCurrentContext(new ImGuiContext(state.imGuiContext));
        ImPlot.setCurrentContext(new ImPlotContext(state.imPlotContext));
    }

    public static void forcePop() {
        if (STATE_STACK.size() > 1) {
            Veil.LOGGER.error("Mismatched begin/end during frame");
        }
        while (STATE_STACK.size() > 1) {
            STATE_STACK.removeLast();
        }
        pop();
    }

    private record State(long imGuiContext, long imPlotContext) {
    }
}
