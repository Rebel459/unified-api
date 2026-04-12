package net.rebel459.unified.util.helper;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ItemLike;
import net.rebel459.unified.platform.UnifiedEvents;
import net.rebel459.unified.registry.UnifiedDataComponents;

public interface DataComponents {

    default <T> void add(ItemLike itemLike, DataComponentType<T> type, T value) {
        UnifiedEvents.DefaultDataComponents.modify((testedItem, builder, provider) -> {
            if (testedItem == itemLike.asItem()) {
                builder.set(type, value);
            }
        });
    }
    default <T> void addWithProvider(ItemLike itemLike, DataComponentType<T> type, DataComponentInitializers.SingleComponentInitializer<T> initializer) {
        UnifiedEvents.DefaultDataComponents.modify((testedItem, builder, provider) -> {
            if (testedItem == itemLike.asItem()) {
                builder.addAll(DataComponentMap.builder().set(type, initializer.create(provider)).build());
            }
        });
    }
    default <T> void addWithKey(ItemLike itemLike, DataComponentType<Holder<T>> type, ResourceKey<T> valueKey) {
        UnifiedEvents.DefaultDataComponents.modify((testedItem, builder, provider) -> {
            if (testedItem == itemLike.asItem()) {
                builder.addAll(DataComponentMap.builder().set(type, provider.getOrThrow(valueKey)).build());
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
