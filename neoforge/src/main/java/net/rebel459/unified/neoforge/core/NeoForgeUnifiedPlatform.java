package net.rebel459.unified.neoforge.core;

import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import net.rebel459.unified.api.platform.ModLoader;
import net.rebel459.unified.api.util.VanillaVersion;
import net.rebel459.unified.impl.core.HelpersImpl;

public class NeoForgeUnifiedPlatform implements HelpersImpl.Platform {

    @Override
    public ModLoader getModLoader() {
        return ModLoader.NEOFORGE;
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