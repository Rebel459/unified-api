package net.rebel459.unified.api.util;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.rebel459.unified.platform.InternalHandlerImpl;

import java.util.function.Supplier;

public class CreativeModeTabBuilder {
        private static final CreativeModeTab.DisplayItemsGenerator EMPTY_GENERATOR = (parameters, output) -> {};
        private CreativeModeTab.Row row;
        private int column;
        private Component displayName = Component.empty();
        private Supplier<ItemStack> iconGenerator = () -> ItemStack.EMPTY;
        private CreativeModeTab.DisplayItemsGenerator displayItemsGenerator;
        private boolean canScroll;
        private boolean showTitle;
        private boolean alignedRight;
        private Identifier backgroundTexture;

        public CreativeModeTabBuilder() {
            this.displayItemsGenerator = EMPTY_GENERATOR;
            this.canScroll = true;
            this.showTitle = true;
            this.alignedRight = false;
            this.backgroundTexture = CreativeModeTab.DEFAULT_BACKGROUND;
            this.row = CreativeModeTab.Row.TOP;
            this.column = 0;
        }

        public CreativeModeTabBuilder title(final Component displayName) {
            this.displayName = displayName;
            return this;
        }

        public CreativeModeTabBuilder icon(final Supplier<ItemStack> iconGenerator) {
            this.iconGenerator = iconGenerator;
            return this;
        }

        public CreativeModeTabBuilder displayItems(final CreativeModeTab.DisplayItemsGenerator displayItemsGenerator) {
            this.displayItemsGenerator = displayItemsGenerator;
            return this;
        }

        public CreativeModeTabBuilder alignedRight() {
            this.alignedRight = true;
            return this;
        }

        public CreativeModeTabBuilder hideTitle() {
            this.showTitle = false;
            return this;
        }

        public CreativeModeTabBuilder noScrollBar() {
            this.canScroll = false;
            return this;
        }

        public CreativeModeTabBuilder backgroundTexture(final Identifier backgroundTexture) {
            this.backgroundTexture = backgroundTexture;
            return this;
        }

        public CreativeModeTabBuilder row(final CreativeModeTab.Row row) {
            this.row = row;
            return this;
        }

        public CreativeModeTabBuilder column(final int column) {
            this.column = column;
            return this;
        }

        public CreativeModeTab build() {
            CreativeModeTab tab = InternalHandlerImpl.INSTANCE.impl().createCreativeModeTab(this.row, this.column, this.displayName, this.iconGenerator, this.displayItemsGenerator);
            tab.alignedRight = this.alignedRight;
            tab.showTitle = this.showTitle;
            tab.canScroll = this.canScroll;
            tab.backgroundTexture = this.backgroundTexture;
            return tab;
        }
    }