package net.rebel459.unified.api.core;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

public class Supplied<T> implements Supplier<T> {

    private final ResourceKey<T> key;
    private final Supplier<? extends T> supplier;
    private final Holder<T> holder;

    @ApiStatus.Internal
    @SuppressWarnings("unchecked")
    public Supplied(ResourceKey<? super T> key, Supplier<? extends T> supplier, Holder<? super T> holder) {
        this.key = (ResourceKey<T>) key;
        this.supplier = supplier;
        this.holder = (Holder<T>) holder;
    }

    public ResourceKey<T> key() {
        return key;
    }

    public Identifier id() {
        return key().identifier();
    }

    public Holder<T> holder() {
        return holder;
    }

    @Override
    public T get() {
        return supplier.get();
    }
}
