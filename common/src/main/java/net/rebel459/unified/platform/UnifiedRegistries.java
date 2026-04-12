package net.rebel459.unified.platform;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.rebel459.unified.util.SuppliedBlock;
import net.rebel459.unified.util.SuppliedItem;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class UnifiedRegistries {

    public interface DeferredRegistry {
        String modId();

        <V, T extends V> Supplier<T> register(String path, Supplier<T> value);

        <V, T extends V> Holder<T> registerHolder(String path, Supplier<T> value);

        void addAlias(Identifier convertedFrom, Identifier convertedTo);

        static DeferredRegistry create(String modId, Registry<?> registry) {
            return InternalHandlerImpl.INSTANCE.createDeferredRegistry(modId, registry);
        }
    }

    public interface Items {
        String modId();

        SuppliedItem register(String path, Function<Item.Properties, Item> function, Supplier<Item.Properties> properties);

        void addAlias(Identifier convertedFrom, Identifier convertedTo);

        <T extends Block> SuppliedItem registerBlockItem(String path, Supplier<T> blockSupplier, Supplier<Item.Properties> properties);

        static Items create(String modId) {
            return InternalHandlerImpl.INSTANCE.createItems(modId);
        }
    }

    public interface Blocks {
        String modId();

        <T extends Block> SuppliedBlock register(String path, Function<BlockBehaviour.Properties, T> function, Supplier<BlockBehaviour.Properties> blockProperties);
        <T extends Block, Y extends BlockEntity> SuppliedBlock register(String path, Function<BlockBehaviour.Properties, T> function, Supplier<BlockBehaviour.Properties> blockProperties, BlockEntityType<Y> type);

        <T extends Block> SuppliedBlock registerWithoutItem(String path, Function<BlockBehaviour.Properties, T> function, Supplier<BlockBehaviour.Properties> properties);
        <T extends Block, Y extends BlockEntity> SuppliedBlock registerWithoutItem(String path, Function<BlockBehaviour.Properties, T> function, Supplier<BlockBehaviour.Properties> properties, BlockEntityType<Y> type);

        @Deprecated
        <T extends Block> SuppliedBlock register(String path, Function<BlockBehaviour.Properties, T> blockFunction, Supplier<BlockBehaviour.Properties> blockProperties, Supplier<Item.Properties> itemProperties);
        @Deprecated
        <T extends Block> SuppliedBlock register(String path, Function<BlockBehaviour.Properties, T> blockFunction, Supplier<BlockBehaviour.Properties> blockProperties, Function<Item.Properties, Item> itemFunction);
        @Deprecated
        <T extends Block> SuppliedBlock register(String path, Function<BlockBehaviour.Properties, T> blockFunction, Supplier<BlockBehaviour.Properties> blockProperties, Function<Item.Properties, Item> itemFunction, Supplier<Item.Properties> itemProperties);
        @Deprecated
        <T extends Block, Y extends BlockEntity> SuppliedBlock register(String path, Function<BlockBehaviour.Properties, T> blockFunction, Supplier<BlockBehaviour.Properties> blockProperties, Supplier<Item.Properties> itemProperties, BlockEntityType<Y> type);
        @Deprecated
        <T extends Block, Y extends BlockEntity> SuppliedBlock register(String path, Function<BlockBehaviour.Properties, T> blockFunction, Supplier<BlockBehaviour.Properties> blockProperties, Function<Item.Properties, Item> itemFunction, BlockEntityType<Y> type);
        @Deprecated
        <T extends Block, Y extends BlockEntity> SuppliedBlock register(String path, Function<BlockBehaviour.Properties, T> blockFunction, Supplier<BlockBehaviour.Properties> blockProperties, Function<Item.Properties, Item> itemFunction, Supplier<Item.Properties> itemProperties, BlockEntityType<Y> type);

        void addAlias(Identifier convertedFrom, Identifier convertedTo);

        static Blocks create(String modId) {
            return InternalHandlerImpl.INSTANCE.createBlocks(modId);
        }
    }

    public interface CreativeTabs {
        String modId();

        ResourceKey<CreativeModeTab> register(String path, Supplier<? extends ItemLike> icon);

        static CreativeTabs create(String modId) {
            return InternalHandlerImpl.INSTANCE.createCreativeTabs(modId);
        }
    }

    public interface DataComponentTypes {
        String modId();

        <T> Supplier<DataComponentType<T>> register(String path, UnaryOperator<DataComponentType.Builder<T>> unaryOperator);

        void addAlias(Identifier convertedFrom, Identifier convertedTo);

        static DataComponentTypes create(String modId) {
            return InternalHandlerImpl.INSTANCE.createDataComponentTypes(modId);
        }
    }

    public interface EntityTypes {
        String modId();

        <T extends Entity> @NotNull Supplier<EntityType<T>> register(String path, EntityType.@NotNull Builder<T> builder);

        void addAlias(Identifier convertedFrom, Identifier convertedTo);

        static EntityTypes create(String modId) {
            return InternalHandlerImpl.INSTANCE.createEntityTypes(modId);
        }
    }

    public interface BlockEntityTypes {
        String modId();

        @NotNull <T extends BlockEntity> Supplier<BlockEntityType<T>> register(String path, @NotNull BlockEntityType.BlockEntitySupplier<T> builder);
        @NotNull <T extends BlockEntity> Supplier<BlockEntityType<T>> register(String path, @NotNull BlockEntityType.BlockEntitySupplier<T> builder, Block... blocks);

        void addAlias(Identifier convertedFrom, Identifier convertedTo);

        static BlockEntityTypes create(String modId) {
            return InternalHandlerImpl.INSTANCE.createBlockEntityTypes(modId);
        }
    }

    public interface SoundEvents {
        String modId();

        Supplier<SoundEvent> register(String path);
        Supplier<SoundEvent> register(String path, float fixedRange);

        Holder<SoundEvent> registerHolder(String path);
        Holder<SoundEvent> registerHolder(String path, float fixedRange);

        static SoundEvents create(String modId) {
            return InternalHandlerImpl.INSTANCE.createSoundEvents(modId);
        }
    }
}