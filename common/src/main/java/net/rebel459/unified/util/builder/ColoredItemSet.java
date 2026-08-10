package net.rebel459.unified.util.builder;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.rebel459.unified.platform.UnifiedPlatform;
import net.rebel459.unified.platform.UnifiedRegistries;
import net.rebel459.unified.util.LoaderType;
import net.rebel459.unified.util.builder.impl.ColoredItemSetImpl;
import net.rebel459.unified.util.registry.SuppliedItem;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class ColoredItemSet {

    public static final List<ColoredItemSet> COLORED_ITEM_SETS = new CopyOnWriteArrayList<>();

    private final List<SuppliedItem> registeredItems = new ArrayList<>();

    private final Identifier id;

    private final UnifiedRegistries.Items itemRegistry;

    private SuppliedItem white;
    private SuppliedItem lightGray;
    private SuppliedItem gray;
    private SuppliedItem black;
    private SuppliedItem brown;
    private SuppliedItem red;
    private SuppliedItem orange;
    private SuppliedItem yellow;
    private SuppliedItem lime;
    private SuppliedItem green;
    private SuppliedItem cyan;
    private SuppliedItem lightBlue;
    private SuppliedItem blue;
    private SuppliedItem purple;
    private SuppliedItem magenta;
    private SuppliedItem pink;

    private final Map<SuppliedItem, DyeColor> dyesByItem = new HashMap<>();
    private final Map<DyeColor, SuppliedItem> itemsByDye = new EnumMap<>(DyeColor.class);

    private final Settings settings;

    private void registerItems() {
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

    public ColoredItemSet(Identifier id, Settings settings, UnifiedRegistries.Items itemRegistry){
        this.settings = settings;
        this.id = id;
        this.itemRegistry = itemRegistry;
        registerItems();
        COLORED_ITEM_SETS.add(this);
        ColoredItemSetProperties.COMPONENTS.put(id, settings.components);
        ColoredItemSetProperties.DYED_COMPONENTS.put(id, settings.dyedComponents);
        ColoredItemSetProperties.PROVIDED_COMPONENTS.put(id, settings.providedComponents);
        ColoredItemSetProperties.KEYED_COMPONENTS.put(id, settings.keyedComponents);
        ColoredItemSetProperties.CREATIVE_ENTRIES.put(id, getSettings().precedingCreativeEntries);
        if (UnifiedPlatform.getLoader() == LoaderType.FABRIC) ColoredItemSetImpl.init(List.of(this));
    }

    public Settings getSettings() {
        return settings;
    }

    public Identifier getId() {
        return id;
    }

    public SuppliedItem getWhite() {
        return white;
    }

    public SuppliedItem getLightGray() {
        return lightGray;
    }

    public SuppliedItem getGray() {
        return gray;
    }

    public SuppliedItem getBlack() {
        return black;
    }

    public SuppliedItem getBrown() {
        return brown;
    }

    public SuppliedItem getRed() {
        return red;
    }

    public SuppliedItem getOrange() {
        return orange;
    }

    public SuppliedItem getYellow() {
        return yellow;
    }

    public SuppliedItem getLime() {
        return lime;
    }

    public SuppliedItem getGreen() {
        return green;
    }

    public SuppliedItem getCyan() {
        return cyan;
    }

    public SuppliedItem getLightBlue() {
        return lightBlue;
    }

    public SuppliedItem getBlue() {
        return blue;
    }

    public SuppliedItem getPurple() {
        return purple;
    }

    public SuppliedItem getMagenta() {
        return magenta;
    }

    public SuppliedItem getPink() {
        return pink;
    }

    public List<SuppliedItem> getRegisteredItems() {
        return registeredItems;
    }

    public DyeColor getDyeFromItem(SuppliedItem item) {
        return dyesByItem.get(item);
    }

    public SuppliedItem getItemFromDye(DyeColor color) {
        return itemsByDye.get(color);
    }

    private SuppliedItem create(String color) {
        String name = color + "_" + id.getPath();
        DyeColor dye = DyeColor.byName(color, null);
        SuppliedItem item;
        if (getSettings().createdForColoredBlockSet()) item = itemRegistry.registerBlockItem(getSettings().coloredBlockSet.getBlockFromDye(dye), getSettings().blockFunction, getSettings().properties);
        else item = itemRegistry.register(name, getSettings().function, getSettings().properties);
        dyesByItem.put(item, dye);
        itemsByDye.put(dye, item);
        registeredItems.add(item);
        return item;
    }

    public static class Settings implements Cloneable {

        private Function<Item.Properties, Item> function = Item::new;
        private BiFunction<Block, Item.Properties, Item> blockFunction = BlockItem::new;
        private Supplier<Item.Properties> properties = Item.Properties::new;

        private @Nullable ColoredBlockSet coloredBlockSet = null;

        private @Nullable PrecedingCreativeEntries precedingCreativeEntries = null;

        private List<Pair<Supplier<? extends DataComponentType<?>>, ?>> components = new ArrayList<>();
        private List<Pair<Supplier<? extends DataComponentType<?>>, Function<DyeColor, ?>>> dyedComponents = new ArrayList<>();
        private List<Pair<Supplier<? extends DataComponentType<?>>, DataComponentInitializers.SingleComponentInitializer<?>>> providedComponents = new ArrayList<>();
        private List<Pair<Supplier<? extends DataComponentType<?>>, ResourceKey<?>>> keyedComponents = new ArrayList<>();

        public boolean createdForColoredBlockSet() {
            return coloredBlockSet != null;
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

        private final UnifiedRegistries.Items itemRegistry;

        public RegistryBuilder createForBlocks(ColoredBlockSet coloredBlockSet) {
            settings.coloredBlockSet = coloredBlockSet;
            return self();
        }

        public ColoredItemSet build() {
            return new ColoredItemSet(id, settings, itemRegistry);
        }

        public RegistryBuilder(Identifier id, ColoredItemPreset preset, UnifiedRegistries.Items itemRegistry) {
            super(preset.settings.copy());

            this.id = id;
            this.itemRegistry = itemRegistry;
        }
    }

    public static class PresetBuilder extends ColoredItemSet.Builder<PresetBuilder> {

        public PresetBuilder() {
            super();
        }

        public PresetBuilder(Settings settings) {
            super(settings);
        }

        public ColoredItemPreset build() {
            return new ColoredItemPreset(settings.copy());
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

        public T creativeInventoryPlacement(ResourceKey<CreativeModeTab> firstTab, Supplier<? extends ItemLike> precedingFirstTabItem) {
            settings.precedingCreativeEntries = new PrecedingCreativeEntries(Pair.of(firstTab, precedingFirstTabItem), null);
            return self();
        }
        public T creativeInventoryPlacement(ResourceKey<CreativeModeTab> firstTab, Supplier<? extends ItemLike> precedingFirstTabItem, ResourceKey<CreativeModeTab> secondTab, Supplier<? extends ItemLike> precedingSecondTabItem) {
            settings.precedingCreativeEntries = new PrecedingCreativeEntries(Pair.of(firstTab, precedingFirstTabItem), Pair.of(secondTab, precedingSecondTabItem));
            return self();
        }

        public T function(Function<Item.Properties, Item> function) {
            settings.function = function;
            return self();
        }
        public T function(BiFunction<Block, Item.Properties, Item> function) {
            settings.blockFunction = function;
            return self();
        }

        public T properties(Supplier<Item.Properties> properties) {
            settings.properties = properties;
            return self();
        }

        public <Y> T setComponent(Supplier<DataComponentType<Y>> type, Y value) {
            var components = settings.components;
            components.add(Pair.of(type, value));
            settings.components = components;
            return self();
        }
        public <Y> T setComponentWithDye(Supplier<DataComponentType<Y>> type, Function<DyeColor, Y> valueFactory) {
            var dyeComponents = settings.dyedComponents;
            dyeComponents.add(Pair.of(type, valueFactory));
            settings.dyedComponents = dyeComponents;
            return self();
        }
        public <Y> T setComponentWithProvider(Supplier<DataComponentType<Y>> type, DataComponentInitializers.SingleComponentInitializer<Y> initializer) {
            var providedComponents = settings.providedComponents;
            providedComponents.add(Pair.of(type, initializer));
            settings.providedComponents = providedComponents;
            return self();
        }
        public <Y> T setComponentWithKey(Supplier<DataComponentType<Holder<Y>>> type, ResourceKey<Y> valueKey) {
            var keyedComponents = settings.keyedComponents;
            keyedComponents.add(Pair.of(type, valueKey));
            settings.keyedComponents = keyedComponents;
            return self();
        }
    }

    public record PrecedingCreativeEntries(Pair<ResourceKey<CreativeModeTab>, Supplier<? extends ItemLike>> firstTab, @Nullable Pair<ResourceKey<CreativeModeTab>, Supplier<? extends ItemLike>> secondTab) {}
}
