package net.rebel459.unified.util;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.ItemLike;

import java.util.function.Supplier;

public class SuppliedItem extends Supplied<Item> implements ItemLike {

    public SuppliedItem(Supplier<Registry<Item>> registry, ResourceKey<Item> key) {
        super(registry, key);
    }

    public ItemStack defaultItemStack() {
        return this.get().getDefaultInstance();
    }

    public ItemStackTemplate defaultTemplate() {
        return new ItemStackTemplate(this.get());
    }

    @Deprecated
    public ItemStack getDefaultInstance() {
        return this.defaultItemStack();
    }

    @Override
    public Holder<Item> holder() {
        return super.holder();
    }

    @Override
    public Item asItem() {
        return this.get();
    }

    @Override
    public Item get() {
        return this.holder().value();
    }
}