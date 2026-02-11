package net.rebel459.unified;

import net.fabricmc.api.ModInitializer;
import net.rebel459.unified.platform.FabricUnifiedRegistries;

public class UnifiedFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        FabricUnifiedRegistries.init();
        Unified.initRegistries();
        Unified.init();
    }
}
