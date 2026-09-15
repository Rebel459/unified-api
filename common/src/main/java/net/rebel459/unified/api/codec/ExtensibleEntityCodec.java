package net.rebel459.unified.api.codec;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ExtensibleEntityCodec extends ExtensibleCodec<ExtensibleEntityCodec.Factory> {
    private final Map<Identifier, List<ResourceKey<EntityType<?>>>> entities = new LinkedHashMap<>();
    private final Map<Identifier, Binding<?>> bindings = new LinkedHashMap<>();

    public <E extends Entity> Simple<E> registerSimple(Identifier id, Supplier<? extends EntityType.Builder<E>> builder) {
        return registerSimpleType(new Simple<>(this, id, builder));
    }

    public <E extends Entity, T> Complex<E, T> registerComplex(Identifier id, MapCodec<T> codec, Function<T, ? extends EntityType.Builder<E>> builder) {
        return registerComplexType(new Complex<>(this, id, codec, builder));
    }

    public <E extends Entity> void bind(ResourceKey<EntityType<?>> key, Consumer<Supplier<EntityType<? extends E>>> renderer) {
        bind(Identifier.fromNamespaceAndPath(key.identifier().getNamespace(), "entities/" + key.identifier().getPath()), renderer);
    }

    private <E extends Entity> void bind(Identifier id, Consumer<Supplier<EntityType<? extends E>>> renderer) {
        Binding<E> binding = new Binding<>(renderer);
        if (bindings.putIfAbsent(id, binding) != null) {
            throw new IllegalArgumentException("Duplicate binding for entity codec type " + id);
        }
        entities.getOrDefault(id, List.of()).forEach(binding::bind);
    }

    private void track(Identifier id, ResourceKey<EntityType<?>> entity) {
        entities.computeIfAbsent(id, ignored -> new ArrayList<>()).add(entity);
        Binding<?> binding = bindings.get(id);
        if (binding != null) binding.bind(entity);
    }

    @FunctionalInterface
    public interface Factory {
        EntityType.Builder<?> builder(ResourceKey<EntityType<?>> key);
    }

    public static final class Simple<E extends Entity> extends ExtensibleCodec.Simple<Factory> {
        private final ExtensibleEntityCodec owner;

        private Simple(ExtensibleEntityCodec owner, Identifier id, Supplier<? extends EntityType.Builder<E>> builder) {
            super(id, () -> key -> {
                owner.track(id, key);
                return builder.get();
            });
            this.owner = owner;
        }

        public void bind(Consumer<Supplier<EntityType<? extends E>>> renderer) {
            owner.bind(id(), renderer);
        }
    }

    public static final class Complex<E extends Entity, T> extends ExtensibleCodec.Complex<Factory, T> {
        private final ExtensibleEntityCodec owner;

        private Complex(ExtensibleEntityCodec owner, Identifier id, MapCodec<T> codec, Function<T, ? extends EntityType.Builder<E>> builder) {
            super(id, codec, data -> key -> {
                owner.track(id, key);
                return builder.apply(data);
            });
            this.owner = owner;
        }

        public void bind(Consumer<Supplier<EntityType<? extends E>>> renderer) {
            owner.bind(id(), renderer);
        }
    }

    private record Binding<E extends Entity>(Consumer<Supplier<EntityType<? extends E>>> renderer) {
        @SuppressWarnings("unchecked")
        private void bind(ResourceKey<EntityType<?>> entity) {
            renderer.accept(() -> (EntityType<? extends E>) BuiltInRegistries.ENTITY_TYPE.getValue(entity));
        }
    }
}
