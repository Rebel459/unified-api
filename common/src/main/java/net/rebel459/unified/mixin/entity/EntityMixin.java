package net.rebel459.unified.mixin.entity;

import net.minecraft.world.entity.Entity;
import net.rebel459.unified.platform.EventsImpl;
import net.rebel459.unified.util.EventType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void preTick(CallbackInfo ci) {
        Entity entity = Entity.class.cast(this);
        EventsImpl.Entities.passOnTick(EventType.PRE, entity);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void postTick(CallbackInfo ci) {
        Entity entity = Entity.class.cast(this);
        EventsImpl.Entities.passOnTick(EventType.POST, entity);
    }
}