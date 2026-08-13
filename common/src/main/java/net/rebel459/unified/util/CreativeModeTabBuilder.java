package net.rebel459.unified.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.rebel459.unified.platform.InternalHandlerImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class CreativeModeTabBuilder {
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

    private CreativeModeTabBuilder() {
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

    public CreativeModeTabBuilder displayItems(final CreativeModeTab.DisplayItemsGenerator generator) {
        this.displayItemsGenerator = generator;
        return this;
    }

    public CreativeModeTabBuilder displayAllFrom(String modId) {
        this.displayItemsModId = modId;
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
        CreativeModeTab tab = InternalHandlerImpl.INSTANCE.impl().createCreativeModeTab(this.row, this.column, this.displayName, this.iconGenerator, getDisplayItemsGenerator());
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