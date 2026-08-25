package net.rebel459.unified.impl.mixin.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.TamableAnimal;
import net.rebel459.unified.impl.util.LivingEntityVariant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(TamableAnimal.class)
public class TamableAnimalMixin {

    @WrapOperation(method = "feed", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/TamableAnimal;playEatingSound()V"))
    private void variantEatSound(TamableAnimal animal, Operation<Void> original) {
        if (animal instanceof LivingEntityVariant variant && variant.getVariant().isPresent()) {
            Optional<Holder<SoundEvent>> sound = variant.getVariant().get().value().sounds().eatSound();
            if (sound.isPresent()) {
                animal.playSound(sound.get().value());
                return;
            }
        }
        original.call(animal);
    }
}
