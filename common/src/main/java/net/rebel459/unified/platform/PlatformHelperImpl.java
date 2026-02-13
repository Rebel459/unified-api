package net.rebel459.unified.platform;

import java.util.ServiceLoader;

public final class PlatformHelperImpl {
    public static final PlatformHelper INSTANCE = load(PlatformHelper.class);

    private PlatformHelperImpl() {}

    private static <T> T load(Class<T> clazz) {
        return ServiceLoader.load(clazz, clazz.getClassLoader())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No PlatformHelper implementation found for " + clazz.getName()));
    }
}