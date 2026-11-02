package net.geforcemods.securitycraft.platform;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

/** Tools that must run before a block opens its ordinary interaction menu. */
public interface FirstUseItem {
    InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context);
    static void register() {
        UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            if (player.isSpectator()) return InteractionResult.PASS;
            ItemStack stack = player.getItemInHand(hand);
            return stack.getItem() instanceof FirstUseItem first
                ? first.onItemUseFirst(stack, new UseOnContext(player, hand, hit)) : InteractionResult.PASS;
        });
    }
}
