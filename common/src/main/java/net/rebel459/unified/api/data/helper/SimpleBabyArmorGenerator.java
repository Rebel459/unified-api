package net.rebel459.unified.api.data.helper;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.rebel459.unified.api.codec.CodecGenerator;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.impl.client.helper.SimpleBabyArmorImpl;
import net.rebel459.unified.impl.data.helper.SimpleBabyArmor;

import java.util.Optional;

public final class SimpleBabyArmorGenerator extends HelperGenerator {

    public SimpleBabyArmorGenerator(String modId, Optional<ExtensibleCodec.Entry<Boolean>> requirement) {
        super(modId, requirement);
    }

    public void add(String name, ResourceKey<EquipmentAsset> asset) {
        add(name, asset, 50);
    }
    public void add(String name, ResourceKey<EquipmentAsset> asset, int cutoff) {
        add(name, asset, true, cutoff);
    }

    public void addWithoutDownscale(String name, ResourceKey<EquipmentAsset> asset) {
        add(name, asset, false, 50);
    }

    private void add(String name, ResourceKey<EquipmentAsset> asset, boolean downscale, int cutoff) {
        CodecGenerator.assets(modId, Identifier.fromNamespaceAndPath(modId, "unified/simple_baby_armor/" + name), requirement, SimpleBabyArmor.Definition.CODEC, () -> new SimpleBabyArmor.Definition(asset, downscale, cutoff));
    }
}