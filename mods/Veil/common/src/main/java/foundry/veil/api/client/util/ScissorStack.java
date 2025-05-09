package foundry.veil.api.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * A utility class to manage scissor clipping regions.
 * This allows for restricting rendering to specific rectangular areas.
 *
 * @author amo, Ocelot
 */
public final class ScissorStack {

    private final Deque<ScissorRegion> regions;

    public ScissorStack() {
        this.regions = new ArrayDeque<>();
    }

    /**
     * Pushes a new scissor clipping region onto the stack.
     * The region is automatically constrained by any existing regions on the stack.
     *
     * @param x      The x-coordinate of the top-left corner
     * @param y      The y-coordinate of the top-left corner
     * @param width  The width of the region
     * @param height The height of the region
     */
    public void push(int x, int y, int width, int height) {
        if (!this.regions.isEmpty()) {
            ScissorRegion parent = this.regions.peek();
            int x2 = x + width;
            x = Mth.clamp(x, parent.x, parent.x + parent.width);
            width = Mth.clamp(x2, parent.x, parent.x + parent.width) - x;
            int y2 = y + height;
            y = Mth.clamp(y, parent.y, parent.y + parent.height);
            height = Mth.clamp(y2, parent.y, parent.y + parent.height) - y;
        }

        ScissorRegion region = new ScissorRegion(x, y, width, height);
        this.regions.push(region);
        region.apply();
    }

    /**
     * Removes the top scissor clipping region from the stack.
     * If there are any regions remaining, the previous region is reapplied.
     */
    public void pop() {
        this.regions.pop();
        if (this.regions.isEmpty()) {
            RenderSystem.disableScissor();
        } else {
            this.regions.peek().apply();
        }
    }

    /**
     * @return The number of regions in the stack
     * @since 1.3.0
     */
    public int size() {
        return this.regions.size();
    }

    /**
     * @return Whether all regions have been popped
     * @since 1.3.0
     */
    public boolean isEmpty() {
        return this.regions.isEmpty();
    }

    /**
     * Clears all scissored regions and disables scissoring.
     *
     * @since 1.3.0
     */
    public void clear() {
        this.regions.clear();
        RenderSystem.disableScissor();
    }

    /**
     * Represents a single scissored clipping region.
     */
    @ApiStatus.Internal
    private record ScissorRegion(int x, int y, int width, int height) {

        /**
         * Applies this scissored region to the rendering system.
         */
        void apply() {
            double scale = Minecraft.getInstance().getWindow().getGuiScale();
            int screenY = (int) ((Minecraft.getInstance().getWindow().getHeight() - (this.y + this.height)) * scale);
            RenderSystem.enableScissor((int) (this.x * scale), screenY, (int) (this.width * scale), (int) (this.height * scale));
        }
    }
}