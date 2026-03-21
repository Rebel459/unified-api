package net.rebel459.unified.platform;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class UnifiedEvents {

    public static class ItemComponents {

        private ItemComponents() {}

        private static final List<Entry> ENTRIES = new CopyOnWriteArrayList<>();

        public static void modify(BiConsumer<Item, DataComponentMap.Builder> modifier) {
            ENTRIES.add(new Entry(modifier));
        }

        private record Entry(BiConsumer<Item, DataComponentMap.Builder> modifier) {}

        private static final List<FilteredEntry> FILTERED_ENTRIES = new CopyOnWriteArrayList<>();

        public static void modifyWithFilter(Predicate<Item> filter, BiConsumer<Item, DataComponentMap.Builder> modifier) {
            FILTERED_ENTRIES.add(new FilteredEntry(filter, modifier));
        }

        private record FilteredEntry(Predicate<Item> filter, BiConsumer<Item, DataComponentMap.Builder> modifier) {}

        static void passModify(Item item, DataComponentMap.Builder builder) {
            for (Entry entry : ENTRIES) {
                entry.modifier.accept(item, builder);
            }
            for (FilteredEntry entry : FILTERED_ENTRIES) {
                if (entry.filter.test(item)) {
                    entry.modifier.accept(item, builder);
                }
            }
        }
    }

    public static class Players {

        private Players() {}

        private static final List<Consumer<Player>> JOIN_LISTENERS = new CopyOnWriteArrayList<>();

        public static void onJoin(Consumer<Player> listener) {
            JOIN_LISTENERS.add(listener);
        }

        static void passOnJoin(Player player) {
            for (Consumer<Player> listener : JOIN_LISTENERS) {
                listener.accept(player);
            }
        }

        private static final List<Consumer<Player>> LEAVE_LISTENERS = new CopyOnWriteArrayList<>();

        public static void onLeave(Consumer<Player> listener) {
            LEAVE_LISTENERS.add(listener);
        }

        static void passOnLeave(Player player) {
            for (Consumer<Player> listener : LEAVE_LISTENERS) {
                listener.accept(player);
            }
        }

        private static final List<Consumer<Player>> RESPAWN_LISTENERS = new CopyOnWriteArrayList<>();

        public static void onRespawn(Consumer<Player> listener) {
            RESPAWN_LISTENERS.add(listener);
        }

        static void passOnRespawn(Player player) {
            for (Consumer<Player> listener : RESPAWN_LISTENERS) {
                listener.accept(player);
            }
        }
    }

    public static class Commands {

        private Commands() {}

        public interface Entry {
            void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext, net.minecraft.commands.Commands.CommandSelection selection);
        }

        private static final List<Entry> ENTRIES = new CopyOnWriteArrayList<>();

        public static void register(Entry handler) {
            ENTRIES.add(handler);
        }

        static void passRegister(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext, net.minecraft.commands.Commands.CommandSelection selection) {
            for (Entry entry : ENTRIES) {
                entry.register(dispatcher, buildContext, selection);
            }
        }
    }

    public static class Servers {

        private Servers() {}

        private static final List<Consumer<MinecraftServer>> DATAPACK_RELOAD_ENTRIES = new CopyOnWriteArrayList<>();

        public static void onDatapackLoad(Consumer<MinecraftServer> handler) {
            DATAPACK_RELOAD_ENTRIES.add(handler);
        }

        static void passOnDatapackLoad(MinecraftServer server) {
            for (Consumer<MinecraftServer> listener : DATAPACK_RELOAD_ENTRIES) {
                listener.accept(server);
            }
        }

        private static final List<Consumer<MinecraftServer>> SERVER_STARTED_LISTENERS = new CopyOnWriteArrayList<>();

        public static void onStart(Consumer<MinecraftServer> handler) {
            SERVER_STARTED_LISTENERS.add(handler);
        }

        static void passOnStart(MinecraftServer server) {
            for (Consumer<MinecraftServer> listener : SERVER_STARTED_LISTENERS) {
                listener.accept(server);
            }
        }

        private static final List<Consumer<MinecraftServer>> SERVER_STOPPED_LISTENERS = new CopyOnWriteArrayList<>();

        public static void onStop(Consumer<MinecraftServer> handler) {
            SERVER_STOPPED_LISTENERS.add(handler);
        }

        static void passOnStop(MinecraftServer server) {
            for (Consumer<MinecraftServer> listener : SERVER_STOPPED_LISTENERS) {
                listener.accept(server);
            }
        }
    }

    public static class LootTables {

        private LootTables() {}

        public interface LootTable {
            ResourceKey<net.minecraft.world.level.storage.loot.LootTable> getKey();

            HolderLookup.Provider getProvider();

            void addPool(LootPool.Builder pool);
        }

        public interface Entry {
            void modify(LootTable context);
        }

        private static final List<Entry> ENTRIES = new CopyOnWriteArrayList<>();

        public static void modify(Entry handler) {
            ENTRIES.add(handler);
        }

        private static final List<FilteredEntry> FILTERED_ENTRIES = new CopyOnWriteArrayList<>();

        public static void modifyWithFilter(Predicate<ResourceKey<net.minecraft.world.level.storage.loot.LootTable>> filter, Entry handler) {
            FILTERED_ENTRIES.add(new FilteredEntry(filter, handler));
        }

        private record FilteredEntry(Predicate<ResourceKey<net.minecraft.world.level.storage.loot.LootTable>> filter, Entry handler) {}

        static void passModify(ResourceKey<net.minecraft.world.level.storage.loot.LootTable> key, Consumer<LootPool.Builder> poolAdder, HolderLookup.Provider provider) {
            var context = new LootTableImpl(key, poolAdder, provider);

            for (Entry entry : ENTRIES) {
                entry.modify(context);
            }
            for (FilteredEntry entry : FILTERED_ENTRIES) {
                if (entry.filter.test(key)) {
                    entry.handler.modify(context);
                }
            }
        }

        private record LootTableImpl(ResourceKey<net.minecraft.world.level.storage.loot.LootTable> key, Consumer<LootPool.Builder> poolAdder, HolderLookup.Provider provider) implements LootTable {
            @Override
            public ResourceKey<net.minecraft.world.level.storage.loot.LootTable> getKey() {
                return key;
            }

            @Override
            public HolderLookup.Provider getProvider() {
                return provider;
            }

            @Override
            public void addPool(LootPool.Builder pool) {
                poolAdder.accept(pool);
            }
        }
    }
}
