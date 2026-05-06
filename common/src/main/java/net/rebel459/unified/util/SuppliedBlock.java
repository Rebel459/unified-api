package net.rebel459.unified.util;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class SuppliedBlock extends Supplied<Block> implements BlockLike, ItemLike {

    @Nullable SuppliedItem item;

    public <T extends Block> SuppliedBlock(Supplier<Registry<Block>> registry, ResourceKey<Block> key, Supplier<T> block, @Nullable SuppliedItem item) {
        super(registry, key, block);
        this.item = item;
    }

    public BlockState defaultBlockState() {
        return this.get().defaultBlockState();
    }

    public ItemStackTemplate defaultTemplate() {
        return new ItemStackTemplate(this.asItem());
    }

    @Override
    public Holder<Block> holder() {
        return super.holder();
    }

    @Override
    public Block asBlock() {
        return this.holder().value();
    }

    @Override
    public Item asItem() {
        return this.item != null ? this.item.get() : this.holder().value().asItem();
    }

    @Override
    public Block get() {
        return super.get();
    }
}