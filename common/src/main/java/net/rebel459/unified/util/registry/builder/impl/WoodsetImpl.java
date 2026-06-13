package net.rebel459.unified.util.registry.builder.impl;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.rebel459.unified.platform.UnifiedHelpers;
import net.rebel459.unified.util.CreativeModeTabs;
import net.rebel459.unified.util.registry.SuppliedBlock;
import net.rebel459.unified.util.registry.builder.Woodset;

import java.util.List;

public class WoodsetImpl {
    
    public static void init(List<Woodset> woodsets) {
        creativeEntries(woodsets);
        for (Woodset woodset : woodsets) {
            registerBlockProperties(woodset);
        }
    }

    public static void registerBlockProperties(Woodset woodset) {
        UnifiedHelpers.BLOCK_CONVERSIONS.addStrippable(woodset.getLog(), woodset.getStrippedLog());

        if (woodset.hasWood()){
            UnifiedHelpers.BLOCK_CONVERSIONS.addStrippable(woodset.getWood(), woodset.getStrippedWood());
        }

        if (woodset.hasLeaves()) UnifiedHelpers.DATA_COMPONENTS.addCompost(woodset.getLeaves(), 0.3F);
        if (woodset.hasSapling()) UnifiedHelpers.DATA_COMPONENTS.addCompost(woodset.getSapling(), 0.3F);

        if (woodset.getWoodsetSettings().canBurn()) {
            addFlammable(woodset.getLog(), 5, 5);
            addFlammable(woodset.getStrippedLog(), 5, 5);

            if (woodset.hasWood()) {
                addFlammable(woodset.getWood(), 5, 5);
                addFlammable(woodset.getStrippedWood(), 5, 5);
            }
            if (woodset.hasMosaic()) {
                addFlammable(woodset.getMosaic(), 5, 20);
                addFlammable(woodset.getMosaicStairs(), 5, 20);
                addFlammable(woodset.getMosaicSlab(), 5, 20);
            }
            if (woodset.hasLeaves()) {
                addFlammable(woodset.getLeaves(), 30, 60);
            }

            addFlammable(woodset.getPlanks(), 5, 20);
            addFlammable(woodset.getSlab(), 5, 20);
            addFlammable(woodset.getStairs(), 5, 20);
            addFlammable(woodset.getFence(), 5, 20);
            addFlammable(woodset.getFenceGate(), 5, 20);

            addFlammable(woodset.getSign(), 5, 20);
            addFlammable(woodset.getWallSign(), 5, 20);

            addFlammable(woodset.getHangingSign(), 5, 20);
            addFlammable(woodset.getWallHangingSign(), 5, 20);
            addFlammable(woodset.getShelf(), 30, 20);

            UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodset.getLog(), 300);
            UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodset.getStrippedLog(), 300);
            if (woodset.hasMosaic()) {
                UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodset.getMosaic(), 300);
                UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodset.getMosaicSlab(), 150);
                UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodset.getMosaicStairs(), 300);
            }
            if (woodset.hasWood()) {
                UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodset.getWood(), 300);
                UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodset.getStrippedWood(), 300);
            }
            UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodset.getPressurePlate(), 300);
            UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodset.getButton(), 100);
            UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodset.getTrapdoor(), 300);
            UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodset.getDoor(), 300);
            UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodset.getFence(), 300);
            UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodset.getFenceGate(), 300);
            UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodset.getSignItem(), 300);
            UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodset.getHangingSignItem(), 800);
            UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodset.getShelf(), 300);

            if (woodset.hasBoats()){
                UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodset.getBoatItem(), 1200);
                UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodset.getChestBoatItem(), 1200);
            }
        }
    }

    private static void addFlammable(SuppliedBlock block, int burn, int spread){
        ((FireBlock) Blocks.FIRE).setFlammable(block.get(), burn, spread);
    }

    private static void creativeEntries(List<Woodset> woodsets) {
        for (Woodset woodset : woodsets) {
            Woodset.PrecedingCreativeEntries precedingItems = Woodset.WOODSET_CREATIVE_ENTRIES.get(woodset);
            if (precedingItems == null) continue;

            UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabs.BUILDING_BLOCKS,
                    precedingItems.building(),
                    woodset.getPlanks(),
                    woodset.getStairs(),
                    woodset.getSlab(),
                    woodset.getFence(),
                    woodset.getFenceGate(),
                    woodset.getDoor(),
                    woodset.getTrapdoor(),
                    woodset.getPressurePlate(),
                    woodset.getButton()
            );
            if (woodset.hasMosaic()) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabs.BUILDING_BLOCKS, precedingItems.building(), woodset.getMosaic(), woodset.getMosaicStairs(), woodset.getMosaicSlab());
            if (woodset.hasWood()) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabs.BUILDING_BLOCKS, precedingItems.building(), woodset.getLog(), woodset.getWood(), woodset.getStrippedLog(), woodset.getStrippedWood());
            else UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabs.BUILDING_BLOCKS, precedingItems.building(), woodset.getLog(), woodset.getStrippedLog());

            if (woodset.hasLeaves() && woodset.hasSapling()) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabs.NATURAL_BLOCKS, precedingItems.natural(), woodset.getLeaves(), woodset.getSapling().asItem());
            else if (woodset.hasLeaves()) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabs.NATURAL_BLOCKS, precedingItems.natural(), woodset.getLeaves());
            else if (woodset.hasSapling()) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabs.NATURAL_BLOCKS, precedingItems.natural(), woodset.getSapling().asItem());

            UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabs.FUNCTIONAL_BLOCKS, precedingItems.functional(), woodset.getShelf(), woodset.getSignItem(), woodset.getHangingSignItem());

            UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabs.TOOLS_AND_UTILITIES, precedingItems.utilities(), woodset.getBoatItem(), woodset.getChestBoatItem());
        }
    }
}
