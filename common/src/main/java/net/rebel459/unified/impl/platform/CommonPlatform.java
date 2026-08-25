package net.rebel459.unified.impl.platform;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.rebel459.unified.api.core.UnifiedAttachments;
import net.rebel459.unified.api.core.UnifiedRegistries;
import net.rebel459.unified.impl.core.CommonHelpers;
import net.rebel459.unified.impl.core.CommonInstance;
import net.rebel459.unified.impl.helper.BlockConversionsImpl;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.BiPredicate;
import java.util.function.Supplier;

public interface CommonPlatform {

    <Y> UnifiedRegistries.DeferredRegistry<Y> createDeferredRegistry(String modId, Registry<Y> registry);
    UnifiedRegistries.Items createItems(String modId);
    UnifiedRegistries.Blocks createBlocks(String modId);
    UnifiedRegistries.DataComponentTypes createDataComponentTypes(String modId);
    UnifiedRegistries.EntityTypes createEntityTypes(String modId);
    UnifiedRegistries.SoundEvents createSoundEvents(String modId);

    CommonInstance getInstance();

    CommonHelpers.CreativeEntries getCreativeEntries();
    CommonHelpers.DataPacks getDataPacks();
    CommonHelpers.Networking getNetworking();
    CommonHelpers.BiomeModifications getBiomeModifications();
    CommonHelpers.ReloadListeners getReloadListeners();
    CommonHelpers.DataRegistries getDataRegistries();
    CommonHelpers.EntityData getEntityData();

    @ApiStatus.Internal
    Internal internal();

    @ApiStatus.Internal
    interface Internal {
        BlockConversionsImpl.Oxidizables getOxidizables();
        CreativeModeTab createCreativeModeTab(CreativeModeTab.Row row, int column, Component displayName, Supplier<ItemStack> iconGenerator, CreativeModeTab.DisplayItemsGenerator displayItemsGenerator);
        <T> UnifiedAttachments.Entity<T> createEntityAttachment(Identifier id, Supplier<T> defaultValue, MapCodec<T> persistenceCodec, StreamCodec<? super RegistryFriendlyByteBuf, T> syncCodec, BiPredicate<Entity, ServerPlayer> syncPredicate, boolean copyOnDeath);
        <T> UnifiedAttachments.BlockEntity<T> createBlockEntityAttachment(Identifier id, Supplier<T> defaultValue, MapCodec<T> persistenceCodec, StreamCodec<? super RegistryFriendlyByteBuf, T> syncCodec, BiPredicate<BlockEntity, ServerPlayer> syncPredicate);
        <T> UnifiedAttachments.Chunk<T> createChunkAttachment(Identifier id, Supplier<T> defaultValue, MapCodec<T> persistenceCodec, StreamCodec<? super RegistryFriendlyByteBuf, T> syncCodec, BiPredicate<ChunkAccess, ServerPlayer> syncPredicate);
        <T> UnifiedAttachments.Level<T> createLevelAttachment(Identifier id, Supplier<T> defaultValue, MapCodec<T> persistenceCodec, StreamCodec<? super RegistryFriendlyByteBuf, T> syncCodec, BiPredicate<ServerLevel, ServerPlayer> syncPredicate);
    }
}
