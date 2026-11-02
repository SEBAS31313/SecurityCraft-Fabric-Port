package net.geforcemods.securitycraft.platform.transfer;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
/** Keeps the original item/component identity while using Fabric's native transfer variant. */
public record ItemResource(ItemVariant variant) implements Resource {
 public static ItemResource of(ItemLike item) { return new ItemResource(ItemVariant.of(item)); }
 public static ItemResource of(ItemStack stack) { return new ItemResource(ItemVariant.of(stack)); }
 public static ItemResource empty() { return new ItemResource(ItemVariant.blank()); }
 public ItemStack toStack() { return variant.toStack(); }
 public ItemStack toStack(int count) { return variant.toStack(count); }
 public Item getItem() { return variant.getItem(); }
 public boolean is(ItemLike item) { return variant.isOf(item.asItem()); }
 public boolean isEmpty() { return variant.isBlank(); }
}
