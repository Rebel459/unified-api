package net.rebel459.unified.impl.data.registry;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.rebel459.unified.Unified;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.api.codec.ExtensibleCodecs;
import net.rebel459.unified.api.codec.ExtensibleSpawnPredicate;
import net.rebel459.unified.api.core.*;
import net.rebel459.unified.impl.data.helper.MobVariants;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class EntityRegistry extends RegistryResourceListener<EntityRegistry.Definition> {

    private static final Codec<Definition> TYPE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtensibleCodecs.ENTITY.mapCodec().forGetter(definition -> ((CodecBase) definition.base()).type()),
            CombinedProperties.CODEC.codec().optionalFieldOf("properties", CombinedProperties.EMPTY)
                    .forGetter(definition -> new CombinedProperties(definition.properties(), definition.variantProperties()))
    ).apply(instance, (type, properties) -> new Definition(new CodecBase(type), properties.properties(), properties.variant())));

    private static final Codec<Definition> COPY_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(Registries.ENTITY_TYPE).fieldOf("base").forGetter(definition -> ((CopiedBase) definition.base()).entity()),
            CombinedProperties.CODEC.codec().optionalFieldOf("properties", CombinedProperties.EMPTY)
                    .forGetter(definition -> new CombinedProperties(definition.properties(), definition.variantProperties()))
    ).apply(instance, (base, properties) -> new Definition(new CopiedBase(base), properties.properties(), properties.variant())));

    public static final Codec<Definition> CODEC = Codec.either(TYPE_CODEC, COPY_CODEC).xmap(
            value -> value.map(definition -> definition, definition -> definition),
            definition -> definition.base() instanceof CodecBase ? Either.left(definition) : Either.right(definition)
    );

    public static final Identifier ID = Unified.id("entities");

    public EntityRegistry() {
        super(ID, CODEC, Registries.ENTITY_TYPE, BlockRegistry.ID);
    }

    @Override
    protected void register(Identifier id, DeferredDeclaration<Definition> declaration) {
        StagedRegistry.register(Registries.ENTITY_TYPE, id, () -> {
            boolean hasAttributes = declaration.decode(Codec.PASSTHROUGH.optionalFieldOf("default_attributes").codec()
                    .optionalFieldOf("properties", Optional.empty())).isPresent();
            Optional<Supplier<Attributes>> attributes = hasAttributes
                    ? Optional.of(() -> declaration.get().properties().attributes.orElseThrow())
                    : Optional.empty();
            Supplied<? extends EntityType<?>> entity = registerDefinition(id, declaration, attributes);
            UnifiedPlatform.executeAfter(Registries.ENTITY_TYPE, () -> createLateProperties(entity, declaration.get().properties()));
            return entity;
        });
    }

    public static Supplied<? extends EntityType<?>> registerDefinition(Identifier id, Supplier<Definition> suppliedDefinition, Optional<Supplier<Attributes>> optionalAttributes) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, id);
        Supplier<Definition> definition = Suppliers.memoize(suppliedDefinition::get);
        Supplier<EntityType.Builder<?>> builder = () -> {
            Definition value = definition.get();
            EntityCopier.setDefaultVariant(key, value.variantProperties());
            EntityType.Builder<?> entityBuilder = switch (value.base()) {
                case CodecBase(ExtensibleCodec.Entry<EntityType.Builder<?>> type) -> ExtensibleCodecs.ENTITY.create(type, key);
                case CopiedBase(ResourceKey<EntityType<?>> base) -> {
                    EntityCopier.declare(key, base);
                    yield EntityType.Builder.createNothing(MobCategory.MISC);
                }
            };
            return entityBuilder;
        };
        UnifiedRegistries.EntityTypes registry = UnifiedRegistries.EntityTypes.create(id.getNamespace());
        if (optionalAttributes.isPresent()) {
            EntityCopier.markAttributeOverride(key);
            return registry.register(id.getPath(), () -> (EntityType.Builder<LivingEntity>) builder.get(),
                    () -> createAttributes(optionalAttributes.get().get()));
        }
        return registry.register(id.getPath(), () -> (EntityType.Builder<Entity>) builder.get());
    }

    private static AttributeSupplier createAttributes(Attributes attributes) {
        AttributeSupplier.Builder builder = AttributeSupplier.builder();
        Set<Holder<Attribute>> overrides = attributes.values().stream().map(AttributeEntry::attribute).collect(Collectors.toSet());

        if (attributes.baseAttributes.isPresent()) {
            AttributeSupplier base = DefaultAttributes.getSupplier((EntityType<? extends LivingEntity>) BuiltInRegistries.ENTITY_TYPE.getValue(attributes.baseAttributes.get()));
            BuiltInRegistries.ATTRIBUTE.listElements().filter(base::hasAttribute).filter(attribute -> !overrides.contains(attribute)).forEach(attribute -> builder.add(attribute, base.getBaseValue(attribute)));
        }

        for (AttributeEntry entry : attributes.values()) {
            if (entry.value().isPresent()) builder.add(entry.attribute(), entry.value().get());
            else builder.add(entry.attribute());
        }
        return builder.build();
    }

    public sealed interface Base permits CodecBase, CopiedBase {}

    public record CodecBase(ExtensibleCodec.Entry<EntityType.Builder<?>> type) implements Base {}

    public record CopiedBase(ResourceKey<EntityType<?>> entity) implements Base {}

    public record Definition(Base base, Properties properties, MobVariants.Definition variantProperties) {}

    public static final class Properties {
        public Optional<SpawnPlacement> spawnPlacement = Optional.empty();
        public Optional<Attributes> attributes = Optional.empty();

        public Properties() {}

        public static final MapCodec<Properties> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                SpawnPlacement.CODEC.optionalFieldOf("spawn_placement").forGetter(properties -> properties.spawnPlacement),
                Attributes.CODEC.optionalFieldOf("default_attributes").forGetter(properties -> properties.attributes)
        ).apply(instance, Properties::new));

        private Properties(Optional<SpawnPlacement> spawnPlacement, Optional<Attributes> attributes) {
            this.spawnPlacement = spawnPlacement;
            this.attributes = attributes;
        }
    }

    private record CombinedProperties(Properties properties, MobVariants.Definition variant) {
        private static final CombinedProperties EMPTY = new CombinedProperties(
                new Properties(),
                MobVariants.Definition.EMPTY
        );

        private static final MapCodec<CombinedProperties> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Properties.MAP_CODEC.forGetter(CombinedProperties::properties),
                MobVariants.Definition.PROPERTIES_CODEC.forGetter(CombinedProperties::variant)
        ).apply(instance, CombinedProperties::new));
    }

    public record SpawnPlacement(ExtensibleCodec.Entry<SpawnPlacementType> placementType, Heightmap.Types heightmap, ExtensibleCodec.Entry<ExtensibleSpawnPredicate.Type> spawnPredicate) {
        private static final Codec<SpawnPlacement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ExtensibleCodecs.SPAWN_PLACEMENT.codec().fieldOf("placement_type").forGetter(SpawnPlacement::placementType),
                Heightmap.Types.CODEC.fieldOf("heightmap").forGetter(SpawnPlacement::heightmap),
                ExtensibleCodecs.SPAWN_PREDICATE.codec().fieldOf("spawn_predicate").forGetter(SpawnPlacement::spawnPredicate)
        ).apply(instance, SpawnPlacement::new));
    }

    public record Attributes(Optional<ResourceKey<EntityType<?>>> baseAttributes, List<AttributeEntry> values) {
        public static final Codec<Attributes> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        ResourceKey.codec(Registries.ENTITY_TYPE).optionalFieldOf("copy_from").forGetter(Attributes::baseAttributes),
                        AttributeEntry.CODEC.listOf().optionalFieldOf("attributes", List.of()).forGetter(Attributes::values)
                ).apply(instance, Attributes::new));
    }

    public record AttributeEntry(Holder<Attribute> attribute, Optional<Double> value) {
        private static final Codec<AttributeEntry> FULL_CODEC =
                RecordCodecBuilder.create(instance -> instance.group(
                        Attribute.CODEC.fieldOf("attribute").forGetter(AttributeEntry::attribute),
                        Codec.DOUBLE.optionalFieldOf("value").forGetter(AttributeEntry::value)
                ).apply(instance, AttributeEntry::new));

        public static final Codec<AttributeEntry> CODEC = Codec.either(Attribute.CODEC, FULL_CODEC).xmap(
                entry -> entry.map(attribute -> new AttributeEntry(attribute, Optional.empty()), value -> value),
                entry -> entry.value().isEmpty() ? Either.left(entry.attribute()) : Either.right(entry)
        );
    }

    private static <T extends Mob> void createLateProperties(Supplied<? extends EntityType<?>> entity, Properties properties) {
        if (properties.spawnPlacement.isPresent()) {
            SpawnPlacement spawnPlacement = properties.spawnPlacement.get();
            EntityCopier.markSpawnPlacementOverride(entity.get());
            UnifiedHelpers.SPAWN_PLACEMENTS.register((Supplied<EntityType<T>>) entity, spawnPlacement.placementType.get(), spawnPlacement.heightmap, spawnPlacement.spawnPredicate.get()::test);
        }
    }
}
