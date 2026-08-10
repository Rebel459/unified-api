package net.rebel459.unified.api.registry;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;

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
        private CreativeModeTab.Row row;
        private int column;
        private Component displayName = Component.empty();
        private Supplier<ItemStack> iconGenerator = () -> ItemStack.EMPTY;
        private CreativeModeTab.DisplayItemsGenerator displayItemsGenerator;
        private boolean canScroll;
        private boolean showTitle;
        private boolean alignedRight;
        private CreativeModeTab.Type type;
        private Identifier backgroundTexture;

        private Builder() {
            this.displayItemsGenerator = EMPTY_GENERATOR;
            this.canScroll = true;
            this.showTitle = true;
            this.alignedRight = false;
            this.type = CreativeModeTab.Type.CATEGORY;
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

        public Builder displayItems(final CreativeModeTab.DisplayItemsGenerator displayItemsGenerator) {
            this.displayItemsGenerator = displayItemsGenerator;
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
            if ((this.type == CreativeModeTab.Type.HOTBAR || this.type == CreativeModeTab.Type.INVENTORY) && this.displayItemsGenerator != EMPTY_GENERATOR) {
                throw new IllegalStateException("Special tabs can't have display items");
            } else {
                CreativeModeTab tab = new CreativeModeTab(this.row, this.column, this.type, this.displayName, this.iconGenerator, this.displayItemsGenerator);
                tab.alignedRight = this.alignedRight;
                tab.showTitle = this.showTitle;
                tab.canScroll = this.canScroll;
                tab.backgroundTexture = this.backgroundTexture;
                return tab;
            }
        }
    }
}
