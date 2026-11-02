package net.geforcemods.securitycraft.mixin.camera;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.geforcemods.securitycraft.entity.camera.CameraController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;

@Mixin(Screenshot.class)
public abstract class ScreenshotMixin {
	@Inject(method = "grab(Lnet/minecraft/client/Minecraft;Z)V", at = @At("HEAD"))
	private static void securitycraft$cameraShutterSound(Minecraft client, boolean includeHud, CallbackInfo ci) {
		CameraController.onScreenshot();
	}
}
