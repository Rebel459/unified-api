package net.rebel459.unified.mixin.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.rebel459.unified.platform.EventsImpl;
import net.rebel459.unified.util.EventType;
import net.rebel459.unified.util.mixin.LivingEntityVariant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

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

    @WrapOperation(method = "walkingStepSound", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;playStepSound(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V"))
    private void variantStepSound(Entity entity, BlockPos pos, BlockState blockState, Operation<Void> original) {
        if (entity instanceof LivingEntityVariant variant && variant.getVariant().isPresent()) {
            Optional<SoundEvent> sound = variant.getVariant().get().value().sounds().stepSound();
            if (sound.isPresent()) {
                entity.playSound(sound.get(), 0.15F, 1.0F);
                return;
            }
        }
        original.call(entity, pos, blockState);
    }

    @Inject(method = "getLootTable", at = @At("HEAD"), cancellable = true)
    private void getVariantLootTable(CallbackInfoReturnable<Optional<ResourceKey<LootTable>>> cir) {
        Entity entity = Entity.class.cast(this);
        if (entity instanceof LivingEntityVariant variant && variant.getVariant().isPresent()) {
            Optional<ResourceKey<LootTable>> lootTable = variant.getVariant().get().value().lootTable();
            lootTable.ifPresent(_ -> cir.setReturnValue(lootTable));
        }
    }
}