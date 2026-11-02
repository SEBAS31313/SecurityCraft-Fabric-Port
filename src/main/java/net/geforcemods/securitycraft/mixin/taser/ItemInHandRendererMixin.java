package net.geforcemods.securitycraft.mixin.taser;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.geforcemods.securitycraft.items.TaserItem;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.ItemInHandRenderer.HandRenderSelection;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.Minecraft;

/**
 * Makes sure that when a taser is held, only the hand that is holding the taser is rendered - prioritizing the main hand
 */
@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
	@Unique private int securitycraft$lastSelectedSlot = -1;

	@Inject(method = "shouldInstantlyReplaceVisibleItem", at = @At("HEAD"), cancellable = true)
	private void securitycraft$retainTaserWhileCharging(ItemStack previous, ItemStack next, CallbackInfoReturnable<Boolean> cir) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (previous.getItem() instanceof TaserItem && next.getItem() == previous.getItem()
				&& player != null && player.getInventory().getSelectedSlot() == securitycraft$lastSelectedSlot)
			cir.setReturnValue(true);
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void securitycraft$rememberSelectedSlot(CallbackInfo ci) {
		LocalPlayer player = Minecraft.getInstance().player;
		securitycraft$lastSelectedSlot = player == null ? -1 : player.getInventory().getSelectedSlot();
	}

	@WrapOperation(method = "submitArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;applyItemArmTransform(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/entity/HumanoidArm;F)V"))
	private void securitycraft$positionTaser(ItemInHandRenderer renderer, PoseStack pose, HumanoidArm arm, float equippedProgress, Operation<Void> original,
			AbstractClientPlayer player, float partialTick, float xRot, InteractionHand hand, float swingProgress, ItemStack stack,
			float inverseArmHeight, PoseStack enclosingPose, SubmitNodeCollector collector, int lightCoords) {
		if (stack.getItem() instanceof TaserItem && swingProgress < 0.001F)
			pose.translate(0.02F, -0.4F, -0.5F);
		else
			original.call(renderer, pose, arm, equippedProgress);
	}

	@Inject(method = "evaluateWhichHandsToRender", at = @At("HEAD"), cancellable = true)
	private static void securitycraft$onlyRenderMainHandWhenHoldingTaser(LocalPlayer player, CallbackInfoReturnable<HandRenderSelection> cir) {
		if (player.getMainHandItem().getItem() instanceof TaserItem)
			cir.setReturnValue(HandRenderSelection.RENDER_MAIN_HAND_ONLY);
		else if (player.getOffhandItem().getItem() instanceof TaserItem)
			cir.setReturnValue(HandRenderSelection.RENDER_OFF_HAND_ONLY);
	}
}
