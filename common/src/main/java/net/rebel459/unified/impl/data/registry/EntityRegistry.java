package net.rebel459.unified.impl.data.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.rebel459.unified.Unified;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.api.codec.ExtensibleCodecs;
import net.rebel459.unified.api.codec.ExtensibleEntityCodec;
import net.rebel459.unified.api.codec.ExtensibleSpawnPredicate;
import net.rebel459.unified.api.core.*;
import net.rebel459.unified.impl.data.helper.MobVariants;

import java.util.Optional;

public class EntityRegistry extends RegistryResourceListener<EntityRegistry.Definition> {

    public static final Codec<Definition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtensibleCodecs.ENTITY.mapCodec().forGetter(Definition::type),
            CombinedProperties.CODEC.codec().optionalFieldOf("properties", CombinedProperties.EMPTY)
                    .forGetter(definition -> new CombinedProperties(definition.properties(), definition.variantProperties()))
    ).apply(instance, (type, properties) -> new Definition(type, properties.properties(), properties.variant())));

    public static final Identifier ID = Unified.id("entities");

    public EntityRegistry() {
        super(ID, CODEC, SoundEventRegistry.ID);
    }

    @Override
    protected void register(Identifier id, DeferredDeclaration<Definition> declaration) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, id);

        DataRegistry.register(Registries.ENTITY_TYPE, id, () -> {
            Definition definition = declaration.get();
            EntityCopier.setDefaultVariant(key, definition.variantProperties());

            Supplied<? extends EntityType<?>> entity = UnifiedRegistries.EntityTypes.create(id.getNamespace()).register(id.getPath(), definition.type().get().builder(key));

            createLateProperties(entity, definition.properties());
            return entity;
        });
    }

    public record Definition(ExtensibleCodec.Entry<ExtensibleEntityCodec.Factory> type, Properties properties, MobVariants.Variant variantProperties) {}

    public record Properties(Optional<SpawnPlacement> spawnPlacement) {
        private static final MapCodec<Properties> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                SpawnPlacement.CODEC.optionalFieldOf("spawn_placement").forGetter(Properties::spawnPlacement)
        ).apply(instance, Properties::new));
    }

    private record CombinedProperties(Properties properties, MobVariants.Variant variant) {
        private static final CombinedProperties EMPTY = new CombinedProperties(
                new Properties(Optional.empty()),
                MobVariants.Variant.DEFAULT_PROPERTIES
        );

        private static final MapCodec<CombinedProperties> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Properties.CODEC.forGetter(CombinedProperties::properties),
                MobVariants.Variant.PROPERTIES_CODEC.forGetter(CombinedProperties::variant)
        ).apply(instance, CombinedProperties::new));
    }

    public record SpawnPlacement(ExtensibleCodec.Entry<SpawnPlacementType> placementType, Heightmap.Types heightmap, ExtensibleCodec.Entry<ExtensibleSpawnPredicate.Type> spawnPredicate) {
        private static final Codec<SpawnPlacement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ExtensibleCodecs.SPAWN_PLACEMENT.codec().fieldOf("placement_type").forGetter(SpawnPlacement::placementType),
                Heightmap.Types.CODEC.fieldOf("heightmap").forGetter(SpawnPlacement::heightmap),
                ExtensibleCodecs.SPAWN_PREDICATE.codec().fieldOf("spawn_predicate").forGetter(SpawnPlacement::spawnPredicate)
        ).apply(instance, SpawnPlacement::new));
    }

    private static <T extends Mob> void createLateProperties(Supplied<? extends EntityType<?>> entity, Properties properties) {
        if (properties.spawnPlacement.isPresent()) {
            SpawnPlacement spawnPlacement = properties.spawnPlacement.get();
            UnifiedHelpers.SPAWN_PLACEMENTS.register((Supplied<EntityType<T>>) entity, spawnPlacement.placementType.get(), spawnPlacement.heightmap, spawnPlacement.spawnPredicate.get()::test);
        }
    }
}
