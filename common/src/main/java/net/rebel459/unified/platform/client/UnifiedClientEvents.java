package net.rebel459.unified.platform.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class UnifiedClientEvents {

    public static class EndTick {

        private static final List<Consumer<Minecraft>> LISTENERS = new CopyOnWriteArrayList<>();

        private EndTick() {}

        public static void insert(Consumer<Minecraft> listener) {
            LISTENERS.add(listener);
        }

        public static void pass(Minecraft client) {
            for (Consumer<Minecraft> listener : LISTENERS) {
                listener.accept(client);
            }
        }
    }

    public static class StartTick {

        private static final List<Consumer<Minecraft>> LISTENERS = new CopyOnWriteArrayList<>();

        private StartTick() {}

        public static void insert(Consumer<Minecraft> listener) {
            LISTENERS.add(listener);
        }

        public static void pass(Minecraft client) {
            for (Consumer<Minecraft> listener : LISTENERS) {
                listener.accept(client);
            }
        }
    }
}
