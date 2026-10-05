package net.rebel459.unified.impl.network;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.rebel459.unified.api.core.UnifiedEvents;
import net.rebel459.unified.api.core.UnifiedHelpers;
import net.rebel459.unified.api.event.EventTiming;

import java.util.*;

public class StructurePacketImpl {

    private static int serverTicks = 0;
    private static boolean shouldUpdateStructures = true;
    private static final Map<ResourceKey<Level>, List<Holder.Reference<Structure>>> STRUCTURES = new HashMap<>();

    public static void init() {
        UnifiedHelpers.NETWORKING.registerPlayToClient(StructurePacket.TYPE, StructurePacket.CODEC, (packet, player) -> {
            clientStructures = new Info(Pair.of(packet.pieceStructures(), packet.pieceStructureTags()), Pair.of(packet.boxStructures(), packet.boxStructureTags()));
        });
        UnifiedEvents.Players.onJoin(player -> clearPlayerState(player.getUUID()));
        UnifiedEvents.Players.onLeave(player -> clearPlayerState(player.getUUID()));
        UnifiedEvents.Server.onDatapackLoad(server -> {
            shouldUpdateStructures = true;
            STRUCTURES.clear();
            LAST_STRUCTURES.clear();
            LAST_POSITION.clear();
        });
        UnifiedEvents.Server.onTick(EventTiming.PRE, server -> {
            if (++serverTicks < 20) return;
            serverTicks = 0;
            Map<ResourceKey<Level>, List<Holder.Reference<Structure>>> structures = getStructures(server);
            server.getAllLevels().forEach(level -> {
                StructureManager structureManager = level.structureManager();
                List<Holder.Reference<Structure>> levelStructures = structures.get(level.dimension());
                if (levelStructures == null) return;

                for (ServerPlayer player : level.players()) {
                    PlayerKey playerKey = new PlayerKey(player.getUUID(), level.dimension());
                    BlockPos pos = BlockPos.containing(player.position());
                    if (LAST_POSITION.get(playerKey) != null && LAST_POSITION.get(playerKey).equals(player.blockPosition())) continue;
                    HashSet<ResourceKey<Structure>> boxStructureIds = new HashSet<>();
                    HashSet<TagKey<Structure>> boxStructureTags = new HashSet<>();
                    HashSet<ResourceKey<Structure>> pieceStructureIds = new HashSet<>();
                    HashSet<TagKey<Structure>> pieceStructureTags = new HashSet<>();
                    for (Holder<Structure> structure : levelStructures) {
                        if (structure.unwrapKey().isEmpty()) continue;
                        if (structureManager.getStructureAt(pos, structure.value()).isValid()) {
                            boxStructureIds.add(structure.unwrapKey().get());
                            boxStructureTags.addAll(structure.tags().toList());
                        }
                        if (structureManager.getStructureWithPieceAt(pos, structure.value()).isValid()) {
                            pieceStructureIds.add(structure.unwrapKey().get());
                            pieceStructureTags.addAll(structure.tags().toList());
                        }
                    }
                    Info info = new Info(Pair.of(pieceStructureIds, pieceStructureTags), Pair.of(boxStructureIds, boxStructureTags));
                    LAST_POSITION.put(playerKey, player.blockPosition());
                    if (LAST_STRUCTURES.get(playerKey) == null || !LAST_STRUCTURES.get(playerKey).equals(info)) {
                        UnifiedHelpers.NETWORKING.send(new StructurePacket(info.piece.getFirst(), info.piece.getSecond(), info.box.getFirst(), info.box.getSecond()), player);
                        LAST_STRUCTURES.put(playerKey, info);
                    }
                }
            });
        });
    }

    private static Map<ResourceKey<Level>, List<Holder.Reference<Structure>>> getStructures(MinecraftServer server) {
        if (shouldUpdateStructures) {
            server.getAllLevels().forEach(level -> STRUCTURES.put(level.dimension(), level.registryAccess().lookupOrThrow(Registries.STRUCTURE).listElements().toList()));
            shouldUpdateStructures = false;
        }
        return STRUCTURES;
    }

    private record PlayerKey(UUID id, ResourceKey<Level> dimension) {}

    private static final Map<PlayerKey, Info> LAST_STRUCTURES = new HashMap<>();
    private static final Map<PlayerKey, BlockPos> LAST_POSITION = new HashMap<>();

    private static Info clientStructures = new Info(Pair.of(Set.of(), Set.of()), (Pair.of(Set.of(), Set.of())));

    public static Info getClientStructures() {
        return clientStructures;
    }

    public static void resetClientStructures() {
        clientStructures = new Info(Pair.of(Set.of(), Set.of()), Pair.of(Set.of(), Set.of()));
    }

    private static void clearPlayerState(UUID playerId) {
        LAST_STRUCTURES.keySet().removeIf(key -> key.id().equals(playerId));
        LAST_POSITION.keySet().removeIf(key -> key.id().equals(playerId));
    }

    public record Info(Pair<Set<ResourceKey<Structure>>, Set<TagKey<Structure>>> piece, Pair<Set<ResourceKey<Structure>>, Set<TagKey<Structure>>> box) {}
}
