package net.rebel459.unified.platform;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.rebel459.unified.util.PackInfo;

import java.util.List;

public class UnifiedEvents {

    public interface FuelEvent {

        void add(ItemLike item, int ticks);
        void add(TagKey<Item> item, int ticks);

        static FuelEvent create() {
            return UnifiedFactory.getEvents().createFuelEvent();
        }
    }

    public interface CreativeEvent {

        void add(ResourceKey<CreativeModeTab> tab, ItemLike... items);
        void add(ResourceKey<CreativeModeTab> tab, ItemStack... items);
        void addAfter(ResourceKey<CreativeModeTab> tab, ItemLike existingItem, ItemLike... addedItems);
        void addAfter(ResourceKey<CreativeModeTab> tab, ItemLike existingItem, ItemStack... addedItems);
        void addBefore(ResourceKey<CreativeModeTab> tab, ItemLike existingItem, ItemLike... addedItems);
        void addBefore(ResourceKey<CreativeModeTab> tab, ItemLike existingItem, ItemStack... addedItems);

        static CreativeEvent create() {
            return UnifiedFactory.getEvents().createCreativeEvent();
        }
    }

    public interface PackEvent {

        void add(Identifier id, PackInfo info);

        static PackEvent create() {
            return UnifiedFactory.getEvents().createPackEvent();
        }
    }

    public interface LootEvent {

        void addPool(ResourceKey<LootTable> table, LootPool.Builder pool);
        void addPool(List<ResourceKey<LootTable>> tables, LootPool.Builder pool);
        void addItem(ResourceKey<LootTable> table, ItemLike item, int chance);
        void addItem(List<ResourceKey<LootTable>> tables, ItemLike item, int chance);

        static LootEvent create() {
            return UnifiedFactory.getEvents().createLootEvent();
        }
    }
}