package net.rebel459.unified.impl.datagen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.api.codec.UnifiedCodecs;
import net.rebel459.unified.api.util.RecipeProvider;
import net.rebel459.unified.impl.registry.BlockRegistry;
import net.rebel459.unified.impl.registry.BlockSetTypeRegistry;
import net.rebel459.unified.impl.registry.ItemRegistry;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class DataProvider {
    private static final Map<String, List<GeneratedBlock>> BLOCKS = new LinkedHashMap<>();
    private static final Map<String, List<GeneratedItem>> ITEMS = new LinkedHashMap<>();
    private static final Map<String, List<GeneratedBlockSetType>> BLOCK_SET_TYPES = new LinkedHashMap<>();
    private static final Map<String, GenerationSettings> SETTINGS = new LinkedHashMap<>();
    private static final Map<String, List<Consumer<TagGenerator<Block>>>> BLOCK_TAGS = new LinkedHashMap<>();
    private static final Map<String, List<Consumer<TagGenerator<Item>>>> ITEM_TAGS = new LinkedHashMap<>();
    private static final Map<String, List<Consumer<LootGenerator>>> LOOT = new LinkedHashMap<>();
    private static final Map<String, List<Consumer<RecipeProvider>>> RECIPES = new LinkedHashMap<>();

    private DataProvider() {}

    public static synchronized void add(String modId, GeneratedBlock block) {
        addGenerated(BLOCKS, modId, block, GeneratedBlock::id, "block");
    }

    public static synchronized void add(String modId, GeneratedItem item) {
        addGenerated(ITEMS, modId, item, GeneratedItem::id, "item");
    }

    public static synchronized void addBlockSetType(String modId, GeneratedBlockSetType blockSetType) {
        addGenerated(BLOCK_SET_TYPES, modId, blockSetType, GeneratedBlockSetType::id, "block set type");
    }

    public static synchronized List<GeneratedBlock> blocks(String modId) {
        return List.copyOf(BLOCKS.getOrDefault(modId, List.of()));
    }

    public static synchronized List<GeneratedItem> items(String modId) {
        return List.copyOf(ITEMS.getOrDefault(modId, List.of()));
    }

    public static synchronized List<GeneratedBlockSetType> blockSetTypes(String modId) {
        return List.copyOf(BLOCK_SET_TYPES.getOrDefault(modId, List.of()));
    }

    public static synchronized void addBlockTags(String modId, Consumer<TagGenerator<Block>> generator) {
        add(BLOCK_TAGS, modId, generator);
    }

    public static synchronized void addItemTags(String modId, Consumer<TagGenerator<Item>> generator) {
        add(ITEM_TAGS, modId, generator);
    }

    public static synchronized void addLoot(String modId, Consumer<LootGenerator> generator) {
        add(LOOT, modId, generator);
    }

    public static synchronized void addRecipes(String modId, Consumer<RecipeProvider> generator) {
        add(RECIPES, modId, generator);
    }

    public static synchronized List<Consumer<TagGenerator<Block>>> blockTags(String modId) {
        return List.copyOf(BLOCK_TAGS.getOrDefault(modId, List.of()));
    }

    public static synchronized List<Consumer<TagGenerator<Item>>> itemTags(String modId) {
        return List.copyOf(ITEM_TAGS.getOrDefault(modId, List.of()));
    }

    public static synchronized List<Consumer<LootGenerator>> loot(String modId) {
        return List.copyOf(LOOT.getOrDefault(modId, List.of()));
    }

    public static synchronized List<Consumer<RecipeProvider>> recipes(String modId) {
        return List.copyOf(RECIPES.getOrDefault(modId, List.of()));
    }

    private static <T> void add(Map<String, List<T>> registry, String modId, T generator) {
        registry.computeIfAbsent(modId, _ -> new ArrayList<>()).add(generator);
    }

    private static <T> void addGenerated(Map<String, List<T>> registry, String modId, T value, Function<T, Identifier> id, String type) {
        List<T> generated = registry.computeIfAbsent(modId, _ -> new ArrayList<>());
        Identifier identifier = id.apply(value);
        if (generated.stream().map(id).anyMatch(identifier::equals)) {
            throw new IllegalArgumentException("Duplicate generated " + type + " " + identifier + " for " + modId);
        }
        generated.add(value);
    }

    public static synchronized void settings(String modId, GenerationSettings settings) {
        SETTINGS.put(modId, settings);
    }

    public static GenerationSettings settings(String modId) {
        return SETTINGS.getOrDefault(modId, GenerationSettings.DEFAULT);
    }

    public record GeneratedBlock(Identifier id, Supplier<BlockRegistry.Definition> definition, Supplier<BlockAssets> assets) {}
    public record GeneratedItem(Identifier id, Supplier<ItemRegistry.Definition> definition, Supplier<ItemAssets> assets) {}
    public record GeneratedBlockSetType(Identifier id, Supplier<BlockSetTypeRegistry.Definition> definition) {}

    public record BlockAssets(Optional<String> name, List<BlockAssetRequest<?>> models) {}
    public record ItemAssets(Optional<String> name, List<ItemAssetRequest<?>> models) {}

    public interface TagGenerator<T> {
        void add(TagKey<T> tag, ResourceKey<T> value);
        void addOptional(TagKey<T> tag, ResourceKey<T> value);
    }

    @FunctionalInterface
    public interface LootGenerator {
        void add(Block block, LootTable.Builder table);
    }

    public record GenerationSettings(PriorityAndRequirement metadata, boolean autoName, String language, Optional<String> injectedTranslations) {
        public static final GenerationSettings DEFAULT = new GenerationSettings(PriorityAndRequirement.DEFAULT, false, "en_us", Optional.empty());
    }

    public record PriorityAndRequirement(int priority, Optional<ExtensibleCodec.Entry<Boolean>> requirement) {
        public static final PriorityAndRequirement DEFAULT =
                new PriorityAndRequirement(0, Optional.empty());

        public static final MapCodec<Integer> PRIORITY_CODEC = Codec.INT.optionalFieldOf("priority", 0);

        public static final Codec<PriorityAndRequirement> CODEC =
                RecordCodecBuilder.create(instance -> instance.group(
                        PRIORITY_CODEC.forGetter(PriorityAndRequirement::priority),
                        UnifiedCodecs.LOAD_REQUIREMENTS.forGetter(PriorityAndRequirement::requirement)
                ).apply(instance, PriorityAndRequirement::new));

        public static final MapCodec<PriorityAndRequirement> MAP_CODEC =
                RecordCodecBuilder.mapCodec(instance -> instance.group(
                        PRIORITY_CODEC.forGetter(PriorityAndRequirement::priority),
                        UnifiedCodecs.LOAD_REQUIREMENTS.forGetter(PriorityAndRequirement::requirement)
                ).apply(instance, PriorityAndRequirement::new));
    }
}
