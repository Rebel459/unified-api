package net.rebel459.unified.util.datagen.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.rebel459.unified.util.data.registry.BlockRegistry;
import net.rebel459.unified.util.data.registry.BlockSetTypeRegistry;
import net.rebel459.unified.util.data.registry.ItemRegistry;
import net.rebel459.unified.util.RecipeProvider;

import java.util.*;
import java.util.function.Function;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public final class DataRegistry {
    private static final Map<Identifier, GeneratedBlock> BLOCKS = new LinkedHashMap<>();
    private static final Map<Identifier, GeneratedItem> ITEMS = new LinkedHashMap<>();
    private static final Map<Identifier, Supplier<BlockSetTypeRegistry.Definition>> BLOCK_SET_TYPES = new LinkedHashMap<>();
    private static final Map<String, GenerationSettings> SETTINGS = new LinkedHashMap<>();

    private DataRegistry() {}

    public static synchronized void add(Identifier id, GeneratedBlock block) {
        if (BLOCKS.putIfAbsent(id, block) != null) throw new IllegalArgumentException("Duplicate generated block " + id);
    }

    public static synchronized void add(Identifier id, GeneratedItem item) {
        if (ITEMS.putIfAbsent(id, item) != null) throw new IllegalArgumentException("Duplicate generated item " + id);
    }

    public static synchronized void addBlockSetType(Identifier id, Supplier<BlockSetTypeRegistry.Definition> definition) {
        if (BLOCK_SET_TYPES.putIfAbsent(id, definition) != null) throw new IllegalArgumentException("Duplicate generated block set type " + id);
    }

    public static Map<Identifier, GeneratedBlock> blocks() {
        return Collections.unmodifiableMap(BLOCKS);
    }

    public static Map<Identifier, GeneratedItem> items() {
        return Collections.unmodifiableMap(ITEMS);
    }

    public static Map<Identifier, Supplier<BlockSetTypeRegistry.Definition>> blockSetTypes() {
        return Collections.unmodifiableMap(BLOCK_SET_TYPES);
    }

    public static synchronized void settings(String modId, GenerationSettings settings) {
        SETTINGS.put(modId, settings);
    }

    public static GenerationSettings settings(String modId) {
        return SETTINGS.getOrDefault(modId, GenerationSettings.DEFAULT);
    }

    public record GeneratedBlock(Supplier<BlockRegistry.Definition> definition, Supplier<BlockAssets> assets, Supplier<BlockData> data) {}
    public record GeneratedItem(Supplier<ItemRegistry.Definition> definition, Supplier<ItemAssets> assets, Supplier<ItemData> data) {}

    public record BlockAssets(Optional<String> name, List<BlockAssetRequest<?>> models) {}
    public record ItemAssets(Optional<String> name, List<ItemAssetRequest<?>> models) {}
    public record BlockData(List<TagKey<Block>> tags, List<TagKey<Block>> optionalTags, List<TagKey<Item>> itemTags, List<TagKey<Item>> optionalItemTags, Optional<Function<Block, LootTable.Builder>> loot, Optional<BiConsumer<Item, RecipeProvider>> recipe) {}
    public record ItemData(List<TagKey<Item>> tags, List<TagKey<Item>> optionalTags, Optional<BiConsumer<Item, RecipeProvider>> recipe) {}
    public record GenerationSettings(PriorityAndDependencies metadata, boolean autoName, String language, Optional<String> injectedTranslations) {
        public static final GenerationSettings DEFAULT = new GenerationSettings(PriorityAndDependencies.DEFAULT, false, "en_us", Optional.empty());
    }

    public record PriorityAndDependencies(int priority, List<String> dependencies) {
        public static final DataRegistry.PriorityAndDependencies DEFAULT = new DataRegistry.PriorityAndDependencies(0, List.of());

        public static MapCodec<Integer> PRIORITY_CODEC = Codec.INT.optionalFieldOf("priority", 0);
        public static MapCodec<List<String>> DEPENDENCIES_CODEC = Codec.STRING.listOf().optionalFieldOf("dependencies", List.of());

        public static final Codec<DataRegistry.PriorityAndDependencies> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                PRIORITY_CODEC.forGetter(DataRegistry.PriorityAndDependencies::priority),
                DEPENDENCIES_CODEC.forGetter(DataRegistry.PriorityAndDependencies::dependencies)
        ).apply(instance, DataRegistry.PriorityAndDependencies::new));

        public static final MapCodec<DataRegistry.PriorityAndDependencies> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                PRIORITY_CODEC.forGetter(DataRegistry.PriorityAndDependencies::priority),
                DEPENDENCIES_CODEC.forGetter(DataRegistry.PriorityAndDependencies::dependencies)
        ).apply(instance, DataRegistry.PriorityAndDependencies::new));

        public PriorityAndDependencies {
            dependencies = List.copyOf(dependencies);
        }
    }
}
