package net.rebel459.unified.mixin.client;

import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.rebel459.unified.client.util.FabricAttributeTooltipImpl;
import org.apache.commons.lang3.function.TriConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemAttributeModifiers.class)
public class FabricItemAttributeModifiersMixin {

    @Inject(method = "forEach(Lnet/minecraft/world/entity/EquipmentSlotGroup;Lorg/apache/commons/lang3/function/TriConsumer;)V", at = @At("HEAD"))
    private void beginTooltipContext(EquipmentSlotGroup slot, TriConsumer<?, ?, ?> consumer, CallbackInfo ci) {
        FabricAttributeTooltipImpl.set(ItemAttributeModifiers.class.cast(this));
    }

    @Inject(method = "forEach(Lnet/minecraft/world/entity/EquipmentSlotGroup;Lorg/apache/commons/lang3/function/TriConsumer;)V", at = @At("TAIL"))
    private void endTooltipContext(EquipmentSlotGroup slot, TriConsumer<?, ?, ?> consumer, CallbackInfo ci) {
        FabricAttributeTooltipImpl.clear();
    }
}
