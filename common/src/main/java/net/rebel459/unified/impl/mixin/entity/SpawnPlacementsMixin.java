package net.rebel459.unified.mixin.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.rebel459.unified.util.data.registry.impl.EntityRegistryImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpawnPlacements.class)
public class SpawnPlacementsMixin {
    @Inject(method = "getPlacementType", at = @At("HEAD"), cancellable = true)
    private static void copiedPlacement(EntityType<?> type, CallbackInfoReturnable<SpawnPlacementType> cir) {
        EntityRegistryImpl.template(type).ifPresent(template -> cir.setReturnValue(SpawnPlacements.getPlacementType(template)));
    }

    @Inject(method = "isSpawnPositionOk", at = @At("HEAD"), cancellable = true)
    private static void copiedPosition(EntityType<?> type, LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        EntityRegistryImpl.template(type).ifPresent(template -> cir.setReturnValue(SpawnPlacements.isSpawnPositionOk(template, level, pos)));
    }

    @Inject(method = "getHeightmapType", at = @At("HEAD"), cancellable = true)
    private static void copiedHeightmap(EntityType<?> type, CallbackInfoReturnable<Heightmap.Types> cir) {
        EntityRegistryImpl.template(type).ifPresent(template -> cir.setReturnValue(SpawnPlacements.getHeightmapType(template)));
    }

    @Inject(method = "checkSpawnRules", at = @At("HEAD"), cancellable = true)
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T extends Entity> void copiedRules(EntityType<T> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random, CallbackInfoReturnable<Boolean> cir) {
        EntityRegistryImpl.template(type).ifPresent(template -> cir.setReturnValue(
                SpawnPlacements.checkSpawnRules((EntityType) template, level, reason, pos, random)
        ));
    }
}
