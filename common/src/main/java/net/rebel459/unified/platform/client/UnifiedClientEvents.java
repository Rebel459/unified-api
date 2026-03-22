package net.rebel459.unified.platform.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class UnifiedClientEvents {

    public static class Ticks {

        private Ticks() {}

        private static final List<Consumer<Minecraft>> START_LISTENERS = new CopyOnWriteArrayList<>();

        public static void atStart(Consumer<Minecraft> listener) {
            START_LISTENERS.add(listener);
        }

        static void passAtStart(Minecraft client) {
            for (Consumer<Minecraft> listener : START_LISTENERS) {
                listener.accept(client);
            }
        }

        private static final List<Consumer<Minecraft>> END_LISTENERS = new CopyOnWriteArrayList<>();

        public static void atEnd(Consumer<Minecraft> listener) {
            END_LISTENERS.add(listener);
        }

        static void passAtEnd(Minecraft client) {
            for (Consumer<Minecraft> listener : END_LISTENERS) {
                listener.accept(client);
            }
        }
    }

    public static class Screens {

        private Screens() {}

        static final List<Consumer<AbstractContainerScreen>> ABSTRACT_CONTAINER_LISTENERS = new CopyOnWriteArrayList<>();

        public static void initAbstractContainerScreen(Consumer<AbstractContainerScreen> listener) {
            ABSTRACT_CONTAINER_LISTENERS.add(listener);
        }

        // pass handled in impl
    }

    public static class Guis {

        private Guis() {}

        public interface Entry {
            void register(net.minecraft.client.gui.Gui gui, GuiGraphicsExtractor graphics, DeltaTracker deltaTracker);
        }

        static final List<Entry> CROSSHAIR_ENTRIES = new CopyOnWriteArrayList<>();

        public static void renderCrosshair(Entry entry) {
            CROSSHAIR_ENTRIES.add(entry);
        }

        // pass handled in impl

        static final List<Entry> HOTBAR_ENTRIES = new CopyOnWriteArrayList<>();

        public static void renderHotbar(Entry entry) {
            HOTBAR_ENTRIES.add(entry);
        }

        // pass handled in impl
    }
}
