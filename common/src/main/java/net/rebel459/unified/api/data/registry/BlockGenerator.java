package net.rebel459.unified.api.data.registry;

import com.mojang.datafixers.util.Either;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockItemTagId;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.rebel459.unified.api.asset.BlockAsset;
import net.rebel459.unified.api.asset.BlockAssets;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.api.codec.ExtensibleCodecs;
import net.rebel459.unified.api.codec.CodecGenerator;
import net.rebel459.unified.api.core.SuppliedBlock;
import net.rebel459.unified.api.data.helper.RecipeGenerator;
import net.rebel459.unified.api.data.helper.TagGenerator;
import net.rebel459.unified.api.registry.VanillaItemCodecs;
import net.rebel459.unified.api.util.BlockLootSubProvider;
import net.rebel459.unified.api.util.RecipeProvider;
import net.rebel459.unified.impl.asset.BlockAssetRequest;
import net.rebel459.unified.impl.core.DataProviders;
import net.rebel459.unified.impl.data.registry.BlockRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.*;

public class BlockGenerator {

    private final String modId;
    private final String namespace;
    private final DataProviders.GenerationSettings settings;
    private final ItemGenerator items;
    private final TagGenerator tags;
    private final RecipeGenerator recipes;

    public BlockGenerator(String modId, String namespace, DataProviders.GenerationSettings settings, ItemGenerator items, TagGenerator tags, RecipeGenerator recipes) {
        this.modId = modId;
        this.namespace = namespace;
        this.settings = settings;
        this.items = items;
        this.tags = tags;
        this.recipes = recipes;
    }

    public SuppliedBlock register(String path, Function<BlockBehaviour.Properties, ? extends Block> type, Consumer<Builder> builder) {
        return register(path, path, type, builder);
    }

    public SuppliedBlock register(String blockPath, String itemPath, Function<BlockBehaviour.Properties, ? extends Block> type, Consumer<Builder> builder) {
        return register(blockPath, itemPath, ExtensibleCodecs.BLOCK.register(Identifier.fromNamespaceAndPath(namespace, blockPath), () -> type).create(), builder);
    }

    public SuppliedBlock register(String path, ExtensibleCodec.Entry<Function<BlockBehaviour.Properties, ? extends Block>> type, Consumer<Builder> builder) {
        return register(path, path, type, builder);
    }

    public SuppliedBlock register(String blockPath, String itemPath, ExtensibleCodec.Entry<Function<BlockBehaviour.Properties, ? extends Block>> type, Consumer<Builder> builder) {
        Builder finalBuilder = createBuilder(blockPath, builder);
        SuppliedBlock block = registerWithoutItem(blockPath, itemPath, type, finalBuilder);
        items.registerBlockItem(block, VanillaItemCodecs.BLOCK_ITEM.create(), itemBuilder -> itemBuilder.properties(finalBuilder.itemProperties));
        return block;
    }

    public SuppliedBlock registerWithoutItem(String path, Function<BlockBehaviour.Properties, ? extends Block> type, Consumer<Builder> builder) {
        return registerWithoutItem(path, path, type, builder);
    }

    public SuppliedBlock registerWithoutItem(String blockPath, String itemPath, Function<BlockBehaviour.Properties, ? extends Block> type, Consumer<Builder> builder) {
        return registerWithoutItem(blockPath, itemPath, ExtensibleCodecs.BLOCK.register(Identifier.fromNamespaceAndPath(namespace, blockPath), () -> type).create(), builder);
    }

    public SuppliedBlock registerWithoutItem(String path, ExtensibleCodec.Entry<Function<BlockBehaviour.Properties, ? extends Block>> type, Consumer<Builder> builder) {
        return registerWithoutItem(path, path, type, builder);
    }

    public SuppliedBlock registerWithoutItem(String blockPath, String itemPath, ExtensibleCodec.Entry<Function<BlockBehaviour.Properties, ? extends Block>> type, Consumer<Builder> builder) {
        return registerWithoutItem(blockPath, itemPath, type, createBuilder(blockPath, builder));
    }

    private Builder createBuilder(String blockPath, Consumer<Builder> builder) {
        Builder finalBuilder = new Builder(namespace, blockPath, tags, recipes);
        builder.accept(finalBuilder);
        return finalBuilder;
    }

    private SuppliedBlock registerWithoutItem(String blockPath, String itemPath, ExtensibleCodec.Entry<Function<BlockBehaviour.Properties, ? extends Block>> type, Builder finalBuilder) {
        Identifier id = Identifier.fromNamespaceAndPath(namespace, blockPath);
        Supplier<BlockRegistry.Definition> definition = () -> new BlockRegistry.Definition(
                type, finalBuilder.buildProperties(), finalBuilder.blockEntity);
        SuppliedBlock block = BlockRegistry.registerDefinition(BlockItemId.create(id,
                Identifier.fromNamespaceAndPath(namespace, itemPath)), definition,
                finalBuilder.blockEntity);
        CodecGenerator.registry(modId, id, "blocks", settings.metadata().priority(), settings.metadata().requirement(),
                BlockRegistry.CODEC, definition);
        DataProviders.MODELS.add(modId, new DataProviders.BlockModels(id, finalBuilder::buildAssets));
        DataProviders.LANGUAGES.add(modId, new DataProviders.LanguageRequest(settings, Optional.of(
                new DataProviders.Translation(id, DataProviders.TranslationType.BLOCK,
                        () -> finalBuilder.buildAssets().name()))));
        finalBuilder.registerData(modId, block.blockItemId(), block);
        return block;
    }

    public static final class Properties {
        private final String modId;
        private final String path;
        private final BlockRegistry.Properties definition = new BlockRegistry.Properties();

        private Properties(String modId, String path) {
            this.modId = modId;
            this.path = path;
        }

        private <R> ExtensibleCodec.Entry<R> register(ExtensibleCodec<R> codec, R value) {
            return codec.register(Identifier.fromNamespaceAndPath(modId, "block/" + path), () -> value).create();
        }

        public Properties copyFrom(Supplier<? extends Block> copyFrom) {
            definition.copyFrom = Optional.of(widen(copyFrom));
            return this;
        }

        public Properties mapColor(MapColor mapColor) {
            definition.mapColor = Optional.of(Either.left(mapColor));
            return this;
        }

        public Properties mapColor(ExtensibleCodec.Entry<Function<BlockState, MapColor>> mapColor) {
            definition.mapColor = Optional.of(Either.right(mapColor));
            return this;
        }

        public Properties mapColor(Function<BlockState, MapColor> mapColor) {
            return mapColor(register(ExtensibleCodecs.MAP_COLOR, mapColor));
        }

        public Properties collision(boolean collision) {
            definition.collision = Optional.of(collision);
            return this;
        }

        public Properties occlusion(boolean occlusion) {
            definition.occlusion = Optional.of(occlusion);
            return this;
        }

        public Properties friction(float friction) {
            definition.friction = Optional.of(friction);
            return this;
        }

        public Properties speedMultiplier(float speedMultiplier) {
            definition.speedMultiplier = Optional.of(speedMultiplier);
            return this;
        }

        public Properties jumpMultiplier(float jumpMultiplier) {
            definition.jumpMultiplier = Optional.of(jumpMultiplier);
            return this;
        }

        public Properties soundType(SoundType soundType) {
            definition.soundType = Optional.of(BlockRegistry.SoundType.create(soundType));
            return this;
        }

        public Properties lightLevel(int lightLevel) {
            definition.lightLevel = Optional.of(Either.left(lightLevel));
            return this;
        }

        public Properties lightLevel(ExtensibleCodec.Entry<ToIntFunction<BlockState>> lightLevel) {
            definition.lightLevel = Optional.of(Either.right(lightLevel));
            return this;
        }

        public Properties lightLevel(ToIntFunction<BlockState> lightLevel) {
            return lightLevel(register(ExtensibleCodecs.LIGHT_EMISSION, lightLevel));
        }

        public Properties strength(float strength) {
            strength(strength, strength);
            return this;
        }

        public Properties strength(float destroyTime, float explosionResistance) {
            definition.destroyTime = Optional.of(destroyTime);
            definition.explosionResistance = Optional.of(explosionResistance);
            return this;
        }

        public Properties randomTicks(boolean randomTicks) {
            definition.randomTicks = Optional.of(randomTicks);
            return this;
        }

        public Properties dynamicShape(boolean dynamicShape) {
            definition.dynamicShape = Optional.of(dynamicShape);
            return this;
        }

        public Properties lootTable(ResourceKey<LootTable> lootTable) {
            definition.lootTable = Optional.of(lootTable);
            definition.noLootTable = Optional.empty();
            return this;
        }

        public Properties noLootTable() {
            definition.lootTable = Optional.empty();
            definition.noLootTable = Optional.of(true);
            return this;
        }

        public Properties requiredFeatures(FeatureFlag... features) {
            definition.requiredFeatures = Optional.of(features.length == 0
                    ? FeatureFlagSet.of()
                    : FeatureFlagSet.of(features[0], Arrays.copyOfRange(features, 1, features.length)));
            return this;
        }

        public Properties liquid(boolean liquid) {
            definition.liquid = Optional.of(liquid);
            return this;
        }

        public Properties solid(boolean solid) {
            definition.solid = Optional.of(solid);
            return this;
        }

        public Properties pushReaction(PushReaction pushReaction) {
            definition.pushReaction = Optional.of(pushReaction);
            return this;
        }

        public Properties air(boolean air) {
            definition.air = Optional.of(air);
            return this;
        }

        public Properties validSpawn(ExtensibleCodec.Entry<BlockBehaviour.StateArgumentPredicate<EntityType<?>>> validSpawn) {
            definition.validSpawn = Optional.of(validSpawn);
            return this;
        }

        public Properties validSpawn(BlockBehaviour.StateArgumentPredicate<EntityType<?>> validSpawn) {
            return validSpawn(register(ExtensibleCodecs.ENTITY_PREDICATE, validSpawn));
        }

        public Properties redstoneConductor(ExtensibleCodec.Entry<BlockBehaviour.StatePredicate> redstoneConductor) {
            definition.redstoneConductor = Optional.of(redstoneConductor);
            return this;
        }

        public Properties redstoneConductor(BlockBehaviour.StatePredicate redstoneConductor) {
            return redstoneConductor(register(ExtensibleCodecs.STATE_PREDICATE, redstoneConductor));
        }

        public Properties suffocating(ExtensibleCodec.Entry<BlockBehaviour.StatePredicate> suffocating) {
            definition.suffocating = Optional.of(suffocating);
            return this;
        }

        public Properties suffocating(BlockBehaviour.StatePredicate suffocating) {
            return suffocating(register(ExtensibleCodecs.STATE_PREDICATE, suffocating));
        }

        public Properties viewBlocking(ExtensibleCodec.Entry<BlockBehaviour.StatePredicate> viewBlocking) {
            definition.viewBlocking = Optional.of(viewBlocking);
            return this;
        }

        public Properties viewBlocking(BlockBehaviour.StatePredicate viewBlocking) {
            return viewBlocking(register(ExtensibleCodecs.STATE_PREDICATE, viewBlocking));
        }

        public Properties postProcess(ExtensibleCodec.Entry<BlockBehaviour.PostProcess> postProcess) {
            definition.postProcess = Optional.of(postProcess);
            return this;
        }

        public Properties postProcess(BlockBehaviour.PostProcess postProcess) {
            return postProcess(register(ExtensibleCodecs.POST_PROCESS, postProcess));
        }

        public Properties emissiveRendering(ExtensibleCodec.Entry<Predicate<BlockState>> emissiveRendering) {
            definition.emissiveRendering = Optional.of(emissiveRendering);
            return this;
        }

        public Properties emissiveRendering(Predicate<BlockState> emissiveRendering) {
            return emissiveRendering(register(ExtensibleCodecs.BLOCK_PREDICATE, emissiveRendering));
        }

        public Properties requiresCorrectToolForDrops(boolean requiresCorrectToolForDrops) {
            definition.requiresCorrectToolForDrops = Optional.of(requiresCorrectToolForDrops);
            return this;
        }

        public Properties offset(BlockBehaviour.OffsetType offset) {
            definition.offset = Optional.of(offset);
            return this;
        }

        public Properties spawnTerrainParticles(boolean spawnTerrainParticles) {
            definition.spawnTerrainParticles = Optional.of(spawnTerrainParticles);
            return this;
        }

        public Properties instrument(NoteBlockInstrument instrument) {
            definition.instrument = Optional.of(instrument);
            return this;
        }

        public Properties replaceable(boolean replaceable) {
            definition.replaceable = Optional.of(replaceable);
            return this;
        }

        public Properties descriptionOverride(String descriptionOverride) {
            definition.descriptionOverride = Optional.of(descriptionOverride);
            return this;
        }

        public Properties instabreak() {
            strength(0);
            return this;
        }

        public Properties flammable(int igniteOdds, int burnOdds) {
            definition.flammability = Optional.of(new BlockRegistry.Flammability(igniteOdds, burnOdds));
            return this;
        }
    }

    public static final class Assets {
        private Optional<String> name = Optional.empty();
        private final List<BlockAssetRequest<?>> models = new ArrayList<>();

        public Assets name(String value) {
            name = Optional.of(value);
            return this;
        }

        public Assets model(BlockAsset<Void> type) {
            models.add(BlockAssetRequest.create(type));
            return this;
        }

        public <T> Assets model(BlockAsset<T> type, T value) {
            models.add(BlockAssetRequest.create(type, value));
            return this;
        }

        public Assets simpleCube() {
            return model(BlockAssets.SIMPLE_CUBE);
        }

        private DataProviders.BlockAssets build() {
            return new DataProviders.BlockAssets(name, List.copyOf(models));
        }
    }

    public static final class Data {
        private final String modId;
        private final BlockItemId key;
        private final Supplier<Block> block;
        private final TagGenerator tagGenerator;
        private final RecipeGenerator recipeGenerator;
        private BiFunction<Block, BlockLootSubProvider, LootTable.Builder> loot;
        private BiConsumer<Item, RecipeProvider> recipes;

        private Data(String modId, BlockItemId key, Supplier<Block> block, TagGenerator tagGenerator, RecipeGenerator recipeGenerator) {
            this.modId = modId;
            this.key = key;
            this.block = block;
            this.tagGenerator = tagGenerator;
            this.recipeGenerator = recipeGenerator;
        }

        public Data tag(TagKey<Block> tag) {
            tagGenerator.create(tag).add(key.block());
            return this;
        }

        public Data optionalTag(TagKey<Block> tag) {
            tagGenerator.create(tag).addOptional(key.block());
            return this;
        }

        public Data itemTag(TagKey<Item> tag) {
            tagGenerator.create(tag).add(key.item());
            return this;
        }

        public Data optionalItemTag(TagKey<Item> tag) {
            tagGenerator.create(tag).addOptional(key.item());
            return this;
        }

        public Data tag(BlockItemTagId tag) {
            tagGenerator.create(tag).add(key);
            return this;
        }

        public Data optionalTag(BlockItemTagId tag) {
            tagGenerator.create(tag).addOptional(key);
            return this;
        }

        public Data loot(BiFunction<Block, BlockLootSubProvider, LootTable.Builder> factory) {
            loot = factory;
            return this;
        }

        public Data recipes(BiConsumer<Item, RecipeProvider> factory) {
            recipes = factory;
            return this;
        }

        public Data dropSelf() {
            loot((value, provider) -> provider.createSingleItemTable(value));
            return this;
        }

        private void register() {
            if (loot != null) DataProviders.BLOCK_LOOT.add(modId, (generator, output) -> {
                Block value = block.get();
                output.accept(value, loot.apply(value, generator));
            });
            if (recipes != null) recipeGenerator.add(provider -> recipes.accept(block.get().asItem(), provider));
        }
    }

    public static final class Builder {
        private final Properties properties;
        private final List<Consumer<Properties>> propertyConfigurations = new ArrayList<>();
        private final List<Consumer<Assets>> assetConfigurations = new ArrayList<>();
        private final List<Consumer<Data>> dataConfigurations = new ArrayList<>();
        private Consumer<Item.Properties> itemProperties = _ -> {};
        private Optional<Supplier<BlockEntityType<?>>> blockEntity = Optional.empty();
        private final TagGenerator tags;
        private final RecipeGenerator recipes;

        private Builder(String modId, String path, TagGenerator tags, RecipeGenerator recipes) {
            properties = new Properties(modId, path);
            this.tags = tags;
            this.recipes = recipes;
        }

        public Builder properties(Consumer<Properties> properties) {
            if (propertiesBuilt) properties.accept(this.properties);
            else propertyConfigurations.add(properties);
            return this;
        }

        private boolean propertiesBuilt;

        private synchronized BlockRegistry.Properties buildProperties() {
            if (!propertiesBuilt) {
                propertyConfigurations.forEach(configuration -> configuration.accept(properties));
                propertyConfigurations.clear();
                propertiesBuilt = true;
            }
            return properties.definition;
        }

        public Builder assets(Consumer<Assets> assets) {
            assetConfigurations.add(assets);
            return this;
        }

        public Builder data(Consumer<Data> data) {
            dataConfigurations.add(data);
            return this;
        }

        public Builder itemProperties(Consumer<Item.Properties> itemProperties) {
            this.itemProperties = itemProperties;
            return this;
        }

        public Builder blockEntity(Supplier<? extends BlockEntityType<?>> type) {
            blockEntity = Optional.of(widen(type));
            return this;
        }

        private DataProviders.BlockAssets buildAssets() {
            Assets assets = new Assets();
            assetConfigurations.forEach(configure -> configure.accept(assets));
            return assets.build();
        }

        private void registerData(String modId, BlockItemId key, Supplier<Block> block) {
            Data data = new Data(modId, key, block, tags, recipes);
            dataConfigurations.forEach(configure -> configure.accept(data));
            data.register();
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> Supplier<T> widen(Supplier<? extends T> supplier) {
        return (Supplier<T>) supplier;
    }
}
