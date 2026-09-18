package net.rebel459.unified.api.codec;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ExtensibleEntityCodec extends ExtensibleCodecBase<EntityType.Builder<?>> {
    private final Map<Identifier, Set<ResourceKey<EntityType<?>>>> entities = new LinkedHashMap<>();
    private final Map<Identifier, Binding<?>> bindings = new LinkedHashMap<>();

    public <E extends Entity> void bind(ResourceKey<EntityType<?>> key, BiConsumer<Supplier<EntityType<? extends E>>, Identifier> consumer) {
        bind(Identifier.fromNamespaceAndPath(key.identifier().getNamespace(), "entities/" + key.identifier().getPath()), consumer);
    }

    public <E extends Entity> Simple<E> register(Identifier id, Supplier<? extends EntityType.Builder<E>> builder) {
        return registerSimpleType(new Simple<>(this, id, builder));
    }

    public <E extends Entity, T> Complex<E, T> register(Identifier id, MapCodec<T> codec, Function<T, ? extends EntityType.Builder<E>> builder) {
        return registerComplexType(new Complex<>(this, id, codec, builder));
    }

    private <E extends Entity> void bind(Identifier id, BiConsumer<Supplier<EntityType<? extends E>>, Identifier> consumer) {
        Binding<E> binding = new Binding<>(consumer);
        if (bindings.putIfAbsent(id, binding) != null) {
            throw new IllegalArgumentException("Duplicate binding for entity codec type " + id);
        }
        entities.getOrDefault(id, Set.of()).forEach(binding::bind);
    }

    private void track(Identifier id, ResourceKey<EntityType<?>> entity) {
        if (entities.computeIfAbsent(id, ignored -> new LinkedHashSet<>()).add(entity)) {
            Binding<?> binding = bindings.get(id);
            if (binding != null) binding.bind(entity);
        }
    }

    public EntityType.Builder<?> create(Entry<EntityType.Builder<?>> type, ResourceKey<EntityType<?>> key) {
        track(type.id(), key);
        return type.get();
    }

    public static final class Simple<E extends Entity> extends ExtensibleCodec.Simple<EntityType.Builder<?>> {
        private final ExtensibleEntityCodec owner;

        private Simple(ExtensibleEntityCodec owner, Identifier id, Supplier<? extends EntityType.Builder<E>> builder) {
            super(id, builder);
            this.owner = owner;
        }

        public void bind(BiConsumer<Supplier<EntityType<? extends E>>, Identifier> consumer) {
            owner.bind(id(), consumer);
        }
    }

    public static final class Complex<E extends Entity, T> extends ExtensibleCodec.Complex<EntityType.Builder<?>, T> {
        private final ExtensibleEntityCodec owner;

        private Complex(ExtensibleEntityCodec owner, Identifier id, MapCodec<T> codec, Function<T, ? extends EntityType.Builder<E>> builder) {
            super(id, codec, builder);
            this.owner = owner;
        }

        public void bind(BiConsumer<Supplier<EntityType<? extends E>>, Identifier> consumer) {
            owner.bind(id(), consumer);
        }
    }

    private record Binding<E extends Entity>(BiConsumer<Supplier<EntityType<? extends E>>, Identifier> consumer) {
        @SuppressWarnings("unchecked")
        private void bind(ResourceKey<EntityType<?>> entity) {
            consumer.accept(() -> (EntityType<? extends E>) BuiltInRegistries.ENTITY_TYPE.getValue(entity), entity.identifier());
        }
    }
}
