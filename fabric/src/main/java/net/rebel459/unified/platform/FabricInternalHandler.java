package net.rebel459.unified.platform;

import com.mojang.serialization.MapCodec;
import com.mojang.datafixers.util.Either;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
import net.fabricmc.loader.api.FabricLoader;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.rebel459.unified.util.helper.impl.BlockConversionsImpl;
import net.rebel459.unified.util.data.MobVariants;
import net.rebel459.unified.util.data.registry.impl.EntityRegistryImpl;

import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.nio.file.Path;
import java.util.List;

public class FabricInternalHandler implements InternalHandler {

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
    public HelpersImpl.CreativeEntries getCreativeEntries() {
        return new FabricHelpersImpl.CreativeEntries();
    }

    @Override
    public HelpersImpl.Packs getPacks() {
        return new FabricHelpersImpl.Packs();
    }

    @Override
    public HelpersImpl.Networking getNetworking() {
        return new FabricHelpersImpl.Networking();
    }

    @Override
    public HelpersImpl.Platform getPlatform() {
        return new FabricUnifiedPlatform();
    }

    @Override
    public HelpersImpl.ReloadListeners getReloadListeners() {
        return new FabricHelpersImpl.ReloadListeners();
    }

    @Override
    public HelpersImpl.DataRegistries getDataRegistries() {
        return new FabricHelpersImpl.DataRegistries();
    }

    @Override
    public HelpersImpl.EntityData getEntityData() {
        return new FabricHelpersImpl.EntityData();
    }

    @Override
    public InternalHandler.Impl impl() {
        return new Impl();
    }

    public static class Impl implements InternalHandler.Impl {

        @Override
        public List<Path> getModResourceRoots() {
            return FabricLoader.getInstance().getAllMods().stream()
                    .flatMap(container -> container.getRootPaths().stream())
                    .distinct()
                    .toList();
        }

        @Override
        public Path getGameDirectory() {
            return FabricLoader.getInstance().getGameDir();
        }

        @Override
        public void prepareRegistryNamespace(String namespace) {
        }

        @Override
        public void registerEntityCopy(Identifier id, ResourceKey<EntityType<?>> base, Either<Identifier, MobVariants.Variant> defaultVariant) {
            ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, id);
            EntityType<?> entityType = defaultVariant.map(
                    variant -> EntityRegistryImpl.create(key, base, variant),
                    variant -> EntityRegistryImpl.create(key, base, variant)
            );
            Registry.register(BuiltInRegistries.ENTITY_TYPE, key, entityType);
        }

        @Override
        public BlockConversionsImpl.Oxidizables getOxidizables() {
            return OxidizableBlocksRegistry::registerNextStage;
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
