package net.rebel459.unified.api.data.helper;

import com.mojang.datafixers.util.Either;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.rebel459.unified.api.codec.CodecGenerator;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.impl.data.helper.CreativeEntries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public final class CreativeEntryGenerator extends HelperGenerator {

    public CreativeEntryGenerator(String modId, Optional<ExtensibleCodec.Entry<Boolean>> requirement) {
        super(modId, requirement);
    }

    public Builder create(String name) {
        return new Builder(name, modId, requirement);
    }

    public static final class Builder extends HelperGenerator.Builder {

        private final List<CreativeEntries.Definition> entries = new ArrayList<>();

        Builder(String name, String modId, Optional<ExtensibleCodec.Entry<Boolean>> requirement) {
            super(name, modId, requirement);
            CodecGenerator.assets(modId, Identifier.fromNamespaceAndPath(modId, "unified/creative_entries/" + name), requirement, (_, ops) ->
                    CreativeEntries.Definition.LIST_CODEC.encodeStart(ops, List.copyOf(entries)).getOrThrow());
        }

        public Builder insert(ResourceKey<CreativeModeTab> tab, ItemLike... items) {
            return add(tab, new CreativeEntries.Insertion.Insert(), items(items));
        }

        public Builder insert(ResourceKey<CreativeModeTab> tab, ItemStack... items) {
            return add(tab, new CreativeEntries.Insertion.Insert(), itemStacks(items));
        }

        public Builder insertAfter(ResourceKey<CreativeModeTab> tab, ItemLike target, ItemLike... items) {
            return add(tab, new CreativeEntries.Insertion.After(Either.left(target.asItem())), items(items));
        }

        public Builder insertAfter(ResourceKey<CreativeModeTab> tab, ItemLike target, ItemStack... items) {
            return add(tab, new CreativeEntries.Insertion.After(Either.left(target.asItem())), itemStacks(items));
        }

        public Builder insertAfter(ResourceKey<CreativeModeTab> tab, ItemStack target, ItemLike... items) {
            return add(tab, new CreativeEntries.Insertion.After(Either.right(target.copy())), items(items));
        }

        public Builder insertAfter(ResourceKey<CreativeModeTab> tab, ItemStack target, ItemStack... items) {
            return add(tab, new CreativeEntries.Insertion.After(Either.right(target.copy())), itemStacks(items));
        }

        public Builder insertBefore(ResourceKey<CreativeModeTab> tab, ItemLike target, ItemLike... items) {
            return add(tab, new CreativeEntries.Insertion.Before(Either.left(target.asItem())), items(items));
        }

        public Builder insertBefore(ResourceKey<CreativeModeTab> tab, ItemLike target, ItemStack... items) {
            return add(tab, new CreativeEntries.Insertion.Before(Either.left(target.asItem())), itemStacks(items));
        }

        public Builder insertBefore(ResourceKey<CreativeModeTab> tab, ItemStack target, ItemLike... items) {
            return add(tab, new CreativeEntries.Insertion.Before(Either.right(target.copy())), items(items));
        }

        public Builder insertBefore(ResourceKey<CreativeModeTab> tab, ItemStack target, ItemStack... items) {
            return add(tab, new CreativeEntries.Insertion.Before(Either.right(target.copy())), itemStacks(items));
        }

        private Builder add(ResourceKey<CreativeModeTab> tab, CreativeEntries.Insertion insertion, List<Either<Item, ItemStack>> items) {
            entries.add(new CreativeEntries.Definition(tab, insertion, items));
            return this;
        }

        private static List<Either<Item, ItemStack>> items(ItemLike[] items) {
            return Arrays.stream(items).map(item -> Either.<Item, ItemStack>left(item.asItem())).toList();
        }

        private static List<Either<Item, ItemStack>> itemStacks(ItemStack[] items) {
            return Arrays.stream(items).map(ItemStack::copy).map(Either::<Item, ItemStack>right).toList();
        }
    }
}
