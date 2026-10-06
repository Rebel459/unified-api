package net.rebel459.unified.impl.data.set;

import com.mojang.datafixers.util.Either;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ItemLike;
import net.rebel459.unified.api.data.set.WoodSet;
import net.rebel459.unified.api.data.helper.BlockConversionGenerator;
import net.rebel459.unified.api.data.helper.CreativeEntryGenerator;
import net.rebel459.unified.api.registry.CreativeModeTabIds;

import java.util.*;
import java.util.function.Supplier;

public class WoodSetProperties {

    public static Map<Identifier, WoodSet.PrecedingCreativeEntries> CREATIVE_ENTRIES = Collections.synchronizedMap(new HashMap<>());
    public static Map<Identifier, CreativeEntryGenerator> CREATIVE_ENTRY_GENERATORS = Collections.synchronizedMap(new HashMap<>());
    public static Map<Identifier, BlockConversionGenerator> BLOCK_CONVERSION_GENERATORS = Collections.synchronizedMap(new HashMap<>());
    public static Map<Identifier, Supplier<? extends ItemLike>> SAPLING_CREATIVE_ENTRIES = Collections.synchronizedMap(new HashMap<>());
    public static Map<Identifier, Either<String, Supplier<? extends ItemLike>>> LEAVES_CREATIVE_ENTRIES = Collections.synchronizedMap(new HashMap<>());

    public static void init(List<WoodSet> woodSets) {
        creativeEntries(woodSets);
        blockConversions(woodSets);
    }

    public static void blockConversions(List<WoodSet> woodSets) {
        for (WoodSet woodSet : woodSets) {
            BlockConversionGenerator generator = BLOCK_CONVERSION_GENERATORS.get(woodSet.getId());
            if (generator == null) continue;
            generator.addStrippable("wood_set/" + woodSet.getLog().id().getPath() + "_to_" + woodSet.getStrippedLog().id().getPath(), woodSet.getLog(), woodSet.getStrippedLog());
            if (woodSet.hasWood()) generator.addStrippable("wood_set/" + woodSet.getWood().id().getPath() + "_to_" + woodSet.getStrippedWood().id().getPath(), woodSet.getWood(), woodSet.getStrippedWood());
        }
    }

    private static void creativeEntries(List<WoodSet> woodSets) {
        for (WoodSet woodSet : woodSets) {
            WoodSet.PrecedingCreativeEntries precedingItems = CREATIVE_ENTRIES.get(woodSet.getId());
            CreativeEntryGenerator generator = CREATIVE_ENTRY_GENERATORS.get(woodSet.getId());
            if (precedingItems == null || generator == null) continue;
            CreativeEntryGenerator.Builder builder = generator.create("wood_set/" + woodSet.getId().getPath());

            builder.insertAfter(CreativeModeTabIds.BUILDING_BLOCKS,
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
            if (woodSet.hasMosaic()) builder.insertAfter(CreativeModeTabIds.BUILDING_BLOCKS, precedingItems.building().get(), woodSet.getMosaic(), woodSet.getMosaicStairs(), woodSet.getMosaicSlab());
            if (woodSet.hasWood()) builder.insertAfter(CreativeModeTabIds.BUILDING_BLOCKS, precedingItems.building().get(), woodSet.getLog(), woodSet.getWood(), woodSet.getStrippedLog(), woodSet.getStrippedWood());
            else builder.insertAfter(CreativeModeTabIds.BUILDING_BLOCKS, precedingItems.building().get(), woodSet.getLog(), woodSet.getStrippedLog());

            builder.insertAfter(CreativeModeTabIds.NATURAL_BLOCKS, precedingItems.natural().get(), woodSet.getLog());

            if (woodSet.hasAnyLeaves()) {
                Either<String, Supplier<? extends ItemLike>> item = LEAVES_CREATIVE_ENTRIES.get(woodSet.getId());
                if (item != null) {
                    List<WoodSet.Leaves> reversed = new ArrayList<>(woodSet.getAllLeaves());
                    Collections.reverse(reversed);
                    Set<WoodSet.Leaves> reversedSet = new LinkedHashSet<>(reversed);
                    for (WoodSet.Leaves leaves : reversedSet) {
                        if (item.left().isPresent()) builder.insertAfter(CreativeModeTabIds.NATURAL_BLOCKS, woodSet.getLeavesVariant(item.left().get()), woodSet.getLeavesVariant(leaves));
                        if (item.right().isPresent()) builder.insertAfter(CreativeModeTabIds.NATURAL_BLOCKS, item.right().get().get(), woodSet.getLeavesVariant(leaves));
                    }
                }
            }
            if (woodSet.hasSapling()) {
                Supplier<? extends ItemLike> item = SAPLING_CREATIVE_ENTRIES.get(woodSet.getId());
                if (item != null) builder.insertAfter(CreativeModeTabIds.NATURAL_BLOCKS, item.get(), woodSet.getSapling());
            }
            builder.insertAfter(CreativeModeTabIds.FUNCTIONAL_BLOCKS, precedingItems.functionalShelf().get(), woodSet.getShelf());
            builder.insertAfter(CreativeModeTabIds.FUNCTIONAL_BLOCKS, precedingItems.functionalSign().get(), woodSet.getSignItem(), woodSet.getHangingSignItem());

            if (precedingItems.utilities() != null && woodSet.hasBoats()) builder.insertAfter(CreativeModeTabIds.TOOLS_AND_UTILITIES, precedingItems.utilities().get(), woodSet.getBoatItem(), woodSet.getChestBoatItem());
        }
    }
}
