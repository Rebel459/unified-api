package net.rebel459.unified.impl.client.core;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.rebel459.unified.api.event.EventTiming;
import net.rebel459.unified.api.util.QuadConsumer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class CommonClientEvents {

    public static class Instance {

        private Instance() {}

        public static final List<Consumer<Minecraft>> START_LISTENERS = new CopyOnWriteArrayList<>();

        public static void passOnStart(Minecraft client) {
            for (Consumer<Minecraft> listener : START_LISTENERS) {
                listener.accept(client);
            }
        }

        public static final List<Consumer<Minecraft>> STOP_LISTENERS = new CopyOnWriteArrayList<>();

        public static void passOnStop(Minecraft client) {
            for (Consumer<Minecraft> listener : STOP_LISTENERS) {
                listener.accept(client);
            }
        }

        public static final EnumMap<EventTiming, List<Consumer<Minecraft>>> TICK_LISTENERS = new EnumMap<>(Map.of(
                EventTiming.PRE, new ArrayList<>(),
                EventTiming.POST, new ArrayList<>()
        ));

        public static void passOnTick(EventTiming type, Minecraft client) {
            for (Consumer<Minecraft> listener : TICK_LISTENERS.get(type)) {
                listener.accept(client);
            }
        }

        public static final List<Consumer<LocalPlayer>> RESPAWN_LISTENERS = new CopyOnWriteArrayList<>();

        public static void passOnRespawn(LocalPlayer player) {
            for (Consumer<LocalPlayer> listener : RESPAWN_LISTENERS) {
                listener.accept(player);
            }
        }

        public static final List<Consumer<ClientLevel>> LEVEL_LOADED_LISTENERS = new CopyOnWriteArrayList<>();

        public static void passOnLevelLoad(ClientLevel level) {
            for (Consumer<ClientLevel> listener : LEVEL_LOADED_LISTENERS) {
                listener.accept(level);
            }
        }

        public static final List<Consumer<ClientLevel>> LEVEL_UNLOADED_LISTENERS = new CopyOnWriteArrayList<>();

        public static void passOnLevelUnload(ClientLevel level) {
            for (Consumer<ClientLevel> listener : LEVEL_UNLOADED_LISTENERS) {
                listener.accept(level);
            }
        }
    }

    public static class Screens {

        private Screens() {}

        public static final List<Consumer<AbstractContainerScreen>> ABSTRACT_CONTAINER_LISTENERS = new CopyOnWriteArrayList<>();

        public static void passInitAbstractContainerScreen(AbstractContainerScreen screen) {
            for (Consumer<AbstractContainerScreen> listener : ABSTRACT_CONTAINER_LISTENERS) {
                listener.accept(screen);
            }
        }
    }

    public static class Hud {

        private Hud() {}

        public interface Entry {
            void register(net.minecraft.client.gui.Gui gui, GuiGraphicsExtractor graphics, DeltaTracker deltaTracker);
        }

        public static final List<CommonClientEvents.Hud.Entry> CROSSHAIR_ENTRIES = new CopyOnWriteArrayList<>();

        public static void passRenderCrosshair(net.minecraft.client.gui.Gui gui, GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
            for (Hud.Entry entry : CROSSHAIR_ENTRIES) {
                entry.register(gui, graphics, deltaTracker);
            }
        }
        public static final List<CommonClientEvents.Hud.Entry> HOTBAR_ENTRIES = new CopyOnWriteArrayList<>();

        public static void passRenderHotbar(net.minecraft.client.gui.Gui gui, GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
            for (Hud.Entry entry : HOTBAR_ENTRIES) {
                entry.register(gui, graphics, deltaTracker);
            }
        }
    }

    public static class ItemTooltips {

        private ItemTooltips() {}

        public record TooltipContext(ItemStack stack, Item.TooltipContext tooltip, TooltipDisplay display, LocalPlayer player, TooltipFlag flag, Consumer<Component> builder) {}

        public record LineContext(ItemStack stack, Item.TooltipContext tooltip, LocalPlayer player, TooltipFlag flag, List<Component> components) {}

        public static final EnumMap<EventTiming, List<Consumer<CommonClientEvents.ItemTooltips.TooltipContext>>> TOOLTIP_DETAILS = new EnumMap<>(Map.of(
                EventTiming.PRE, new ArrayList<>(),
                EventTiming.POST, new ArrayList<>()
        ));

        public static void passAddDetails(EventTiming type, CommonClientEvents.ItemTooltips.TooltipContext client) {
            for (Consumer<TooltipContext> listener : TOOLTIP_DETAILS.get(type)) {
                listener.accept(client);
            }
        }

        public static final EnumMap<EventTiming, List<QuadConsumer<ItemStack, Consumer<Component>, TooltipDisplay, LocalPlayer>>> TOOLTIP_ATTRIBUTES = new EnumMap<>(Map.of(
                EventTiming.PRE, new ArrayList<>(),
                EventTiming.POST, new ArrayList<>()
        ));

        public static void passAddAttributes(EventTiming type, ItemStack stack, Consumer<Component> builder, TooltipDisplay display, LocalPlayer player) {
            for (QuadConsumer<ItemStack, Consumer<Component>, TooltipDisplay, LocalPlayer> listener : TOOLTIP_ATTRIBUTES.get(type)) {
                listener.accept(stack, builder, display, player);
            }
        }

        public interface AttributeEntry {
            void register(Consumer<Component> builder, ItemStack stack, ItemAttributeModifiers itemModifiers, @Nullable Player player, Holder<Attribute> attribute, AttributeModifier modifier);
        }

        public static final List<CommonClientEvents.ItemTooltips.AttributeEntry> ATTRIBUTE_ENTRIES = new CopyOnWriteArrayList<>();

        public static void passAfterAttributeAdded(Consumer<Component> builder, ItemStack stack, ItemAttributeModifiers itemModifiers, @Nullable Player player, Holder<Attribute> attribute, AttributeModifier modifier) {
            for (AttributeEntry entry : ATTRIBUTE_ENTRIES) {
                entry.register(builder, stack, itemModifiers, player, attribute, modifier);
            }
        }

        public interface BaseAttributeEntry {
            void register(Consumer<Component> builder, ItemStack stack, ItemAttributeModifiers itemModifiers, @Nullable Player player, Holder<Attribute> attribute, double displayValue);
        }

        public static final List<CommonClientEvents.ItemTooltips.BaseAttributeEntry> BASE_ATTRIBUTE_ENTRIES = new CopyOnWriteArrayList<>();

        public static void passAfterBaseAttributeAdded(Consumer<Component> builder, ItemStack stack, ItemAttributeModifiers itemModifiers, @Nullable Player player, Holder<Attribute> attribute, double displayValue) {
            for (BaseAttributeEntry entry : BASE_ATTRIBUTE_ENTRIES) {
                entry.register(builder, stack, itemModifiers, player, attribute, displayValue);
            }
        }

        public static final List<Consumer<CommonClientEvents.ItemTooltips.LineContext>> TOOLTIP_LINES = new CopyOnWriteArrayList<>();

        public static void passInsertLines(LineContext context) {
            for (Consumer<LineContext> listener : TOOLTIP_LINES) {
                listener.accept(context);
            }
        }
    }
}
