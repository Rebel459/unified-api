package net.rebel459.unified.impl.builder;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.rebel459.unified.api.core.UnifiedHelpers;
import net.rebel459.unified.api.core.SuppliedBlock;
import net.rebel459.unified.api.builder.WoodSet;
import net.rebel459.unified.api.registry.UnifiedCreativeModeTabs;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class WoodSetProperties {

    public static Map<Identifier, WoodSet.PrecedingCreativeEntries> CREATIVE_ENTRIES = Collections.synchronizedMap(new HashMap<>());
    public static Map<Identifier, Supplier<? extends ItemLike>> SAPLING_CREATIVE_ENTRIES = Collections.synchronizedMap(new HashMap<>());
    public static Map<Identifier, Supplier<? extends ItemLike>> LEAF_CREATIVE_ENTRIES = Collections.synchronizedMap(new HashMap<>());

    public static void init(List<WoodSet> woodSets) {
        creativeEntries(woodSets);
        for (WoodSet woodset : woodSets) {
            registerBlockProperties(woodset);
        }
    }

    public static void registerBlockProperties(WoodSet woodSets) {
        UnifiedHelpers.BLOCK_CONVERSIONS.addStrippable(woodSets.getLog(), woodSets.getStrippedLog());

        if (woodSets.hasWood()){
            UnifiedHelpers.BLOCK_CONVERSIONS.addStrippable(woodSets.getWood(), woodSets.getStrippedWood());
        }

        if (woodSets.hasLeaves()) UnifiedHelpers.DATA_COMPONENTS.addCompost(woodSets.getLeaves(), 0.3F);
        if (woodSets.hasSapling()) UnifiedHelpers.DATA_COMPONENTS.addCompost(woodSets.getSapling(), 0.3F);

        if (woodSets.getSettings().isFlammable()) {
            addFlammable(woodSets.getLog(), 5, 5);
            addFlammable(woodSets.getStrippedLog(), 5, 5);

            if (woodSets.hasWood()) {
                addFlammable(woodSets.getWood(), 5, 5);
                addFlammable(woodSets.getStrippedWood(), 5, 5);
            }
            if (woodSets.hasMosaic()) {
                addFlammable(woodSets.getMosaic(), 5, 20);
                addFlammable(woodSets.getMosaicStairs(), 5, 20);
                addFlammable(woodSets.getMosaicSlab(), 5, 20);
            }
            if (woodSets.hasLeaves()) {
                addFlammable(woodSets.getLeaves(), 30, 60);
            }

            addFlammable(woodSets.getPlanks(), 5, 20);
            addFlammable(woodSets.getSlab(), 5, 20);
            addFlammable(woodSets.getStairs(), 5, 20);
            addFlammable(woodSets.getFence(), 5, 20);
            addFlammable(woodSets.getFenceGate(), 5, 20);

            addFlammable(woodSets.getSign(), 5, 20);
            addFlammable(woodSets.getWallSign(), 5, 20);

            addFlammable(woodSets.getHangingSign(), 5, 20);
            addFlammable(woodSets.getWallHangingSign(), 5, 20);
            addFlammable(woodSets.getShelf(), 30, 20);

            UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodSets.getLog(), 300);
            UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodSets.getStrippedLog(), 300);
            if (woodSets.hasMosaic()) {
                UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodSets.getMosaic(), 300);
                UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodSets.getMosaicSlab(), 150);
                UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodSets.getMosaicStairs(), 300);
            }
            if (woodSets.hasWood()) {
                UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodSets.getWood(), 300);
                UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodSets.getStrippedWood(), 300);
            }
            UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodSets.getPressurePlate(), 300);
            UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodSets.getButton(), 100);
            UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodSets.getTrapdoor(), 300);
            UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodSets.getDoor(), 300);
            UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodSets.getFence(), 300);
            UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodSets.getFenceGate(), 300);
            UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodSets.getSignItem(), 300);
            UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodSets.getHangingSignItem(), 800);
            UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodSets.getShelf(), 300);

            if (woodSets.hasBoats()){
                UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodSets.getBoatItem(), 1200);
                UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodSets.getChestBoatItem(), 1200);
            }
        }
    }

    private static void addFlammable(SuppliedBlock block, int burn, int spread){
        ((FireBlock) Blocks.FIRE).setFlammable(block.get(), burn, spread);
    }

    private static void creativeEntries(List<WoodSet> woodSets) {
        for (WoodSet woodSet : woodSets) {
            WoodSet.PrecedingCreativeEntries precedingItems = CREATIVE_ENTRIES.get(woodSet.getId());
            if (precedingItems == null) continue;

            UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(UnifiedCreativeModeTabs.BUILDING_BLOCKS,
                    precedingItems.building().get(),
                    woodSet.getPlanks(),
                    woodSet.getStairs(),
                    woodSet.getSlab(),
                    woodSet.getFence(),
                    woodSet.getFenceGate(),
                    woodSet.getDoor(),
                    woodSet.getTrapdoor(),
                    woodSet.getPressurePlate(),
                    woodSet.getButton()
            );
            if (woodSet.hasMosaic()) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(UnifiedCreativeModeTabs.BUILDING_BLOCKS, precedingItems.building().get(), woodSet.getMosaic(), woodSet.getMosaicStairs(), woodSet.getMosaicSlab());
            if (woodSet.hasWood()) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(UnifiedCreativeModeTabs.BUILDING_BLOCKS, precedingItems.building().get(), woodSet.getLog(), woodSet.getWood(), woodSet.getStrippedLog(), woodSet.getStrippedWood());
            else UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(UnifiedCreativeModeTabs.BUILDING_BLOCKS, precedingItems.building().get(), woodSet.getLog(), woodSet.getStrippedLog());

            UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(UnifiedCreativeModeTabs.NATURAL_BLOCKS, precedingItems.natural().get(), woodSet.getLog());

            if (woodSet.hasLeaves()) {
                Supplier<? extends ItemLike> item = LEAF_CREATIVE_ENTRIES.get(woodSet.getId());
                if (item != null) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(UnifiedCreativeModeTabs.NATURAL_BLOCKS, item.get(), woodSet.getLeaves());
            }
            if (woodSet.hasSapling()) {
                Supplier<? extends ItemLike> item = SAPLING_CREATIVE_ENTRIES.get(woodSet.getId());
                if (item != null) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(UnifiedCreativeModeTabs.NATURAL_BLOCKS, item.get(), woodSet.getSapling());
            }
            UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(UnifiedCreativeModeTabs.FUNCTIONAL_BLOCKS, precedingItems.functionalShelf().get(), woodSet.getShelf());
            UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(UnifiedCreativeModeTabs.FUNCTIONAL_BLOCKS, precedingItems.functionalSign().get(), woodSet.getSignItem(), woodSet.getHangingSignItem());

            if (precedingItems.utilities() != null && woodSet.hasBoats()) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(UnifiedCreativeModeTabs.TOOLS_AND_UTILITIES, precedingItems.utilities().get(), woodSet.getBoatItem(), woodSet.getChestBoatItem());
        }
    }
}
