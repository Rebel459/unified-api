package net.rebel459.unified.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.rebel459.unified.network.StructurePackets;
import net.rebel459.unified.platform.client.UnifiedClientHelpers;
import net.rebel459.unified.util.helper.StructureMusicImpl;
import net.rebel459.unified.util.mixin.PlayerStructureMusic;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow
    @Nullable
    public LocalPlayer player;

    @Shadow
    @Final
    private MusicManager musicManager;

    @Unique
    private boolean pendingUpdate = false;

    @Unique
    private int pendingTicks = 0;

    @Unique
    private final Identifier EMPTY = Identifier.withDefaultNamespace("empty");

    @Inject(method = "getSituationalMusic", at = @At(value = "HEAD"), cancellable = true)
    private void passRenderCrosshair(CallbackInfoReturnable<Music> cir) {
        if (StructureMusicImpl.STRUCTURE_MUSIC.isEmpty() || cir.getReturnValue() == Musics.MENU) return;
        LocalPlayer player = this.player;
        if (player == null) return;
        if (player.getAbilities().instabuild && player.getAbilities().mayfly) return;
        if (this.pendingUpdate && this.player instanceof PlayerStructureMusic music && this.musicManager.nextSongDelay - 1 <= 0) {
            if (music.getBoxStructure() == EMPTY && music.getPieceStructure() == EMPTY) {
                this.pendingTicks++;
            } else {
                this.pendingUpdate = false;
                if (music.getPieceStructure() != EMPTY) {
                    var pieceMusic = StructureMusicImpl.STRUCTURE_MUSIC.get(music.getPieceStructure());
                    if (pieceMusic != null) cir.setReturnValue(pieceMusic.music());
                }
                else if (music.getBoxStructure() != EMPTY) {
                    var boxMusic = StructureMusicImpl.STRUCTURE_MUSIC.get(music.getPieceStructure());
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
        MusicManager musicManager = this.musicManager;
        SoundInstance currentMusic = musicManager.currentMusic;
        if (currentMusic == null && musicManager.nextSongDelay - 21 <= 0) {
            if (pendingTicks > 300) {
                pendingTicks = 0;
                return;
            }
            UnifiedClientHelpers.NETWORKING.send(new StructurePackets.Request());
            this.pendingUpdate = true;
            this.pendingTicks = 0;
        }
    }
}