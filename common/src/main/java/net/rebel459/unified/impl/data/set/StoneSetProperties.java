package net.rebel459.unified.impl.data.set;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.rebel459.unified.api.data.set.StoneSet;
import net.rebel459.unified.api.data.helper.CreativeEntryGenerator;
import net.rebel459.unified.api.registry.CreativeModeTabIds;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StoneSetProperties {

    public static Map<Identifier, StoneSet.PrecedingCreativeEntries> CREATIVE_ENTRIES = Collections.synchronizedMap(new HashMap<>());
    public static Map<Identifier, CreativeEntryGenerator> CREATIVE_ENTRY_GENERATORS = Collections.synchronizedMap(new HashMap<>());

    public static void init(List<StoneSet> stoneSets) {
        creativeEntries(stoneSets);
        stoneSets.forEach(blockSet -> BlockSetType.register(blockSet.getBlockSetType().get()));
    }

    private static void creativeEntries(List<StoneSet> stoneSets) {
        for (StoneSet stoneSet : stoneSets) {
            StoneSet.PrecedingCreativeEntries precedingItems = CREATIVE_ENTRIES.get(stoneSet.getId());
            CreativeEntryGenerator generator = CREATIVE_ENTRY_GENERATORS.get(stoneSet.getId());
            if (precedingItems == null || generator == null) continue;
            CreativeEntryGenerator.Builder builder = generator.create("stone_set/" + stoneSet.getId().getPath());

            if (stoneSet.hasButton()) builder.insertAfter(CreativeModeTabIds.BUILDING_BLOCKS, precedingItems.building().get(), stoneSet.getButton());
            if (stoneSet.hasPressurePlate()) builder.insertAfter(CreativeModeTabIds.BUILDING_BLOCKS, precedingItems.building().get(), stoneSet.getPressurePlate());
            if (stoneSet.hasChiseled()) builder.insertAfter(CreativeModeTabIds.BUILDING_BLOCKS, precedingItems.building().get(), stoneSet.getChiseled());
            if (stoneSet.hasFence()) builder.insertAfter(CreativeModeTabIds.BUILDING_BLOCKS, precedingItems.building().get(), stoneSet.getFence());
            if (stoneSet.hasWall()) builder.insertAfter(CreativeModeTabIds.BUILDING_BLOCKS, precedingItems.building().get(), stoneSet.getWall());
            if (stoneSet.hasSlab()) builder.insertAfter(CreativeModeTabIds.BUILDING_BLOCKS, precedingItems.building().get(), stoneSet.getSlab());
            if (stoneSet.hasStairs()) builder.insertAfter(CreativeModeTabIds.BUILDING_BLOCKS, precedingItems.building().get(), stoneSet.getStairs());
            if (stoneSet.hasPillar()) builder.insertAfter(CreativeModeTabIds.BUILDING_BLOCKS, precedingItems.building().get(), stoneSet.getPillar());
            if (stoneSet.hasCracked()) builder.insertAfter(CreativeModeTabIds.BUILDING_BLOCKS, precedingItems.building().get(), stoneSet.getCracked());
            builder.insertAfter(CreativeModeTabIds.BUILDING_BLOCKS, precedingItems.building().get(), stoneSet.getBase());

            if (precedingItems.natural() != null) builder.insertAfter(CreativeModeTabIds.NATURAL_BLOCKS, precedingItems.natural().get(), stoneSet.getBase());
        }
    }
}
