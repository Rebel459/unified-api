package net.rebel459.unified.mixin.entity;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.animal.feline.Cat;
import net.rebel459.unified.util.mixin.LivingEntityVariant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(Cat.class)
public class CatMixin {

    @Inject(method = "playEatingSound", at = @At(value = "HEAD"), cancellable = true)
    private void variantEatSound(CallbackInfo ci) {
        Cat cat = Cat.class.cast(this);
        if (cat instanceof LivingEntityVariant variant && variant.getVariant().isPresent()) {
            Optional<SoundEvent> sound = variant.getVariant().get().value().sounds().hurtSound();
            if (sound.isPresent()) {
                cat.playSound(sound.get());
                ci.cancel();
            }
        }
    }
}
