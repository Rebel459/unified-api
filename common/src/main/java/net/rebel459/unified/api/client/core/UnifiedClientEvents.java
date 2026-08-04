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
import net.rebel459.unified.impl.client.core.CommonClientEvents;

import java.util.function.Consumer;

public class UnifiedClientEvents {

    public static class Instance {

        private Instance() {}

        public static void onStart(Consumer<Minecraft> listener) {
            CommonClientEvents.Instance.START_LISTENERS.add(listener);
        }

        public static void onStop(Consumer<Minecraft> listener) {
            CommonClientEvents.Instance.STOP_LISTENERS.add(listener);
        }

        public static void onTick(EventTiming type, Consumer<Minecraft> listener) {
            CommonClientEvents.Instance.TICK_LISTENERS.get(type).add(listener);
        }

        public static void onRespawn(Consumer<LocalPlayer> listener) {
            CommonClientEvents.Instance.RESPAWN_LISTENERS.add(listener);
        }

        public static void onLevelLoad(Consumer<ClientLevel> handler) {
            CommonClientEvents.Instance.LEVEL_LOADED_LISTENERS.add(handler);
        }

        public static void onLevelUnload(Consumer<ClientLevel> handler) {
            CommonClientEvents.Instance.LEVEL_UNLOADED_LISTENERS.add(handler);
        }
    }

    public static class Screens {

        private Screens() {}

        public static void initAbstractContainerScreen(Consumer<AbstractContainerScreen> listener) {
            CommonClientEvents.Screens.ABSTRACT_CONTAINER_LISTENERS.add(listener);
        }
    }

    public static class Hud {

        private Hud() {}

        public static void renderCrosshair(CommonClientEvents.Hud.Entry entry) {
            CommonClientEvents.Hud.CROSSHAIR_ENTRIES.add(entry);
        }

        public static void renderHotbar(CommonClientEvents.Hud.Entry entry) {
            CommonClientEvents.Hud.HOTBAR_ENTRIES.add(entry);
        }
    }

    public static class ItemTooltips {

        private ItemTooltips() {}


        public static void addDetails(EventTiming type, Consumer<CommonClientEvents.ItemTooltips.TooltipContext> listener) {
            CommonClientEvents.ItemTooltips.TOOLTIP_DETAILS.get(type).add(listener);
        }

        public static void addAttributes(EventTiming type, QuadConsumer<ItemStack, Consumer<Component>, TooltipDisplay, LocalPlayer> listener) {
            CommonClientEvents.ItemTooltips.TOOLTIP_ATTRIBUTES.get(type).add(listener);
        }

        public static void afterAttributeAdded(CommonClientEvents.ItemTooltips.AttributeEntry context) {
            CommonClientEvents.ItemTooltips.ATTRIBUTE_ENTRIES.add(context);
        }

        public static void afterBaseAttributeAdded(CommonClientEvents.ItemTooltips.BaseAttributeEntry context) {
            CommonClientEvents.ItemTooltips.BASE_ATTRIBUTE_ENTRIES.add(context);
        }

        public static void insertLines(Consumer<CommonClientEvents.ItemTooltips.LineContext> listener) {
            CommonClientEvents.ItemTooltips.TOOLTIP_LINES.add(listener);
        }
    }
}
