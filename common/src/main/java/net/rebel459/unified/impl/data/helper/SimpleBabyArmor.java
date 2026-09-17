package net.rebel459.unified.impl.data.helper;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.rebel459.unified.Unified;
import net.rebel459.unified.api.client.core.UnifiedClientHelpers;
import net.rebel459.unified.api.codec.UnifiedCodecs;
import net.rebel459.unified.impl.client.helper.SimpleBabyArmorImpl;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SimpleBabyArmor extends SimpleJsonResourceReloadListener<SimpleBabyArmor.Definition> {

    private static final ResourceKey<Registry<EquipmentAsset>> EQUIPMENT_ASSET = ResourceKey.createRegistryKey(Identifier.withDefaultNamespace("equipment_asset"));

    public static final Identifier ID = Unified.id("simple_baby_armor");

    private volatile List<ResourceKey<EquipmentAsset>> previousAssets = List.of();

    public SimpleBabyArmor() {
        super(UnifiedCodecs.loadRequirements(Definition.CODEC, () -> new Definition(ResourceKey.create(EQUIPMENT_ASSET, Unified.id("empty")), true, 50)), FileToIdConverter.json(Unified.MOD_ID + "/" + ID.getPath()));
    }

    @Override
    protected void apply(@NonNull Map<Identifier, SimpleBabyArmor.Definition> entries, @NonNull ResourceManager resourceManager, @NonNull ProfilerFiller profilerFiller) {
        SimpleBabyArmorImpl.onReload(previousAssets);
        List<ResourceKey<EquipmentAsset>> loaded = new ArrayList<>();
        entries.forEach((_, definition) -> {
            if (definition.downscale) UnifiedClientHelpers.SIMPLE_BABY_ARMOR.add(definition.equipmentAsset, definition.cutoff);
            else UnifiedClientHelpers.SIMPLE_BABY_ARMOR.addWithoutDownscale(definition.equipmentAsset);
            loaded.add(definition.equipmentAsset);
        });
        previousAssets = List.copyOf(loaded);
    }

    public record Definition(ResourceKey<EquipmentAsset> equipmentAsset, Boolean downscale, Integer cutoff) {
        public static final Codec<Definition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceKey.codec(EQUIPMENT_ASSET).fieldOf("equipment_asset").forGetter(Definition::equipmentAsset),
                Codec.BOOL.optionalFieldOf("downscale", true).forGetter(Definition::downscale),
                ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("cutoff", 50).forGetter(Definition::cutoff)
        ).apply(instance, Definition::new));
    }
}
