package net.rebel459.unified.platform.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class UnifiedClientEvents {

    public static class Tick {

        private Tick() {}

        private static final List<Consumer<Minecraft>> START_LISTENERS = new CopyOnWriteArrayList<>();

        public static void accessStart(Consumer<Minecraft> listener) {
            START_LISTENERS.add(listener);
        }

        static void passStart(Minecraft client) {
            for (Consumer<Minecraft> listener : START_LISTENERS) {
                listener.accept(client);
            }
        }

        private static final List<Consumer<Minecraft>> END_LISTENERS = new CopyOnWriteArrayList<>();

        public static void accessEnd(Consumer<Minecraft> listener) {
            END_LISTENERS.add(listener);
        }

        static void passEnd(Minecraft client) {
            for (Consumer<Minecraft> listener : END_LISTENERS) {
                listener.accept(client);
            }
        }
    }

    public static class Screen {

        private Screen() {}

        static final List<Consumer<AbstractContainerScreen>> ABSTRACT_CONTAINER_LISTENERS = new CopyOnWriteArrayList<>();

        public static void accessAbstractContainer(Consumer<AbstractContainerScreen> listener) {
            ABSTRACT_CONTAINER_LISTENERS.add(listener);
        }

        // pass handled in impl
    }

    public static class Gui {

        private Gui() {}

        public interface Entry {
            void register(net.minecraft.client.gui.Gui gui, GuiGraphics guiGraphics, DeltaTracker deltaTracker);
        }

        static final List<Entry> CROSSHAIR_ENTRIES = new CopyOnWriteArrayList<>();

        public static void accessCrosshair(Entry entry) {
            CROSSHAIR_ENTRIES.add(entry);
        }

        // pass handled in impl

        static final List<Entry> HOTBAR_ENTRIES = new CopyOnWriteArrayList<>();

        public static void accessHotbar(Entry entry) {
            HOTBAR_ENTRIES.add(entry);
        }

        // pass handled in impl
    }
}
