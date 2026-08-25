package net.rebel459.unified.api.event;

import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;

import java.util.function.Predicate;

public interface LootTableContext {
    void addPool(LootPool.Builder pool);
    void modifyPool(Predicate<Holder<Item>> predicate, LootEntry entry);
    @Deprecated
    void editPool(Predicate<Item> predicate, LootEntry entry);
}