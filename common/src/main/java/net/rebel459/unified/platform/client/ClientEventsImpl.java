package net.rebel459.unified.platform.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class ClientEventsImpl {

    public static class Screen {

        private Screen() {}

        public static void passAbstractContainer(AbstractContainerScreen screen) {
            for (Consumer<AbstractContainerScreen> listener : UnifiedClientEvents.Screen.ABSTRACT_CONTAINER_LISTENERS) {
                listener.accept(screen);
            }
        }
    }

    public static class Gui {

        private Gui() {}

        public static void passHotbar(net.minecraft.client.gui.Gui gui, GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
            for (UnifiedClientEvents.Gui.Entry entry : UnifiedClientEvents.Gui.HOTBAR_ENTRIES) {
                entry.register(gui, guiGraphics, deltaTracker);
            }
        }

        public static void passCrosshair(net.minecraft.client.gui.Gui gui, GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
            for (UnifiedClientEvents.Gui.Entry entry : UnifiedClientEvents.Gui.CROSSHAIR_ENTRIES) {
                entry.register(gui, guiGraphics, deltaTracker);
            }
        }
    }
}
