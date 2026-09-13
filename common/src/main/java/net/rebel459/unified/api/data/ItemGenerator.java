package net.rebel459.unified.api.data;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.rebel459.unified.api.asset.ItemAsset;
import net.rebel459.unified.api.asset.ItemAssets;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.api.codec.ExtensibleCodecs;
import net.rebel459.unified.api.codec.CodecGenerator;
import net.rebel459.unified.api.core.SuppliedBlock;
import net.rebel459.unified.api.core.SuppliedItem;
import net.rebel459.unified.api.core.UnifiedRegistries;
import net.rebel459.unified.api.util.RecipeProvider;
import net.rebel459.unified.impl.core.DataProviders;
import net.rebel459.unified.impl.asset.ItemAssetRequest;
import net.rebel459.unified.impl.registry.ItemRegistry;

import java.util.*;
import java.util.function.*;

public class ItemGenerator {

    private final String modId;
    private final String namespace;
    private final DataProviders.GenerationSettings settings;
    private final UnifiedRegistries.Items items;

    public ItemGenerator(String modId, String namespace, DataProviders.GenerationSettings settings, UnifiedRegistries.Items runtime) {
        this.modId = modId;
        this.namespace = namespace;
        this.settings = settings;
        this.items = runtime;
    }

    public SuppliedItem register(String path, Function<Item.Properties, Item> type, Consumer<Builder> builder) {
        return register(path, ExtensibleCodecs.ITEM_TYPES.register(Identifier.fromNamespaceAndPath(namespace, path), () -> type).create(), builder);
    }

    public SuppliedItem register(String path, ExtensibleCodec.Entry<Function<Item.Properties, Item>> type, Consumer<Builder> builder) {
        Builder finalBuilder = new Builder();
        builder.accept(finalBuilder);
        Identifier id = Identifier.fromNamespaceAndPath(namespace, path);
        Supplier<Item.Properties> runtimeProperties = () -> {
            Item.Properties createProperties = new Item.Properties();
            finalBuilder.properties.accept(createProperties);
            return createProperties;
        };
        SuppliedItem registered = items.register(path, itemProperties -> type.get().apply(itemProperties), runtimeProperties);
        CodecGenerator.registry(modId, id, "items", settings.metadata().priority(), settings.metadata().requirement(),
                ItemRegistry.CODEC, () -> ItemRegistry.Definition.item(type,
                                componentValues(finalBuilder.properties, ResourceKey.create(Registries.ITEM, id))));
        DataProviders.MODELS.add(modId, new DataProviders.ItemModels(id, finalBuilder::buildAssets));
        DataProviders.LANGUAGES.add(modId, new DataProviders.LanguageRequest(settings, Optional.of(
                new DataProviders.Translation(id, DataProviders.TranslationType.ITEM,
                        () -> finalBuilder.buildAssets().name()))));
        finalBuilder.registerData(modId, ResourceKey.create(Registries.ITEM, id), registered);
        return registered;
    }

    public SuppliedItem registerBlockItem(SuppliedBlock block, BiFunction<Block, Item.Properties, Item> type, Consumer<Builder> builder) {
        Identifier id = block.blockItemId().item().identifier();
        return registerBlockItem(block, ExtensibleCodecs.BLOCK_ITEM_TYPES.register(id, () -> type).create(), builder);
    }

    public SuppliedItem registerBlockItem(SuppliedBlock block, ExtensibleCodec.Entry<BiFunction<Block, Item.Properties, Item>> type, Consumer<Builder> builder) {
        Builder finalBuilder = new Builder();
        builder.accept(finalBuilder);
        Identifier id = block.blockItemId().item().identifier();
        Supplier<Item.Properties> runtimeProperties = () -> {
            Item.Properties properties = new Item.Properties();
            finalBuilder.properties.accept(properties);
            return properties;
        };
        SuppliedItem registered = items.registerBlockItem(
                block.blockItemId(), block, type.get(), runtimeProperties);
        CodecGenerator.registry(modId, id, "items", settings.metadata().priority(), settings.metadata().requirement(),
                ItemRegistry.CODEC, () -> ItemRegistry.Definition.blockItem(block, type,
                                componentValues(finalBuilder.properties, block.blockItemId().item())));
        DataProviders.MODELS.add(modId, new DataProviders.ItemModels(id, finalBuilder::buildAssets));
        DataProviders.LANGUAGES.add(modId, new DataProviders.LanguageRequest(settings, Optional.of(
                new DataProviders.Translation(id, DataProviders.TranslationType.ITEM,
                        () -> finalBuilder.buildAssets().name()))));
        finalBuilder.registerData(modId, block.blockItemId().item(), registered);
        return registered;
    }

    private static Map<DataComponentType<?>, Object> componentValues(Consumer<Item.Properties> properties, ResourceKey<Item> key) {
        Item.Properties finalProperties = new Item.Properties();
        properties.accept(finalProperties);
        DataComponentMap.Builder componentBuilder = DataComponentMap.builder();
        finalProperties.componentInitializer.run(
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

        public Builder properties(Consumer<Item.Properties> properties) {
            this.properties = properties;
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

        private DataProviders.ItemAssets buildAssets() {
            Assets assets = new Assets();
            assetConfigurations.forEach(configure -> configure.accept(assets));
            return assets.build();
        }

        private void registerData(String modId, ResourceKey<Item> key, Supplier<Item> item) {
            Data data = new Data(modId, key, item);
            dataConfigurations.forEach(configure -> configure.accept(data));
            data.register();
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

        private DataProviders.ItemAssets build() {
            return new DataProviders.ItemAssets(name, List.copyOf(models));
        }
    }

    public static final class Data {
        private final String modId;
        private final ResourceKey<Item> key;
        private final Supplier<Item> item;
        private Consumer<DataProviders.TagGenerator<Item>> tags = _ -> {};
        private BiConsumer<Item, RecipeProvider> recipe;

        private Data(String modId, ResourceKey<Item> key, Supplier<Item> item) {
            this.modId = modId;
            this.key = key;
            this.item = item;
        }

        public Data tag(TagKey<Item> tag) {
            tags = tags.andThen(generator -> generator.add(tag, key));
            return this;
        }

        public Data optionalTag(TagKey<Item> tag) {
            tags = tags.andThen(generator -> generator.addOptional(tag, key));
            return this;
        }

        public Data recipe(BiConsumer<Item, RecipeProvider> factory) {
            recipe = factory;
            return this;
        }

        private void register() {
            DataProviders.TAGS.add(modId, new DataProviders.TagRequest<>(Registries.ITEM, tags));
            if (recipe != null) DataProviders.RECIPES.add(modId, provider -> recipe.accept(item.get(), provider));
        }
    }
}
