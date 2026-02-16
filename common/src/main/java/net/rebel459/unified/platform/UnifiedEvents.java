package net.rebel459.unified.platform;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class UnifiedEvents {

    public static class ItemComponents {

        private ItemComponents() {}

        private static final List<Entry> ENTRIES = new CopyOnWriteArrayList<>();

        public static void modify(Predicate<Item> filter, BiConsumer<Builder, Item> modifier) {
            ENTRIES.add(new Entry(filter, modifier));
        }

        static void passModify(Item item, Builder builder) {
            for (Entry entry : ENTRIES) {
                if (entry.filter.test(item)) {
                    entry.modifier.accept(builder, item);
                }
            }
        }

        private record Entry(Predicate<Item> filter, BiConsumer<Builder, Item> modifier) {}

        public interface Builder {
            <T> void set(DataComponentType<? super T> type, T value);
        }
    }

    public static class Players {

        private Players() {}

        private static final List<Consumer<Player>> JOIN_LISTENERS = new CopyOnWriteArrayList<>();

        public static void onJoin(Consumer<Player> listener) {
            JOIN_LISTENERS.add(listener);
        }

        static void passOnJoin(Player player) {
            for (Consumer<Player> listener : JOIN_LISTENERS) {
                listener.accept(player);
            }
        }

        private static final List<Consumer<Player>> LEAVE_LISTENERS = new CopyOnWriteArrayList<>();

        public static void onLeave(Consumer<Player> listener) {
            LEAVE_LISTENERS.add(listener);
        }

        static void passOnLeave(Player player) {
            for (Consumer<Player> listener : LEAVE_LISTENERS) {
                listener.accept(player);
            }
        }

        private static final List<Consumer<Player>> RESPAWN_LISTENERS = new CopyOnWriteArrayList<>();

        public static void onRespawn(Consumer<Player> listener) {
            RESPAWN_LISTENERS.add(listener);
        }

        static void passOnRespawn(Player player) {
            for (Consumer<Player> listener : RESPAWN_LISTENERS) {
                listener.accept(player);
            }
        }
    }

    public static class Commands {

        private Commands() {}

        public interface Entry {
            void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext, net.minecraft.commands.Commands.CommandSelection selection);
        }

        private static final List<Entry> ENTRIES = new CopyOnWriteArrayList<>();

        public static void register(Entry handler) {
            ENTRIES.add(handler);
        }

        static void passRegister(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext, net.minecraft.commands.Commands.CommandSelection selection) {
            for (Entry entry : ENTRIES) {
                entry.register(dispatcher, buildContext, selection);
            }
        }
    }
}
