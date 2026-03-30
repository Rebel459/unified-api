package net.rebel459.unified.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public class StructurePackets {
    public record Request() implements CustomPacketPayload {

        public static final Type<Request> TYPE = new Type<>(Identifier.fromNamespaceAndPath("unified", "request_structure"));

        public static final StreamCodec<FriendlyByteBuf, Request> CODEC =
                CustomPacketPayload.codec(
                        (packet, buf) -> {},
                        buf -> new Request()
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record Send(Identifier pieceStructure, Identifier boxStructure) implements CustomPacketPayload {

        public static final Type<Send> TYPE = new Type<>(Identifier.fromNamespaceAndPath("unified", "send_structure"));

        public static final StreamCodec<FriendlyByteBuf, Send> CODEC =
                CustomPacketPayload.codec(
                        (packet, buf) -> {
                            buf.writeIdentifier(packet.pieceStructure());
                            buf.writeIdentifier(packet.boxStructure());
                        },
                        buf -> new Send(buf.readIdentifier(), buf.readIdentifier())
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
