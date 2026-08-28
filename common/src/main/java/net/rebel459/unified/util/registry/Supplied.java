package net.rebel459.unified.util.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

public class Supplied<T> implements Supplier<T> {

    private final Supplier<? extends Registry<?>> registry;
    private final ResourceKey<?> key;
    private final Supplier<?> supplied;

    @ApiStatus.Internal
    public Supplied(Supplier<? extends Registry<?>> registry, ResourceKey<?> key, Supplier<?> supplied) {
        this.registry = registry;
        this.key = key;
        this.supplied = supplied;
    }

    @SuppressWarnings("unchecked")
    public ResourceKey<T> key() {
        return (ResourceKey<T>) key;
    }

    public Identifier id() {
        return key().identifier();
    }

    @SuppressWarnings("unchecked")
    public Holder<T> holder() {
        return ((Registry<T>) registry.get()).getOrThrow(key());
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get() {
        return (T) supplied.get();
    }
}
