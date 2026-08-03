package net.rebel459.unified.neoforge.mixin.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.rebel459.unified.neoforge.platform.NeoForgeInternalHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(WeatheringCopper.class)
public interface WeatheringCopperMixin {

    @Inject(at = @At(value = "TAIL"), method = "getNext(Lnet/minecraft/world/level/block/Block;)Ljava/util/Optional;", cancellable = true)
    private static void getNextFromUnified(Block block, CallbackInfoReturnable<Optional<Block>> cir) {
        if (cir.getReturnValue().isPresent()) return;
        Block next = NeoForgeInternalHandler.Impl.OXIDIZABLES.get(block);
        if (next == null) return;
        cir.setReturnValue(Optional.of(next));
    }
}