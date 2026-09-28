package net.rebel459.unified.impl.core;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.rebel459.unified.api.platform.ModLoader;

import java.nio.file.Path;

public interface CommonPlatform {

    ModLoader getModLoader();

    Path getGameDirectory();

    boolean isClientSide();
    boolean isServerSide();

    boolean isModLoaded(String modId);

    boolean isDevelopmentEnvironment();

    void executeAfter(ResourceKey<? extends Registry<?>> registry, Runnable runnable);
}