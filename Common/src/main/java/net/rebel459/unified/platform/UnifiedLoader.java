package net.rebel459.unified.platform;

import java.util.ServiceLoader;

public class UnifiedLoader {
    public static <T> T load(Class<T> clazz) {
        return ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> exception(clazz));
    }

    public static <T> NullPointerException exception(Class<T> clazz){
        return new NullPointerException("Failed to load service for " + clazz.getName());
    }
}