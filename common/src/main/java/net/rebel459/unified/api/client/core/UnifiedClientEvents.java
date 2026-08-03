package net.rebel459.unified.api.client.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;
import net.rebel459.unified.api.event.EventTiming;
import net.rebel459.unified.api.util.QuadConsumer;
import net.rebel459.unified.impl.client.core.ClientEventsImpl;

import java.util.function.Consumer;

public class UnifiedClientEvents {

    public static class Instance {

        private Instance() {}

        public static void onStart(Consumer<Minecraft> listener) {
            ClientEventsImpl.Instance.START_LISTENERS.add(listener);
        }

        public static void onStop(Consumer<Minecraft> listener) {
            ClientEventsImpl.Instance.STOP_LISTENERS.add(listener);
        }

        public static void onTick(EventTiming type, Consumer<Minecraft> listener) {
            ClientEventsImpl.Instance.TICK_LISTENERS.get(type).add(listener);
        }

        public static void onRespawn(Consumer<LocalPlayer> listener) {
            ClientEventsImpl.Instance.RESPAWN_LISTENERS.add(listener);
        }

        public static void onLevelLoad(Consumer<ClientLevel> handler) {
            ClientEventsImpl.Instance.LEVEL_LOADED_LISTENERS.add(handler);
        }

        public static void onLevelUnload(Consumer<ClientLevel> handler) {
            ClientEventsImpl.Instance.LEVEL_UNLOADED_LISTENERS.add(handler);
        }
    }

    public static class Screens {

        private Screens() {}

        public static void initAbstractContainerScreen(Consumer<AbstractContainerScreen> listener) {
            ClientEventsImpl.Screens.ABSTRACT_CONTAINER_LISTENERS.add(listener);
        }
    }

    public static class Hud {

        private Hud() {}

        public static void renderCrosshair(ClientEventsImpl.Hud.Entry entry) {
            ClientEventsImpl.Hud.CROSSHAIR_ENTRIES.add(entry);
        }

        public static void renderHotbar(ClientEventsImpl.Hud.Entry entry) {
            ClientEventsImpl.Hud.HOTBAR_ENTRIES.add(entry);
        }
    }

    public static class ItemTooltips {

        private ItemTooltips() {}


        public static void addDetails(EventTiming type, Consumer<ClientEventsImpl.ItemTooltips.TooltipContext> listener) {
            ClientEventsImpl.ItemTooltips.TOOLTIP_DETAILS.get(type).add(listener);
        }

        public static void addAttributes(EventTiming type, QuadConsumer<ItemStack, Consumer<Component>, TooltipDisplay, LocalPlayer> listener) {
            ClientEventsImpl.ItemTooltips.TOOLTIP_ATTRIBUTES.get(type).add(listener);
        }

        public static void afterAttributeAdded(ClientEventsImpl.ItemTooltips.AttributeEntry context) {
            ClientEventsImpl.ItemTooltips.ATTRIBUTE_ENTRIES.add(context);
        }

        public static void afterBaseAttributeAdded(ClientEventsImpl.ItemTooltips.BaseAttributeEntry context) {
            ClientEventsImpl.ItemTooltips.BASE_ATTRIBUTE_ENTRIES.add(context);
        }

        public static void insertLines(Consumer<ClientEventsImpl.ItemTooltips.LineContext> listener) {
            ClientEventsImpl.ItemTooltips.TOOLTIP_LINES.add(listener);
        }
    }
}
