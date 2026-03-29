package net.rebel459.unified.platform.client;

import java.util.ServiceLoader;

public final class ClientInternalHelperImpl {
    public static final ClientInternalHelper INSTANCE = load(ClientInternalHelper.class);

    private ClientInternalHelperImpl() {}

    private static <T> T load(Class<T> clazz) {
        return ServiceLoader.load(clazz, clazz.getClassLoader())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No ClientInternalHelper implementation found for " + clazz.getName()));
    }
}