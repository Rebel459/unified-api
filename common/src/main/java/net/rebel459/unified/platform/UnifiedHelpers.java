package net.rebel459.unified.platform;

import java.util.ServiceLoader;

public class UnifiedHelpers {

    public static HelpersImpl.Platform PLATFORM = Factory.getHelpers().createPlatform();
    public static HelpersImpl.Packs PACKS = Factory.getHelpers().createPacks();
    public static HelpersImpl.CreativeEntries CREATIVE_ENTRIES = Factory.getHelpers().createCreativeEntries();
    public static HelpersImpl.NetworkPayloads NETWORK_PAYLOADS = Factory.getHelpers().createNetworkPayloads();
    public static HelpersImpl.FurnaceFuels FURNACE_FUELS = Factory.getHelpers().createFurnaceFuels();
    public static HelpersImpl.LootTables LOOT_TABLES = Factory.getHelpers().createLootTables();
    public static HelpersImpl.StrippableBlocks STRIPPABLE_BLOCKS = Factory.getHelpers().createStrippableBlocks();

}