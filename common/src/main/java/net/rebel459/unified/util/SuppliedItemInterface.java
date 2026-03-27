package net.rebel459.unified.util;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

public interface SuppliedItemInterface {
    ItemStack getDefaultInstance();
    ItemStackTemplate getTemplate();
}