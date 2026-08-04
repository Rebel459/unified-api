package net.rebel459.unified.impl.mixin.entity;

import net.minecraft.world.entity.Entity;
import net.rebel459.unified.impl.core.CommonEvents;
import net.rebel459.unified.api.event.EventTiming;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void preTick(CallbackInfo ci) {
        Entity entity = Entity.class.cast(this);
        CommonEvents.Entities.passOnTick(EventTiming.PRE, entity);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void postTick(CallbackInfo ci) {
        Entity entity = Entity.class.cast(this);
        CommonEvents.Entities.passOnTick(EventTiming.POST, entity);
    }
}