package net.rebel459.unified.util;

import net.minecraft.core.Holder;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public interface SuppliedBlock extends SuppliedBlockInterface, Holder<Block>, Supplier<Block>, ItemLike {}