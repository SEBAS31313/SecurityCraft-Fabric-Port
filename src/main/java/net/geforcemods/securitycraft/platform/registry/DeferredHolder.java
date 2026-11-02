package net.geforcemods.securitycraft.platform.registry;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

/** Deferred registry reference. Values are bound during common initialization, before registry freeze. */
public class DeferredHolder<R, T extends R> implements Supplier<T> {
    private final ResourceKey<R> key;
    private T value;
    private Holder<R> holder;

    protected DeferredHolder(ResourceKey<R> key) { this.key = key; }
    void bind(T value, Holder<R> holder) { this.value = value; this.holder = holder; }
    @Override public T get() {
        if (value == null) throw new IllegalStateException("Registry value used before registration: " + key);
        return value;
    }
    public ResourceKey<R> getKey() { return key; }
    public Identifier getId() { return key.identifier(); }
    public T value() { return get(); }
    public Holder<R> asHolder() { if (holder == null) throw new IllegalStateException("Unbound holder: " + key); return holder; }
    public boolean isBound() { return value != null; }
    public boolean areComponentsBound() { return holder != null && holder.areComponentsBound(); }
    public boolean is(Identifier id) { return key.identifier().equals(id); }
    public boolean is(ResourceKey<R> key) { return this.key.equals(key); }
    public boolean is(Predicate<ResourceKey<R>> predicate) { return predicate.test(key); }
    public boolean is(TagKey<R> tag) { return holder != null && holder.is(tag); }
    public boolean is(Holder<R> other) { return other.unwrapKey().map(key::equals).orElseGet(() -> isBound() && other.value() == value); }
    public Stream<TagKey<R>> tags() { return holder == null ? Stream.empty() : holder.tags(); }
    public DataComponentMap components() { return holder == null ? DataComponentMap.EMPTY : holder.components(); }
    public Either<ResourceKey<R>, R> unwrap() { return Either.left(key); }
    public Optional<ResourceKey<R>> unwrapKey() { return Optional.of(key); }
    public Holder.Kind kind() { return Holder.Kind.REFERENCE; }
    public boolean canSerializeIn(HolderOwner<R> owner) { return holder != null && holder.canSerializeIn(owner); }
}
