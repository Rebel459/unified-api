package net.rebel459.unified.util.builder;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.rebel459.unified.platform.UnifiedPlatform;
import net.rebel459.unified.platform.UnifiedRegistries;
import net.rebel459.unified.util.LoaderType;
import net.rebel459.unified.util.builder.impl.ColoredBlockSetImpl;
import net.rebel459.unified.util.registry.SuppliedBlock;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.function.Supplier;

public class ColoredBlockSet {

    public static final List<ColoredBlockSet> COLORED_BLOCK_SETS = new CopyOnWriteArrayList<>();

    private final List<SuppliedBlock> registeredBlocks = new ArrayList<>();

    private final Identifier id;

    private final UnifiedRegistries.Blocks blockRegistry;

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

    public ColoredBlockSet(Identifier id, Settings settings, UnifiedRegistries.Blocks blockRegistry){
        this.settings = settings;
        this.id = id;
        this.blockRegistry = blockRegistry;
        registerBlocks();
        COLORED_BLOCK_SETS.add(this);
        ColoredBlockSetImpl.CREATIVE_ENTRIES.put(id, getSettings().precedingCreativeEntries);
        if (UnifiedPlatform.getLoader() == LoaderType.FABRIC) ColoredBlockSetImpl.init(List.of(this));
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
        Supplier<BlockBehaviour.Properties> finalProperties = () -> getSettings().properties.get().mapColor(dye);
        SuppliedBlock block;
        if (getSettings().createWithoutItems) block = blockRegistry.registerWithoutItem(name, getSettings().function, finalProperties);
        else block = blockRegistry.register(name, getSettings().function, finalProperties);
        dyesByBlock.put(block, dye);
        blocksByDye.put(dye, block);
        registeredBlocks.add(block);
        return block;
    }

    public static class Settings implements Cloneable {

        private Function<BlockBehaviour.Properties, Block> function = Block::new;
        private Supplier<BlockBehaviour.Properties> properties = BlockBehaviour.Properties::new;
        private @Nullable Triple<Integer, Integer, Integer> flammability = null;

        private boolean createWithoutItems = false;

        private @Nullable PrecedingCreativeEntries precedingCreativeEntries = null;

        public boolean createdWithoutItems() {
            return createWithoutItems;
        }

        public @Nullable Triple<Integer, Integer, Integer> getFlammability() {
            return flammability;
        }

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

        private final UnifiedRegistries.Blocks blockRegistry;

        public RegistryBuilder createWithoutItems() {
            settings.createWithoutItems = true;
            return self();
        }

        public ColoredBlockSet build() {
            return new ColoredBlockSet(id, settings, blockRegistry);
        }

        public RegistryBuilder(Identifier id, ColoredBlockPreset preset, UnifiedRegistries.Blocks blockRegistry) {
            super(preset.settings.copy());

            this.id = id;
            this.blockRegistry = blockRegistry;
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

        public T function(Function<BlockBehaviour.Properties, Block> function) {
            settings.function = function;
            return self();
        }
        public T properties(Supplier<BlockBehaviour.Properties> properties) {
            settings.properties = properties;
            return self();
        }

        public T setFlammability(int igniteOdds, int burnOdds) {
            settings.flammability = Triple.of(igniteOdds, burnOdds, 0);
            return self();
        }
        public T setFlammability(int igniteOdds, int burnOdds, int furnaceTicks) {
            settings.flammability = Triple.of(igniteOdds, burnOdds, furnaceTicks);
            return self();
        }
    }

    public record PrecedingCreativeEntries(Supplier<? extends ItemLike> colored, @Nullable Pair<ResourceKey<CreativeModeTab>, Supplier<? extends ItemLike>> secondTab) {}
}