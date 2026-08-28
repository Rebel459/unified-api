package net.rebel459.unified.util.registry;

/**
 * Service-provider bootstrap hook for registering custom {@link RegistryResourceListener}s.
 *
 * <p>Implementations must be listed in
 * {@code META-INF/services/net.rebel459.unified.util.registry.RegistryResourceInitializer}.
 * This hook runs after Unified's registry codecs are initialized and before any registry-resource
 * listener is loaded or the platform's deferred registrations are committed.</p>
 */
@FunctionalInterface
public interface RegistryResourceInitializer {
    void initializeRegistryResources();
}
