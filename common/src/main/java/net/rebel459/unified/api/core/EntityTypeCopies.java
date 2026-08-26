package net.rebel459.unified.util.registry;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.storage.loot.LootTable;
import net.rebel459.unified.util.data.MobVariants;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class EntityTypeCopies {
    private static final Map<ResourceKey<EntityType<?>>, Declaration> DECLARATIONS = new LinkedHashMap<>();
    private static final Map<EntityType<?>, EntityType<?>> TEMPLATES = new LinkedHashMap<>();
    private static final Map<EntityType<?>, ResourceKey<MobVariants.Variant>> DEFAULT_VARIANTS = new LinkedHashMap<>();
    private static final Map<EntityType<?>, MobVariants.Variant> INLINE_DEFAULT_VARIANTS = new LinkedHashMap<>();

    private EntityTypeCopies() {}

    public static EntityType<?> create(ResourceKey<EntityType<?>> key, ResourceKey<EntityType<?>> base, Identifier defaultVariant) {
        EntityType<?> entityType = create(key, base);
        DEFAULT_VARIANTS.put(entityType, ResourceKey.create(MobVariants.KEY, defaultVariant));
        return entityType;
    }

    public static EntityType<?> create(ResourceKey<EntityType<?>> key, ResourceKey<EntityType<?>> base, MobVariants.Variant defaultVariant) {
        EntityType<?> entityType = create(key, base);
        INLINE_DEFAULT_VARIANTS.put(entityType, defaultVariant);
        return entityType;
    }

    private static EntityType<?> create(ResourceKey<EntityType<?>> key, ResourceKey<EntityType<?>> base) {
        EntityType<?> entityType = placeholder(key);
        Declaration previous = DECLARATIONS.put(key, new Declaration(entityType, base));
        if (previous != null) throw new IllegalStateException("Duplicate copied entity declaration for " + key.identifier());
        resolve(key);
        return entityType;
    }

    public static void onRegistered(ResourceKey<EntityType<?>> key, EntityType<?> entityType) {
        resolveDependants(key, entityType);
    }

    public static Optional<EntityType<?>> template(EntityType<?> entityType) {
        return Optional.ofNullable(TEMPLATES.get(entityType));
    }

    public static Optional<ResourceKey<MobVariants.Variant>> defaultVariant(EntityType<?> entityType) {
        return Optional.ofNullable(DEFAULT_VARIANTS.get(entityType));
    }

    public static Optional<Holder<MobVariants.Variant>> resolveDefaultVariant(EntityType<?> entityType, HolderGetter<MobVariants.Variant> variants) {
        ResourceKey<MobVariants.Variant> key = DEFAULT_VARIANTS.get(entityType);
        if (key != null) return variants.get(key).map(holder -> holder);
        return Optional.ofNullable(INLINE_DEFAULT_VARIANTS.get(entityType)).map(Holder::direct);
    }

    public static boolean isDefaultVariant(EntityType<?> entityType, Holder<MobVariants.Variant> variant) {
        ResourceKey<MobVariants.Variant> key = DEFAULT_VARIANTS.get(entityType);
        if (key != null) return variant.is(key);
        MobVariants.Variant inline = INLINE_DEFAULT_VARIANTS.get(entityType);
        return inline != null && inline.equals(variant.value());
    }

    public static Map<EntityType<?>, EntityType<?>> templates() {
        return Map.copyOf(TEMPLATES);
    }

    public static void validateResolved() {
        List<String> unresolved = DECLARATIONS.entrySet().stream()
                .filter(entry -> !entry.getValue().resolved)
                .map(entry -> entry.getKey().identifier() + " (base " + entry.getValue().base.identifier() + ")")
                .toList();
        if (!unresolved.isEmpty()) {
            throw new IllegalStateException("Copied entity types have unregistered bases: " + String.join(", ", unresolved));
        }
    }

    private static EntityType<?> placeholder(ResourceKey<EntityType<?>> key) {
        Identifier id = key.identifier();
        return new EntityType<>(
                (type, level) -> null,
                MobCategory.MISC,
                false,
                false,
                false,
                false,
                ImmutableSet.of(),
                EntityDimensions.fixed(0.6F, 1.8F),
                1F,
                5,
                3,
                Util.makeDescriptionId("entity", id),
                Optional.of(ResourceKey.create(Registries.LOOT_TABLE, id.withPrefix("entities/"))),
                FeatureFlags.VANILLA_SET,
                true
        );
    }

    private static void resolve(ResourceKey<EntityType<?>> key) {
        Declaration declaration = DECLARATIONS.get(key);
        if (declaration == null || declaration.resolved) return;

        Declaration baseDeclaration = DECLARATIONS.get(declaration.base);
        if (baseDeclaration != null && !baseDeclaration.resolved) return;

        EntityType<?> base = BuiltInRegistries.ENTITY_TYPE.getValue(declaration.base);
        if (base != null) hydrate(key, declaration, base);
    }

    private static void resolveDependants(ResourceKey<EntityType<?>> registeredKey, EntityType<?> registeredType) {
        for (Map.Entry<ResourceKey<EntityType<?>>, Declaration> entry : DECLARATIONS.entrySet()) {
            Declaration declaration = entry.getValue();
            if (!declaration.resolved && declaration.base.equals(registeredKey)) {
                Declaration baseDeclaration = DECLARATIONS.get(registeredKey);
                if (baseDeclaration == null || baseDeclaration.resolved) hydrate(entry.getKey(), declaration, registeredType);
            }
        }
    }

    @SuppressWarnings({"rawtypes"})
    private static void hydrate(ResourceKey<EntityType<?>> key, Declaration declaration, EntityType<?> base) {
        EntityType target = declaration.entityType;
        target.factory = base.factory;
        target.category = base.category;
        target.immuneTo = base.immuneTo;
        target.serialize = base.serialize;
        target.summon = base.summon;
        target.fireImmune = base.fireImmune;
        target.canSpawnFarFromPlayer = base.canSpawnFarFromPlayer;
        target.clientTrackingRange = base.clientTrackingRange;
        target.updateInterval = base.updateInterval;
        target.dimensions = base.dimensions;
        target.spawnDimensionsScale = base.spawnDimensionsScale;
        target.requiredFeatures = base.requiredFeatures;
        target.allowedInPeaceful = base.allowedInPeaceful;
        if (target.serialize) Util.fetchChoiceType(References.ENTITY_TREE, key.identifier().toString());

        declaration.resolved = true;
        EntityType<?> root = TEMPLATES.getOrDefault(base, base);
        TEMPLATES.put(declaration.entityType, root);
        resolveDependants(key, declaration.entityType);
    }

    private static final class Declaration {
        private final EntityType<?> entityType;
        private final ResourceKey<EntityType<?>> base;
        private boolean resolved;

        private Declaration(EntityType<?> entityType, ResourceKey<EntityType<?>> base) {
            this.entityType = entityType;
            this.base = base;
        }
    }
}
