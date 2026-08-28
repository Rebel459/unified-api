package net.rebel459.unified.platform;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.rebel459.unified.util.codec.CodecUtils;
import net.rebel459.unified.util.codec.ExtensibleCodec;
import net.rebel459.unified.util.data.registry.BlockRegistry;
import net.rebel459.unified.util.data.registry.ItemRegistry;
import net.rebel459.unified.util.datagen.BlockAsset;
import net.rebel459.unified.util.datagen.BlockAssets;
import net.rebel459.unified.util.datagen.ItemAsset;
import net.rebel459.unified.util.datagen.ItemAssets;
import net.rebel459.unified.util.RecipeProvider;
import net.rebel459.unified.util.datagen.impl.BlockAssetRequest;
import net.rebel459.unified.util.datagen.impl.DataRegistry;
import net.rebel459.unified.util.datagen.impl.ItemAssetRequest;
import net.rebel459.unified.util.registry.SuppliedBlock;
import net.rebel459.unified.util.registry.SuppliedItem;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

public final class UnifiedDataRegistries {
    private final Items items;
    private final Blocks blocks;
    
    private UnifiedDataRegistries(String modId, DataRegistry.GenerationSettings settings) {
        this.items = new Items(modId, UnifiedRegistries.Items.create(modId));
        this.blocks = new Blocks(modId, UnifiedRegistries.Blocks.create(modId));
        DataRegistry.settings(modId, settings);
    }

    public static Builder create(String modId) {
        return new Builder(modId);
    }
    public Items items() {
        return items;
    }
    public Blocks blocks() {
        return blocks;
    }

    public static final class Builder {
        private final String modId;
        private int priority;
        private final List<String> dependencies = new ArrayList<>();
        private boolean autoName;
        private String language = "en_us";
        private Optional<String> injectedTranslations = Optional.empty();

        private Builder(String modId) {
            this.modId = modId;
        }

        public Builder priority(int value) {
            priority = value; return this;
        }
        public Builder dependency(String modId) {
            dependencies.add(modId); return this;
        }
        public Builder autoName() {
            autoName = true; injectedTranslations = Optional.empty(); return this;
        }
        public Builder autoName(String injectedTranslations) {
            autoName = true;
            this.injectedTranslations = Optional.of(injectedTranslations);
            return this;
        }
        public Builder language(String language) {
            this.language = language; return this;
        }

        public UnifiedDataRegistries build() {
            return new UnifiedDataRegistries(modId, new DataRegistry.GenerationSettings(
                    new DataRegistry.PriorityAndDependencies(priority, dependencies),
                    autoName,
                    language,
                    injectedTranslations
            ));
        }
    }

    public static class Items {

        private final String modId;
        private final UnifiedRegistries.Items items;

        private Items(String modId, UnifiedRegistries.Items runtime) {
            this.modId = modId;
            this.items = runtime;
        }

        public SuppliedItem register(String path, ExtensibleCodec.Entry<Function<Item.Properties, Item>> type, Consumer<Builder> configure) {
            return register(path, () -> type, configure);
        }

        public SuppliedItem register(String path, Supplier<ExtensibleCodec.Entry<Function<Item.Properties, Item>>> type, Consumer<Builder> configure) {
            Builder builder = new Builder();
            configure.accept(builder);
            Identifier id = Identifier.fromNamespaceAndPath(modId, path);
            Supplier<Item.Properties> runtimeProperties = () -> {
                Item.Properties createProperties = new Item.Properties();
                builder.properties.accept(createProperties);
                return createProperties;
            };
            SuppliedItem registered = items.register(path, itemProperties -> type.get().value().apply(itemProperties), runtimeProperties);
            DataRegistry.add(id, new DataRegistry.GeneratedItem(
                    () -> new ItemRegistry.Definition(type.get(), CodecUtils.suppliedValue(
                            DataComponentType.VALUE_MAP_CODEC,
                            componentValues(builder.properties, ResourceKey.create(Registries.ITEM, id)),
                            () -> RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY),
                            ItemRegistry::createProperties
                    )),
                    builder::buildAssets,
                    builder::buildData
            ));
            return registered;
        }

        private static Map<DataComponentType<?>, Object> componentValues(Consumer<Item.Properties> configure, ResourceKey<Item> key) {
            Item.Properties properties = new Item.Properties();
            configure.accept(properties);
            DataComponentMap.Builder componentBuilder = DataComponentMap.builder();
            properties.componentInitializer.run(
                    componentBuilder,
                    RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY),
                    key
            );
            Map<DataComponentType<?>, Object> components = new LinkedHashMap<>();
            componentBuilder.build().forEach(component -> components.put(component.type(), component.value()));
            return components;
        }

        public static final class Builder {
            private Consumer<Item.Properties> properties = _ -> {};
            private final List<Consumer<Assets>> assetConfigurations = new ArrayList<>();
            private final List<Consumer<Data>> dataConfigurations = new ArrayList<>();

            public Builder properties(Consumer<Item.Properties> configure) {
                properties = configure;
                return this;
            }
            public Builder assets(Consumer<Assets> configure) {
                assetConfigurations.add(configure); return this;
            }
            public Builder data(Consumer<Data> configure) {
                dataConfigurations.add(configure); return this;
            }

            private DataRegistry.ItemAssets buildAssets() {
                Assets assets = new Assets();
                assetConfigurations.forEach(configure -> configure.accept(assets));
                return assets.build();
            }

            private DataRegistry.ItemData buildData() {
                Data data = new Data();
                dataConfigurations.forEach(configure -> configure.accept(data));
                return data.build();
            }
        }

        public static final class Assets {
            private Optional<String> name = Optional.empty();
            private final List<ItemAssetRequest<?>> models = new ArrayList<>();

            public Assets name(String value) {
                name = Optional.of(value);
                return this;
            }

            public Assets model(ItemAsset<Void> type) {
                models.add(ItemAssetRequest.create(type));
                return this;
            }

            public <T> Assets model(ItemAsset<T> type, T value) {
                models.add(ItemAssetRequest.create(type, value));
                return this;
            }

            public Assets generated() {
                return model(ItemAssets.GENERATED);
            }

            public Assets handheld() {
                return model(ItemAssets.HANDHELD);
            }

            private DataRegistry.ItemAssets build() {
                return new DataRegistry.ItemAssets(name, List.copyOf(models));
            }
        }

        public static final class Data {
            private final List<TagKey<Item>> tags = new ArrayList<>();
            private final List<TagKey<Item>> optionalTags = new ArrayList<>();
            private Optional<BiConsumer<Item, RecipeProvider>> recipe = Optional.empty();

            public Data tag(TagKey<Item> tag) {
                tags.add(tag);
                return this;
            }

            public Data optionalTag(TagKey<Item> tag) {
                optionalTags.add(tag);
                return this;
            }

            public Data recipe(BiConsumer<Item, RecipeProvider> factory) {
                recipe = Optional.of(factory);
                return this;
            }

            private DataRegistry.ItemData build() {
                return new DataRegistry.ItemData(List.copyOf(tags), List.copyOf(optionalTags), recipe);
            }
        }
    }

    public static class Blocks {

        private final String modId;
        private final UnifiedRegistries.Blocks blocks;

        private Blocks(String modId, UnifiedRegistries.Blocks blocks) {
            this.modId = modId;
            this.blocks = blocks;
        }

        public SuppliedBlock register(String path, ExtensibleCodec.Entry<Function<BlockBehaviour.Properties, ? extends Block>> type, Consumer<Builder> configure) {
            return register(path, () -> type, configure);
        }

        public SuppliedBlock register(String path, Supplier<ExtensibleCodec.Entry<Function<BlockBehaviour.Properties, ? extends Block>>> type, Consumer<Builder> configure) {
            Builder builder = new Builder();
            configure.accept(builder);
            Identifier id = Identifier.fromNamespaceAndPath(modId, path);
            Supplier<BlockBehaviour.Properties> properties = builder.properties.buildSupplier();
            DataRegistry.add(id, new DataRegistry.GeneratedBlock(
                    () -> new BlockRegistry.Definition(type.get(), builder.registerItem, properties, builder.blockEntity),
                    builder::buildAssets,
                    builder::buildData
            ));

            if (builder.blockEntity.isPresent()) {
                Supplier<BlockEntityType<BlockEntity>> blockEntity = () -> (BlockEntityType<BlockEntity>) BuiltInRegistries.BLOCK_ENTITY_TYPE.getValueOrThrow(builder.blockEntity.orElseThrow());
                if (!builder.registerItem) return blocks.registerWithoutItem(path, blockProperties -> type.get().value().apply(blockProperties), properties, blockEntity);
                return blocks.register(path, blockProperties -> type.get().value().apply(blockProperties), properties, blockEntity);
            }
            if (!builder.registerItem) return blocks.registerWithoutItem(path, blockProperties -> type.get().value().apply(blockProperties), properties);
            return blocks.register(path, blockProperties -> type.get().value().apply(blockProperties), properties);
        }

        public static final class Properties {
            private Optional<Identifier> copyFrom = Optional.empty();
            private Optional<Either<MapColor, ExtensibleCodec.Entry<Function<BlockState, MapColor>>>> mapColor = Optional.empty();
            private Optional<Boolean> collision = Optional.empty();
            private Optional<Boolean> occlusion = Optional.empty();
            private Optional<Float> friction = Optional.empty();
            private Optional<Float> speedMultiplier = Optional.empty();
            private Optional<Float> jumpMultiplier = Optional.empty();
            private Optional<SoundType> soundType = Optional.empty();
            private Optional<Either<Integer, ExtensibleCodec.Entry<ToIntFunction<BlockState>>>> lightLevel = Optional.empty();
            private Optional<Float> destroyTime = Optional.empty();
            private Optional<Float> explosionResistance = Optional.empty();
            private Optional<Boolean> randomTicks = Optional.empty();
            private Optional<Boolean> dynamicShape = Optional.empty();
            private Optional<ResourceKey<LootTable>> lootTable = Optional.empty();
            private Optional<Boolean> ignitedByLava = Optional.empty();
            private Optional<Boolean> liquid = Optional.empty();
            private Optional<Boolean> solid = Optional.empty();
            private Optional<PushReaction> pushReaction = Optional.empty();
            private Optional<Boolean> air = Optional.empty();
            private Optional<ExtensibleCodec.Entry<BlockBehaviour.StateArgumentPredicate<EntityType<?>>>> validSpawn = Optional.empty();
            private Optional<ExtensibleCodec.Entry<BlockBehaviour.StatePredicate>> redstoneConductor = Optional.empty();
            private Optional<ExtensibleCodec.Entry<BlockBehaviour.StatePredicate>> suffocating = Optional.empty();
            private Optional<ExtensibleCodec.Entry<BlockBehaviour.StatePredicate>> viewBlocking = Optional.empty();
            private Optional<ExtensibleCodec.Entry<BlockBehaviour.PostProcess>> postProcess = Optional.empty();
            private Optional<ExtensibleCodec.Entry<BlockBehaviour.StatePredicate>> emissiveRendering = Optional.empty();
            private Optional<Boolean> requiresCorrectToolForDrops = Optional.empty();
            private Optional<BlockBehaviour.OffsetType> offset = Optional.empty();
            private Optional<Boolean> spawnTerrainParticles = Optional.empty();
            private Optional<NoteBlockInstrument> instrument = Optional.empty();
            private Optional<Boolean> replaceable = Optional.empty();
            private Optional<String> descriptionOverride = Optional.empty();
            private Optional<FeatureFlagSet> requiredFeatures = Optional.empty();
            private Optional<Boolean> noLootTable = Optional.empty();

            public Properties copyFrom(ResourceKey<Block> copyFrom) {
                this.copyFrom = Optional.of(copyFrom.identifier());
                return this;
            }

            public Properties mapColor(MapColor mapColor) {
                this.mapColor = Optional.of(Either.left(mapColor));
                return this;
            }
            public Properties mapColor(ExtensibleCodec.Entry<Function<BlockState, MapColor>> mapColor) {
                this.mapColor = Optional.of(Either.right(mapColor));
                return this;
            }

            public Properties collision(boolean collision) {
                this.collision = Optional.of(collision);
                return this;
            }

            public Properties occlusion(boolean occlusion) {
                this.occlusion = Optional.of(occlusion);
                return this;
            }

            public Properties friction(float friction) {
                this.friction = Optional.of(friction);
                return this;
            }

            public Properties speedMultiplier(float speedMultiplier) {
                this.speedMultiplier = Optional.of(speedMultiplier);
                return this;
            }

            public Properties jumpMultiplier(float jumpMultiplier) {
                this.jumpMultiplier = Optional.of(jumpMultiplier);
                return this;
            }

            public Properties soundType(SoundType soundType) {
                this.soundType = Optional.of(soundType);
                return this;
            }

            public Properties lightLevel(int lightLevel) {
                this.lightLevel = Optional.of(Either.left(lightLevel));
                return this;
            }
            public Properties lightLevel(ExtensibleCodec.Entry<ToIntFunction<BlockState>> lightLevel) {
                this.lightLevel = Optional.of(Either.right(lightLevel));
                return this;
            }

            public Properties strength(float strength) {
                strength(strength, strength);
                return this;
            }
            public Properties strength(float destroyTime, float explosionResistance) {
                this.destroyTime = Optional.of(destroyTime);
                this.explosionResistance = Optional.of(explosionResistance);
                return this;
            }

            public Properties randomTicks(boolean randomTicks) {
                this.randomTicks = Optional.of(randomTicks);
                return this;
            }

            public Properties dynamicShape(boolean dynamicShape) {
                this.dynamicShape = Optional.of(dynamicShape);
                return this;
            }

            public Properties lootTable(ResourceKey<LootTable> lootTable) {
                this.lootTable = Optional.of(lootTable);
                this.noLootTable = Optional.empty();
                return this;
            }

            public Properties noLootTable() {
                this.lootTable = Optional.empty();
                this.noLootTable = Optional.of(true);
                return this;
            }

            public Properties requiredFeatures(FeatureFlag... features) {
                this.requiredFeatures = Optional.of(features.length == 0
                        ? FeatureFlagSet.of()
                        : FeatureFlagSet.of(features[0], Arrays.copyOfRange(features, 1, features.length)));
                return this;
            }

            public Properties ignitedByLava(boolean ignitedByLava) {
                this.ignitedByLava = Optional.of(ignitedByLava);
                return this;
            }

            public Properties liquid(boolean liquid) {
                this.liquid = Optional.of(liquid);
                return this;
            }

            public Properties solid(boolean solid) {
                this.solid = Optional.of(solid);
                return this;
            }

            public Properties pushReaction(PushReaction pushReaction) {
                this.pushReaction = Optional.of(pushReaction);
                return this;
            }

            public Properties air(boolean air) {
                this.air = Optional.of(air);
                return this;
            }

            public Properties validSpawn(ExtensibleCodec.Entry<BlockBehaviour.StateArgumentPredicate<EntityType<?>>> validSpawn) {
                this.validSpawn = Optional.of(validSpawn);
                return this;
            }

            public Properties redstoneConductor(ExtensibleCodec.Entry<BlockBehaviour.StatePredicate> redstoneConductor) {
                this.redstoneConductor = Optional.of(redstoneConductor);
                return this;
            }

            public Properties suffocating(ExtensibleCodec.Entry<BlockBehaviour.StatePredicate> suffocating) {
                this.suffocating = Optional.of(suffocating);
                return this;
            }

            public Properties viewBlocking(ExtensibleCodec.Entry<BlockBehaviour.StatePredicate> viewBlocking) {
                this.viewBlocking = Optional.of(viewBlocking);
                return this;
            }

            public Properties postProcess(ExtensibleCodec.Entry<BlockBehaviour.PostProcess> postProcess) {
                this.postProcess = Optional.of(postProcess);
                return this;
            }

            public Properties emissiveRendering(ExtensibleCodec.Entry<BlockBehaviour.StatePredicate> emissiveRendering) {
                this.emissiveRendering = Optional.of(emissiveRendering);
                return this;
            }

            public Properties requiresCorrectToolForDrops(boolean requiresCorrectToolForDrops) {
                this.requiresCorrectToolForDrops = Optional.of(requiresCorrectToolForDrops);
                return this;
            }

            public Properties offset(BlockBehaviour.OffsetType offset) {
                this.offset = Optional.of(offset);
                return this;
            }

            public Properties spawnTerrainParticles(boolean spawnTerrainParticles) {
                this.spawnTerrainParticles = Optional.of(spawnTerrainParticles);
                return this;
            }

            public Properties instrument(NoteBlockInstrument instrument) {
                this.instrument = Optional.of(instrument);
                return this;
            }

            public Properties replaceable(boolean replaceable) {
                this.replaceable = Optional.of(replaceable);
                return this;
            }

            public Properties descriptionOverride(String descriptionOverride) {
                this.descriptionOverride = Optional.of(descriptionOverride);
                return this;
            }

            public Properties instabreak() {
                strength(0);
                return this;
            }

            private Supplier<BlockBehaviour.Properties> buildSupplier() {
                BlockRegistry.Properties definition = new BlockRegistry.Properties(
                        copyFrom, mapColor, collision, occlusion, friction, speedMultiplier, jumpMultiplier,
                        soundType.map(BlockRegistry.SoundType::create), lightLevel, destroyTime, explosionResistance,
                        randomTicks, dynamicShape, lootTable, ignitedByLava, liquid, solid, pushReaction, air,
                        validSpawn, redstoneConductor, suffocating, viewBlocking, postProcess, emissiveRendering,
                        requiresCorrectToolForDrops, offset, spawnTerrainParticles, instrument, replaceable, descriptionOverride,
                        requiredFeatures, noLootTable
                );
                return CodecUtils.suppliedValue(
                        BlockRegistry.Properties.CODEC,
                        definition,
                        () -> RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY),
                        BlockRegistry::createProperties
                );
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

            private DataRegistry.BlockAssets build() {
                return new DataRegistry.BlockAssets(name, List.copyOf(models));
            }
        }

        public static final class Data {
            private final List<TagKey<Block>> tags = new ArrayList<>();
            private final List<TagKey<Block>> optionalTags = new ArrayList<>();
            private final List<TagKey<Item>> itemTags = new ArrayList<>();
            private final List<TagKey<Item>> optionalItemTags = new ArrayList<>();
            private Optional<Function<Block, LootTable.Builder>> loot = Optional.empty();
            private Optional<BiConsumer<Item, RecipeProvider>> recipes = Optional.empty();

            public Data tag(TagKey<Block> tag) {
                tags.add(tag);
                return this;
            }

            public Data optionalTag(TagKey<Block> tag) {
                optionalTags.add(tag);
                return this;
            }

            public Data itemTag(TagKey<Item> tag) {
                itemTags.add(tag);
                return this;
            }

            public Data optionalItemTag(TagKey<Item> tag) {
                optionalItemTags.add(tag);
                return this;
            }

            public Data loot(Function<Block, LootTable.Builder> factory) {
                loot = Optional.of(factory);
                return this;
            }

            public Data recipes(BiConsumer<Item, RecipeProvider> factory) {
                recipes = Optional.of(factory);
                return this;
            }

            public Data dropSelf() {
                loot = Optional.empty();
                loot(block -> LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1F)).add(LootItem.lootTableItem(block))));
                return this;
            }

            private DataRegistry.BlockData build() {
                return new DataRegistry.BlockData(List.copyOf(tags), List.copyOf(optionalTags), List.copyOf(itemTags), List.copyOf(optionalItemTags), loot, recipes);
            }
        }

        public static final class Builder {
            private final Properties properties = new Properties();
            private final List<Consumer<Assets>> assetConfigurations = new ArrayList<>();
            private final List<Consumer<Data>> dataConfigurations = new ArrayList<>();
            private boolean registerItem = true;
            private Optional<ResourceKey<BlockEntityType<? extends BlockEntity>>> blockEntity = Optional.empty();

            public Builder properties(Consumer<Properties> configure) { configure.accept(properties); return this; }
            public Builder assets(Consumer<Assets> configure) { assetConfigurations.add(configure); return this; }
            public Builder data(Consumer<Data> configure) { dataConfigurations.add(configure); return this; }
            public Builder withoutItem() { registerItem = false; return this; }
            public Builder blockEntity(ResourceKey<BlockEntityType<? extends BlockEntity>> type) { blockEntity = Optional.of(type); return this; }

            private DataRegistry.BlockAssets buildAssets() {
                Assets assets = new Assets();
                assetConfigurations.forEach(configure -> configure.accept(assets));
                return assets.build();
            }

            private DataRegistry.BlockData buildData() {
                Data data = new Data();
                dataConfigurations.forEach(configure -> configure.accept(data));
                return data.build();
            }
        }
    }
}
