package net.rebel459.unified.util.helper.impl;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.Music;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.rebel459.unified.network.StructurePackets;
import net.rebel459.unified.platform.UnifiedEvents;
import net.rebel459.unified.platform.UnifiedHelpers;
import net.rebel459.unified.util.EventType;
import net.rebel459.unified.util.mixin.PlayerStructureMusic;

import java.util.*;

public class StructureMusicImpl {

    private static final int TOTAL_PLAYER_GROUPS = 5;
    private static final Identifier EMPTY = Identifier.withDefaultNamespace("empty");

    static boolean structureSync = false;
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
        UnifiedEvents.Server.onTick(EventType.PRE, server -> {
            if (!structureSync) return;
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

                        var pieceMusic = getStructureMusic(STRUCTURE_MUSIC, pieceStructure, level);
                        var boxMusic = getStructureMusic(STRUCTURE_MUSIC, boxStructure, level);

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
        UnifiedEvents.Server.onDatapackLoad(server -> {
            server.getAllLevels().forEach(level -> {
                level.players().forEach(player -> {
                    Registry<Structure> registry = player.level().registryAccess().lookupOrThrow(Registries.STRUCTURE);
                    UnifiedHelpers.NETWORKING.send(new StructurePackets.Sync(resolveStructureMusic(registry)), player);
                });
            });
        });
        UnifiedEvents.Players.onJoin(player -> {
            if (player instanceof ServerPlayer serverPlayer) {
                Registry<Structure> registry = serverPlayer.level().registryAccess().lookupOrThrow(Registries.STRUCTURE);
                UnifiedHelpers.NETWORKING.send(new StructurePackets.Sync(resolveStructureMusic(registry)), serverPlayer);
            }
            if (player instanceof PlayerStructureMusic music) {
                music.setPlayerGroup(Math.floorMod(player.getUUID().hashCode(), TOTAL_PLAYER_GROUPS) + 1);
            }
        });
    }

    public static StructureMusicImpl.Info getStructureMusic(Map<Target, Info> music, Identifier structure, Level level) {;
        var structureMusic = music.get(new StructureMusicImpl.Target(structure, false));
        if (structureMusic == null && level != null && !EMPTY.equals(structure)) {
            for (var entry : music.entrySet()) {
                if (entry.getKey().tag()) {
                    Optional<Holder.Reference<Structure>> optionalStructure = level.registryAccess()
                            .lookup(Registries.STRUCTURE)
                            .flatMap(registry -> registry.get(structure));
                    if (optionalStructure.isEmpty()) {
                        continue;
                    }
                    if (optionalStructure.get().is(TagKey.create(Registries.STRUCTURE, entry.getKey().id()))) {
                        structureMusic = entry.getValue();
                        break;
                    }
                }
            }
        }
        return structureMusic;
    }

    private static HashMap<Target, Info> resolveStructureMusic(Registry<Structure> registry) {
        HashMap<Target, Info> resolved = new HashMap<>();

        for (var entry : STRUCTURE_MUSIC.entrySet()) {
            Target target = entry.getKey();
            if (!target.tag()) continue;

            registry.get(TagKey.create(Registries.STRUCTURE, target.id())).ifPresent(structures -> {
                for (Holder<Structure> structure : structures) {
                    structure.unwrapKey().ifPresent(key -> resolved.put(new Target(key.identifier(), false), entry.getValue()));
                }
            });
        }

        for (var entry : STRUCTURE_MUSIC.entrySet()) {
            if (!entry.getKey().tag()) {
                resolved.put(entry.getKey(), entry.getValue());
            }
        }

        return resolved;
    }

    public static void addStructure(Identifier structure, Music music, boolean fullBox) {
        StructureMusicImpl.STRUCTURE_MUSIC.put(new Target(structure, false), new Info(music, fullBox));
        afterStructureAdded(music);
    }

    public static void addStructureTag(TagKey<Structure> structure, Music music, boolean fullBox) {
        StructureMusicImpl.STRUCTURE_MUSIC.put(new Target(structure.location(), true), new Info(music, fullBox));
        afterStructureAdded(music);
    }

    private static void afterStructureAdded(Music music) {
        StructureMusicImpl.updateStructures();
        if (music.replaceCurrentMusic()) StructureMusicImpl.enableAutoSync();
    }

    public static void enableAutoSync() {
        structureSync = true;
    }

    public static void updateStructures() {
        shouldUpdateStructures = true;
    }

    private static List<Structure> getStructures(Registry<Structure> registry) {
        if (shouldUpdateStructures) {
            structures = registry.keySet().stream()
                    .map(registry::getValue)
                    .filter(Objects::nonNull)
                    .toList();
            shouldUpdateStructures = false;
        }
        return structures;
    }

    public static HashMap<Target, Info> STRUCTURE_MUSIC = new HashMap<>();

    public record Target(Identifier id, boolean tag) {}
    public record Info(Music music, boolean fullBox) {}
}
