package net.rebel459.unified.neoforge.mixin.server;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.rebel459.unified.impl.core.EventsImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PlayerList.class, remap = false)
public class PlayerListMixin {

    @Unique
    private ServerPlayer oldPlayer;

    @Inject(
            method = "respawn",
            at = @At(value = "HEAD")
    )
    private void getOldPlayer(ServerPlayer serverPlayer, boolean keepAllPlayerData, Entity.RemovalReason removalReason, CallbackInfoReturnable<ServerPlayer> cir) {
        this.oldPlayer = serverPlayer;
    }

    @WrapOperation(
            method = "respawn",
            at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/event/EventHooks;firePlayerRespawnEvent(Lnet/minecraft/server/level/ServerPlayer;Z)V")
    )
    private void passOnRespawn(ServerPlayer newPlayer, boolean fromEndFight, Operation<Void> original) {
        EventsImpl.Players.passOnRespawn(this.oldPlayer, newPlayer);
        original.call(newPlayer, fromEndFight);
    }
}