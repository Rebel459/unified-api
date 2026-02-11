package net.rebel459.unified.platform;

public final class UnifiedFactory {

    private static Registries registries;

    private UnifiedFactory() {}

    public static void setRegistries(Registries factory) {
        if (registries != null) {
            throw new IllegalStateException("UnifiedFactory Registries already set");
        }
        registries = factory;
    }

    public static Registries getRegistries() {
        if (registries == null) {
            throw new IllegalStateException("UnifiedFactory Registries not initialized");
        }
        return registries;
    }

    public interface Registries {
        UnifiedRegistries.ItemRegistry createItemRegistry(String modId);
        UnifiedRegistries.BlockRegistry createBlockRegistry(String modId);
        UnifiedRegistries.CreativeRegistry createCreativeRegistry(String modId);
        UnifiedRegistries.ComponentRegistry createComponentRegistry(String modId);
    }

    private static Events events;

    public static void setEvents(Events factory) {
        if (events != null) {
            throw new IllegalStateException("UnifiedFactory Events already set");
        }
        events = factory;
    }

    public static Events getEvents() {
        if (events == null) {
            throw new IllegalStateException("UnifiedFactory Events not initialized");
        }
        return events;
    }

    public interface Events {
        UnifiedEvents.CreativeEvent createCreativeEvent();
        UnifiedEvents.LootEvent createLootEvent();
        UnifiedEvents.PackEvent createPackEvent();
        UnifiedEvents.FuelEvent createFuelEvent();
    }
}