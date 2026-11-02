package net.geforcemods.securitycraft.mixin.camera;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.geforcemods.securitycraft.screen.components.ScreenLayers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/** Escape closes the active modal and restores its parent, retaining the parent's widget state. */
@Mixin(Screen.class)
public abstract class ScreenLayersMixin {
	@Inject(method = "onClose", at = @At("HEAD"), cancellable = true)
	private void securitycraft$closeModal(CallbackInfo ci) {
		Minecraft client = Minecraft.getInstance();
		ScreenLayers layers = (ScreenLayers) client.gui;
		if (client.gui.screen() == (Object) this && layers.securitycraft$hasLayers()) {
			layers.securitycraft$popLayer();
			ci.cancel();
		}
	}
}
