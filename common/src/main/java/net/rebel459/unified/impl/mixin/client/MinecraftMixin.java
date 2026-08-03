package net.rebel459.unified.impl.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.util.Mth;
import net.rebel459.unified.api.event.EventTiming;
import net.rebel459.unified.impl.network.StructurePackets;
import net.rebel459.unified.impl.core.EventsImpl;
import net.rebel459.unified.impl.client.core.ClientEventsImpl;
import net.rebel459.unified.api.client.core.UnifiedClientHelpers;
import net.rebel459.unified.impl.helper.StructureMusicImpl;
import net.rebel459.unified.impl.helper.PlayerStructureMusic;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow
    @Nullable
    public LocalPlayer player;

    @Shadow
    @Final
    private MusicManager musicManager;

    @Shadow
    @Final
    private SoundManager soundManager;

    @Shadow
    @Nullable
    public ClientLevel level;

    @Shadow
    public abstract boolean isDemo();

    @Unique
    private boolean pendingUpdate = false;

    @Unique
    private int pendingTicks = 0;

    @Unique
    private boolean onCooldown = true;

    @Unique
    private int cooldownTicks = 0;

    @Unique
    private final int MAX_TICKS = 300;

    @Unique
    private final Identifier EMPTY = Identifier.withDefaultNamespace("empty");

    @Inject(method = "getSituationalMusic", at = @At(value = "HEAD"), cancellable = true)
    private void getStructureMusic(CallbackInfoReturnable<Music> cir) {
        LocalPlayer player = this.player;
        if (player == null || cir.getReturnValue() == Musics.MENU) return;
        if ((player.getAbilities().instabuild && player.getAbilities().mayfly) || !(player instanceof PlayerStructureMusic music) || music.getStructureMusic().isEmpty()) return;
        MusicManager musicManager = this.musicManager;
        boolean replaceCurrentMusic = false;
        if (music.getReplaceCurrentMusic()) {
            replaceCurrentMusic = true;
            var structureMusic = StructureMusicImpl.getStructureMusic(music.getStructureMusic(), music.getPieceStructure(), this.level);
            if (structureMusic == null) structureMusic = StructureMusicImpl.getStructureMusic(music.getStructureMusic(), music.getBoxStructure(), this.level);
            if (structureMusic != null) {
                SoundInstance currentMusic = musicManager.currentMusic;
                if (currentMusic != null && (MusicManager.canReplace(structureMusic.music(), currentMusic))) {
                    this.soundManager.stop(currentMusic);
                    musicManager.nextSongDelay = Mth.nextInt(musicManager.random, 0, structureMusic.music().minDelay() / 2);
                }
            }
            music.setReplaceCurrentMusic(false);
        }
        else if (this.onCooldown) {
            if (this.cooldownTicks > MAX_TICKS) {
                this.onCooldown = false;
                this.cooldownTicks = 0;
            } else {
                this.cooldownTicks++;
                return;
            }
        }
        if ((this.pendingUpdate || replaceCurrentMusic) && musicManager.nextSongDelay - 1 <= 0) {
            if (music.getBoxStructure().equals(EMPTY) && music.getPieceStructure().equals(EMPTY)) {
                this.pendingTicks++;
            } else {
                this.pendingUpdate = false;
                var pieceMusic = StructureMusicImpl.getStructureMusic(music.getStructureMusic(), music.getPieceStructure(), this.level);
                if (pieceMusic != null) {
                    cir.setReturnValue(pieceMusic.music());
                } else {
                    var boxMusic = StructureMusicImpl.getStructureMusic(music.getStructureMusic(), music.getBoxStructure(), this.level);
                    if (boxMusic != null && boxMusic.fullBox()) {
                        cir.setReturnValue(boxMusic.music());
                    }
                }
                music.setBoxStructure(EMPTY);
                music.setPieceStructure(EMPTY);
                this.pendingTicks = 0;
            }
        }
        if (musicManager.currentMusic == null && musicManager.nextSongDelay - 21 <= 0) {
            if (pendingTicks > MAX_TICKS) {
                pendingTicks = 0;
                this.pendingUpdate = false;
                this.onCooldown = true;
                this.cooldownTicks = 0;
                return;
            }
            UnifiedClientHelpers.NETWORKING.send(new StructurePackets.Request());
            this.pendingUpdate = true;
            this.pendingTicks = 0;
        }
    }

    @Inject(method = "setLevel", at = @At(value = "HEAD"))
    private void stopClientLevel(ClientLevel level, CallbackInfo ci) {
        if (level != null) {
            ClientEventsImpl.Instance.passOnLevelUnload(level);
            EventsImpl.Levels.passOnUnload(level);
        }
    }

    @WrapOperation(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;ZZ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;onDisconnected()V"))
    private void stopClientLevel(Hud hud, Operation<Void> original) {
        if (this.level != null) {
            ClientEventsImpl.Instance.passOnLevelUnload(this.level);
            EventsImpl.Levels.passOnUnload(this.level);
        }
        original.call(hud);
    }
}
