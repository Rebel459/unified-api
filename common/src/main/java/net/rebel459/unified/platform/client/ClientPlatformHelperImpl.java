package net.rebel459.unified.platform.client;

import java.util.ServiceLoader;

public final class ClientPlatformHelperImpl {
    public static final ClientPlatformHelper INSTANCE = load(ClientPlatformHelper.class);

    private ClientPlatformHelperImpl() {}

    private static <T> T load(Class<T> clazz) {
        return ServiceLoader.load(clazz, clazz.getClassLoader())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No ClientPlatformHelper implementation found for " + clazz.getName()));
    }
}