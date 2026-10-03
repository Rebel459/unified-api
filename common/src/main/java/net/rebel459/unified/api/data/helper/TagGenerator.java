package net.rebel459.unified.api.data.helper;

import net.minecraft.references.BlockItemId;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockItemTagId;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.impl.core.DataProviders;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class TagGenerator extends HelperGenerator {

    public TagGenerator(String modId, Optional<ExtensibleCodec.Entry<Boolean>> requirement) {
        super(modId, requirement);
    }

    public <T> Builder<T> create(TagKey<T> tag) {
        return new Builder<>(tag, modId, requirement);
    }

    public BlockItemBuilder create(BlockItemTagId tag) {
        return new BlockItemBuilder(tag, modId, requirement);
    }

    public static final class Builder<T> extends HelperGenerator {
        private final List<ResourceKey<T>> entries = new ArrayList<>();
        private final List<ResourceKey<T>> optionalEntries = new ArrayList<>();
        private final List<TagKey<T>> tags = new ArrayList<>();
        private final List<TagKey<T>> optionalTags = new ArrayList<>();

        Builder(TagKey<T> tag, String modId, Optional<ExtensibleCodec.Entry<Boolean>> requirement) {
            super(modId, requirement);
            DataProviders.TAGS.add(modId, new DataProviders.TagRequest<>(tag.registry(), provider -> {
                entries.forEach(entry -> provider.add(tag, entry));
                optionalEntries.forEach(entry -> provider.addOptional(tag, entry));
                tags.forEach(entry -> provider.addTag(tag, entry));
                optionalTags.forEach(entry -> provider.addOptionalTag(tag, entry));
            }));
        }

        public TagGenerator.Builder<T> add(ResourceKey<T> entry) {
            entries.add(entry);
            return this;
        }

        public TagGenerator.Builder<T> addOptional(ResourceKey<T> entry) {
            optionalEntries.add(entry);
            return this;
        }

        public TagGenerator.Builder<T> add(TagKey<T> entry) {
            tags.add(entry);
            return this;
        }

        public TagGenerator.Builder<T> addOptional(TagKey<T> entry) {
            optionalTags.add(entry);
            return this;
        }
    }

    public static final class BlockItemBuilder extends HelperGenerator {
        private final TagGenerator.Builder<Block> blocks;
        private final TagGenerator.Builder<Item> items;

        BlockItemBuilder(BlockItemTagId tag, String modId, Optional<ExtensibleCodec.Entry<Boolean>> requirement) {
            super(modId, requirement);
            blocks = new TagGenerator.Builder<>(tag.block(), modId, requirement);
            items = new TagGenerator.Builder<>(tag.item(), modId, requirement);
        }

        public BlockItemBuilder add(BlockItemId entry) {
            blocks.add(entry.block());
            items.add(entry.item());
            return this;
        }

        public BlockItemBuilder addOptional(BlockItemId entry) {
            blocks.addOptional(entry.block());
            items.addOptional(entry.item());
            return this;
        }

        public BlockItemBuilder add(BlockItemTagId entry) {
            blocks.add(entry.block());
            items.add(entry.item());
            return this;
        }

        public BlockItemBuilder addOptional(BlockItemTagId entry) {
            blocks.addOptional(entry.block());
            items.addOptional(entry.item());
            return this;
        }
    }
}
