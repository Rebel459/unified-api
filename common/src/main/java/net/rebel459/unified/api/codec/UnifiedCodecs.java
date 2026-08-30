package net.rebel459.unified.api.codec;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.rebel459.unified.api.core.Supplied;
import net.rebel459.unified.api.registry.UnifiedRequirementTypes;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public final class UnifiedCodecs {

    public static final MapCodec<Optional<ExtensibleCodec.Entry<Boolean>>> LOAD_REQUIREMENTS = Codec.either(
            Codec.BOOL,
            ExtensibleCodecs.REQUIREMENT_TYPES.codec()
    ).xmap(either -> either.map(
            value -> (value ? UnifiedRequirementTypes.ALWAYS : UnifiedRequirementTypes.NEVER).create(),
            entry -> entry
    ), Either::right).optionalFieldOf("load_requirements");

    /** Wraps a completed codec to make it safely configurable */
    public static <T> Codec<T> loadRequirements(Codec<T> codec, Supplier<? extends T> disabled) {
        Objects.requireNonNull(codec, "codec");
        Objects.requireNonNull(disabled, "disabled");

        Decoder<T> decoder = new Decoder<>() {
            @Override
            public <O> DataResult<Pair<T, O>> decode(DynamicOps<O> ops, O input) {
                return LOAD_REQUIREMENTS.codec().parse(ops, input).flatMap(requirement -> {
                    if (!requirement.map(ExtensibleCodec.Entry::get).orElse(true)) {
                        return DataResult.success(Pair.of(
                                Objects.requireNonNull(disabled.get(), "disabled value"),
                                ops.empty()
                        ));
                    }

                    O definition = ops.remove(input, "load_requirements");
                    return codec.decode(ops, definition);
                });
            }
        };

        return Codec.of(codec, decoder);
    }

    /** Safely defers registry objects to their corresponding identifier */
    public static <T> Codec<Supplier<T>> supplied(Registry<T> registry) {
        return Identifier.CODEC.flatXmap(
                id -> DataResult.success(new RegistrySupplier<>(registry, ResourceKey.create(registry.key(), id))),
                supplier -> {
                    if (supplier instanceof RegistrySupplier<?> reference) {
                        return DataResult.success(reference.key.identifier());
                    }
                    if (supplier instanceof Supplied<?> supplied) {
                        return DataResult.success(supplied.id());
                    }
                    try {
                        T value = supplier.get();
                        Identifier id = registry.getKey(value);
                        return id != null ? DataResult.success(id) : DataResult.error(() -> "Value supplied for " + registry.key().identifier() + " is not registered");
                    } catch (RuntimeException exception) {
                        return DataResult.error(() -> "Could not resolve value supplied for " + registry.key().identifier() + ": " + exception.getMessage());
                    }
                }
        );
    }

    private record RegistrySupplier<T>(Registry<T> registry, ResourceKey<T> key) implements Supplier<T> {
        @Override
        public T get() {
            return registry.getValueOrThrow(key);
        }
    }

    public static final Codec<BlockSetType> BLOCK_SET_TYPE = Identifier.CODEC.flatXmap(
            id -> registered(BlockSetType.TYPES, id, "block set type"),
            type -> DataResult.success(Identifier.parse(type.name()))
    );

    public static final Codec<WoodType> WOOD_TYPE = Identifier.CODEC.flatXmap(
            id -> registered(WoodType.TYPES, id, "wood type"),
            type -> DataResult.success(Identifier.parse(type.name()))
    );

    private static <T> DataResult<T> registered(Map<String, T> values, Identifier id, String type) {
        String name = id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE) ? id.getPath() : id.toString();
        T value = values.get(name);
        return value != null ? DataResult.success(value) : DataResult.error(() -> "Unknown " + type + ": " + id);
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
}
