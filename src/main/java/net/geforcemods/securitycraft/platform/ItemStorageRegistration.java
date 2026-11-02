package net.geforcemods.securitycraft.platform;

import java.util.function.BiFunction;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.Collections;
import net.fabricmc.fabric.api.lookup.v1.entity.EntityApiLookup;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.platform.transfer.ItemResource;
import net.geforcemods.securitycraft.platform.transfer.ResourceHandler;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.Block;
import net.geforcemods.securitycraft.mixin.platform.BlockEntityTypeBlocksAccess;

public final class ItemStorageRegistration {
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static final EntityApiLookup<Storage<ItemVariant>, Direction> ENTITY = EntityApiLookup.get(
        SecurityCraft.resLoc("entity_item_storage"), (Class) Storage.class, Direction.class);
    private static final IdentityHashMap<BlockEntityType<?>,BiFunction<BlockEntity,Direction,ResourceHandler<ItemResource>>> BLOCK_HANDLERS = new IdentityHashMap<>();
    private static final Set<Block> REGISTERED_BLOCKS = Collections.newSetFromMap(new IdentityHashMap<>());
    private ItemStorageRegistration() {}
    @SuppressWarnings("unchecked")
    public static <T extends BlockEntity> void registerBlockEntity(BlockEntityType<T> type, BiFunction<T,Direction,ResourceHandler<ItemResource>> provider) {
        if (BLOCK_HANDLERS.putIfAbsent(type, (entity, side) -> provider.apply((T)entity, side)) != null)
            throw new IllegalStateException("Duplicate SecurityCraft storage provider for " + type);
        for (Block block : ((BlockEntityTypeBlocksAccess)type).securitycraft$validBlocks()) {
            if (!REGISTERED_BLOCKS.add(block)) continue;
            // Moving piston states belong to several block entity types. Dispatch by the live type,
            // rather than allowing the first registration for that block to hide later providers.
            ItemStorage.SIDED.registerForBlocks((level, pos, state, entity, side) -> {
                if (entity == null) return null;
                var selected = BLOCK_HANDLERS.get(entity.getType());
                ResourceHandler<ItemResource> handler = selected == null ? null : selected.apply(entity, side);
                return handler == null ? null : handler.asStorage();
            }, block);
        }
    }
    public static <T extends Entity> void registerEntity(EntityType<T> type, BiFunction<T,Direction,ResourceHandler<ItemResource>> provider) {
        ENTITY.registerForType((entity, side) -> {
            ResourceHandler<ItemResource> handler = provider.apply(entity, side);
            return handler == null ? null : handler.asStorage();
        }, type);
    }
}
