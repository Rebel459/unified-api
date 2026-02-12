package net.rebel459.unified;

import net.fabricmc.api.ClientModInitializer;
import net.rebel459.unified.platform.client.FabricUnifiedClientEvents;
import net.rebel459.unified.platform.client.FabricUnifiedClientHelpers;
import net.rebel459.unified.platform.client.FabricUnifiedClientRegistries;
import net.rebel459.unified.test.UnifiedTest;

public class UnifiedFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        FabricUnifiedClientEvents.init();
        FabricUnifiedClientRegistries.init();
        FabricUnifiedClientHelpers.init();
        UnifiedClient.initClient();
    }
}
