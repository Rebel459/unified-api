package net.rebel459.unified.platform.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import java.util.function.Consumer;

public class ClientEventsImpl {

    public static class Screens {

        private Screens() {}

        public static void passInitAbstractContainerScreen(AbstractContainerScreen screen) {
            for (Consumer<AbstractContainerScreen> listener : UnifiedClientEvents.Screens.ABSTRACT_CONTAINER_LISTENERS) {
                listener.accept(screen);
            }
        }
    }

    public static class Guis {

        private Guis() {}

        public static void passRenderCrosshair(net.minecraft.client.gui.Gui gui, GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
            for (UnifiedClientEvents.Guis.Entry entry : UnifiedClientEvents.Guis.CROSSHAIR_ENTRIES) {
                entry.register(gui, graphics, deltaTracker);
            }
        }

        public static void passRenderHotbar(net.minecraft.client.gui.Gui gui, GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
            for (UnifiedClientEvents.Guis.Entry entry : UnifiedClientEvents.Guis.HOTBAR_ENTRIES) {
                entry.register(gui, graphics, deltaTracker);
            }
        }
    }
}
