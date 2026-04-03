package net.rebel459.unified.platform.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;
import net.rebel459.unified.util.EventType;
import net.rebel459.unified.util.event.QuadConsumer;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class UnifiedClientEvents {

    public static class Instance {

        private Instance() {}

        private static final EnumMap<EventType, List<Consumer<Minecraft>>> TICK_LISTENERS = new EnumMap<>(Map.of(
                EventType.PRE, new ArrayList<>(),
                EventType.POST, new ArrayList<>()
        ));

        public static void onTick(EventType type, Consumer<Minecraft> listener) {
            TICK_LISTENERS.get(type).add(listener);
        }

        static void passOnTick(EventType type, Minecraft client) {
            for (Consumer<Minecraft> listener : TICK_LISTENERS.get(type)) {
                listener.accept(client);
            }
        }

        static final List<Consumer<LocalPlayer>> RESPAWN_LISTENERS = new CopyOnWriteArrayList<>();

        public static void onRespawn(Consumer<LocalPlayer> listener) {
            RESPAWN_LISTENERS.add(listener);
        }

        // pass handled in impl
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

        static final List<ClientEventsImpl.Guis.Entry> CROSSHAIR_ENTRIES = new CopyOnWriteArrayList<>();

        public static void renderCrosshair(ClientEventsImpl.Guis.Entry entry) {
            CROSSHAIR_ENTRIES.add(entry);
        }

        // pass handled in impl

        static final List<ClientEventsImpl.Guis.Entry> HOTBAR_ENTRIES = new CopyOnWriteArrayList<>();

        public static void renderHotbar(ClientEventsImpl.Guis.Entry entry) {
            HOTBAR_ENTRIES.add(entry);
        }

        // pass handled in impl
    }

    public static class ItemTooltips {

        private ItemTooltips() {}

        static final EnumMap<EventType, List<Consumer<ClientEventsImpl.ItemTooltips.TooltipContext>>> TOOLTIP_DETAILS = new EnumMap<>(Map.of(
                EventType.PRE, new ArrayList<>(),
                EventType.POST, new ArrayList<>()
        ));

        public static void addDetails(EventType type, Consumer<ClientEventsImpl.ItemTooltips.TooltipContext> listener) {
            TOOLTIP_DETAILS.get(type).add(listener);
        }

        // pass handled in impl

        static final EnumMap<EventType, List<QuadConsumer<ItemStack, Consumer<Component>, TooltipDisplay, LocalPlayer>>> TOOLTIP_ATTRIBUTES = new EnumMap<>(Map.of(
                EventType.PRE, new ArrayList<>(),
                EventType.POST, new ArrayList<>()
        ));

        public static void addAttributes(EventType type, QuadConsumer<ItemStack, Consumer<Component>, TooltipDisplay, LocalPlayer> listener) {
            TOOLTIP_ATTRIBUTES.get(type).add(listener);
        }

        // pass handled in impl

        static final List<Consumer<ClientEventsImpl.ItemTooltips.LineContext>> TOOLTIP_LINES = new CopyOnWriteArrayList<>();

        public static void insertLines(Consumer<ClientEventsImpl.ItemTooltips.LineContext> listener) {
            TOOLTIP_LINES.add(listener);
        }

        // pass handled in impl
    }
}
