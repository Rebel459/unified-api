package net.rebel459.unified.fabric;

import net.rebel459.unified.fabric.util.FabricInitializerState;

import java.util.Objects;

/**A Fabric equivalent of NeoForge's FMLCommonSetupEvent which ensures your post-registry init runs at a safe time to access Unified's deferred registries from your Fabric-side init */
@FunctionalInterface
public interface FabricUnifiedInitializer {

    void onInitializeCommon();

    static void register(FabricUnifiedInitializer initializer) {
        FabricInitializerState.register(initializer);
    }
}