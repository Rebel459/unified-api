package net.rebel459.unified.api.util;

import net.minecraft.advancements.predicates.DamageSourcePredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Holder;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.frog.FrogVariant;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.ColorCollection;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class EntityLootProvider extends EntityLootSubProvider {

    public EntityLootProvider(HolderLookup.Provider registries) {
        super(FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override public void generate() {}

    public static LootPool.Builder createSheepDispatchPool(ColorCollection<ResourceKey<LootTable>> tableNames) {
        return EntityLootSubProvider.createSheepDispatchPool(tableNames);
    }

    @Override public LootItemCondition.Builder killedByFrog(HolderGetter<EntityType<?>> entityTypes) { return super.killedByFrog(entityTypes); }

    @Override public LootItemCondition.Builder killedByFrogVariant(HolderGetter<EntityType<?>> entityTypes, HolderGetter<FrogVariant> frogVariants, ResourceKey<FrogVariant> variant) {
        return super.killedByFrogVariant(entityTypes,  frogVariants, variant);
    }
}
