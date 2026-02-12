package net.rebel459.unified;

import net.fabricmc.api.ModInitializer;
import net.rebel459.unified.platform.FabricUnifiedEvents;
import net.rebel459.unified.platform.FabricUnifiedHelpers;
import net.rebel459.unified.platform.FabricUnifiedRegistries;

public class UnifiedFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        FabricUnifiedEvents.init();
        FabricUnifiedRegistries.init();
        FabricUnifiedHelpers.init();
        Unified.initRegistries();
        Unified.init();
    }
}
