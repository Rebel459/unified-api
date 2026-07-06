package net.rebel459.unified.util.registry.builder.impl;

import net.rebel459.unified.platform.UnifiedHelpers;
import net.rebel459.unified.util.CreativeModeTabs;
import net.rebel459.unified.util.registry.builder.BlockSet;

import java.util.List;

public class BlockSetImpl {
    
    public static void init(List<BlockSet> blockSets) {
        creativeEntries(blockSets);
    }

    private static void creativeEntries(List<BlockSet> blockSets) {
        for (BlockSet blockSet : blockSets) {
            BlockSet.PrecedingCreativeEntries precedingItems = BlockSet.BLOCKSET_CREATIVE_ENTRIES.get(blockSet);
            if (precedingItems == null) continue;

            if (blockSet.hasButton()) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabs.BUILDING_BLOCKS, precedingItems.building().get(), blockSet.getButton());
            if (blockSet.hasPressurePlate()) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabs.BUILDING_BLOCKS, precedingItems.building().get(), blockSet.getPressurePlate());
            if (blockSet.hasChiseled()) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabs.BUILDING_BLOCKS, precedingItems.building().get(), blockSet.getChiseled());
            if (blockSet.hasFence()) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabs.BUILDING_BLOCKS, precedingItems.building().get(), blockSet.getFence());
            if (blockSet.hasWall()) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabs.BUILDING_BLOCKS, precedingItems.building().get(), blockSet.getWall());
            if (blockSet.hasSlab()) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabs.BUILDING_BLOCKS, precedingItems.building().get(), blockSet.getSlab());
            if (blockSet.hasStairs()) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabs.BUILDING_BLOCKS, precedingItems.building().get(), blockSet.getStairs());
            if (blockSet.hasPillar()) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabs.BUILDING_BLOCKS, precedingItems.building().get(), blockSet.getPillar());
            if (blockSet.hasCracked()) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabs.BUILDING_BLOCKS, precedingItems.building().get(), blockSet.getCracked());
            UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabs.BUILDING_BLOCKS, precedingItems.building().get(), blockSet.getBase());

            if (precedingItems.natural() != null) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabs.NATURAL_BLOCKS, precedingItems.natural().get(), blockSet.getBase());
        }
    }
}
