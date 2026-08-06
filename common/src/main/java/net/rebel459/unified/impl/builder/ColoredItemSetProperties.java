package net.rebel459.unified.impl.builder;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.rebel459.unified.api.builder.ColoredItemSet;
import net.rebel459.unified.api.core.SuppliedItem;
import net.rebel459.unified.api.core.UnifiedHelpers;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ColoredItemSetProperties {

    public static Map<Identifier, ColoredItemSet.PrecedingCreativeEntries> CREATIVE_ENTRIES = Collections.synchronizedMap(new HashMap<>());

    public static Map<Identifier, List<Pair<Supplier<? extends DataComponentType<?>>, ?>>> EQUIPMENT_COMPONENTS = Collections.synchronizedMap(new HashMap<>());
    public static Map<Identifier, List<Pair<Supplier<? extends DataComponentType<?>>, DataComponentInitializers.SingleComponentInitializer<?>>>> EQUIPMENT_PROVIDED_COMPONENTS = Collections.synchronizedMap(new HashMap<>());
    public static Map<Identifier, List<Pair<Supplier<? extends DataComponentType<?>>, ResourceKey<?>>>> EQUIPMENT_KEYED_COMPONENTS = Collections.synchronizedMap(new HashMap<>());

    public static void init(List<ColoredItemSet> coloredItemSets) {
        creativeEntries(coloredItemSets);
        components(coloredItemSets);
    }

    private static <T, Y> void components(List<ColoredItemSet> coloredItemSets) {
        for (ColoredItemSet coloredItemSet : coloredItemSets) {
            var components = EQUIPMENT_COMPONENTS.get(coloredItemSet.getId());
            if (components != null) for (Pair<Supplier<? extends DataComponentType<?>>, ?> entry : components) {
                for (SuppliedItem item : coloredItemSet.getRegisteredItems()) {
                    UnifiedHelpers.DATA_COMPONENTS.add(item, (DataComponentType<T>) entry.getFirst().get(), (T) entry.getSecond());
                }
            }
            var providedComponents = EQUIPMENT_PROVIDED_COMPONENTS.get(coloredItemSet.getId());
            if (providedComponents != null) for (Pair<Supplier<? extends DataComponentType<?>>, DataComponentInitializers.SingleComponentInitializer<?>> entry : providedComponents) {
                for (SuppliedItem item : coloredItemSet.getRegisteredItems()) {
                    UnifiedHelpers.DATA_COMPONENTS.addWithProvider(item, (DataComponentType<T>) entry.getFirst().get(), (DataComponentInitializers.SingleComponentInitializer<T>) entry.getSecond());
                }
            }
            var keyedComponents = EQUIPMENT_KEYED_COMPONENTS.get(coloredItemSet.getId());
            if (keyedComponents != null) for (Pair<Supplier<? extends DataComponentType<?>>, ResourceKey<?>> entry : keyedComponents) {
                for (SuppliedItem item : coloredItemSet.getRegisteredItems()) {
                    UnifiedHelpers.DATA_COMPONENTS.addWithKey(item, (DataComponentType<Holder<Y>>) entry.getFirst().get(), (ResourceKey<Y>) entry.getSecond());
                }
            }
        }
    }

    private static void creativeEntries(List<ColoredItemSet> coloredItemSets) {
        for (ColoredItemSet coloredItemSet : coloredItemSets) {
            ColoredItemSet.PrecedingCreativeEntries entries = CREATIVE_ENTRIES.get(coloredItemSet.getId());
            if (entries == null) continue;
            UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(
                    entries.firstTab().getFirst(),
                    entries.firstTab().getSecond().get(),
                    coloredItemSet.getWhite(),
                    coloredItemSet.getLightGray(),
                    coloredItemSet.getGray(),
                    coloredItemSet.getBlack(),
                    coloredItemSet.getBrown(),
                    coloredItemSet.getRed(),
                    coloredItemSet.getOrange(),
                    coloredItemSet.getYellow(),
                    coloredItemSet.getLime(),
                    coloredItemSet.getGreen(),
                    coloredItemSet.getCyan(),
                    coloredItemSet.getLightBlue(),
                    coloredItemSet.getBlue(),
                    coloredItemSet.getPurple(),
                    coloredItemSet.getMagenta(),
                    coloredItemSet.getPink()
            );
            if (entries.secondTab() != null) {
                UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(
                        entries.secondTab().getFirst(),
                        entries.secondTab().getSecond().get(),
                        coloredItemSet.getWhite(),
                        coloredItemSet.getLightGray(),
                        coloredItemSet.getGray(),
                        coloredItemSet.getBlack(),
                        coloredItemSet.getBrown(),
                        coloredItemSet.getRed(),
                        coloredItemSet.getOrange(),
                        coloredItemSet.getYellow(),
                        coloredItemSet.getLime(),
                        coloredItemSet.getGreen(),
                        coloredItemSet.getCyan(),
                        coloredItemSet.getLightBlue(),
                        coloredItemSet.getBlue(),
                        coloredItemSet.getPurple(),
                        coloredItemSet.getMagenta(),
                        coloredItemSet.getPink()
                );
            }
        }
    }
}