package net.rebel459.unified.fabric.util;

import net.rebel459.unified.fabric.FabricUnifiedInitializer;

import java.util.ArrayList;
import java.util.List;

public final class FabricInitializerState {

    private static final List<FabricUnifiedInitializer> INITIALIZERS = new ArrayList<>();
    private static boolean completed;

    public static void initializeCommon() {
        List<FabricUnifiedInitializer> initializers;
        synchronized (INITIALIZERS) {
            if (completed) return;
            completed = true;
            initializers = List.copyOf(INITIALIZERS);
            INITIALIZERS.clear();
        }
        initializers.forEach(FabricUnifiedInitializer::onInitializeCommon);
    }

    public static List<FabricUnifiedInitializer> getInitializers() {
        return List.copyOf(INITIALIZERS);
    }

    public static void addInitializer(FabricUnifiedInitializer initializer) {
        INITIALIZERS.add(initializer);
    }

    public static boolean completed() {
        return completed;
    }
}
