package net.rebel459.unified.api.data.helper;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.rebel459.unified.api.codec.CodecGenerator;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.api.event.LootEntry;
import net.rebel459.unified.impl.client.helper.SimpleBabyArmorImpl;
import net.rebel459.unified.impl.data.helper.LootInjections;
import net.rebel459.unified.impl.data.helper.SimpleBabyArmor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public final class SimpleBabyArmorGenerator extends HelperGenerator {

    public SimpleBabyArmorGenerator(String modId, String namespace, Optional<ExtensibleCodec.Entry<Boolean>> requirement) {
        super(modId, namespace, requirement);
    }

    @SafeVarargs
    public final Builder create(String name, ResourceKey<EquipmentAsset>... assets) {
        return new Builder(name, List.of(assets), true, modId, namespace, requirement);
    }

    @SafeVarargs
    public final Builder createWithoutDownscale(String name, ResourceKey<EquipmentAsset>... assets) {
        return new Builder(name, List.of(assets), false, modId, namespace, requirement);
    }

    public static final class Builder extends HelperGenerator.Builder {

        private int cutoff = 50;

        Builder(String name, List<ResourceKey<EquipmentAsset>> assets, boolean downscale, String modId, String namespace, Optional<ExtensibleCodec.Entry<Boolean>> requirement) {
            super(name, modId, namespace, requirement);
            CodecGenerator.assets(modId, Identifier.fromNamespaceAndPath(namespace, "unified/simple_baby_armor/" + name), requirement, SimpleBabyArmor.Definition.CODEC, () -> new SimpleBabyArmor.Definition(assets, downscale, cutoff));
        }

        public SimpleBabyArmorGenerator.Builder setCutoff(int cutoff) {
            this.cutoff = cutoff;
            return this;
        }
    }
}
