package net.rebel459.unified.platform;

import com.mojang.serialization.MapCodec;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.rebel459.unified.util.helper.impl.BlockConversionsImpl;
import net.rebel459.unified.util.data.MobVariants;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.nio.file.Path;
import java.util.List;

public interface InternalHandler {

    <Y> UnifiedRegistries.DeferredRegistry<Y> createDeferredRegistry(String modId, Registry<Y> registry);
    UnifiedRegistries.Items createItems(String modId);
    UnifiedRegistries.Blocks createBlocks(String modId);
    UnifiedRegistries.CreativeTabs createCreativeTabs(String modId);
    UnifiedRegistries.DataComponentTypes createDataComponentTypes(String modId);
    UnifiedRegistries.EntityTypes createEntityTypes(String modId);
    UnifiedRegistries.BlockEntityTypes createBlockEntityTypes(String modId);
    UnifiedRegistries.SoundEvents createSoundEvents(String modId);

    HelpersImpl.CreativeEntries getCreativeEntries();
    HelpersImpl.Packs getPacks();
    HelpersImpl.Networking getNetworking();
    HelpersImpl.Platform getPlatform();
    HelpersImpl.ReloadListeners getReloadListeners();
    HelpersImpl.DataRegistries getDataRegistries();
    HelpersImpl.EntityData getEntityData();

    @ApiStatus.Internal
    Impl impl();

    @ApiStatus.Internal
    interface Impl {
        List<Path> getModResourceRoots();
        Path getGameDirectory();
        void prepareRegistryNamespace(String namespace);
        void registerEntityCopy(Identifier id, ResourceKey<EntityType<?>> base, Either<Identifier, MobVariants.Variant> defaultVariant);
        BlockConversionsImpl.Oxidizables getOxidizables();
        CreativeModeTab createCreativeModeTab(CreativeModeTab.Row row, int column, Component displayName, Supplier<ItemStack> iconGenerator, CreativeModeTab.DisplayItemsGenerator displayItemsGenerator);
        <T> UnifiedAttachments.Entity<T> createEntityAttachment(Identifier id, Supplier<T> defaultValue, MapCodec<T> persistenceCodec, StreamCodec<? super RegistryFriendlyByteBuf, T> syncCodec, BiPredicate<Entity, ServerPlayer> syncPredicate, boolean copyOnDeath);
        <T> UnifiedAttachments.BlockEntity<T> createBlockEntityAttachment(Identifier id, Supplier<T> defaultValue, MapCodec<T> persistenceCodec, StreamCodec<? super RegistryFriendlyByteBuf, T> syncCodec, BiPredicate<BlockEntity, ServerPlayer> syncPredicate);
        <T> UnifiedAttachments.Chunk<T> createChunkAttachment(Identifier id, Supplier<T> defaultValue, MapCodec<T> persistenceCodec, StreamCodec<? super RegistryFriendlyByteBuf, T> syncCodec, BiPredicate<ChunkAccess, ServerPlayer> syncPredicate);
        <T> UnifiedAttachments.Level<T> createLevelAttachment(Identifier id, Supplier<T> defaultValue, MapCodec<T> persistenceCodec, StreamCodec<? super RegistryFriendlyByteBuf, T> syncCodec, BiPredicate<ServerLevel, ServerPlayer> syncPredicate);
    }
}
