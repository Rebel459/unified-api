package net.rebel459.unified;

import net.fabricmc.api.ModInitializer;
import net.rebel459.unified.platform.FabricUnifiedRegistries;
import net.rebel459.unified.test.UnifiedTest;

public class UnifiedFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        FabricUnifiedRegistries.init();
        UnifiedTest.TEST_ITEM.get();
        UnifiedTest.TEST_BLOCK.get();
        Unified.initialize();
    }
}
