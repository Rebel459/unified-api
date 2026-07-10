package net.rebel459.unified.mixin.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.rebel459.unified.platform.EventsImpl;
import net.rebel459.unified.platform.client.ClientEventsImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
}
