package net.rebel459.unified.platform;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Player;
import net.rebel459.unified.platform.client.UnifiedClientEvents;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class EventsImpl {

    public static class Players {

        private Players() {}

        public static void passOnRespawn(Player oldPlayer, Player newPlayer) {
            for (BiConsumer<Player, Player> listener : UnifiedEvents.Players.RESPAWN_LISTENERS) {
                listener.accept(oldPlayer, newPlayer);
            }
        }
    }
}
