package net.rebel459.unified.impl.data.set;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTabs;
import net.rebel459.unified.api.data.set.ColoredBlockSet;
import net.rebel459.unified.api.data.helper.CreativeEntryGenerator;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColoredBlockSetProperties {

    public static Map<Identifier, ColoredBlockSet.PrecedingCreativeEntries> CREATIVE_ENTRIES = Collections.synchronizedMap(new HashMap<>());
    public static Map<Identifier, CreativeEntryGenerator> CREATIVE_ENTRY_GENERATORS = Collections.synchronizedMap(new HashMap<>());

    public static void init(List<ColoredBlockSet> coloredBlockSets) {
        creativeEntries(coloredBlockSets);
    }

    private static void creativeEntries(List<ColoredBlockSet> coloredBlockSets) {
        for (ColoredBlockSet coloredBlockSet : coloredBlockSets) {
            ColoredBlockSet.PrecedingCreativeEntries entries = CREATIVE_ENTRIES.get(coloredBlockSet.getId());
            CreativeEntryGenerator generator = CREATIVE_ENTRY_GENERATORS.get(coloredBlockSet.getId());
            if (entries == null || generator == null) continue;
            CreativeEntryGenerator.Builder builder = generator.create("colored_block_set/" + coloredBlockSet.getId().getPath());
            builder.insertAfter(
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
                builder.insertAfter(
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
