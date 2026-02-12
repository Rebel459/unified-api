package net.rebel459.unified.test;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.rebel459.unified.Unified;

public record PingPayload(String message) implements CustomPacketPayload {

    public static final Type<PingPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Unified.MOD_ID, "ping"));

    public static final StreamCodec<FriendlyByteBuf, PingPayload> CODEC = StreamCodec.of(
        (buf, payload) -> buf.writeUtf(payload.message),
        buf -> new PingPayload(buf.readUtf(32767))
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}