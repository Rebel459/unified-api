package net.rebel459.unified.impl.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;
import net.rebel459.unified.Unified;
import net.rebel459.unified.api.core.UnifiedEvents;
import net.rebel459.unified.api.core.UnifiedHelpers;
import net.rebel459.unified.api.event.LootEntry;
import net.rebel459.unified.api.event.LootTableContext;
import net.rebel459.unified.impl.event.LootTableProvider;

import java.util.List;

public class LootInjections {

    public static final ResourceKey<Registry<Injection>> KEY = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(Unified.MOD_ID, "loot_injections"));

    public static void init() {
        UnifiedHelpers.DATA_REGISTRIES.register(KEY, Injection.CODEC);
        UnifiedEvents.LootTables.modify((table, key, provider) -> {
            provider.lookup(KEY).ifPresent(injections ->
                    injections.listElements().forEach(holder -> {
                        Injection injection = holder.value();

                        if (!key.identifier().equals(injection.target())) {
                            return;
                        }

                        applyInjection(table, injection);
                    })
            );
        });
    }

    private static void applyInjection(LootTableContext table, Injection injection) {
        for (LootPool pool : injection.pools()) {
            table.addPool(LootTableProvider.poolBuilder(pool));
        }
        for (Modifier modifier : injection.modifiers()) {
            table.modifyPool(modifier.items::contains, modifier.entry());
        }
    }

    public record Injection(Identifier target, List<LootPool> pools, List<Modifier> modifiers) {
        public static final Codec<Injection> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("target").forGetter(Injection::target),
                LootPool.CODEC.listOf().optionalFieldOf("pools", List.of()).forGetter(Injection::pools),
                Modifier.CODEC.listOf().optionalFieldOf("modifiers", List.of()).forGetter(Injection::modifiers)
        ).apply(instance, Injection::new));
    }

    public record Modifier(HolderSet<Item> items, LootEntry entry) {
        public static final Codec<Modifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                RegistryCodecs.homogeneousList(Registries.ITEM).fieldOf("items").forGetter(Modifier::items),
                LootEntry.MAP_CODEC.forGetter(Modifier::entry)
        ).apply(instance, Modifier::new));
    }
}