package net.rebel459.unified.util.neoforge;

import net.minecraft.core.HolderLookup;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class BiomeBuilderEvent {

        private BiomeBuilderEvent() {}

        private static final List<Consumer<HolderLookup.Provider>> ENTRIES = new CopyOnWriteArrayList<>();

        public static void onRunModifiers(Consumer<HolderLookup.Provider> provider) {
            ENTRIES.add(provider);
        }

        public static void passOnRunModifiers(HolderLookup.Provider provider) {
            for (Consumer<HolderLookup.Provider> listener : ENTRIES) {
                listener.accept(provider);
            }
        }
    }