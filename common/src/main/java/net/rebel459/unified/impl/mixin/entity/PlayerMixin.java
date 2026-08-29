package net.rebel459.unified.impl.mixin.entity;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.rebel459.unified.impl.core.CommonEvents;
import net.rebel459.unified.api.event.EventTiming;
import net.rebel459.unified.impl.network.StructurePacketImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(Player.class)
public class PlayerMixin {

    @Unique
    private Identifier pieceStructure = Identifier.withDefaultNamespace("empty");

    @Unique
    private Identifier boxStructure = Identifier.withDefaultNamespace("empty");

    @Unique
    private boolean replaceCurrentMusic = false;

    @Unique
    private int playerGroup = 1;

    @Inject(method = "tick", at = @At("HEAD"))
    private void preTick(CallbackInfo ci) {
        Player player = Player.class.cast(this);
        CommonEvents.Players.passOnTick(EventTiming.PRE, player);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void postTick(CallbackInfo ci) {
        Player player = Player.class.cast(this);
        CommonEvents.Players.passOnTick(EventTiming.POST, player);
    }
}