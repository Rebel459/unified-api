package net.rebel459.unified.util.impl;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.ComposterBlock;
import net.rebel459.unified.platform.UnifiedRegistries;

public class CompostingRegistryImpl implements UnifiedRegistries.CompostingRegistry {
        public Float get(ItemLike item) {
            return ComposterBlock.COMPOSTABLES.getOrDefault(item.asItem(), 0.0F);
        }

        public void add(ItemLike item, Float value) {
            ComposterBlock.COMPOSTABLES.put(item.asItem(), value);
        }

        public void add(TagKey<Item> tag, Float value) {
            throw new UnsupportedOperationException("Tags currently not supported!");
        }

        public void remove(ItemLike item) {
            ComposterBlock.COMPOSTABLES.removeFloat(item.asItem());
        }

        public void remove(TagKey<Item> tag) {
            throw new UnsupportedOperationException("Tags currently not supported!");
        }

        public void clear(ItemLike item) {
            throw new UnsupportedOperationException("CompostingChanceRegistry operates directly on the vanilla map - clearing not supported!");
        }

        public void clear(TagKey<Item> tag) {
            throw new UnsupportedOperationException("CompostingChanceRegistry operates directly on the vanilla map - clearing not supported!");
        }
    }