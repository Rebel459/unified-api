package net.rebel459.unified.util.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public final class CodecUtils {

    public static <T> Codec<Supplier<T>> supplied(Codec<T> codec, Supplier<? extends RegistryAccess> registries) {
        return supplied(codec, registries, Function.identity());
    }
    public static <T, R> Codec<Supplier<R>> supplied(Codec<T> codec, Supplier<? extends RegistryAccess> registries, Function<? super T, ? extends R> mapper) {
        return Codec.PASSTHROUGH.flatComapMap(
                raw -> serializableSupplier(raw, codec, registries, mapper),
                supplier -> supplier instanceof SerializableSupplier<?> serializable
                        ? DataResult.success(serializable.raw())
                        : DataResult.error(() -> "Supplier does not retain its encoded value")
        );
    }

    public static <T, R> Supplier<R> suppliedValue(
            Codec<T> codec,
            T value,
            Supplier<? extends RegistryAccess> registries,
            Function<? super T, ? extends R> mapper
    ) {
        RegistryAccess access = registries.get();
        DynamicOps<com.google.gson.JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, access);
        com.google.gson.JsonElement encoded = codec.encodeStart(ops, value).getOrThrow();
        return serializableSupplier(new Dynamic<>(ops, encoded), codec, registries, mapper);
    }

    public static <T> Codec<T> named(Class<T> type) {
        Map<String, T> valuesByName = new LinkedHashMap<>();
        Map<T, String> namesByValue = new IdentityHashMap<>();
        for (Field field : type.getFields()) {
            int modifiers = field.getModifiers();
            if (!Modifier.isPublic(modifiers) || !Modifier.isStatic(modifiers) || !type.isAssignableFrom(field.getType())) {
                continue;
            }
            try {
                T value = type.cast(field.get(null));
                String name = field.getName().toLowerCase(Locale.ROOT);

                valuesByName.put(name, value);
                namesByValue.putIfAbsent(value, name);
            } catch (IllegalAccessException exception) {
                throw new ExceptionInInitializerError(exception);
            }
        }
        return Codec.STRING.flatXmap(
                name -> {
                    T value = valuesByName.get(name);
                    return value != null ? DataResult.success(value) : DataResult.error(() -> "Unknown " + type.getSimpleName() + ": " + name);
                },
                value -> {
                    String name = namesByValue.get(value);
                    return name != null ? DataResult.success(name) : DataResult.error(() -> "Unregistered " + type.getSimpleName() + ": " + value
                    );
                }
        );
    }

    private static <I, T, R> R decode(Codec<T> codec, Dynamic<I> raw, Supplier<? extends RegistryAccess> registries, Function<? super T, ? extends R> mapper) {
        DynamicOps<I> ops = RegistryOps.create(raw.getOps(), registries.get());
        return mapper.apply(codec.parse(ops, raw.getValue()).getOrThrow());
    }

    private static <T, R> Supplier<R> serializableSupplier(
            Dynamic<?> raw,
            Codec<T> codec,
            Supplier<? extends RegistryAccess> registries,
            Function<? super T, ? extends R> mapper
    ) {
        return new SerializableSupplier<>(raw, () -> decodeUnchecked(codec, raw, registries, mapper));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static <T, R> R decodeUnchecked(Codec<T> codec, Dynamic<?> raw, Supplier<? extends RegistryAccess> registries, Function<? super T, ? extends R> mapper) {
        return (R) decode(codec, (Dynamic) raw, registries, mapper);
    }

    private record SerializableSupplier<T>(Dynamic<?> raw, Supplier<T> delegate) implements Supplier<T> {
        @Override
        public T get() {
            return delegate.get();
        }
    }
}
