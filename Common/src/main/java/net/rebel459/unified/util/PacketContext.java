package net.rebel459.unified.util;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public interface PacketContext {
    ServerPlayer player();

    void respond(CustomPacketPayload payload);
}