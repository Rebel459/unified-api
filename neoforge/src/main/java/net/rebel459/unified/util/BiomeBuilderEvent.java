package net.rebel459.unified.util;

import net.minecraft.core.HolderLookup;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class BiomeBuilderEvent {

        private BiomeBuilderEvent() {}

        private static final List<Consumer<HolderLookup.Provider>> ENTRIES = new CopyOnWriteArrayList<>();

        public static void onRunModifiers(Consumer<HolderLookup.Provider> lookup) {
            ENTRIES.add(lookup);
        }

        public static void passOnRunModifiers(HolderLookup.Provider lookup) {
            for (Consumer<HolderLookup.Provider> listener : ENTRIES) {
                listener.accept(lookup);
            }
        }
    }