package net.rebel459.unified.fabric.platform;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
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
import net.rebel459.unified.fabric.core.FabricHelpers;
import net.rebel459.unified.fabric.core.FabricInstance;
import net.rebel459.unified.fabric.core.FabricUnifiedRegistries;
import net.rebel459.unified.impl.core.CommonHelpers;
import net.rebel459.unified.impl.core.CommonInstance;
import net.rebel459.unified.impl.platform.CommonPlatform;
import net.rebel459.unified.impl.helper.BlockConversionsImpl;

import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class FabricPlatform implements CommonPlatform {

    @Override
    public <Y> UnifiedRegistries.DeferredRegistry<Y> createDeferredRegistry(String modId, Registry<Y> registry) {
        return new FabricUnifiedRegistries.DeferredRegistry<>(modId, registry);
    }

    @Override
    public UnifiedRegistries.Items createItems(String modId) {
        return new FabricUnifiedRegistries.Items(modId);
    }

    @Override
    public UnifiedRegistries.Blocks createBlocks(String modId) {
        return new FabricUnifiedRegistries.Blocks(modId);
    }

    @Override
    public UnifiedRegistries.CreativeTabs createCreativeTabs(String modId) {
        return new FabricUnifiedRegistries.CreativeTabs(modId);
    }

    @Override
    public UnifiedRegistries.DataComponentTypes createDataComponentTypes(String modId) {
        return new FabricUnifiedRegistries.DataComponentTypes(modId);
    }

    @Override
    public UnifiedRegistries.EntityTypes createEntityTypes(String modId) {
        return new FabricUnifiedRegistries.EntityTypes(modId);
    }

    @Override
    public UnifiedRegistries.BlockEntityTypes createBlockEntityTypes(String modId) {
        return new FabricUnifiedRegistries.BlockEntityTypes(modId);
    }

    @Override
    public UnifiedRegistries.SoundEvents createSoundEvents(String modId) {
        return new FabricUnifiedRegistries.SoundEvents(modId);
    }

    @Override
    public CommonInstance getInstance() {
        return new FabricInstance();
    }

    @Override
    public CommonHelpers.CreativeEntries getCreativeEntries() {
        return new FabricHelpers.CreativeEntries();
    }

    @Override
    public CommonHelpers.DataPacks getDataPacks() {
        return new FabricHelpers.DataPacks();
    }

    @Override
    public CommonHelpers.Networking getNetworking() {
        return new FabricHelpers.Networking();
    }

    @Override
    public CommonHelpers.BiomeModifications getBiomeModifications() {
        return new FabricHelpers.BiomeModifications();
    }

    @Override
    public CommonHelpers.ReloadListeners getReloadListeners() {
        return new FabricHelpers.ReloadListeners();
    }

    public CommonHelpers.DataRegistries getDataRegistries() {
        return new FabricHelpers.DataRegistries();
    }

    @Override
    public CommonHelpers.EntityData getEntityData() {
        return new FabricHelpers.EntityData();
    }

    @Override
    public CommonPlatform.Internal internal() {
        return new Internal();
    }

    public static class Internal implements CommonPlatform.Internal {

        @Override
        public BlockConversionsImpl.Oxidizables getOxidizables() {
            return (from, to) -> OxidizableBlocksRegistry.registerNextStage(from.asBlock(), to.asBlock());
        }

        @Override
        public CreativeModeTab createCreativeModeTab(CreativeModeTab.Row row, int column, Component displayName, Supplier<ItemStack> iconGenerator, CreativeModeTab.DisplayItemsGenerator displayItemsGenerator) {
            return new CreativeModeTab(row, column, CreativeModeTab.Type.CATEGORY, displayName, iconGenerator, displayItemsGenerator);
        }

        @Override
        public <T> UnifiedAttachments.Entity<T> createEntityAttachment(Identifier id, Supplier<T> defaultValue, MapCodec<T> persistenceCodec, StreamCodec<? super RegistryFriendlyByteBuf, T> syncCodec, BiPredicate<Entity, ServerPlayer> syncPredicate, boolean copyOnDeath) {
            return new FabricEntityAttachment<>(createAttachment(id, defaultValue, persistenceCodec, syncCodec, syncPredicate, Entity.class, copyOnDeath));
        }

        @Override
        public <T> UnifiedAttachments.BlockEntity<T> createBlockEntityAttachment(Identifier id, Supplier<T> defaultValue, MapCodec<T> persistenceCodec, StreamCodec<? super RegistryFriendlyByteBuf, T> syncCodec, BiPredicate<BlockEntity, ServerPlayer> syncPredicate) {
            return new FabricBlockEntityAttachment<>(createAttachment(id, defaultValue, persistenceCodec, syncCodec, syncPredicate, BlockEntity.class, false));
        }

        @Override
        public <T> UnifiedAttachments.Chunk<T> createChunkAttachment(Identifier id, Supplier<T> defaultValue, MapCodec<T> persistenceCodec, StreamCodec<? super RegistryFriendlyByteBuf, T> syncCodec, BiPredicate<ChunkAccess, ServerPlayer> syncPredicate) {
            return new FabricChunkAttachment<>(createAttachment(id, defaultValue, persistenceCodec, syncCodec, syncPredicate, ChunkAccess.class, false));
        }

        @Override
        public <T> UnifiedAttachments.Level<T> createLevelAttachment(Identifier id, Supplier<T> defaultValue, MapCodec<T> persistenceCodec, StreamCodec<? super RegistryFriendlyByteBuf, T> syncCodec, BiPredicate<ServerLevel, ServerPlayer> syncPredicate) {
            return new FabricLevelAttachment<>(createAttachment(id, defaultValue, persistenceCodec, syncCodec, syncPredicate, ServerLevel.class, false));
        }

        private static <T, H> AttachmentType<T> createAttachment(Identifier id, Supplier<T> defaultValue, MapCodec<T> persistenceCodec, StreamCodec<? super RegistryFriendlyByteBuf, T> syncCodec, BiPredicate<H, ServerPlayer> syncPredicate, Class<H> holderClass, boolean copyOnDeath) {
            return AttachmentRegistry.create(id, builder -> {
                builder.initializer(defaultValue);
                if (persistenceCodec != null) builder.persistent(persistenceCodec.codec());
                if (syncCodec != null) {
                    builder.syncWith(
                            syncCodec,
                            (target, player) -> holderClass.isInstance(target) && syncPredicate.test(holderClass.cast(target), player)
                    );
                }
                if (copyOnDeath) builder.copyOnDeath();
            });
        }

        private abstract static class FabricAttachment<T, H> implements UnifiedAttachments<T, H> {
            private final AttachmentType<T> attachment;

            private FabricAttachment(AttachmentType<T> attachment) {
                this.attachment = attachment;
            }

            private AttachmentTarget holder(H holder) {
                return (AttachmentTarget) holder;
            }

            @Override
            public T get(H holder) {
                return holder(holder).getAttachedOrCreate(attachment);
            }

            @Override
            public boolean has(H holder) {
                return holder(holder).hasAttached(attachment);
            }

            @Override
            public T set(H holder, T value) {
                return holder(holder).setAttached(attachment, value);
            }

            @Override
            public T remove(H holder) {
                return holder(holder).removeAttached(attachment);
            }

            @Override
            public T modify(H holder, UnaryOperator<T> modifier) {
                return holder(holder).modifyAttached(attachment, modifier);
            }
        }

        private static final class FabricEntityAttachment<T> extends FabricAttachment<T, Entity> implements UnifiedAttachments.Entity<T> {
            private FabricEntityAttachment(AttachmentType<T> attachment) { super(attachment); }
        }

        private static final class FabricBlockEntityAttachment<T> extends FabricAttachment<T, BlockEntity> implements UnifiedAttachments.BlockEntity<T> {
            private FabricBlockEntityAttachment(AttachmentType<T> attachment) { super(attachment); }
        }

        private static final class FabricChunkAttachment<T> extends FabricAttachment<T, ChunkAccess> implements UnifiedAttachments.Chunk<T> {
            private FabricChunkAttachment(AttachmentType<T> attachment) { super(attachment); }
        }

        private static final class FabricLevelAttachment<T> extends FabricAttachment<T, ServerLevel> implements UnifiedAttachments.Level<T> {
            private FabricLevelAttachment(AttachmentType<T> attachment) { super(attachment); }
        }
    }
}
