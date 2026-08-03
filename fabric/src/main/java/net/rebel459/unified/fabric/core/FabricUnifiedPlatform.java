package net.rebel459.unified.fabric.core;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.rebel459.unified.api.platform.ModLoader;
import net.rebel459.unified.api.util.VanillaVersion;
import net.rebel459.unified.impl.core.HelpersImpl;

public class FabricUnifiedPlatform implements HelpersImpl.Platform {

    @Override
    public ModLoader getModLoader() {
        return ModLoader.FABRIC;
    }

    @Override
    public boolean isClientSide() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    @Override
    public boolean isServerSide() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER;
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }
}