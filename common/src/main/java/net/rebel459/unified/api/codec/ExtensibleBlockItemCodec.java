package net.rebel459.unified.api.codec;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ExtensibleBlockItemCodec extends ExtensibleCodec<BiFunction<Block, Item.Properties, Item>> {
    public ExtensibleBlockItemCodec() {
        super();
    }

    @Override
    public Simple register(Identifier id, Supplier<? extends BiFunction<Block, Item.Properties, Item>> factory) {
        return (Simple) super.register(id, factory);
    }

    @Override
    public synchronized <T> Complex<T> register(Identifier id, MapCodec<T> codec, Function<T, ? extends BiFunction<Block, Item.Properties, Item>> factory) {
        return (Complex<T>) super.register(id, codec, factory);
    }

    @Override
    protected ExtensibleCodec.Simple<BiFunction<Block, Item.Properties, Item>> createSimple(Identifier id, Supplier<? extends BiFunction<Block, Item.Properties, Item>> factory) {
        return new Simple(id, factory);
    }

    @Override
    protected <T> ExtensibleCodec.Complex<BiFunction<Block, Item.Properties, Item>, T> createComplex(Identifier id, MapCodec<T> codec, Function<T, ? extends BiFunction<Block, Item.Properties, Item>> factory) {
        return new Complex<>(id, codec, factory);
    }

    public static final class Simple extends ExtensibleCodec.Simple<BiFunction<Block, Item.Properties, Item>> {
        private Simple(Identifier id, Supplier<? extends BiFunction<Block, Item.Properties, Item>> factory) { super(id, factory); }
    }

    public static final class Complex<T> extends ExtensibleCodec.Complex<BiFunction<Block, Item.Properties, Item>, T> {
        private Complex(Identifier id, MapCodec<T> codec, Function<T, ? extends BiFunction<Block, Item.Properties, Item>> factory) { super(id, codec, factory); }
    }
}
