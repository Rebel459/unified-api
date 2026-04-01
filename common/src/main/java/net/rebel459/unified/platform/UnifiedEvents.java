package net.rebel459.unified.platform;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.rebel459.unified.util.event.LootTableProvider;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class UnifiedEvents {

    public static class DefaultItemComponents {

        private DefaultItemComponents() {}

        private static final List<Entry> ENTRIES = new CopyOnWriteArrayList<>();

        public static void modify(TriConsumer<Item, DataComponentMap.Builder, HolderLookup.Provider> modifier) {
            ENTRIES.add(new Entry(modifier));
        }

        private record Entry(TriConsumer<Item, DataComponentMap.Builder, HolderLookup.Provider> modifier) {}

        private static final List<FilteredEntry> FILTERED_ENTRIES = new CopyOnWriteArrayList<>();

        public static void modifyFiltered(Predicate<Item> filter, TriConsumer<Item, DataComponentMap.Builder, HolderLookup.Provider> modifier) {
            FILTERED_ENTRIES.add(new FilteredEntry(filter, modifier));
        }

        private record FilteredEntry(Predicate<Item> filter, TriConsumer<Item, DataComponentMap.Builder, HolderLookup.Provider> modifier) {}

        static void passModify(Item item, DataComponentMap.Builder builder, HolderLookup.Provider provider) {
            for (Entry entry : ENTRIES) {
                entry.modifier.accept(item, builder, provider);
            }
            for (FilteredEntry entry : FILTERED_ENTRIES) {
                if (entry.filter.test(item)) {
                    entry.modifier.accept(item, builder, provider);
                }
            }
        }
    }

    public static class Players {

        private Players() {}

        private static final List<Consumer<ServerPlayer>> JOIN_LISTENERS = new CopyOnWriteArrayList<>();

        public static void onJoin(Consumer<ServerPlayer> listener) {
            JOIN_LISTENERS.add(listener);
        }

        static void passOnJoin(ServerPlayer player) {
            for (Consumer<ServerPlayer> listener : JOIN_LISTENERS) {
                listener.accept(player);
            }
        }

        private static final List<Consumer<ServerPlayer>> LEAVE_LISTENERS = new CopyOnWriteArrayList<>();

        public static void onLeave(Consumer<ServerPlayer> listener) {
            LEAVE_LISTENERS.add(listener);
        }

        static void passOnLeave(ServerPlayer player) {
            for (Consumer<ServerPlayer> listener : LEAVE_LISTENERS) {
                listener.accept(player);
            }
        }

        static final List<BiConsumer<ServerPlayer, ServerPlayer>> RESPAWN_LISTENERS = new CopyOnWriteArrayList<>();

        public static void onRespawn(BiConsumer<ServerPlayer, ServerPlayer> listener) {
            RESPAWN_LISTENERS.add(listener);
        }

        // pass handled in impl
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

        private static final List<Consumer<MinecraftServer>> SERVER_TICKED_START_LISTENERS = new CopyOnWriteArrayList<>();

        public static void onTickStart(Consumer<MinecraftServer> handler) {
            SERVER_TICKED_START_LISTENERS.add(handler);
        }

        static void passOnTickStart(MinecraftServer server) {
            for (Consumer<MinecraftServer> listener : SERVER_TICKED_START_LISTENERS) {
                listener.accept(server);
            }
        }

        private static final List<Consumer<ServerLevel>> SERVER_LEVEL_TICKED_START_LISTENERS = new CopyOnWriteArrayList<>();

        public static void onLevelTickStart(Consumer<ServerLevel> handler) {
            SERVER_LEVEL_TICKED_START_LISTENERS.add(handler);
        }

        static void passOnLevelTickStart(ServerLevel level) {
            for (Consumer<ServerLevel> listener : SERVER_LEVEL_TICKED_START_LISTENERS) {
                listener.accept(level);
            }
        }

        private static final List<Consumer<MinecraftServer>> SERVER_TICKED_END_LISTENERS = new CopyOnWriteArrayList<>();

        public static void onTickEnd(Consumer<MinecraftServer> handler) {
            SERVER_TICKED_END_LISTENERS.add(handler);
        }

        static void passOnTickEnd(MinecraftServer server) {
            for (Consumer<MinecraftServer> listener : SERVER_TICKED_END_LISTENERS) {
                listener.accept(server);
            }
        }

        private static final List<Consumer<ServerLevel>> SERVER_LEVEL_TICKED_END_LISTENERS = new CopyOnWriteArrayList<>();

        public static void onLevelTickEnd(Consumer<ServerLevel> handler) {
            SERVER_LEVEL_TICKED_END_LISTENERS.add(handler);
        }

        static void passOnLevelTickEnd(ServerLevel level) {
            for (Consumer<ServerLevel> listener : SERVER_LEVEL_TICKED_END_LISTENERS) {
                listener.accept(level);
            }
        }
    }

    public static class LootTables {

        private LootTables() {}

        public interface LootTable {
            ResourceKey<net.minecraft.world.level.storage.loot.LootTable> getKey();

            HolderLookup.Provider getProvider();

            void addPool(LootPool.Builder pool);

            void editPool(Predicate<Holder<Item>> itemPredicate, LootPoolEntryContainer.Builder<?> entry, boolean replace);
        }

        public interface Entry {
            void modify(LootTable lootTable);
        }

        private static final List<Entry> ENTRIES = new CopyOnWriteArrayList<>();

        public static void modify(Entry handler) {
            ENTRIES.add(handler);
        }

        private static final List<FilteredEntry> FILTERED_ENTRIES = new CopyOnWriteArrayList<>();

        public static void modifyFiltered(Predicate<ResourceKey<net.minecraft.world.level.storage.loot.LootTable>> filter, Entry handler) {
            FILTERED_ENTRIES.add(new FilteredEntry(filter, handler));
        }

        private record FilteredEntry(Predicate<ResourceKey<net.minecraft.world.level.storage.loot.LootTable>> filter, Entry handler) {}

        static void passModify(ResourceKey<net.minecraft.world.level.storage.loot.LootTable> key, PoolAccess pools, HolderLookup.Provider provider) {
            var lootTable = new LootTableImpl(key, pools, provider);

            for (Entry entry : ENTRIES) {
                entry.modify(lootTable);
            }
            for (FilteredEntry entry : FILTERED_ENTRIES) {
                if (entry.filter.test(key)) {
                    entry.handler.modify(lootTable);
                }
            }
        }

        interface PoolAccess {
            List<LootPool.Builder> pools();

            void addPool(LootPool.Builder pool);
        }

        private record LootTableImpl(ResourceKey<net.minecraft.world.level.storage.loot.LootTable> key, PoolAccess pools, HolderLookup.Provider provider) implements LootTable {
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
                pools.addPool(pool);
            }

            @Override
            public void editPool(Predicate<Holder<Item>> itemPredicate, LootPoolEntryContainer.Builder<?> entry, boolean replace) {
                LootPoolEntryContainer builtEntry = entry.build();

                for (LootPool.Builder pool : pools.pools()) {
                    List<LootPoolEntryContainer> entries = new ArrayList<>(LootTableProvider.getEntries(pool));
                    boolean matchesPool = entries.stream().anyMatch(existing -> matches(existing, itemPredicate));
                    if (!matchesPool) {
                        continue;
                    }

                    if (!replace) {
                        entries.add(builtEntry);
                        pool.entries = LootTableProvider.immutableBuilder(entries);
                        continue;
                    }

                    boolean changed = false;
                    for (int i = 0; i < entries.size(); i++) {
                        if (matches(entries.get(i), itemPredicate)) {
                            entries.set(i, builtEntry);
                            changed = true;
                        }
                    }

                    if (changed) {
                        pool.entries = LootTableProvider.immutableBuilder(entries);
                    }
                }
            }

            private static boolean matches(LootPoolEntryContainer entry, Predicate<Holder<Item>> itemPredicate) {
                if (entry instanceof LootItem lootItem && lootItem.item instanceof Holder<?> holder && holder.value() instanceof Item) {
                    return itemPredicate.test(lootItem.item);
                }
                return false;
            }
        }
    }

    public static class Items {

        private Items() {}

        static final List<TriConsumer<Level, Player, InteractionHand>> BEFORE_USE_LISTENERS = new CopyOnWriteArrayList<>();

        public static void beforeUse(TriConsumer<Level, Player, InteractionHand> listener) {
            BEFORE_USE_LISTENERS.add(listener);
        }

        // pass handled in impl

        static final List<TriConsumer<Level, Player, InteractionHand>> AFTER_USE_LISTENERS = new CopyOnWriteArrayList<>();

        public static void afterUse(TriConsumer<Level, Player, InteractionHand> listener) {
            AFTER_USE_LISTENERS.add(listener);
        }

        // pass handled in impl

        static final List<Consumer<UseOnContext>> USE_ON_LISTENERS = new CopyOnWriteArrayList<>();

        public static void onUseOn(Consumer<UseOnContext> listener) {
            USE_ON_LISTENERS.add(listener);
        }

        // pass handled in impl
    }

    public static class Blocks {

        private Blocks() {}

        static final List<Consumer<BlockPlaceContext>> BEFORE_PLACE_LISTENERS = new CopyOnWriteArrayList<>();

        public static void beforePlace(Consumer<BlockPlaceContext> listener) {
            BEFORE_PLACE_LISTENERS.add(listener);
        }

        // pass handled in impl

        static final List<Consumer<BlockPlaceContext>> AFTER_PLACE_LISTENERS = new CopyOnWriteArrayList<>();

        public static void afterPlace(Consumer<BlockPlaceContext> listener) {
            AFTER_PLACE_LISTENERS.add(listener);
        }

        // pass handled in impl

        static final List<Consumer<UseOnContext>> USE_ON_LISTENERS = new CopyOnWriteArrayList<>();

        public static void onUseOn(Consumer<UseOnContext> listener) {
            USE_ON_LISTENERS.add(listener);
        }

        // pass handled in impl
    }

    public static class Entities {

        private Entities() {}

        private static final List<BiConsumer<LivingEntity, DamageSource>> DEATH_LISTENERS = new CopyOnWriteArrayList<>();

        public static void onDeath(BiConsumer<LivingEntity, DamageSource> listener) {
            DEATH_LISTENERS.add(listener);
        }

        static void passOnDeath(LivingEntity entity, DamageSource source) {
            for (BiConsumer<LivingEntity, DamageSource> listener : DEATH_LISTENERS) {
                listener.accept(entity, source);
            }
        }

        private static final List<Consumer<EquipmentContext>> EQUIPMENT_CHANGE_LISTENERS = new CopyOnWriteArrayList<>();

        public static void onEquipmentChange(Consumer<EquipmentContext> listener) {
            EQUIPMENT_CHANGE_LISTENERS.add(listener);
        }

        static void passOnEquipmentChange(LivingEntity entity, EquipmentSlot slot, ItemStack oldStack, ItemStack newStack) {
            for (Consumer<EquipmentContext> listener : EQUIPMENT_CHANGE_LISTENERS) {
                listener.accept(new EquipmentContext(entity, slot, oldStack, newStack));
            }
        }

        public record EquipmentContext(LivingEntity entity, EquipmentSlot slot, ItemStack oldStack, ItemStack newStack) {}

        static final List<BiConsumer<Entity, ServerLevel>> LOAD_LISTENERS = new CopyOnWriteArrayList<>();

        public static void onLoad(BiConsumer<Entity, ServerLevel> listener) {
            LOAD_LISTENERS.add(listener);
        }

        // pass handled in impl

        static final List<BiConsumer<Entity, ServerLevel>> UNLOAD_LISTENERS = new CopyOnWriteArrayList<>();

        public static void onUnload(BiConsumer<Entity, ServerLevel> listener) {
            UNLOAD_LISTENERS.add(listener);
        }

        // pass handled in impl
    }
}
