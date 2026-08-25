package net.rebel459.unified.platform;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public interface UnifiedAttachments<T, H> {
    T get(H holder);
    boolean has(H holder);
    @Nullable T set(H holder, T value);
    @Nullable T remove(H holder);
    @Nullable T modify(H holder, UnaryOperator<T> modifier);

    abstract class Builder<T, H, A extends UnifiedAttachments<T, H>, B extends Builder<T, H, A, B>> {
        protected final Identifier id;
        protected final Supplier<T> defaultValue;
        protected MapCodec<T> persistenceCodec;
        protected StreamCodec<? super RegistryFriendlyByteBuf, T> syncCodec;
        protected BiPredicate<H, ServerPlayer> syncPredicate = (_, _) -> true;

        protected Builder(Identifier id, Supplier<T> defaultValue) {
            this.id = Objects.requireNonNull(id);
            this.defaultValue = Objects.requireNonNull(defaultValue);
        }

        @SuppressWarnings("unchecked")
        protected B self() {
            return (B) this;
        }

        public B persistent(MapCodec<T> codec) {
            this.persistenceCodec = Objects.requireNonNull(codec);
            return self();
        }

        public B synced(StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
            return synced(codec, (_, _) -> true);
        }

        public B synced(StreamCodec<? super RegistryFriendlyByteBuf, T> codec, BiPredicate<H, ServerPlayer> predicate) {
            this.syncCodec = Objects.requireNonNull(codec);
            this.syncPredicate = Objects.requireNonNull(predicate);
            return self();
        }

        public abstract A build();
    }

    interface BlockEntity<T> extends UnifiedAttachments<T, net.minecraft.world.level.block.entity.BlockEntity> {
        static <T> Builder<T> builder(Identifier id, Supplier<T> defaultValue) {
            return new Builder<>(id, defaultValue);
        }

        final class Builder<T> extends UnifiedAttachments.Builder<T, net.minecraft.world.level.block.entity.BlockEntity, BlockEntity<T>, Builder<T>> {
            private Builder(Identifier id, Supplier<T> defaultValue) {
                super(id, defaultValue);
            }

            @Override
            public BlockEntity<T> build() {
                return InternalHandlerImpl.INSTANCE.impl().createBlockEntityAttachment(
                        this.id,
                        this.defaultValue,
                        this.persistenceCodec,
                        this.syncCodec,
                        this.syncPredicate
                );
            }
        }
    }

    interface Chunk<T> extends UnifiedAttachments<T, ChunkAccess> {
        static <T> Builder<T> builder(Identifier id, Supplier<T> defaultValue) {
            return new Builder<>(id, defaultValue);
        }

        final class Builder<T> extends UnifiedAttachments.Builder<T, ChunkAccess, Chunk<T>, Builder<T>> {
            private Builder(Identifier id, Supplier<T> defaultValue) {
                super(id, defaultValue);
            }

            @Override
            public Chunk<T> build() {
                return InternalHandlerImpl.INSTANCE.impl().createChunkAttachment(
                        this.id,
                        this.defaultValue,
                        this.persistenceCodec,
                        this.syncCodec,
                        this.syncPredicate
                );
            }
        }
    }

    interface Entity<T> extends UnifiedAttachments<T, net.minecraft.world.entity.Entity> {
        static <T> Builder<T> builder(Identifier id, Supplier<T> defaultValue) {
            return new Builder<>(id, defaultValue);
        }

        final class Builder<T> extends UnifiedAttachments.Builder<T, net.minecraft.world.entity.Entity, Entity<T>, Builder<T>> {
            private boolean copyOnDeath;

            private Builder(Identifier id, Supplier<T> defaultValue) {
                super(id, defaultValue);
            }

            public Builder<T> copyOnDeath() {
                this.copyOnDeath = true;
                return this;
            }

            @Override
            public Entity<T> build() {
                if (this.copyOnDeath && this.persistenceCodec == null) {
                    throw new IllegalStateException("copyOnDeath requires a persistent attachment");
                }

                return InternalHandlerImpl.INSTANCE.impl().createEntityAttachment(
                        this.id,
                        this.defaultValue,
                        this.persistenceCodec,
                        this.syncCodec,
                        this.syncPredicate,
                        this.copyOnDeath
                );
            }
        }
    }

    interface Level<T> extends UnifiedAttachments<T, ServerLevel> {
        static <T> Builder<T> builder(Identifier id, Supplier<T> defaultValue) {
            return new Builder<>(id, defaultValue);
        }

        final class Builder<T> extends UnifiedAttachments.Builder<T, ServerLevel, Level<T>, Builder<T>> {
            private Builder(Identifier id, Supplier<T> defaultValue) {
                super(id, defaultValue);
            }

            @Override
            public Level<T> build() {
                return InternalHandlerImpl.INSTANCE.impl().createLevelAttachment(
                        this.id,
                        this.defaultValue,
                        this.persistenceCodec,
                        this.syncCodec,
                        this.syncPredicate
                );
            }
        }
    }
}
