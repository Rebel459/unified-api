package net.rebel459.unified.mixin.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.ServerLevelAccessor;
import net.rebel459.unified.util.mixin.LivingEntityVariant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Mob.class)
public abstract class MobMixin {

    @WrapOperation(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;is(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean shouldVariantBurnInDaylight(Mob mob, TagKey<EntityType<?>> tagKey, Operation<Boolean> original) {
        if (Mob.class.cast(this) instanceof LivingEntityVariant variant && variant.getVariant().isPresent()) {
            Optional<Boolean> burnInDaylight = variant.getVariant().get().value().burnInDaylight();
            if (burnInDaylight.isPresent()) return burnInDaylight.get();
        }
        return original.call(mob, tagKey);
    }

    @Inject(method = "finalizeSpawn", at = @At("TAIL"))
    private void selectVariant(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnReason, SpawnGroupData groupData, CallbackInfoReturnable<SpawnGroupData> cir) {
        if (Mob.class.cast(this) instanceof LivingEntityVariant variant && variant.getVariant().isEmpty()) {
            variant.spawnVariant(level);
        }
    }

    @Inject(method = "doHurtTarget", at = @At("HEAD"))
    private void applyVariantMeleeEffects(ServerLevel level, Entity target, CallbackInfoReturnable<Boolean> cir) {
        if (Mob.class.cast(this) instanceof LivingEntityVariant variant && variant.getVariant().isPresent() && target instanceof LivingEntity livingEntity) {
            for (MobEffectInstance effect : variant.getVariant().get().value().attackEffects()) {
                livingEntity.addEffect(effect);
            }
        }
    }

    @WrapOperation(method = "playAmbientSound", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;getAmbientSound()Lnet/minecraft/sounds/SoundEvent;"))
    private SoundEvent variantAmbientSound(Mob mob, Operation<SoundEvent> original) {
        if (mob instanceof LivingEntityVariant variant && variant.getVariant().isPresent()) {
            Optional<Holder<SoundEvent>> sound = variant.getVariant().get().value().sounds().ambientSound();
            if (sound.isPresent()) return sound.get().value();
        }
        return original.call(mob);
    }
}
