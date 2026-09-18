package net.rebel459.unified.api.util;

import net.minecraft.advancements.predicates.DamageSourcePredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Holder;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.animal.frog.FrogVariant;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.ColorCollection;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.rebel459.unified.impl.util.LootProviderContext;

public class EntityLootProvider extends EntityLootSubProvider {

    public EntityLootProvider(HolderLookup.Provider registries) {
        super(FeatureFlags.REGISTRY.allFlags(), new LootProviderContext(registries));
    }

    @Override public void generate() {}
    @Override public DamageSourcePredicate.Builder projectileDamage() { return super.projectileDamage(); }
    public static LootPool.Builder createSheepDispatchPool(ColorCollection<Holder<LootTable>> tables) {
        return EntityLootSubProvider.createSheepDispatchPool(tables);
    }
    @Override public LootItemCondition.Builder killedByFrog() { return super.killedByFrog(); }

    @Override public LootItemCondition.Builder killedByFrogVariant(ResourceKey<FrogVariant> variant) {
        return super.killedByFrogVariant(variant);
    }
}
