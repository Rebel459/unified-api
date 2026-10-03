package net.rebel459.unified.api.core;

/**
 * Service-provider bootstrap hook for registering custom {@link RegistryResourceListener}s.
 *
 * <p>Implementations must be listed in
 * {@code META-INF/services/net.rebel459.unified.api.core.RegistryResourceInitializer}.
 * This hook runs after Unified's registry codecs are initialized and before any registry-resource
 * listener is loaded or the platform's deferred registrations are committed.</p>
 * <p>This hook adds new data-driven registries. Custom codecs for existing registries can be
 * registered during normal mod registry initialization and do not need a service provider.</p>
 */
@FunctionalInterface
public interface RegistryResourceInitializer {
    void initializeRegistryResources();
}
