package net.rebel459.unified.fabric.datagen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.fabricmc.fabric.api.datagen.v1.recipe.FabricRecipeOutput;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BootstrapRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Block;
import net.rebel459.unified.fabric.FabricUnifiedDatagen;
import net.rebel459.unified.api.asset.BlockAsset;
import net.rebel459.unified.api.asset.BlockAssets;
import net.rebel459.unified.api.asset.ItemAsset;
import net.rebel459.unified.api.asset.ItemAssets;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.api.codec.ExtensibleCodecs;
import net.rebel459.unified.impl.core.DataProvider;
import net.rebel459.unified.api.util.RecipeProvider;
import net.rebel459.unified.impl.asset.BlockAssetRequest;
import net.rebel459.unified.impl.core.DataProviders;
import net.rebel459.unified.impl.asset.ItemAssetRequest;
import net.rebel459.unified.impl.codec.CodecRequest;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class FabricDatagenProvider implements DataGeneratorEntrypoint {
    public static final Map<BlockAsset<?>, BlockAssetAdapter<?>> BLOCK_ASSET_ADAPTERS = new LinkedHashMap<>();
    public static final Map<ItemAsset<?>, ItemAssetAdapter<?>> ITEM_ASSET_ADAPTERS = new LinkedHashMap<>();
    public static final Map<DataProvider<?>, FabricUnifiedDatagen.ProviderFactory<?>> DATA_PROVIDER_ADAPTERS = new LinkedHashMap<>();

    static {
        FabricUnifiedDatagen.registerBlockAsset(BlockAssets.SIMPLE_CUBE, context -> {
            if (context.familyBases().contains(context.block())) context.family(context.block());
            else context.generator().createTrivialCube(context.block());
        });
        FabricUnifiedDatagen.registerBlockAsset(BlockAssets.LEAVES, context -> context.generator().createTrivialBlock(context.block, TexturedModel.LEAVES));
        FabricUnifiedDatagen.registerBlockAsset(BlockAssets.LOG, context -> context.generator().woodProvider(context.block).logWithHorizontal(context.block));
        FabricUnifiedDatagen.registerBlockAsset(BlockAssets.LOG_UV_LOCKED, context -> context.generator().woodProvider(context.block).logUVLocked(context.block));
        FabricUnifiedDatagen.registerBlockAsset(BlockAssets.LANTERN, context -> context.generator().createLantern(context.block));
        FabricUnifiedDatagen.registerBlockAsset(BlockAssets.DOOR, context -> context.generator().createDoor(context.block));
        FabricUnifiedDatagen.registerBlockAsset(BlockAssets.TRAPDOOR, context -> context.generator().createTrapdoor(context.block));
        FabricUnifiedDatagen.registerBlockAsset(BlockAssets.CHAIN, context -> context.generator().createAxisAlignedPillarBlockCustomModel(context.block(), BlockModelGenerators.plainVariant(TexturedModel.CHAIN.create(context.block(), context.generator().modelOutput))));
        FabricUnifiedDatagen.registerBlockAsset(BlockAssets.TINTED_DOUBLE_PLANT, context -> context.generator().createTintedDoublePlant(context.block));
        FabricUnifiedDatagen.registerBlockAsset(BlockAssets.PARTICLE_ONLY, context -> context.generator().createParticleOnlyBlock(context.block));
        FabricUnifiedDatagen.registerBlockAsset(BlockAssets.ROTATED_PILLAR, context -> context.generator().createRotatedPillarWithHorizontalVariant(context.block, TexturedModel.COLUMN_ALT, TexturedModel.COLUMN_HORIZONTAL_ALT));

        FabricUnifiedDatagen.registerBlockAsset(BlockAssets.TINTED_LEAVES, (definition, context) -> context.generator().createTintedLeaves(context.block, TexturedModel.LEAVES, definition));
        FabricUnifiedDatagen.registerBlockAsset(BlockAssets.SLAB, (definition, context) -> context.family(definition).slab(context.block));
        FabricUnifiedDatagen.registerBlockAsset(BlockAssets.STAIRS, (definition, context) -> context.family(definition).stairs(context.block));
        FabricUnifiedDatagen.registerBlockAsset(BlockAssets.WALL, (definition, context) -> context.family(definition).wall(context.block));
        FabricUnifiedDatagen.registerBlockAsset(BlockAssets.PLANT, (definition, context) -> context.generator.createCrossBlockWithDefaultItem(context.block, convertPlantType(definition)));
        FabricUnifiedDatagen.registerBlockAsset(BlockAssets.DOUBLE_PLANT, (definition, context) -> context.generator().createDoublePlant(context.block, convertPlantType(definition)));
        FabricUnifiedDatagen.registerBlockAsset(BlockAssets.POTTED_PLANT, (definition, context) -> context.generator().createPlant(context.block, definition.potted(), convertPlantType(definition.type())));
        FabricUnifiedDatagen.registerBlockAsset(BlockAssets.CROP, (definition, context) -> context.generator().createCropBlock(context.block, definition.property(), IntStream.range(0, definition.stages()).toArray()));
        FabricUnifiedDatagen.registerBlockAsset(BlockAssets.COPIED_PARTICLE_ONLY, (definition, context) -> context.generator().createParticleOnlyBlock(context.block, definition));
        FabricUnifiedDatagen.registerBlockAsset(BlockAssets.WOOD, (definition, context) -> context.generator().woodProvider(definition).wood(context.block));
        FabricUnifiedDatagen.registerBlockAsset(BlockAssets.HANGING_SIGN, FabricDatagenProvider::createHangingSignModels);
        FabricUnifiedDatagen.registerBlockAsset(BlockAssets.SHELF, (definition, context) -> context.generator().createShelf(context.block, definition));
        FabricUnifiedDatagen.registerBlockAsset(BlockAssets.BUTTON, (definition, context) -> context.family(definition).button(context.block));
        FabricUnifiedDatagen.registerBlockAsset(BlockAssets.FENCE, (definition, context) -> context.family(definition).fence(context.block));
        FabricUnifiedDatagen.registerBlockAsset(BlockAssets.FENCE_GATE, (definition, context) -> context.family(definition).fenceGate(context.block));
        FabricUnifiedDatagen.registerBlockAsset(BlockAssets.PRESSURE_PLATE, (definition, context) -> context.family(definition).pressurePlate(context.block));
        FabricUnifiedDatagen.registerBlockAsset(BlockAssets.SIGN, (definition, context) -> context.family(definition).sign(context.block));

        FabricUnifiedDatagen.registerItemAsset(ItemAssets.GENERATED, context -> context.generator().generateFlatItem(context.item(), ModelTemplates.FLAT_ITEM));
        FabricUnifiedDatagen.registerItemAsset(ItemAssets.HANDHELD, context -> context.generator().generateFlatItem(context.item(), ModelTemplates.FLAT_HANDHELD_ITEM));
        FabricUnifiedDatagen.registerItemAsset(ItemAssets.MACE, context -> context.generator().generateFlatItem(context.item(), ModelTemplates.FLAT_HANDHELD_MACE_ITEM));
        FabricUnifiedDatagen.registerItemAsset(ItemAssets.SPEAR, context -> context.generator().generateFlatItem(context.item(), ModelTemplates.SPEAR_IN_HAND));

        FabricUnifiedDatagen.registerDataProvider(CodecRequest.FILES, (pack, modId, requests) ->
                pack.addProvider((output, registries) -> new JsonProvider(output, registries, modId, requests)));
        FabricUnifiedDatagen.registerDataProvider(DataProviders.LANGUAGES, (pack, modId, requests) ->
                pack.addProvider((FabricDataGenerator.Pack.Factory<LanguageProvider>) output -> new LanguageProvider(output, modId, requests)));
        FabricUnifiedDatagen.registerDataProvider(DataProviders.MODELS, (pack, modId, requests) ->
                pack.addProvider((FabricDataGenerator.Pack.Factory<ModelsProvider>) output -> new ModelsProvider(output, modId, requests)));
        FabricUnifiedDatagen.registerDataProvider(DataProviders.BLOCK_LOOT, (pack, modId, requests) ->
                pack.addProvider((output, registries) -> new BlockLootProvider(output, registries, modId, requests)));
        FabricUnifiedDatagen.registerDataProvider(DataProviders.RECIPES, (pack, modId, requests) ->
                pack.addProvider((output, registries) -> new RecipesProvider(output, registries, modId, requests)));
        FabricUnifiedDatagen.registerDataProvider(DataProviders.TAGS, (pack, modId, requests) ->
                requests.requests(modId).stream()
                        .map(DataProviders.TagRequest::registry)
                        .distinct()
                        .forEach(registry -> registerTagProvider(pack, modId, registry, requests)));
    }

    public static void register(FabricDataGenerator generator) {
        String modId = generator.getModId();
        FabricDataGenerator.Pack pack = generator.createPack();
        dataProviderAdapters().forEach((provider, factory) -> registerDataProvider(pack, modId, provider, factory));
    }

    private static synchronized Map<DataProvider<?>, FabricUnifiedDatagen.ProviderFactory<?>> dataProviderAdapters() {
        return new LinkedHashMap<>(DATA_PROVIDER_ADAPTERS);
    }

    private static <T> void registerTagProvider(FabricDataGenerator.Pack pack, String modId,
            ResourceKey<? extends Registry<T>> registry, DataProvider<DataProviders.TagRequest<?>> requests) {
        pack.addProvider((output, registries) -> new RegistryTagsProvider<>(output, registries, modId, registry, requests));
    }

    @SuppressWarnings("unchecked")
    private static <T> void registerDataProvider(FabricDataGenerator.Pack pack, String modId,
            DataProvider<?> provider, FabricUnifiedDatagen.ProviderFactory<?> factory) {
        ((FabricUnifiedDatagen.ProviderFactory<T>) factory).register(pack, modId, (DataProvider<T>) provider);
    }

    private static BlockModelGenerators.PlantType convertPlantType(BlockAssets.PlantType type) {
        return switch (type) {
            case TINTED -> BlockModelGenerators.PlantType.TINTED;
            case NOT_TINTED -> BlockModelGenerators.PlantType.NOT_TINTED;
            case EMISSIVE_NOT_TINTED -> BlockModelGenerators.PlantType.EMISSIVE_NOT_TINTED;
        };
    }

    private static void createHangingSignModels(BlockAssets.HangingSign definition, BlockModelContext context) {
        TextureMapping textures = TextureMapping.particle(definition.strippedLog());
        MultiVariant hanging0 = BlockModelGenerators.plainVariant(ModelTemplates.HANGING_SIGN_ROT_0.create(context.block(), textures, context.generator().modelOutput));
        MultiVariant hanging1 = BlockModelGenerators.plainVariant(ModelTemplates.HANGING_SIGN_ROT_1.create(context.block(), textures, context.generator().modelOutput));
        MultiVariant hanging2 = BlockModelGenerators.plainVariant(ModelTemplates.HANGING_SIGN_ROT_2.create(context.block(), textures, context.generator().modelOutput));
        MultiVariant hanging3 = BlockModelGenerators.plainVariant(ModelTemplates.HANGING_SIGN_ROT_3.create(context.block(), textures, context.generator().modelOutput));
        MultiVariant attached0 = BlockModelGenerators.plainVariant(ModelTemplates.ATTACHED_HANGING_SIGN_ROT_0.create(context.block(), textures, context.generator().modelOutput));
        MultiVariant attached1 = BlockModelGenerators.plainVariant(ModelTemplates.ATTACHED_HANGING_SIGN_ROT_1.create(context.block(), textures, context.generator().modelOutput));
        MultiVariant attached2 = BlockModelGenerators.plainVariant(ModelTemplates.ATTACHED_HANGING_SIGN_ROT_2.create(context.block(), textures, context.generator().modelOutput));
        MultiVariant attached3 = BlockModelGenerators.plainVariant(ModelTemplates.ATTACHED_HANGING_SIGN_ROT_3.create(context.block(), textures, context.generator().modelOutput));
        context.generator().blockStateOutput.accept(BlockModelGenerators.createHangingSign(
                context.block(), hanging0, hanging1, hanging2, hanging3, attached0, attached1, attached2, attached3
        ));

        Identifier wallModel = ModelTemplates.WALL_HANGING_SIGN.create(definition.wallHangingSign(), textures, context.generator().modelOutput);
        context.generator().blockStateOutput.accept(MultiVariantGenerator.dispatch(
                definition.wallHangingSign(), BlockModelGenerators.plainVariant(wallModel)
        ).with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
    }

    @Override
    public void onInitializeDataGenerator(@NonNull FabricDataGenerator generator) {
        FabricUnifiedDatagen.register(generator);
    }

    @FunctionalInterface public interface BlockAssetAdapter<T> {
        void generate(T value, BlockModelContext context);
    }
    @FunctionalInterface public interface ItemAssetAdapter<T> {
        void generate(T value, ItemModelContext context);
    }

    public record BlockModelContext(
            Identifier id,
            Block block,
            BlockModelGenerators generator,
            Set<Block> familyBases,
            Function<Block, BlockModelGenerators.BlockFamilyProvider> familyProvider
    ) {
        public BlockModelGenerators.BlockFamilyProvider family(Block base) {
            return familyProvider.apply(base);
        }
    }
    public record ItemModelContext(Identifier id, Item item, ItemModelGenerators generator) {}

    public static final class JsonProvider implements net.minecraft.data.DataProvider {

        private final FabricPackOutput output;
        private final CompletableFuture<HolderLookup.Provider> registries;
        private final String modId;
        private final DataProvider<CodecRequest> requests;
        public JsonProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries,
                String modId, DataProvider<CodecRequest> requests) {
            this.output = output;
            this.registries = registries;
            this.modId = modId;
            this.requests = requests;
        }

        @Override public CompletableFuture<?> run(CachedOutput cache) {
            return registries.thenCompose(provider -> {
                List<CompletableFuture<?>> writes = new ArrayList<>();
                DynamicOps<JsonElement> registryOps = provider.createSerializationContext(JsonOps.INSTANCE);
                requests.requests(modId).forEach(request -> writes.add(net.minecraft.data.DataProvider.saveStable(
                        cache, request.encoder().apply(provider, registryOps), output.getOutputFolder().resolve(request.path()))));
                return CompletableFuture.allOf(writes.toArray(CompletableFuture[]::new));
            });
        }

        @Override public String getName() {
            return "Unified JSON files for " + modId;
        }
    }

    public static final class LanguageProvider implements net.minecraft.data.DataProvider {

        private final FabricPackOutput output;
        private final String modId;
        private final DataProvider<DataProviders.LanguageRequest> requests;
        public LanguageProvider(FabricPackOutput output, String modId, DataProvider<DataProviders.LanguageRequest> requests) {
            this.output = output;
            this.modId = modId;
            this.requests = requests;
        }

        @Override public CompletableFuture<?> run(CachedOutput cache) {
            Map<String, JsonObject> translations = new LinkedHashMap<>();
            requests.requests(modId).forEach(request -> {
                var settings = request.settings();
                JsonObject language = translations.computeIfAbsent(settings.language(), _ -> new JsonObject());
                if (request.translation().isEmpty()) {
                    settings.injectedTranslations().ifPresent(path -> injectTranslations(language, path));
                    return;
                }
                DataProviders.Translation translation = request.translation().orElseThrow();
                String name = translation.name().get().orElse(null);
                if (translation.type() == DataProviders.TranslationType.BLOCK) {
                    addTranslation(language, settings, translation.id(), "block", name);
                } else {
                    addItemTranslation(language, settings, translation.id(), name);
                }
            });

            List<CompletableFuture<?>> writes = new ArrayList<>();
            translations.forEach((language, values) -> {
                Path path = output.getOutputFolder().resolve("assets").resolve(modId)
                        .resolve("lang").resolve(language + ".json");
                writes.add(net.minecraft.data.DataProvider.saveStable(cache, values, path));
            });
            return CompletableFuture.allOf(writes.toArray(CompletableFuture[]::new));
        }

        @Override public String getName() { return "Unified translations for " + modId; }

        private void injectTranslations(JsonObject translations, String path) {
            String normalized = path.startsWith("/") ? path.substring(1) : path;
            try (InputStream stream = FabricDatagenProvider.class.getClassLoader().getResourceAsStream(normalized)) {
                if (stream == null) {
                    throw new IllegalArgumentException("Injected translation resource does not exist: " + path);
                }
                try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                    JsonObject injected = JsonParser.parseReader(reader).getAsJsonObject();
                    injected.entrySet().forEach(entry -> translations.add(entry.getKey(), entry.getValue()));
                }
            } catch (IOException exception) {
                throw new IllegalStateException("Failed to read injected translations " + path, exception);
            }
        }

        private void addTranslation(JsonObject translations, DataProviders.GenerationSettings settings, Identifier id, String kind, String explicitName) {
            if (explicitName == null && !settings.autoName()) return;
            translations.addProperty(kind + "." + id.getNamespace() + "." + id.getPath(), explicitName != null ? explicitName : autoName(id.getPath()));
        }

        private void addItemTranslation(JsonObject translations, DataProviders.GenerationSettings settings,
                Identifier id, String explicitName) {
            if (explicitName == null && !settings.autoName()) return;
            Component name = BuiltInRegistries.ITEM.getValueOrThrow(ResourceKey.create(Registries.ITEM, id))
                    .components().get(DataComponents.ITEM_NAME);
            if (name == null || !(name.getContents() instanceof TranslatableContents translation)) return;
            String key = translation.getKey();
            String itemPrefix = "item." + id.getNamespace() + ".";
            String blockPrefix = "block." + id.getNamespace() + ".";
            String path = key.startsWith(itemPrefix) ? key.substring(itemPrefix.length())
                    : key.startsWith(blockPrefix) ? key.substring(blockPrefix.length())
                    : id.getPath();
            translations.addProperty(key, explicitName != null ? explicitName : autoName(path));
        }

    }

    public static final class ModelsProvider extends FabricModelProvider {

        private final String modId;
        private final DataProvider<DataProviders.ModelRequest> requests;
        private final Set<Block> familyBases = Collections.newSetFromMap(new IdentityHashMap<>());
        private final Map<Block, BlockModelGenerators.BlockFamilyProvider> families = new IdentityHashMap<>();
        public ModelsProvider(FabricPackOutput output, String modId, DataProvider<DataProviders.ModelRequest> requests) {
            super(output);
            this.modId = modId;
            this.requests = requests;
        }

        @Override public void generateBlockStateModels(BlockModelGenerators generator) {
            requests.requests(modId).forEach(request -> {
                if (!(request instanceof DataProviders.BlockModels generated)) return;
                generated.assets().get().models().forEach(asset -> {
                    if (isFamilyAsset(asset.type())) familyBases.add((Block) asset.value());
                });
            });
            requests.requests(modId).forEach(request -> {
                if (!(request instanceof DataProviders.BlockModels generated)) return;
                Identifier id = generated.id();
                Block block = BuiltInRegistries.BLOCK.getValue(id);
                BlockModelContext context = new BlockModelContext(
                        id,
                        block,
                        generator,
                        familyBases,
                        base -> families.computeIfAbsent(base, generator::family)
                );
                generated.assets().get().models().forEach(asset -> generateBlockAsset(asset, context));
            });
        }

        private static boolean isFamilyAsset(BlockAsset<?> asset) {
            return asset == BlockAssets.SLAB || asset == BlockAssets.STAIRS || asset == BlockAssets.WALL
                    || asset == BlockAssets.BUTTON || asset == BlockAssets.FENCE || asset == BlockAssets.FENCE_GATE
                    || asset == BlockAssets.PRESSURE_PLATE || asset == BlockAssets.SIGN;
        }

        @Override public void generateItemModels(ItemModelGenerators generator) {
            requests.requests(modId).forEach(request -> {
                if (!(request instanceof DataProviders.ItemModels generated)) return;
                Identifier id = generated.id();
                Item item = BuiltInRegistries.ITEM.getValue(id);
                ItemModelContext context = new ItemModelContext(id, item, generator);
                generated.assets().get().models().forEach(asset -> generateItemAsset(asset, context));
            });
        }
    }

    public static final class BlockLootProvider extends FabricBlockLootSubProvider {

        private final String modId;
        private final DataProvider<Consumer<DataProviders.BlockLootGenerator>> requests;
        public BlockLootProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries, String modId, DataProvider<Consumer<DataProviders.BlockLootGenerator>> requests) {
            super(output, registries);
            this.modId = modId;
            this.requests = requests;
        }

        @Override public void generate() {
            requests.requests(modId).forEach(generator -> generator.accept(this::add));
        }
    }

    public static final class RecipesProvider implements net.minecraft.data.DataProvider {

        private final FabricPackOutput output;
        private final CompletableFuture<HolderLookup.Provider> registries;
        private final String modId;
        private final DataProvider<DataProviders.RecipeRequest> requests;

        public RecipesProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries,
                String modId, DataProvider<DataProviders.RecipeRequest> requests) {
            this.output = output;
            this.registries = registries;
            this.modId = modId;
            this.requests = requests;
        }

        @Override
        public CompletableFuture<?> run(CachedOutput cache) {
            return registries.thenCompose(provider -> generate(cache, provider));
        }

        private CompletableFuture<?> generate(CachedOutput cache, HolderLookup.Provider registries) {
            ConditionalBootstrapContext<Recipe<?>> recipes = new RecipeBootstrapContext(registries, modId);
            ConditionalBootstrapContext<Advancement> advancements =
                    new ConditionalBootstrapContext<>(registries, Registries.ADVANCEMENT);

            requests.requests(modId).forEach(request -> {
                recipes.requirement = request.requirement();
                advancements.requirement = request.requirement();
                net.minecraft.data.recipes.RecipeProvider vanilla = new net.minecraft.data.recipes.RecipeProvider(recipes, advancements) {
                    @Override public void buildRecipes() {}
                };
                request.generator().accept(new RecipeProvider(vanilla, recipes, advancements) {
                    @Override public void buildRecipes() {}
                });
            });

            DynamicOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, new RegistryOps.RegistryInfoLookup() {
                @Override
                public <T> Optional<HolderGetter<T>> lookup(ResourceKey<? extends Registry<? extends T>> key) {
                    return localLookup(key, recipes, advancements, registries);
                }
            });
            List<CompletableFuture<?>> writes = new ArrayList<>();
            recipes.entries.forEach((key, generated) -> writes.add(save(cache, ops, key, generated,
                    Recipe.DIRECT_CODEC, Registries.RECIPE, "unified/recipes")));
            advancements.entries.forEach((key, generated) -> writes.add(save(cache, ops, key, generated,
                    Advancement.CODEC, Registries.ADVANCEMENT, "unified/advancements")));
            return CompletableFuture.allOf(writes.toArray(CompletableFuture[]::new));
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        private static <T> Optional<HolderGetter<T>> localLookup(
                ResourceKey<? extends Registry<? extends T>> key,
                ConditionalBootstrapContext<Recipe<?>> recipes,
                ConditionalBootstrapContext<Advancement> advancements,
                HolderLookup.Provider registries) {
            if (key.equals(Registries.RECIPE)) return Optional.of((HolderGetter) recipes.entryLookup);
            if (key.equals(Registries.ADVANCEMENT)) return Optional.of((HolderGetter) advancements.entryLookup);
            return (Optional) registries.lookup(key);
        }

        private <T> CompletableFuture<?> save(CachedOutput cache, DynamicOps<JsonElement> ops, ResourceKey<T> key,
                Generated<T> generated, com.mojang.serialization.Codec<T> codec,
                ResourceKey<? extends Registry<T>> registry, String conditionalDirectory) {
            JsonObject json = codec.encodeStart(ops, generated.value()).getOrThrow().getAsJsonObject();
            Path path;
            if (generated.requirement().isPresent()) {
                json.add("load_requirements", ExtensibleCodecs.LOAD_REQUIREMENT.codec()
                        .encodeStart(ops, generated.requirement().orElseThrow()).getOrThrow());
                path = output.getOutputFolder().resolve("data").resolve(key.identifier().getNamespace())
                        .resolve(conditionalDirectory).resolve(key.identifier().getPath() + ".json");
            } else {
                path = output.createRegistryElementsPathProvider(registry).json(key);
            }
            return net.minecraft.data.DataProvider.saveStable(cache, json, path);
        }

        @Override public String getName() {
            return "Unified recipes for " + modId;
        }

        private record Generated<T>(T value, Optional<ExtensibleCodec.Entry<Boolean>> requirement) {}

        private static class ConditionalBootstrapContext<T> implements BootstrapContext<T> {
            private final HolderLookup.Provider registries;
            private final ResourceKey<? extends Registry<T>> registry;
            private final Map<ResourceKey<T>, Generated<T>> entries = new LinkedHashMap<>();
            private final BootstrapRegistry<T> entryLookup;
            private Optional<ExtensibleCodec.Entry<Boolean>> requirement = Optional.empty();

            private ConditionalBootstrapContext(HolderLookup.Provider registries,
                    ResourceKey<? extends Registry<T>> registry) {
                this.registries = registries;
                this.registry = registry;
                this.entryLookup = new BootstrapRegistry<>(registry, Lifecycle.stable());
            }

            @Override
            public Holder.Reference<T> register(ResourceKey<T> key, T value) {
                if (entries.putIfAbsent(key, new Generated<>(value, requirement)) != null) {
                    throw new IllegalStateException("Duplicate generated " + registry.identifier()
                            + " entry " + key.identifier());
                }
                return entryLookup.getOrThrow(key);
            }

            @Override
            @SuppressWarnings("unchecked")
            public <S> HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> key) {
                if (key.equals(registry)) return (HolderGetter<S>) entryLookup;
                return registries.lookupOrThrow(key);
            }

            @Override
            @SuppressWarnings("unchecked")
            public <S> java.util.stream.Stream<Holder.Reference<S>> listContextElements(
                    ResourceKey<? extends Registry<? extends S>> key) {
                if (key.equals(registry)) {
                    return (java.util.stream.Stream<Holder.Reference<S>>) (java.util.stream.Stream<?>) entryLookup.listElements();
                }
                return registries.lookupOrThrow(key).listElements();
            }
        }

        private static final class RecipeBootstrapContext extends ConditionalBootstrapContext<Recipe<?>>
                implements FabricRecipeOutput {
            private final String modId;

            private RecipeBootstrapContext(HolderLookup.Provider registries, String modId) {
                super(registries, Registries.RECIPE);
                this.modId = modId;
            }

            @Override
            public Identifier getRecipeIdentifier(Identifier identifier) {
                return Identifier.fromNamespaceAndPath(modId, identifier.getPath());
            }
        }
    }

    public static final class RegistryTagsProvider<T> extends FabricTagsProvider<T> {
        private final String modId;
        private final ResourceKey<? extends Registry<T>> registry;
        private final DataProvider<DataProviders.TagRequest<?>> requests;

        public RegistryTagsProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries,
                String modId, ResourceKey<? extends Registry<T>> registry,
                DataProvider<DataProviders.TagRequest<?>> requests) {
            super(output, registry, registries);
            this.modId = modId;
            this.registry = registry;
            this.requests = requests;
        }

        @Override protected void addTags(HolderLookup.Provider registries) {
            requests.requests(modId).forEach(request -> {
                if (!registry.equals(request.registry())) return;
                generate(request, new DataProviders.TagGenerator<>() {
                @Override public void add(TagKey<T> tagKey, ResourceKey<T> value) {
                    builder(tagKey).add(value);
                }

                @Override public void addOptional(TagKey<T> tagKey, ResourceKey<T> value) {
                    builder(tagKey).addOptional(value);
                }
                });
            });
        }

        @SuppressWarnings("unchecked")
        private void generate(DataProviders.TagRequest<?> request, DataProviders.TagGenerator<T> generator) {
            ((DataProviders.TagRequest<T>) request).generator().accept(generator);
        }
    }

    private static <T> void generateBlockAsset(BlockAssetRequest<T> request, BlockModelContext context) {
        BlockAssetAdapter<T> adapter = (BlockAssetAdapter<T>) BLOCK_ASSET_ADAPTERS.get(request.type());
        if (adapter == null) throw new IllegalStateException("No Fabric block asset binding registered for " + request.type().id());
        adapter.generate(request.value(), context);
    }
    private static <T> void generateItemAsset(ItemAssetRequest<T> request, ItemModelContext context) {
        ItemAssetAdapter<T> adapter = (ItemAssetAdapter<T>) ITEM_ASSET_ADAPTERS.get(request.type());
        if (adapter == null) throw new IllegalStateException("No Fabric item asset binding registered for " + request.type().id());
        adapter.generate(request.value(), context);
    }
    private static String autoName(String path) {
        return Arrays.stream(path.split("_"))
                .filter(part -> !part.isEmpty())
                .map(part -> Character.toUpperCase(part.charAt(0)) + part.substring(1))
                .collect(Collectors.joining(" "));
    }
}
