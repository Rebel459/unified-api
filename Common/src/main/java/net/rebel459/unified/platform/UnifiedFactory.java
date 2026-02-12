package net.rebel459.unified.platform;

import net.rebel459.unified.platform.client.UnifiedClientHelpers;
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

    private static Helpers helpers;

    public static void setHelpers(Helpers factory) {
        if (helpers != null) {
            throw new IllegalStateException("UnifiedFactory Helpers already set");
        }
        helpers = factory;
    }

    public static Helpers getHelpers() {
        if (helpers == null) {
            throw new IllegalStateException("UnifiedFactory Helpers not initialized");
        }
        return helpers;
    }

    public interface Helpers {
        UnifiedHelpers.CreativeEntries createCreativeEntries();
        UnifiedHelpers.LootTables createLootTables();
        UnifiedHelpers.Packs createPacks();
        UnifiedHelpers.FurnaceFuels createFurnaceFuels();
        UnifiedHelpers.StrippableBlocks createStrippableBlocks();
    }

    // CLIENT EVENTS

    private static ClientHelpers clientHelpers;

    public static void setClientHelpers(ClientHelpers factory) {
        if (clientHelpers != null) {
            throw new IllegalStateException("UnifiedFactory ClientHelpers already set");
        }
        clientHelpers = factory;
    }

    public static ClientHelpers getClientHelpers() {
        if (clientHelpers == null) {
            throw new IllegalStateException("UnifiedFactory ClientHelpers not initialized");
        }
        return clientHelpers;
    }

    public interface ClientHelpers {
        UnifiedClientHelpers.ParticleProviders createParticleProviders();
        UnifiedClientHelpers.EntityRenderers createEntityRenderers();
        UnifiedClientHelpers.BlockLayers createBlockLayers();
    }
}