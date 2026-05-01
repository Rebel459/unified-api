package net.rebel459.unified.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.rebel459.unified.network.StructurePackets;
import net.rebel459.unified.platform.EventsImpl;
import net.rebel459.unified.platform.client.ClientEventsImpl;
import net.rebel459.unified.platform.client.UnifiedClientHelpers;
import net.rebel459.unified.util.mixin.PlayerStructureMusic;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void startClientLevel(ClientPacketListener connection, ClientLevel.ClientLevelData levelData, ResourceKey<Level> dimension, Holder<DimensionType> dimensionType, int serverChunkRadius, int serverSimulationDistance, LevelRenderer levelRenderer, boolean isDebug, long biomeZoomSeed, int seaLevel, CallbackInfo ci) {
        ClientLevel level = ClientLevel.class.cast(this);
        if (level != null) {
            ClientEventsImpl.Instance.passOnLevelLoad(level);
            EventsImpl.Levels.passOnLoad(level);
        }
    }
}
