package net.rebel459.unified.api.data.registry;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.variant.PriorityProvider;
import net.minecraft.world.entity.variant.SpawnCondition;
import net.minecraft.world.entity.variant.SpawnContext;
import net.minecraft.world.entity.variant.SpawnPrioritySelectors;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import net.minecraft.world.phys.AABB;
import net.rebel459.unified.api.asset.BlockAsset;
import net.rebel459.unified.api.asset.BlockAssets;
import net.rebel459.unified.api.codec.*;
import net.rebel459.unified.api.core.Supplied;
import net.rebel459.unified.api.core.SuppliedBlock;
import net.rebel459.unified.api.core.UnifiedRegistries;
import net.rebel459.unified.api.data.helper.HelperGenerator;
import net.rebel459.unified.api.util.RecipeProvider;
import net.rebel459.unified.impl.asset.BlockAssetRequest;
import net.rebel459.unified.impl.core.DataProviders;
import net.rebel459.unified.impl.data.helper.MobVariants;
import net.rebel459.unified.impl.data.registry.BlockRegistry;
import net.rebel459.unified.impl.data.registry.EntityRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.*;

public class EntityGenerator {

    private final String modId;
    private final String namespace;
    private final DataProviders.GenerationSettings settings;

    public EntityGenerator(String modId, String namespace, DataProviders.GenerationSettings settings) {
        this.modId = modId;
        this.namespace = namespace;
        this.settings = settings;
    }

    public String modId() {
        return modId;
    }

    public DataProviders.GenerationSettings settings() {
        return settings;
    }

    public Supplied<? extends EntityType<?>> register(String path, ExtensibleCodec.Entry<ExtensibleEntityCodec.Factory> type, Consumer<Builder> builder) {
        Builder finalBuilder = new Builder(namespace, path);
        builder.accept(finalBuilder);
        Identifier id = Identifier.fromNamespaceAndPath(namespace, path);
        Supplier<EntityRegistry.Definition> definition = () -> {
            Properties builderProperties = finalBuilder.properties;
            return new EntityRegistry.Definition(type, builderProperties.properties, new MobVariants.Definition(
                    Optional.empty(),
                    builderProperties.texture,
                    builderProperties.babyTexture,
                    new MobVariants.Sounds(builderProperties.ambientSound, builderProperties.hurtSound, builderProperties.eatSound, builderProperties.deathSound, builderProperties.stepSound),
                    SpawnPrioritySelectors.EMPTY,
                    1F,
                    List.of(),
                    builderProperties.attackEffects,
                    builderProperties.burnInDaylight,
                    Optional.empty()
            ));
        };
        Supplied<? extends EntityType<?>> entity = EntityRegistry.registerDefinition(id, definition, finalBuilder.properties.attributes);
        CodecGenerator.registry(modId, id, "entities", settings.metadata().priority(), settings.metadata().requirement(), EntityRegistry.CODEC, () -> {
            finalBuilder.properties.attributes.ifPresent(attributes -> finalBuilder.properties.properties.attributes = Optional.of(attributes.get()));
            return definition.get();
        });
        DataProviders.LANGUAGES.add(modId, new DataProviders.LanguageRequest(settings, Optional.of(new DataProviders.Translation(id, DataProviders.TranslationType.ENTITY, () -> finalBuilder.buildAssets().name()))));
        finalBuilder.registerData(modId, settings.metadata().requirement(), ResourceKey.create(Registries.ENTITY_TYPE, id), entity);
        return entity;
    }

    public static final class Properties {
        private final String modId;
        private final String path;

        private final EntityRegistry.Properties properties = new EntityRegistry.Properties();
        private Optional<Supplier<EntityRegistry.Attributes>> attributes = Optional.empty();

        private Optional<MobVariants.TextureReplacement> texture = Optional.empty();
        private Optional<MobVariants.TextureReplacement> babyTexture = Optional.empty();
        private Optional<SoundEvent> ambientSound = Optional.empty();
        private Optional<SoundEvent> hurtSound = Optional.empty();
        private Optional<SoundEvent> eatSound = Optional.empty();
        private Optional<SoundEvent> deathSound = Optional.empty();
        private Optional<SoundEvent> stepSound = Optional.empty();
        private final List<MobEffectInstance> attackEffects = new ArrayList<>();
        private Optional<Boolean> burnInDaylight = Optional.empty();

        private Properties(String modId, String path) {
            this.modId = modId;
            this.path = path;
        }

        private <R> ExtensibleCodec.Entry<R> register(ExtensibleCodec<R> codec, R value) {
            return codec.register(Identifier.fromNamespaceAndPath(modId, "entities/" + path), () -> value).create();
        }

        public Properties attributes(Supplier<AttributeSupplier> attributes) {
            this.attributes = Optional.of(() -> {
                AttributeSupplier supplier = attributes.get();
                List<EntityRegistry.AttributeEntry> entries = new ArrayList<>();
                for (Holder<Attribute> attribute : supplier.instances.keySet()) {
                    entries.add(new EntityRegistry.AttributeEntry(attribute, Optional.of(supplier.getBaseValue(attribute))));
                }
                return new EntityRegistry.Attributes(Optional.empty(), entries);
            });
            return this;
        }

        public <T extends Entity> Properties spawnPlacement(SpawnPlacementType placementType, Heightmap.Types heightmap, SpawnPlacements.SpawnPredicate<T> spawnPredicate) {
            return spawnPlacement(register(ExtensibleCodecs.SPAWN_PLACEMENT, placementType), heightmap, ExtensibleCodecs.SPAWN_PREDICATE.registerSpawnPredicate(Identifier.fromNamespaceAndPath(modId, "entities/" + path), () -> spawnPredicate).create());
        }
        public Properties spawnPlacement(ExtensibleCodec.Entry<SpawnPlacementType> placementType, Heightmap.Types heightmap, ExtensibleCodec.Entry<ExtensibleSpawnPredicate.Type> spawnPredicate) {
            properties.spawnPlacement = Optional.of(new EntityRegistry.SpawnPlacement(placementType, heightmap, spawnPredicate));
            return this;
        }

        public Properties texture(Identifier replacement) {
            texture = Optional.of(new MobVariants.TextureReplacement(Optional.empty(), replacement));
            return this;
        }
        public Properties texture(Identifier target, Identifier replacement) {
            texture = Optional.of(new MobVariants.TextureReplacement(Optional.of(target), replacement));
            return this;
        }

        public Properties babyTexture(Identifier replacement) {
            babyTexture = Optional.of(new MobVariants.TextureReplacement(Optional.empty(), replacement));
            return this;
        }
        public Properties babyTexture(Identifier target, Identifier replacement) {
            babyTexture = Optional.of(new MobVariants.TextureReplacement(Optional.of(target), replacement));
            return this;
        }

        public Properties ambientSound(SoundEvent sound) {
            ambientSound = Optional.of(sound);
            return this;
        }
        public Properties hurtSound(SoundEvent sound) {
            hurtSound = Optional.of(sound);
            return this;
        }
        public Properties eatSound(SoundEvent sound) {
            eatSound = Optional.of(sound);
            return this;
        }
        public Properties deathSound(SoundEvent sound) {
            deathSound = Optional.of(sound);
            return this;
        }
        public Properties stepSound(SoundEvent sound) {
            stepSound = Optional.of(sound);
            return this;
        }

        public Properties addAttackEffect(MobEffectInstance effect) {
            attackEffects.add(effect);
            return this;
        }

        public Properties shouldBurnInDaylight(boolean burnInDaylight) {
            this.burnInDaylight = Optional.of(burnInDaylight);
            return this;
        }
    }

    public static final class Assets {
        private Optional<String> name = Optional.empty();

        public Assets name(String value) {
            name = Optional.of(value);
            return this;
        }


        private DataProviders.EntityAssets build() {
            return new DataProviders.EntityAssets(name);
        }
    }

    public static final class Data {
        private final String modId;
        private final Optional<ExtensibleCodec.Entry<Boolean>> requirement;
        private final ResourceKey<EntityType<?>> key;
        private final Supplier<? extends EntityType<?>> entity;
        private Consumer<DataProviders.TagGenerator<EntityType<?>>> tags = _ -> {};
        private Supplier<LootTable.Builder> loot;

        private Data(String modId, Optional<ExtensibleCodec.Entry<Boolean>> requirement, ResourceKey<EntityType<?>> key, Supplier<? extends EntityType<?>> entity) {
            this.modId = modId;
            this.requirement = requirement;
            this.key = key;
            this.entity = entity;
        }

        public Data tag(TagKey<EntityType<?>> tag) {
            tags = tags.andThen(generator -> generator.add(tag, key));
            return this;
        }

        public Data optionalTag(TagKey<EntityType<?>> tag) {
            tags = tags.andThen(generator -> generator.addOptional(tag, key));
            return this;
        }

        public Data loot(Supplier<LootTable.Builder> lootTable) {
            loot = lootTable;
            return this;
        }

        private void register() {
            DataProviders.TAGS.add(modId, new DataProviders.TagRequest<>(Registries.ENTITY_TYPE, tags));
            if (loot != null) DataProviders.ENTITY_LOOT.add(modId, generator -> generator.add(entity.get(), loot.get()));
        }
    }

    public static final class Builder {
        private final Properties properties;
        private final List<Consumer<Assets>> assetConfigurations = new ArrayList<>();
        private final List<Consumer<Data>> dataConfigurations = new ArrayList<>();

        private Builder(String modId, String path) {
            properties = new Properties(modId, path);
        }

        public Builder properties(Consumer<Properties> properties) {
            properties.accept(this.properties);
            return this;
        }

        public Builder assets(Consumer<Assets> assets) {
            assetConfigurations.add(assets);
            return this;
        }

        public Builder data(Consumer<Data> data) {
            dataConfigurations.add(data);
            return this;
        }

        private DataProviders.EntityAssets buildAssets() {
            Assets assets = new Assets();
            assetConfigurations.forEach(configure -> configure.accept(assets));
            return assets.build();
        }

        private void registerData(String modId, Optional<ExtensibleCodec.Entry<Boolean>> requirement, ResourceKey<EntityType<?>> key, Supplier<? extends EntityType<?>> entity) {
            Data data = new Data(modId, requirement, key, entity);
            dataConfigurations.forEach(configure -> configure.accept(data));
            data.register();
        }
    }
}