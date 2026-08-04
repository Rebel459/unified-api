package net.rebel459.unified.api.core;

import com.mojang.brigadier.CommandDispatcher;
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
import net.rebel459.unified.api.event.EventTiming;
import net.rebel459.unified.api.util.QuadConsumer;
import net.rebel459.unified.impl.core.CommonEvents;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class UnifiedEvents {

    public static class DefaultDataComponents {

        private DefaultDataComponents() {}

        public static void modify(TriConsumer<Item, DataComponentMap.Builder, HolderLookup.Provider> modifier) {
            CommonEvents.DefaultDataComponents.ENTRIES.add(new CommonEvents.DefaultDataComponents.Entry(modifier));
        }

        public static void modifyWithFilter(Predicate<Item> filter, TriConsumer<Item, DataComponentMap.Builder, HolderLookup.Provider> modifier) {
            CommonEvents.DefaultDataComponents.FILTERED_ENTRIES.add(new CommonEvents.DefaultDataComponents.FilteredEntry(filter, modifier));
        }
    }

    public static class Players {

        private Players() {}

        public static void onJoin(Consumer<ServerPlayer> listener) {
            CommonEvents.Players.JOIN_LISTENERS.add(listener);
        }

        public static void onLeave(Consumer<ServerPlayer> listener) {
            CommonEvents.Players.LEAVE_LISTENERS.add(listener);
        }

        public static void onRespawn(BiConsumer<ServerPlayer, ServerPlayer> listener) {
            CommonEvents.Players.RESPAWN_LISTENERS.add(listener);
        }

        public static void onTick(EventTiming type, Consumer<Player> listener) {
            CommonEvents.Players.TICK_LISTENERS.get(type).add(listener);
        }
    }

    public static class Commands {

        private Commands() {}

        public static void register(TriConsumer<CommandDispatcher<CommandSourceStack>, CommandBuildContext, net.minecraft.commands.Commands.CommandSelection> listener) {
            CommonEvents.Commands.ENTRIES.add(listener);
        }
    }

    public static class Server {

        private Server() {}

        public static void onDatapackLoad(Consumer<MinecraftServer> handler) {
            CommonEvents.Server.DATAPACK_RELOAD_ENTRIES.add(handler);
        }

        public static void onStart(Consumer<MinecraftServer> handler) {
            CommonEvents.Server.SERVER_STARTED_LISTENERS.add(handler);
        }

        public static void onStop(Consumer<MinecraftServer> handler) {
            CommonEvents.Server.SERVER_STOPPED_LISTENERS.add(handler);
        }

        public static void onTick(EventTiming type, Consumer<MinecraftServer> listener) {
            CommonEvents.Server.TICK_LISTENERS.get(type).add(listener);
        }

        public static void onLevelTick(EventTiming type, Consumer<ServerLevel> listener) {
            CommonEvents.Server.LEVEL_TICK_LISTENERS.get(type).add(listener);
        }

        public static void onLevelLoad(Consumer<ServerLevel> handler) {
            CommonEvents.Server.LEVEL_LOADED_LISTENERS.add(handler);
        }

        public static void onLevelUnload(Consumer<ServerLevel> handler) {
            CommonEvents.Server.LEVEL_UNLOADED_LISTENERS.add(handler);
        }
    }

    public static class LootTables {

        private LootTables() {}

        public static void modify(CommonEvents.LootTables.Entry handler) {
            CommonEvents.LootTables.ENTRIES.add(handler);
        }

        public static void modifyWithFilter(Predicate<ResourceKey<net.minecraft.world.level.storage.loot.LootTable>> filter, CommonEvents.LootTables.Entry handler) {
            CommonEvents.LootTables.FILTERED_ENTRIES.add(new CommonEvents.LootTables.FilteredEntry(filter, handler));
        }
    }

    public static class Items {

        private Items() {}

        public static void onUse(EventTiming type, TriConsumer<Level, Player, InteractionHand> listener) {
            CommonEvents.Items.USE_LISTENERS.get(type).add(listener);
        }

        public static void onUseOn(Consumer<UseOnContext> listener) {
            CommonEvents.Items.USE_ON_LISTENERS.add(listener);
        }
    }

    public static class Blocks {

        private Blocks() {}

        public static void onPlace(EventTiming type, Consumer<BlockPlaceContext> listener) {
            CommonEvents.Blocks.PLACE_LISTENERS.get(type).add(listener);
        }

        public static void onUseOn(Consumer<UseOnContext> listener) {
            CommonEvents.Blocks.USE_ON_LISTENERS.add(listener);
        }
    }

    public static class Entities {

        private Entities() {}

        public static void onDeath(BiConsumer<LivingEntity, DamageSource> listener) {
            CommonEvents.Entities.DEATH_LISTENERS.add(listener);
        }

        public static void onEquipmentChange(QuadConsumer<LivingEntity, EquipmentSlot, ItemStack, ItemStack> listener) {
            CommonEvents.Entities.EQUIPMENT_CHANGE_LISTENERS.add(listener);
        }

        public static void onLoad(BiConsumer<Entity, ServerLevel> listener) {
            CommonEvents.Entities.LOAD_LISTENERS.add(listener);
        }

        public static void onUnload(BiConsumer<Entity, ServerLevel> listener) {
            CommonEvents.Entities.UNLOAD_LISTENERS.add(listener);
        }

        public static void onTick(EventTiming type, Consumer<Entity> listener) {
            CommonEvents.Entities.TICK_LISTENERS.get(type).add(listener);
        }

        public static void onLivingTick(EventTiming type, Consumer<LivingEntity> listener) {
            CommonEvents.Entities.LIVING_TICK_LISTENERS.get(type).add(listener);
        }
    }

    public static class Levels {

        private Levels() {}

        public static void onLoad(Consumer<Level> handler) {
            CommonEvents.Levels.LEVEL_LOADED_LISTENERS.add(handler);
        }

        public static void onUnload(Consumer<Level> handler) {
            CommonEvents.Levels.LEVEL_UNLOADED_LISTENERS.add(handler);
        }

        public static void onTick(EventTiming type, Consumer<Level> listener) {
            CommonEvents.Levels.LEVEL_TICK_LISTENERS.get(type).add(listener);
        }
    }
}
