package net.rebel459.unified.platform;

import net.rebel459.unified.platform.client.UnifiedClientEvents;
import net.rebel459.unified.platform.client.UnifiedClientRegistries;

public final class UnifiedFactory {

    private UnifiedFactory() {}

    // REGISTRIES

    private static Registries registries;

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

    // CLIENT REGISTRIES

    private static ClientRegistries clientRegistries;

    public static void setClientRegistries(ClientRegistries factory) {
        if (clientRegistries != null) {
            throw new IllegalStateException("UnifiedFactory ClientRegistries already set");
        }
        clientRegistries = factory;
    }

    public static ClientRegistries getClientRegistries() {
        if (clientRegistries == null) {
            throw new IllegalStateException("UnifiedFactory ClientRegistries not initialized");
        }
        return clientRegistries;
    }

    public interface ClientRegistries {
        UnifiedClientRegistries.KeyMappings createKeyMappings(String modId);
    }

    // EVENTS

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
    }

    // CLIENT EVENTS

    private static ClientEvents clientEvents;

    public static void setClientEvents(ClientEvents factory) {
        if (clientEvents != null) {
            throw new IllegalStateException("UnifiedFactory ClientEvents already set");
        }
        clientEvents = factory;
    }

    public static ClientEvents getClientEvents() {
        if (clientEvents == null) {
            throw new IllegalStateException("UnifiedFactory ClientEvents not initialized");
        }
        return clientEvents;
    }

    public interface ClientEvents {
        UnifiedClientEvents.ParticleProviders createParticleProviders();
        UnifiedClientEvents.EntityRenderers createEntityRenderers();
        UnifiedClientEvents.BlockLayers createBlockLayers();
    }
}