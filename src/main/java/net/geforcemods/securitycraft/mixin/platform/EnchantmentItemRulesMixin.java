package net.geforcemods.securitycraft.mixin.platform;

import net.geforcemods.securitycraft.items.CodebreakerItem;
import net.geforcemods.securitycraft.items.TaserItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public class EnchantmentItemRulesMixin {
    @Inject(method = {"isSupportedItem", "isPrimaryItem"}, at = @At("HEAD"), cancellable = true)
    private void securitycraft$preserveNonEnchantableTools(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() instanceof CodebreakerItem || stack.getItem() instanceof TaserItem) cir.setReturnValue(false);
    }
}
