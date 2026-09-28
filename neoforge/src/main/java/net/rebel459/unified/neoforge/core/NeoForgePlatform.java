package net.rebel459.unified.neoforge.core;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.rebel459.unified.api.platform.ModLoader;
import net.rebel459.unified.impl.core.CommonPlatform;

import java.nio.file.Path;

public class NeoForgePlatform implements CommonPlatform {

    @Override
    public ModLoader getModLoader() {
        return ModLoader.NEOFORGE;
    }

    @Override
    public Path getGameDirectory() {
        return FMLPaths.GAMEDIR.get();
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

    @Override
    public void executeAfter(ResourceKey<? extends Registry<?>> registry, Runnable runnable) {
        NeoForgeUnifiedRegistries.afterRegistry(registry, runnable);
    }
}