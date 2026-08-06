package net.rebel459.unified.util.builder.impl;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.rebel459.unified.platform.UnifiedHelpers;
import net.rebel459.unified.util.CreativeModeTabs;
import net.rebel459.unified.util.builder.BlockSet;
import net.rebel459.unified.util.builder.ColoredBlockSet;
import net.rebel459.unified.util.registry.SuppliedBlock;

import java.util.*;

public class ColoredBlockSetImpl {

    public static Map<Identifier, ColoredBlockSet.PrecedingCreativeEntries> CREATIVE_ENTRIES = Collections.synchronizedMap(new HashMap<>());

    public static void init(List<ColoredBlockSet> coloredBlockSets) {
        creativeEntries(coloredBlockSets);
        flammability(coloredBlockSets);
    }

    private static void flammability(List<ColoredBlockSet> coloredBlockSets) {
        for (ColoredBlockSet coloredBlockSet : coloredBlockSets) {
            final FireBlock fire = (FireBlock) Blocks.FIRE;
            Pair<Integer, Integer> flammability = coloredBlockSet.getSettings().getFlammability();
            if (flammability == null) continue;
            for (SuppliedBlock block : coloredBlockSet.getRegisteredBlocks()) {
                fire.setFlammable(block.get(), flammability.getFirst(), flammability.getSecond());
            }
        }
    }

    private static void creativeEntries(List<ColoredBlockSet> coloredBlockSets) {
        for (ColoredBlockSet coloredBlockSet : coloredBlockSets) {
            ColoredBlockSet.PrecedingCreativeEntries entries = CREATIVE_ENTRIES.get(coloredBlockSet.getId());
            UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(
                    CreativeModeTabs.COLORED_BLOCKS,
                    entries.colored().get(),
                    coloredBlockSet.getWhite(),
                    coloredBlockSet.getLightGray(),
                    coloredBlockSet.getGray(),
                    coloredBlockSet.getBlack(),
                    coloredBlockSet.getBrown(),
                    coloredBlockSet.getRed(),
                    coloredBlockSet.getOrange(),
                    coloredBlockSet.getYellow(),
                    coloredBlockSet.getLime(),
                    coloredBlockSet.getGreen(),
                    coloredBlockSet.getCyan(),
                    coloredBlockSet.getLightBlue(),
                    coloredBlockSet.getBlue(),
                    coloredBlockSet.getPurple(),
                    coloredBlockSet.getMagenta(),
                    coloredBlockSet.getPink()
            );
            if (entries.secondTab() != null) {
                UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(
                        entries.secondTab().getFirst(),
                        entries.secondTab().getSecond().get(),
                        coloredBlockSet.getWhite(),
                        coloredBlockSet.getLightGray(),
                        coloredBlockSet.getGray(),
                        coloredBlockSet.getBlack(),
                        coloredBlockSet.getBrown(),
                        coloredBlockSet.getRed(),
                        coloredBlockSet.getOrange(),
                        coloredBlockSet.getYellow(),
                        coloredBlockSet.getLime(),
                        coloredBlockSet.getGreen(),
                        coloredBlockSet.getCyan(),
                        coloredBlockSet.getLightBlue(),
                        coloredBlockSet.getBlue(),
                        coloredBlockSet.getPurple(),
                        coloredBlockSet.getMagenta(),
                        coloredBlockSet.getPink()
                );
            }
        }
    }
}
