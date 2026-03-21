package net.rebel459.unified.platform;

import net.rebel459.unified.util.BlockConversions;
import net.rebel459.unified.util.DataComponents;

public class UnifiedHelpers {

    public static HelpersImpl.Platform PLATFORM = PlatformHelperImpl.INSTANCE.getPlatform();
    public static HelpersImpl.CreativeEntries CREATIVE_ENTRIES = PlatformHelperImpl.INSTANCE.getCreativeEntries();
    public static HelpersImpl.Packs PACKS = PlatformHelperImpl.INSTANCE.getPacks();
    public static HelpersImpl.Networking NETWORKING = PlatformHelperImpl.INSTANCE.getNetworkPayloads();
    public static BlockConversions BLOCK_CONVERSIONS = new BlockConversions() {};
    public static DataComponents DATA_COMPONENTS = new DataComponents() {};
    public static HelpersImpl.BiomeModifications BIOME_MODIFICATIONS = PlatformHelperImpl.INSTANCE.getBiomeModifications();
}