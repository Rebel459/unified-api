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
import net.rebel459.unified.util.EventType;
import net.rebel459.unified.util.SuppliedItem;
import net.rebel459.unified.util.event.LootTableProvider;
import net.rebel459.unified.util.event.QuadConsumer;
import net.rebel459.unified.util.registry.SuppliedItemImpl;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class UnifiedEvents {

    public static class DefaultDataComponents {

        private DefaultDataComponents() {}

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

        private static final List<TriConsumer<CommandDispatcher<CommandSourceStack>, CommandBuildContext, net.minecraft.commands.Commands.CommandSelection>> ENTRIES = new CopyOnWriteArrayList<>();

        public static void register(TriConsumer<CommandDispatcher<CommandSourceStack>, CommandBuildContext, net.minecraft.commands.Commands.CommandSelection> listener) {
            ENTRIES.add(listener);
        }

        static void passRegister(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext, net.minecraft.commands.Commands.CommandSelection selection) {
            for (TriConsumer<CommandDispatcher<CommandSourceStack>, CommandBuildContext, net.minecraft.commands.Commands.CommandSelection> listener : ENTRIES) {
                listener.accept(dispatcher, buildContext, selection);
            }
        }
    }

    public static class Server {

        private Server() {}

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

        private static final EnumMap<EventType, List<Consumer<MinecraftServer>>> TICK_LISTENERS = new EnumMap<>(Map.of(
                EventType.PRE, new ArrayList<>(),
                EventType.POST, new ArrayList<>()
        ));

        public static void onTick(EventType type, Consumer<MinecraftServer> listener) {
            TICK_LISTENERS.get(type).add(listener);
        }

        static void passOnTick(EventType type, MinecraftServer server) {
            for (Consumer<MinecraftServer> listener : TICK_LISTENERS.get(type)) {
                listener.accept(server);
            }
        }

        private static final EnumMap<EventType, List<Consumer<ServerLevel>>> LEVEL_TICK_LISTENERS = new EnumMap<>(Map.of(
                EventType.PRE, new ArrayList<>(),
                EventType.POST, new ArrayList<>()
        ));

        public static void onLevelTick(EventType type, Consumer<ServerLevel> listener) {
            LEVEL_TICK_LISTENERS.get(type).add(listener);
        }

        static void passOnLevelTick(EventType type, ServerLevel level) {
            for (Consumer<ServerLevel> listener : LEVEL_TICK_LISTENERS.get(type)) {
                listener.accept(level);
            }
        }
    }

    public static class LootTables {

        private LootTables() {}

        private static final List<EventsImpl.LootTables.Entry> ENTRIES = new CopyOnWriteArrayList<>();

        public static void modify(EventsImpl.LootTables.Entry handler) {
            ENTRIES.add(handler);
        }

        private static final List<FilteredEntry> FILTERED_ENTRIES = new CopyOnWriteArrayList<>();

        public static void modifyFiltered(Predicate<ResourceKey<net.minecraft.world.level.storage.loot.LootTable>> filter, EventsImpl.LootTables.Entry handler) {
            FILTERED_ENTRIES.add(new FilteredEntry(filter, handler));
        }

        private record FilteredEntry(Predicate<ResourceKey<net.minecraft.world.level.storage.loot.LootTable>> filter, EventsImpl.LootTables.Entry handler) {}

        static void passModify(ResourceKey<net.minecraft.world.level.storage.loot.LootTable> key, PoolAccess pools, HolderLookup.Provider provider) {
            passModify(key, new LootTableImpl(key, pools, provider), provider);
        }

        static void passModify(ResourceKey<net.minecraft.world.level.storage.loot.LootTable> key, EventsImpl.LootTables.LootTable table, HolderLookup.Provider provider) {
            runHandlers(key, table, provider);
        }

        private static void runHandlers(ResourceKey<net.minecraft.world.level.storage.loot.LootTable> key, EventsImpl.LootTables.LootTable table, HolderLookup.Provider provider) {
            for (EventsImpl.LootTables.Entry entry : ENTRIES) {
                entry.modify(table, key, provider);
            }
            for (FilteredEntry entry : FILTERED_ENTRIES) {
                if (entry.filter.test(key)) {
                    entry.handler.modify(table, key, provider);
                }
            }
        }

        interface PoolAccess {
            List<LootPool.Builder> pools();

            void addPool(LootPool.Builder pool);
        }

        private record LootTableImpl(ResourceKey<net.minecraft.world.level.storage.loot.LootTable> key, PoolAccess pools, HolderLookup.Provider provider) implements EventsImpl.LootTables.LootTable {

            @Override
            public void addPool(LootPool.Builder pool) {
                pools.addPool(pool);
            }

            @Override
            public void editPool(Predicate<SuppliedItem> itemPredicate, LootPoolEntryContainer.Builder<?> entry, boolean replace) {
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

            private static boolean matches(LootPoolEntryContainer entry, Predicate<SuppliedItem> itemPredicate) {
                if (entry instanceof LootItem lootItem && lootItem.item instanceof Holder<?> holder && holder.value() instanceof Item) {
                    return itemPredicate.test(new SuppliedItemImpl(lootItem.item));
                }
                return false;
            }
        }
    }

    public static class Items {

        private Items() {}

        static final EnumMap<EventType, List<TriConsumer<Level, Player, InteractionHand>>> USE_LISTENERS = new EnumMap<>(Map.of(
                EventType.PRE, new ArrayList<>(),
                EventType.POST, new ArrayList<>()
        ));

        public static void onUse(EventType type, TriConsumer<Level, Player, InteractionHand> listener) {
            USE_LISTENERS.get(type).add(listener);
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

        static final EnumMap<EventType, List<Consumer<BlockPlaceContext>>> PLACE_LISTENERS = new EnumMap<>(Map.of(
                EventType.PRE, new ArrayList<>(),
                EventType.POST, new ArrayList<>()
        ));

        public static void onPlace(EventType type, Consumer<BlockPlaceContext> listener) {
            PLACE_LISTENERS.get(type).add(listener);
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

        private static final List<QuadConsumer<LivingEntity, EquipmentSlot, ItemStack, ItemStack>> EQUIPMENT_CHANGE_LISTENERS = new CopyOnWriteArrayList<>();

        public static void onEquipmentChange(QuadConsumer<LivingEntity, EquipmentSlot, ItemStack, ItemStack> listener) {
            EQUIPMENT_CHANGE_LISTENERS.add(listener);
        }

        static void passOnEquipmentChange(LivingEntity entity, EquipmentSlot slot, ItemStack oldStack, ItemStack newStack) {
            for (QuadConsumer<LivingEntity, EquipmentSlot, ItemStack, ItemStack> listener : EQUIPMENT_CHANGE_LISTENERS) {
                listener.accept(entity, slot, oldStack, newStack);
            }
        }

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
