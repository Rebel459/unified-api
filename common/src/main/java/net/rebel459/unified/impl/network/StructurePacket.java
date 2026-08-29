package net.rebel459.unified.impl.network;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.rebel459.unified.Unified;

import java.util.HashSet;
import java.util.Set;

public record StructurePacket(Set<ResourceKey<Structure>> pieceStructures, Set<TagKey<Structure>> pieceStructureTags, Set<ResourceKey<Structure>> boxStructures, Set<TagKey<Structure>> boxStructureTags) implements CustomPacketPayload {

        public static final Type<StructurePacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Unified.MOD_ID, "structure"));

    public static final StreamCodec<RegistryFriendlyByteBuf, StructurePacket> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.collection(HashSet::new, ResourceKey.streamCodec(Registries.STRUCTURE)), StructurePacket::pieceStructures,
                    ByteBufCodecs.collection(HashSet::new, TagKey.streamCodec(Registries.STRUCTURE)), StructurePacket::pieceStructureTags,
                    ByteBufCodecs.collection(HashSet::new, ResourceKey.streamCodec(Registries.STRUCTURE)), StructurePacket::boxStructures,
                    ByteBufCodecs.collection(HashSet::new, TagKey.streamCodec(Registries.STRUCTURE)), StructurePacket::boxStructureTags,
                    StructurePacket::new
            );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }