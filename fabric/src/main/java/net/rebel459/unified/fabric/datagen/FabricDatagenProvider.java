package net.rebel459.unified.fabric.datagen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.DynamicOps;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.RegistryOps;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Block;
import net.rebel459.unified.api.codec.ExtensibleCodecs;
import net.rebel459.unified.fabric.FabricUnifiedDatagen;
import net.rebel459.unified.impl.registry.BlockRegistry;
import net.rebel459.unified.impl.registry.BlockSetTypeRegistry;
import net.rebel459.unified.api.asset.BlockAsset;
import net.rebel459.unified.api.asset.BlockAssets;
import net.rebel459.unified.api.asset.ItemAsset;
import net.rebel459.unified.api.asset.ItemAssets;
import net.rebel459.unified.impl.registry.ItemRegistry;
import net.rebel459.unified.api.util.RecipeProvider;
import net.rebel459.unified.impl.datagen.BlockAssetRequest;
import net.rebel459.unified.impl.datagen.DataProvider;
import net.rebel459.unified.impl.datagen.ItemAssetRequest;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class FabricDatagenProvider implements DataGeneratorEntrypoint {
    public static final Map<BlockAsset<?>, BlockAssetAdapter<?>> BLOCK_ASSET_ADAPTERS = new LinkedHashMap<>();
    public static final Map<ItemAsset<?>, ItemAssetAdapter<?>> ITEM_ASSET_ADAPTERS = new LinkedHashMap<>();

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

    public static final class DefinitionProvider implements net.minecraft.data.DataProvider {

        private final FabricPackOutput output;
        private final String modId;
        public DefinitionProvider(FabricPackOutput output, String modId) {
            this.output = output; this.modId = modId;
        }

        @Override public CompletableFuture<?> run(CachedOutput cache) {
            List<CompletableFuture<?>> writes = new ArrayList<>();
            DynamicOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
            DataProvider.blocks(modId).forEach(generated -> writes.add(saveDefinition(cache, BlockRegistry.CODEC.encodeStart(registryOps, generated.definition().get()).getOrThrow(), definitionPath(output, generated.id(), "blocks"), modId)));
            DataProvider.items(modId).forEach(generated -> writes.add(saveDefinition(cache, ItemRegistry.CODEC.encodeStart(registryOps, generated.definition().get()).getOrThrow(), definitionPath(output, generated.id(), "items"), modId)));
            DataProvider.blockSetTypes(modId).forEach(generated -> writes.add(saveDefinition(cache, BlockSetTypeRegistry.CODEC.encodeStart(JsonOps.INSTANCE, generated.definition().get()).getOrThrow(), definitionPath(output, generated.id(), "block_set_types"), modId)));
            return CompletableFuture.allOf(writes.toArray(CompletableFuture[]::new));
        }

        @Override public String getName() {
            return "Unified registry definitions for " + modId;
        }
    }

    public static final class LanguageProvider implements net.minecraft.data.DataProvider {

        private final FabricPackOutput output;
        private final String modId;
        public LanguageProvider(FabricPackOutput output, String modId) {
            this.output = output; this.modId = modId;
        }

        @Override public CompletableFuture<?> run(CachedOutput cache) {
            DataProvider.GenerationSettings settings = DataProvider.settings(modId);
            JsonObject translations = new JsonObject();
            settings.injectedTranslations().ifPresent(path -> {
                String normalized = path.startsWith("/") ? path.substring(1) : path;
                try (InputStream stream = FabricDatagenProvider.class.getClassLoader().getResourceAsStream(normalized)) {
                    if (stream == null) throw new IllegalArgumentException("Injected translation resource does not exist: " + path);
                    try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                        JsonObject injected = JsonParser.parseReader(reader).getAsJsonObject();
                        injected.entrySet().forEach(entry -> translations.add(entry.getKey(), entry.getValue()));
                    }
                } catch (IOException exception) {
                    throw new IllegalStateException("Failed to read injected translations " + path, exception);
                }
            });
            DataProvider.blocks(modId).forEach(generated -> addTranslation(translations, settings, generated.id(), "block", generated.assets().get().name().orElse(null)));
            DataProvider.items(modId).forEach(generated ->
                    addItemTranslation(translations, settings, generated.id(), generated.assets().get().name().orElse(null)));
            Path path = output.getOutputFolder().resolve("assets").resolve(modId).resolve("lang").resolve(settings.language() + ".json");
            return net.minecraft.data.DataProvider.saveStable(cache, translations, path);
        }

        @Override public String getName() { return "Unified translations for " + modId; }

        private void addTranslation(JsonObject translations, DataProvider.GenerationSettings settings, Identifier id, String kind, String explicitName) {
            if (explicitName == null && !settings.autoName()) return;
            translations.addProperty(kind + "." + id.getNamespace() + "." + id.getPath(), explicitName != null ? explicitName : autoName(id.getPath()));
        }

        private void addItemTranslation(JsonObject translations, DataProvider.GenerationSettings settings,
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
        private final Set<Block> familyBases = Collections.newSetFromMap(new IdentityHashMap<>());
        private final Map<Block, BlockModelGenerators.BlockFamilyProvider> families = new IdentityHashMap<>();
        public ModelsProvider(FabricPackOutput output, String modId) {
            super(output); this.modId = modId;
        }

        @Override public void generateBlockStateModels(BlockModelGenerators generator) {
            DataProvider.blocks(modId).forEach(generated -> {
                generated.assets().get().models().forEach(request -> {
                    if (isFamilyAsset(request.type())) familyBases.add((Block) request.value());
                });
            });
            DataProvider.blocks(modId).forEach(generated -> {
                Identifier id = generated.id();
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
            DataProvider.items(modId).forEach(generated -> {
                Identifier id = generated.id();
                Item item = BuiltInRegistries.ITEM.getValue(id);
                ItemModelContext context = new ItemModelContext(id, item, generator);
                generated.assets().get().models().forEach(request -> generateItemAsset(request, context));
            });
        }
    }

    public static final class BlockLootProvider extends FabricBlockLootSubProvider {

        private final String modId;
        public BlockLootProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries, String modId) {
            super(output, registries); this.modId = modId;
        }

        @Override public void generate() {
            DataProvider.loot(modId).forEach(generator ->
                    generator.accept((block, table) -> add(block, table)));
        }
    }

    public static final class RecipesProvider extends FabricRecipeProvider {

        private final String modId;
        public RecipesProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries, String modId) {
            super(output, registries); this.modId = modId;
        }

        @Override protected net.minecraft.data.recipes.RecipeProvider createRecipeProvider(HolderLookup.Provider registries, BootstrapContext<Recipe<?>> recipes, BootstrapContext<Advancement> advancements) {
            return new net.minecraft.data.recipes.RecipeProvider(recipes, advancements) {
                @Override public void buildRecipes() {
                    RecipeProvider recipeProvider = new RecipeProvider(this, recipes, advancements) {
                        @Override
                        public void buildRecipes() {}
                    };
                    DataProvider.recipes(modId).forEach(generator -> generator.accept(recipeProvider));
                }
            };
        }

        @Override public String getName() {
            return "Unified recipes for " + modId;
        }
    }

    public static final class BlockTagsProvider extends FabricTagsProvider.BlockTagsProvider {

        private final String modId;
        public BlockTagsProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries, String modId) {
            super(output, registries);
            this.modId = modId;
        }

        @Override protected void addTags(HolderLookup.Provider registries) {
            DataProvider.blockTags(modId).forEach(generator -> generator.accept(new DataProvider.TagGenerator<>() {
                @Override public void add(TagKey<Block> tagKey, ResourceKey<Block> value) {
                    tag(tagKey).add(value);
                }

                @Override public void addOptional(TagKey<Block> tagKey, ResourceKey<Block> value) {
                    tag(tagKey).addOptional(value);
                }
            }));
        }
    }

    public static final class ItemTagsProvider extends FabricTagsProvider.ItemTagsProvider {

        private final String modId;
        public ItemTagsProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries, String modId, BlockTagsProvider blocks) {
            super(output, registries, blocks); this.modId = modId;
        }

        @Override protected void addTags(HolderLookup.Provider registries) {
            DataProvider.itemTags(modId).forEach(generator -> generator.accept(new DataProvider.TagGenerator<>() {
                @Override public void add(TagKey<Item> tagKey, ResourceKey<Item> value) {
                    tag(tagKey).add(value);
                }

                @Override public void addOptional(TagKey<Item> tagKey, ResourceKey<Item> value) {
                    tag(tagKey).addOptional(value);
                }
            }));
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
    private static CompletableFuture<?> saveDefinition(CachedOutput cache, JsonElement encoded, Path path, String modId) {
        JsonObject definition = encoded.getAsJsonObject();
        var metadata = DataProvider.settings(modId).metadata();
        if (metadata.priority() != 0) definition.addProperty("priority", metadata.priority());
        metadata.requirement().ifPresent(requirement -> definition.add(
                "load_requirements",
                ExtensibleCodecs.REQUIREMENT_TYPES.codec()
                        .encodeStart(JsonOps.INSTANCE, requirement)
                        .getOrThrow(error -> new IllegalStateException("Failed to encode load requirement: " + error))
        ));
        return net.minecraft.data.DataProvider.saveStable(cache, definition, path);
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
