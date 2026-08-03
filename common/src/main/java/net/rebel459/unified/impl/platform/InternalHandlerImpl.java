package net.rebel459.unified.impl.platform;

import java.util.ServiceLoader;

public final class InternalHandlerImpl {
    public static final InternalHandler INSTANCE = load(InternalHandler.class);

    private InternalHandlerImpl() {}

    private static <T> T load(Class<T> clazz) {
        return ServiceLoader.load(clazz, clazz.getClassLoader())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No InternalHandler implementation found for " + clazz.getName()));
    }
}