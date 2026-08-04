package net.rebel459.unified.impl.core;

import net.rebel459.unified.api.platform.ModLoader;

public interface CommonInstance {

    ModLoader getModLoader();

    boolean isClientSide();
    boolean isServerSide();

    boolean isModLoaded(String modId);

    boolean isDevelopmentEnvironment();
}