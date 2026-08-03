package net.rebel459.unified.platform;

import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import net.rebel459.unified.util.LoaderType;
import net.rebel459.unified.util.VanillaVersion;

public class NeoForgeUnifiedPlatform implements HelpersImpl.Platform {

    @Override
    public LoaderType getLoader() {
        return LoaderType.NEOFORGE;
    }

    @Override
    public boolean isClientSide() {
        return FMLEnvironment.getDist().isClient();
    }

    @Override
    public boolean isServerSide() {
        return FMLEnvironment.getDist().isDedicatedServer();
    }

    @Override
    public boolean isModLoaded(String modId) {
        var modList = ModList.get();
        boolean loadingModCheck = FMLLoader.getCurrent().getLoadingModList().getModFileById(modId) != null;
        if (modList == null) return loadingModCheck;
        else return ModList.get().isLoaded(modId) || loadingModCheck;
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.getCurrent().isProduction();
    }
}