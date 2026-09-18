package net.rebel459.unified.impl.util;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.stream.Stream;

public record LootProviderContext(HolderLookup.Provider registries) implements LootTableSubProvider.Context {
    @Override public Holder.Reference<LootTable> accept(ResourceKey<LootTable> key, LootTable.Builder value) {
        return Holder.Reference.createStandAlone(registries.lookupOrThrow(Registries.LOOT_TABLE), key);
    }

    @Override public <S> HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> key) {
        return registries.lookupOrThrow(key);
    }

    @Override public <S> Stream<Holder.Reference<S>> listContextElements(ResourceKey<? extends Registry<? extends S>> key) {
        return registries.lookupOrThrow(key).listElements();
    }
}
