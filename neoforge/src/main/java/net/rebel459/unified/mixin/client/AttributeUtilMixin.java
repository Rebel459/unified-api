package net.rebel459.unified.mixin.client;

import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
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

    @Inject(method = "applyModifierTooltips", at = @At(value = "TAIL"))
    private static void passAttributesTail(ItemStack stack, Consumer<Component> tooltip, AttributeTooltipContext ctx, CallbackInfo ci) {
        if (ctx.player() instanceof LocalPlayer player) ClientEventsImpl.ItemTooltips.passAddAttributes(EventType.POST, stack, tooltip, ctx.tooltipDisplay(), player);
    }

    @Inject(
            method = "applyTextFor",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V",
                    ordinal = 0,
                    shift = At.Shift.AFTER
            )
    )
    private static void afterBaseModifier(ItemStack stack, Consumer<Component> tooltip, Multimap<Holder<Attribute>, AttributeModifier> modifierMap, AttributeTooltipContext ctx, CallbackInfo ci, @Local(name = "attr") Holder<Attribute> attr, @Local(name = "amt") double amt) {
        ClientEventsImpl.ItemTooltips.passAfterBaseAttributeAdded(tooltip, stack, stack.getAttributeModifiers(), ctx.player(), attr, amt);
    }

    @Inject(
            method = "applyTextFor",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V",
                    ordinal = 3,
                    shift = At.Shift.AFTER
            )
    )
    private static void afterMergedModifier(ItemStack stack, Consumer<Component> tooltip, Multimap<Holder<Attribute>, AttributeModifier> modifierMap, AttributeTooltipContext ctx, CallbackInfo ci, @Local(name = "attr") Holder<Attribute> attr, @Local(name = "fakeModif") AttributeModifier fakeModif) {
        ClientEventsImpl.ItemTooltips.passAfterAttributeAdded(tooltip, stack, stack.getAttributeModifiers(), ctx.player(), attr, fakeModif);
    }

    @Inject(
            method = "applyTextFor",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V",
                    ordinal = 4,
                    shift = At.Shift.AFTER
            )
    )
    private static void afterSingleModifier(ItemStack stack, Consumer<Component> tooltip, Multimap<Holder<Attribute>, AttributeModifier> modifierMap, AttributeTooltipContext ctx, CallbackInfo ci, @Local(name = "attr") Holder<Attribute> attr, @Local(name = "fakeModif", ordinal = 0) AttributeModifier fakeModif) {
        ClientEventsImpl.ItemTooltips.passAfterAttributeAdded(tooltip, stack, stack.getAttributeModifiers(), ctx.player(), attr, fakeModif);
    }

    @Inject(
            method = "applyTextFor",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V",
                    ordinal = 5,
                    shift = At.Shift.AFTER
            )
    )
    private static void afterMergeDisabledModifier(ItemStack stack, Consumer<Component> tooltip, Multimap<Holder<Attribute>, AttributeModifier> modifierMap, AttributeTooltipContext ctx, CallbackInfo ci, @Local(name = "attr") Holder<Attribute> attr, @Local(name = "m") AttributeModifier m) {
        ClientEventsImpl.ItemTooltips.passAfterAttributeAdded(tooltip, stack, stack.getAttributeModifiers(), ctx.player(), attr, m);
    }
}