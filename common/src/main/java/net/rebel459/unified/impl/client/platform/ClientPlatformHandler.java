package net.rebel459.unified.impl.client.platform;

import java.util.ServiceLoader;

public final class ClientPlatformHandler {
    public static final CommonClientPlatform INSTANCE = load(CommonClientPlatform.class);

    private ClientPlatformHandler() {}

    private static <T> T load(Class<T> clazz) {
        return ServiceLoader.load(clazz, clazz.getClassLoader())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No ClientInternalHandler implementation found for " + clazz.getName()));
    }
}