package net.rebel459.unified.impl.mixin.entity;

import net.minecraft.world.entity.LivingEntity;
import net.rebel459.unified.impl.core.CommonEvents;
import net.rebel459.unified.api.event.EventTiming;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

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
}