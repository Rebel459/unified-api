package net.rebel459.unified.impl.mixin.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.rebel459.unified.impl.core.CommonEvents;
import net.rebel459.unified.api.event.EventTiming;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.stream.Stream;

@Mixin(LivingEntity.class)
public class LivingEntityMixin implements LivingEntityVariant {

    @Inject(method = "tick", at = @At("HEAD"))
    private void preTick(CallbackInfo ci) {
        LivingEntity entity = LivingEntity.class.cast(this);
        CommonEvents.Entities.passOnLivingTick(EventTiming.PRE, entity);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void postTick(CallbackInfo ci) {
        LivingEntity entity = LivingEntity.class.cast(this);
        CommonEvents.Entities.passOnLivingTick(EventTiming.POST, entity);
    }

    @WrapOperation(method = "handleEntityEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getDeathSound()Lnet/minecraft/sounds/SoundEvent;"))
    private SoundEvent variantDeathSoundEvent(LivingEntity entity, Operation<SoundEvent> original) {
        if (entity instanceof LivingEntityVariant variant && variant.getVariant().isPresent()) {
            Optional<Holder<SoundEvent>> sound = variant.getVariant().get().value().sounds().deathSound();
            if (sound.isPresent()) return sound.get().value();
        }
        return original.call(entity);
    }

    @WrapOperation(method = "hurtServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getDeathSound()Lnet/minecraft/sounds/SoundEvent;"))
    private SoundEvent variantDeathSoundServer(LivingEntity entity, Operation<SoundEvent> original) {
        if (entity instanceof LivingEntityVariant variant && variant.getVariant().isPresent()) {
            Optional<Holder<SoundEvent>> sound = variant.getVariant().get().value().sounds().deathSound();
            if (sound.isPresent()) return sound.get().value();
        }
        return original.call(entity);
    }

    @WrapOperation(method = "playHurtSound", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getHurtSound(Lnet/minecraft/world/damagesource/DamageSource;)Lnet/minecraft/sounds/SoundEvent;"))
    private SoundEvent variantHurtSound(LivingEntity entity, DamageSource source, Operation<SoundEvent> original) {
        if (entity instanceof LivingEntityVariant variant && variant.getVariant().isPresent()) {
            Optional<Holder<SoundEvent>> sound = variant.getVariant().get().value().sounds().hurtSound();
            if (sound.isPresent()) return sound.get().value();
        }
        return original.call(entity, source);
    }

    @Override
    public void setVariant(Optional<Holder<MobVariants.Variant>> variant) {
        LivingEntity entity = LivingEntity.class.cast(this);
        MobVariants.MOB_VARIANT_ATTEMPTED.set(entity, true);
        MobVariants.MOB_VARIANT.set(entity, variant);
        if (variant.isPresent()) {
            AttributeMap attributes = entity.getAttributes();
            for (MobVariants.AttributeEntry entry : variant.get().value().attributes()) {
                AttributeInstance instance = attributes.attributes.computeIfAbsent(
                        entry.attribute(),
                        attribute -> new AttributeInstance(
                                attribute,
                                attributes::onAttributeModified
                        )
                );

                instance.addOrUpdateTransientModifier(entry.modifier());
            }
        }
    }

    @Override
    public Optional<Holder<MobVariants.Variant>> getVariant() {
        return MobVariants.MOB_VARIANT.get(LivingEntity.class.cast(this));
    }

    @Override
    public void spawnVariant(ServerLevelAccessor level) {
        LivingEntity entity = LivingEntity.class.cast(this);

        if (MobVariants.MOB_VARIANT_ATTEMPTED.get(entity)) return;
        MobVariants.MOB_VARIANT_ATTEMPTED.set(entity, true);

        Identifier entityType = EntityType.getKey(entity.getType());
        RandomSource random = level.getRandom();

        Stream<Holder.Reference<MobVariants.Variant>> candidates = level.registryAccess()
                        .lookupOrThrow(MobVariants.KEY)
                        .listElements()
                        .filter(holder -> holder.value().target().equals(entityType))
                        .filter(holder -> random.nextFloat() < holder.value().spawnChance());

        PriorityProvider.pick(candidates, Holder::value, random, SpawnContext.create(level, entity.blockPosition())).ifPresent(holder -> setVariant(Optional.of(holder)));
    }
}
