package net.rebel459.unified.impl.mixin.client;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.rebel459.unified.api.core.UnifiedInstance;
import net.rebel459.unified.api.platform.ModLoader;
import net.rebel459.unified.impl.client.core.CommonClientEvents;
import net.rebel459.unified.api.event.EventTiming;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Consumer;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(at = @At("HEAD"), method = "addDetailsToTooltip")
    private void passDetailsHead(Item.TooltipContext context, TooltipDisplay display, @Nullable Player player, TooltipFlag tooltipFlag, Consumer<Component> builder, CallbackInfo ci) {
        ItemStack stack = ItemStack.class.cast(this);
        if (player instanceof LocalPlayer localPlayer) {
            CommonClientEvents.ItemTooltips.passAddDetails(EventTiming.PRE, new CommonClientEvents.ItemTooltips.TooltipContext(stack, context, display, localPlayer, tooltipFlag, builder));
        }
    }
    @Inject(at = @At("TAIL"), method = "addDetailsToTooltip")
    private void passDetailsTail(Item.TooltipContext context, TooltipDisplay display, @Nullable Player player, TooltipFlag tooltipFlag, Consumer<Component> builder, CallbackInfo ci) {
        ItemStack stack = ItemStack.class.cast(this);
        if (player instanceof LocalPlayer localPlayer) {
            CommonClientEvents.ItemTooltips.passAddDetails(EventTiming.POST, new CommonClientEvents.ItemTooltips.TooltipContext(stack, context, display, localPlayer, tooltipFlag, builder));
        }
    }

    @Inject(at = @At("HEAD"), method = "addAttributeTooltips")
    private void passAttributesHead(Consumer<Component> consumer, TooltipDisplay display, @Nullable Player player, CallbackInfo ci) {
        ItemStack stack = ItemStack.class.cast(this);
        if (player instanceof LocalPlayer localPlayer && UnifiedInstance.getModLoader() != ModLoader.NEOFORGE) {
            CommonClientEvents.ItemTooltips.passAddAttributes(EventTiming.PRE, stack, consumer, display, localPlayer);
        }
    }
    @Inject(at = @At("TAIL"), method = "addAttributeTooltips")
    private void passAttributesTail(Consumer<Component> consumer, TooltipDisplay display, @Nullable Player player, CallbackInfo ci) {
        ItemStack stack = ItemStack.class.cast(this);
        if (player instanceof LocalPlayer localPlayer && UnifiedInstance.getModLoader() != ModLoader.NEOFORGE) {
            CommonClientEvents.ItemTooltips.passAddAttributes(EventTiming.POST, stack, consumer, display, localPlayer);
        }
    }

    @Inject(at = @At("RETURN"), method = "getTooltipLines")
    private void passLines(Item.TooltipContext context, @Nullable Player player, TooltipFlag tooltipFlag, CallbackInfoReturnable<List<Component>> cir) {
        ItemStack stack = ItemStack.class.cast(this);
        if (player instanceof LocalPlayer localPlayer) {
            CommonClientEvents.ItemTooltips.passInsertLines(new CommonClientEvents.ItemTooltips.LineContext(stack, context, localPlayer, tooltipFlag, cir.getReturnValue()));
        }
    }
}