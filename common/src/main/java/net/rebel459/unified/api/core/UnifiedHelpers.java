package net.rebel459.unified.api.core;

import net.rebel459.unified.impl.core.CommonHelpers;
import net.rebel459.unified.impl.platform.PlatformHandler;

public class UnifiedHelpers {

    public static CommonHelpers.CreativeEntries CREATIVE_ENTRIES = PlatformHandler.INSTANCE.getCreativeEntries();
    public static CommonHelpers.DataPacks DATA_PACKS = PlatformHandler.INSTANCE.getDataPacks();
    public static CommonHelpers.Networking NETWORKING = PlatformHandler.INSTANCE.getNetworking();
    public static CommonHelpers.BlockConversions BLOCK_CONVERSIONS = new CommonHelpers.BlockConversions() {};
    public static CommonHelpers.DataComponents DATA_COMPONENTS = new CommonHelpers.DataComponents() {};
    public static CommonHelpers.BiomeModifications BIOME_MODIFICATIONS = PlatformHandler.INSTANCE.getBiomeModifications();
    public static CommonHelpers.StructureMusic STRUCTURE_MUSIC = new CommonHelpers.StructureMusic() {};
}