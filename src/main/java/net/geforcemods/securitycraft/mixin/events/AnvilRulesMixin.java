package net.geforcemods.securitycraft.mixin.events;

import net.geforcemods.securitycraft.platform.GameplayEventBus;
import net.geforcemods.securitycraft.platform.events.SCEvents.AnvilUpdateEvent;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilMenu.class)
public abstract class AnvilRulesMixin {
    @Inject(method="createResult",at=@At("RETURN"))
    private void securitycraft$result(CallbackInfo ci) {
        AnvilMenu menu=(AnvilMenu)(Object)this;
        if(GameplayEventBus.EVENT_BUS.post(new AnvilUpdateEvent(menu.getSlot(0).getItem(),menu.getSlot(1).getItem())).isCanceled()) menu.getSlot(2).set(ItemStack.EMPTY);
    }
}
