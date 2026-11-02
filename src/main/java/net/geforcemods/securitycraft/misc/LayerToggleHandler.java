package net.geforcemods.securitycraft.misc;

import java.util.ArrayList;
import java.util.List;

import net.geforcemods.securitycraft.SecurityCraft;
import net.minecraft.resources.Identifier;

public class LayerToggleHandler {
	private static final List<Identifier> DISABLED_LAYERS = new ArrayList<>();

	private LayerToggleHandler() {}

	public static void register() {
		for (Identifier layer : List.of(net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements.INFO_BAR,
				net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements.MOB_EFFECTS,
				net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements.EXPERIENCE_LEVEL)) {
			net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry.replaceElement(layer, original -> (graphics, delta) -> {
				if (!isDisabled(layer))
					original.extractRenderState(graphics, delta);
			});
		}
	}

	public static void reset() {
		DISABLED_LAYERS.clear();
		disable(net.geforcemods.securitycraft.ClientHandler.CAMERA_LAYER);
	}

	public static boolean isDisabled(Identifier layer) {
		return DISABLED_LAYERS.contains(layer);
	}

	public static void enable(Identifier layer) {
		DISABLED_LAYERS.remove(layer);
	}

	public static void disable(Identifier layer) {
		if (!isDisabled(layer))
			DISABLED_LAYERS.add(layer);
	}
}
