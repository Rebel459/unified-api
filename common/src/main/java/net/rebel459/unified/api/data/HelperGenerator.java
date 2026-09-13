package net.rebel459.unified.api.data;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.rebel459.unified.api.codec.CodecGenerator;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.api.event.LootEntry;
import net.rebel459.unified.api.util.RecipeProvider;
import net.rebel459.unified.impl.core.DataProviders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class HelperGenerator {
    protected final String modId;
    protected final Optional<ExtensibleCodec.Entry<Boolean>> requirement;

    protected HelperGenerator(String modId, Optional<ExtensibleCodec.Entry<Boolean>> requirement) {
        this.modId = modId;
        this.requirement = requirement;
    }

    public static final class CreativeEntries extends HelperGenerator {
        private final List<net.rebel459.unified.impl.data.CreativeEntries.Definition> entries = new ArrayList<>();

        public CreativeEntries(String name, String modId, Optional<ExtensibleCodec.Entry<Boolean>> requirement) {
            super(modId, requirement);
            CodecGenerator.assets(modId, Identifier.fromNamespaceAndPath(modId, "unified/creative_entries/" + name), requirement, ops ->
                    net.rebel459.unified.impl.data.CreativeEntries.Definition.LIST_CODEC.encodeStart(ops, List.copyOf(entries)).getOrThrow());
        }

        public CreativeEntries insert(ResourceKey<CreativeModeTab> tab, ItemLike... items) {
            return add(tab, new net.rebel459.unified.impl.data.CreativeEntries.Insertion.Insert(), itemReferences(items));
        }

        public CreativeEntries insert(ResourceKey<CreativeModeTab> tab, ItemStack... items) {
            return add(tab, new net.rebel459.unified.impl.data.CreativeEntries.Insertion.Insert(), itemStacks(items));
        }

        public CreativeEntries insertAfter(ResourceKey<CreativeModeTab> tab, ItemLike target, ItemLike... items) {
            return add(tab, after(Either.left(target.asItem())), itemReferences(items));
        }

        public CreativeEntries insertAfter(ResourceKey<CreativeModeTab> tab, ItemLike target, ItemStack... items) {
            return add(tab, after(Either.left(target.asItem())), itemStacks(items));
        }

        public CreativeEntries insertAfter(ResourceKey<CreativeModeTab> tab, ItemStack target, ItemLike... items) {
            return add(tab, after(Either.right(target.copy())), itemReferences(items));
        }

        public CreativeEntries insertAfter(ResourceKey<CreativeModeTab> tab, ItemStack target, ItemStack... items) {
            return add(tab, after(Either.right(target.copy())), itemStacks(items));
        }

        public CreativeEntries insertBefore(ResourceKey<CreativeModeTab> tab, ItemLike target, ItemLike... items) {
            return add(tab, before(Either.left(target.asItem())), itemReferences(items));
        }

        public CreativeEntries insertBefore(ResourceKey<CreativeModeTab> tab, ItemLike target, ItemStack... items) {
            return add(tab, before(Either.left(target.asItem())), itemStacks(items));
        }

        public CreativeEntries insertBefore(ResourceKey<CreativeModeTab> tab, ItemStack target, ItemLike... items) {
            return add(tab, before(Either.right(target.copy())), itemReferences(items));
        }

        public CreativeEntries insertBefore(ResourceKey<CreativeModeTab> tab, ItemStack target, ItemStack... items) {
            return add(tab, before(Either.right(target.copy())), itemStacks(items));
        }

        private CreativeEntries add(ResourceKey<CreativeModeTab> tab, net.rebel459.unified.impl.data.CreativeEntries.Insertion insertion, List<Either<Item, ItemStack>> items) {
            entries.add(new net.rebel459.unified.impl.data.CreativeEntries.Definition(tab, insertion, items));
            return this;
        }

        private static net.rebel459.unified.impl.data.CreativeEntries.Insertion.After after(Either<Item, ItemStack> target) {
            return new net.rebel459.unified.impl.data.CreativeEntries.Insertion.After(target);
        }

        private static net.rebel459.unified.impl.data.CreativeEntries.Insertion.Before before(Either<Item, ItemStack> target) {
            return new net.rebel459.unified.impl.data.CreativeEntries.Insertion.Before(target);
        }

        private static List<Either<Item, ItemStack>> itemReferences(ItemLike[] items) {
            return Arrays.stream(items).map(item -> Either.<Item, ItemStack>left(item.asItem())).toList();
        }

        private static List<Either<Item, ItemStack>> itemStacks(ItemStack[] items) {
            return Arrays.stream(items).map(ItemStack::copy).map(Either::<Item, ItemStack>right).toList();
        }
    }

    public static final class Tags extends HelperGenerator {

        Tags(String modId, Optional<ExtensibleCodec.Entry<Boolean>> requirement) {
            super(modId, requirement);
        }

        public <T> void add(ResourceKey<? extends Registry<T>> registry, TagKey<T> tag, ResourceKey<T>... entries) {
            DataProviders.TAGS.add(this.modId, new DataProviders.TagRequest<>(registry, generator -> {
                for (ResourceKey<T> entry : entries) {
                    generator.add(tag, entry);
                }
            }));
        }

        public <T> void addOptional(ResourceKey<? extends Registry<T>> registry, TagKey<T> tag, ResourceKey<T>... entries) {
            DataProviders.TAGS.add(this.modId, new DataProviders.TagRequest<>(registry, generator -> {
                for (ResourceKey<T> entry : entries) {
                    generator.addOptional(tag, entry);
                }
            }));
        }
    }

    public static final class Recipes extends HelperGenerator {

        Recipes(String modId, Optional<ExtensibleCodec.Entry<Boolean>> requirement) {
            super(modId, requirement);
        }

        public void add(Consumer<RecipeProvider> recipes) {
            DataProviders.RECIPES.add(this.modId, recipes);
        }
    }

    public static final class LootInjections extends HelperGenerator {
        private final List<LootPool> pools = new ArrayList<>();
        private final List<Function<HolderLookup.Provider, net.rebel459.unified.impl.data.LootInjections.Modifier>> modifiers = new ArrayList<>();

        public LootInjections(String name, ResourceKey<LootTable> target, String modId, Optional<ExtensibleCodec.Entry<Boolean>> requirement) {
            super(modId, requirement);
            CodecGenerator.data(modId, Identifier.fromNamespaceAndPath(modId, "unified/loot_injections/" + name), requirement, (provider, ops) ->
                    net.rebel459.unified.impl.data.LootInjections.Definition.CODEC.encodeStart(ops,
                            new net.rebel459.unified.impl.data.LootInjections.Definition(target.identifier(), List.copyOf(pools),
                                    modifiers.stream().map(modifier -> modifier.apply(provider)).toList())).getOrThrow());
        }

        public LootInjections addPool(LootPool.Builder pool) {
            return addPool(pool.build());
        }
        public LootInjections addPool(LootPool pool) {
            pools.add(pool);
            return this;
        }

        public LootInjections addModifier(Function<HolderLookup.Provider, HolderSet<Item>> items, LootEntry entry) {
            modifiers.add(provider -> new net.rebel459.unified.impl.data.LootInjections.Modifier(items.apply(provider), entry));
            return this;
        }
        public LootInjections addModifier(ItemLike item, LootEntry entry) {
            return addModifier(provider -> HolderSet.direct(provider.getOrThrow(ResourceKey.create(Registries.ITEM, BuiltInRegistries.ITEM.getKey(item.asItem())))), entry);
        }
    }
}
