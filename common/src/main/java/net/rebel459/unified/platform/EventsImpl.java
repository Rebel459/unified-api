package net.rebel459.unified.platform;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class EventsImpl {

    public static class Players {

        private Players() {}

        public static void passOnRespawn(ServerPlayer oldPlayer, ServerPlayer newPlayer) {
            for (BiConsumer<ServerPlayer, ServerPlayer> listener : UnifiedEvents.Players.RESPAWN_LISTENERS) {
                listener.accept(oldPlayer, newPlayer);
            }
        }
    }

    public static class Items {

        private Items() {}

        public static void passBeforeUse(Level level, Player player, InteractionHand hand) {
            for (TriConsumer<Level, Player, InteractionHand> listener : UnifiedEvents.Items.BEFORE_USE_LISTENERS) {
                listener.accept(level, player, hand);
            }
        }

        public static void passAfterUse(Level level, Player player, InteractionHand hand) {
            for (TriConsumer<Level, Player, InteractionHand> listener : UnifiedEvents.Items.AFTER_USE_LISTENERS) {
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

        public static void passBeforePlace(BlockPlaceContext context) {
            for (Consumer<BlockPlaceContext> listener : UnifiedEvents.Blocks.BEFORE_PLACE_LISTENERS) {
                listener.accept(context);
            }
        }

        public static void passAfterPlace(BlockPlaceContext context) {
            for (Consumer<BlockPlaceContext> listener : UnifiedEvents.Blocks.AFTER_PLACE_LISTENERS) {
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
    }
}
