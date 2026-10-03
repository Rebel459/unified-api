package net.rebel459.unified.impl.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.rebel459.unified.Unified;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.api.codec.UnifiedCodecs;
import net.rebel459.unified.api.util.BlockLootSubProvider;
import net.rebel459.unified.api.util.EntityLootSubProvider;
import net.rebel459.unified.api.util.RecipeProvider;
import net.rebel459.unified.impl.asset.BlockAssetRequest;
import net.rebel459.unified.impl.asset.ItemAssetRequest;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Built-in data generation channels and their loader-independent requests. */
public final class DataProviders {
    public static final DataProvider<LanguageRequest> LANGUAGES = DataProvider.create(Unified.id("languages"));
    public static final DataProvider<ModelRequest> MODELS = DataProvider.keyed(Unified.id("models"), request -> (request instanceof BlockModels ? "block/" : "item/") + request.id());
    public static final DataProvider<TagRequest<?>> TAGS = DataProvider.create(Unified.id("tags"));
    public static final DataProvider<BlockLootRequest> BLOCK_LOOT = DataProvider.create(Unified.id("block_loot"));
    public static final DataProvider<EntityLootRequest> ENTITY_LOOT = DataProvider.create(Unified.id("entity_loot"));
    public static final DataProvider<RecipeRequest> RECIPES = DataProvider.create(Unified.id("recipes"));

    private DataProviders() {}

    public record LanguageRequest(GenerationSettings settings, Optional<Translation> translation) {}
    public record Translation(Identifier id, TranslationType type, Supplier<Optional<String>> name) {}

    public enum TranslationType {
        BLOCK,
        ITEM,
        ENTITY
    }

    public sealed interface ModelRequest permits BlockModels, ItemModels {
        Identifier id();
    }

    public record BlockModels(Identifier id, Supplier<BlockAssets> assets) implements ModelRequest {}
    public record ItemModels(Identifier id, Supplier<ItemAssets> assets) implements ModelRequest {}

    public record BlockAssets(Optional<String> name, List<BlockAssetRequest<?>> models) {}
    public record EntityAssets(Optional<String> name) {}
    public record ItemAssets(Optional<String> name, List<ItemAssetRequest<?>> models) {}

    public record TagRequest<T>(ResourceKey<? extends Registry<T>> registry, Consumer<TagGenerator<T>> generator) {}
    public record RecipeRequest(Optional<ExtensibleCodec.Entry<Boolean>> requirement, Consumer<RecipeProvider> generator) {}

    @FunctionalInterface
    public interface BlockLootRequest {
        void generate(BlockLootSubProvider provider, BiConsumer<Block, LootTable.Builder> output);
    }

    @FunctionalInterface
    public interface EntityLootRequest {
        void generate(EntityLootSubProvider provider, BiConsumer<EntityType<?>, LootTable.Builder> output);
    }

    public interface TagGenerator<T> {
        void add(TagKey<T> tag, ResourceKey<T> value);
        void addOptional(TagKey<T> tag, ResourceKey<T> value);
        void addTag(TagKey<T> tag, TagKey<T> value);
        void addOptionalTag(TagKey<T> tag, TagKey<T> value);
    }

    public record GenerationSettings(PriorityAndRequirement metadata, boolean autoName, String language, Optional<String> injectedTranslations) {
        public static final GenerationSettings DEFAULT = new GenerationSettings(PriorityAndRequirement.DEFAULT, false, "en_us", Optional.empty());
    }

    public record PriorityAndRequirement(int priority, Optional<ExtensibleCodec.Entry<Boolean>> requirement) {
        public static final PriorityAndRequirement DEFAULT = new PriorityAndRequirement(0, Optional.empty());

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
