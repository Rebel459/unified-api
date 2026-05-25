package net.rebel459.unified.platform;

import com.mojang.logging.LogUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.*;
import net.rebel459.unified.util.EventType;
import net.rebel459.unified.util.LootEntry;
import net.rebel459.unified.util.event.LootTableProvider;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class EventsImpl {

    public static class Players {

        private Players() {}

        public static void passOnRespawn(ServerPlayer oldPlayer, ServerPlayer newPlayer) {
            for (BiConsumer<ServerPlayer, ServerPlayer> listener : UnifiedEvents.Players.RESPAWN_LISTENERS) {
                listener.accept(oldPlayer, newPlayer);
            }
        }

        public static void passOnTick(EventType type, Player player) {
            for (Consumer<Player> listener : UnifiedEvents.Players.TICK_LISTENERS.get(type)) {
                listener.accept(player);
            }
        }
    }

    public static class Server {

        private Server() {}

        public static void passOnLevelLoad(ServerLevel level) {
            for (Consumer<ServerLevel> listener : UnifiedEvents.Server.LEVEL_LOADED_LISTENERS) {
                listener.accept(level);
            }
        }

        public static void passOnLevelUnload(ServerLevel level) {
            for (Consumer<ServerLevel> listener : UnifiedEvents.Server.LEVEL_UNLOADED_LISTENERS) {
                listener.accept(level);
            }
        }
    }

    public static class LootTables {

        private LootTables() {}

        public interface LootTable {
            void addPool(LootPool.Builder pool);
            void editPool(Predicate<Item> predicate, LootEntry entry);
            @Deprecated
            void editPool(Predicate<Item> itemPredicate, LootPoolEntryContainer.Builder<?> entry, boolean replace);
        }

        public interface Entry {
            void modify(LootTable table, ResourceKey<net.minecraft.world.level.storage.loot.LootTable> key, HolderLookup.Provider provider);
        }

        public static boolean matches(LootPoolEntryContainer entry, Predicate<Item> itemPredicate) {
            if (entry instanceof LootItem lootItem) {
                return itemPredicate.test(lootItem.item.value());
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

        public static boolean handlePoolReplacements(List<LootPoolEntryContainer> entries, Predicate<Item> itemPredicate, LootPoolEntryContainer builtEntry, LootPool.Builder pool) {
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

        public static boolean handlePoolRemovals(List<LootPoolEntryContainer> entries, Predicate<Item> itemPredicate, LootPool.Builder pool) {
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

        private static Result replaceEntry(LootPoolEntryContainer entry, Predicate<Item> itemPredicate, LootPoolEntryContainer replacement) {
            if (entry instanceof LootItem lootItem) {
                return itemPredicate.test(lootItem.item.value()) ? new Result(replacement, true) : new Result(entry, false);
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

        private static Result removeEntry(LootPoolEntryContainer entry, Predicate<Item> itemPredicate) {
            if (entry instanceof LootItem lootItem) {
                return itemPredicate.test(lootItem.item.value()) ? new Result(null, true) : new Result(entry, false);
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
                return new AlternativesEntry(rewrittenChildren, compositeEntry.conditions);
            }
            if (compositeEntry instanceof EntryGroup) {
                return new EntryGroup(rewrittenChildren, compositeEntry.conditions);
            }
            if (compositeEntry instanceof SequentialEntry) {
                return new SequentialEntry(rewrittenChildren, compositeEntry.conditions);
            }

            LogUtils.getLogger().warn("Unsupported CompositeEntryBase type skipped");
            return compositeEntry;
        }

        private record Result(LootPoolEntryContainer entry, boolean changed) {}
    }

    public static class Items {

        private Items() {}

        public static void passOnUse(EventType type, Level level, Player player, InteractionHand hand) {
            for (TriConsumer<Level, Player, InteractionHand> listener : UnifiedEvents.Items.USE_LISTENERS.get(type)) {
                listener.accept(level, player, hand);
            }
        }

        public static void passOnUseOn(UseOnContext context) {
            for (Consumer<UseOnContext> listener : UnifiedEvents.Items.USE_ON_LISTENERS) {
                listener.accept(context);
            }
        }
    }

    public static class Blocks {

        private Blocks() {}

        public static void passOnPlace(EventType type, BlockPlaceContext context) {
            for (Consumer<BlockPlaceContext> listener : UnifiedEvents.Blocks.PLACE_LISTENERS.get(type)) {
                listener.accept(context);
            }
        }

        public static void passOnUseOn(UseOnContext context) {
            for (Consumer<UseOnContext> listener : UnifiedEvents.Blocks.USE_ON_LISTENERS) {
                listener.accept(context);
            }
        }
    }

    public static class Entities {

        private Entities() {}

        public static void passOnLoad(Entity entity, ServerLevel level) {
            for (BiConsumer<Entity, ServerLevel> listener : UnifiedEvents.Entities.LOAD_LISTENERS) {
                listener.accept(entity, level);
            }
        }

        public static void passOnUnload(Entity entity, ServerLevel level) {
            for (BiConsumer<Entity, ServerLevel> listener : UnifiedEvents.Entities.UNLOAD_LISTENERS) {
                listener.accept(entity, level);
            }
        }

        public static void passOnTick(EventType type, Entity entity) {
            for (Consumer<Entity> listener : UnifiedEvents.Entities.TICK_LISTENERS.get(type)) {
                listener.accept(entity);
            }
        }

        public static void passOnLivingTick(EventType type, LivingEntity entity) {
            for (Consumer<LivingEntity> listener : UnifiedEvents.Entities.LIVING_TICK_LISTENERS.get(type)) {
                listener.accept(entity);
            }
        }
    }

    public static class Levels {

        private Levels() {}

        public static void passOnLoad(Level level) {
            for (Consumer<Level> listener : UnifiedEvents.Levels.LEVEL_LOADED_LISTENERS) {
                listener.accept(level);
            }
        }

        public static void passOnUnload(Level level) {
            for (Consumer<Level> listener : UnifiedEvents.Levels.LEVEL_UNLOADED_LISTENERS) {
                listener.accept(level);
            }
        }
    }
}
