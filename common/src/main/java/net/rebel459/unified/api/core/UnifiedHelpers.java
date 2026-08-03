package net.rebel459.unified.api.core;

import net.rebel459.unified.impl.core.HelpersImpl;
import net.rebel459.unified.impl.platform.InternalHandlerImpl;

public class UnifiedHelpers {

    public static HelpersImpl.CreativeEntries CREATIVE_ENTRIES = InternalHandlerImpl.INSTANCE.getCreativeEntries();
    public static HelpersImpl.DataPacks DATA_PACKS = InternalHandlerImpl.INSTANCE.getDataPacks();
    public static HelpersImpl.Networking NETWORKING = InternalHandlerImpl.INSTANCE.getNetworking();
    public static HelpersImpl.BlockConversions BLOCK_CONVERSIONS = new HelpersImpl.BlockConversions() {};
    public static HelpersImpl.DataComponents DATA_COMPONENTS = new HelpersImpl.DataComponents() {};
    public static HelpersImpl.BiomeModifications BIOME_MODIFICATIONS = InternalHandlerImpl.INSTANCE.getBiomeModifications();
    public static HelpersImpl.StructureMusic STRUCTURE_MUSIC = new HelpersImpl.StructureMusic() {};
}