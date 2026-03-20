package net.rebel459.unified.util;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.rebel459.unified.platform.UnifiedEvents;
import net.rebel459.unified.registry.UnifiedDataComponents;

public interface DataComponents {

    default <T> void add(DataComponentMap.Builder builder, DataComponentType<T> type, T value) {
        builder.addAll(DataComponentMap.builder().set(type, value).build());
    }
    default <T> void add(Item item, DataComponentType<T> type, T value) {
        UnifiedEvents.ItemComponents.modify((testedItem, builder) -> {
            if (testedItem == item) {
                add(builder, type, value);
            }
        });
    }
    default <T> void add(Block block, DataComponentType<T> type, T value) {
        add(block.asItem(), type, value);
    }

    default void addFurnaceFuel(Item item, int ticks) {
        add(item, UnifiedDataComponents.FURNACE_FUEL.get(), ticks);
    }
    default void addFurnaceFuel(Block block, int ticks) {
        addFurnaceFuel(block.asItem(), ticks);
    }

    default void addComposting(Item item, float chance) {
        add(item, UnifiedDataComponents.COMPOST.get(), chance);
    }
    default void addComposting(Block block, float chance) {
        addComposting(block.asItem(), chance);
    }
}
