package net.geforcemods.securitycraft.mixin.platform;

import net.geforcemods.securitycraft.platform.FakeFluidBrewing;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.inventory.BrewingStandMenu$PotionSlot")
public class BrewingPotionSlotMixin {
    @Inject(method = "mayPlaceItem", at = @At("HEAD"), cancellable = true)
    private static void securitycraft$acceptBucket(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (FakeFluidBrewing.isBucketInput(stack)) cir.setReturnValue(true);
    }
}
