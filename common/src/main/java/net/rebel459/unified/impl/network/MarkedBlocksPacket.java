package net.rebel459.unified.impl.network;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.rebel459.unified.Unified;

public record MarkedBlocksPacket(ResourceKey<Level> dimension, boolean reset, long[] added, long[] removed) implements CustomPacketPayload {

    public static final Type<MarkedBlocksPacket> TYPE = new Type<>(Unified.id("marked_blocks"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MarkedBlocksPacket> CODEC =
            StreamCodec.composite(
                    ResourceKey.streamCodec(Registries.DIMENSION), MarkedBlocksPacket::dimension,
                    ByteBufCodecs.BOOL, MarkedBlocksPacket::reset,
                    ByteBufCodecs.LONG_ARRAY, MarkedBlocksPacket::added,
                    ByteBufCodecs.LONG_ARRAY, MarkedBlocksPacket::removed,
                    MarkedBlocksPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
