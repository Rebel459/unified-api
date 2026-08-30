package net.rebel459.unified.api.core;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.references.BlockItemId;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.rebel459.unified.api.util.BlockLike;
import org.jspecify.annotations.NonNull;

import java.util.function.Supplier;

public class SuppliedBlock extends Supplied<Block> implements BlockLike, ItemLike {

    private final BlockItemId blockItemId;

    public <T extends Block> SuppliedBlock(Supplier<Registry<Block>> registry, BlockItemId id, Supplier<T> block) {
        super(registry, id.block(), block);
        this.blockItemId = id;
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
