package net.rebel459.unified.util;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public interface SuppliedBlockInterface {
    BlockState defaultBlockState();
    ItemStackTemplate getTemplate();
}