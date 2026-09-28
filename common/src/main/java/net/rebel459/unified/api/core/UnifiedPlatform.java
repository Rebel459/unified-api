package net.rebel459.unified.api.core;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.rebel459.unified.api.platform.ModLoader;
import net.rebel459.unified.impl.core.CommonPlatform;
import net.rebel459.unified.impl.platform.PlatformLoader;

import java.nio.file.Path;

public class UnifiedPlatform {

    private static CommonPlatform get() {
        return PlatformLoader.INSTANCE.getInstance();
    }

    public static ModLoader getModLoader() {
        return get().getModLoader();
    }

    public static Path getGameDirectory() {
        return get().getGameDirectory();
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

    public static void executeAfter(ResourceKey<? extends Registry<?>> registry, Runnable runnable) {
        get().executeAfter(registry, runnable);
    }
}
