package net.rebel459.unified.util.registry;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.rebel459.unified.util.SuppliedItem;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public record SuppliedItemImpl(Holder<Item> holder) implements SuppliedItem {

    @Override
    public Item value() {
        return holder.value();
    }

    @Override
    public boolean isBound() {
        return holder.isBound();
    }

    @Override
    public boolean areComponentsBound() {
        return holder.areComponentsBound();
    }

    @Override
    public boolean is(Identifier identifier) {
        return holder.is(identifier);
    }

    @Override
    public boolean is(ResourceKey<Item> resourceKey) {
        return holder.is(resourceKey);
    }

    @Override
    public boolean is(Predicate<ResourceKey<Item>> predicate) {
        return holder.is(predicate);
    }

    @Override
    public boolean is(TagKey<Item> tagKey) {
        return holder.is(tagKey);
    }

    @Override
    public boolean is(Holder<Item> holder) {
        return this.holder == holder;
    }

    @Override
    public Stream<TagKey<Item>> tags() {
        return holder.tags();
    }

    @Override
    public DataComponentMap components() {
        return holder.components();
    }

    @Override
    public Either<ResourceKey<Item>, Item> unwrap() {
        return holder.unwrap();
    }

    @Override
    public Optional<ResourceKey<Item>> unwrapKey() {
        return holder.unwrapKey();
    }

    @Override
    public Kind kind() {
        return holder.kind();
    }

    @Override
    public boolean canSerializeIn(HolderOwner<Item> holderOwner) {
        return holder.canSerializeIn(holderOwner);
    }

    @Override
    public Item asItem() {
        return holder.value().asItem();
    }

    @Override
    public ItemStack getDefaultInstance() {
        return holder.value().getDefaultInstance();
    }

    @Override
    public ItemStackTemplate getTemplate() {
        return new ItemStackTemplate(holder.value());
    }

    @Override
    public Item get() {
        return holder.value();
    }
}
