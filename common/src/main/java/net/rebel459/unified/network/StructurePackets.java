package net.rebel459.unified.network;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.rebel459.unified.util.helper.impl.StructureMusicImpl;

import java.util.HashMap;
import java.util.Map;

public class StructurePackets {
    public record Request() implements CustomPacketPayload {

        public static final Type<Request> TYPE = new Type<>(Identifier.fromNamespaceAndPath("unified", "request_structure_music"));

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

    public record Send(Identifier pieceStructure, Identifier boxStructure, boolean replaceCurrentMusic) implements CustomPacketPayload {

        public static final Type<Send> TYPE = new Type<>(Identifier.fromNamespaceAndPath("unified", "send_structure_music"));

        public static final StreamCodec<FriendlyByteBuf, Send> CODEC =
                CustomPacketPayload.codec(
                        (packet, buf) -> {
                            buf.writeIdentifier(packet.pieceStructure());
                            buf.writeIdentifier(packet.boxStructure());
                            buf.writeBoolean(packet.replaceCurrentMusic);
                        },
                        buf -> new Send(buf.readIdentifier(), buf.readIdentifier(), buf.readBoolean())
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record Sync(Map<StructureMusicImpl.Target, StructureMusicImpl.Info> structureMusic) implements CustomPacketPayload {

        public static final Type<Sync> TYPE = new Type<>(Identifier.fromNamespaceAndPath("unified", "sync_structure_music"));

        public static final StreamCodec<FriendlyByteBuf, Sync> CODEC =
                CustomPacketPayload.codec(
                        (packet, buf) -> buf.writeMap(
                                packet.structureMusic,
                                (recordBuf, target) -> {
                                    recordBuf.writeIdentifier(target.id());
                                    recordBuf.writeBoolean(target.tag());
                                },
                                (recordBuf, info) -> {
                                    recordBuf.writeIdentifier(BuiltInRegistries.SOUND_EVENT.getKey(info.music().sound().value()));
                                    recordBuf.writeVarInt(info.music().minDelay());
                                    recordBuf.writeVarInt(info.music().maxDelay());
                                    recordBuf.writeBoolean(info.music().replaceCurrentMusic());
                                    recordBuf.writeBoolean(info.fullBox());
                                }
                        ),
                        buf -> new Sync(buf.readMap(
                                HashMap::new,
                                recordBuf -> new StructureMusicImpl.Target(recordBuf.readIdentifier(), recordBuf.readBoolean()),
                                recordBuf -> {
                                    Identifier soundId = recordBuf.readIdentifier();
                                    SoundEvent soundEvent = BuiltInRegistries.SOUND_EVENT.getValue(soundId);
                                    if (soundEvent == null) {
                                        soundEvent = SoundEvents.EMPTY;
                                    }

                                    Music music = new Music(
                                            Holder.direct(soundEvent),
                                            recordBuf.readVarInt(),
                                            recordBuf.readVarInt(),
                                            recordBuf.readBoolean()
                                    );

                                    return new StructureMusicImpl.Info(music, recordBuf.readBoolean());
                                }
                        ))
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
