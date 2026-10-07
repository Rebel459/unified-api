package net.rebel459.unified.api.core;

import net.rebel459.unified.impl.core.CommonHelpers;
import net.rebel459.unified.impl.platform.PlatformLoader;

public class UnifiedHelpers {

    public static CommonHelpers.DataPacks DATA_PACKS = PlatformLoader.INSTANCE.getDataPacks();
    public static CommonHelpers.Networking NETWORKING = PlatformLoader.INSTANCE.getNetworking();
    public static CommonHelpers.BlockConversions BLOCK_CONVERSIONS = new CommonHelpers.BlockConversions() {};
    public static CommonHelpers.ReloadListeners RELOAD_LISTENERS = PlatformLoader.INSTANCE.getReloadListeners();
    public static CommonHelpers.DataRegistries DATA_REGISTRIES = PlatformLoader.INSTANCE.getDataRegistries();
    public static CommonHelpers.EntityDataSerializers ENTITY_DATA_SERIALIZERS = PlatformLoader.INSTANCE.getEntityData();
    public static CommonHelpers.SpawnPlacements SPAWN_PLACEMENTS = PlatformLoader.INSTANCE.getSpawnPlacements();
}