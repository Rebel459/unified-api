package net.rebel459.unified.api.data.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.rebel459.unified.api.codec.CodecGenerator;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.api.codec.ExtensibleCodecs;
import net.rebel459.unified.api.core.Supplied;
import net.rebel459.unified.api.registry.UnifiedDisplayItemCodecs;
import net.rebel459.unified.impl.core.DataProviders;
import net.rebel459.unified.impl.data.registry.CreativeTabRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CreativeTabGenerator {

    private final String modId;
    private final String namespace;
    private final DataProviders.GenerationSettings settings;
    public CreativeTabGenerator(String modId, String namespace, DataProviders.GenerationSettings settings) {
        this.modId = modId;
        this.namespace = namespace;
        this.settings = settings;
    }

    public Supplied<CreativeModeTab> register(String path, Consumer<Builder> builder) {
        Identifier id = Identifier.fromNamespaceAndPath(namespace, path);
        Builder finalBuilder = new Builder(id);
        builder.accept(finalBuilder);
        Supplier<CreativeTabRegistry.Definition> definition = finalBuilder::definition;
        CodecGenerator.registry(modId, id, "creative_tabs", settings.metadata().priority(), settings.metadata().requirement(), CreativeTabRegistry.CODEC, definition);
        return CreativeTabRegistry.registerDefinition(id, definition);
    }

    public static class Builder {
        private CreativeModeTab.Row row = CreativeModeTab.Row.TOP;
        private int column = 0;
        private Component displayName = Component.empty();
        private Supplier<ItemStack> iconGenerator = () -> ItemStack.EMPTY;
        private final List<ExtensibleCodec.Entry<CreativeModeTab.DisplayItemsGenerator>> displayItemsGenerators = new ArrayList<>();
        private boolean canScroll = true;
        private boolean showTitle = true;
        private boolean alignedRight = false;
        private Identifier backgroundTexture = CreativeModeTab.DEFAULT_BACKGROUND;

        private final Identifier id;

        private Builder(Identifier id) {
            this.id = id;
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
            return displayItems(ExtensibleCodecs.DISPLAY_ITEMS.register(Identifier.fromNamespaceAndPath(id.getNamespace(), "creative_tabs/" + id.getPath()), () -> generator).create());
        }
        public Builder displayItems(final ExtensibleCodec.Entry<CreativeModeTab.DisplayItemsGenerator> generator) {
            this.displayItemsGenerators.add(generator);
            return this;
        }

        public Builder displayAllFrom(String modId) {
            return displayItems(UnifiedDisplayItemCodecs.DISPLAY_FROM_MOD.create(() -> new UnifiedDisplayItemCodecs.ModEntry(modId, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS)));
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

        private CreativeTabRegistry.Definition definition() {
            return new CreativeTabRegistry.Definition(
                    row,
                    column,
                    displayName,
                    iconGenerator,
                    List.copyOf(displayItemsGenerators),
                    canScroll,
                    showTitle,
                    alignedRight ? CreativeTabRegistry.Alignment.RIGHT : CreativeTabRegistry.Alignment.LEFT,
                    backgroundTexture
            );
        }
    }
}
