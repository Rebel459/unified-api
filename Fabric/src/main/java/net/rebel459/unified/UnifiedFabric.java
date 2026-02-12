package net.rebel459.unified;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.rebel459.unified.platform.FabricUnifiedEvents;
import net.rebel459.unified.platform.FabricUnifiedRegistries;

public class UnifiedFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        FabricUnifiedRegistries.init();
        FabricUnifiedEvents.init();
        Unified.initRegistries();
        Unified.init();
    }
}
