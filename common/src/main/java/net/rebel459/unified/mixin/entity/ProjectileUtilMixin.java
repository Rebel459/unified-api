package net.rebel459.unified.mixin.entity;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.rebel459.unified.util.mixin.LivingEntityVariant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ProjectileUtil.class)
public class ProjectileUtilMixin {

    @Inject(method = "getMobArrow", at = @At("RETURN"))
    private static void getVariantArrow(LivingEntity mob, ItemStack projectile, float power, ItemStack firedFromWeapon, CallbackInfoReturnable<AbstractArrow> cir) {
        if (mob instanceof LivingEntityVariant variant && variant.getVariant().isPresent() && cir.getReturnValue() instanceof Arrow arrow) {
            for (MobEffectInstance effect : variant.getVariant().get().value().attackEffects()) {
                arrow.addEffect(effect);
            }
        }
    }
}
