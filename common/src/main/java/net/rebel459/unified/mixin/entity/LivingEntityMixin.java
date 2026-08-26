package net.rebel459.unified.mixin.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.variant.PriorityProvider;
import net.minecraft.world.entity.variant.SpawnContext;
import net.minecraft.world.level.ServerLevelAccessor;
import net.rebel459.unified.platform.EventsImpl;
import net.rebel459.unified.util.EventType;
import net.rebel459.unified.util.data.MobVariants;
import net.rebel459.unified.util.mixin.LivingEntityVariant;
import net.rebel459.unified.util.data.registry.impl.EntityRegistryImpl;
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
        EventsImpl.Entities.passOnLivingTick(EventType.PRE, entity);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void postTick(CallbackInfo ci) {
        LivingEntity entity = LivingEntity.class.cast(this);
        EventsImpl.Entities.passOnLivingTick(EventType.POST, entity);
    }

    @WrapOperation(method = "handleEntityEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getDeathSound()Lnet/minecraft/sounds/SoundEvent;"))
    private SoundEvent variantDeathSoundEvent(LivingEntity entity, Operation<SoundEvent> original) {
        if (entity instanceof LivingEntityVariant variant && variant.getVariant().isPresent()) {
            Optional<SoundEvent> sound = variant.getVariant().get().value().sounds().deathSound();
            if (sound.isPresent()) return sound.get();
        }
        return original.call(entity);
    }

    @WrapOperation(method = "hurtServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getDeathSound()Lnet/minecraft/sounds/SoundEvent;"))
    private SoundEvent variantDeathSoundServer(LivingEntity entity, Operation<SoundEvent> original) {
        if (entity instanceof LivingEntityVariant variant && variant.getVariant().isPresent()) {
            Optional<SoundEvent> sound = variant.getVariant().get().value().sounds().deathSound();
            if (sound.isPresent()) return sound.get();
        }
        return original.call(entity);
    }

    @WrapOperation(method = "playHurtSound", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getHurtSound(Lnet/minecraft/world/damagesource/DamageSource;)Lnet/minecraft/sounds/SoundEvent;"))
    private SoundEvent variantHurtSound(LivingEntity entity, DamageSource source, Operation<SoundEvent> original) {
        if (entity instanceof LivingEntityVariant variant && variant.getVariant().isPresent()) {
            Optional<SoundEvent> sound = variant.getVariant().get().value().sounds().hurtSound();
            if (sound.isPresent()) return sound.get();
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

        var variants = level.registryAccess().lookupOrThrow(MobVariants.KEY);
        var defaultVariantKey = EntityRegistryImpl.defaultVariant(entity.getType());
        Optional<Holder<MobVariants.Variant>> defaultVariant = EntityRegistryImpl.resolveDefaultVariant(entity.getType(), variants);
        if (defaultVariant.isEmpty()) {
            defaultVariantKey.ifPresent(key -> {
                throw new IllegalStateException("Missing default mob variant " + key.identifier() + " for " + entityType);
            });
        }

        Stream<Holder.Reference<MobVariants.Variant>> candidates = variants
                        .listElements()
                        .filter(holder -> holder.value().target().map(entityType::equals).orElse(false))
                        .filter(holder -> defaultVariantKey.map(key -> !holder.key().equals(key)).orElse(true))
                        .filter(holder -> random.nextFloat() < holder.value().spawnChance());

        Optional<Holder<MobVariants.Variant>> selected = PriorityProvider.pick(candidates, Holder::value, random, SpawnContext.create(level, entity.blockPosition())).map(holder -> holder);
        selected.or(() -> defaultVariant).ifPresent(holder -> setVariant(Optional.of(holder)));
    }
}
