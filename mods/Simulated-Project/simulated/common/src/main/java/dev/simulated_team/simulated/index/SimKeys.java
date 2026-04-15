package dev.simulated_team.simulated.index;

import com.mojang.blaze3d.platform.InputConstants;
import dev.simulated_team.simulated.Simulated;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public enum SimKeys {
	ROTATE_MODE("rotate_mode", GLFW.GLFW_KEY_TAB, "Physics Staff Rotate Mode"),;

	private KeyMapping keybind;
	private final String description;
	private final String translation;
	private final int key;
	private final boolean modifiable;

	SimKeys(final int defaultKey) {
		this("", defaultKey, "");
	}

	SimKeys(final String description, final int defaultKey, final String translation) {
		this.description = Simulated.MOD_ID + ".keyinfo." + description;
		this.key = defaultKey;
		this.modifiable = !description.isEmpty();
		this.translation = translation;
	}

	public static void provideLang(final BiConsumer<String, String> consumer) {
		for (final SimKeys key : values())
			if (key.modifiable)
				consumer.accept(key.description, key.translation);
	}

	public static void registerTo(final Consumer<KeyMapping> consumer) {
		for (final SimKeys key : values()) {
			key.keybind = new KeyMapping(key.description, key.key, Simulated.MOD_NAME);
			if (!key.modifiable)
				continue;

			consumer.accept(key.keybind);
		}
	}

	public KeyMapping getKeybind() {
		return this.keybind;
	}

	public boolean isPressed() {
		if (!this.modifiable)
			return isKeyDown(this.key);
		return this.keybind.isDown();
	}

	public String getBoundKey() {
		return this.keybind.getTranslatedKeyMessage()
			.getString()
			.toUpperCase();
	}

	public static boolean isKeyDown(final int key) {
		return InputConstants.isKeyDown(Minecraft.getInstance()
			.getWindow()
			.getWindow(), key);
	}

	public static boolean isMouseButtonDown(final int button) {
		return GLFW.glfwGetMouseButton(Minecraft.getInstance()
			.getWindow()
			.getWindow(), button) == 1;
	}

	public static boolean ctrlDown() {
		return Screen.hasControlDown();
	}

	public static boolean shiftDown() {
		return Screen.hasShiftDown();
	}

	public static boolean altDown() {
		return Screen.hasAltDown();
	}

}
