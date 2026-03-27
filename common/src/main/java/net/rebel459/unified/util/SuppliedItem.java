package net.rebel459.unified.util;

import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.function.Supplier;

public interface SuppliedItem extends SuppliedItemInterface, Holder<Item>, Supplier<Item>, ItemLike {}