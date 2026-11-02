package net.geforcemods.securitycraft.screen.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/** Preserves the original screen and its unsaved widget state while opening a modal. */
public interface ScreenLayers {
	void securitycraft$pushLayer(Screen screen);
	void securitycraft$popLayer();
	boolean securitycraft$hasLayers();

	static void push(Screen screen) {
		((ScreenLayers) Minecraft.getInstance().gui).securitycraft$pushLayer(screen);
	}

	static void pop() {
		((ScreenLayers) Minecraft.getInstance().gui).securitycraft$popLayer();
	}
}
