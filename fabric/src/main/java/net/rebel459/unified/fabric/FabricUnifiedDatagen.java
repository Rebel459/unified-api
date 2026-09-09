package net.rebel459.unified.fabric;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.rebel459.unified.api.asset.BlockAsset;
import net.rebel459.unified.api.asset.ItemAsset;
import net.rebel459.unified.fabric.datagen.FabricDatagenProvider;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class FabricUnifiedDatagen {

    public static void register(FabricDataGenerator generator) {
        String modId = generator.getModId();
        FabricDataGenerator.Pack pack = generator.createPack();
        pack.addProvider((FabricDataGenerator.Pack.Factory<FabricDatagenProvider.DefinitionProvider>) output -> new FabricDatagenProvider.DefinitionProvider(output, modId));
        pack.addProvider((FabricDataGenerator.Pack.Factory<FabricDatagenProvider.LanguageProvider>) output -> new FabricDatagenProvider.LanguageProvider(output, modId));
        pack.addProvider((FabricDataGenerator.Pack.Factory<FabricDatagenProvider.ModelsProvider>) output -> new FabricDatagenProvider.ModelsProvider(output, modId));
        pack.addProvider((output, registries) -> new FabricDatagenProvider.BlockLootProvider(output, registries, modId));
        pack.addProvider((output, registries) -> new FabricDatagenProvider.RecipesProvider(output, registries, modId));
        FabricDatagenProvider.BlockTagsProvider blockTags = pack.addProvider((output, registries) -> new FabricDatagenProvider.BlockTagsProvider(output, registries, modId));
        pack.addProvider((output, registries) -> new FabricDatagenProvider.ItemTagsProvider(output, registries, modId, blockTags));
    }

    public static synchronized void registerBlockAsset(BlockAsset<Void> type, Consumer<FabricDatagenProvider.BlockModelContext> generator) {
        registerBlockAsset(type, (_, context) -> generator.accept(context));
    }
    public static synchronized <T> void registerBlockAsset(BlockAsset<T> type, BiConsumer<T, FabricDatagenProvider.BlockModelContext> generator) {
        FabricDatagenProvider.BlockAssetAdapter<T> adapter = generator::accept;
        if (FabricDatagenProvider.BLOCK_ASSET_ADAPTERS.putIfAbsent(type, adapter) != null) throw new IllegalArgumentException("Duplicate Fabric block asset binding for " + type.id());
    }
    public static synchronized void registerItemAsset(ItemAsset<Void> type, Consumer<FabricDatagenProvider.ItemModelContext> generator) {
        registerItemAsset(type, (_, context) -> generator.accept(context));
    }
    public static synchronized <T> void registerItemAsset(ItemAsset<T> type, BiConsumer<T, FabricDatagenProvider.ItemModelContext> generator) {
        FabricDatagenProvider.ItemAssetAdapter<T> adapter = generator::accept;
        if (FabricDatagenProvider.ITEM_ASSET_ADAPTERS.putIfAbsent(type, adapter) != null) throw new IllegalArgumentException("Duplicate Fabric item asset binding for " + type.id());
    }
}
