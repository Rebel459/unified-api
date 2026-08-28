package net.rebel459.unified.util.codec;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;
import java.util.function.Supplier;

public final class ExtensibleBlockCodec extends ExtensibleCodec<Function<BlockBehaviour.Properties, ? extends Block>> {
    public ExtensibleBlockCodec(String typeField) { super(typeField); }

    @Override
    public Simple register(Identifier id, Supplier<? extends Function<BlockBehaviour.Properties, ? extends Block>> factory) {
        return (Simple) super.register(id, factory);
    }

    @Override
    public synchronized <T> Complex<T> register(Identifier id, MapCodec<T> codec, Function<T, ? extends Function<BlockBehaviour.Properties, ? extends Block>> factory) {
        return (Complex<T>) super.register(id, codec, factory);
    }

    @Override
    protected ExtensibleCodec.Simple<Function<BlockBehaviour.Properties, ? extends Block>> createSimple(Identifier id, Supplier<? extends Function<BlockBehaviour.Properties, ? extends Block>> factory) { return new Simple(id, factory); }

    @Override
    protected <T> ExtensibleCodec.Complex<Function<BlockBehaviour.Properties, ? extends Block>, T> createComplex(Identifier id, MapCodec<T> codec, Function<T, ? extends Function<BlockBehaviour.Properties, ? extends Block>> factory) { return new Complex<>(id, codec, factory); }

    public static final class Simple extends ExtensibleCodec.Simple<Function<BlockBehaviour.Properties, ? extends Block>> {
        private Simple(Identifier id, Supplier<? extends Function<BlockBehaviour.Properties, ? extends Block>> factory) { super(id, factory); }
    }

    public static final class Complex<T> extends ExtensibleCodec.Complex<Function<BlockBehaviour.Properties, ? extends Block>, T> {
        private Complex(Identifier id, MapCodec<T> codec, Function<T, ? extends Function<BlockBehaviour.Properties, ? extends Block>> factory) { super(id, codec, factory); }
    }
}
