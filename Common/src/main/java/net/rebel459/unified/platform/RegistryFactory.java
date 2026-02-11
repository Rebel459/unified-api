package net.rebel459.unified.platform;

public final class RegistryFactory {

    private static Factory instance;

    private RegistryFactory() {}

    public static void set(Factory factory) {
        if (instance != null) {
            throw new IllegalStateException("RegistryFactory already set");
        }
        instance = factory;
    }

    public static Factory get() {
        if (instance == null) {
            throw new IllegalStateException("RegistryFactory not initialized");
        }
        return instance;
    }

    public interface Factory {
        UnifiedRegistries.ItemRegistry createItemRegistry(String modId);
        UnifiedRegistries.BlockRegistry createBlockRegistry(String modId);
        UnifiedRegistries.FuelRegistry createFuelRegistry();
        UnifiedRegistries.CreativeRegistry createCreativeRegistry();
        UnifiedRegistries.ComponentRegistry createComponentRegistry(String modId);
    }
}