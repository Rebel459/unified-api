package net.rebel459.unified.impl.network;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.rebel459.unified.impl.helper.StructureMusicImpl;

import java.util.HashMap;
import java.util.Map;

public class StructurePackets {
    public record Request() implements CustomPacketPayload {

        public static final Type<Request> TYPE = new Type<>(Identifier.fromNamespaceAndPath("unified", "request_structure_music"));

        public static final StreamCodec<FriendlyByteBuf, Request> CODEC = StreamCodec.unit(new Request());

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record Send(Identifier pieceStructure, Identifier boxStructure, boolean replaceCurrentMusic) implements CustomPacketPayload {

        public static final Type<Send> TYPE = new Type<>(Identifier.fromNamespaceAndPath("unified", "send_structure_music"));

        public static final StreamCodec<FriendlyByteBuf, Send> CODEC = StreamCodec.composite(
                Identifier.STREAM_CODEC,
                Send::pieceStructure,
                Identifier.STREAM_CODEC,
                Send::boxStructure,
                ByteBufCodecs.BOOL,
                Send::replaceCurrentMusic,
                Send::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record Sync(Map<StructureMusicImpl.Target, StructureMusicImpl.Info> structureMusic) implements CustomPacketPayload {

        public static final Type<Sync> TYPE = new Type<>(Identifier.fromNamespaceAndPath("unified", "sync_structure_music"));

        private static final StreamCodec<FriendlyByteBuf, StructureMusicImpl.Target> TARGET_CODEC = StreamCodec.composite(
                Identifier.STREAM_CODEC,
                StructureMusicImpl.Target::id,
                ByteBufCodecs.BOOL,
                StructureMusicImpl.Target::tag,
                StructureMusicImpl.Target::new
        );

        private static final StreamCodec<FriendlyByteBuf, StructureMusicImpl.Info> INFO_CODEC = StreamCodec.composite(
                Identifier.STREAM_CODEC,
                info -> BuiltInRegistries.SOUND_EVENT.getKey(info.music().sound().value()),
                ByteBufCodecs.VAR_INT,
                info -> info.music().minDelay(),
                ByteBufCodecs.VAR_INT,
                info -> info.music().maxDelay(),
                ByteBufCodecs.BOOL,
                info -> info.music().replaceCurrentMusic(),
                ByteBufCodecs.BOOL,
                StructureMusicImpl.Info::fullBox,
                (soundId, minDelay, maxDelay, replaceCurrentMusic, fullBox) -> {
                    SoundEvent soundEvent = BuiltInRegistries.SOUND_EVENT.getValue(soundId);
                    if (soundEvent == null) soundEvent = SoundEvents.EMPTY;
                    return new StructureMusicImpl.Info(new Music(Holder.direct(soundEvent), minDelay, maxDelay, replaceCurrentMusic), fullBox);
                }
        );

        private static final StreamCodec<FriendlyByteBuf, Map<StructureMusicImpl.Target, StructureMusicImpl.Info>> MAP_CODEC = ByteBufCodecs.map(HashMap::new, TARGET_CODEC, INFO_CODEC);

        public static final StreamCodec<FriendlyByteBuf, Sync> CODEC = StreamCodec.composite(MAP_CODEC, Sync::structureMusic, Sync::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
