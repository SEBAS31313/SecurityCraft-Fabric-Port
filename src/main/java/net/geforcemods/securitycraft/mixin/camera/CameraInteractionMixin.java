package net.geforcemods.securitycraft.mixin.camera;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.geforcemods.securitycraft.ClientHandler;
import net.geforcemods.securitycraft.SCContent;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;

/** Suppresses normal player interactions while viewing a camera, retaining the monitor control. */
@Mixin(Minecraft.class)
public abstract class CameraInteractionMixin {
	@Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
	private void securitycraft$preventAttackFromCamera(CallbackInfoReturnable<Boolean> cir) {
		if (ClientHandler.isPlayerMountedOnCamera())
			cir.setReturnValue(false);
	}

	@Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
	private void securitycraft$onlyUseMonitorFromCamera(CallbackInfo ci) {
		if (!ClientHandler.isPlayerMountedOnCamera())
			return;
		Minecraft mc = (Minecraft) (Object) this;
		if (mc.player != null && mc.level != null) {
			for (InteractionHand hand : InteractionHand.values()) {
				if (mc.player.getItemInHand(hand).is(SCContent.CAMERA_MONITOR.get())) {
					SCContent.CAMERA_MONITOR.get().use(mc.level, mc.player, hand);
					break;
				}
			}
		}
		ci.cancel();
	}
}
