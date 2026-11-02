package net.geforcemods.securitycraft.platform.registry;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

public final class DeferredItem<T extends Item> extends DeferredHolder<Item,T> implements ItemLike {
    DeferredItem(ResourceKey<Item> key) { super(key); }
    @Override public Item asItem() { return get(); }
}
