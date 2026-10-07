package net.rebel459.unified.impl.core;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockTransformers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.rebel459.unified.api.event.BiomeModificationContext;
import net.rebel459.unified.api.util.BlockLike;
import net.rebel459.unified.impl.helper.BlockConversionsImpl;
import net.rebel459.unified.impl.platform.PlatformLoader;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class CommonHelpers {

    public interface DataPacks {

        void addRequired(Identifier id);
        void addOptional(Identifier id);
    }

    public interface Networking {

        <T extends CustomPacketPayload> void registerPlayToServer(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec);
        <T extends CustomPacketPayload> void registerPlayToClient(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec);
        <T extends CustomPacketPayload> void registerConfigToServer(CustomPacketPayload.Type<T> type, StreamCodec<? super FriendlyByteBuf, T> codec);
        <T extends CustomPacketPayload> void registerConfigToClient(CustomPacketPayload.Type<T> type, StreamCodec<? super FriendlyByteBuf, T> codec);

        <T extends CustomPacketPayload> void registerPlayToServer(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, BiConsumer<T, ServerPlayer> handler);
        <T extends CustomPacketPayload> void registerPlayToClient(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, BiConsumer<T, Player> handler);
        <T extends CustomPacketPayload> void registerConfigToServer(CustomPacketPayload.Type<T> type, StreamCodec<? super FriendlyByteBuf, T> codec, Consumer<T> handler);
        <T extends CustomPacketPayload> void registerConfigToClient(CustomPacketPayload.Type<T> type, StreamCodec<? super FriendlyByteBuf, T> codec, Consumer<T> handler);

        boolean canSend(CustomPacketPayload payload, ServerPlayer player);
        void send(CustomPacketPayload payload, ServerPlayer player);
    }

    public interface BlockConversions {

        default void addStrippable(BlockLike originalBlock, BlockLike convertedBlock) {
            add(this::hasAxeTransforming, originalBlock, convertedBlock, SoundEvents.AXE_STRIP);
        }

        default void addWeathering(BlockLike unaffected, BlockLike exposed, BlockLike weathered, BlockLike oxidized, BlockLike waxed, BlockLike waxedExposed, BlockLike waxedWeathered, BlockLike waxedOxidized) {
            List<Pair<BlockLike, BlockLike>> waxPairs = List.of(Pair.of(unaffected, waxed), Pair.of(exposed, waxedExposed), Pair.of(weathered, waxedWeathered), Pair.of(oxidized, waxedOxidized));
            List<Pair<BlockLike, BlockLike>> oxidizationPairs = List.of(Pair.of(oxidized, weathered), Pair.of(weathered, exposed), Pair.of(exposed, unaffected));
            for (Pair<BlockLike, BlockLike> pair : waxPairs) {
                add(stack -> stack.getItem() instanceof HoneycombItem, pair.getFirst(), pair.getSecond(), (context -> {
                    Player player = context.getPlayer();
                    Level level = context.getLevel();
                    BlockPos pos = context.getClickedPos();
                    BlockState oldState = level.getBlockState(pos);
                    if (player == null) return;
                    context.getItemInHand().shrink(1);
                    level.levelEvent(player, 3003, pos, 0);
                    if (oldState.getBlock() instanceof ChestBlock && oldState.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
                        BlockPos neighborPos = ChestBlock.getConnectedBlockPos(pos, oldState);
                        level.gameEvent(GameEvent.BLOCK_CHANGE, neighborPos, GameEvent.Context.of(player, level.getBlockState(neighborPos)));
                        level.levelEvent(player, 3003, neighborPos, 0);
                    }
                }));
                add(this::hasAxeTransforming, pair.getSecond(), pair.getFirst(), (context) -> {
                    Player player = context.getPlayer();
                    Level level = context.getLevel();
                    BlockPos pos = context.getClickedPos();
                    BlockState oldState = level.getBlockState(pos);
                    if (player == null) return;
                    spawnSoundAndParticle(level, pos, player, oldState, SoundEvents.AXE_WAX_OFF, 3004);
                    context.getItemInHand().hurtAndBreak(1, player, player.getEquipmentSlotForItem(context.getItemInHand()));
                });
            }
            for (Pair<BlockLike, BlockLike> pair : oxidizationPairs) {
                add(this::hasAxeTransforming, pair.getFirst(), pair.getSecond(), (context) -> {
                    Player player = context.getPlayer();
                    Level level = context.getLevel();
                    BlockPos pos = context.getClickedPos();
                    BlockState oldState = level.getBlockState(pos);
                    if (player == null) return;
                    spawnSoundAndParticle(level, pos, player, oldState, SoundEvents.AXE_SCRAPE, 3005);
                    context.getItemInHand().hurtAndBreak(1, player, player.getEquipmentSlotForItem(context.getItemInHand()));
                });
            }
            BlockConversionsImpl.Oxidizables oxidizables = PlatformLoader.INSTANCE.internal().getOxidizables();
            oxidizables.add(unaffected, exposed);
            oxidizables.add(exposed, weathered);
            oxidizables.add(weathered, oxidized);
        }

        default void add(Predicate<ItemStack> item, BlockLike originalBlock, BlockLike convertedBlock, Holder<SoundEvent> sound) {
            add(item, originalBlock, convertedBlock, sound, 1F, 1F);
        }
        default void add(Predicate<ItemStack> item, BlockLike originalBlock, BlockLike convertedBlock, Holder<SoundEvent> sound, float volume, float pitch) {
            BlockConversionsImpl.INTERACTIONS.computeIfAbsent(originalBlock.asBlock(), _ -> new ArrayList<>()).add(new BlockConversionsImpl.Record(item, convertedBlock.asBlock(), (context) -> {
                Player player = context.getPlayer();
                if (player == null) return;
                context.getLevel().playSound(player, context.getClickedPos(), sound.value(), SoundSource.BLOCKS, volume, pitch);
                context.getItemInHand().hurtAndBreak(1, player, player.getEquipmentSlotForItem(context.getItemInHand()));
            }));
        }
        default void add(Predicate<ItemStack> item, BlockLike originalBlock, BlockLike convertedBlock, Consumer<UseOnContext> context) {
            BlockConversionsImpl.INTERACTIONS.computeIfAbsent(originalBlock.asBlock(), _ -> new ArrayList<>()).add(new BlockConversionsImpl.Record(item, convertedBlock.asBlock(), context));
        }

        private boolean hasAxeTransforming(ItemStack stack) {
            if (!stack.has(net.minecraft.core.component.DataComponents.BLOCK_TRANSFORMER)) return false;
            return stack.get(net.minecraft.core.component.DataComponents.BLOCK_TRANSFORMER).is(BlockTransformers.AXE);
        }

        private void spawnSoundAndParticle(final Level level, final BlockPos pos, final @Nullable Player player, final BlockState oldState, final Holder<SoundEvent> soundEvent, final int particle) {
            level.playSound(player, pos, soundEvent.value(), SoundSource.BLOCKS, 1.0F, 1.0F);
            level.levelEvent(player, particle, pos, 0);
            if (oldState.getBlock() instanceof ChestBlock && oldState.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
                BlockPos neighborPos = ChestBlock.getConnectedBlockPos(pos, oldState);
                level.gameEvent(GameEvent.BLOCK_CHANGE, neighborPos, GameEvent.Context.of(player, level.getBlockState(neighborPos)));
                level.levelEvent(player, particle, neighborPos, 0);
            }
        }
    }

    public interface BiomeModifications {

        void register(ResourceKey<Biome> biome, Consumer<BiomeModificationContext> context);
        void register(List<ResourceKey<Biome>> biomes, Consumer<BiomeModificationContext> context);
        void register(TagKey<Biome> biome, Consumer<BiomeModificationContext> context);
    }

    public interface ReloadListeners {

        void addListener(Identifier id, PreparableReloadListener listener);
        void addOrdering(Identifier first, Identifier second);
    }

    public interface DataRegistries {

        <T> void register(ResourceKey<Registry<T>> key, Codec<T> codec);
        <T> void registerSynced(ResourceKey<Registry<T>> key, Codec<T> serverCodec, Codec<T> clientCodec);
    }

    public interface EntityDataSerializers {
        void register(Identifier id, Supplier<EntityDataSerializer<?>> serializer);
    }

    public interface SpawnPlacements {
        <T extends Mob> void register(Supplier<EntityType<T>> type, SpawnPlacementType placementType, Heightmap.Types heightmap, net.minecraft.world.entity.SpawnPlacements.SpawnPredicate<T> spawnPredicate);
    }
}
