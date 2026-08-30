package net.rebel459.unified.impl.core;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.rebel459.unified.api.core.Supplied;
import net.rebel459.unified.api.core.SuppliedBlock;
import net.rebel459.unified.api.core.SuppliedItem;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class DataRegistryClaims {
    private static final Map<ResourceKey<? extends Registry<?>>, Map<Identifier, Supplied<?>>> CLAIMS = new ConcurrentHashMap<>();
    private static final ThreadLocal<Map<ResourceKey<? extends Registry<?>>, Identifier>> ACTIVE =
            ThreadLocal.withInitial(HashMap::new);
    private static final ThreadLocal<PlatformRegistration> PLATFORM_REGISTRATION = new ThreadLocal<>();

    private DataRegistryClaims() {}

    public static @Nullable SuppliedBlock block(Identifier id) {
        if (isPlatformRegistration(Registries.BLOCK, id)) return null;
        return claimed(Registries.BLOCK, id);
    }

    public static @Nullable SuppliedItem item(Identifier id) {
        if (isPlatformRegistration(Registries.ITEM, id)) return null;
        return claimed(Registries.ITEM, id);
    }

    public static boolean isRegisteringBlock(Identifier id) {
        return isRegistering(Registries.BLOCK, id);
    }

    public static boolean isRegisteringItem(Identifier id) {
        return isRegistering(Registries.ITEM, id);
    }

    public static SuppliedBlock registerBlock(Identifier id, Supplier<SuppliedBlock> registration) {
        return register(Registries.BLOCK, id, registration, "block");
    }

    public static SuppliedItem registerItem(Identifier id, Supplier<SuppliedItem> registration) {
        return register(Registries.ITEM, id, registration, "item");
    }

    static <T> T platformRegistration(ResourceKey<? extends Registry<?>> registry, Identifier id,
            Supplier<T> registration) {
        PlatformRegistration previous = PLATFORM_REGISTRATION.get();
        PLATFORM_REGISTRATION.set(new PlatformRegistration(registry, id));
        try {
            return registration.get();
        } finally {
            if (previous == null) PLATFORM_REGISTRATION.remove();
            else PLATFORM_REGISTRATION.set(previous);
        }
    }

    private static boolean isPlatformRegistration(ResourceKey<? extends Registry<?>> registry, Identifier id) {
        PlatformRegistration registration = PLATFORM_REGISTRATION.get();
        return registration != null && registration.registry.equals(registry) && registration.id.equals(id);
    }

    public static void claimBlock(Identifier id, SuppliedBlock block) {
        claim(Registries.BLOCK, id, block, "block");
    }

    public static void claimItem(Identifier id, SuppliedItem item) {
        claim(Registries.ITEM, id, item, "item");
    }

    @SuppressWarnings("unchecked")
    private static <S extends Supplied<?>> @Nullable S claimed(
            ResourceKey<? extends Registry<?>> registry, Identifier id) {
        Map<Identifier, Supplied<?>> claims = CLAIMS.get(registry);
        return claims == null ? null : (S) claims.get(id);
    }

    private static boolean isRegistering(ResourceKey<? extends Registry<?>> registry, Identifier id) {
        return id.equals(ACTIVE.get().get(registry));
    }

    public static <S extends Supplied<?>> S register(ResourceKey<? extends Registry<?>> registry,
            Identifier id, Supplier<S> registration, String type) {
        Map<ResourceKey<? extends Registry<?>>, Identifier> active = ACTIVE.get();
        if (active.putIfAbsent(registry, id) != null) {
            throw new IllegalStateException("Nested JSON " + type + " registration: " + id);
        }
        try {
            S value = registration.get();
            claim(registry, id, value, type);
            return value;
        } finally {
            active.remove(registry);
            if (active.isEmpty()) ACTIVE.remove();
        }
    }

    private static void claim(ResourceKey<? extends Registry<?>> registry, Identifier id,
            Supplied<?> value, String type) {
        Supplied<?> previous = CLAIMS.computeIfAbsent(registry, ignored -> new ConcurrentHashMap<>())
                .putIfAbsent(id, value);
        if (previous != null && previous != value) {
            throw new IllegalStateException("JSON " + type + " identifier was claimed twice: " + id);
        }
    }

    private record PlatformRegistration(ResourceKey<? extends Registry<?>> registry, Identifier id) {}
}
