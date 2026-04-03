package net.rebel459.unified.client.util.helper;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.EquipmentAsset;

public interface LegacyBabyArmor {

    default void add(ResourceKey<EquipmentAsset> asset) {
        LegacyBabyArmorImpl.LEGACY_BABY_ARMOR_EQUIPMENT.add(asset);
    }
}