package net.rebel459.unified.impl.builder;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.rebel459.unified.api.builder.StoneSet;
import net.rebel459.unified.api.core.UnifiedHelpers;
import net.rebel459.unified.api.registry.UnifiedCreativeModeTabs;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StoneSetProperties {

    public static Map<Identifier, StoneSet.PrecedingCreativeEntries> CREATIVE_ENTRIES = Collections.synchronizedMap(new HashMap<>());

    public static void init(List<StoneSet> stoneSets) {
        creativeEntries(stoneSets);
        stoneSets.forEach(blockSet -> BlockSetType.register(blockSet.getBlockSetType().get()));
    }

    private static void creativeEntries(List<StoneSet> stoneSets) {
        for (StoneSet stoneSet : stoneSets) {
            StoneSet.PrecedingCreativeEntries precedingItems = CREATIVE_ENTRIES.get(stoneSet.getId());
            if (precedingItems == null) continue;

            if (stoneSet.hasButton()) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(UnifiedCreativeModeTabs.BUILDING_BLOCKS, precedingItems.building().get(), stoneSet.getButton());
            if (stoneSet.hasPressurePlate()) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(UnifiedCreativeModeTabs.BUILDING_BLOCKS, precedingItems.building().get(), stoneSet.getPressurePlate());
            if (stoneSet.hasChiseled()) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(UnifiedCreativeModeTabs.BUILDING_BLOCKS, precedingItems.building().get(), stoneSet.getChiseled());
            if (stoneSet.hasFence()) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(UnifiedCreativeModeTabs.BUILDING_BLOCKS, precedingItems.building().get(), stoneSet.getFence());
            if (stoneSet.hasWall()) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(UnifiedCreativeModeTabs.BUILDING_BLOCKS, precedingItems.building().get(), stoneSet.getWall());
            if (stoneSet.hasSlab()) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(UnifiedCreativeModeTabs.BUILDING_BLOCKS, precedingItems.building().get(), stoneSet.getSlab());
            if (stoneSet.hasStairs()) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(UnifiedCreativeModeTabs.BUILDING_BLOCKS, precedingItems.building().get(), stoneSet.getStairs());
            if (stoneSet.hasPillar()) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(UnifiedCreativeModeTabs.BUILDING_BLOCKS, precedingItems.building().get(), stoneSet.getPillar());
            if (stoneSet.hasCracked()) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(UnifiedCreativeModeTabs.BUILDING_BLOCKS, precedingItems.building().get(), stoneSet.getCracked());
            UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(UnifiedCreativeModeTabs.BUILDING_BLOCKS, precedingItems.building().get(), stoneSet.getBase());

            if (precedingItems.natural() != null) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(UnifiedCreativeModeTabs.NATURAL_BLOCKS, precedingItems.natural().get(), stoneSet.getBase());
        }
    }
}
