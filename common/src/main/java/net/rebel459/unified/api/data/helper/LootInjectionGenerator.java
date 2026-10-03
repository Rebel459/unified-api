package net.rebel459.unified.api.data.helper;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.rebel459.unified.api.codec.CodecGenerator;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.api.event.LootEntry;
import net.rebel459.unified.impl.data.helper.LootInjections;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public final class LootInjectionGenerator extends HelperGenerator {

    public LootInjectionGenerator(String modId, Optional<ExtensibleCodec.Entry<Boolean>> requirement) {
        super(modId, requirement);
    }
    
    public Builder create(String name, ResourceKey<LootTable> target) {
        return new Builder(name, target, modId, requirement);
    }
    
    public static final class Builder extends HelperGenerator.Builder {

        private final List<LootPool> pools = new ArrayList<>();
        private final List<Function<HolderLookup.Provider, LootInjections.Modifier>> modifiers = new ArrayList<>();

        Builder(String name, ResourceKey<LootTable> target, String modId, Optional<ExtensibleCodec.Entry<Boolean>> requirement) {
            super(name, modId, requirement);
            CodecGenerator.data(modId, Identifier.fromNamespaceAndPath(modId, "unified/loot_injections/" + name), requirement, (provider, ops) ->
                    LootInjections.Definition.CODEC.encodeStart(ops,
                            new LootInjections.Definition(target.identifier(), List.copyOf(pools),
                                    modifiers.stream().map(modifier -> modifier.apply(provider)).toList())).getOrThrow());
        }

        public LootInjectionGenerator.Builder addPool(LootPool.Builder pool) {
            return addPool(pool.build());
        }

        public LootInjectionGenerator.Builder addPool(LootPool pool) {
            pools.add(pool);
            return this;
        }

        public LootInjectionGenerator.Builder addModifier(Function<HolderLookup.Provider, HolderSet<Item>> items, LootEntry entry) {
            modifiers.add(provider -> new LootInjections.Modifier(items.apply(provider), entry));
            return this;
        }

        public LootInjectionGenerator.Builder addModifier(ItemLike item, LootEntry entry) {
            return addModifier(provider -> HolderSet.direct(provider.getOrThrow(ResourceKey.create(Registries.ITEM, BuiltInRegistries.ITEM.getKey(item.asItem())))), entry);
        }
    }
}
