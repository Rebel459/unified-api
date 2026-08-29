package net.rebel459.unified;

import net.fabricmc.api.ModInitializer;
import net.rebel459.unified.fabric.core.FabricUnifiedEvents;
import net.rebel459.unified.fabric.util.FabricInitializerState;

public class UnifiedFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Unified.initRegistries();
        FabricInitializerState.initializeCommon();

        FabricUnifiedEvents.init();
        Unified.init();
    }
}
