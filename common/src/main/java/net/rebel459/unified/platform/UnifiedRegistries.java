package net.rebel459.unified.platform;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class UnifiedRegistries {

    public interface Items {
        String modId();

        Supplier<Item> register(String name, Function<Item.Properties, Item> function, Supplier<Item.Properties> properties);

        <T extends Block> Supplier<BlockItem> registerBlockItem(String name, Supplier<T> blockSupplier, Supplier<Item.Properties> properties);

        static Items create(String modId) {
            return PlatformHelperImpl.INSTANCE.createItems(modId);
        }
    }

    public interface Blocks {
        String modId();

        <T extends Block> Supplier<T> register(String name, Function<BlockBehaviour.Properties, T> function, BlockBehaviour.Properties blockProperties);
        <T extends Block, Y extends BlockEntity> Supplier<T> register(String name, Function<BlockBehaviour.Properties, T> function, BlockBehaviour.Properties blockProperties, BlockEntityType<Y> type);

        <T extends Block> Supplier<T> registerWithoutItem(String name, Function<BlockBehaviour.Properties, T> function, BlockBehaviour.Properties properties);
        <T extends Block, Y extends BlockEntity> Supplier<T> registerWithoutItem(String name, Function<BlockBehaviour.Properties, T> function, BlockBehaviour.Properties properties, BlockEntityType<Y> type);

        static Blocks create(String modId) {
            return PlatformHelperImpl.INSTANCE.createBlocks(modId);
        }
    }

    public interface CreativeTabs {
        String modId();

        ResourceKey<CreativeModeTab> registerTab(String path, Supplier<? extends ItemLike> icon);

        static CreativeTabs create(String modId) {
            return PlatformHelperImpl.INSTANCE.createCreativeTabs(modId);
        }
    }

    public interface ItemComponents {
        String modId();

        <T> Supplier<DataComponentType<T>> register(String string, UnaryOperator<DataComponentType.Builder<T>> unaryOperator);

        static ItemComponents create(String modId) {
            return PlatformHelperImpl.INSTANCE.createItemComponents(modId);
        }
    }

    public interface Particles {
        String modId();

        <T extends ParticleType> Supplier<T> register(String path, ParticleType type);

        static Particles create(String modId) {
            return PlatformHelperImpl.INSTANCE.createParticles(modId);
        }
    }

    public interface MobEffects {
        String modId();

        Holder<MobEffect> register(String path, MobEffect effect);

        static MobEffects create(String modId) {
            return PlatformHelperImpl.INSTANCE.createMobEffects(modId);
        }
    }

    public interface EntityTypes {
        String modId();

        <T extends Entity> @NotNull Supplier<EntityType<T>> register(String string, EntityType.@NotNull Builder<T> builder);

        static EntityTypes create(String modId) {
            return PlatformHelperImpl.INSTANCE.createEntityTypes(modId);
        }
    }
}