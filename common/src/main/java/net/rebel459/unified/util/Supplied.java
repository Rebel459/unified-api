package net.rebel459.unified.util;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

public class Supplied<T> extends ResourceKey<T> implements Supplier<T> {

    private final Supplier<? extends Registry<?>> registry;
    private final ResourceKey<?> key;
    private final Supplier<?> supplied;

    @ApiStatus.Internal
    public Supplied(Supplier<? extends Registry<?>> registry, ResourceKey<?> key, Supplier<?> supplied) {
        super(key.registry(), key.identifier());
        this.registry = registry;
        this.key = key;
        this.supplied = supplied;
    }

    @SuppressWarnings("unchecked")
    public Holder<T> holder() {
        return (Holder<T>) ((Registry<Object>) registry.get()).getOrThrow((ResourceKey<Object>) key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get() {
        return (T) supplied.get();
    }
}