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
    private static final Map<ResourceKey<? extends Registry<?>>, List<Runnable>> AFTER_FINISH = new LinkedHashMap<>();
    private static boolean staging = true;

    private final ResourceKey<? extends Registry<T>> registry;
    private final Map<Identifier, Registration<T>> registrations = new LinkedHashMap<>();

    private StagedRegistry(ResourceKey<? extends Registry<T>> registry) {
        this.registry = registry;
    }
    
    public static <T, S extends Supplied<T>> S stage(
            Registry<T> registry,
            Identifier id,
            BiFunction<Supplier<? extends T>, Supplier<? extends Holder<T>>, S> placeholder,
            Supplier<S> registration) {
        synchronized (StagedRegistry.class) {
            ResourceKey<? extends Registry<T>> registryKey = registry.key();
            ResourceKey<T> key = ResourceKey.create(registryKey, id);
            S claimed = StagedRegistry.getClaimed(key);
            if (claimed != null) return claimed;
            if (!staging || FINISHED.contains(registryKey)) return registration.get();

            StagedRegistry<T> stagedRegistry = StagedRegistry.registry(registryKey);
            Registration<T> existing = stagedRegistry.registrations.get(id);
            if (existing == null) {
                AtomicReference<Supplied<T>> registered = new AtomicReference<>();
                Supplier<Supplied<T>> resolved = () -> stagedRegistry.resolve(registered, id);
                S supplied = placeholder.apply(() -> resolved.get().get(), () -> resolved.get().holder());
                stagedRegistry.registrations.put(id, new Registration<>(supplied, registered, registration));
                return supplied;
            }

            if (!StagedRegistry.isRegistering(registryKey, id)) {
                throw new IllegalStateException("Duplicate code registration in " + registryKey.identifier() + ": " + id);
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
        List<Runnable> actions = AFTER_FINISH.values().stream().flatMap(Collection::stream).toList();
        AFTER_FINISH.clear();
        actions.forEach(Runnable::run);
    }

    @ApiStatus.Internal
    public static synchronized void finish(ResourceKey<? extends Registry<?>> registry) {
        FINISHED.add(registry);
        StagedRegistry<?> stagedRegistry = REGISTRIES.get(registry);
        if (stagedRegistry != null) stagedRegistry.flush();
        List<Runnable> actions = AFTER_FINISH.remove(registry);
        if (actions != null) actions.forEach(Runnable::run);
    }

    /** Call UnifiedInstance.executeAfter instead */
    @ApiStatus.Internal
    public static synchronized void afterFinish(ResourceKey<? extends Registry<?>> registry, Runnable runnable) {
        if (!staging || FINISHED.contains(registry)) {
            runnable.run();
            return;
        }
        AFTER_FINISH.computeIfAbsent(registry, ignored -> new ArrayList<>()).add(runnable);
    }

    private void flush() {
        while (!registrations.isEmpty()) {
            Map.Entry<Identifier, Registration<T>> entry = registrations.entrySet().iterator().next();
            registrations.remove(entry.getKey());
            register(entry.getKey(), entry.getValue());
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

    private Supplied<T> resolve(AtomicReference<Supplied<T>> reference, Identifier id) {
        Supplied<T> supplied = reference.get();
        if (supplied == null) {
            Registration<T> registration = registrations.remove(id);
            if (registration == null) {
                throw new IllegalStateException("Requested staged registry value while it was being registered: " + id);
            }
            register(id, registration);
            supplied = reference.get();
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
