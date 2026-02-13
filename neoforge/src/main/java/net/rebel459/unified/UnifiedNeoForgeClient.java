package net.rebel459.unified;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.rebel459.unified.platform.client.NeoForgeUnifiedClientEvents;
import net.rebel459.unified.platform.client.NeoForgeUnifiedClientHelpers;
import net.rebel459.unified.platform.client.NeoForgeUnifiedClientRegistries;

@Mod(value = Unified.MOD_ID, dist = Dist.CLIENT)
public class UnifiedNeoForgeClient {

    public UnifiedNeoForgeClient(IEventBus modEventBus) {
        NeoForgeUnifiedClientEvents.init();
        UnifiedClient.initClient();
        modEventBus.addListener(NeoForgeUnifiedClientHelpers.ParticleProviders::registerParticleProviders);
        modEventBus.addListener(NeoForgeUnifiedClientHelpers.EntityRenderers::registerLayerDefinitions);
        modEventBus.addListener(NeoForgeUnifiedClientHelpers.EntityRenderers::registerRenderers);
        modEventBus.addListener(NeoForgeUnifiedClientRegistries.KeyMappings::registerBindings);
    }
}