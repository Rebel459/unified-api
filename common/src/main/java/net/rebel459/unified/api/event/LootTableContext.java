package net.rebel459.unified.api.event;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;

import java.util.function.Predicate;

public interface LootTableContext {
    void addPool(LootPool.Builder pool);
    void editPool(Predicate<Item> predicate, LootEntry entry);
}