package net.rebel459.unified.impl.builder;

import net.minecraft.resources.Identifier;
import net.rebel459.unified.api.core.UnifiedHelpers;
import net.rebel459.unified.api.util.CreativeModeTabIds;
import net.rebel459.unified.api.builder.BlockSet;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockSetImpl {

    public static Map<Identifier, BlockSet.PrecedingCreativeEntries> CREATIVE_ENTRIES = Collections.synchronizedMap(new HashMap<>());

    public static void init(List<BlockSet> blockSets) {
        creativeEntries(blockSets);
    }

    private static void creativeEntries(List<BlockSet> blockSets) {
        for (BlockSet blockSet : blockSets) {
            BlockSet.PrecedingCreativeEntries precedingItems = CREATIVE_ENTRIES.get(blockSet.getId());
            if (precedingItems == null) continue;

            if (blockSet.hasButton()) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabIds.BUILDING_BLOCKS, precedingItems.building().get(), blockSet.getButton());
            if (blockSet.hasPressurePlate()) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabIds.BUILDING_BLOCKS, precedingItems.building().get(), blockSet.getPressurePlate());
            if (blockSet.hasChiseled()) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabIds.BUILDING_BLOCKS, precedingItems.building().get(), blockSet.getChiseled());
            if (blockSet.hasFence()) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabIds.BUILDING_BLOCKS, precedingItems.building().get(), blockSet.getFence());
            if (blockSet.hasWall()) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabIds.BUILDING_BLOCKS, precedingItems.building().get(), blockSet.getWall());
            if (blockSet.hasSlab()) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabIds.BUILDING_BLOCKS, precedingItems.building().get(), blockSet.getSlab());
            if (blockSet.hasStairs()) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabIds.BUILDING_BLOCKS, precedingItems.building().get(), blockSet.getStairs());
            if (blockSet.hasPillar()) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabIds.BUILDING_BLOCKS, precedingItems.building().get(), blockSet.getPillar());
            if (blockSet.hasCracked()) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabIds.BUILDING_BLOCKS, precedingItems.building().get(), blockSet.getCracked());
            UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabIds.BUILDING_BLOCKS, precedingItems.building().get(), blockSet.getBase());

            if (precedingItems.natural() != null) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabIds.NATURAL_BLOCKS, precedingItems.natural().get(), blockSet.getBase());
        }
    }
}
