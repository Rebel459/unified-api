package net.rebel459.unified;

import net.fabricmc.api.ModInitializer;
import net.rebel459.unified.fabric.core.FabricUnifiedEvents;

public class UnifiedFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Unified.initRegistries();
        FabricUnifiedEvents.init();
        Unified.init();
    }
}
