package net.rebel459.unified.api.data.registry;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.registries.Registries;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import net.minecraft.world.phys.AABB;
import net.rebel459.unified.api.asset.BlockAsset;
import net.rebel459.unified.api.asset.BlockAssets;
import net.rebel459.unified.api.codec.CodecGenerator;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.api.codec.ExtensibleCodecs;
import net.rebel459.unified.api.core.SuppliedBlock;
import net.rebel459.unified.api.registry.VanillaItemCodecs;
import net.rebel459.unified.api.util.RecipeProvider;
import net.rebel459.unified.impl.asset.BlockAssetRequest;
import net.rebel459.unified.impl.core.DataProviders;
import net.rebel459.unified.impl.data.registry.BlockRegistry;
import net.rebel459.unified.impl.data.registry.BlockSetTypeRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.*;

public class BlockSetTypeGenerator {

    private final String modId;
    private final String namespace;
    private final DataProviders.GenerationSettings settings;

    public BlockSetTypeGenerator(String modId, String namespace, DataProviders.GenerationSettings settings) {
        this.modId = modId;
        this.namespace = namespace;
        this.settings = settings;
    }

    public Supplier<BlockSetType> register(String path, Supplier<BlockSetType> blockSetType) {
        Identifier id = Identifier.fromNamespaceAndPath(namespace, path);
        CodecGenerator.registry(modId, Identifier.fromNamespaceAndPath(namespace, path), "block_set_types", settings.metadata().priority(), settings.metadata().requirement(), BlockSetTypeRegistry.CODEC, () -> {
            BlockSetType type = blockSetType.get();
            return new BlockSetTypeRegistry.Definition(
                    type.canOpenByHand(),
                    type.canOpenByWindCharge(),
                    type.canButtonBeActivatedByArrows(),
                    type.pressurePlateSensitivity(),
                    BlockRegistry.SoundType.create(type.soundType()),
                    type.doorClose(),
                    type.doorOpen(),
                    type.trapdoorClose(),
                    type.trapdoorOpen(),
                    type.pressurePlateClickOff(),
                    type.pressurePlateClickOn(),
                    type.buttonClickOff(),
                    type.buttonClickOn()
            );
        });
        return () -> {
            BlockSetType registered = BlockSetType.TYPES.get(id.toString());
            return registered != null ? registered : blockSetType.get();
        };
    }
}
