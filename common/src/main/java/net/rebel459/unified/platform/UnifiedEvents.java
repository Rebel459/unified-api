package net.rebel459.unified.platform;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import org.apache.logging.log4j.util.InternalApi;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class UnifiedEvents {

    public static class ModifyItemComponents {

        private ModifyItemComponents() {}

        private static final List<Entry> ENTRIES = new CopyOnWriteArrayList<>();

        public static void access(Predicate<Item> filter, BiConsumer<Builder, Item> modifier) {
            ENTRIES.add(new Entry(filter, modifier));
        }

        static void pass(Item item, Builder builder) {
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

    public static class Player {

        private Player() {}

        private static final List<Consumer<net.minecraft.world.entity.player.Player>> JOIN_LISTENERS = new CopyOnWriteArrayList<>();

        public static void accessJoin(Consumer<net.minecraft.world.entity.player.Player> listener) {
            JOIN_LISTENERS.add(listener);
        }

        static void passJoin(net.minecraft.world.entity.player.Player player) {
            for (Consumer<net.minecraft.world.entity.player.Player> listener : JOIN_LISTENERS) {
                listener.accept(player);
            }
        }

        private static final List<Consumer<net.minecraft.world.entity.player.Player>> LEAVE_LISTENERS = new CopyOnWriteArrayList<>();

        public static void accessLeave(Consumer<net.minecraft.world.entity.player.Player> listener) {
            LEAVE_LISTENERS.add(listener);
        }

        static void passLeave(net.minecraft.world.entity.player.Player player) {
            for (Consumer<net.minecraft.world.entity.player.Player> listener : LEAVE_LISTENERS) {
                listener.accept(player);
            }
        }

        private static final List<Consumer<net.minecraft.world.entity.player.Player>> RESPAWN_LISTENERS = new CopyOnWriteArrayList<>();

        public static void accessRespawn(Consumer<net.minecraft.world.entity.player.Player> listener) {
            RESPAWN_LISTENERS.add(listener);
        }

        static void passRespawn(net.minecraft.world.entity.player.Player player) {
            for (Consumer<net.minecraft.world.entity.player.Player> listener : RESPAWN_LISTENERS) {
                listener.accept(player);
            }
        }
    }

    public static class CommandRegistration {

        private CommandRegistration() {}

        public interface Entry {
            void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext, Commands.CommandSelection selection);
        }

        private static final List<Entry> ENTRIES = new CopyOnWriteArrayList<>();

        public static void access(Entry handler) {
            ENTRIES.add(handler);
        }

        static void pass(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext, Commands.CommandSelection selection) {
            for (Entry entry : ENTRIES) {
                entry.register(dispatcher, buildContext, selection);
            }
        }
    }
}
