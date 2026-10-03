package net.rebel459.unified.api.core;

import net.minecraft.core.Holder;
import net.rebel459.unified.impl.util.BlockItemId;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.rebel459.unified.api.util.BlockLike;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;

import java.util.function.Supplier;

public class SuppliedBlock extends Supplied<Block> implements BlockLike, ItemLike {

    private final BlockItemId blockItemId;

    @ApiStatus.Internal
    public SuppliedBlock(BlockItemId blockItemId, Supplier<? extends Block> block, Supplier<? extends Holder<Block>> holder) {
        super(blockItemId.block(), block, holder);
        this.blockItemId = blockItemId;
    }

    public BlockState defaultBlockState() {
        return this.get().defaultBlockState();
    }

    public ItemStackTemplate defaultTemplate() {
        return new ItemStackTemplate(this.asItem());
    }

    public BlockItemId blockItemId() {
        return this.blockItemId;
    }

    @Override
    public Block asBlock() {
        return this.get();
    }

    @Override
    public @NonNull Item asItem() {
        return Item.BY_BLOCK.getOrDefault(get(), Items.AIR);
    }
}
