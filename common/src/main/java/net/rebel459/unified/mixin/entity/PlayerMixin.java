package net.rebel459.unified.mixin.entity;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.rebel459.unified.platform.EventsImpl;
import net.rebel459.unified.util.EventType;
import net.rebel459.unified.util.helper.impl.StructureMusicImpl;
import net.rebel459.unified.util.mixin.PlayerStructureMusic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(Player.class)
public class PlayerMixin implements PlayerStructureMusic {

    @Unique
    private Identifier pieceStructure = Identifier.withDefaultNamespace("empty");

    @Unique
    private Identifier boxStructure = Identifier.withDefaultNamespace("empty");

    @Unique
    private boolean replaceCurrentMusic = false;

    @Unique
    private Map<StructureMusicImpl.Target, StructureMusicImpl.Info> structureMusic = StructureMusicImpl.STRUCTURE_MUSIC;

    @Unique
    private int playerGroup = 1;

    @Override
    public Identifier getPieceStructure() {
        return this.pieceStructure;
    }

    @Override
    public Identifier getBoxStructure() {
        return this.boxStructure;
    }

    @Override
    public boolean getReplaceCurrentMusic() {
        return this.replaceCurrentMusic;
    }

    @Override
    public Map<StructureMusicImpl.Target, StructureMusicImpl.Info> getStructureMusic() {
        return this.structureMusic;
    }

    @Override
    public int getPlayerGroup() {
        return this.playerGroup;
    }

    @Override
    public void setPieceStructure(Identifier id) {
        this.pieceStructure = id;
    }

    @Override
    public void setBoxStructure(Identifier id) {
        this.boxStructure = id;
    }

    @Override
    public void setReplaceCurrentMusic(boolean replaceCurrentMusic) {
        this.replaceCurrentMusic = replaceCurrentMusic;
    }

    @Override
    public void setStructureMusic(Map<StructureMusicImpl.Target, StructureMusicImpl.Info> structureMusic) {
        this.structureMusic = structureMusic;
    }

    @Override
    public void setPlayerGroup(int playerGroup) {
        this.playerGroup = playerGroup;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void preTick(CallbackInfo ci) {
        Player player = Player.class.cast(this);
        EventsImpl.Players.passOnTick(EventType.PRE, player);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void postTick(CallbackInfo ci) {
        Player player = Player.class.cast(this);
        EventsImpl.Players.passOnTick(EventType.POST, player);
    }
}