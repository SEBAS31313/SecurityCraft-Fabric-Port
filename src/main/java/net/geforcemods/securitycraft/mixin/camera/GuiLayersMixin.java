package net.geforcemods.securitycraft.mixin.camera;

import java.util.ArrayDeque;
import java.util.Deque;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.geforcemods.securitycraft.screen.components.ScreenLayers;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

/** Native modal screen stack, retaining the parent's layout and contents when a modal closes. */
@Mixin(Gui.class)
public abstract class GuiLayersMixin implements ScreenLayers {
	@Shadow @Final private Minecraft minecraft;
	@Shadow private Screen screen;
	@Unique private final Deque<Screen> securitycraft$layers = new ArrayDeque<>();

	@Override
	public boolean securitycraft$hasLayers() {
		return !securitycraft$layers.isEmpty();
	}

	@Override
	public void securitycraft$pushLayer(Screen next) {
		if (screen == null) {
			minecraft.gui.setScreen(next);
			return;
		}
		securitycraft$layers.addLast(screen);
		screen = next;
		next.added();
		KeyMapping.releaseAll();
		next.init(minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());
		minecraft.updateTitle();
	}

	@Override
	public void securitycraft$popLayer() {
		if (securitycraft$layers.isEmpty()) {
			minecraft.gui.setScreen(null);
			return;
		}
		screen.removed();
		screen = securitycraft$layers.removeLast();
		int width = minecraft.getWindow().getGuiScaledWidth();
		int height = minecraft.getWindow().getGuiScaledHeight();
		if (screen.width != width || screen.height != height)
			screen.resize(width, height);
		minecraft.updateTitle();
	}

	@Inject(method = "setScreen", at = @At("HEAD"))
	private void securitycraft$clearOldLayers(Screen next, CallbackInfo ci) {
		while (!securitycraft$layers.isEmpty())
			securitycraft$layers.removeLast().removed();
	}

	@WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;extractRenderStateWithTooltipAndSubtitles(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V"))
	private void securitycraft$drawScreenLayers(Screen current, GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, Operation<Void> original) {
		for (Screen parent : securitycraft$layers) {
			parent.extractRenderStateWithTooltipAndSubtitles(graphics, -1, -1, partialTick);
			graphics.nextStratum();
		}
		original.call(current, graphics, mouseX, mouseY, partialTick);
	}
}
