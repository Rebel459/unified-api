package net.rebel459.unified.api.event;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public interface CreativeEntryContext {
    void insert(ItemLike... items);
    void insert(ItemStack... items);
    void insertAfter(ItemLike existingItem, ItemLike... addedItems);
    void insertAfter(ItemLike existingItem, ItemStack... addedItems);
    void insertAfter(ItemStack existingItem, ItemStack... addedItems);
    void insertBefore(ItemLike existingItem, ItemLike... addedItems);
    void insertBefore(ItemLike existingItem, ItemStack... addedItems);
    void insertBefore(ItemStack existingItem, ItemStack... addedItems);
}
