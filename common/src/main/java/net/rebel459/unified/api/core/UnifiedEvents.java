package net.rebel459.unified.api.core;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
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
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.rebel459.unified.api.event.EventTiming;
import net.rebel459.unified.api.event.LootEntry;
import net.rebel459.unified.api.util.QuadConsumer;
import net.rebel459.unified.impl.core.EventsImpl;
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

public class UnifiedEvents {

    public static class DefaultDataComponents {

        private DefaultDataComponents() {}

        public static void modify(TriConsumer<Item, DataComponentMap.Builder, HolderLookup.Provider> modifier) {
            EventsImpl.DefaultDataComponents.ENTRIES.add(new EventsImpl.DefaultDataComponents.Entry(modifier));
        }

        public static void modifyWithFilter(Predicate<Item> filter, TriConsumer<Item, DataComponentMap.Builder, HolderLookup.Provider> modifier) {
            EventsImpl.DefaultDataComponents.FILTERED_ENTRIES.add(new EventsImpl.DefaultDataComponents.FilteredEntry(filter, modifier));
        }
    }

    public static class Players {

        private Players() {}

        public static void onJoin(Consumer<ServerPlayer> listener) {
            EventsImpl.Players.JOIN_LISTENERS.add(listener);
        }

        public static void onLeave(Consumer<ServerPlayer> listener) {
            EventsImpl.Players.LEAVE_LISTENERS.add(listener);
        }

        public static void onRespawn(BiConsumer<ServerPlayer, ServerPlayer> listener) {
            EventsImpl.Players.RESPAWN_LISTENERS.add(listener);
        }

        public static void onTick(EventTiming type, Consumer<Player> listener) {
            EventsImpl.Players.TICK_LISTENERS.get(type).add(listener);
        }
    }

    public static class Commands {

        private Commands() {}

        public static void register(TriConsumer<CommandDispatcher<CommandSourceStack>, CommandBuildContext, net.minecraft.commands.Commands.CommandSelection> listener) {
            EventsImpl.Commands.ENTRIES.add(listener);
        }
    }

    public static class Server {

        private Server() {}

        public static void onDatapackLoad(Consumer<MinecraftServer> handler) {
            EventsImpl.Server.DATAPACK_RELOAD_ENTRIES.add(handler);
        }

        public static void onStart(Consumer<MinecraftServer> handler) {
            EventsImpl.Server.SERVER_STARTED_LISTENERS.add(handler);
        }

        public static void onStop(Consumer<MinecraftServer> handler) {
            EventsImpl.Server.SERVER_STOPPED_LISTENERS.add(handler);
        }

        public static void onTick(EventTiming type, Consumer<MinecraftServer> listener) {
            EventsImpl.Server.TICK_LISTENERS.get(type).add(listener);
        }

        public static void onLevelTick(EventTiming type, Consumer<ServerLevel> listener) {
            EventsImpl.Server.LEVEL_TICK_LISTENERS.get(type).add(listener);
        }

        public static void onLevelLoad(Consumer<ServerLevel> handler) {
            EventsImpl.Server.LEVEL_LOADED_LISTENERS.add(handler);
        }

        public static void onLevelUnload(Consumer<ServerLevel> handler) {
            EventsImpl.Server.LEVEL_UNLOADED_LISTENERS.add(handler);
        }
    }

    public static class LootTables {

        private LootTables() {}

        public static void modify(EventsImpl.LootTables.Entry handler) {
            EventsImpl.LootTables.ENTRIES.add(handler);
        }

        public static void modifyWithFilter(Predicate<ResourceKey<net.minecraft.world.level.storage.loot.LootTable>> filter, EventsImpl.LootTables.Entry handler) {
            EventsImpl.LootTables.FILTERED_ENTRIES.add(new EventsImpl.LootTables.FilteredEntry(filter, handler));
        }
    }

    public static class Items {

        private Items() {}

        public static void onUse(EventTiming type, TriConsumer<Level, Player, InteractionHand> listener) {
            EventsImpl.Items.USE_LISTENERS.get(type).add(listener);
        }

        public static void onUseOn(Consumer<UseOnContext> listener) {
            EventsImpl.Items.USE_ON_LISTENERS.add(listener);
        }
    }

    public static class Blocks {

        private Blocks() {}

        public static void onPlace(EventTiming type, Consumer<BlockPlaceContext> listener) {
            EventsImpl.Blocks.PLACE_LISTENERS.get(type).add(listener);
        }

        public static void onUseOn(Consumer<UseOnContext> listener) {
            EventsImpl.Blocks.USE_ON_LISTENERS.add(listener);
        }
    }

    public static class Entities {

        private Entities() {}

        public static void onDeath(BiConsumer<LivingEntity, DamageSource> listener) {
            EventsImpl.Entities.DEATH_LISTENERS.add(listener);
        }

        public static void onEquipmentChange(QuadConsumer<LivingEntity, EquipmentSlot, ItemStack, ItemStack> listener) {
            EventsImpl.Entities.EQUIPMENT_CHANGE_LISTENERS.add(listener);
        }

        public static void onLoad(BiConsumer<Entity, ServerLevel> listener) {
            EventsImpl.Entities.LOAD_LISTENERS.add(listener);
        }

        public static void onUnload(BiConsumer<Entity, ServerLevel> listener) {
            EventsImpl.Entities.UNLOAD_LISTENERS.add(listener);
        }

        public static void onTick(EventTiming type, Consumer<Entity> listener) {
            EventsImpl.Entities.TICK_LISTENERS.get(type).add(listener);
        }

        public static void onLivingTick(EventTiming type, Consumer<LivingEntity> listener) {
            EventsImpl.Entities.LIVING_TICK_LISTENERS.get(type).add(listener);
        }

        // pass handled in impl
    }

    public static class Levels {

        private Levels() {}

        public static void onLoad(Consumer<Level> handler) {
            EventsImpl.Levels.LEVEL_LOADED_LISTENERS.add(handler);
        }

        public static void onUnload(Consumer<Level> handler) {
            EventsImpl.Levels.LEVEL_UNLOADED_LISTENERS.add(handler);
        }

        public static void onTick(EventTiming type, Consumer<Level> listener) {
            EventsImpl.Levels.LEVEL_TICK_LISTENERS.get(type).add(listener);
        }
    }
}
