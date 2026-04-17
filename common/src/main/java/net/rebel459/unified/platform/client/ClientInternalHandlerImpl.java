package net.rebel459.unified.platform.client;

import java.util.ServiceLoader;

public final class ClientInternalHandlerImpl {
    public static final ClientInternalHandler INSTANCE = load(ClientInternalHandler.class);

    private ClientInternalHandlerImpl() {}

    private static <T> T load(Class<T> clazz) {
        return ServiceLoader.load(clazz, clazz.getClassLoader())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No ClientInternalHandler implementation found for " + clazz.getName()));
    }
}