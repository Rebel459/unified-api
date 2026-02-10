package net.rebel459.unified.util;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface Item2ObjectMap<V> {
    V get(ItemLike var1);

    void add(ItemLike var1, V var2);

    void add(TagKey<Item> var1, V var2);

    void remove(ItemLike var1);

    void remove(TagKey<Item> var1);

    void clear(ItemLike var1);

    void clear(TagKey<Item> var1);
}