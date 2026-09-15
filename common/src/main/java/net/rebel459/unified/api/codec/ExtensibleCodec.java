package net.rebel459.unified.api.codec;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Unit;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

/** Used to create codecs which other mods can append to */
public class ExtensibleCodec<R> {
    private final Map<Identifier, InternalType<R>> types = new ConcurrentHashMap<>();

    public boolean contains(Identifier id) {
        return types.containsKey(id);
    }

    public synchronized <T> Complex<R, T> register(Identifier id, MapCodec<T> codec, Function<T, ? extends R> factory) {
        Complex<R, T> type = createComplex(id, codec, factory);
        return registerComplexType(type);
    }

    public Simple<R> register(Identifier id, Supplier<? extends R> factory) {
        Simple<R> type = createSimple(id, factory);
        return registerSimpleType(type);
    }

    protected synchronized <S extends Simple<R>> S registerSimpleType(S type) {
        registerType(type);
        return type;
    }

    protected synchronized <C extends Complex<R, ?>> C registerComplexType(C type) {
        registerType(type);
        return type;
    }

    private void registerType(InternalType<R> type) {
        if (types.putIfAbsent(type.id(), type) != null) {
            throw new IllegalArgumentException("Duplicate extensible codec type " + type.id());
        }
    }

    protected Simple<R> createSimple(Identifier id, Supplier<? extends R> factory) {
        return new Simple<>(id, factory);
    }

    protected <T> Complex<R, T> createComplex(Identifier id, MapCodec<T> codec, Function<T, ? extends R> factory) {
        return new Complex<>(id, codec, factory);
    }


    /**
     * Provides a codec which can be used with a named field. If the identifier has no additional fields, it can be displayed compactly
     * If the identifier has no additional fields, it can be displayed compactly like "[field_name]": "[identifier]"
     * Otherwise, it can be displayed in full like:
     * {
     *  "[type_field]": "[identifier]",
     *  "[other_field]": "[other_value]",
     *  ...
     * }
     */
    public Codec<Entry<R>> codec() {
        return codec("type");
    }

    public Codec<Entry<R>> codec(String name) {
        MapCodec<Entry<R>> objectCodec = typeCodec(name).dispatchMap(Entry::type, this::entryCodec);

        return Codec.either(Identifier.CODEC, objectCodec.codec()).flatXmap(
                value -> value.map(this::compactEntry, DataResult::success),
                entry -> DataResult.success(entry.type.compact
                        ? Either.left(entry.type.id())
                        : Either.right(entry))
        );
    }

    private DataResult<Entry<R>> compactEntry(Identifier id) {
        InternalType<R> type = types.get(id);
        if (type == null) {
            return DataResult.error(() -> "Unknown extensible codec type " + id);
        }
        if (!type.compact) {
            return DataResult.error(() -> "Extensible codec type " + id + " requires an object value");
        }
        return DataResult.success(new Entry<>(type, () -> Unit.INSTANCE));
    }

    private MapCodec<InternalType<R>> typeCodec(String name) {
        return Identifier.CODEC.fieldOf(name).flatXmap(
                id -> {
                    InternalType<R> type = types.get(id);
                    return type == null
                            ? DataResult.error(() -> "Unknown extensible codec type " + id)
                            : DataResult.success(type);
                },
                type -> DataResult.success(type.id())
        );
    }

    /** Gets a flat-level codec */
    public MapCodec<Entry<R>> mapCodec() {
        return mapCodec("type");
    }

    public MapCodec<Entry<R>> mapCodec(String name) {
        MapCodec<InternalType<R>> typeCodec = Identifier.CODEC.fieldOf(name).flatXmap(
                id -> {
                    InternalType<R> type = types.get(id);
                    return type == null
                            ? DataResult.error(() -> "Unknown extensible codec type " + id)
                            : DataResult.success(type);
                },
                type -> DataResult.success(type.id())
        );

        return typeCodec.dispatchMap(
                Entry::type,
                this::entryCodec
        );
    }

    private MapCodec<Entry<R>> entryCodec(InternalType<R> type) {
        return type.codec.xmap(
                data -> new Entry<>(type, () -> data),
                entry -> entry.data(type)
        );
    }

    public static final class Entry<R> implements Supplier<R> {
        private final InternalType<R> type;
        private final Supplier<?> data;

        private Entry(InternalType<R> type, Supplier<?> data) {
            this.type = type;
            this.data = Suppliers.memoize(data::get);
        }

        public Identifier id() {
            return type.id();
        }

        @Override
        public R get() {
            return type.instantiate(data.get());
        }

        private InternalType<R> type() {
            return type;
        }

        private Object data(InternalType<R> expectedType) {
            if (type != expectedType) {
                throw new IllegalStateException("Entry was encoded with the wrong extensible codec type");
            }
            return data.get();
        }
    }

    private abstract static sealed class InternalType<R> permits Simple, Complex {
        private final Identifier id;
        private final MapCodec<Object> codec;
        private final Function<Object, ? extends R> factory;
        private final boolean compact;

        @SuppressWarnings("unchecked")
        private <T> InternalType(Identifier id, MapCodec<T> codec, Function<T, ? extends R> factory, boolean compact) {
            this.id = id;
            this.codec = (MapCodec<Object>) codec;
            this.factory = data -> factory.apply((T) data);
            this.compact = compact;
        }

        public Identifier id() {
            return id;
        }

        private R instantiate(Object data) {
            return factory.apply(data);
        }
    }

    public static non-sealed class Simple<R> extends InternalType<R> {
        protected Simple(Identifier id, Supplier<? extends R> factory) {
            super(id, MapCodec.unit(Unit.INSTANCE), _ -> factory.get(), true);
        }

        public Entry<R> create() {
            return new Entry<>(this, () -> Unit.INSTANCE);
        }
    }

    public static non-sealed class Complex<R, T> extends InternalType<R> {
        protected Complex(Identifier id, MapCodec<T> codec, Function<T, ? extends R> factory) {
            super(id, codec, factory, false);
        }

        public Entry<R> create(Supplier<? extends T> data) {
            return new Entry<>(this, data);
        }
    }
}
