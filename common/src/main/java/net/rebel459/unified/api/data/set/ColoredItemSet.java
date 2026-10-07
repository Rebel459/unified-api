package net.rebel459.unified.api.data.set;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ItemLike;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.api.codec.ExtensibleCodecs;
import net.rebel459.unified.api.core.*;
import net.rebel459.unified.api.data.helper.CreativeEntryGenerator;
import net.rebel459.unified.api.data.registry.ItemGenerator;
import net.rebel459.unified.api.platform.ModLoader;
import net.rebel459.unified.api.registry.VanillaItemCodecs;
import net.rebel459.unified.api.util.QuadConsumer;
import net.rebel459.unified.api.util.RecipeProvider;
import net.rebel459.unified.impl.data.set.ColoredItemSetProperties;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ColoredItemSet {

    public static final List<ColoredItemSet> COLORED_ITEM_SETS = new CopyOnWriteArrayList<>();

    private final List<SuppliedItem> registeredItems = new ArrayList<>();

    private final Identifier id;

    private final ItemGenerator items;

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

    public ColoredItemSet(Identifier id, Settings settings, ItemGenerator items, CreativeEntryGenerator creativeEntries){
        this.settings = settings;
        this.id = id;
        this.items = items;
        registerItems();
        COLORED_ITEM_SETS.add(this);
        ColoredItemSetProperties.CREATIVE_ENTRIES.put(id, getSettings().precedingCreativeEntries);
        ColoredItemSetProperties.CREATIVE_ENTRY_GENERATORS.put(id, creativeEntries);
        if (UnifiedPlatform.getModLoader() == ModLoader.FABRIC) {
            UnifiedPlatform.executeAfter(net.minecraft.core.registries.Registries.ITEM, () -> ColoredItemSetProperties.init(List.of(this)));
        }
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
        ExtensibleCodec.Entry<Function<Item.Properties, Item>> itemType;
        if (getSettings().type.left().isPresent()) itemType = getSettings().type.left().get().apply(dye);
        else itemType = ExtensibleCodecs.ITEM.register(Identifier.fromNamespaceAndPath(id.getNamespace(), name), () -> getSettings().type.right().get().apply(dye)).create();
        SuppliedItem item = items.register(name, itemType, builder -> {
            getSettings().builder.accept(dye, builder);
            if (getSettings().dyeRecipe != null) builder.data(data -> data.recipes((currentItem, provider) -> {
                List<SuppliedItem> otherItems = registeredItems.stream()
                        .filter(otherItem -> getDyeFromItem(otherItem) != dye)
                        .toList();
                getSettings().dyeRecipe.accept(Items.DYE.pick(dye), otherItems, currentItem, provider);
            }));
        });
        dyesByItem.put(item, dye);
        itemsByDye.put(dye, item);
        registeredItems.add(item);
        return item;
    }

    public static class Settings implements Cloneable {

        private Either<Function<DyeColor, ExtensibleCodec.Entry<Function<Item.Properties, Item>>>, Function<DyeColor, Function<Item.Properties, Item>>> type = Either.left(_ -> VanillaItemCodecs.ITEM.create());
        private BiConsumer<DyeColor, ItemGenerator.Builder> builder = (_, _) -> {};

        private @Nullable QuadConsumer<Item, List<SuppliedItem>, Item, RecipeProvider> dyeRecipe;

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

        private final ItemGenerator items;
        private final CreativeEntryGenerator creativeEntries;

        public ColoredItemSet build() {
            return new ColoredItemSet(id, settings, items, creativeEntries);
        }

        public RegistryBuilder(Identifier id, ColoredItemPreset preset, ItemGenerator items, CreativeEntryGenerator creativeEntries) {
            super(preset.settings.copy());

            this.id = id;
            this.items = items;
            this.creativeEntries = creativeEntries;
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

        public T type(Function<DyeColor, ExtensibleCodec.Entry<Function<Item.Properties, Item>>> type) {
            settings.type = Either.left(type);
            return self();
        }

        public T builder(BiConsumer<DyeColor, ItemGenerator.Builder> builder) {
            settings.builder = settings.builder.andThen(builder);
            return self();
        }

        public T dyeRecipe(QuadConsumer<Item, List<SuppliedItem>, Item, RecipeProvider> recipe) {
            settings.dyeRecipe = recipe;
            return self();
        }
    }

    public record PrecedingCreativeEntries(Pair<ResourceKey<CreativeModeTab>, Supplier<? extends ItemLike>> firstTab, @Nullable Pair<ResourceKey<CreativeModeTab>, Supplier<? extends ItemLike>> secondTab) {}
}
