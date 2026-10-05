package net.rebel459.unified.impl.data.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.flag.FeatureFlags;
import net.rebel459.unified.impl.data.helper.MobVariants;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class EntityCopier {
    private static final Map<ResourceKey<EntityType<?>>, Declaration> DECLARATIONS = new LinkedHashMap<>();
    private static final Map<EntityType<?>, EntityType<?>> TEMPLATES = new LinkedHashMap<>();
    private static final Map<EntityType<?>, ResourceKey<MobVariants.Definition>> DEFAULT_VARIANTS = new LinkedHashMap<>();
    private static final Map<EntityType<?>, MobVariants.Definition> INLINE_DEFAULT_VARIANTS = new LinkedHashMap<>();
    private static final Map<ResourceKey<EntityType<?>>, ResourceKey<MobVariants.Definition>> PENDING_DEFAULT_VARIANTS = new LinkedHashMap<>();
    private static final Map<ResourceKey<EntityType<?>>, MobVariants.Definition> PENDING_INLINE_DEFAULT_VARIANTS = new LinkedHashMap<>();
    private static final Set<ResourceKey<EntityType<?>>> PENDING_ATTRIBUTE_OVERRIDES = new LinkedHashSet<>();
    private static final Set<EntityType<?>> ATTRIBUTE_OVERRIDES = new LinkedHashSet<>();
    private static final Set<EntityType<?>> SPAWN_PLACEMENT_OVERRIDES = new LinkedHashSet<>();

    private EntityCopier() {}

    public static EntityType<?> create(ResourceKey<EntityType<?>> key, ResourceKey<EntityType<?>> base, Identifier defaultVariant) {
        PENDING_DEFAULT_VARIANTS.put(key, ResourceKey.create(MobVariants.KEY, defaultVariant));
        EntityType<?> entityType = create(key, base);
        return entityType;
    }

    public static EntityType<?> create(ResourceKey<EntityType<?>> key, ResourceKey<EntityType<?>> base, MobVariants.Definition defaultVariant) {
        setDefaultVariant(key, defaultVariant);
        EntityType<?> entityType = create(key, base);
        return entityType;
    }

    public static void setDefaultVariant(ResourceKey<EntityType<?>> entityType, MobVariants.Definition defaultVariant) {
        PENDING_INLINE_DEFAULT_VARIANTS.put(entityType, defaultVariant);
    }

    public static void markAttributeOverride(ResourceKey<EntityType<?>> entityType) {
        PENDING_ATTRIBUTE_OVERRIDES.add(entityType);
    }

    public static void markSpawnPlacementOverride(EntityType<?> entityType) {
        SPAWN_PLACEMENT_OVERRIDES.add(entityType);
    }

    public static boolean hasAttributeOverride(EntityType<?> entityType) {
        return ATTRIBUTE_OVERRIDES.contains(entityType);
    }

    public static boolean hasSpawnPlacementOverride(EntityType<?> entityType) {
        return SPAWN_PLACEMENT_OVERRIDES.contains(entityType);
    }

    public static void declare(ResourceKey<EntityType<?>> key, ResourceKey<EntityType<?>> base) {
        Declaration previous = DECLARATIONS.put(key, new Declaration(base));
        if (previous != null) throw new IllegalStateException("Duplicate copied entity declaration for " + key.identifier());
    }

    public static EntityType<?> create(ResourceKey<EntityType<?>> key, ResourceKey<EntityType<?>> base) {
        EntityType<?> entityType = placeholder(key);
        declare(key, base);
        onRegistered(key, entityType);
        return entityType;
    }

    public static void onRegistered(ResourceKey<EntityType<?>> key, EntityType<?> entityType) {
        if (PENDING_ATTRIBUTE_OVERRIDES.remove(key)) ATTRIBUTE_OVERRIDES.add(entityType);
        ResourceKey<MobVariants.Definition> defaultVariant = PENDING_DEFAULT_VARIANTS.remove(key);
        if (defaultVariant != null) DEFAULT_VARIANTS.put(entityType, defaultVariant);
        MobVariants.Definition inlineDefaultVariant = PENDING_INLINE_DEFAULT_VARIANTS.remove(key);
        if (inlineDefaultVariant != null) INLINE_DEFAULT_VARIANTS.put(entityType, inlineDefaultVariant);

        Declaration declaration = DECLARATIONS.get(key);
        if (declaration != null) {
            if (declaration.entityType != null && declaration.entityType != entityType) {
                throw new IllegalStateException("Copied entity type registered twice: " + key.identifier());
            }
            declaration.entityType = entityType;
            resolve(key);
        } else {
            resolveDependants(key, entityType);
        }
    }

    public static Optional<EntityType<?>> template(EntityType<?> entityType) {
        return Optional.ofNullable(TEMPLATES.get(entityType));
    }

    public static Optional<ResourceKey<MobVariants.Definition>> defaultVariant(EntityType<?> entityType) {
        return Optional.ofNullable(DEFAULT_VARIANTS.get(entityType));
    }

    public static Optional<Holder<MobVariants.Definition>> resolveDefaultVariant(EntityType<?> entityType, HolderGetter<MobVariants.Definition> variants) {
        ResourceKey<MobVariants.Definition> key = DEFAULT_VARIANTS.get(entityType);
        if (key != null) return variants.get(key).map(holder -> holder);
        return Optional.ofNullable(INLINE_DEFAULT_VARIANTS.get(entityType)).map(Holder::direct);
    }

    public static boolean isDefaultVariant(EntityType<?> entityType, Holder<MobVariants.Definition> variant) {
        ResourceKey<MobVariants.Definition> key = DEFAULT_VARIANTS.get(entityType);
        if (key != null) return variant.is(key);
        MobVariants.Definition inline = INLINE_DEFAULT_VARIANTS.get(entityType);
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
                BlockTags.ANIMALS_SPAWNABLE_ON,
                EntityDimensions.fixed(0.6F, 1.8F),
                1F,
                5,
                3,
                Util.makeDescriptionId("entity", id),
                Optional.of(ResourceKey.create(Registries.LOOT_TABLE, id.withPrefix("entities/"))),
                FeatureFlags.VANILLA_SET,
                true,
                false
        );
    }

    private static void resolve(ResourceKey<EntityType<?>> key) {
        Declaration declaration = DECLARATIONS.get(key);
        if (declaration == null || declaration.entityType == null || declaration.resolved) return;

        Declaration baseDeclaration = DECLARATIONS.get(declaration.base);
        if (baseDeclaration != null && !baseDeclaration.resolved) return;

        EntityType<?> base = BuiltInRegistries.ENTITY_TYPE.getValue(declaration.base);
        if (base != null) hydrate(key, declaration, base);
    }

    private static void resolveDependants(ResourceKey<EntityType<?>> registeredKey, EntityType<?> registeredType) {
        for (Map.Entry<ResourceKey<EntityType<?>>, Declaration> entry : DECLARATIONS.entrySet()) {
            Declaration declaration = entry.getValue();
            if (declaration.entityType != null && !declaration.resolved && declaration.base.equals(registeredKey)) {
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
        private EntityType<?> entityType;
        private final ResourceKey<EntityType<?>> base;
        private boolean resolved;

        private Declaration(ResourceKey<EntityType<?>> base) {
            this.base = base;
        }
    }
}
