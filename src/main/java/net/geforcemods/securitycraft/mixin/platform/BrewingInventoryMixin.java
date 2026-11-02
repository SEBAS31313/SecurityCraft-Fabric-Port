package net.geforcemods.securitycraft.mixin.platform;

import net.geforcemods.securitycraft.platform.FakeFluidBrewing;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BrewingStandBlockEntity.class)
public abstract class BrewingInventoryMixin {
    @Inject(method = "canPlaceItem", at = @At("HEAD"), cancellable = true)
    private void securitycraft$acceptBucketForAutomation(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (slot >= 0 && slot < 3 && FakeFluidBrewing.isBucketInput(stack)) cir.setReturnValue(((net.minecraft.world.Container)(Object)this).getItem(slot).isEmpty());
    }
}
