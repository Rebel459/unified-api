package net.rebel459.unified.platform;

import net.neoforged.fml.ModList;

public class NeoForgeUnifiedPlatform implements UnifiedPlatform {

    @Override
    public String getPlatform() {
        return UnifiedPlatform.NEOFORGE;
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }
}