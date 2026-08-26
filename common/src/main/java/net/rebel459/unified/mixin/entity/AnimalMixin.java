package net.rebel459.unified.mixin.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.rebel459.unified.util.data.MobVariants;
import net.rebel459.unified.util.mixin.LivingEntityVariant;
import net.rebel459.unified.util.registry.EntityTypeCopies;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(Animal.class)
public class AnimalMixin {

    @Inject(method = "finalizeSpawnChildFromBreeding", at = @At("HEAD"))
    private void createVariantFromBreeding(ServerLevel level, Animal partner, AgeableMob offspring, CallbackInfo ci) {
        Animal animal = Animal.class.cast(this);
        if (animal instanceof LivingEntityVariant firstParent && partner instanceof LivingEntityVariant secondParent && offspring instanceof LivingEntityVariant child) {
            Identifier offspringType = EntityType.getKey(offspring.getType());
            List<Holder<MobVariants.Variant>> inherited = new ArrayList<>(2);
            firstParent.getVariant().filter(variant -> appliesTo(variant, offspring.getType(), offspringType)).ifPresent(inherited::add);
            secondParent.getVariant().filter(variant -> appliesTo(variant, offspring.getType(), offspringType)).ifPresent(inherited::add);

            if (!inherited.isEmpty()) {
                child.setVariant(Optional.of(inherited.get(animal.getRandom().nextInt(inherited.size()))));
            }
        }
    }

    private static boolean appliesTo(Holder<MobVariants.Variant> variant, EntityType<?> entityType, Identifier entityTypeId) {
        return variant.value().target().map(entityTypeId::equals).orElseGet(() -> EntityTypeCopies.isDefaultVariant(entityType, variant));
    }

    @WrapOperation(
            method = "mobInteract",
            at = {
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/entity/animal/Animal;playEatingSound()V",
                            ordinal = 0
                    ),
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/entity/animal/Animal;playEatingSound()V",
                            ordinal = 1
                    )
            }
    )
    private void variantEatSound(Animal animal, Operation<Void> original) {
        if (animal instanceof LivingEntityVariant variant && variant.getVariant().isPresent()) {
            Optional<SoundEvent> sound = variant.getVariant().get().value().sounds().eatSound();
            if (sound.isPresent()) {
                animal.playSound(sound.get());
                return;
            }
        }
        original.call(animal);
    }
}
