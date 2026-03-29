package net.rebel459.unified.util;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.level.ItemLike;
import net.rebel459.unified.platform.UnifiedEvents;
import net.rebel459.unified.registry.UnifiedDataComponents;

public interface DataComponents {

    default <T> void add(DataComponentMap.Builder builder, DataComponentType<T> type, T value) {
        builder.addAll(DataComponentMap.builder().set(type, value).build());
    }
    default <T> void add(ItemLike itemLike, DataComponentType<T> type, T value) {
        UnifiedEvents.DefaultItemComponents.modify((testedItem, builder) -> {
            if (testedItem == itemLike.asItem()) {
                add(builder, type, value);
            }
        });
    }

    default void addFurnaceFuel(ItemLike itemLike, int ticks) {
        add(itemLike, UnifiedDataComponents.FURNACE_FUEL.get(), ticks);
    }

    default void addCompost(ItemLike itemLike, float chance) {
        add(itemLike, UnifiedDataComponents.COMPOST.get(), chance);
    }
}
