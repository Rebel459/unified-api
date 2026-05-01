package net.rebel459.unified.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.util.Mth;
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
            Identifier identifier = music.getPieceStructure();
            if (identifier == EMPTY && music.getStructureMusic().get(music.getBoxStructure()).fullBox()) identifier = music.getBoxStructure();
            var structureMusic = music.getStructureMusic().get(identifier);
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
            if (music.getBoxStructure() == EMPTY && music.getPieceStructure() == EMPTY) {
                this.pendingTicks++;
            } else {
                this.pendingUpdate = false;
                if (music.getPieceStructure() != EMPTY) {
                    var pieceMusic = music.getStructureMusic().get(music.getPieceStructure());
                    if (pieceMusic != null) {
                        cir.setReturnValue(pieceMusic.music());
                    }
                }
                else if (music.getBoxStructure() != EMPTY) {
                    var boxMusic = music.getStructureMusic().get(music.getBoxStructure());
                    if (boxMusic != null) {
                        if (boxMusic.fullBox()) {
                            cir.setReturnValue(boxMusic.music());
                        }
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

    @WrapOperation(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;ZZ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;onDisconnected()V"))
    private void stopClientLevel(Gui gui, Operation<Void> original) {
        if (this.level != null) {
            ClientEventsImpl.Instance.passOnLevelUnload(this.level);
            EventsImpl.Levels.passOnUnload(this.level);
        }
        original.call(gui);
    }
}
