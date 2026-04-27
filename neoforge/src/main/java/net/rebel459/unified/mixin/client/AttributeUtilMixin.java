package net.rebel459.unified.mixin.client;

import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.common.util.AttributeUtil;
import net.rebel459.unified.platform.client.ClientEventsImpl;
import net.rebel459.unified.util.EventType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(value = AttributeUtil.class, remap = false)
public class AttributeUtilMixin {

    @Inject(method = "applyModifierTooltips", at = @At("HEAD"))
    private static void passAttributesHead(ItemStack stack, Consumer<Component> tooltip, AttributeTooltipContext ctx, CallbackInfo ci) {
        if (ctx.player() instanceof LocalPlayer player) ClientEventsImpl.ItemTooltips.passAddAttributes(EventType.PRE, stack, tooltip, ctx.tooltipDisplay(), player);
    }

    @WrapOperation(method = "applyModifierTooltips", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/common/util/AttributeUtil;applyTextFor(Lnet/minecraft/world/item/ItemStack;Ljava/util/function/Consumer;Lcom/google/common/collect/Multimap;Lnet/neoforged/neoforge/common/util/AttributeTooltipContext;)V"))
    private static void passAttributesPost(ItemStack stack, Consumer<Component> tooltip, Multimap<Holder<Attribute>, AttributeModifier> modifierMap, AttributeTooltipContext ctx, Operation<Void> original) {
        if (ctx.player() instanceof LocalPlayer player) ClientEventsImpl.ItemTooltips.passAddAttributes(EventType.POST, stack, tooltip, ctx.tooltipDisplay(), player);
        original.call(stack, tooltip, modifierMap, ctx);
    }
}