package net.rebel459.unified.platform;

import net.fabricmc.loader.api.FabricLoader;
import net.rebel459.unified.util.EnvInfo;
import net.rebel459.unified.util.PlatformInfo;

public class FabricUnifiedPlatform implements HelpersImpl.Platform {

    @Override
    public PlatformInfo getPlatform() {
        return PlatformInfo.FABRIC;
    }

    @Override
    public EnvInfo getEnvironment() {
        return switch (FabricLoader.getInstance().getEnvironmentType()) {
            case CLIENT -> EnvInfo.CLIENT;
            case SERVER -> EnvInfo.SERVER;
        };
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentInstance() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }
}