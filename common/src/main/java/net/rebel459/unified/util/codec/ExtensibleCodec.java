package net.rebel459.unified.util.codec;

import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

/** Used to supplied codecs which other mods can append to */
public final class ExtensibleCodec<R> {
    private final String typeField;
    private final Map<Identifier, Type<R>> types = new ConcurrentHashMap<>();

    public ExtensibleCodec(String typeField) {
        this.typeField = typeField;
    }

    public synchronized <T> Type<R> register(
            Identifier id,
            MapCodec<T> codec,
            Function<T, ? extends R> factory
    ) {
        if (types.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate extensible codec type " + id);
        }

        Type<R> type = new Type<>(id, codec, factory);
        types.put(id, type);
        return type;
    }

    public Type<R> register(Identifier id, Supplier<? extends R> factory) {
        return register(id, MapCodec.unit(Unit.INSTANCE), _ -> factory.get());
    }

    public MapCodec<Entry<R>> codec(Identifier defaultTypeId) {
        MapCodec<Type<R>> typeCodec = Identifier.CODEC.optionalFieldOf(typeField, defaultTypeId).flatXmap(
                id -> {
                    Type<R> type = types.get(id);
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

    private MapCodec<Entry<R>> entryCodec(Type<R> type) {
        return type.codec.xmap(
                data -> new Entry<>(type, data),
                entry -> entry.data(type)
        );
    }

    public static final class Entry<R> {
        private final Type<R> type;
        private final Object data;

        private Entry(Type<R> type, Object data) {
            this.type = type;
            this.data = data;
        }

        public Identifier typeId() {
            return type.id();
        }

        public R value() {
            return type.create(data);
        }

        private Type<R> type() {
            return type;
        }

        private Object data(Type<R> expectedType) {
            if (type != expectedType) {
                throw new IllegalStateException("Entry was encoded with the wrong extensible codec type");
            }
            return data;
        }
    }

    public static final class Type<R> {
        private final Identifier id;
        private final MapCodec<Object> codec;
        private final Function<Object, ? extends R> factory;

        @SuppressWarnings("unchecked")
        private <T> Type(Identifier id, MapCodec<T> codec, Function<T, ? extends R> factory) {
            this.id = id;
            this.codec = (MapCodec<Object>) codec;
            this.factory = data -> factory.apply((T) data);
        }

        public Identifier id() {
            return id;
        }

        private R create(Object data) {
            return factory.apply(data);
        }
    }
}
