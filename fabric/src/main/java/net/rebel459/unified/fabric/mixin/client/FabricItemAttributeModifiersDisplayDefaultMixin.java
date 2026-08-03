package net.rebel459.unified.fabric.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.rebel459.unified.fabric.util.FabricAttributeTooltipImpl;
import net.rebel459.unified.impl.client.core.ClientEventsImpl;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemAttributeModifiers.Display.Default.class)
public class FabricItemAttributeModifiersDisplayDefaultMixin {

    @Inject(method = "apply", at = @At(value = "TAIL"))
    private void addCriticalDamageTooltip(Consumer<Component> consumer, @Nullable Player player, Holder<Attribute> attribute, AttributeModifier modifier, CallbackInfo ci, @Local(name = "displayAmount") double displayAmount, @Local(name = "displayWithBase") boolean displayWithbase) {
        if (displayWithbase) ClientEventsImpl.ItemTooltips.passAfterBaseAttributeAdded(consumer, FabricAttributeTooltipImpl.getStack(), FabricAttributeTooltipImpl.get(), player, attribute, displayAmount);
        else ClientEventsImpl.ItemTooltips.passAfterAttributeAdded(consumer, FabricAttributeTooltipImpl.getStack(), FabricAttributeTooltipImpl.get(), player, attribute, modifier);
    }
}