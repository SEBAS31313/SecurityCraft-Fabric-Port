package net.geforcemods.securitycraft.screen.components;

import java.util.Locale;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/** A native Minecraft slider with numeric ranges and the original SC screen contract. */
public class ExtendedSlider extends AbstractSliderButton {
	protected double minValue;
	protected double maxValue;
	private final Component prefix;
	private final Component suffix;
	private final double stepSize;
	private final int precision;
	private final boolean drawString;
	private int foreground = 0xE0E0E0;

	public ExtendedSlider(int x, int y, int width, int height, Component prefix, Component suffix,
			double minValue, double maxValue, double currentValue, boolean drawString) {
		this(x, y, width, height, prefix, suffix, minValue, maxValue, currentValue, 1.0D, 0, drawString);
	}

	public ExtendedSlider(int x, int y, int width, int height, Component prefix, Component suffix,
			double minValue, double maxValue, double currentValue, double stepSize, int precision, boolean drawString) {
		super(x, y, width, height, Component.empty(), maxValue == minValue ? 0.0D : Mth.clamp((currentValue - minValue) / (maxValue - minValue), 0.0D, 1.0D));
		this.prefix = prefix;
		this.suffix = suffix;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.stepSize = stepSize;
		this.precision = Math.max(0, precision);
		this.drawString = drawString;
		updateMessage();
	}

	public double getValue() {
		double actual = minValue + value * (maxValue - minValue);
		return stepSize > 0.0D ? Mth.clamp(minValue + Math.round((actual - minValue) / stepSize) * stepSize, minValue, maxValue) : actual;
	}

	public int getValueInt() {
		return (int) Math.round(getValue());
	}

	@Override
	public void setValue(double actual) {
		super.setValue(maxValue == minValue ? 0.0D : Mth.clamp((actual - minValue) / (maxValue - minValue), 0.0D, 1.0D));
	}

	private void updateFromMouse(MouseButtonEvent event) {
		super.setValue(Mth.clamp((event.x() - getX() - 4.0D) / Math.max(1, getWidth() - 8), 0.0D, 1.0D));
	}

	@Override
	public void onClick(MouseButtonEvent event, boolean doubleClick) {
		updateFromMouse(event);
	}

	@Override
	protected void onDrag(MouseButtonEvent event, double dragX, double dragY) {
		updateFromMouse(event);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (!active)
			return false;
		if (event.key() == GLFW.GLFW_KEY_LEFT || event.key() == GLFW.GLFW_KEY_RIGHT) {
			setValue(getValue() + (event.key() == GLFW.GLFW_KEY_LEFT ? -1.0D : 1.0D) * (stepSize > 0.0D ? stepSize : (maxValue - minValue) / 100.0D));
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	protected void updateMessage() {
		setMessage(drawString ? Component.empty().append(prefix).append(String.format(Locale.ROOT, "%." + precision + "f", getValue()).trim()).append(suffix) : Component.empty());
	}

	@Override
	protected void applyValue() {
		updateMessage();
	}

	public ExtendedSlider setFGColor(int color) {
		foreground = color;
		return this;
	}

	@Override
	protected void extractScrollingStringOverContents(ActiveTextCollector text, Component message, int color) {
		super.extractScrollingStringOverContents(text, message, foreground);
	}
}
