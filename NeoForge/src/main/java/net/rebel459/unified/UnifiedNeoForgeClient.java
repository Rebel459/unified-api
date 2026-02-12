package net.rebel459.unified;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.rebel459.unified.platform.NeoForgeUnifiedEvents;
import net.rebel459.unified.platform.NeoForgeUnifiedRegistries;
import net.rebel459.unified.test.UnifiedTest;

@Mod(value = Unified.MOD_ID, dist = Dist.CLIENT)
public class UnifiedNeoForgeClient {

    public UnifiedNeoForgeClient(IEventBus modEventBus) {
        UnifiedTest.clientInit();
        modEventBus.addListener(NeoForgeUnifiedEvents.ClientParticleProviders::registerParticleProviders);
    }
}