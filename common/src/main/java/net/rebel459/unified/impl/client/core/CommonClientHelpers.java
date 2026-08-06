package net.rebel459.unified.impl.client.core;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.particle.ParticleResources;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.rebel459.unified.impl.client.helper.SimpleBabyArmorImpl;
import net.rebel459.unified.impl.client.platform.ClientPlatformHandler;

import java.util.function.Function;
import java.util.function.Supplier;

public class CommonClientHelpers {

    public interface Tooltips {

        <T extends TooltipComponent> void bind(Class<T> type, Function<T, ClientTooltipComponent> factory);

        static Tooltips get() {
            return ClientPlatformHandler.INSTANCE.getTooltips();
        }
    }

    public interface EntityRenderers {

        void addLayerDefinition(ModelLayerLocation location, Supplier<LayerDefinition> definition);

        <T extends Entity> void addEntityRenderer(Supplier<EntityType<? extends T>> entityType, EntityRendererProvider<T> entityRendererProvider);

        <T extends BlockEntity, S extends BlockEntityRenderState> void addBlockEntityRenderer(Supplier<BlockEntityType<? extends T>> blockEntityType, BlockEntityRendererProvider<T, S> blockEntityRendererProvider);

        static EntityRenderers get() {
            return ClientPlatformHandler.INSTANCE.getEntityRenderers();
        }
    }

    public interface Networking {

        boolean canSend(CustomPacketPayload payload);

        void send(CustomPacketPayload payload);

        static Networking get() {
            return ClientPlatformHandler.INSTANCE.getNetworking();
        }
    }

    public interface ParticleProviders {

        <T extends ParticleOptions> void add(Supplier<? extends ParticleType<T>> type, ParticleResources.SpriteParticleRegistration<T> sprite);


        static ParticleProviders get() {
            return ClientPlatformHandler.INSTANCE.getParticleProviders();
        }
    }

    public interface SimpleBabyArmor {

        default void add(ResourceKey<EquipmentAsset> asset) {
            add(asset, 50);
        }
        default void add(ResourceKey<EquipmentAsset> asset, int cutoff) {
            int clampedCutoff = Math.clamp(cutoff, 0, 100);
            int alphaCutoff = Math.round(255 * (clampedCutoff / 100F));
            SimpleBabyArmorImpl.LEGACY_BABY_ARMOR_EQUIPMENT.put(asset, Pair.of(true, alphaCutoff));
        }

        default void addWithoutDownscale(ResourceKey<EquipmentAsset> asset) {
            SimpleBabyArmorImpl.LEGACY_BABY_ARMOR_EQUIPMENT.put(asset, Pair.of(false, 0));
        }
    }

    public interface ResourcePacks {

        void addRequired(Identifier id);
        void addOptional(Identifier id);
    }

    public interface ReloadListeners {

        void addListener(Identifier id, PreparableReloadListener listener);
        void addOrdering(Identifier first, Identifier second);
    }
}
