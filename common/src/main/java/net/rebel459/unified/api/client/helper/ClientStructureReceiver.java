package net.rebel459.unified.api.client.helper;

import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.rebel459.unified.impl.network.StructurePacketImpl;

import java.util.Set;

public class ClientStructureReceiver {

    public static Set<ResourceKey<Structure>> getPieceStructures() {
        return StructurePacketImpl.getClientStructures().piece().getFirst();
    }
    public static Set<TagKey<Structure>> getPieceStructureTags() {
        return StructurePacketImpl.getClientStructures().piece().getSecond();
    }

    public static Set<ResourceKey<Structure>> getBoxStructures() {
        return StructurePacketImpl.getClientStructures().box().getFirst();
    }
    public static Set<TagKey<Structure>> getBoxStructureTags() {
        return StructurePacketImpl.getClientStructures().box().getSecond();
    }
}
