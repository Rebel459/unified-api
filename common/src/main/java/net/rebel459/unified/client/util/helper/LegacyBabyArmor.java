package net.rebel459.unified.client.util.helper;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.EquipmentAsset;

public interface LegacyBabyArmor {

    default void add(ResourceKey<EquipmentAsset> asset) {
        add(asset, false);
    }
    default void add(ResourceKey<EquipmentAsset> asset, boolean resize) {
        add(asset, resize, 50);
    }
    default void add(ResourceKey<EquipmentAsset> asset, boolean resize, int cutoff) {
        int clampedCutoff = Math.clamp(cutoff, 0, 100);
        int alphaCutoff = resize ? Math.round(255 * (clampedCutoff / 100F)) : 0;
        LegacyBabyArmorImpl.LEGACY_BABY_ARMOR_EQUIPMENT.put(asset, Pair.of(resize, alphaCutoff));
    }
}
