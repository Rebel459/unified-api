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
        UnifiedRegistries.Items createItems(String modId);
        UnifiedRegistries.Blocks createBlocks(String modId);
        UnifiedRegistries.CreativeTabs createCreativeTabs(String modId);
        UnifiedRegistries.ItemComponents createItemComponents(String modId);
        UnifiedRegistries.Particles createParticles(String modId);
        UnifiedRegistries.MobEffects createMobEffects(String modId);
        UnifiedRegistries.EntityTypes createEntityTypes(String modId);
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
        UnifiedEvents.CreativeEntries createCreativeEntries();
        UnifiedEvents.LootTables createLootTables();
        UnifiedEvents.Packs createPacks();
        UnifiedEvents.FurnaceFuels createFurnaceFuels();
        UnifiedEvents.StrippableBlocks createStrippableBlocks();
        UnifiedEvents.ClientParticleProviders createClientParticleProviders();
    }
}