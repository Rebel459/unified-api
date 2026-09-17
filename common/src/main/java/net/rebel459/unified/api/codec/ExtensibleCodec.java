package net.rebel459.unified.api.codec;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;

import java.util.function.Function;
import java.util.function.Supplier;

/** Used to create codecs which other mods can append to. */
public class ExtensibleCodec<R> extends ExtensibleCodecBase<R> {

    public synchronized <T> Complex<R, T> register(Identifier id, MapCodec<T> codec, Function<T, ? extends R> factory) {
        return registerComplexType(createComplex(id, codec, factory));
    }

    public Simple<R> register(Identifier id, Supplier<? extends R> factory) {
        return registerSimpleType(createSimple(id, factory));
    }

    protected Simple<R> createSimple(Identifier id, Supplier<? extends R> factory) {
        return new Simple<>(id, factory);
    }

    protected <T> Complex<R, T> createComplex(Identifier id, MapCodec<T> codec, Function<T, ? extends R> factory) {
        return new Complex<>(id, codec, factory);
    }
}
