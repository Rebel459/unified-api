package net.rebel459.unified.api.builder;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.api.codec.ExtensibleCodecs;
import net.rebel459.unified.api.core.*;
import net.rebel459.unified.api.platform.ModLoader;
import net.rebel459.unified.api.registry.VanillaBlockTypes;
import net.rebel459.unified.api.registry.VanillaItemTypes;
import net.rebel459.unified.api.util.QuadConsumer;
import net.rebel459.unified.impl.builder.ColoredBlockSetProperties;
import net.rebel459.unified.api.util.RecipeProvider;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.*;

public class ColoredBlockSet {

    public static final List<ColoredBlockSet> COLORED_BLOCK_SETS = new CopyOnWriteArrayList<>();

    private final List<SuppliedBlock> registeredBlocks = new ArrayList<>();

    private final Identifier id;

    private final UnifiedDataRegistries.Blocks blocks;
    private final UnifiedDataRegistries.Items items;

    private SuppliedBlock white;
    private SuppliedBlock lightGray;
    private SuppliedBlock gray;
    private SuppliedBlock black;
    private SuppliedBlock brown;
    private SuppliedBlock red;
    private SuppliedBlock orange;
    private SuppliedBlock yellow;
    private SuppliedBlock lime;
    private SuppliedBlock green;
    private SuppliedBlock cyan;
    private SuppliedBlock lightBlue;
    private SuppliedBlock blue;
    private SuppliedBlock purple;
    private SuppliedBlock magenta;
    private SuppliedBlock pink;

    private final Map<SuppliedBlock, DyeColor> dyesByBlock = new HashMap<>();
    private final Map<DyeColor, SuppliedBlock> blocksByDye = new EnumMap<>(DyeColor.class);

    private final Settings settings;

    private void registerBlocks() {
        white = create("white");
        lightGray = create("light_gray");
        gray = create("gray");
        black = create("black");
        brown = create("brown");
        red = create("red");
        orange = create("orange");
        yellow = create("yellow");
        lime = create("lime");
        green = create("green");
        cyan = create("cyan");
        lightBlue = create("light_blue");
        blue = create("blue");
        purple = create("purple");
        magenta = create("magenta");
        pink = create("pink");
    }

    public ColoredBlockSet(Identifier id, Settings settings, UnifiedDataRegistries.Blocks blocks, UnifiedDataRegistries.Items items) {
        this.settings = settings;
        this.id = id;
        this.blocks = blocks;
        this.items = items;
        registerBlocks();
        COLORED_BLOCK_SETS.add(this);
        ColoredBlockSetProperties.CREATIVE_ENTRIES.put(id, getSettings().precedingCreativeEntries);
        if (UnifiedInstance.getModLoader() == ModLoader.FABRIC) ColoredBlockSetProperties.init(List.of(this));
    }

    public Settings getSettings() {
        return settings;
    }

    public Identifier getId() {
        return id;
    }

    public SuppliedBlock getWhite() {
        return white;
    }

    public SuppliedBlock getLightGray() {
        return lightGray;
    }

    public SuppliedBlock getGray() {
        return gray;
    }

    public SuppliedBlock getBlack() {
        return black;
    }

    public SuppliedBlock getBrown() {
        return brown;
    }

    public SuppliedBlock getRed() {
        return red;
    }

    public SuppliedBlock getOrange() {
        return orange;
    }

    public SuppliedBlock getYellow() {
        return yellow;
    }

    public SuppliedBlock getLime() {
        return lime;
    }

    public SuppliedBlock getGreen() {
        return green;
    }

    public SuppliedBlock getCyan() {
        return cyan;
    }

    public SuppliedBlock getLightBlue() {
        return lightBlue;
    }

    public SuppliedBlock getBlue() {
        return blue;
    }

    public SuppliedBlock getPurple() {
        return purple;
    }

    public SuppliedBlock getMagenta() {
        return magenta;
    }

    public SuppliedBlock getPink() {
        return pink;
    }

    public List<SuppliedBlock> getRegisteredBlocks() {
        return registeredBlocks;
    }

    public DyeColor getDyeFromBlock(SuppliedBlock block) {
        return dyesByBlock.get(block);
    }

    public SuppliedBlock getBlockFromDye(DyeColor color) {
        return blocksByDye.get(color);
    }

    private SuppliedBlock create(String color) {
        String name = color + "_" + id.getPath();
        DyeColor dye = DyeColor.byName(color, null);
        SuppliedBlock block;
        ExtensibleCodec.Entry<Function<BlockBehaviour.Properties, ? extends Block>> blockType;
        if (getSettings().blockType.left().isPresent()) blockType = getSettings().blockType.left().get().apply(dye);
        else blockType = ExtensibleCodecs.BLOCK_TYPES.register(Identifier.fromNamespaceAndPath(id.getNamespace(), name), () -> getSettings().blockType.right().get().apply(dye)).create();
        block = blocks.registerWithoutItem(name, blockType, builder -> getSettings().blockBuilder.accept(dye, builder));
        ExtensibleCodec.Entry<BiFunction<Block, Item.Properties, Item>> itemType;
        if (getSettings().blockType.left().isPresent()) itemType = getSettings().itemType.left().get().apply(dye);
        else itemType = ExtensibleCodecs.BLOCK_ITEM_TYPES.register(Identifier.fromNamespaceAndPath(id.getNamespace(), name), () -> getSettings().itemType.right().get().apply(dye)).create();
        items.registerBlockItem(block, itemType, builder -> {
            getSettings().itemBuilder.accept(dye, builder);
            if (getSettings().dyeRecipe != null) builder.data(data -> data.recipe((item, provider) -> {
                List<SuppliedBlock> otherBlocks = registeredBlocks.stream()
                        .filter(otherBlock -> otherBlock != block)
                        .toList();
                getSettings().dyeRecipe.accept(Items.DYE.pick(dye), otherBlocks, item, provider);
            }));
        });
        dyesByBlock.put(block, dye);
        blocksByDye.put(dye, block);
        registeredBlocks.add(block);
        return block;
    }

    public static class Settings implements Cloneable {

        private Either<Function<DyeColor, ExtensibleCodec.Entry<Function<BlockBehaviour.Properties, ? extends Block>>>, Function<DyeColor, Function<BlockBehaviour.Properties, ? extends Block>>> blockType = Either.left(_ -> VanillaBlockTypes.BLOCK.create());
        private Either<Function<DyeColor, ExtensibleCodec.Entry<BiFunction<Block, Item.Properties, Item>>>, Function<DyeColor, BiFunction<Block, Item.Properties, Item>>> itemType = Either.left(_ -> VanillaItemTypes.BLOCK_ITEM.create());
        private BiConsumer<DyeColor, UnifiedDataRegistries.Blocks.Builder> blockBuilder = (color, builder) -> builder.properties(properties -> properties.mapColor(color.getMapColor()));
        private BiConsumer<DyeColor, UnifiedDataRegistries.Items.Builder> itemBuilder = (_, _) -> {};
        private @Nullable QuadConsumer<Item, List<SuppliedBlock>, Item, RecipeProvider> dyeRecipe;

        private @Nullable PrecedingCreativeEntries precedingCreativeEntries = null;

        Settings() {}

        public Settings copy() {
            try {
                return (Settings) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new AssertionError(e);
            }
        }
    }

    public static class RegistryBuilder extends Builder<RegistryBuilder> {

        private final Identifier id;

        private final UnifiedDataRegistries.Blocks blocks;
        private final UnifiedDataRegistries.Items items;

        public ColoredBlockSet build() {
            return new ColoredBlockSet(id, settings, blocks, items);
        }

        public RegistryBuilder(Identifier id, ColoredBlockPreset preset, UnifiedDataRegistries.Blocks blocks, UnifiedDataRegistries.Items items) {
            super(preset.settings.copy());

            this.id = id;
            this.blocks = blocks;
            this.items = items;
        }
    }

    public static class PresetBuilder extends ColoredBlockSet.Builder<PresetBuilder> {

        public PresetBuilder() {
            super();
        }

        public PresetBuilder(Settings settings) {
            super(settings);
        }

        public ColoredBlockPreset build() {
            return new ColoredBlockPreset(settings.copy());
        }
    }

    public static class Builder<T extends Builder<T>> {

        protected final Settings settings;

        public Builder() {
            this.settings = new Settings();
        }

        protected Builder(Settings settings) {
            this.settings = settings;
        }

        @SuppressWarnings("unchecked")
        protected T self() {
            return (T) this;
        }

        public T creativeInventoryPlacement(Supplier<? extends ItemLike> precedingColoredItem) {
            settings.precedingCreativeEntries = new PrecedingCreativeEntries(precedingColoredItem, null);
            return self();
        }
        public T creativeInventoryPlacement(Supplier<? extends ItemLike> precedingColoredItem, ResourceKey<CreativeModeTab> secondTab) {
            settings.precedingCreativeEntries = new PrecedingCreativeEntries(precedingColoredItem, Pair.of(secondTab, precedingColoredItem));
            return self();
        }
        public T creativeInventoryPlacement(Supplier<? extends ItemLike> precedingColoredItem, ResourceKey<CreativeModeTab> secondTab, Supplier<? extends ItemLike> precedingSecondTabItem) {
            settings.precedingCreativeEntries = new PrecedingCreativeEntries(precedingColoredItem, Pair.of(secondTab, precedingSecondTabItem));
            return self();
        }

        public T blockTypeFunction(Function<DyeColor, Function<BlockBehaviour.Properties, ? extends Block>> type) {
            settings.blockType = Either.right(type);
            return self();
        }
        public T blockTypeCodec(Function<DyeColor, ExtensibleCodec.Entry<Function<BlockBehaviour.Properties, ? extends Block>>> type) {
            settings.blockType = Either.left(type);
            return self();
        }

        public T blockBuilder(BiConsumer<DyeColor, UnifiedDataRegistries.Blocks.Builder> builder) {
            settings.blockBuilder = settings.blockBuilder.andThen(builder);
            return self();
        }

        public T itemTypeFunction(Function<DyeColor, BiFunction<Block, Item.Properties, Item>> type) {
            settings.itemType = Either.right(type);
            return self();
        }
        public T itemTypeCodec(Function<DyeColor, ExtensibleCodec.Entry<BiFunction<Block, Item.Properties, Item>>> type) {
            settings.itemType = Either.left(type);
            return self();
        }

        public T itemBuilder(BiConsumer<DyeColor, UnifiedDataRegistries.Items.Builder> builder) {
            settings.itemBuilder = settings.itemBuilder.andThen(builder);
            return self();
        }

        public T dyeRecipe(QuadConsumer<Item, List<SuppliedBlock>, Item, RecipeProvider> recipe) {
            settings.dyeRecipe = recipe;
            return self();
        }
    }

    public record PrecedingCreativeEntries(Supplier<? extends ItemLike> colored, @Nullable Pair<ResourceKey<CreativeModeTab>, Supplier<? extends ItemLike>> secondTab) {}
}
