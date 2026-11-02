package net.geforcemods.securitycraft.mixin.events;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.geforcemods.securitycraft.platform.CraftingContext;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ResultSlot.class)
public abstract class CraftingPlayerMixin {
    @WrapMethod(method="onTake")
    private void securitycraft$craftingPlayer(Player player,ItemStack stack,Operation<Void> original) {
        try(var scope=CraftingContext.push(player)){original.call(player,stack);}
    }
}
