package net.rebel459.unified.impl.mixin.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.rebel459.unified.impl.core.CommonEvents;
import net.rebel459.unified.impl.util.LivingEntityVariant;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.server.level.ServerLevel$EntityCallbacks")
public abstract class ServerLevelEntityCallbacksMixin {

	@Shadow
	@Final
    ServerLevel this$0;

	@Inject(method = "onTrackingStart(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"))
	private void selectMissingVariant(Entity entity, CallbackInfo ci) {
		if (entity instanceof Mob && entity instanceof LivingEntityVariant variant && variant.getVariant().isEmpty()) {
			variant.spawnVariant(this$0);
		}
	}

	@Inject(method = "onTrackingStart(Lnet/minecraft/world/entity/Entity;)V", at = @At("TAIL"))
	private void passOnLoad(Entity entity, CallbackInfo ci) {
		CommonEvents.Entities.passOnLoad(entity, this$0);
	}

	@Inject(method = "onTrackingEnd(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"))
	private void passOnUnload(Entity entity, CallbackInfo info) {
		CommonEvents.Entities.passOnUnload(entity, this$0);
	}
}
