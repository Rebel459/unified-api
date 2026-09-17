package net.rebel459.unified.api.data.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.variant.SpawnPrioritySelectors;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.loot.LootTable;
import net.rebel459.unified.api.codec.CodecGenerator;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.api.codec.ExtensibleCodecs;
import net.rebel459.unified.api.codec.ExtensibleSpawnPredicate;
import net.rebel459.unified.api.core.Supplied;
import net.rebel459.unified.api.data.helper.TagGenerator;
import net.rebel459.unified.impl.core.DataProviders;
import net.rebel459.unified.impl.data.helper.MobVariants;
import net.rebel459.unified.impl.data.registry.EntityRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class EntityGenerator {

    private final String modId;
    private final String namespace;
    private final DataProviders.GenerationSettings settings;
    private final TagGenerator tags;

    public EntityGenerator(String modId, String namespace, DataProviders.GenerationSettings settings, TagGenerator tags) {
        this.modId = modId;
        this.namespace = namespace;
        this.settings = settings;
        this.tags = tags;
    }

    public <T extends Entity> Supplied<? extends EntityType<?>> register(String path, EntityType.Builder<T> type, Consumer<Builder> builder) {
        return register(path, ExtensibleCodecs.ENTITY.register(Identifier.fromNamespaceAndPath(modId, path), () -> type).create(), builder);
    }

    public Supplied<? extends EntityType<?>> register(String path, ExtensibleCodec.Entry<EntityType.Builder<?>> type, Consumer<Builder> builder) {
        return register(path, new EntityRegistry.CodecBase(type), builder);
    }

    private Supplied<? extends EntityType<?>> register(String path, EntityRegistry.Base base, Consumer<Builder> builder) {
        Builder finalBuilder = new Builder(namespace, path);
        builder.accept(finalBuilder);
        Identifier id = Identifier.fromNamespaceAndPath(namespace, path);
        Supplier<EntityRegistry.Definition> definition = () -> {
            Properties builderProperties = finalBuilder.properties;
            return new EntityRegistry.Definition(base, builderProperties.properties, new MobVariants.Definition(
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
        finalBuilder.registerData(modId, ResourceKey.create(Registries.ENTITY_TYPE, id), entity, tags);
        return entity;
    }

    public Supplied<? extends EntityType<?>> registerCopy(String path, ResourceKey<EntityType<?>> base, Consumer<Builder> builder) {
        return register(path, new EntityRegistry.CopiedBase(base), builder);
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
        private final ResourceKey<EntityType<?>> key;
        private final Supplier<? extends EntityType<?>> entity;
        private final TagGenerator tagGenerator;
        private Supplier<LootTable.Builder> loot;

        private Data(String modId, ResourceKey<EntityType<?>> key, Supplier<? extends EntityType<?>> entity, TagGenerator tags) {
            this.modId = modId;
            this.key = key;
            this.entity = entity;
            this.tagGenerator = tags;
        }

        public Data tag(TagKey<EntityType<?>> tag) {
            tagGenerator.create(tag).add(key);
            return this;
        }

        public Data optionalTag(TagKey<EntityType<?>> tag) {
            tagGenerator.create(tag).addOptional(key);
            return this;
        }

        public Data loot(Supplier<LootTable.Builder> lootTable) {
            loot = lootTable;
            return this;
        }

        private void register() {
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

        private void registerData(String modId, ResourceKey<EntityType<?>> key, Supplier<? extends EntityType<?>> entity, TagGenerator tags) {
            Data data = new Data(modId, key, entity, tags);
            dataConfigurations.forEach(configure -> configure.accept(data));
            data.register();
        }
    }
}
