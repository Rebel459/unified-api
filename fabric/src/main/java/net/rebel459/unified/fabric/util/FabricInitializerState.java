package net.rebel459.unified.fabric.util;

import net.rebel459.unified.fabric.FabricUnifiedInitializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class FabricInitializerState {

    private static final List<FabricUnifiedInitializer> INITIALIZERS = new ArrayList<>();
    private static boolean completed;

    private FabricInitializerState() {}

    public static void register(FabricUnifiedInitializer initializer) {
        Objects.requireNonNull(initializer, "initializer");

        synchronized (FabricInitializerState.class) {
            if (!completed) {
                INITIALIZERS.add(initializer);
                return;
            }
        }

        initializer.onInitializeCommon();
    }

    public static void initializeCommon() {
        List<FabricUnifiedInitializer> initializers;

        synchronized (FabricInitializerState.class) {
            if (completed) return;

            completed = true;
            initializers = List.copyOf(INITIALIZERS);
            INITIALIZERS.clear();
        }

        initializers.forEach(FabricUnifiedInitializer::onInitializeCommon);
    }
}
