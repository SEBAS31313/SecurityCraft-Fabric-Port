package net.geforcemods.securitycraft.platform;

import net.geforcemods.securitycraft.SCContent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

/** The original bucket brewing recipes, including their component-sensitive potion ingredients. */
public final class FakeFluidBrewing {
    private FakeFluidBrewing() {}
    public static boolean isBucketInput(ItemStack stack) { return stack.is(Items.WATER_BUCKET) || stack.is(Items.LAVA_BUCKET); }
    public static boolean isIngredient(ItemStack stack) { return potionKind(stack) != 0; }
    public static boolean matches(ItemStack input, ItemStack ingredient) {
        int kind = potionKind(ingredient);
        return kind == 1 && input.is(Items.WATER_BUCKET) || kind == 2 && input.is(Items.LAVA_BUCKET);
    }
    public static ItemStack brew(ItemStack input) {
        return new ItemStack(input.is(Items.WATER_BUCKET) ? SCContent.FAKE_WATER_BUCKET.get() : SCContent.FAKE_LAVA_BUCKET.get());
    }
    private static int potionKind(ItemStack stack) {
        if (!(stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION))) return 0;
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null) return 0;
        if (contents.equals(new PotionContents(Potions.HARMING)) || contents.equals(new PotionContents(Potions.STRONG_HARMING))) return 1;
        if (contents.equals(new PotionContents(Potions.HEALING)) || contents.equals(new PotionContents(Potions.STRONG_HEALING))) return 2;
        return 0;
    }
}
