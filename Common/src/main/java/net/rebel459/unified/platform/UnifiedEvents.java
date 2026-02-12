package net.rebel459.unified.platform;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.rebel459.unified.util.PackInfo;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class UnifiedEvents {

    public interface FurnaceFuels {

        void add(ItemLike item, int ticks);

        static FurnaceFuels create() {
            return UnifiedFactory.getEvents().createFurnaceFuels();
        }
    }

    public interface CreativeEntries {

        void add(ResourceKey<CreativeModeTab> tab, ItemLike... items);
        void add(ResourceKey<CreativeModeTab> tab, ItemStack... items);
        void addAfter(ResourceKey<CreativeModeTab> tab, ItemLike existingItem, ItemLike... addedItems);
        void addAfter(ResourceKey<CreativeModeTab> tab, ItemLike existingItem, ItemStack... addedItems);
        void addBefore(ResourceKey<CreativeModeTab> tab, ItemLike existingItem, ItemLike... addedItems);
        void addBefore(ResourceKey<CreativeModeTab> tab, ItemLike existingItem, ItemStack... addedItems);

        static CreativeEntries create() {
            return UnifiedFactory.getEvents().createCreativeEntries();
        }
    }

    public interface Packs {

        void add(Identifier id, PackInfo info);

        static Packs create() {
            return UnifiedFactory.getEvents().createPacks();
        }
    }

    public interface LootTables {

        void addPool(ResourceKey<LootTable> table, LootPool.Builder... pools);
        void addPool(List<ResourceKey<LootTable>> tables, LootPool.Builder... pools);
        void addItem(ResourceKey<LootTable> table, ItemLike item, int chance);
        void addItem(List<ResourceKey<LootTable>> tables, ItemLike item, int chance);

        static LootTables create() {
            return UnifiedFactory.getEvents().createLootTables();
        }
    }

    public interface StrippableBlocks {

        void add(Block original, Block stripped);

        static StrippableBlocks create() {
            return UnifiedFactory.getEvents().createStrippableBlocks();
        }
    }

    public static class ClientTickEvent {

        private static final List<Consumer<Minecraft>> END_TICK_LISTENERS = new CopyOnWriteArrayList<>();

        private ClientTickEvent() {}

        public static void insert(Consumer<Minecraft> listener) {
            END_TICK_LISTENERS.add(listener);
        }

        public static void pass(Minecraft client) {
            for (Consumer<Minecraft> listener : END_TICK_LISTENERS) {
                listener.accept(client);
            }
        }
    }
}