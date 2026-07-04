package net.rebel459.unified.platform;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.rebel459.unified.util.BlockLike;
import net.rebel459.unified.util.registry.Supplied;
import net.rebel459.unified.util.registry.SuppliedBlock;
import net.rebel459.unified.util.registry.SuppliedItem;
import net.rebel459.unified.util.registry.builder.WoodPreset;
import net.rebel459.unified.util.registry.builder.WoodSet;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class UnifiedRegistries {

    public interface DeferredRegistry<Y> {
        String modId();

        <T extends Y> Supplied<T> register(String path, Supplier<T> value);

        <T extends Y> Holder<T> registerForHolder(String path, Supplier<T> value);

        @Deprecated
        <T extends Y> Holder<T> registerHolder(String path, Supplier<T> value);

        void addAlias(Identifier convertedFrom, Identifier convertedTo);

        static <Y> DeferredRegistry<Y> create(String modId, Registry<Y> registry) {
            return InternalHandlerImpl.INSTANCE.createDeferredRegistry(modId, registry);
        }
    }

    public interface Items {
        String modId();

        SuppliedItem register(String path, Function<Item.Properties, Item> function, Supplier<Item.Properties> properties);

        void addAlias(Identifier convertedFrom, Identifier convertedTo);

        public SuppliedItem registerBlockItem(SuppliedBlock block, Supplier<Item.Properties> properties);
        <T extends Block> SuppliedItem registerBlockItem(String path, Supplier<T> blockSupplier, Supplier<Item.Properties> properties);

        static Items create(String modId) {
            return InternalHandlerImpl.INSTANCE.createItems(modId);
        }
    }

    public interface Blocks {
        String modId();

        <T extends Block> SuppliedBlock register(String path, Function<BlockBehaviour.Properties, T> function, Supplier<BlockBehaviour.Properties> blockProperties);
        <T extends Block, Y extends BlockEntity> SuppliedBlock register(String path, Function<BlockBehaviour.Properties, T> function, Supplier<BlockBehaviour.Properties> blockProperties, BlockEntityType<Y> type);
        <T extends Block, Y extends BlockEntity> SuppliedBlock register(String path, Function<BlockBehaviour.Properties, T> function, Supplier<BlockBehaviour.Properties> blockProperties, Supplier<BlockEntityType<Y>> type);

        <T extends Block> SuppliedBlock registerWithoutItem(String path, Function<BlockBehaviour.Properties, T> function, Supplier<BlockBehaviour.Properties> properties);
        <T extends Block, Y extends BlockEntity> SuppliedBlock registerWithoutItem(String path, Function<BlockBehaviour.Properties, T> function, Supplier<BlockBehaviour.Properties> properties, BlockEntityType<Y> type);
        <T extends Block, Y extends BlockEntity> SuppliedBlock registerWithoutItem(String path, Function<BlockBehaviour.Properties, T> function, Supplier<BlockBehaviour.Properties> properties, Supplier<BlockEntityType<Y>> type);

        default Builders builders() {
            return new Builders(modId());
        }

        class Builders {

            private final String modId;
            private final UnifiedRegistries.Items itemRegistry;
            private final UnifiedRegistries.Blocks blockRegistry;
            private final UnifiedRegistries.EntityTypes entityRegistry;

            public Builders(String modId){
                this.modId = modId;
                this.itemRegistry = UnifiedRegistries.Items.create(modId);
                this.blockRegistry = UnifiedRegistries.Blocks.create(modId);
                this.entityRegistry = UnifiedRegistries.EntityTypes.create(modId);
            }

            public WoodSet.Builder<WoodSet.RegistryBuilder> woodsetBuilder(String name, WoodPreset preset, MapColor barkColor, MapColor plankColor) {
                return new WoodSet.RegistryBuilder(Identifier.fromNamespaceAndPath(this.modId, name), barkColor, plankColor, preset, this.itemRegistry, this.blockRegistry, this.entityRegistry);
            }
        }

        @Deprecated
        <T extends Block> SuppliedBlock register(String path, Function<BlockBehaviour.Properties, T> blockFunction, Supplier<BlockBehaviour.Properties> blockProperties, Function<Item.Properties, Item> itemFunction);
        @Deprecated
        <T extends Block> SuppliedBlock register(String path, Function<BlockBehaviour.Properties, T> blockFunction, Supplier<BlockBehaviour.Properties> blockProperties, Function<Item.Properties, Item> itemFunction, Supplier<Item.Properties> itemProperties);
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

        <T> Supplied<DataComponentType<T>> register(String path, UnaryOperator<DataComponentType.Builder<T>> unaryOperator);

        void addAlias(Identifier convertedFrom, Identifier convertedTo);

        static DataComponentTypes create(String modId) {
            return InternalHandlerImpl.INSTANCE.createDataComponentTypes(modId);
        }
    }

    public interface EntityTypes {
        String modId();

        <T extends Entity> Supplied<EntityType<T>> register(String path, EntityType.Builder<T> builder);
        <T extends LivingEntity> Supplied<EntityType<T>> register(String path, EntityType.Builder<T> builder, AttributeSupplier attributes);

        void addAlias(Identifier convertedFrom, Identifier convertedTo);

        static EntityTypes create(String modId) {
            return InternalHandlerImpl.INSTANCE.createEntityTypes(modId);
        }
    }

    public interface BlockEntityTypes {
        String modId();

        <T extends BlockEntity> Supplied<BlockEntityType<T>> register(String path, BlockEntityType.BlockEntitySupplier<T> builder);
        <T extends BlockEntity> Supplied<BlockEntityType<T>> register(String path, BlockEntityType.BlockEntitySupplier<T> builder, BlockLike... blocks);
        @Deprecated
        <T extends BlockEntity> Supplied<BlockEntityType<T>> register(String path, BlockEntityType.BlockEntitySupplier<T> builder, Block... blocks);

        void addAlias(Identifier convertedFrom, Identifier convertedTo);

        static BlockEntityTypes create(String modId) {
            return InternalHandlerImpl.INSTANCE.createBlockEntityTypes(modId);
        }
    }

    public interface SoundEvents {
        String modId();

        Supplied<SoundEvent> register(String path);
        Supplied<SoundEvent> register(String path, float fixedRange);

        Holder<SoundEvent> registerForHolder(String path);
        Holder<SoundEvent> registerForHolder(String path, float fixedRange);

        @Deprecated
        Holder<SoundEvent> registerHolder(String path);
        @Deprecated
        Holder<SoundEvent> registerHolder(String path, float fixedRange);

        static SoundEvents create(String modId) {
            return InternalHandlerImpl.INSTANCE.createSoundEvents(modId);
        }
    }
}