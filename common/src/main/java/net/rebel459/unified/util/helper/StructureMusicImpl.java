package net.rebel459.unified.util.helper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.Music;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.rebel459.unified.network.StructurePackets;
import net.rebel459.unified.platform.UnifiedEvents;
import net.rebel459.unified.platform.UnifiedHelpers;
import net.rebel459.unified.util.mixin.PlayerStructureMusic;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class StructureMusicImpl {

    private static final int TOTAL_PLAYER_GROUPS = 5;

    static boolean hasReplaceMusic = false;
    static int serverTicks = 0;
    static int currentPlayerGroup = 1;
    private static boolean shouldUpdateStructures = true;
    private static List<Structure> structures = List.of();

    public static void init() {
        UnifiedHelpers.NETWORKING.registerPlayToServer(StructurePackets.Request.TYPE, StructurePackets.Request.CODEC, (packet, player) -> {
            ServerLevel level = player.level();
            Identifier pieceStructure = Identifier.withDefaultNamespace("empty");
            Identifier boxStructure = Identifier.withDefaultNamespace("empty");

            Registry<Structure> registry = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
            List<Structure> structures = getStructures(registry);
            for (Structure structure : structures) {
                if (level.structureManager().getStructureWithPieceAt(BlockPos.containing(player.position()), structure).isValid()) {
                    pieceStructure = registry.getKey(structure);
                    boxStructure = registry.getKey(structure);
                    break;
                }
                else if (level.structureManager().getStructureAt(BlockPos.containing(player.position()), structure).isValid()) boxStructure = registry.getKey(structure);
            }
            UnifiedHelpers.NETWORKING.send(new StructurePackets.Send(pieceStructure, boxStructure, false), player);
        });
        UnifiedHelpers.NETWORKING.registerPlayToClient(StructurePackets.Send.TYPE, StructurePackets.Send.CODEC, (packet, player) -> {
            if (player instanceof PlayerStructureMusic music) {
                music.setBoxStructure(packet.boxStructure());
                music.setPieceStructure(packet.pieceStructure());
                music.setReplaceCurrentMusic(packet.replaceCurrentMusic());
            }
        });
        UnifiedHelpers.NETWORKING.registerPlayToClient(StructurePackets.Sync.TYPE, StructurePackets.Sync.CODEC, (packet, player) -> {
            if (player instanceof PlayerStructureMusic music) {
                music.setStructureMusic(packet.structureMusic());
            }
        });
        UnifiedEvents.Servers.onTickStart(server -> {
            if (!hasReplaceMusic) return;
            if (++serverTicks <= 20) return;
            serverTicks = 0;
            server.getAllLevels().forEach(level -> {
                Registry<Structure> registry = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
                var structureManager = level.structureManager();
                List<Structure> structures = getStructures(registry);

                for (ServerPlayer player : level.players()) {
                    if (!(player instanceof PlayerStructureMusic music) || music.getPlayerGroup() != currentPlayerGroup) continue;
                    BlockPos pos = BlockPos.containing(player.position());

                    Identifier pieceStructure = Identifier.withDefaultNamespace("empty");
                    Identifier boxStructure = Identifier.withDefaultNamespace("empty");

                    for (Structure structure : structures) {
                        Identifier id = registry.getKey(structure);

                        if (structureManager.getStructureWithPieceAt(pos, structure).isValid()) {
                            pieceStructure = id;
                            boxStructure = id;
                            break;
                        }

                        if (structureManager.getStructureAt(pos, structure).isValid()) {
                            boxStructure = id;
                        }
                    }

                    if (!Objects.equals(pieceStructure, music.getPieceStructure()) || !Objects.equals(boxStructure, music.getBoxStructure())) {
                        music.setPieceStructure(pieceStructure);
                        music.setBoxStructure(boxStructure);

                        var pieceMusic = STRUCTURE_MUSIC.get(pieceStructure);
                        var boxMusic = STRUCTURE_MUSIC.get(boxStructure);

                        if ((pieceMusic != null && pieceMusic.music().replaceCurrentMusic())
                                || (boxMusic != null && boxMusic.music().replaceCurrentMusic())) {
                            UnifiedHelpers.NETWORKING.send(new StructurePackets.Send(pieceStructure, boxStructure, true), player);
                        }
                    }
                }
            });
            currentPlayerGroup += 1;
            if (currentPlayerGroup > TOTAL_PLAYER_GROUPS) currentPlayerGroup = 1;
        });
        UnifiedEvents.Players.onJoin(player -> {
            if (player instanceof ServerPlayer serverPlayer) {
                UnifiedHelpers.NETWORKING.send(new StructurePackets.Sync(new HashMap<>(STRUCTURE_MUSIC)), serverPlayer);
            }
            if (player instanceof PlayerStructureMusic music) {
                music.setPlayerGroup(Math.floorMod(player.getUUID().hashCode(), TOTAL_PLAYER_GROUPS) + 1);
            }
        });
    }

    static void updateStructures() {
        shouldUpdateStructures = true;
    }

    private static List<Structure> getStructures(Registry<Structure> registry) {
        if (shouldUpdateStructures) {
            structures = STRUCTURE_MUSIC.keySet().stream()
                    .map(registry::getValue)
                    .filter(Objects::nonNull)
                    .toList();
            shouldUpdateStructures = false;
        }
        return structures;
    }

    public static HashMap<Identifier, StructureMusicImpl.Record> STRUCTURE_MUSIC = new HashMap<>();

    public record Record(Music music, boolean fullBox) {}
}
