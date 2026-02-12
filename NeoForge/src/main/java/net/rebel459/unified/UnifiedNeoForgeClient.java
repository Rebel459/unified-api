package net.rebel459.unified;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.rebel459.unified.platform.client.NeoForgeUnifiedClientEvents;
import net.rebel459.unified.platform.client.NeoForgeUnifiedClientRegistries;
import net.rebel459.unified.test.UnifiedTest;

@Mod(value = Unified.MOD_ID, dist = Dist.CLIENT)
public class UnifiedNeoForgeClient {

    public UnifiedNeoForgeClient(IEventBus modEventBus) {
        NeoForgeUnifiedClientEvents.init();
        UnifiedTest.clientInit();
        modEventBus.addListener(NeoForgeUnifiedClientEvents.ParticleProviders::registerParticleProviders);
        modEventBus.addListener(NeoForgeUnifiedClientEvents.EntityRenderers::registerLayerDefinitions);
        modEventBus.addListener(NeoForgeUnifiedClientEvents.EntityRenderers::registerRenderers);
        modEventBus.addListener(NeoForgeUnifiedClientRegistries.KeyMappings::registerBindings);
    }
}