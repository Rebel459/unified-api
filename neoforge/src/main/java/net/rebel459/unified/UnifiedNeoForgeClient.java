package net.rebel459.unified;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.rebel459.unified.platform.client.NeoForgeUnifiedClientEvents;
import net.rebel459.unified.platform.client.NeoForgeClientHelpersImpl;
import net.rebel459.unified.platform.client.NeoForgeUnifiedClientRegistries;

@Mod(value = Unified.MOD_ID, dist = Dist.CLIENT)
public class UnifiedNeoForgeClient {

    public UnifiedNeoForgeClient(IEventBus modEventBus) {
        NeoForgeUnifiedClientEvents.init();
        UnifiedClient.initClient();
        modEventBus.addListener(NeoForgeClientHelpersImpl.ParticleProviders::registerParticleProviders);
        modEventBus.addListener(NeoForgeClientHelpersImpl.EntityRenderers::registerLayerDefinitions);
        modEventBus.addListener(NeoForgeClientHelpersImpl.EntityRenderers::registerRenderers);
        modEventBus.addListener(NeoForgeUnifiedClientRegistries.KeyMappings::registerBindings);
        modEventBus.addListener(NeoForgeClientHelpersImpl.Tooltips::registerTooltipFactories);
    }
}