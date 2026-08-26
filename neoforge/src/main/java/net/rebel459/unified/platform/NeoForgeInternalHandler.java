package net.rebel459.unified.platform;

import com.mojang.serialization.MapCodec;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.rebel459.unified.util.helper.impl.BlockConversionsImpl;
import net.rebel459.unified.util.data.MobVariants;
import net.rebel459.unified.util.data.impl.EntityRegistryImpl;

import java.util.HashMap;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.nio.file.Path;
import java.util.List;

public class NeoForgeInternalHandler implements InternalHandler {

    @Override
    public <Y> UnifiedRegistries.DeferredRegistry<Y> createDeferredRegistry(String modId, Registry<Y> registry) {
        return new NeoForgeUnifiedRegistries.DeferredRegistry<>(modId, registry);
    }

    @Override
    public UnifiedRegistries.Items createItems(String modId) {
        return new NeoForgeUnifiedRegistries.Items(modId);
    }

    @Override
    public UnifiedRegistries.Blocks createBlocks(String modId) {
        return new NeoForgeUnifiedRegistries.Blocks(modId);
    }

    @Override
    public UnifiedRegistries.CreativeTabs createCreativeTabs(String modId) {
        return new NeoForgeUnifiedRegistries.CreativeTabs(modId);
    }

    @Override
    public UnifiedRegistries.DataComponentTypes createDataComponentTypes(String modId) {
        return new NeoForgeUnifiedRegistries.DataComponentTypes(modId);
    }

    @Override
    public UnifiedRegistries.EntityTypes createEntityTypes(String modId) {
        return new NeoForgeUnifiedRegistries.EntityTypes(modId);
    }

    @Override
    public UnifiedRegistries.BlockEntityTypes createBlockEntityTypes(String modId) {
        return new NeoForgeUnifiedRegistries.BlockEntityTypes(modId);
    }

    @Override
    public UnifiedRegistries.SoundEvents createSoundEvents(String modId) {
        return new NeoForgeUnifiedRegistries.SoundEvents(modId);
    }

    @Override
    public HelpersImpl.CreativeEntries getCreativeEntries() {
        return new NeoForgeHelpersImpl.CreativeEntries();
    }

    @Override
    public HelpersImpl.Packs getPacks() {
        return new NeoForgeHelpersImpl.Packs();
    }

    @Override
    public HelpersImpl.Networking getNetworking() {
        return new NeoForgeHelpersImpl.Networking();
    }

    @Override
    public HelpersImpl.Platform getPlatform() {
        return new NeoForgeUnifiedPlatform();
    }

    @Override
    public HelpersImpl.ReloadListeners getReloadListeners() {
        return new NeoForgeHelpersImpl.ReloadListeners();
    }

    @Override
    public HelpersImpl.DataRegistries getDataRegistries() {
        return new NeoForgeHelpersImpl.DataRegistries();
    }

    @Override
    public HelpersImpl.EntityData getEntityData() {
        return new NeoForgeHelpersImpl.EntityData();
    }

    @Override
    public InternalHandler.Impl impl() {
        return new Impl();
    }

    public static class Impl implements InternalHandler.Impl {

        @Override
        public List<Path> getModResourceRoots() {
            return ModList.get().getModFiles().stream()
                    .flatMap(fileInfo -> fileInfo.getFile().getContents().getContentRoots().stream())
                    .distinct()
                    .toList();
        }

        @Override
        public Path getGameDirectory() {
            return FMLPaths.GAMEDIR.get();
        }

        @Override
        public void prepareRegistryNamespace(String namespace) {
            NeoForgeUnifiedRegistries.prepareNamespace(namespace);
        }

        @Override
        @SuppressWarnings({"rawtypes", "unchecked"})
        public void registerEntityCopy(Identifier id, ResourceKey<EntityType<?>> base, Either<Identifier, MobVariants.Variant> defaultVariant) {
            ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, id);
            net.neoforged.neoforge.registries.DeferredRegister registry = NeoForgeUnifiedRegistries.DEFERRED.get(
                    com.mojang.datafixers.util.Pair.of(id.getNamespace(), BuiltInRegistries.ENTITY_TYPE)
            );
            registry.register(id.getPath(), () -> defaultVariant.map(
                    variant -> EntityRegistryImpl.create(key, base, variant),
                    variant -> EntityRegistryImpl.create(key, base, variant)
            ));
        }

        public static HashMap<Block, Block> OXIDIZABLES = new HashMap<>();

        @Override
        public BlockConversionsImpl.Oxidizables getOxidizables() {
            return (from, to) -> {
                OXIDIZABLES.put(from, to);

                for (BlockState state : from.getStateDefinition().getPossibleStates()) {
                    state.initCache();
                }
            };
        }

        @Override
        public CreativeModeTab createCreativeModeTab(CreativeModeTab.Row row, int column, Component displayName, Supplier<ItemStack> iconGenerator, CreativeModeTab.DisplayItemsGenerator displayItemsGenerator) {
            return new CreativeModeTab.Builder(row, column)
                    .title(displayName)
                    .icon(iconGenerator)
                    .displayItems(displayItemsGenerator)
                    .build();
        }

        @Override
        public <T> UnifiedAttachments.Entity<T> createEntityAttachment(Identifier id, Supplier<T> defaultValue, MapCodec<T> persistenceCodec, StreamCodec<? super RegistryFriendlyByteBuf, T> syncCodec, BiPredicate<Entity, ServerPlayer> syncPredicate, boolean copyOnDeath) {
            return new NeoForgeEntityAttachment<>(createAttachment(id, defaultValue, persistenceCodec, syncCodec, syncPredicate, Entity.class, copyOnDeath));
        }

        @Override
        public <T> UnifiedAttachments.BlockEntity<T> createBlockEntityAttachment(Identifier id, Supplier<T> defaultValue, MapCodec<T> persistenceCodec, StreamCodec<? super RegistryFriendlyByteBuf, T> syncCodec, BiPredicate<BlockEntity, ServerPlayer> syncPredicate) {
            return new NeoForgeBlockEntityAttachment<>(createAttachment(id, defaultValue, persistenceCodec, syncCodec, syncPredicate, BlockEntity.class, false));
        }

        @Override
        public <T> UnifiedAttachments.Chunk<T> createChunkAttachment(Identifier id, Supplier<T> defaultValue, MapCodec<T> persistenceCodec, StreamCodec<? super RegistryFriendlyByteBuf, T> syncCodec, BiPredicate<ChunkAccess, ServerPlayer> syncPredicate) {
            return new NeoForgeChunkAttachment<>(createAttachment(id, defaultValue, persistenceCodec, syncCodec, syncPredicate, ChunkAccess.class, false));
        }

        @Override
        public <T> UnifiedAttachments.Level<T> createLevelAttachment(Identifier id, Supplier<T> defaultValue, MapCodec<T> persistenceCodec, StreamCodec<? super RegistryFriendlyByteBuf, T> syncCodec, BiPredicate<ServerLevel, ServerPlayer> syncPredicate) {
            return new NeoForgeLevelAttachment<>(createAttachment(id, defaultValue, persistenceCodec, syncCodec, syncPredicate, ServerLevel.class, false));
        }

        private static <T, H> Supplier<AttachmentType<T>> createAttachment(Identifier id, Supplier<T> defaultValue, MapCodec<T> persistenceCodec, StreamCodec<? super RegistryFriendlyByteBuf, T> syncCodec, BiPredicate<H, ServerPlayer> syncPredicate, Class<H> holderClass, boolean copyOnDeath) {
            AttachmentType.Builder<T> builder = AttachmentType.builder(defaultValue);
            if (persistenceCodec != null) builder.serialize(persistenceCodec);
            if (syncCodec != null) {
                builder.sync(
                        (holder, player) -> holderClass.isInstance(holder) && syncPredicate.test(holderClass.cast(holder), player),
                        syncCodec
                );
            }
            if (copyOnDeath) builder.copyOnDeath();

            return UnifiedRegistries.DeferredRegistry.create(id.getNamespace(), NeoForgeRegistries.ATTACHMENT_TYPES).register(id.getPath(), builder::build);
        }

        private abstract static class NeoForgeAttachment<T, H> implements UnifiedAttachments<T, H> {
            private final Supplier<AttachmentType<T>> attachment;

            private NeoForgeAttachment(Supplier<AttachmentType<T>> attachment) {
                this.attachment = attachment;
            }

            private IAttachmentHolder holder(H holder) {
                return (IAttachmentHolder) holder;
            }

            @Override
            public T get(H holder) {
                return holder(holder).getData(attachment);
            }

            @Override
            public boolean has(H holder) {
                return holder(holder).hasData(attachment);
            }

            @Override
            public T set(H holder, T value) {
                return holder(holder).setData(attachment, value);
            }

            @Override
            public T remove(H holder) {
                return holder(holder).removeData(attachment);
            }

            @Override
            public T modify(H holder, UnaryOperator<T> modifier) {
                IAttachmentHolder attachmentHolder = holder(holder);
                T previous = attachmentHolder.getExistingDataOrNull(attachment);
                T updated = modifier.apply(previous);
                if (updated == null) {
                    attachmentHolder.removeData(attachment);
                } else {
                    attachmentHolder.setData(attachment, updated);
                }
                return previous;
            }
        }

        private static final class NeoForgeEntityAttachment<T> extends NeoForgeAttachment<T, Entity> implements UnifiedAttachments.Entity<T> {
            private NeoForgeEntityAttachment(Supplier<AttachmentType<T>> attachment) { super(attachment); }
        }

        private static final class NeoForgeBlockEntityAttachment<T> extends NeoForgeAttachment<T, BlockEntity> implements UnifiedAttachments.BlockEntity<T> {
            private NeoForgeBlockEntityAttachment(Supplier<AttachmentType<T>> attachment) { super(attachment); }
        }

        private static final class NeoForgeChunkAttachment<T> extends NeoForgeAttachment<T, ChunkAccess> implements UnifiedAttachments.Chunk<T> {
            private NeoForgeChunkAttachment(Supplier<AttachmentType<T>> attachment) { super(attachment); }
        }

        private static final class NeoForgeLevelAttachment<T> extends NeoForgeAttachment<T, ServerLevel> implements UnifiedAttachments.Level<T> {
            private NeoForgeLevelAttachment(Supplier<AttachmentType<T>> attachment) { super(attachment); }
        }
    }
}
