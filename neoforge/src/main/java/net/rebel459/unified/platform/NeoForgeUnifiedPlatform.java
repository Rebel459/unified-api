package net.rebel459.unified.platform;

import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.LoadingModList;
import net.rebel459.unified.util.EnvInfo;
import net.rebel459.unified.util.PlatformInfo;

public class NeoForgeUnifiedPlatform implements HelpersImpl.Platform {

    @Override
    public PlatformInfo getPlatform() {
        return PlatformInfo.NEOFORGE;
    }

    @Override
    public EnvInfo getEnvironment() {
        return switch (FMLEnvironment.getDist()) {
            case CLIENT -> EnvInfo.CLIENT;
            case DEDICATED_SERVER -> EnvInfo.SERVER;
        };
    }

    @Override
    public boolean isModLoaded(String modId) {
        var modList = ModList.get();
        boolean loadingModCheck = FMLLoader.getCurrent().getLoadingModList().getModFileById(modId) != null;
        if (modList == null) return loadingModCheck;
        else return ModList.get().isLoaded(modId) || loadingModCheck;
    }

    @Override
    public boolean isDevelopmentInstance() {
        return !FMLLoader.getCurrent().isProduction();
    }
}