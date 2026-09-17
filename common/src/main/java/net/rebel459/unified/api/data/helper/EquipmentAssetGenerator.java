package net.rebel459.unified.api.data.helper;

import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.rebel459.unified.api.codec.CodecGenerator;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.api.codec.ExtensibleCodecBase;

import java.util.Optional;
import java.util.function.Supplier;

public final class EquipmentAssetGenerator extends HelperGenerator {

    public EquipmentAssetGenerator(String modId, Optional<ExtensibleCodec.Entry<Boolean>> requirement) {
        super(modId, requirement);
    }

    public void add(ResourceKey<EquipmentAsset> asset, EquipmentClientInfo info) {
        Identifier id = asset.identifier();
        CodecGenerator.assets(modId, Identifier.fromNamespaceAndPath(id.getNamespace(), "equipment/" + id.getPath()), requirement, EquipmentClientInfo.CODEC, () -> info);
    }
}