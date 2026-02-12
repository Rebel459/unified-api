package net.rebel459.unified;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.rebel459.unified.platform.FabricUnifiedEvents;
import net.rebel459.unified.platform.FabricUnifiedRegistries;
import net.rebel459.unified.test.UnifiedTest;

public class UnifiedFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        UnifiedTest.clientInit();
    }
}
