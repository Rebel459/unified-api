package net.rebel459.unified.impl.core;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.rebel459.unified.api.core.UnifiedEvents;
import net.rebel459.unified.api.helper.BiomeModificationContext;
import net.rebel459.unified.api.registry.UnifiedDataComponents;
import net.rebel459.unified.api.util.BlockLike;
import net.rebel459.unified.impl.helper.BlockConversionsImpl;
import net.rebel459.unified.impl.helper.StructureMusicImpl;
import net.rebel459.unified.impl.platform.PlatformHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class CommonHelpers {

    public interface CreativeEntries {

        void insert(ResourceKey<CreativeModeTab> tab, ItemLike... items);
        void insert(ResourceKey<CreativeModeTab> tab, ItemStackTemplate... items);
        void insert(List<ResourceKey<CreativeModeTab>> tabs, ItemLike... items);
        void insert(List<ResourceKey<CreativeModeTab>> tabs, ItemStackTemplate... items);
        void insertAfter(ResourceKey<CreativeModeTab> tab, ItemLike existingItem, ItemLike... addedItems);
        void insertAfter(ResourceKey<CreativeModeTab> tab, ItemLike existingItem, ItemStackTemplate... addedItems);
        void insertAfter(List<ResourceKey<CreativeModeTab>> tabs, ItemLike existingItem, ItemLike... addedItems);
        void insertAfter(List<ResourceKey<CreativeModeTab>> tabs, ItemLike existingItem, ItemStackTemplate... addedItems);
        void insertBefore(ResourceKey<CreativeModeTab> tab, ItemLike existingItem, ItemLike... addedItems);
        void insertBefore(ResourceKey<CreativeModeTab> tab, ItemLike existingItem, ItemStackTemplate... addedItems);
        void insertBefore(List<ResourceKey<CreativeModeTab>> tabs, ItemLike existingItem, ItemLike... addedItems);
        void insertBefore(List<ResourceKey<CreativeModeTab>> tabs, ItemLike existingItem, ItemStackTemplate... addedItems);
    }

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
        <T extends CustomPacketPayload> void registerConfigToServer(CustomPacketPayload.Type<T> type, StreamCodec<? super FriendlyByteBuf, T> codec, BiConsumer<T, ServerPlayer> handler);
        <T extends CustomPacketPayload> void registerConfigToClient(CustomPacketPayload.Type<T> type, StreamCodec<? super FriendlyByteBuf, T> codec, BiConsumer<T, Player> handler);

        boolean canSend(CustomPacketPayload payload, ServerPlayer player);
        void send(CustomPacketPayload payload, ServerPlayer player);
    }

    public interface BlockConversions {

        default void addStrippable(BlockLike originalBlock, BlockLike convertedBlock) {
            add(stack -> stack.getItem() instanceof AxeItem, originalBlock, convertedBlock, SoundEvents.AXE_STRIP);
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
                add(stack -> stack.getItem() instanceof AxeItem, pair.getSecond(), pair.getFirst(), (context) -> {
                    Player player = context.getPlayer();
                    Level level = context.getLevel();
                    BlockPos pos = context.getClickedPos();
                    BlockState oldState = level.getBlockState(pos);
                    if (player == null) return;
                    AxeItem.spawnSoundAndParticle(level, pos, player, oldState, SoundEvents.AXE_WAX_OFF, 3004);
                    context.getItemInHand().hurtAndBreak(1, player, player.getEquipmentSlotForItem(context.getItemInHand()));
                });
            }
            for (Pair<BlockLike, BlockLike> pair : oxidizationPairs) {
                add(stack -> stack.getItem() instanceof AxeItem, pair.getFirst(), pair.getSecond(), (context) -> {
                    Player player = context.getPlayer();
                    Level level = context.getLevel();
                    BlockPos pos = context.getClickedPos();
                    BlockState oldState = level.getBlockState(pos);
                    if (player == null) return;
                    AxeItem.spawnSoundAndParticle(level, pos, player, oldState, SoundEvents.AXE_SCRAPE, 3005);
                    context.getItemInHand().hurtAndBreak(1, player, player.getEquipmentSlotForItem(context.getItemInHand()));
                });
            }
            BlockConversionsImpl.Oxidizables oxidizables = PlatformHandler.INSTANCE.impl().getOxidizables();
            oxidizables.add(unaffected, exposed);
            oxidizables.add(exposed, weathered);
            oxidizables.add(weathered, oxidized);
        }

        default void add(Predicate<ItemStack> item, BlockLike originalBlock, BlockLike convertedBlock, SoundEvent sound) {
            add(item, originalBlock, convertedBlock, sound, 1F, 1F);
        }
        default void add(Predicate<ItemStack> item, BlockLike originalBlock, BlockLike convertedBlock, SoundEvent sound, float volume, float pitch) {
            BlockConversionsImpl.INTERACTIONS.computeIfAbsent(originalBlock.asBlock(), _ -> new ArrayList<>()).add(new BlockConversionsImpl.Record(item, convertedBlock.asBlock(), (context) -> {
                Player player = context.getPlayer();
                if (player == null) return;
                context.getLevel().playSound(player, context.getClickedPos(), sound, SoundSource.BLOCKS, volume, pitch);
                context.getItemInHand().hurtAndBreak(1, player, player.getEquipmentSlotForItem(context.getItemInHand()));
            }));
        }
        default void add(Predicate<ItemStack> item, BlockLike originalBlock, BlockLike convertedBlock, Consumer<UseOnContext> context) {
            BlockConversionsImpl.INTERACTIONS.computeIfAbsent(originalBlock.asBlock(), _ -> new ArrayList<>()).add(new BlockConversionsImpl.Record(item, convertedBlock.asBlock(), context));
        }
    }

    public interface DataComponents {

        default <T> void add(ItemLike itemLike, DataComponentType<T> type, T value) {
            UnifiedEvents.DefaultDataComponents.modify((testedItem, builder, provider) -> {
                if (testedItem == itemLike.asItem()) {
                    builder.set(type, value);
                }
            });
        }
        default <T> void addWithProvider(ItemLike itemLike, DataComponentType<T> type, DataComponentInitializers.SingleComponentInitializer<T> initializer) {
            UnifiedEvents.DefaultDataComponents.modify((testedItem, builder, provider) -> {
                if (testedItem == itemLike.asItem()) {
                    builder.addAll(DataComponentMap.builder().set(type, initializer.create(provider)).build());
                }
            });
        }
        default <T> void addWithKey(ItemLike itemLike, DataComponentType<Holder<T>> type, ResourceKey<T> valueKey) {
            UnifiedEvents.DefaultDataComponents.modify((testedItem, builder, provider) -> {
                if (testedItem == itemLike.asItem()) {
                    builder.addAll(DataComponentMap.builder().set(type, provider.getOrThrow(valueKey)).build());
                }
            });
        }

        default void addFurnaceFuel(ItemLike itemLike, int ticks) {
            add(itemLike, UnifiedDataComponents.FURNACE_FUEL.get(), ticks);
        }
        default void addCompost(ItemLike itemLike, float chance) {
            add(itemLike, UnifiedDataComponents.COMPOST.get(), chance);
        }
    }

    public interface BiomeModifications {

        void register(ResourceKey<Biome> biome, Consumer<BiomeModificationContext> context);
        void register(List<ResourceKey<Biome>> biomes, Consumer<BiomeModificationContext> context);
        void register(TagKey<Biome> biome, Consumer<BiomeModificationContext> context);
    }

    public interface StructureMusic {

        default void add(Identifier structure, Music music) {
            add(structure, music, false);
        }
        default void add(ResourceKey<Structure> structure, Music music) {
            add(structure, music, false);
        }
        default void add(TagKey<Structure> structure, Music music) {
            add(structure, music, false);
        }

        default void add(Identifier structure, Music music, boolean fullBox) {
            StructureMusicImpl.addStructure(structure, music, fullBox);
        }
        default void add(ResourceKey<Structure> structure, Music music, boolean fullBox) {
            add(structure.identifier(), music, fullBox);
        }
        default void add(TagKey<Structure> structure, Music music, boolean fullBox) {
            StructureMusicImpl.addStructureTag(structure, music, fullBox);
        }
    }

    public interface ReloadListeners {

        void addListener(Identifier id, PreparableReloadListener listener);
        void addOrdering(Identifier first, Identifier second);
    }
}