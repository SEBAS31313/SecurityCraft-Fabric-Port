package net.geforcemods.securitycraft.screen.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/** Scrollable, clipped native GUI panel shared by the SecurityCraft screens. */
public abstract class ScrollPanel implements Renderable, GuiEventListener, NarratableEntry {
	protected final Minecraft client;
	protected final int width;
	protected final int height;
	protected final int top;
	protected final int bottom;
	protected final int left;
	protected final int right;
	protected final int border;
	protected final int barWidth;
	protected double scrollDistance;
	private final int barBackground;
	private final int barColor;
	private final int barHighlight;
	private boolean draggingScrollbar;
	private boolean focused;

	public ScrollPanel(Minecraft client, int width, int height, int top, int left) {
		this(client, width, height, top, left, 4, 6, 0xFF000000, 0xFF808080, 0xFFC0C0C0);
	}

	public ScrollPanel(Minecraft client, int width, int height, int top, int left, int border, int barWidth) {
		this(client, width, height, top, left, border, barWidth, 0xFF000000, 0xFF808080, 0xFFC0C0C0);
	}

	public ScrollPanel(Minecraft client, int width, int height, int top, int left, int border, int barWidth,
			int barBackground, int barColor, int barHighlight) {
		this.client = client;
		this.width = width;
		this.height = height;
		this.top = top;
		this.bottom = top + height;
		this.left = left;
		this.right = left + width;
		this.border = border;
		this.barWidth = barWidth;
		this.barBackground = barBackground;
		this.barColor = barColor;
		this.barHighlight = barHighlight;
	}

	protected abstract int getContentHeight();
	protected abstract void drawPanel(GuiGraphicsExtractor graphics, int entryRight, int relativeY, int mouseX, int mouseY);

	protected void drawBackground(GuiGraphicsExtractor graphics, float partialTick) {
		graphics.fill(left, top, right, bottom, 0x80000000);
	}

	protected boolean clickPanel(double mouseX, double mouseY, MouseButtonEvent event) {
		return false;
	}

	private int maximumScroll() {
		return Math.max(0, getContentHeight() + border * 2 - height);
	}

	private int thumbHeight() {
		return Mth.clamp((height * height) / Math.max(1, getContentHeight() + border * 2), Math.min(16, height), height);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		scrollDistance = Mth.clamp(scrollDistance, 0.0D, maximumScroll());
		drawBackground(graphics, partialTick);
		graphics.enableScissor(left, top, right, bottom);
		drawPanel(graphics, right - barWidth, top + border - (int) scrollDistance, mouseX, mouseY);
		graphics.disableScissor();
		if (maximumScroll() > 0) {
			int thumbHeight = thumbHeight();
			int thumbTop = top + (int) (scrollDistance * (height - thumbHeight) / maximumScroll());
			graphics.fill(right - barWidth, top, right, bottom, barBackground);
			graphics.fill(right - barWidth, thumbTop, right, thumbTop + thumbHeight, barColor);
			graphics.fill(right - barWidth, thumbTop, right - 1, thumbTop + thumbHeight - 1, barHighlight);
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (event.button() != 0 || !isMouseOver(event.x(), event.y()))
			return false;
		if (event.x() >= right - barWidth && maximumScroll() > 0) {
			draggingScrollbar = true;
			moveScrollbar(event.y());
			return true;
		}
		return clickPanel(event.x() - left, event.y() - top + scrollDistance - border, event);
	}

	private void moveScrollbar(double mouseY) {
		scrollDistance = Mth.clamp((mouseY - top - thumbHeight() / 2.0D) * maximumScroll() / Math.max(1, height - thumbHeight()), 0.0D, maximumScroll());
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		if (!draggingScrollbar)
			return false;
		moveScrollbar(event.y());
		return true;
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		boolean wasDragging = draggingScrollbar;
		draggingScrollbar = false;
		return wasDragging;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
		if (!isMouseOver(mouseX, mouseY))
			return false;
		scrollDistance = Mth.clamp(scrollDistance - vertical * getScrollAmount(), 0.0D, maximumScroll());
		return true;
	}

	protected double getScrollAmount() {
		return 12.0D;
	}

	protected void drawGradientRect(GuiGraphicsExtractor graphics, int left, int top, int right, int bottom, int startColor, int endColor) {
		graphics.fillGradient(left, top, right, bottom, startColor, endColor);
	}

	@Override public boolean isMouseOver(double mouseX, double mouseY) { return mouseX >= left && mouseX < right && mouseY >= top && mouseY < bottom; }
	@Override public void setFocused(boolean focused) { this.focused = focused; }
	@Override public boolean isFocused() { return focused; }
	@Override public ScreenRectangle getRectangle() { return new ScreenRectangle(left, top, width, height); }
	@Override public NarrationPriority narrationPriority() { return focused ? NarrationPriority.FOCUSED : NarrationPriority.NONE; }
	@Override public void updateNarration(NarrationElementOutput output) { output.add(net.minecraft.client.gui.narration.NarratedElementType.TITLE, Component.translatable("narration.scrollbar.position", (int) scrollDistance, maximumScroll())); }
}
