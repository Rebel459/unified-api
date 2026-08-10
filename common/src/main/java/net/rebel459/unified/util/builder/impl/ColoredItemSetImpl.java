package net.rebel459.unified.util.builder.impl;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.rebel459.unified.platform.UnifiedHelpers;
import net.rebel459.unified.util.builder.ColoredItemSet;
import net.rebel459.unified.util.builder.EquipmentSet;
import net.rebel459.unified.util.registry.SuppliedItem;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.Function;

public class ColoredItemSetImpl {

    public static Map<Identifier, ColoredItemSet.PrecedingCreativeEntries> CREATIVE_ENTRIES = Collections.synchronizedMap(new HashMap<>());

    public static Map<Identifier, List<Pair<Supplier<? extends DataComponentType<?>>, ?>>> COMPONENTS = Collections.synchronizedMap(new HashMap<>());
    public static Map<Identifier, List<Pair<Supplier<? extends DataComponentType<?>>, Function<DyeColor, ?>>>> DYED_COMPONENTS = Collections.synchronizedMap(new HashMap<>());
    public static Map<Identifier, List<Pair<Supplier<? extends DataComponentType<?>>, DataComponentInitializers.SingleComponentInitializer<?>>>> PROVIDED_COMPONENTS = Collections.synchronizedMap(new HashMap<>());
    public static Map<Identifier, List<Pair<Supplier<? extends DataComponentType<?>>, ResourceKey<?>>>> KEYED_COMPONENTS = Collections.synchronizedMap(new HashMap<>());

    public static void init(List<ColoredItemSet> coloredItemSets) {
        creativeEntries(coloredItemSets);
        components(coloredItemSets);
    }

    private static <T, Y> void components(List<ColoredItemSet> coloredItemSets) {
        for (ColoredItemSet coloredItemSet : coloredItemSets) {
            var components = COMPONENTS.get(coloredItemSet.getId());
            if (components != null) for (Pair<Supplier<? extends DataComponentType<?>>, ?> entry : components) {
                for (SuppliedItem item : coloredItemSet.getRegisteredItems()) {
                    UnifiedHelpers.DATA_COMPONENTS.add(item, (DataComponentType<T>) entry.getFirst().get(), (T) entry.getSecond());
                }
            }
            var dyedComponents = DYED_COMPONENTS.get(coloredItemSet.getId());
            if (dyedComponents != null) for (Pair<Supplier<? extends DataComponentType<?>>, Function<DyeColor, ?>> entry : dyedComponents) {
                for (SuppliedItem item : coloredItemSet.getRegisteredItems()) {
                    DyeColor dye = coloredItemSet.getDyeFromItem(item);
                    UnifiedHelpers.DATA_COMPONENTS.add(item, (DataComponentType<T>) entry.getFirst().get(), (T) entry.getSecond().apply(dye));
                }
            }
            var providedComponents = PROVIDED_COMPONENTS.get(coloredItemSet.getId());
            if (providedComponents != null) for (Pair<Supplier<? extends DataComponentType<?>>, DataComponentInitializers.SingleComponentInitializer<?>> entry : providedComponents) {
                for (SuppliedItem item : coloredItemSet.getRegisteredItems()) {
                    UnifiedHelpers.DATA_COMPONENTS.addWithProvider(item, (DataComponentType<T>) entry.getFirst().get(), (DataComponentInitializers.SingleComponentInitializer<T>) entry.getSecond());
                }
            }
            var keyedComponents = KEYED_COMPONENTS.get(coloredItemSet.getId());
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
