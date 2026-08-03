package net.rebel459.unified;

import net.fabricmc.api.ClientModInitializer;
import net.rebel459.unified.fabric.client.core.FabricUnifiedClientEvents;

public class UnifiedFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        FabricUnifiedClientEvents.init();
        UnifiedClient.init();
    }
}
