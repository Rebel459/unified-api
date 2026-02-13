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

    public static class EndTick {

        private static final List<Consumer<Minecraft>> LISTENERS = new CopyOnWriteArrayList<>();

        private EndTick() {}

        public static void access(Consumer<Minecraft> listener) {
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

        public static void access(Consumer<Minecraft> listener) {
            LISTENERS.add(listener);
        }

        public static void pass(Minecraft client) {
            for (Consumer<Minecraft> listener : LISTENERS) {
                listener.accept(client);
            }
        }
    }

    public static class AbstractScreen {

        private static final List<Consumer<AbstractContainerScreen>> LISTENERS = new CopyOnWriteArrayList<>();

        private AbstractScreen() {}

        public static void access(Consumer<AbstractContainerScreen> listener) {
            LISTENERS.add(listener);
        }

        public static void pass(AbstractContainerScreen screen) {
            for (Consumer<AbstractContainerScreen> listener : LISTENERS) {
                listener.accept(screen);
            }
        }
    }

    public static class HotbarGui {

        public interface Entry {
            void register(Gui gui, GuiGraphics guiGraphics, DeltaTracker deltaTracker);
        }

        private static final List<Entry> ENTRIES = new CopyOnWriteArrayList<>();

        private HotbarGui() {}

        public static void access(Entry entry) {
            ENTRIES.add(entry);
        }

        public static void pass(Gui gui, GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
            for (Entry entry : ENTRIES) {
                entry.register(gui, guiGraphics, deltaTracker);
            }
        }
    }
}
