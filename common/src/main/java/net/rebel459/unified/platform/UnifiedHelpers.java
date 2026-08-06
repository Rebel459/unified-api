package net.rebel459.unified.platform;

import net.rebel459.unified.util.helper.BlockConversions;
import net.rebel459.unified.util.helper.DataComponents;
import net.rebel459.unified.util.helper.StructureMusic;

public class UnifiedHelpers {

    public static HelpersImpl.CreativeEntries CREATIVE_ENTRIES = InternalHandlerImpl.INSTANCE.getCreativeEntries();
    public static HelpersImpl.Packs PACKS = InternalHandlerImpl.INSTANCE.getPacks();
    public static HelpersImpl.Networking NETWORKING = InternalHandlerImpl.INSTANCE.getNetworking();
    public static BlockConversions BLOCK_CONVERSIONS = new BlockConversions() {};
    public static DataComponents DATA_COMPONENTS = new DataComponents() {};
    public static HelpersImpl.BiomeModifications BIOME_MODIFICATIONS = InternalHandlerImpl.INSTANCE.getBiomeModifications();
    public static StructureMusic STRUCTURE_MUSIC = new StructureMusic() {};
    public static HelpersImpl.ReloadListeners RELOAD_LISTENERS = InternalHandlerImpl.INSTANCE.getReloadListeners();
}