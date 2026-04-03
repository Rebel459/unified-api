package net.rebel459.unified.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.Equippable;
import net.rebel459.unified.client.util.helper.LegacyBabyArmorImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
        if (!state.isBaby || state.entityType == EntityType.ARMOR_STAND) return;

        Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
        if (equippable == null || equippable.assetId().isEmpty() || equippable.slot() != slot) return;

        EquipmentAssetManager equipmentAssets = this.equipmentRenderer.equipmentAssets;
        ResourceKey<EquipmentAsset> asset = equippable.assetId().orElseThrow();
        if (!LegacyBabyArmorImpl.LEGACY_BABY_ARMOR_EQUIPMENT.contains(asset)) return;

        EquipmentClientInfo equipmentInfo = equipmentAssets.get(asset);
        if (!equipmentInfo.getLayers(EquipmentClientInfo.LayerType.HUMANOID_BABY).isEmpty()) return;

        HumanoidModel<HumanoidRenderState> prototype = (HumanoidModel<HumanoidRenderState>) this.babyModelSet.get(slot);
        HumanoidModel<HumanoidRenderState> model = (HumanoidModel<HumanoidRenderState>) LegacyBabyArmorImpl.get((Class<? extends HumanoidModel<?>>) prototype.getClass()).get(slot);
        this.equipmentRenderer.renderLayers(
                this.getLayer(slot),
                equippable.assetId().orElseThrow(),
                model,
                state,
                itemStack,
                poseStack,
                submitNodeCollector,
                lightCoords,
                state.outlineColor
        );
        ci.cancel();
    }

    @Unique
    private EquipmentClientInfo.LayerType getLayer(EquipmentSlot slot) {
        return slot == EquipmentSlot.LEGS ? EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS : EquipmentClientInfo.LayerType.HUMANOID;
    }
}