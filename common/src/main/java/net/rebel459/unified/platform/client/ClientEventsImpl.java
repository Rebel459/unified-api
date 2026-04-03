package net.rebel459.unified.platform.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.rebel459.unified.util.EventType;
import net.rebel459.unified.util.event.QuadConsumer;

import java.util.List;
import java.util.function.Consumer;

import static net.rebel459.unified.platform.client.UnifiedClientEvents.Instance.RESPAWN_LISTENERS;

public class ClientEventsImpl {

    public static class Instance {

        private Instance() {}

        public static void passOnRespawn(LocalPlayer player) {
            for (Consumer<LocalPlayer> listener : RESPAWN_LISTENERS) {
                listener.accept(player);
            }
        }
    }

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

        public interface Entry {
            void register(net.minecraft.client.gui.Gui gui, GuiGraphicsExtractor graphics, DeltaTracker deltaTracker);
        }

        public static void passRenderCrosshair(net.minecraft.client.gui.Gui gui, GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
            for (Entry entry : UnifiedClientEvents.Guis.CROSSHAIR_ENTRIES) {
                entry.register(gui, graphics, deltaTracker);
            }
        }

        public static void passRenderHotbar(net.minecraft.client.gui.Gui gui, GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
            for (Entry entry : UnifiedClientEvents.Guis.HOTBAR_ENTRIES) {
                entry.register(gui, graphics, deltaTracker);
            }
        }
    }

    public static class ItemTooltips {

        private ItemTooltips() {}

        public record TooltipContext(ItemStack stack, Item.TooltipContext tooltip, TooltipDisplay display, LocalPlayer player, TooltipFlag flag, Consumer<Component> consumer) {}

        public record LineContext(ItemStack stack, Item.TooltipContext tooltip, LocalPlayer player, TooltipFlag flag, List<Component> components) {}

        public static void passAddDetails(EventType type, ClientEventsImpl.ItemTooltips.TooltipContext client) {
            for (Consumer<TooltipContext> listener : UnifiedClientEvents.ItemTooltips.TOOLTIP_DETAILS.get(type)) {
                listener.accept(client);
            }
        }

        public static void passAddAttributes(EventType type, ItemStack stack, Consumer<Component> consumer, TooltipDisplay display, LocalPlayer player) {
            for (QuadConsumer<ItemStack, Consumer<Component>, TooltipDisplay, LocalPlayer> listener : UnifiedClientEvents.ItemTooltips.TOOLTIP_ATTRIBUTES.get(type)) {
                listener.accept(stack, consumer, display, player);
            }
        }

        public static void passInsertLines(LineContext context) {
            for (Consumer<LineContext> listener : UnifiedClientEvents.ItemTooltips.TOOLTIP_LINES) {
                listener.accept(context);
            }
        }
    }
}
