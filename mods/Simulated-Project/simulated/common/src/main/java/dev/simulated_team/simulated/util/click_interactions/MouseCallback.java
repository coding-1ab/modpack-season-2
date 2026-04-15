package dev.simulated_team.simulated.util.click_interactions;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * An interface denoting this class as a client-side MouseCallback. <br/>
 * Exposes Left, right, middle-clicking, and client-side ticking, along with mouse scrolling. <br/>
 * Clicking and scrolling are both cancelable utilizing {@link MouseInputResult input results}
 */
public interface MouseCallback {

    /**
     * Filters the given button and calls the appropriate method inside the given MouseCallback interface.
     *
     * @param clickInteraction The interface to call methods from.
     * @param button The mouse button being clicked.
     * @param modifiers Modifiers held down during this interaction.
     * @param action The type of action this interaction is.
     * @param associatedMappings Commonly used mouse mappings
     * @return Whether this interaction should be canceled, or handled by vanilla logic
     */
    @NotNull
    static MouseCallback.MouseInputResult filterClick(final MouseCallback clickInteraction, final int button, final int modifiers, final int action, final MouseMappings associatedMappings) {
        if (associatedMappings.attack.matchesMouse(button)) {
            return clickInteraction.onLeftClick(modifiers, action, associatedMappings.attack);
        }

        if (associatedMappings.middle.matchesMouse(button)) {
            return clickInteraction.onMiddleClick(modifiers, action, associatedMappings.middle);
        }

        if (associatedMappings.use.matchesMouse(button)) {
            return clickInteraction.onRightClick(modifiers, action, associatedMappings.use);
        }

        return MouseInputResult.empty();
    }

    default MouseInputResult onMiddleClick(final int modifiers, final int action, final KeyMapping middleKey) {
        return MouseInputResult.empty();
    }

    default MouseInputResult onLeftClick(final int modifiers, final int action, final KeyMapping leftKey) {
        return MouseInputResult.empty();
    }

    default MouseInputResult onRightClick(final int modifiers, final int action, final KeyMapping rightKey) {
        return MouseInputResult.empty();
    }

    default MouseInputResult onScroll(final double deltaX, final double deltaY) {
        return MouseInputResult.empty();
    }

    default MouseInputResult onMouseMove(final double yaw, final double pitch) {
        return MouseInputResult.empty();
    }

    /**
     * The general client tick. Called at the end of the level's ticking event.
     */
    default void clientTick(final Level level, final LocalPlayer player) {
    }

    /**
     * Commonly used mouse mappings.
     */
    record MouseMappings(KeyMapping use, KeyMapping attack, KeyMapping middle) {
        private static final MouseMappings MAPPINGS = populateMappings();

        public static MouseMappings getMappings() {
            return MAPPINGS;
        }

        private static MouseMappings populateMappings() {
            final Options options = Minecraft.getInstance().options;
            return new MouseMappings(options.keyUse, options.keyAttack, options.keyPickItem);
        }
    }

    /**
     * Determines whether this interaction should be canceled, or handled by vanilla logic.
     */
    record MouseInputResult(boolean cancelled) {
        private static final MouseInputResult EMPTY = new MouseInputResult(false);
        public static MouseInputResult empty() {
            return EMPTY;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj.getClass() != this.getClass()) {
                return false;
            }

            final MouseInputResult otherEvent = (MouseInputResult) obj;
            return otherEvent.cancelled == this.cancelled;
        }
    }
}
