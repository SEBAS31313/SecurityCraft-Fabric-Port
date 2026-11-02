package net.geforcemods.securitycraft.screen.components;

import net.minecraft.client.gui.components.AbstractWidget;

/** Applies semantic option colors through Minecraft's native text styling. */
public final class WidgetColors {
	private WidgetColors() {}
	public static void set(AbstractWidget widget, int color) {
		widget.setMessage(widget.getMessage().copy().withStyle(style -> style.withColor(color)));
	}
	public static int get(AbstractWidget widget) {
		var color = widget.getMessage().getStyle().getColor();
		return color == null ? 0xFFE0E0E0 : 0xFF000000 | color.getValue();
	}
}
