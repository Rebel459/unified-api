package net.rebel459.unified.util;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.ItemLike;

import java.util.function.Supplier;

@Deprecated
public interface SuppliedItem extends Supplier<Item>, ItemLike {

    @Deprecated
    ItemStack getDefaultInstance();

    @Deprecated
    ItemStackTemplate getTemplate();
}
