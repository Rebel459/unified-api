package net.rebel459.unified.util.helper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.Music;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.rebel459.unified.network.StructurePackets;
import net.rebel459.unified.platform.UnifiedHelpers;
import net.rebel459.unified.util.mixin.PlayerStructureMusic;

import java.util.HashMap;
import java.util.List;

public class StructureMusicImpl {

    public static void init() {
        UnifiedHelpers.NETWORKING.registerPlayToServer(StructurePackets.Request.TYPE, StructurePackets.Request.CODEC, (packet, player) -> {
            ServerLevel level = player.level();
            Identifier pieceStructure = Identifier.withDefaultNamespace("empty");
            Identifier boxStructure = Identifier.withDefaultNamespace("empty");

            Registry<Structure> registry = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
            List<Structure> structures = registry.stream().toList();
            for (Structure structure : structures) {
                if (level.structureManager().getStructureWithPieceAt(BlockPos.containing(player.position()), structure).isValid()) {
                    pieceStructure = registry.getKey(structure);
                    boxStructure = registry.getKey(structure);
                    break;
                }
                else if (level.structureManager().getStructureAt(BlockPos.containing(player.position()), structure).isValid()) boxStructure = registry.getKey(structure);
            }
            UnifiedHelpers.NETWORKING.send(new StructurePackets.Send(pieceStructure, boxStructure), player);
        });
        UnifiedHelpers.NETWORKING.registerPlayToClient(StructurePackets.Send.TYPE, StructurePackets.Send.CODEC, (packet, player) -> {
            if (player instanceof PlayerStructureMusic music) {
                music.setBoxStructure(packet.boxStructure());
                music.setPieceStructure(packet.pieceStructure());
            }
        });
    }

    public static HashMap<Identifier, StructureMusicImpl.Record> STRUCTURE_MUSIC = new HashMap<>();

    public record Record(Music music, boolean fullBox) {}
}
