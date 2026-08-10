package net.rebel459.unified.impl.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.palette.PalettedTextureManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.rebel459.unified.impl.client.helper.SimpleBabyArmorImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin {

    @Shadow
    @Final
    private ArmorModelSet<?> babyModelSet;

    @Shadow
    @Final
    private EquipmentLayerRenderer equipmentRenderer;

    @SuppressWarnings("unchecked")
    @Inject(method = "renderArmorPiece", at = @At("HEAD"), cancellable = true)
    private void renderLegacyBabyArmor(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, ItemStack itemStack, EquipmentSlot slot, int lightCoords, HumanoidRenderState state, CallbackInfo ci) {
        if (!state.isBaby || state.entityType == EntityTypes.ARMOR_STAND) return;

        Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
        if (equippable == null || equippable.assetId().isEmpty() || equippable.slot() != slot) return;

        EquipmentAssetManager equipmentAssets = this.equipmentRenderer.equipmentAssets;
        ResourceKey<EquipmentAsset> asset = equippable.assetId().orElseThrow();
        if (!SimpleBabyArmorImpl.LEGACY_BABY_ARMOR_EQUIPMENT.containsKey(asset)) return;

        EquipmentClientInfo equipmentInfo = equipmentAssets.get(asset);
        if (!equipmentInfo.getLayers(EquipmentClientInfo.LayerType.HUMANOID_BABY).isEmpty()) return;

        HumanoidModel<HumanoidRenderState> prototype = (HumanoidModel<HumanoidRenderState>) this.babyModelSet.get(slot);
        HumanoidModel<HumanoidRenderState> model = (HumanoidModel<HumanoidRenderState>) SimpleBabyArmorImpl.get((Class<? extends HumanoidModel<?>>) prototype.getClass()).get(slot);
        if (SimpleBabyArmorImpl.isResizable(asset)) {
            this.renderResizedLegacyBabyArmor(equipmentInfo, asset, slot, model, state, itemStack, poseStack, submitNodeCollector, lightCoords);
        } else {
            this.equipmentRenderer.renderLayers(
                    this.getLayer(slot),
                    asset,
                    model,
                    state,
                    itemStack,
                    poseStack,
                    submitNodeCollector,
                    lightCoords,
                    state.outlineColor
            );
        }
        ci.cancel();
    }

    @Unique
    private EquipmentClientInfo.LayerType getLayer(EquipmentSlot slot) {
        return slot == EquipmentSlot.LEGS ? EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS : EquipmentClientInfo.LayerType.HUMANOID;
    }

    @Unique
    private void renderResizedLegacyBabyArmor(
            EquipmentClientInfo equipmentInfo,
            ResourceKey<EquipmentAsset> asset,
            EquipmentSlot slot,
            HumanoidModel<HumanoidRenderState> model,
            HumanoidRenderState state,
            ItemStack itemStack,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int lightCoords
    ) {
        EquipmentClientInfo.LayerType layerType = this.getLayer(slot);
        List<EquipmentClientInfo.Layer> layers = equipmentInfo.getLayers(layerType);
        if (layers.isEmpty()) {
            return;
        }

        int dyeColor = DyedItemColor.getOrDefault(itemStack, 0);
        boolean renderFoil = itemStack.hasFoil();
        ArmorTrim trim = itemStack.get(DataComponents.TRIM);
        boolean renderTrim = trim != null;
        boolean renderLayerGlint = renderFoil && !renderTrim;
        int order = 1;

        for (EquipmentClientInfo.Layer layer : layers) {
            int color = this.getColorForLayer(layer, dyeColor);
            if (color == 0) {
                continue;
            }

            Identifier texture = SimpleBabyArmorImpl.getResizedTexture(layer.getTextureLocation(layerType), asset);
            submitNodeCollector.order(order++)
                    .submitModel(
                            model,
                            state,
                            poseStack,
                            renderLayerGlint ? RenderTypes.armorCutoutNoCullGlint(texture) : RenderTypes.armorCutoutNoCull(texture),
                            lightCoords,
                            OverlayTexture.NO_OVERLAY,
                            color,
                            null,
                            state.outlineColor
                    );
            renderLayerGlint = false;
        }

        if (renderTrim) {
            PalettedTextureManager.Handle trimTexture = this.equipmentRenderer.trimTextureLookup.apply(
                    new EquipmentLayerRenderer.TrimTextureKey(trim, layerType, equipmentInfo)
            );
            submitNodeCollector.order(order++)
                    .submitModel(
                            model,
                            state,
                            poseStack,
                            RenderTypes.armorTrim(trimTexture.textureLocation(), trim.pattern().value().decal()),
                            lightCoords,
                            OverlayTexture.NO_OVERLAY,
                            -1,
                            trimTexture,
                            state.outlineColor
                    );
            if (renderFoil) {
                submitNodeCollector.order(order)
                        .submitModel(
                                model,
                                state,
                                poseStack,
                                RenderTypes.trimmedArmorGlint(),
                                lightCoords,
                                OverlayTexture.NO_OVERLAY,
                                -1,
                                null,
                                0
                        );
            }
        }
    }

    @Unique
    private int getColorForLayer(EquipmentClientInfo.Layer layer, int dyeColor) {
        Optional<EquipmentClientInfo.Dyeable> dyeable = layer.dyeable();
        if (dyeable.isEmpty()) {
            return -1;
        }

        int colorWhenUndyed = dyeable.get().colorWhenUndyed().map(ARGB::opaque).orElse(0);
        return dyeColor != 0 ? dyeColor : colorWhenUndyed;
    }
}
