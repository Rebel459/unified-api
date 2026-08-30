package net.rebel459.unified.api.core;

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
import net.minecraft.world.phys.AABB;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.api.codec.ExtensibleCodecs;
import net.rebel459.unified.impl.registry.BlockRegistry;
import net.rebel459.unified.api.datagen.BlockAsset;
import net.rebel459.unified.api.datagen.BlockAssets;
import net.rebel459.unified.api.datagen.ItemAsset;
import net.rebel459.unified.api.datagen.ItemAssets;
import net.rebel459.unified.impl.registry.ItemRegistry;
import net.rebel459.unified.api.registry.VanillaItemTypes;
import net.rebel459.unified.impl.util.RecipeProvider;
import net.rebel459.unified.impl.datagen.BlockAssetRequest;
import net.rebel459.unified.impl.datagen.DataRegistry;
import net.rebel459.unified.impl.datagen.ItemAssetRequest;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

public final class UnifiedDataRegistries {
    private final Items items;
    private final Blocks blocks;
    
    private UnifiedDataRegistries(String modId, DataRegistry.GenerationSettings settings) {
        this.items = new Items(modId, UnifiedRegistries.Items.create(modId));
        this.blocks = new Blocks(modId, UnifiedRegistries.Blocks.create(modId), this.items);
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
        private Optional<ExtensibleCodec.Entry<Boolean>> requirement = Optional.empty();
        private boolean autoName;
        private String language = "en_us";
        private Optional<String> injectedTranslations = Optional.empty();

        private Builder(String modId) {
            this.modId = modId;
        }

        public Builder priority(int value) {
            priority = value; return this;
        }
        public Builder requirement(String path, Supplier<Boolean> value) {
            requirement = Optional.of(ExtensibleCodecs.REQUIREMENT_TYPES
                    .register(Identifier.fromNamespaceAndPath(modId, path), value)
                    .create());
            return this;
        }
        public Builder requirement(ExtensibleCodec.Entry<Boolean> requirement) {
            this.requirement = Optional.of(requirement);
            return this;
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
                    new DataRegistry.PriorityAndRequirement(priority, requirement),
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

        public SuppliedItem register(String path, Function<Item.Properties, Item> type, Consumer<Builder> configure) {
            return register(path, ExtensibleCodecs.ITEM_TYPES.register(Identifier.fromNamespaceAndPath(modId, path), () -> type).create(), configure);
        }

        public SuppliedItem register(String path, ExtensibleCodec.Entry<Function<Item.Properties, Item>> type, Consumer<Builder> configure) {
            Builder builder = new Builder();
            configure.accept(builder);
            Identifier id = Identifier.fromNamespaceAndPath(modId, path);
            Supplier<Item.Properties> runtimeProperties = () -> {
                Item.Properties createProperties = new Item.Properties();
                builder.properties.accept(createProperties);
                return createProperties;
            };
            SuppliedItem registered = items.register(path, itemProperties -> type.get().apply(itemProperties), runtimeProperties);
            DataRegistry.add(id, new DataRegistry.GeneratedItem(
                    () -> ItemRegistry.Definition.item(type,
                            componentValues(builder.properties, ResourceKey.create(Registries.ITEM, id))),
                    builder::buildAssets,
                    builder::buildData
            ));
            return registered;
        }

        public SuppliedItem registerBlockItem(SuppliedBlock block, BiFunction<Block, Item.Properties, Item> type, Consumer<Builder> configure) {
            Identifier id = block.blockItemId().item().identifier();
            return registerBlockItem(block, ExtensibleCodecs.BLOCK_ITEM_TYPES.register(id, () -> type).create(), configure);
        }

        public SuppliedItem registerBlockItem(SuppliedBlock block, ExtensibleCodec.Entry<BiFunction<Block, Item.Properties, Item>> type, Consumer<Builder> configure) {
            Builder builder = new Builder();
            configure.accept(builder);
            Identifier id = block.blockItemId().item().identifier();
            Supplier<Item.Properties> runtimeProperties = () -> {
                Item.Properties properties = new Item.Properties();
                builder.properties.accept(properties);
                return properties;
            };
            SuppliedItem registered = items.registerBlockItem(
                    block.blockItemId(), block, type.get(), runtimeProperties);
            DataRegistry.add(id, new DataRegistry.GeneratedItem(
                    () -> ItemRegistry.Definition.blockItem(block, type,
                            componentValues(builder.properties, block.blockItemId().item())),
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
        private final Items items;

        private Blocks(String modId, UnifiedRegistries.Blocks blocks, Items items) {
            this.modId = modId;
            this.blocks = blocks;
            this.items = items;
        }

        public SuppliedBlock register(String path, Function<BlockBehaviour.Properties, ? extends Block> type, Consumer<Builder> configure) {
            return register(path, path, type, configure);
        }

        public SuppliedBlock register(String blockPath, String itemPath, Function<BlockBehaviour.Properties, ? extends Block> type, Consumer<Builder> configure) {
            return register(blockPath, itemPath, ExtensibleCodecs.BLOCK_TYPES.register(Identifier.fromNamespaceAndPath(modId, blockPath), () -> type).create(), configure);
        }

        public SuppliedBlock register(String path, ExtensibleCodec.Entry<Function<BlockBehaviour.Properties, ? extends Block>> type, Consumer<Builder> configure) {
            return register(path, path, type, configure);
        }

        public SuppliedBlock register(String blockPath, String itemPath, ExtensibleCodec.Entry<Function<BlockBehaviour.Properties, ? extends Block>> type, Consumer<Builder> configure) {
            Builder builder = new Builder(modId, blockPath);
            configure.accept(builder);
            Identifier id = Identifier.fromNamespaceAndPath(modId, blockPath);
            Supplier<BlockBehaviour.Properties> properties =
                    () -> BlockRegistry.createProperties(builder.properties.definition);
            SuppliedBlock block;
            if (builder.blockEntity.isPresent()) {
                block = register(blocks, blockPath, itemPath,
                        blockProperties -> type.get().apply(blockProperties), properties,
                        builder.blockEntity.orElseThrow());
            } else {
                block = blocks.registerWithoutItem(blockPath, itemPath,
                        blockProperties -> type.get().apply(blockProperties), properties);
            }
            DataRegistry.add(id, new DataRegistry.GeneratedBlock(
                    block.blockItemId(),
                    () -> new BlockRegistry.Definition(type, builder.properties.definition, builder.blockEntity),
                    builder::buildAssets,
                    builder::buildData
            ));
            if (builder.generateItem) {
                items.registerBlockItem(block, VanillaItemTypes.BLOCK_ITEM.create(), _ -> {});
            }
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
                return codec.register(Identifier.fromNamespaceAndPath(modId, path), () -> value).create();
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
                return mapColor(register(ExtensibleCodecs.MAP_COLOR_TYPES, mapColor));
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
                return lightLevel(register(ExtensibleCodecs.LIGHT_EMISSION_TYPES, lightLevel));
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

            public Properties ignitedByLava(boolean ignitedByLava) {
                definition.ignitedByLava = Optional.of(ignitedByLava);
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
                return validSpawn(register(ExtensibleCodecs.ENTITY_PREDICATE_TYPES, validSpawn));
            }

            public Properties redstoneConductor(ExtensibleCodec.Entry<BlockBehaviour.StatePredicate> redstoneConductor) {
                definition.redstoneConductor = Optional.of(redstoneConductor);
                return this;
            }
            public Properties redstoneConductor(BlockBehaviour.StatePredicate redstoneConductor) {
                return redstoneConductor(register(ExtensibleCodecs.STATE_PREDICATE_TYPES, redstoneConductor));
            }

            public Properties suffocating(ExtensibleCodec.Entry<BlockBehaviour.StatePredicate> suffocating) {
                definition.suffocating = Optional.of(suffocating);
                return this;
            }
            public Properties suffocating(BlockBehaviour.StatePredicate suffocating) {
                return suffocating(register(ExtensibleCodecs.STATE_PREDICATE_TYPES, suffocating));
            }

            public Properties viewBlocking(ExtensibleCodec.Entry<BlockBehaviour.StateArgumentPredicate<AABB>> viewBlocking) {
                definition.viewBlocking = Optional.of(viewBlocking);
                return this;
            }
            public Properties viewBlocking(BlockBehaviour.StateArgumentPredicate<AABB> viewBlocking) {
                return viewBlocking(register(ExtensibleCodecs.COLLISION_PREDICATE_TYPES, viewBlocking));
            }

            public Properties postProcess(ExtensibleCodec.Entry<BlockBehaviour.PostProcess> postProcess) {
                definition.postProcess = Optional.of(postProcess);
                return this;
            }
            public Properties postProcess(BlockBehaviour.PostProcess postProcess) {
                return postProcess(register(ExtensibleCodecs.POST_PROCESS_TYPES, postProcess));
            }

            public Properties emissiveRendering(ExtensibleCodec.Entry<Predicate<BlockState>> emissiveRendering) {
                definition.emissiveRendering = Optional.of(emissiveRendering);
                return this;
            }
            public Properties emissiveRendering(Predicate<BlockState> emissiveRendering) {
                return emissiveRendering(register(ExtensibleCodecs.PREDICATE_TYPES, emissiveRendering));
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
            private final Properties properties;
            private final List<Consumer<Assets>> assetConfigurations = new ArrayList<>();
            private final List<Consumer<Data>> dataConfigurations = new ArrayList<>();
            private boolean generateItem = true;
            private Optional<Supplier<BlockEntityType<?>>> blockEntity = Optional.empty();

            private Builder(String modId, String path) {
                properties = new Properties(modId, path);
            }

            public Builder properties(Consumer<Properties> configure) { configure.accept(properties); return this; }
            public Builder assets(Consumer<Assets> configure) { assetConfigurations.add(configure); return this; }
            public Builder data(Consumer<Data> configure) { dataConfigurations.add(configure); return this; }
            public Builder withoutItem() { generateItem = false; return this; }
            public Builder blockEntity(Supplier<? extends BlockEntityType<?>> type) {
                blockEntity = Optional.of(widen(type));
                return this;
            }

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

        @SuppressWarnings({"rawtypes", "unchecked"})
        private static SuppliedBlock register(UnifiedRegistries.Blocks blocks, String blockPath, String itemPath,
                Function<BlockBehaviour.Properties, ? extends Block> factory,
                Supplier<BlockBehaviour.Properties> properties,
                Supplier<BlockEntityType<?>> blockEntity) {
            Supplier type = blockEntity;
            return blocks.registerWithoutItem(blockPath, itemPath, factory, properties, type);
        }

        @SuppressWarnings("unchecked")
        private static <T> Supplier<T> widen(Supplier<? extends T> supplier) {
            return (Supplier<T>) supplier;
        }
    }
}
