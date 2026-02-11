package net.rebel459.unified.platform;

import net.fabricmc.loader.api.FabricLoader;

public class FabricUnifiedPlatform implements UnifiedPlatform {

    @Override
    public String getPlatform() {
        return UnifiedPlatform.FABRIC;
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

}