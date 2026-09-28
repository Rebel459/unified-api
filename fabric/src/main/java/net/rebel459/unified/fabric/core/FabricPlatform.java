package net.rebel459.unified.fabric.core;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.rebel459.unified.api.core.StagedRegistry;
import net.rebel459.unified.api.platform.ModLoader;
import net.rebel459.unified.impl.core.CommonPlatform;

import java.nio.file.Path;

public class FabricPlatform implements CommonPlatform {

    @Override
    public ModLoader getModLoader() {
        return ModLoader.FABRIC;
    }

    @Override
    public Path getGameDirectory() {
        return FabricLoader.getInstance().getGameDir();
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

    @Override
    public void executeAfter(ResourceKey<? extends Registry<?>> registry, Runnable runnable) {
        StagedRegistry.afterFinish(registry, runnable);
    }
}