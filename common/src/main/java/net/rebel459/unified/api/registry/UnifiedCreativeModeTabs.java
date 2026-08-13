package net.rebel459.unified.api.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.rebel459.unified.impl.platform.PlatformHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class UnifiedCreativeModeTabs {

    public static final ResourceKey<CreativeModeTab> BUILDING_BLOCKS = CreativeModeTabs.BUILDING_BLOCKS;
    public static final ResourceKey<CreativeModeTab> COLORED_BLOCKS = CreativeModeTabs.COLORED_BLOCKS;
    public static final ResourceKey<CreativeModeTab> NATURAL_BLOCKS = CreativeModeTabs.NATURAL_BLOCKS;
    public static final ResourceKey<CreativeModeTab> FUNCTIONAL_BLOCKS = CreativeModeTabs.FUNCTIONAL_BLOCKS;
    public static final ResourceKey<CreativeModeTab> REDSTONE_BLOCKS = CreativeModeTabs.REDSTONE_BLOCKS;
    public static final ResourceKey<CreativeModeTab> TOOLS_AND_UTILITIES = CreativeModeTabs.TOOLS_AND_UTILITIES;
    public static final ResourceKey<CreativeModeTab> COMBAT = CreativeModeTabs.COMBAT;
    public static final ResourceKey<CreativeModeTab> FOOD_AND_DRINKS = CreativeModeTabs.FOOD_AND_DRINKS;
    public static final ResourceKey<CreativeModeTab> INGREDIENTS = CreativeModeTabs.INGREDIENTS;
    public static final ResourceKey<CreativeModeTab> SPAWN_EGGS = CreativeModeTabs.SPAWN_EGGS;
    public static final ResourceKey<CreativeModeTab> OP_BLOCKS = CreativeModeTabs.OP_BLOCKS;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private static final CreativeModeTab.DisplayItemsGenerator EMPTY_GENERATOR = (parameters, output) -> {};
        private static final Map<String, List<Item>> ITEMS = new ConcurrentHashMap<>();
        private CreativeModeTab.Row row;
        private int column;
        private Component displayName = Component.empty();
        private Supplier<ItemStack> iconGenerator = () -> ItemStack.EMPTY;
        private CreativeModeTab.DisplayItemsGenerator displayItemsGenerator;
        private String displayItemsModId;
        private boolean canScroll;
        private boolean showTitle;
        private boolean alignedRight;
        private Identifier backgroundTexture;

        private Builder() {
            this.displayItemsGenerator = EMPTY_GENERATOR;
            this.canScroll = true;
            this.showTitle = true;
            this.alignedRight = false;
            this.backgroundTexture = CreativeModeTab.DEFAULT_BACKGROUND;
            this.row = CreativeModeTab.Row.TOP;
            this.column = 0;
        }

        public Builder title(final Component displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder icon(final Supplier<ItemStack> iconGenerator) {
            this.iconGenerator = iconGenerator;
            return this;
        }

        public Builder displayItems(final CreativeModeTab.DisplayItemsGenerator generator) {
            this.displayItemsGenerator = generator;
            return this;
        }

        public Builder displayAllFrom(String modId) {
            this.displayItemsModId = modId;
            return this;
        }

        public Builder alignedRight() {
            this.alignedRight = true;
            return this;
        }

        public Builder hideTitle() {
            this.showTitle = false;
            return this;
        }

        public Builder noScrollBar() {
            this.canScroll = false;
            return this;
        }

        public Builder backgroundTexture(final Identifier backgroundTexture) {
            this.backgroundTexture = backgroundTexture;
            return this;
        }

        public Builder row(final CreativeModeTab.Row row) {
            this.row = row;
            return this;
        }

        public Builder column(final int column) {
            this.column = column;
            return this;
        }

        public CreativeModeTab build() {
            CreativeModeTab tab = PlatformHandler.INSTANCE.internal().createCreativeModeTab(this.row, this.column, this.displayName, this.iconGenerator, getDisplayItemsGenerator());
            tab.alignedRight = this.alignedRight;
            tab.showTitle = this.showTitle;
            tab.canScroll = this.canScroll;
            tab.backgroundTexture = this.backgroundTexture;
            return tab;
        }

        private CreativeModeTab.DisplayItemsGenerator getDisplayItemsGenerator() {
            CreativeModeTab.DisplayItemsGenerator generator = this.displayItemsGenerator;
            String modId = this.displayItemsModId;

            return (parameters, output) -> {
                generator.accept(parameters, output);

                if (modId != null) {
                    for (Item item : getItems(modId)) {
                        output.accept(item);
                    }
                }
            };
        }

        private static List<Item> getItems(String modId) {
            return ITEMS.computeIfAbsent(modId, id -> {
                List<Item> items = new ArrayList<>();

                for (Map.Entry<ResourceKey<Item>, Item> entry : BuiltInRegistries.ITEM.entrySet()) {
                    if (entry.getKey().identifier().getNamespace().equals(id)) {
                        items.add(entry.getValue());
                    }
                }

                return List.copyOf(items);
            });
        }
    }
}
