package net.rebel459.unified.impl.mixin.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.rebel459.unified.api.event.EventTiming;
import net.rebel459.unified.impl.core.EventsImpl;
import net.rebel459.unified.impl.client.core.ClientEventsImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void startClientLevel(ClientPacketListener connection, ClientLevel.ClientLevelData levelData, ResourceKey<Level> dimension, Holder<DimensionType> dimensionType, int serverChunkRadius, int serverSimulationDistance, LevelExtractor levelExtractor, boolean isDebug, long biomeZoomSeed, int seaLevel, CallbackInfo ci) {
        ClientLevel level = ClientLevel.class.cast(this);
        if (level != null) {
            ClientEventsImpl.Instance.passOnLevelLoad(level);
            EventsImpl.Levels.passOnLoad(level);
        }
    }

    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void preTickLevel(BooleanSupplier haveTime, CallbackInfo ci) {
        EventsImpl.Levels.passOnTick(EventTiming.PRE, ClientLevel.class.cast(this));
    }

    @Inject(method = "tick", at = @At(value = "TAIL"))
    private void postTickLevel(BooleanSupplier haveTime, CallbackInfo ci) {
        EventsImpl.Levels.passOnTick(EventTiming.POST, ClientLevel.class.cast(this));
    }
}
