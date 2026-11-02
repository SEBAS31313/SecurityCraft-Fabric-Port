package net.geforcemods.securitycraft.mixin.platform;

import net.geforcemods.securitycraft.platform.FakeFluidBrewing;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotionBrewing.class)
public class PotionBrewingMixin {
    @Inject(method = "isIngredient", at = @At("HEAD"), cancellable = true)
    private void securitycraft$acceptRecipePotion(ItemStack ingredient, CallbackInfoReturnable<Boolean> cir) {
        if (FakeFluidBrewing.isIngredient(ingredient)) cir.setReturnValue(true);
    }
    @Inject(method = "hasMix", at = @At("HEAD"), cancellable = true)
    private void securitycraft$bucketRecipe(ItemStack input, ItemStack ingredient, CallbackInfoReturnable<Boolean> cir) {
        if (FakeFluidBrewing.matches(input, ingredient)) cir.setReturnValue(true);
    }
    @Inject(method = "mix", at = @At("HEAD"), cancellable = true)
    private void securitycraft$brewBucket(ItemStack ingredient, ItemStack input, CallbackInfoReturnable<ItemStack> cir) {
        if (FakeFluidBrewing.matches(input, ingredient)) cir.setReturnValue(FakeFluidBrewing.brew(input));
    }
}
