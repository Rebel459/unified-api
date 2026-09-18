package net.rebel459.unified.api.core;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.rebel459.unified.api.builder.*;
import net.rebel459.unified.impl.core.StagedRegistries;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class UnifiedRegistries {

    public interface DeferredRegistry<Y> {
        String modId();

        <T extends Y> Supplied<T> register(String path, Supplier<T> value);

        void addAlias(Identifier convertedFrom, Identifier convertedTo);

        static <Y> DeferredRegistry<Y> create(String modId, Registry<Y> registry) {
            return new StagedRegistries.StagedDeferredRegistry<>(modId, registry);
        }
    }

    public interface Items {
        String modId();

        SuppliedItem register(String path, Function<Item.Properties, Item> function, Supplier<Item.Properties> properties);

        void addAlias(Identifier convertedFrom, Identifier convertedTo);

        SuppliedItem registerBlockItem(SuppliedBlock block, BiFunction<Block, Item.Properties, Item> function, Supplier<Item.Properties> properties);
        <T extends Block> SuppliedItem registerBlockItem(BlockItemId id, Supplier<T> block, BiFunction<Block, Item.Properties, Item> function, Supplier<Item.Properties> properties);

        static Items create(String modId) {
            return new StagedRegistries.StagedItems(modId);
        }
    }

    public interface Blocks {
        String modId();

        <T extends Block> SuppliedBlock register(String path, Function<BlockBehaviour.Properties, T> function, Supplier<BlockBehaviour.Properties> blockProperties);
        <T extends Block, Y extends BlockEntity> SuppliedBlock register(String path, Function<BlockBehaviour.Properties, T> function, Supplier<BlockBehaviour.Properties> blockProperties, Supplier<BlockEntityType<Y>> type);

        <T extends Block> SuppliedBlock registerWithoutItem(String path, Function<BlockBehaviour.Properties, T> function, Supplier<BlockBehaviour.Properties> properties);
        <T extends Block, Y extends BlockEntity> SuppliedBlock registerWithoutItem(String path, Function<BlockBehaviour.Properties, T> function, Supplier<BlockBehaviour.Properties> properties, Supplier<BlockEntityType<Y>> type);

        <T extends Block> SuppliedBlock registerWithoutItem(String blockPath, String itemPath, Function<BlockBehaviour.Properties, T> function, Supplier<BlockBehaviour.Properties> properties);
        <T extends Block, Y extends BlockEntity> SuppliedBlock registerWithoutItem(String blockPath, String itemPath, Function<BlockBehaviour.Properties, T> function, Supplier<BlockBehaviour.Properties> properties, Supplier<BlockEntityType<Y>> type);

        void addAlias(Identifier convertedFrom, Identifier convertedTo);

        static Blocks create(String modId) {
            return new StagedRegistries.StagedBlocks(modId);
        }
    }

    public interface DataComponentTypes {
        String modId();

        <T> Supplied<DataComponentType<T>> register(String path, UnaryOperator<DataComponentType.Builder<T>> unaryOperator);

        void addAlias(Identifier convertedFrom, Identifier convertedTo);

        static DataComponentTypes create(String modId) {
            return new StagedRegistries.StagedDataComponentTypes(modId);
        }
    }

    public interface EntityTypes {
        String modId();

        <T extends Entity> Supplied<EntityType<T>> register(String path, EntityType.Builder<T> builder);
        <T extends LivingEntity> Supplied<EntityType<T>> register(String path, EntityType.Builder<T> builder, Supplier<AttributeSupplier> attributes);

        void addAlias(Identifier convertedFrom, Identifier convertedTo);

        static EntityTypes create(String modId) {
            return new StagedRegistries.StagedEntityTypes(modId);
        }
    }

    public interface SoundEvents {
        String modId();

        Supplied<SoundEvent> register(String path);
        Supplied<SoundEvent> register(String path, float fixedRange);

        static SoundEvents create(String modId) {
            return new StagedRegistries.StagedSoundEvents(modId);
        }
    }
}
