package net.rebel459.unified;

import net.fabricmc.api.ModInitializer;
import net.rebel459.unified.platform.FabricUnifiedEvents;
import net.rebel459.unified.platform.FabricHelpersImpl;
import net.rebel459.unified.platform.FabricUnifiedRegistries;

public class UnifiedFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        FabricUnifiedEvents.init();
        FabricUnifiedRegistries.init();
        FabricHelpersImpl.init();
        Unified.initRegistries();
        Unified.init();
    }
}
