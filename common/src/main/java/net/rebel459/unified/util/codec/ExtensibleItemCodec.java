package net.rebel459.unified.util.codec;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

import java.util.function.Function;
import java.util.function.Supplier;

public final class ExtensibleItemCodec extends ExtensibleCodec<Function<Item.Properties, Item>> {
    public ExtensibleItemCodec(String typeField) {
        super(typeField);
    }

    @Override
    public Simple register(Identifier id, Supplier<? extends Function<Item.Properties, Item>> factory) {
        return (Simple) super.register(id, factory);
    }

    @Override
    public synchronized <T> Complex<T> register(Identifier id, MapCodec<T> codec, Function<T, ? extends Function<Item.Properties, Item>> factory) {
        return (Complex<T>) super.register(id, codec, factory);
    }

    @Override
    protected ExtensibleCodec.Simple<Function<Item.Properties, Item>> createSimple(Identifier id, Supplier<? extends Function<Item.Properties, Item>> factory) {
        return new Simple(id, factory);
    }

    @Override
    protected <T> ExtensibleCodec.Complex<Function<Item.Properties, Item>, T> createComplex(Identifier id, MapCodec<T> codec, Function<T, ? extends Function<Item.Properties, Item>> factory) {
        return new Complex<>(id, codec, factory);
    }

    public static final class Simple extends ExtensibleCodec.Simple<Function<Item.Properties, Item>> {
        private Simple(Identifier id, Supplier<? extends Function<Item.Properties, Item>> factory) { super(id, factory); }
    }

    public static final class Complex<T> extends ExtensibleCodec.Complex<Function<Item.Properties, Item>, T> {
        private Complex(Identifier id, MapCodec<T> codec, Function<T, ? extends Function<Item.Properties, Item>> factory) { super(id, codec, factory); }
    }
}
