package net.rebel459.unified.util;

import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

@Deprecated
public interface SuppliedBlock extends Supplier<Block>, ItemLike {

    BlockState defaultBlockState();

    @Deprecated
    ItemStackTemplate getTemplate();
}
