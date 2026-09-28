package net.rebel459.unified.impl.platform;

import java.util.ServiceLoader;

public final class PlatformLoader {
    public static final PlatformHandler INSTANCE = load(PlatformHandler.class);

    private PlatformLoader() {}

    private static <T> T load(Class<T> clazz) {
        return ServiceLoader.load(clazz, clazz.getClassLoader())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No CommonPlatform implementation found for " + clazz.getName()));
    }
}