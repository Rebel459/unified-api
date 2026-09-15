package net.rebel459.unified.api.core;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

public class SuppliedItem extends Supplied<Item> implements ItemLike {

    @ApiStatus.Internal
    public SuppliedItem(ResourceKey<Item> key, Supplier<? extends Item> item, Holder<Item> holder) {
        super(key, item, holder);
    }

    public ItemStack defaultItemStack() {
        return this.get().getDefaultInstance();
    }

    public ItemStackTemplate defaultTemplate() {
        return new ItemStackTemplate(this.get());
    }

    @Override
    public Item asItem() {
        return this.get();
    }
}
