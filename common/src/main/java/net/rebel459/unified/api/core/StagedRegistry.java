package net.rebel459.unified.api.core;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public final class StagedRegistry<T> {

    private static final Map<ResourceKey<? extends Registry<?>>, Map<Identifier, Supplied<?>>> CLAIMS = new ConcurrentHashMap<>();
    private static final ThreadLocal<Map<ResourceKey<? extends Registry<?>>, Identifier>> ACTIVE = ThreadLocal.withInitial(HashMap::new);
    private static final ThreadLocal<StagedRegistry.PlatformRegistration> PLATFORM_REGISTRATION = new ThreadLocal<>();

    @SuppressWarnings("unchecked")
    public static <S extends Supplied<?>> @Nullable S getClaimed(ResourceKey<?> key) {
        StagedRegistry.PlatformRegistration registration = PLATFORM_REGISTRATION.get();
        if (registration != null && registration.registry.equals(key.registryKey()) && registration.id.equals(key.identifier())) return null;
        Map<Identifier, Supplied<?>> claims = CLAIMS.get(key.registryKey());
        return claims == null ? null : (S) claims.get(key.identifier());
    }

    public static boolean isRegistering(ResourceKey<? extends Registry<?>> registry, Identifier id) {
        return id.equals(ACTIVE.get().get(registry));
    }

    public static <S extends Supplied<?>> S register(ResourceKey<? extends Registry<?>> registry, Identifier id, Supplier<S> registration) {
        Map<ResourceKey<? extends Registry<?>>, Identifier> active = ACTIVE.get();
        if (active.putIfAbsent(registry, id) != null) {
            throw new IllegalStateException("Nested JSON " + registry.identifier() + " registration: " + id);
        }
        try {
            S value = registration.get();
            Supplied<?> previous = CLAIMS.computeIfAbsent(registry, ignored -> new ConcurrentHashMap<>()).putIfAbsent(id, value);
            if (previous != null && previous != value) {
                throw new IllegalStateException("JSON " + registry.identifier() + " identifier was claimed twice: " + id);
            }
            return value;
        } finally {
            active.remove(registry);
            if (active.isEmpty()) ACTIVE.remove();
        }
    }

    private record PlatformRegistration(ResourceKey<? extends Registry<?>> registry, Identifier id) {}

    private static final Map<ResourceKey<? extends Registry<?>>, StagedRegistry<?>> REGISTRIES = new LinkedHashMap<>();
    private static final Set<ResourceKey<? extends Registry<?>>> FINISHED = new HashSet<>();
    private static boolean staging = true;

    private final ResourceKey<? extends Registry<T>> registry;
    private final Map<Identifier, Registration<T>> registrations = new LinkedHashMap<>();

    private StagedRegistry(ResourceKey<? extends Registry<T>> registry) {
        this.registry = registry;
    }

    /**
     * Stages a registration or performs it immediately after static bootstrap has
     * completed. A data-driven registration for the same identifier replaces the
     * staged registration while retaining the original supplied handle.
     */
    public static <T, S extends Supplied<T>> S stage(
            ResourceKey<? extends Registry<T>> registry,
            Identifier id,
            BiFunction<Supplier<? extends T>, Supplier<? extends Holder<T>>, S> placeholder,
            Supplier<S> registration) {
        synchronized (StagedRegistry.class) {
            S claimed = StagedRegistry.getClaimed(ResourceKey.create(registry, id));
            if (claimed != null) return claimed;
            if (!staging || FINISHED.contains(registry)) return registration.get();

            StagedRegistry<T> stagedRegistry = StagedRegistry.registry(registry);
            Registration<T> existing = stagedRegistry.registrations.get(id);
            if (existing == null) {
                AtomicReference<Supplied<T>> registered = new AtomicReference<>();
                Supplier<Supplied<T>> resolved = () -> resolve(registered, id);
                S supplied = placeholder.apply(() -> resolved.get().get(), () -> resolved.get().holder());
                stagedRegistry.registrations.put(id, new Registration<>(supplied, registered, registration));
                return supplied;
            }

            if (!StagedRegistry.isRegistering(registry, id)) {
                throw new IllegalStateException("Duplicate code registration in " + registry.identifier() + ": " + id);
            }
            existing.registration = registration;
            return cast(existing.supplied);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> StagedRegistry<T> registry(ResourceKey<? extends Registry<T>> registry) {
        return (StagedRegistry<T>) REGISTRIES.computeIfAbsent(registry, ignored -> new StagedRegistry<>(registry));
    }

    @SuppressWarnings("unchecked")
    private static <S extends Supplied<?>> S cast(Supplied<?> supplied) {
        return (S) supplied;
    }

    @ApiStatus.Internal
    public static synchronized void finish() {
        if (!staging) return;
        staging = false;
        for (StagedRegistry<?> registry : REGISTRIES.values()) registry.flush();
    }

    @ApiStatus.Internal
    public static synchronized void finish(ResourceKey<? extends Registry<?>> registry) {
        FINISHED.add(registry);
        StagedRegistry<?> stagedRegistry = REGISTRIES.get(registry);
        if (stagedRegistry != null) stagedRegistry.flush();
    }

    private void flush() {
        Iterator<Map.Entry<Identifier, Registration<T>>> iterator = registrations.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Identifier, Registration<T>> entry = iterator.next();
            register(entry.getKey(), entry.getValue());
            iterator.remove();
        }
    }

    private void register(Identifier id, Registration<T> registration) {
        Supplied<T> registered = StagedRegistry.platformRegistration(registry, id, registration.registration::get);
        registration.registered.set(registered);
    }

    private static <T> T platformRegistration(ResourceKey<? extends Registry<?>> registry, Identifier id, Supplier<T> registration) {
        StagedRegistry.PlatformRegistration previous = PLATFORM_REGISTRATION.get();
        PLATFORM_REGISTRATION.set(new StagedRegistry.PlatformRegistration(registry, id));
        try {
            return registration.get();
        } finally {
            if (previous == null) PLATFORM_REGISTRATION.remove();
            else PLATFORM_REGISTRATION.set(previous);
        }
    }

    private static <T> Supplied<T> resolve(AtomicReference<Supplied<T>> reference, Identifier id) {
        Supplied<T> supplied = reference.get();
        if (supplied == null) {
            throw new IllegalStateException("Requested staged registry value before static bootstrap: " + id);
        }
        return supplied;
    }

    private static final class Registration<T> {
        private final Supplied<T> supplied;
        private final AtomicReference<Supplied<T>> registered;
        private Supplier<? extends Supplied<T>> registration;

        private Registration(Supplied<T> supplied, AtomicReference<Supplied<T>> registered, Supplier<? extends Supplied<T>> registration) {
            this.supplied = supplied;
            this.registered = registered;
            this.registration = registration;
        }
    }
}
