package net.rebel459.unified.api.core;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public abstract class DataRegistry<T, P, S extends Supplied<T>> {

    private static final Map<ResourceKey<? extends Registry<?>>, Map<Identifier, Supplied<?>>> CLAIMS = new ConcurrentHashMap<>();
    private static final ThreadLocal<Map<ResourceKey<? extends Registry<?>>, Identifier>> ACTIVE = ThreadLocal.withInitial(HashMap::new);
    private static final ThreadLocal<DataRegistry.PlatformRegistration> PLATFORM_REGISTRATION = new ThreadLocal<>();

    @SuppressWarnings("unchecked")
    public static <S extends Supplied<?>> @Nullable S getClaimed(ResourceKey<? extends Registry<?>> registry, Identifier id) {
        DataRegistry.PlatformRegistration registration = PLATFORM_REGISTRATION.get();
        if (registration != null && registration.registry.equals(registry) && registration.id.equals(id)) return null;
        Map<Identifier, Supplied<?>> claims = CLAIMS.get(registry);
        return claims == null ? null : (S) claims.get(id);
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

    private static final Map<ResourceKey<? extends Registry<?>>, RegistryStage<?>> STAGES = new LinkedHashMap<>();
    private static boolean staging = true;

    private final ResourceKey<? extends Registry<T>> registry;
    private final RegistryStage<Registration<P, S>> stage;

    protected DataRegistry(ResourceKey<? extends Registry<T>> registry,
                           List<ResourceKey<? extends Registry<?>>> dependencies) {
        this.registry = registry;
        this.stage = createStage(registry, dependencies, this::register);
    }

    @SafeVarargs
    protected DataRegistry(ResourceKey<? extends Registry<T>> registry,
                           ResourceKey<? extends Registry<?>>... dependencies) {
        this(registry, List.of(dependencies));
    }

    /**
     * Stages a registration or performs it immediately after static bootstrap has
     * completed. A data-driven registration for the same identifier replaces the
     * staged plan while retaining the original supplied handle.
     */
    protected final S stage(Identifier id, P plan) {
        synchronized (DataRegistry.class) {
            S claimed = DataRegistry.getClaimed(registry, id);
            if (claimed != null) return claimed;
            if (!staging) return actualRegister(id, plan);

            Registration<P, S> registration = stage.get(id);
            if (registration == null) {
                AtomicReference<S> registered = new AtomicReference<>();
                S supplied = createPlaceholder(id, plan, () -> resolve(registered, id).get());
                stage.put(id, new Registration<>(supplied, registered, plan));
                return supplied;
            }

            if (!DataRegistry.isRegistering(registry, id)) {
                throw new IllegalStateException("Duplicate code registration in "
                        + registry.identifier() + ": " + id);
            }
            registration.plan = plan;
            return registration.supplied;
        }
    }

    /** Creates the stable handle returned while {@code id} is still staged. */
    protected abstract S createPlaceholder(Identifier id, P plan, Supplier<? extends T> value);

    /** Performs the implementation-specific platform registration. */
    protected abstract S actualRegister(Identifier id, P plan);

    public final ResourceKey<? extends Registry<T>> registryKey() {
        return registry;
    }

    private void register(Registration<P, S> registration, Identifier id) {
        S registered = DataRegistry.platformRegistration(registry, id, () -> actualRegister(id, registration.plan));
        registration.registered.set(registered);
    }
    
    private static <T> T platformRegistration(ResourceKey<? extends Registry<?>> registry, Identifier id, Supplier<T> registration) {
        DataRegistry.PlatformRegistration previous = PLATFORM_REGISTRATION.get();
        PLATFORM_REGISTRATION.set(new DataRegistry.PlatformRegistration(registry, id));
        try {
            return registration.get();
        } finally {
            if (previous == null) PLATFORM_REGISTRATION.remove();
            else PLATFORM_REGISTRATION.set(previous);
        }
    }

    private static synchronized <P> RegistryStage<P> createStage(
            ResourceKey<? extends Registry<?>> registry,
            List<ResourceKey<? extends Registry<?>>> dependencies,
            PlanRegistrar<P> registrar) {
        if (!staging) {
            throw new IllegalStateException("Static registry bootstrap has already completed: "
                    + registry.identifier());
        }
        RegistryStage<P> stage = new RegistryStage<>(registry, dependencies, registrar);
        if (STAGES.putIfAbsent(registry, stage) != null) {
            throw new IllegalStateException("Static registry stage already exists for " + registry.identifier());
        }
        return stage;
    }

    @ApiStatus.Internal
    public static synchronized void finish() {
        if (!staging) return;
        Set<ResourceKey<? extends Registry<?>>> completed = new LinkedHashSet<>();
        Set<ResourceKey<? extends Registry<?>>> visiting = new LinkedHashSet<>();
        for (RegistryStage<?> stage : STAGES.values()) stage.finish(completed, visiting);
        staging = false;
    }

    private static <T, S extends Supplied<T>> S resolve(AtomicReference<S> reference, Identifier id) {
        S supplied = reference.get();
        if (supplied == null) {
            throw new IllegalStateException("Requested staged registry value before static bootstrap: " + id);
        }
        return supplied;
    }

    private static final class Registration<P, S extends Supplied<?>> {
        private final S supplied;
        private final AtomicReference<S> registered;
        private P plan;

        private Registration(S supplied, AtomicReference<S> registered, P plan) {
            this.supplied = supplied;
            this.registered = registered;
            this.plan = plan;
        }
    }

    @FunctionalInterface
    private interface PlanRegistrar<P> {
        void register(P plan, Identifier id);
    }

    private static final class RegistryStage<P> {
        private final ResourceKey<? extends Registry<?>> registry;
        private final List<ResourceKey<? extends Registry<?>>> dependencies;
        private final PlanRegistrar<P> registrar;
        private final Map<Identifier, P> plans = new LinkedHashMap<>();

        private RegistryStage(ResourceKey<? extends Registry<?>> registry,
                List<ResourceKey<? extends Registry<?>>> dependencies, PlanRegistrar<P> registrar) {
            this.registry = registry;
            this.dependencies = List.copyOf(dependencies);
            this.registrar = registrar;
        }

        public ResourceKey<? extends Registry<?>> registryKey() {
            return registry;
        }

        public List<ResourceKey<? extends Registry<?>>> dependencies() {
            return dependencies;
        }

        private P get(Identifier id) {
            return plans.get(id);
        }

        private void put(Identifier id, P plan) {
            plans.put(id, plan);
        }

        private void finish(Set<ResourceKey<? extends Registry<?>>> completed,
                Set<ResourceKey<? extends Registry<?>>> visiting) {
            if (completed.contains(registry)) return;
            if (!visiting.add(registry)) {
                throw new IllegalStateException("Circular static registry stage dependency involving "
                        + registry.identifier());
            }
            try {
                for (ResourceKey<? extends Registry<?>> dependency : dependencies) {
                    RegistryStage<?> stage = STAGES.get(dependency);
                    if (stage != null) stage.finish(completed, visiting);
                }
                Iterator<Map.Entry<Identifier, P>> registrations = plans.entrySet().iterator();
                while (registrations.hasNext()) {
                    Map.Entry<Identifier, P> registration = registrations.next();
                    registrar.register(registration.getValue(), registration.getKey());
                    registrations.remove();
                }
                completed.add(registry);
            } finally {
                visiting.remove(registry);
            }
        }
    }
}
