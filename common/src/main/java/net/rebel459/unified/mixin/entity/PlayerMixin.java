package net.rebel459.unified.mixin.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.rebel459.unified.Unified;
import net.rebel459.unified.tag.UnifiedItemTags;
import net.rebel459.unified.util.mixin.PlayerStructureMusic;
import net.rebel459.unified.util.tag.PersistentCooldowns;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Player.class)
public class PlayerMixin implements PlayerStructureMusic {

    @Unique
    private Identifier pieceStructure = Identifier.withDefaultNamespace("empty");

    @Unique
    private Identifier boxStructure = Identifier.withDefaultNamespace("empty");

    @Override
    public Identifier getPieceStructure() {
        return this.pieceStructure;
    }

    @Override
    public Identifier getBoxStructure() {
        return this.boxStructure;
    }

    @Override
    public void setPieceStructure(Identifier id) {
        this.pieceStructure = id;
    }

    @Override
    public void setBoxStructure(Identifier id) {
        this.boxStructure = id;
    }
}