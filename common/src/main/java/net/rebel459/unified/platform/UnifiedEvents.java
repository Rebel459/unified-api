package net.rebel459.unified.platform;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class UnifiedEvents {

    public static class ModifyItemComponents {

        private static final List<Entry> ENTRIES = new CopyOnWriteArrayList<>();

        private ModifyItemComponents() {}

        public static void access(Predicate<Item> filter, BiConsumer<Builder, Item> modifier) {
            ENTRIES.add(new Entry(filter, modifier));
        }

        public static void pass(Item item, Builder builder) {
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

    public static class PlayerJoin {

        private static final List<Consumer<Player>> LISTENERS = new CopyOnWriteArrayList<>();

        private PlayerJoin() {}

        public static void access(Consumer<Player> listener) {
            LISTENERS.add(listener);
        }

        public static void pass(Player player) {
            for (Consumer<Player> listener : LISTENERS) {
                listener.accept(player);
            }
        }
    }

    public static class PlayerLeave {

        private static final List<Consumer<Player>> LISTENERS = new CopyOnWriteArrayList<>();

        private PlayerLeave() {}

        public static void access(Consumer<Player> listener) {
            LISTENERS.add(listener);
        }

        public static void pass(Player player) {
            for (Consumer<Player> listener : LISTENERS) {
                listener.accept(player);
            }
        }
    }

    public static class PlayerRespawn {

        private static final List<Consumer<Player>> LISTENERS = new CopyOnWriteArrayList<>();

        private PlayerRespawn() {}

        public static void access(Consumer<Player> listener) {
            LISTENERS.add(listener);
        }

        public static void pass(Player player) {
            for (Consumer<Player> listener : LISTENERS) {
                listener.accept(player);
            }
        }
    }

    public static class CommandRegistration {

        public interface Entry {
            void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext, Commands.CommandSelection environment);
        }

        private static final List<Entry> ENTRIES = new CopyOnWriteArrayList<>();

        private CommandRegistration() {}

        public static void access(Entry handler) {
            ENTRIES.add(handler);
        }

        public static void pass(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext, Commands.CommandSelection environment) {
            for (Entry entry : ENTRIES) {
                entry.register(dispatcher, buildContext, environment);
            }
        }
    }
}
