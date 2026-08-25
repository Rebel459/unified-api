package net.rebel459.unified.impl.core;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;
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
import net.minecraft.world.level.storage.loot.entries.*;
import net.rebel459.unified.api.event.EventTiming;
import net.rebel459.unified.api.event.LootEntry;
import net.rebel459.unified.api.event.LootTableContext;
import net.rebel459.unified.api.util.QuadConsumer;
import net.rebel459.unified.impl.event.LootTableProvider;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class CommonEvents {

    public static class DefaultDataComponents {
        private DefaultDataComponents() {}

        public static final List<Entry> ENTRIES = new CopyOnWriteArrayList<>();

        public record Entry(TriConsumer<Item, DataComponentMap.Builder, HolderLookup.Provider> modifier) {}

        public static final List<FilteredEntry> FILTERED_ENTRIES = new CopyOnWriteArrayList<>();

        public record FilteredEntry(Predicate<Item> filter, TriConsumer<Item, DataComponentMap.Builder, HolderLookup.Provider> modifier) {}

        public static void passModify(Item item, DataComponentMap.Builder builder, HolderLookup.Provider provider) {
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

        public static final List<Consumer<ServerPlayer>> JOIN_LISTENERS = new CopyOnWriteArrayList<>();

        public static void passOnJoin(ServerPlayer player) {
            for (Consumer<ServerPlayer> listener : JOIN_LISTENERS) {
                listener.accept(player);
            }
        }

        public static final List<Consumer<ServerPlayer>> LEAVE_LISTENERS = new CopyOnWriteArrayList<>();

        public static void passOnLeave(ServerPlayer player) {
            for (Consumer<ServerPlayer> listener : LEAVE_LISTENERS) {
                listener.accept(player);
            }
        }

        public static final List<BiConsumer<ServerPlayer, ServerPlayer>> RESPAWN_LISTENERS = new CopyOnWriteArrayList<>();

        public static void passOnRespawn(ServerPlayer oldPlayer, ServerPlayer newPlayer) {
            for (BiConsumer<ServerPlayer, ServerPlayer> listener : RESPAWN_LISTENERS) {
                listener.accept(oldPlayer, newPlayer);
            }
        }

        public static final EnumMap<EventTiming, List<Consumer<Player>>> TICK_LISTENERS = new EnumMap<>(Map.of(
                EventTiming.PRE, new ArrayList<>(),
                EventTiming.POST, new ArrayList<>()
        ));

        public static void passOnTick(EventTiming type, Player player) {
            for (Consumer<Player> listener : TICK_LISTENERS.get(type)) {
                listener.accept(player);
            }
        }
    }

    public static class Commands {

        private Commands() {}

        public static final List<TriConsumer<CommandDispatcher<CommandSourceStack>, CommandBuildContext, net.minecraft.commands.Commands.CommandSelection>> ENTRIES = new CopyOnWriteArrayList<>();

        public static void passRegister(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext, net.minecraft.commands.Commands.CommandSelection selection) {
            for (TriConsumer<CommandDispatcher<CommandSourceStack>, CommandBuildContext, net.minecraft.commands.Commands.CommandSelection> listener : ENTRIES) {
                listener.accept(dispatcher, buildContext, selection);
            }
        }
    }

    public static class Server {

        private Server() {}

        public static final List<Consumer<MinecraftServer>> DATAPACK_RELOAD_ENTRIES = new CopyOnWriteArrayList<>();

        public static void passOnDatapackLoad(MinecraftServer server) {
            for (Consumer<MinecraftServer> listener : DATAPACK_RELOAD_ENTRIES) {
                listener.accept(server);
            }
        }

        public static final List<Consumer<MinecraftServer>> SERVER_STARTED_LISTENERS = new CopyOnWriteArrayList<>();

        public static void passOnStart(MinecraftServer server) {
            for (Consumer<MinecraftServer> listener : SERVER_STARTED_LISTENERS) {
                listener.accept(server);
            }
        }

        public static final List<Consumer<MinecraftServer>> SERVER_STOPPED_LISTENERS = new CopyOnWriteArrayList<>();

        public static void passOnStop(MinecraftServer server) {
            for (Consumer<MinecraftServer> listener : SERVER_STOPPED_LISTENERS) {
                listener.accept(server);
            }
        }

        public static final EnumMap<EventTiming, List<Consumer<MinecraftServer>>> TICK_LISTENERS = new EnumMap<>(Map.of(
                EventTiming.PRE, new ArrayList<>(),
                EventTiming.POST, new ArrayList<>()
        ));

        public static void passOnTick(EventTiming type, MinecraftServer server) {
            for (Consumer<MinecraftServer> listener : TICK_LISTENERS.get(type)) {
                listener.accept(server);
            }
        }

        public static final EnumMap<EventTiming, List<Consumer<ServerLevel>>> LEVEL_TICK_LISTENERS = new EnumMap<>(Map.of(
                EventTiming.PRE, new ArrayList<>(),
                EventTiming.POST, new ArrayList<>()
        ));

        public static void passOnLevelTick(EventTiming type, ServerLevel level) {
            for (Consumer<ServerLevel> listener : LEVEL_TICK_LISTENERS.get(type)) {
                listener.accept(level);
            }
        }

        public static final List<Consumer<ServerLevel>> LEVEL_LOADED_LISTENERS = new CopyOnWriteArrayList<>();

        public static void passOnLevelLoad(ServerLevel level) {
            for (Consumer<ServerLevel> listener : LEVEL_LOADED_LISTENERS) {
                listener.accept(level);
            }
        }
        public static final List<Consumer<ServerLevel>> LEVEL_UNLOADED_LISTENERS = new CopyOnWriteArrayList<>();

        public static void passOnLevelUnload(ServerLevel level) {
            for (Consumer<ServerLevel> listener : LEVEL_UNLOADED_LISTENERS) {
                listener.accept(level);
            }
        }
    }

    public static class LootTables {

        private LootTables() {}

        public static final List<CommonEvents.LootTables.Entry> ENTRIES = new CopyOnWriteArrayList<>();

        public static final List<FilteredEntry> FILTERED_ENTRIES = new CopyOnWriteArrayList<>();

        public record FilteredEntry(Predicate<ResourceKey<net.minecraft.world.level.storage.loot.LootTable>> filter, CommonEvents.LootTables.Entry handler) {}

        public static boolean passModify(ResourceKey<net.minecraft.world.level.storage.loot.LootTable> key, PoolAccess pools, HolderLookup.Provider provider) {
            passModify(key, new LootTableImpl(key, pools, provider), provider);
            return pools.hasChanged();
        }

        public static void passModify(ResourceKey<net.minecraft.world.level.storage.loot.LootTable> key, LootTableContext table, HolderLookup.Provider provider) {
            runHandlers(key, table, provider);
        }

        private static void runHandlers(ResourceKey<net.minecraft.world.level.storage.loot.LootTable> key, LootTableContext table, HolderLookup.Provider provider) {
            for (CommonEvents.LootTables.Entry entry : ENTRIES) {
                entry.modify(table, key, provider);
            }
            for (FilteredEntry entry : FILTERED_ENTRIES) {
                if (entry.filter.test(key)) {
                    entry.handler.modify(table, key, provider);
                }
            }
        }

        public interface PoolAccess {
            List<LootPool.Builder> pools();

            void addPool(LootPool.Builder pool);

            void markChanged();

            boolean hasChanged();
        }

        public interface Entry {
            void modify(LootTableContext table, ResourceKey<net.minecraft.world.level.storage.loot.LootTable> key, HolderLookup.Provider provider);
        }

        public static boolean matches(LootPoolEntryContainer entry, Predicate<Holder<Item>> itemPredicate) {
            if (entry instanceof LootItem lootItem) {
                return itemPredicate.test(lootItem.item);
            }
            if (entry instanceof CompositeEntryBase compositeEntry) {
                for (LootPoolEntryContainer child : compositeEntry.children) {
                    if (matches(child, itemPredicate)) {
                        return true;
                    }
                }
            }
            return false;
        }

        public static boolean handlePoolReplacements(List<LootPoolEntryContainer> entries, Predicate<Holder<Item>> itemPredicate, LootPoolEntryContainer builtEntry, LootPool.Builder pool) {
            boolean changed = false;
            List<LootPoolEntryContainer> rewrittenEntries = new ArrayList<>(entries.size());

            for (int i = 0; i < entries.size(); i++) {
                Result result = replaceEntry(entries.get(i), itemPredicate, builtEntry);
                changed |= result.changed();
                if (result.entry() != null) {
                    rewrittenEntries.add(result.entry());
                }
            }

            if (changed) {
                pool.entries = LootTableProvider.immutableBuilder(rewrittenEntries);
            }
            return changed;
        }

        public static boolean handlePoolRemovals(List<LootPoolEntryContainer> entries, Predicate<Holder<Item>> itemPredicate, LootPool.Builder pool) {
            boolean changed = false;
            List<LootPoolEntryContainer> rewrittenEntries = new ArrayList<>(entries.size());

            for (LootPoolEntryContainer entry : entries) {
                Result result = removeEntry(entry, itemPredicate);
                changed |= result.changed();
                if (result.entry() != null) {
                    rewrittenEntries.add(result.entry());
                }
            }

            if (changed) {
                pool.entries = LootTableProvider.immutableBuilder(rewrittenEntries);
            }
            return changed;
        }

        private static Result replaceEntry(LootPoolEntryContainer entry, Predicate<Holder<Item>> itemPredicate, LootPoolEntryContainer replacement) {
            if (entry instanceof LootItem lootItem) {
                return itemPredicate.test(lootItem.item) ? new Result(replacement, true) : new Result(entry, false);
            }
            if (entry instanceof CompositeEntryBase compositeEntry) {
                boolean changed = false;
                List<LootPoolEntryContainer> rewrittenChildren = new ArrayList<>(compositeEntry.children.size());
                for (LootPoolEntryContainer child : compositeEntry.children) {
                    Result result = replaceEntry(child, itemPredicate, replacement);
                    changed |= result.changed();
                    if (result.entry() != null) {
                        rewrittenChildren.add(result.entry());
                    }
                }

                if (!changed) {
                    return new Result(entry, false);
                }
                if (rewrittenChildren.isEmpty()) {
                    return new Result(null, true);
                }

                return new Result(rebuildEntry(compositeEntry, rewrittenChildren), true);
            }

            return new Result(entry, false);
        }

        private static Result removeEntry(LootPoolEntryContainer entry, Predicate<Holder<Item>> itemPredicate) {
            if (entry instanceof LootItem lootItem) {
                return itemPredicate.test(lootItem.item) ? new Result(null, true) : new Result(entry, false);
            }
            if (entry instanceof CompositeEntryBase compositeEntry) {
                boolean changed = false;
                List<LootPoolEntryContainer> rewrittenChildren = new ArrayList<>(compositeEntry.children.size());
                for (LootPoolEntryContainer child : compositeEntry.children) {
                    Result result = removeEntry(child, itemPredicate);
                    changed |= result.changed();
                    if (result.entry() != null) {
                        rewrittenChildren.add(result.entry());
                    }
                }

                if (!changed) {
                    return new Result(entry, false);
                }
                if (rewrittenChildren.isEmpty()) {
                    return new Result(null, true);
                }

                return new Result(rebuildEntry(compositeEntry, rewrittenChildren), true);
            }

            return new Result(entry, false);
        }

        private static LootPoolEntryContainer rebuildEntry(CompositeEntryBase compositeEntry, List<LootPoolEntryContainer> rewrittenChildren) {
            if (compositeEntry instanceof AlternativesEntry) {
                return new AlternativesEntry(rewrittenChildren, compositeEntry.condition, compositeEntry.modifier);
            }
            if (compositeEntry instanceof EntryGroup) {
                return new EntryGroup(rewrittenChildren, compositeEntry.condition, compositeEntry.modifier);
            }
            if (compositeEntry instanceof SequentialEntry) {
                return new SequentialEntry(rewrittenChildren, compositeEntry.condition, compositeEntry.modifier);
            }

            LogUtils.getLogger().warn("Unsupported CompositeEntryBase type skipped");
            return compositeEntry;
        }

        private record Result(LootPoolEntryContainer entry, boolean changed) {}

        private record LootTableImpl(ResourceKey<net.minecraft.world.level.storage.loot.LootTable> key, PoolAccess pools, HolderLookup.Provider provider) implements LootTableContext {

            @Override
            public void addPool(LootPool.Builder pool) {
                pools.addPool(pool);
            }

            @Override
            public void modifyPool(Predicate<Holder<Item>> predicate, LootEntry entry) {
                switch (entry.getType()) {
                    case INSERT -> {
                        if (entry.getEntry().isEmpty()) {
                            LogUtils.getLogger().warn("Invalid UnifiedLootEntry. Type INSERT requires a LootPoolEntryContainer.Builder<?>");
                            return;
                        }
                        LootPoolEntryContainer builtEntry = entry.getEntry().get().build();

                        for (LootPool.Builder pool : pools.pools()) {
                            List<LootPoolEntryContainer> entries = new ArrayList<>(LootTableProvider.getEntries(pool));
                            boolean matchesPool = entries.stream().anyMatch(existing -> CommonEvents.LootTables.matches(existing, predicate));
                            if (!matchesPool) {
                                continue;
                            }

                            entries.add(builtEntry);
                            pool.entries = LootTableProvider.immutableBuilder(entries);
                            pools.markChanged();
                        }
                    }
                    case REPLACE -> {
                        if (entry.getEntry().isEmpty()) {
                            LogUtils.getLogger().warn("Invalid UnifiedLootEntry. Type REPLACE requires a LootPoolEntryContainer.Builder<?>");
                            return;
                        }
                        LootPoolEntryContainer builtEntry = entry.getEntry().get().build();

                        for (LootPool.Builder pool : pools.pools()) {
                            List<LootPoolEntryContainer> entries = new ArrayList<>(LootTableProvider.getEntries(pool));
                            boolean matchesPool = entries.stream().anyMatch(existing -> CommonEvents.LootTables.matches(existing, predicate));
                            if (!matchesPool) {
                                continue;
                            }

                            if (CommonEvents.LootTables.handlePoolReplacements(entries, predicate, builtEntry, pool)) {
                                pools.markChanged();
                            }
                        }
                    }
                    case REMOVE -> {
                        for (LootPool.Builder pool : pools.pools()) {
                            List<LootPoolEntryContainer> entries = new ArrayList<>(LootTableProvider.getEntries(pool));
                            if (CommonEvents.LootTables.handlePoolRemovals(entries, predicate, pool)) {
                                pools.markChanged();
                            }
                        }
                    }
                }
            }

            @Override
            @Deprecated
            public void editPool(Predicate<Item> predicate, LootEntry entry) {
                this.modifyPool(holder -> predicate.test(holder.value()), entry);
            }
        }
    }

    public static class Items {

        private Items() {}

        public static final EnumMap<EventTiming, List<TriConsumer<Level, Player, InteractionHand>>> USE_LISTENERS = new EnumMap<>(Map.of(
                EventTiming.PRE, new ArrayList<>(),
                EventTiming.POST, new ArrayList<>()
        ));

        public static void passOnUse(EventTiming type, Level level, Player player, InteractionHand hand) {
            for (TriConsumer<Level, Player, InteractionHand> listener : USE_LISTENERS.get(type)) {
                listener.accept(level, player, hand);
            }
        }

        public static final List<Consumer<UseOnContext>> USE_ON_LISTENERS = new CopyOnWriteArrayList<>();

        public static void passOnUseOn(UseOnContext context) {
            for (Consumer<UseOnContext> listener : USE_ON_LISTENERS) {
                listener.accept(context);
            }
        }
    }

    public static class Blocks {

        private Blocks() {}

        public static final EnumMap<EventTiming, List<Consumer<BlockPlaceContext>>> PLACE_LISTENERS = new EnumMap<>(Map.of(
                EventTiming.PRE, new ArrayList<>(),
                EventTiming.POST, new ArrayList<>()
        ));

        public static void passOnPlace(EventTiming type, BlockPlaceContext context) {
            for (Consumer<BlockPlaceContext> listener : PLACE_LISTENERS.get(type)) {
                listener.accept(context);
            }
        }

        public static final List<Consumer<UseOnContext>> USE_ON_LISTENERS = new CopyOnWriteArrayList<>();

        public static void passOnUseOn(UseOnContext context) {
            for (Consumer<UseOnContext> listener : USE_ON_LISTENERS) {
                listener.accept(context);
            }
        }
    }

    public static class Entities {

        private Entities() {}

        public static final List<BiConsumer<LivingEntity, DamageSource>> DEATH_LISTENERS = new CopyOnWriteArrayList<>();

        public static void passOnDeath(LivingEntity entity, DamageSource source) {
            for (BiConsumer<LivingEntity, DamageSource> listener : DEATH_LISTENERS) {
                listener.accept(entity, source);
            }
        }

        public static final List<QuadConsumer<LivingEntity, EquipmentSlot, ItemStack, ItemStack>> EQUIPMENT_CHANGE_LISTENERS = new CopyOnWriteArrayList<>();

        public static void passOnEquipmentChange(LivingEntity entity, EquipmentSlot slot, ItemStack oldStack, ItemStack newStack) {
            for (QuadConsumer<LivingEntity, EquipmentSlot, ItemStack, ItemStack> listener : EQUIPMENT_CHANGE_LISTENERS) {
                listener.accept(entity, slot, oldStack, newStack);
            }
        }

        public static final List<BiConsumer<Entity, ServerLevel>> LOAD_LISTENERS = new CopyOnWriteArrayList<>();

        public static void passOnLoad(Entity entity, ServerLevel level) {
            for (BiConsumer<Entity, ServerLevel> listener : LOAD_LISTENERS) {
                listener.accept(entity, level);
            }
        }

        public static final List<BiConsumer<Entity, ServerLevel>> UNLOAD_LISTENERS = new CopyOnWriteArrayList<>();

        public static void passOnUnload(Entity entity, ServerLevel level) {
            for (BiConsumer<Entity, ServerLevel> listener : UNLOAD_LISTENERS) {
                listener.accept(entity, level);
            }
        }

        public static final EnumMap<EventTiming, List<Consumer<Entity>>> TICK_LISTENERS = new EnumMap<>(Map.of(
                EventTiming.PRE, new ArrayList<>(),
                EventTiming.POST, new ArrayList<>()
        ));

        public static void passOnTick(EventTiming type, Entity entity) {
            for (Consumer<Entity> listener : TICK_LISTENERS.get(type)) {
                listener.accept(entity);
            }
        }

        public static final EnumMap<EventTiming, List<Consumer<LivingEntity>>> LIVING_TICK_LISTENERS = new EnumMap<>(Map.of(
                EventTiming.PRE, new ArrayList<>(),
                EventTiming.POST, new ArrayList<>()
        ));

        public static void passOnLivingTick(EventTiming type, LivingEntity entity) {
            for (Consumer<LivingEntity> listener : LIVING_TICK_LISTENERS.get(type)) {
                listener.accept(entity);
            }
        }
    }

    public static class Levels {

        private Levels() {}

        public static final List<Consumer<Level>> LEVEL_LOADED_LISTENERS = new CopyOnWriteArrayList<>();

        public static void passOnLoad(Level level) {
            for (Consumer<Level> listener : LEVEL_LOADED_LISTENERS) {
                listener.accept(level);
            }
        }

        public static final List<Consumer<Level>> LEVEL_UNLOADED_LISTENERS = new CopyOnWriteArrayList<>();

        public static void passOnUnload(Level level) {
            for (Consumer<Level> listener : LEVEL_UNLOADED_LISTENERS) {
                listener.accept(level);
            }
        }

        public static final EnumMap<EventTiming, List<Consumer<Level>>> LEVEL_TICK_LISTENERS = new EnumMap<>(Map.of(
                EventTiming.PRE, new ArrayList<>(),
                EventTiming.POST, new ArrayList<>()
        ));

        public static void passOnTick(EventTiming type, Level level) {
            for (Consumer<Level> listener : LEVEL_TICK_LISTENERS.get(type)) {
                listener.accept(level);
            }
        }
    }
}
