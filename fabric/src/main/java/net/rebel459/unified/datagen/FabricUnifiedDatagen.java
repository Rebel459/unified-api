package net.rebel459.unified.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.rebel459.unified.util.data.registry.BlockRegistry;
import net.rebel459.unified.util.data.registry.BlockSetTypeRegistry;
import net.rebel459.unified.util.data.registry.ItemRegistry;
import net.rebel459.unified.util.datagen.BlockAsset;
import net.rebel459.unified.util.datagen.BlockAssets;
import net.rebel459.unified.util.datagen.ItemAsset;
import net.rebel459.unified.util.datagen.ItemAssets;
import net.rebel459.unified.util.RecipeProvider;
import net.rebel459.unified.util.datagen.impl.BlockAssetRequest;
import net.rebel459.unified.util.datagen.impl.DataRegistry;
import net.rebel459.unified.util.datagen.impl.ItemAssetRequest;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class FabricUnifiedDatagen implements DataGeneratorEntrypoint {
    private static final Map<BlockAsset<?>, BlockAssetAdapter<?>> BLOCK_ASSET_ADAPTERS = new LinkedHashMap<>();
    private static final Map<ItemAsset<?>, ItemAssetAdapter<?>> ITEM_ASSET_ADAPTERS = new LinkedHashMap<>();

    static {
        registerBlockAsset(BlockAssets.SIMPLE_CUBE, context -> {
            if (context.familyBases().contains(context.block())) context.family(context.block());
            else context.generator().createTrivialCube(context.block());
        });
        registerBlockAsset(BlockAssets.LEAVES, context -> context.generator().createTrivialBlock(context.block, TexturedModel.LEAVES));
        registerBlockAsset(BlockAssets.LOG, context -> context.generator().woodProvider(context.block).logWithHorizontal(context.block));
        registerBlockAsset(BlockAssets.LOG_UV_LOCKED, context -> context.generator().woodProvider(context.block).logUVLocked(context.block));
        registerBlockAsset(BlockAssets.LANTERN, context -> context.generator().createLantern(context.block));
        registerBlockAsset(BlockAssets.DOOR, context -> context.generator().createDoor(context.block));
        registerBlockAsset(BlockAssets.TRAPDOOR, context -> context.generator().createTrapdoor(context.block));
        registerBlockAsset(BlockAssets.CHAIN, context -> context.generator().createAxisAlignedPillarBlockCustomModel(context.block(), BlockModelGenerators.plainVariant(TexturedModel.CHAIN.create(context.block(), context.generator().modelOutput))));
        registerBlockAsset(BlockAssets.TINTED_DOUBLE_PLANT, context -> context.generator().createTintedDoublePlant(context.block));
        registerBlockAsset(BlockAssets.PARTICLE_ONLY, context -> context.generator().createParticleOnlyBlock(context.block));
        registerBlockAsset(BlockAssets.ROTATED_PILLAR, context -> context.generator().createRotatedPillarWithHorizontalVariant(context.block, TexturedModel.COLUMN_ALT, TexturedModel.COLUMN_HORIZONTAL_ALT));

        registerBlockAsset(BlockAssets.TINTED_LEAVES, (definition, context) -> context.generator().createTintedLeaves(context.block, TexturedModel.LEAVES, definition));
        registerBlockAsset(BlockAssets.SLAB, (definition, context) -> context.family(definition).slab(context.block));
        registerBlockAsset(BlockAssets.STAIRS, (definition, context) -> context.family(definition).stairs(context.block));
        registerBlockAsset(BlockAssets.WALL, (definition, context) -> context.family(definition).wall(context.block));
        registerBlockAsset(BlockAssets.PLANT, (definition, context) -> {
            context.generator.registerSimpleItemModel(context.block.asItem(), convertPlantType(definition).createItemModel(context.generator, context.block));
            context.generator.createCrossBlock(context.block, convertPlantType(definition));
        });
        registerBlockAsset(BlockAssets.DOUBLE_PLANT, (definition, context) -> context.generator().createDoublePlant(context.block, convertPlantType(definition)));
        registerBlockAsset(BlockAssets.POTTED_PLANT, (definition, context) -> context.generator().createPlant(context.block, definition.potted(), convertPlantType(definition.type())));
        registerBlockAsset(BlockAssets.CROP, (definition, context) -> context.generator().createCropBlock(context.block, definition.property(), IntStream.range(0, definition.stages()).toArray()));
        registerBlockAsset(BlockAssets.COPIED_PARTICLE_ONLY, (definition, context) -> context.generator().createParticleOnlyBlock(context.block, definition));
        registerBlockAsset(BlockAssets.WOOD, (definition, context) -> context.generator().woodProvider(definition).wood(context.block));
        registerBlockAsset(BlockAssets.HANGING_SIGN, (definition, context) -> context.generator().createHangingSign(definition.strippedLog(), context.block, definition.wallHangingSign()));
        registerBlockAsset(BlockAssets.SHELF, (definition, context) -> context.generator().createShelf(context.block, definition));
        registerBlockAsset(BlockAssets.BUTTON, (definition, context) -> context.family(definition).button(context.block));
        registerBlockAsset(BlockAssets.FENCE, (definition, context) -> context.family(definition).fence(context.block));
        registerBlockAsset(BlockAssets.FENCE_GATE, (definition, context) -> context.family(definition).fenceGate(context.block));
        registerBlockAsset(BlockAssets.PRESSURE_PLATE, (definition, context) -> context.family(definition).pressurePlate(context.block));
        registerBlockAsset(BlockAssets.SIGN, (definition, context) -> context.family(definition).sign(context.block));

        registerItemAsset(ItemAssets.GENERATED, context -> context.generator().generateFlatItem(context.item(), ModelTemplates.FLAT_ITEM));
        registerItemAsset(ItemAssets.HANDHELD, context -> context.generator().generateFlatItem(context.item(), ModelTemplates.FLAT_HANDHELD_ITEM));
        registerItemAsset(ItemAssets.MACE, context -> context.generator().generateFlatItem(context.item(), ModelTemplates.FLAT_HANDHELD_MACE_ITEM));
        registerItemAsset(ItemAssets.SPEAR, context -> context.generator().generateFlatItem(context.item(), ModelTemplates.SPEAR_IN_HAND));
    }

    private static BlockModelGenerators.PlantType convertPlantType(BlockAssets.PlantType type) {
        return switch (type) {
            case TINTED -> BlockModelGenerators.PlantType.TINTED;
            case NOT_TINTED -> BlockModelGenerators.PlantType.NOT_TINTED;
            case EMISSIVE_NOT_TINTED -> BlockModelGenerators.PlantType.EMISSIVE_NOT_TINTED;
        };
    }

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        registerGenerator(generator);
    }

    public static void registerGenerator(FabricDataGenerator generator) {
        String modId = generator.getModId();
        FabricDataGenerator.Pack pack = generator.createPack();
        pack.addProvider((FabricDataGenerator.Pack.Factory<DefinitionProvider>) output -> new DefinitionProvider(output, modId));
        pack.addProvider((FabricDataGenerator.Pack.Factory<LanguageProvider>) output -> new LanguageProvider(output, modId));
        pack.addProvider((FabricDataGenerator.Pack.Factory<ModelsProvider>) output -> new ModelsProvider(output, modId));
        pack.addProvider((output, registries) -> new BlockLootProvider(output, registries, modId));
        pack.addProvider((output, registries) -> new RecipesProvider(output, registries, modId));
        BlockTagsProvider blockTags = pack.addProvider((output, registries) -> new BlockTagsProvider(output, registries, modId));
        pack.addProvider((output, registries) -> new ItemTagsProvider(output, registries, modId, blockTags));
    }

    public static synchronized void registerBlockAsset(BlockAsset<Void> type, Consumer<BlockModelContext> generator) {
        registerBlockAsset(type, (_, context) -> generator.accept(context));
    }
    public static synchronized <T> void registerBlockAsset(BlockAsset<T> type, BiConsumer<T, BlockModelContext> generator) {
        BlockAssetAdapter<T> adapter = generator::accept;
        if (BLOCK_ASSET_ADAPTERS.putIfAbsent(type, adapter) != null) throw new IllegalArgumentException("Duplicate Fabric block asset binding for " + type.id());
    }
    public static synchronized void registerItemAsset(ItemAsset<Void> type, Consumer<ItemModelContext> generator) {
        registerItemAsset(type, (_, context) -> generator.accept(context));
    }
    public static synchronized <T> void registerItemAsset(ItemAsset<T> type, BiConsumer<T, ItemModelContext> generator) {
        ItemAssetAdapter<T> adapter = generator::accept;
        if (ITEM_ASSET_ADAPTERS.putIfAbsent(type, adapter) != null) throw new IllegalArgumentException("Duplicate Fabric item asset binding for " + type.id());
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

    private static final class DefinitionProvider implements DataProvider {

        private final FabricPackOutput output;
        private final String modId;
        private DefinitionProvider(FabricPackOutput output, String modId) {
            this.output = output; this.modId = modId;
        }

        @Override public CompletableFuture<?> run(CachedOutput cache) {
            List<CompletableFuture<?>> writes = new ArrayList<>();
            DataRegistry.blocks().forEach((id, generated) -> { if (owns(id, modId)) writes.add(saveDefinition(cache, BlockRegistry.CODEC.encodeStart(JsonOps.INSTANCE, generated.definition().get()).getOrThrow(), definitionPath(output, id, "blocks"), modId)); });
            DataRegistry.items().forEach((id, generated) -> { if (owns(id, modId)) writes.add(saveDefinition(cache, ItemRegistry.CODEC.encodeStart(JsonOps.INSTANCE, generated.definition().get()).getOrThrow(), definitionPath(output, id, "items"), modId)); });
            DataRegistry.blockSetTypes().forEach((id, definition) -> { if (owns(id, modId)) writes.add(saveDefinition(cache, BlockSetTypeRegistry.CODEC.encodeStart(JsonOps.INSTANCE, definition.get()).getOrThrow(), definitionPath(output, id, "block_set_types"), modId)); });
            return CompletableFuture.allOf(writes.toArray(CompletableFuture[]::new));
        }

        @Override public String getName() {
            return "Unified registry definitions for " + modId;
        }
    }

    private static final class LanguageProvider implements DataProvider {

        private final FabricPackOutput output;
        private final String modId;
        private LanguageProvider(FabricPackOutput output, String modId) {
            this.output = output; this.modId = modId;
        }

        @Override public CompletableFuture<?> run(CachedOutput cache) {
            DataRegistry.GenerationSettings settings = DataRegistry.settings(modId);
            JsonObject translations = new JsonObject();
            settings.injectedTranslations().ifPresent(path -> {
                String normalized = path.startsWith("/") ? path.substring(1) : path;
                try (InputStream stream = FabricUnifiedDatagen.class.getClassLoader().getResourceAsStream(normalized)) {
                    if (stream == null) throw new IllegalArgumentException("Injected translation resource does not exist: " + path);
                    try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                        JsonObject injected = JsonParser.parseReader(reader).getAsJsonObject();
                        injected.entrySet().forEach(entry -> translations.add(entry.getKey(), entry.getValue()));
                    }
                } catch (IOException exception) {
                    throw new IllegalStateException("Failed to read injected translations " + path, exception);
                }
            });
            DataRegistry.blocks().forEach((id, generated) -> addTranslation(translations, settings, id, "block", generated.assets().get().name().orElse(null)));
            DataRegistry.items().forEach((id, generated) -> addTranslation(translations, settings, id, "item", generated.assets().get().name().orElse(null)));
            Path path = output.getOutputFolder().resolve("assets").resolve(modId).resolve("lang").resolve(settings.language() + ".json");
            return DataProvider.saveStable(cache, translations, path);
        }

        @Override public String getName() { return "Unified translations for " + modId; }

        private void addTranslation(JsonObject translations, DataRegistry.GenerationSettings settings, Identifier id, String kind, String explicitName) {
            if (!owns(id, modId) || explicitName == null && !settings.autoName()) return;
            translations.addProperty(kind + "." + id.getNamespace() + "." + id.getPath(), explicitName != null ? explicitName : autoName(id.getPath()));
        }

    }

    private static final class ModelsProvider extends FabricModelProvider {

        private final String modId;
        private final Set<Block> familyBases = Collections.newSetFromMap(new IdentityHashMap<>());
        private final Map<Block, BlockModelGenerators.BlockFamilyProvider> families = new IdentityHashMap<>();
        private ModelsProvider(FabricPackOutput output, String modId) {
            super(output); this.modId = modId;
        }

        @Override public void generateBlockStateModels(BlockModelGenerators generator) {
            DataRegistry.blocks().forEach((id, generated) -> {
                if (!owns(id, modId)) return;
                generated.assets().get().models().forEach(request -> {
                    if (isFamilyAsset(request.type())) familyBases.add((Block) request.value());
                });
            });
            DataRegistry.blocks().forEach((id, generated) -> {
                if (!owns(id, modId)) return;
                Block block = BuiltInRegistries.BLOCK.getValue(id);
                BlockModelContext context = new BlockModelContext(
                        id,
                        block,
                        generator,
                        familyBases,
                        base -> families.computeIfAbsent(base, generator::family)
                );
                generated.assets().get().models().forEach(request -> generateBlockAsset(request, context));
            });
        }

        private static boolean isFamilyAsset(BlockAsset<?> asset) {
            return asset == BlockAssets.SLAB || asset == BlockAssets.STAIRS || asset == BlockAssets.WALL
                    || asset == BlockAssets.BUTTON || asset == BlockAssets.FENCE || asset == BlockAssets.FENCE_GATE
                    || asset == BlockAssets.PRESSURE_PLATE || asset == BlockAssets.SIGN;
        }

        @Override public void generateItemModels(ItemModelGenerators generator) {
            DataRegistry.items().forEach((id, generated) -> {
                if (!owns(id, modId)) return;
                Item item = BuiltInRegistries.ITEM.getValue(id);
                ItemModelContext context = new ItemModelContext(id, item, generator);
                generated.assets().get().models().forEach(request -> generateItemAsset(request, context));
            });
        }
    }

    private static final class BlockLootProvider extends FabricBlockLootSubProvider {

        private final String modId;
        private BlockLootProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries, String modId) {
            super(output, registries); this.modId = modId;
        }

        @Override public void generate() {
            DataRegistry.blocks().forEach((id, generated) -> {
                if (!owns(id, modId)) return;
                Block block = BuiltInRegistries.BLOCK.getValue(id);
                generated.data().get().loot().ifPresent(factory -> add(block, factory.apply(block)));
            });
        }
    }

    private static final class RecipesProvider extends FabricRecipeProvider {

        private final String modId;
        private RecipesProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries, String modId) {
            super(output, registries); this.modId = modId;
        }

        @Override protected net.minecraft.data.recipes.RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new net.minecraft.data.recipes.RecipeProvider(registries, output) {
                @Override public void buildRecipes() {
                    RecipeProvider recipeProvider = new RecipeProvider(this, registries, output) {
                        @Override
                        public void buildRecipes() {}
                    };
                    DataRegistry.blocks().forEach((id, generated) -> {
                        if (!owns(id, modId)) return;
                        Item item = BuiltInRegistries.ITEM.getValue(id);
                        generated.data().get().recipe().ifPresent(factory -> factory.accept(item, recipeProvider));
                    });
                    DataRegistry.items().forEach((id, generated) -> {
                        if (!owns(id, modId)) return;
                        Item item = BuiltInRegistries.ITEM.getValue(id);
                        generated.data().get().recipe().ifPresent(factory -> factory.accept(item, recipeProvider));
                    });
                }
            };
        }

        @Override public String getName() {
            return "Unified recipes for " + modId;
        }
    }

    private static final class BlockTagsProvider extends FabricTagsProvider.BlockTagsProvider {

        private final String modId;
        private BlockTagsProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries, String modId) {
            super(output, registries); this.modId = modId;
        }

        @Override protected void addTags(HolderLookup.Provider registries) {
            DataRegistry.blocks().forEach((id, generated) -> {
                if (!owns(id, modId)) return;
                Block block = BuiltInRegistries.BLOCK.getValue(id);
                generated.data().get().tags().forEach(tag -> valueLookupBuilder(tag).add(block));
                generated.data().get().optionalTags().forEach(tag -> valueLookupBuilder(tag).addOptional(block));
            });
        }
    }

    private static final class ItemTagsProvider extends FabricTagsProvider.ItemTagsProvider {

        private final String modId;
        private ItemTagsProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries, String modId, BlockTagsProvider blocks) {
            super(output, registries, blocks); this.modId = modId;
        }

        @Override protected void addTags(HolderLookup.Provider registries) {
            DataRegistry.items().forEach((id, generated) -> {
                if (!owns(id, modId)) return;
                Item item = BuiltInRegistries.ITEM.getValue(id);
                generated.data().get().tags().forEach(tag -> valueLookupBuilder(tag).add(item));
                generated.data().get().optionalTags().forEach(tag -> valueLookupBuilder(tag).addOptional(item));
            });
            DataRegistry.blocks().forEach((id, generated) -> {
                if (!owns(id, modId)) return;
                Item item = BuiltInRegistries.ITEM.getValue(id);
                generated.data().get().itemTags().forEach(tag -> valueLookupBuilder(tag).add(item));
                generated.data().get().optionalItemTags().forEach(tag -> valueLookupBuilder(tag).addOptional(item));
            });
        }
    }

    private static boolean owns(Identifier id, String modId) {
        return id.getNamespace().equals(modId);
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
    private static CompletableFuture<?> saveDefinition(CachedOutput cache, JsonElement encoded, Path path, String modId) {
        JsonObject definition = encoded.getAsJsonObject();
        var metadata = DataRegistry.settings(modId).metadata();
        if (metadata.priority() != 0) definition.addProperty("priority", metadata.priority());
        if (!metadata.dependencies().isEmpty()) {
            JsonArray dependencies = new JsonArray();
            metadata.dependencies().forEach(dependencies::add);
            definition.add("dependencies", dependencies);
        }
        return DataProvider.saveStable(cache, definition, path);
    }

    private static String autoName(String path) {
        return Arrays.stream(path.split("_"))
                .filter(part -> !part.isEmpty())
                .map(part -> Character.toUpperCase(part.charAt(0)) + part.substring(1))
                .collect(Collectors.joining(" "));
    }
    private static Path definitionPath(FabricPackOutput output, Identifier id, String kind) {
        return output.getOutputFolder().resolve("data").resolve(id.getNamespace()).resolve("unified/registry").resolve(kind).resolve(id.getPath() + ".json");
    }
}
