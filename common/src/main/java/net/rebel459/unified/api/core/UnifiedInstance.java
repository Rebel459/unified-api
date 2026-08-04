package net.rebel459.unified.api.core;

import net.rebel459.unified.api.platform.ModLoader;
import net.rebel459.unified.impl.core.CommonInstance;
import net.rebel459.unified.impl.platform.PlatformHandler;

public class UnifiedInstance {

    private static CommonInstance get() {
        return PlatformHandler.INSTANCE.getInstance();
    }

    public static ModLoader getModLoader() {
        return get().getModLoader();
    }

    public static boolean isClientSide() {
        return get().isClientSide();
    }
    public static boolean isServerSide() {
        return get().isServerSide();
    }

    public static boolean isModLoaded(String modId) {
        return get().isModLoaded(modId);
    }

    public static boolean isDevelopmentEnvironment() {
        return get().isDevelopmentEnvironment();
    }
}
