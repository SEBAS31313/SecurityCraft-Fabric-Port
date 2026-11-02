package net.geforcemods.securitycraft.platform.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** Keeps the original dependency ordering while registering directly into the Fabric/vanilla registries. */
public class DeferredRegister<R> {
    private final ResourceKey<? extends Registry<R>> registryKey;
    protected final String namespace;
    private final List<Runnable> registrations = new ArrayList<>();
    private final List<DeferredHolder<R,? extends R>> entries = new ArrayList<>();
    public List<DeferredHolder<R,? extends R>> getEntries() { return List.copyOf(entries); }
    private boolean registered;
    protected DeferredRegister(ResourceKey<? extends Registry<R>> registryKey, String namespace) {
        this.registryKey = registryKey; this.namespace = namespace;
    }
    public static <R> DeferredRegister<R> create(ResourceKey<? extends Registry<R>> key, String namespace) { return new DeferredRegister<>(key, namespace); }
    public static Blocks createBlocks(String namespace) { return new Blocks(namespace); }
    public static Items createItems(String namespace) { return new Items(namespace); }
    public static DataComponents createDataComponents(ResourceKey<? extends Registry<DataComponentType<?>>> key, String namespace) { return new DataComponents(key, namespace); }
    public static DeferredRegister<EntityDataSerializer<?>> createEntityDataSerializers(String namespace) { return new DeferredRegister<>(null, namespace); }
    protected ResourceKey<R> key(String path) { return ResourceKey.create(registryKey, Identifier.fromNamespaceAndPath(namespace, path)); }
    public <T extends R> DeferredHolder<R,T> register(String path, Supplier<T> supplier) {
        ResourceKey<R> key = registryKey == null ? ResourceKey.create(ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(namespace, "entity_data_serializers")), Identifier.fromNamespaceAndPath(namespace, path)) : key(path);
        return defer(new DeferredHolder<>(key), supplier);
    }
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected <T extends R, H extends DeferredHolder<R,T>> H defer(H holder, Supplier<T> supplier) {
        if (registered) throw new IllegalStateException("Registration added after initialization: " + holder.getId());
        entries.add(holder);
        registrations.add(() -> {
            T value = supplier.get();
            if (registryKey == null) {
                net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityDataRegistry.register(holder.getId(), (EntityDataSerializer<?>) value);
                holder.bind(value, net.minecraft.core.Holder.direct(value));
            } else {
                Registry<R> registry = (Registry<R>) BuiltInRegistries.REGISTRY.getValue((ResourceKey) registryKey);
                Registry.register(registry, holder.getKey(), value);
                holder.bind(value, registry.wrapAsHolder(value));
            }
        });
        return holder;
    }
    public void register() {
        if (registered) throw new IllegalStateException("Registry initialized twice: " + registryKey);
        registered = true;
        registrations.forEach(Runnable::run);
    }
    public static final class Blocks extends DeferredRegister<Block> {
        private Blocks(String namespace) { super(Registries.BLOCK, namespace); }
        public DeferredBlock<Block> registerSimpleBlock(String path, Supplier<BlockBehaviour.Properties> properties) { return registerBlock(path, Block::new, properties); }
        public <T extends Block> DeferredBlock<T> registerBlock(String path, Function<BlockBehaviour.Properties,T> constructor, Supplier<BlockBehaviour.Properties> properties) {
            ResourceKey<Block> key = key(path);
            return defer(new DeferredBlock<>(key), () -> constructor.apply(properties.get().setId(key)));
        }
    }
    public static final class Items extends DeferredRegister<Item> {
        private Items(String namespace) { super(Registries.ITEM, namespace); }
        public <T extends Item> DeferredItem<T> registerItem(String path, Function<Item.Properties,T> constructor, UnaryOperator<Item.Properties> properties) {
            ResourceKey<Item> key = key(path);
            return defer(new DeferredItem<>(key), () -> constructor.apply(properties.apply(new Item.Properties().setId(key))));
        }
        public DeferredItem<Item> registerSimpleItem(String path) { return registerSimpleItem(path, UnaryOperator.identity()); }
        public DeferredItem<net.minecraft.world.item.BlockItem> registerSimpleBlockItem(DeferredBlock<? extends Block> block, UnaryOperator<Item.Properties> properties) { return registerSimpleBlockItem(block.getId().getPath(), block, properties); }
        public DeferredItem<net.minecraft.world.item.BlockItem> registerSimpleBlockItem(String path, Supplier<? extends Block> block, UnaryOperator<Item.Properties> properties) { return registerItem(path, p -> new net.minecraft.world.item.BlockItem(block.get(), p.useBlockDescriptionPrefix()), properties); }
        public DeferredItem<Item> registerSimpleItem(String path, UnaryOperator<Item.Properties> properties) { return registerItem(path, Item::new, properties); }
    }
    public static final class DataComponents extends DeferredRegister<DataComponentType<?>> {
        private DataComponents(ResourceKey<? extends Registry<DataComponentType<?>>> key, String namespace) { super(key, namespace); }
        public <T> DeferredHolder<DataComponentType<?>,DataComponentType<T>> registerComponentType(String path, UnaryOperator<DataComponentType.Builder<T>> builder) {
            return register(path, () -> builder.apply(DataComponentType.builder()).build());
        }
    }
}
