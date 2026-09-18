package net.rebel459.unified.api.builder;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import net.rebel459.unified.api.asset.BlockAsset;
import net.rebel459.unified.api.asset.BlockAssets;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.api.core.SuppliedBlock;
import net.rebel459.unified.api.core.SuppliedItem;
import net.rebel459.unified.api.core.UnifiedInstance;
import net.rebel459.unified.api.data.helper.BlockConversionGenerator;
import net.rebel459.unified.api.data.helper.CreativeEntryGenerator;
import net.rebel459.unified.api.data.helper.TagGenerator;
import net.rebel459.unified.api.data.registry.*;
import net.rebel459.unified.api.platform.ModLoader;
import net.rebel459.unified.api.registry.*;
import net.rebel459.unified.api.util.BlockLootProvider;
import net.rebel459.unified.api.util.RecipeProvider;
import net.rebel459.unified.impl.builder.WoodSetProperties;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.*;

public class WoodSet {

    public static final List<WoodSet> WOOD_SETS = new CopyOnWriteArrayList<>();

    private final List<SuppliedBlock> registeredBlocks = new ArrayList<>();
    private final List<SuppliedItem> registeredItems = new ArrayList<>();

    private final Identifier id;
    private final BlockItemTagId logTag;
    private final MapColor barkColor;
    private final MapColor plankColor;

    private final BlockGenerator blocks;
    private final ItemGenerator items;
    private final EntityGenerator entities;

    private final TagGenerator tags;

    private SuppliedBlock log;
    private SuppliedBlock strippedLog;
    private @Nullable SuppliedBlock wood;
    private @Nullable SuppliedBlock strippedWood;
    private @Nullable Map<String, SuppliedBlock> leaves = null;
    private @Nullable SuppliedBlock sapling = null;
    private @Nullable SuppliedBlock pottedSapling = null;
    private SuppliedBlock planks;
    private SuppliedBlock stairs;
    private SuppliedBlock slab;
    private @Nullable SuppliedBlock mosaic;
    private @Nullable SuppliedBlock mosaicStairs;
    private @Nullable SuppliedBlock mosaicSlab;
    private SuppliedBlock fence;
    private SuppliedBlock fenceGate;
    private SuppliedBlock pressurePlate;
    private SuppliedBlock button;
    private SuppliedBlock door;
    private SuppliedBlock trapdoor;
    private SuppliedBlock sign;
    private SuppliedBlock wallSign;
    private SuppliedBlock hangingSign;
    private SuppliedBlock wallHangingSign;
    private SuppliedBlock shelf;

    private SuppliedItem signItem;
    private SuppliedItem hangingSignItem;
    private @Nullable SuppliedItem boatItem;
    private @Nullable SuppliedItem chestBoatItem;

    private @Nullable Supplier<? extends EntityType<?>> boat;
    private @Nullable Supplier<? extends EntityType<?>> chestBoat;

    private BlockFamily.Builder blockFamily = null;
    private Supplier<WoodType> woodType = null;

    private final Settings settings;

    private void registerWood() {
        var logTag = tags.create(this.logTag);

        planks = createPlanks();
        tags.create(BlockItemTags.PLANKS).add(planks.blockItemId());

        log = createLog();
        logTag.add(log.blockItemId());
        strippedLog = createStrippedLog();
        logTag.add(strippedLog.blockItemId());
		if (this.hasWood()) {
			wood = createWood();
            logTag.add(wood.blockItemId());
			strippedWood = createStrippedWood();
            logTag.add(strippedWood.blockItemId());
		}

        if (hasMosaic()){
            mosaic = createMosaic();
            mosaicStairs = createMosaicStairs();
            mosaicSlab = createMosaicSlab();
        }
        if (this.settings.leaves != null){
            leaves = createLeaves();
        }
        if (this.settings.sapling != null){
            sapling = createSapling();
            pottedSapling = createPottedSapling(sapling);
        }
        stairs = createStairs();
        slab = createSlab();
        fence = createFence();
        fenceGate = createFenceGate();
        pressurePlate = createPressurePlate();
        button = createButton();
        door = createDoor();
        trapdoor = createTrapDoor();
        sign = createSign();
        wallSign = createWallSign();
        hangingSign = createHangingSign();
        wallHangingSign = createWallHangingSign();
        shelf = createShelf();

        signItem = createSignItem();
        hangingSignItem = createHangingSignItem();

        if (hasBoats()){
            boat = createBoatEntity();
            chestBoat = createChestBoatEntity();
            boatItem = createBoatItem();
            chestBoatItem = createChestBoatItem();
        }

        if (getSettings().isOverworld) tags.create(BlockTags.OVERWORLD_NATURAL_LOGS).add(this.logTag.block());
        else tags.create(BlockTags.LOGS).add(this.logTag.block());

        if (settings.isFlammable) tags.create(ItemTags.LOGS_THAT_BURN).add(this.logTag.item());
        if (!settings.isFlammable) {
            tags.create(ItemTags.LOGS).add(this.logTag.item());
            var nonFlammableTag = tags.create(ItemTags.NON_FLAMMABLE_WOOD);
            for (SuppliedItem item : registeredItems) {
                nonFlammableTag.add(item.key());
            }
            for (SuppliedBlock block : registeredBlocks) {
                nonFlammableTag.add(block.blockItemId().item());
            }
        }
    }

    public WoodSet(Identifier id, BlockItemTagId logTag, MapColor sideColor, MapColor plankColor, Settings settings, BlockGenerator blocks, ItemGenerator items, EntityGenerator entities, BlockSetTypeGenerator blockSetTypes, WoodTypeGenerator woodTypes, TagGenerator tags, CreativeEntryGenerator creativeEntries, BlockConversionGenerator blockConversions) {
        this.settings = settings;
        this.id = id;
        this.logTag = logTag;
        this.barkColor = sideColor;
        this.plankColor = plankColor;
        this.blocks = blocks;
        this.items = items;
        this.entities = entities;
        this.tags = tags;
        registerWood();
        blockSetTypes.register(id.getPath(), () -> getWoodType().get().setType());
        woodTypes.register(id.getPath(), () -> getWoodType().get());
        WOOD_SETS.add(this);
        WoodSetProperties.CREATIVE_ENTRIES.put(id, getSettings().precedingCreativeEntries);
        WoodSetProperties.CREATIVE_ENTRY_GENERATORS.put(id, creativeEntries);
        WoodSetProperties.BLOCK_CONVERSION_GENERATORS.put(id, blockConversions);
        if (UnifiedInstance.getModLoader() == ModLoader.FABRIC) {
            net.rebel459.unified.impl.platform.PlatformHandler.INSTANCE.internal()
                    .afterRegistry(Registries.ITEM, () -> WoodSetProperties.init(List.of(this)));
        }
    }

    private SuppliedBlock registerBlock(String path, ExtensibleCodec.Entry<Function<BlockBehaviour.Properties, ? extends Block>> type, Consumer<BlockGenerator.Builder> builder) {
        SuppliedBlock block = blocks.register(path, type, builder);
        registeredBlocks.add(block);
        return block;
    }
    private SuppliedBlock registerBlockWithoutItem(String path, ExtensibleCodec.Entry<Function<BlockBehaviour.Properties, ? extends Block>> type, Consumer<BlockGenerator.Builder> builder) {
        SuppliedBlock block = blocks.registerWithoutItem(path, type, builder);
        registeredBlocks.add(block);
        return block;
    }
    private SuppliedItem registerBlockItem(SuppliedBlock block, ExtensibleCodec.Entry<BiFunction<Block, Item.Properties, Item>> type, Consumer<ItemGenerator.Builder> builder) {
		SuppliedItem item = items.registerBlockItem(block, type, builder);
        registeredItems.add(item);
        return item;
    }

    private SuppliedItem registerItem(String path, ExtensibleCodec.Entry<Function<Item.Properties, Item>> type, Consumer<ItemGenerator.Builder> builder) {
        SuppliedItem item = items.register(path, type, builder);
        registeredItems.add(item);
        return item;
    }

	public Supplier<? extends EntityType<?>> registerEntity(String name, ExtensibleCodec.Entry<EntityType.Builder<?>> type, Consumer<EntityGenerator.Builder> builder) {
		return entities.register(name, type, builder);
	}

    private void createLogProperties(BlockGenerator.Properties properties, MapColor topMapColor, MapColor sideMapColor) {
        properties.mapColor(VanillaMapColorCodecs.BLOCK_ROTATION.create(() -> new VanillaMapColorCodecs.MultiColored(topMapColor, sideMapColor))).strength(2.0F).soundType(this.getSettings().woodSoundType.get());
    }

    public Settings getSettings() {
        return settings;
    }

    public Identifier getId() {
        return id;
    }

    public SuppliedBlock getButton() {
        return button;
    }

    public SuppliedBlock getFence() {
        return fence;
    }

    public SuppliedBlock getPlanks() {
        return planks;
    }

    public SuppliedBlock getSlab() {
        return slab;
    }

    public SuppliedBlock getFenceGate() {
        return fenceGate;
    }

    public SuppliedBlock getStairs() {
        return stairs;
    }

    public SuppliedBlock getDoor() {
        return door;
    }

    public SuppliedBlock getHangingSign() {
        return hangingSign;
    }

    public SuppliedBlock getWallHangingSign() {
        return wallHangingSign;
    }

    public SuppliedBlock getPressurePlate() {
        return pressurePlate;
    }

    public SuppliedBlock getSign() {
        return sign;
    }

    public SuppliedBlock getTrapdoor() {
        return trapdoor;
    }

    public SuppliedBlock getWallSign() {
        return wallSign;
    }

    public SuppliedItem getHangingSignItem() {
        return hangingSignItem;
    }

    public SuppliedItem getSignItem() {
        return signItem;
    }

    public SuppliedBlock getLog() {
        return log;
    }

    public SuppliedBlock getStrippedLog() {
        return strippedLog;
    }

    public @Nullable SuppliedBlock getWood() {
        return wood;
    }

    public @Nullable SuppliedBlock getStrippedWood() {
        return strippedWood;
    }

    public @Nullable SuppliedBlock getMosaic() {
        return mosaic;
    }

    public @Nullable SuppliedBlock getMosaicStairs() {
        return mosaicStairs;
    }

    public @Nullable SuppliedBlock getMosaicSlab() {
        return mosaicSlab;
    }

    public @Nullable SuppliedBlock getLeaves() {
        return getLeavesVariant("");
    }

    public @Nullable SuppliedBlock getLeavesVariant(Leaves leaves) {
        return getLeavesVariant(leaves.getPrefix());
    }
    public @Nullable SuppliedBlock getLeavesVariant(String prefix) {
        if (leaves == null) return null;
        return leaves.get(prefix);
    }

    public Set<Leaves> getAllLeaves() {
        return getSettings().leaves;
    }

    public @Nullable SuppliedBlock getSapling() {
        return sapling;
    }

    public @Nullable SuppliedBlock getPottedSapling() {
        return pottedSapling;
    }

    public @Nullable Supplier<? extends EntityType<?>> getBoat() {
        return boat;
    }

    public @Nullable Supplier<? extends EntityType<?>> getChestBoat() {
        return chestBoat;
    }

    public @Nullable SuppliedItem getBoatItem() {
        return boatItem;
    }

    public @Nullable SuppliedItem getChestBoatItem() {
        return chestBoatItem;
    }

    public List<SuppliedBlock> getRegisteredBlocks() {
        return registeredBlocks;
    }

    public List<SuppliedItem> getRegisteredItems() {
        return registeredItems;
    }

    public SuppliedBlock getShelf() {
        return shelf;
    }

    public BlockFamily getBlockFamily() {
        if (blockFamily == null) {
            blockFamily = new BlockFamily.Builder(planks.get()).recipeGroupPrefix("wooden").recipeUnlockedBy(RecipeProvider.getHasName(getPlanks().get()));
            blockFamily.stairs(stairs.get());
            blockFamily.slab(slab.get());
            if (this.hasMosaic()) {
                blockFamily.customFence(fence.get());
                blockFamily.customFenceGate(fenceGate.get());
            } else {
                blockFamily.fence(fence.get());
                blockFamily.fenceGate(fenceGate.get());
            }
            blockFamily.door(door.get());
            blockFamily.trapdoor(trapdoor.get());
            blockFamily.sign(sign.get(), wallSign.get());
            blockFamily.button(button.get());
            blockFamily.pressurePlate(pressurePlate.get());
        }
        return blockFamily.getFamily();
    }
    private SuppliedBlock createLog() {
        return registerBlock(
                this.getId().getPath() + "_" + settings.getLogName(),
                VanillaBlockCodecs.ROTATED_PILLAR_BLOCK.create(),
                builder -> builder
                        .properties(properties -> {
                            createLogProperties(properties, this.barkColor, this.plankColor);
                            if (settings.isFlammable) properties.flammable(5, 5);
                        })
                        .assets(assets -> assets
                                .model(getSettings().logModel)
                        )
                        .data(data -> data
                                .tag(logTag)
                                .dropSelf()
                        )
                        .itemProperties(item -> {
                            if (settings.isFlammable) item.cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_BLOCKS);
                        })
        );
    }
    private SuppliedBlock createStrippedLog() {
        return registerBlock(
                "stripped_" + this.getId().getPath() + "_" + settings.getLogName(),
                VanillaBlockCodecs.ROTATED_PILLAR_BLOCK.create(),
                builder -> builder
                        .properties(properties -> {
                            createLogProperties(properties, this.barkColor, this.plankColor);
                            if (settings.isFlammable) properties.flammable(5, 5);
                        })
                        .assets(assets -> assets
                                .model(getSettings().logModel)
                        )
                        .data(data -> data
                                .tag(logTag)
                                .dropSelf()
                        )
                        .itemProperties(item -> {
                            if (settings.isFlammable) item.cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_BLOCKS);
                        })
        );
    }
    private SuppliedBlock createWood() {
        return registerBlock(
                this.getId().getPath() + "_" + settings.getWoodName(),
                VanillaBlockCodecs.ROTATED_PILLAR_BLOCK.create(),
                builder -> builder
                        .properties(properties -> {
                            createLogProperties(properties, this.barkColor, this.barkColor);
                            if (settings.isFlammable) properties.flammable(5, 5);
                        })
                        .assets(assets -> assets
                                .model(BlockAssets.WOOD, getLog())
                        )
                        .data(data -> data
                                .tag(logTag)
                                .dropSelf()
                                .recipes((item, provider) -> provider.woodFromLogs(item, getLog()))
                        )
                        .itemProperties(item -> {
                            if (settings.isFlammable) item.cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_BLOCKS);
                        })
        );
    }
    private SuppliedBlock createStrippedWood() {
        return registerBlock(
                "stripped_" + this.getId().getPath() + "_" + settings.getWoodName(),
                VanillaBlockCodecs.ROTATED_PILLAR_BLOCK.create(),
                builder -> builder
                        .properties(properties -> {
                            createLogProperties(properties, this.plankColor, this.plankColor);
                            if (settings.isFlammable) properties.flammable(5, 5);
                        })
                        .assets(assets -> assets
                                .model(BlockAssets.WOOD, getStrippedLog())
                        )
                        .data(data -> data
                                .tag(logTag)
                                .dropSelf()
                                .recipes((item, provider) -> provider.woodFromLogs(item, getStrippedLog()))
                        )
                        .itemProperties(item -> {
                            if (settings.isFlammable) item.cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_BLOCKS);
                        })
        );
    }
    private Map<String, SuppliedBlock> createLeaves() {
        Map<String, SuppliedBlock> leaves = new HashMap<>();
        for (Leaves entry : getAllLeaves()) {
            String name = this.getId().getPath() + "_" + this.settings.getLeavesName();
            if (!entry.getPrefix().isEmpty()) name = entry.getPrefix() + "_" + name;
            SuppliedBlock block = registerBlock(name, entry.type, builder -> builder
                    .assets(assets -> assets
                            .model(entry.model)
                    )
                    .properties(properties -> properties
                            .mapColor(entry.getMapColor())
                            .strength(0.2F)
                            .randomTicks(true)
                            .soundType(settings.leavesSoundType.get())
                            .occlusion(false)
                            .validSpawn(VanillaBlockPredicateCodecs.OCELOT_OR_PARROT.entityPredicate().get().create())
                            .suffocating(VanillaBlockPredicateCodecs.NEVER.statePredicate().get().create())
                            .viewBlocking(VanillaBlockPredicateCodecs.NEVER.collisionPredicate().get().create())
                            .pushReaction(PushReaction.POPPED)
                            .redstoneConductor(VanillaBlockPredicateCodecs.NEVER.statePredicate().get().create())
                            .flammable(30, 60)
                    )
                    .data(data -> data
                            .loot(entry.loot)
                            .tag(BlockItemTags.LEAVES)
                    )
                    .itemProperties(itemProperties -> itemProperties
                            .compostable(ContextIntProviders.COMPOSTABLE_LOW)
                    )
            );
            leaves.put(entry.prefix, block);
        }
        return leaves;
    }
    private SuppliedBlock createSapling() {
        return registerBlock(
                this.getId().getPath() + "_" + this.settings.getSaplingName(),
                this.settings.sapling.getLeft(),
                builder -> builder
                        .assets(assets -> assets
                                .model(BlockAssets.POTTED_PLANT, new BlockAssets.PottedPlant(getPottedSapling(), this.settings.sapling.getRight()))
                        )
                        .properties(properties -> properties
                                .copyFrom(() -> Blocks.OAK_SAPLING)
                                .mapColor(this.settings.sapling.getMiddle())
                        )
                        .data(data -> data
                                .dropSelf()
                                .tag(BlockItemTags.SAPLINGS)
                        )
                        .itemProperties(itemProperties -> {
                            itemProperties.compostable(ContextIntProviders.COMPOSTABLE_LOW);
                            if (settings.isFlammable) itemProperties.cookingFuel(ContextIntProviders.COOKING_TIME_DRY_PLANTS);
                        })
        );
    }
    private SuppliedBlock createPottedSapling(SuppliedBlock sapling) {
        return registerBlockWithoutItem(
                "potted_" + this.getId().getPath() + "_sapling",
                VanillaBlockCodecs.FLOWER_POT.create(() -> getSapling().get()),
                builder -> builder
                        .properties(properties -> properties
                                .instabreak()
                                .occlusion(false)
                                .pushReaction(PushReaction.POPPED)
                        )
                        .data(data -> data
                                .dropSelf()
                                .tag(BlockTags.FLOWER_POTS)
                        )
        );
    }
    private SuppliedBlock createPlanks() {
        return registerBlock(
                this.getId().getPath() + "_planks",
                VanillaBlockCodecs.BLOCK.create(),
                builder -> builder
                        .properties(properties -> {
                            Block base;
                            if (!this.getSettings().isFlammable) base = Blocks.CRIMSON_PLANKS;
                            else base = Blocks.OAK_PLANKS;
                            properties.copyFrom(() -> base);
                            properties.soundType(getSettings().woodSoundType.get());
                            properties.mapColor(this.plankColor);
                            if (settings.isFlammable) properties.flammable(5, 20);
                        })
                        .assets(assets -> assets
                                .simpleCube()
                        )
                        .data(data -> data
                                .tag(BlockItemTags.PLANKS)
                                .dropSelf()
                                .recipes((item, provider) -> provider.planksFromLog(item, logTag.item(), getSettings().planksFromLog))
                        )
                        .itemProperties(item -> {
                            if (settings.isFlammable) item.cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_BLOCKS);
                        })
        );
    }
    private SuppliedBlock createStairs(){
        return registerBlock(
                this.getId().getPath() + "_stairs",
                VanillaBlockCodecs.STAIRS.create(() -> getPlanks().get()),
                builder -> builder
                        .properties(properties -> properties
                                .copyFrom(getPlanks())
                        )
                        .assets(assets -> assets
                                .model(BlockAssets.STAIRS, getPlanks())
                        )
                        .data(data -> data
                                .tag(BlockItemTags.WOODEN_STAIRS)
                                .dropSelf()
                                .recipes((item, provider) -> provider.stairBuilder(item, Ingredient.of(getPlanks()))
                                        .unlockedBy(RecipeProvider.getHasName(getPlanks()), provider.has(getPlanks()))
                                        .save(provider.output)
                                )
                        )
                        .itemProperties(item -> {
                            if (settings.isFlammable) item.cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_BLOCKS);
                        })
        );
    }
    private SuppliedBlock createSlab() {
        return registerBlock(
                this.getId().getPath() + "_slab",
                VanillaBlockCodecs.SLAB.create(),
                builder -> builder
                        .properties(properties -> properties
                                .copyFrom(getPlanks())
                        )
                        .assets(assets -> assets
                                .model(BlockAssets.SLAB, getPlanks())
                        )
                        .data(data -> data
                                .tag(BlockItemTags.WOODEN_SLABS)
                                .dropSelf()
                                .recipes((item, provider) -> provider.slab(RecipeCategory.BUILDING_BLOCKS, item, getPlanks()))
                        )
                        .itemProperties(item -> {
                            if (settings.isFlammable) item.cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_SLABS);
                        })
        );
    }
    private SuppliedBlock createMosaic(){
        return registerBlock(
                this.getId().getPath() + "_mosaic",
                VanillaBlockCodecs.BLOCK.create(),
                builder -> builder
                        .properties(properties -> properties
                                .copyFrom(getPlanks())
                        )
                        .assets(assets -> assets
                                .simpleCube()
                        )
                        .data(data -> data
                                .tag(BlockTags.MINEABLE_WITH_AXE)
                                .dropSelf()
                                .recipes((item, provider) -> provider.mosaicBuilder(RecipeCategory.BUILDING_BLOCKS, item, getSlab()))
                        )
                        .itemProperties(item -> {
                            if (settings.isFlammable) item.cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_BLOCKS);
                        })
        );
    }
    private SuppliedBlock createMosaicStairs(){
        return registerBlock(
                this.getId().getPath() + "_mosaic_stairs",
                VanillaBlockCodecs.STAIRS.create(getMosaic()),
                builder -> builder
                        .properties(properties -> properties
                                .copyFrom(getMosaic())
                        )
                        .assets(assets -> assets
                                .model(BlockAssets.STAIRS, getMosaic())
                        )
                        .data(data -> data
                                .tag(BlockTags.WOODEN_STAIRS)
                                .dropSelf()
                                .recipes((item, provider) -> provider.stairBuilder(item, Ingredient.of(getMosaic()))
                                        .unlockedBy(RecipeProvider.getHasName(getMosaic()), provider.has(getMosaic()))
                                        .save(provider.output)
                                )
                        )
                        .itemProperties(item -> {
                            if (settings.isFlammable) item.cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_BLOCKS);
                        })
        );
    }
    private SuppliedBlock createMosaicSlab(){
        return registerBlock(
                this.getId().getPath() + "_mosaic_slab",
                VanillaBlockCodecs.SLAB.create(),
                builder -> builder
                        .properties(properties -> properties
                                .copyFrom(getMosaic())
                        )
                        .assets(assets -> assets
                                .model(BlockAssets.SLAB, getMosaic())
                        )
                        .data(data -> data
                                .tag(BlockItemTags.WOODEN_SLABS)
                                .dropSelf()
                                .recipes((item, provider) -> provider.slab(RecipeCategory.BUILDING_BLOCKS, item, getMosaic()))
                        )
                        .itemProperties(item -> {
                            if (settings.isFlammable) item.cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_SLABS);
                        })
        );
    }
    private SuppliedBlock createFence(){
        return registerBlock(
                this.getId().getPath() + "_fence",
                VanillaBlockCodecs.FENCE.create(),
                builder -> builder
                        .properties(properties -> properties
                                .copyFrom(getPlanks())
                                .solid(true)
                        )
                        .assets(assets -> assets
                                .model(BlockAssets.FENCE, getPlanks())
                        )
                        .data(data -> data
                                .tag(BlockItemTags.WOODEN_FENCES)
                                .dropSelf()
                                .recipes((item, provider) -> provider.fenceBuilder(item, Ingredient.of(getPlanks()))
                                        .unlockedBy(RecipeProvider.getHasName(getPlanks()), provider.has(getPlanks()))
                                        .save(provider.output)
                                )
                        )
                        .itemProperties(item -> {
                            if (settings.isFlammable) item.cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_BLOCKS);
                        })
        );
    }
    private SuppliedBlock createFenceGate(){
        return registerBlock(
                this.getId().getPath() + "_fence_gate",
                VanillaBlockCodecs.FENCE_GATE.create(getWoodType()),
                builder -> builder
                        .properties(properties -> properties
                                .copyFrom(getPlanks())
                                .solid(true)
                        )
                        .assets(assets -> assets
                                .model(BlockAssets.FENCE_GATE, getPlanks())
                        )
                        .data(data -> data
                                .tag(BlockItemTags.FENCE_GATES)
                                .dropSelf()
                                .recipes((item, provider) -> provider.fenceGateBuilder(item, Ingredient.of(getPlanks()))
                                        .unlockedBy(RecipeProvider.getHasName(getPlanks()), provider.has(getPlanks()))
                                        .save(provider.output)
                                )
                        )
                        .itemProperties(item -> {
                            if (settings.isFlammable) item.cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_BLOCKS);
                        })
        );
    }
    private SuppliedBlock createPressurePlate() {
        return registerBlock(
                this.getId().getPath() + "_pressure_plate",
                VanillaBlockCodecs.PRESSURE_PLATE.create(() -> getWoodType().get().setType()),
                builder -> builder
                        .properties(properties -> properties
                                .copyFrom(getPlanks())
                                .solid(true)
                                .collision(false)
                                .pushReaction(PushReaction.POPPED)
                        )
                        .assets(assets -> assets
                                .model(BlockAssets.PRESSURE_PLATE, getPlanks())
                        )
                        .data(data -> data
                                .tag(BlockItemTags.WOODEN_PRESSURE_PLATES)
                                .dropSelf()
                                .recipes((item, provider) -> provider.pressurePlate(item, getPlanks()))
                        )
                        .itemProperties(item -> {
                            if (settings.isFlammable) item.cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_BLOCKS);
                        })
        );
    }
    private SuppliedBlock createButton(){
        return registerBlock(
                this.getId().getPath() + "_button",
                VanillaBlockCodecs.BUTTON.create(() -> new VanillaBlockCodecs.Button(getWoodType().get().setType(), 30)),
                builder -> builder
                        .properties(properties -> properties
                                .copyFrom(getPlanks())
                                .solid(true)
                                .collision(false)
                                .pushReaction(PushReaction.POPPED)
                                .strength(0.5F)
                        )
                        .assets(assets -> assets
                                .model(BlockAssets.BUTTON, getPlanks())
                        )
                        .data(data -> data
                                .tag(BlockItemTags.WOODEN_BUTTONS)
                                .dropSelf()
                                .recipes((item, provider) -> provider.buttonBuilder(item, Ingredient.of(getPlanks()))
                                        .unlockedBy(RecipeProvider.getHasName(getPlanks()), provider.has(getPlanks()))
                                        .save(provider.output)
                                )
                        )
                        .itemProperties(item -> {
                            if (settings.isFlammable) item.cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_ITEMS_EXTRA_SMALL);
                        })
        );
    }
    private SuppliedBlock createDoor(){
        return registerBlock(
                this.getId().getPath() + "_door",
                VanillaBlockCodecs.DOOR.create(() -> getWoodType().get().setType()),
                builder -> builder
                        .properties(properties -> properties
                                .copyFrom(getPlanks())
                                .occlusion(false)
                                .pushReaction(PushReaction.POPPED)
                                .strength(3F)
                        )
                        .assets(assets -> assets
                                .model(BlockAssets.DOOR)
                        )
                        .data(data -> data
                                .tag(BlockItemTags.WOODEN_DOORS)
                                .loot((block, provider) -> provider.createDoorTable(block))
                                .recipes((item, provider) -> provider.doorBuilder(item, Ingredient.of(getPlanks()))
                                        .unlockedBy(RecipeProvider.getHasName(getPlanks()), provider.has(getPlanks()))
                                        .save(provider.output)
                                )
                        )
                        .itemProperties(item -> {
                            if (settings.isFlammable) item.cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_ITEMS_LARGE);
                        })
        );
    }
    private SuppliedBlock createTrapDoor() {
        return registerBlock(
                this.getId().getPath() + "_trapdoor",
                VanillaBlockCodecs.TRAPDOOR.create(() -> getWoodType().get().setType()),
                builder -> builder
                        .properties(properties -> properties
                                .copyFrom(getPlanks())
                                .occlusion(false)
                                .strength(3F)
                                .validSpawn(VanillaBlockPredicateCodecs.NEVER.entityPredicate().get().create())
                        )
                        .assets(assets -> assets
                                .model(BlockAssets.TRAPDOOR)
                        )
                        .data(data -> data
                                .tag(BlockItemTags.WOODEN_TRAPDOORS)
                                .dropSelf()
                                .recipes((item, provider) -> provider.trapdoorBuilder(item, Ingredient.of(getPlanks()))
                                        .unlockedBy(RecipeProvider.getHasName(getPlanks()), provider.has(getPlanks()))
                                        .save(provider.output)
                                )
                        )
                        .itemProperties(item -> {
                            if (settings.isFlammable) item.cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_ITEMS_LARGE);
                        })
        );
    }
    private SuppliedBlock createSign(){
        return registerBlockWithoutItem(
                this.getId().getPath() + "_sign",
                VanillaBlockCodecs.STANDING_SIGN.create(getWoodType()),
                builder -> builder
                        .properties(properties -> properties
                                .copyFrom(getPlanks())
                                .collision(false)
                                .solid(true)
                                .strength(1F)
                        )
                        .assets(assets -> assets
                                .model(BlockAssets.SIGN, new BlockAssets.Sign(getPlanks(), getWallSign()))
                        )
                        .data(data -> data
                                .tag(BlockItemTags.SIGNS)
                                .tag(BlockTags.STANDING_SIGNS)
                                .dropSelf()
                        )
                        .blockEntity(() -> BlockEntityTypes.SIGN)
        );
    }
    private SuppliedBlock createWallSign(){
        return registerBlockWithoutItem(
                this.getId().getPath() + "_wall_sign",
                VanillaBlockCodecs.WALL_SIGN.create(getWoodType()),
                builder -> builder
                        .properties(properties -> properties
                                .copyFrom(getSign())
                                .lootTable(sign.get().getLootTable().get())
                        )
                        .data(data -> data
                                .tag(BlockTags.WALL_SIGNS)
                                .dropSelf()
                        )
                        .blockEntity(() -> BlockEntityTypes.SIGN)
        );
    }

    private SuppliedBlock createHangingSign() {
        return registerBlockWithoutItem(
                this.getId().getPath() + "_hanging_sign",
                VanillaBlockCodecs.CEILING_HANGING_SIGN.create(getWoodType()),
                builder -> builder
                        .properties(properties -> properties
                                .copyFrom(getPlanks())
                                .collision(false)
                                .solid(true)
                                .strength(1F)
                        )
                        .assets(assets -> assets
                                .model(BlockAssets.HANGING_SIGN, new BlockAssets.HangingSign(getStrippedLog(), getWallHangingSign()))
                        )
                        .data(data -> data
                                .tag(BlockItemTags.HANGING_SIGNS)
                                .tag(BlockTags.CEILING_HANGING_SIGNS)
                                .dropSelf()
                        )
                        .blockEntity(() -> BlockEntityTypes.HANGING_SIGN)
        );
    }
    private SuppliedBlock createWallHangingSign() {
        return registerBlockWithoutItem(
                this.getId().getPath() + "_wall_hanging_sign",
                VanillaBlockCodecs.WALL_HANGING_SIGN.create(getWoodType()),
                builder -> builder
                        .properties(properties -> properties
                                .copyFrom(getHangingSign())
                                .lootTable(hangingSign.get().getLootTable().get())
                        )
                        .data(data -> data
                                .tag(BlockTags.WALL_HANGING_SIGNS)
                                .dropSelf()
                        )
                        .blockEntity(() -> BlockEntityTypes.HANGING_SIGN)
        );
    }

    private SuppliedBlock createShelf() {
        return registerBlock(
                this.getId().getPath() + "_shelf",
                VanillaBlockCodecs.SHELF.create(),
                builder -> builder
                        .properties(properties -> properties
                                .copyFrom(getPlanks())
                                .soundType(SoundType.SHELF)
                                .flammable(30, 20)
                        )
                        .assets(assets -> assets
                                .model(BlockAssets.SHELF, getPlanks())
                        )
                        .data(data -> data
                                .tag(BlockItemTags.WOODEN_SHELVES)
                                .dropSelf()
                                .recipes((item, provider) -> provider.shelf(item, getStrippedLog()))
                        )
                        .itemProperties(itemProperties -> {
                            if (settings.isFlammable) itemProperties.cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_BLOCKS);
                        })
                        .blockEntity(() -> BlockEntityTypes.SHELF)
        );
    }

    private SuppliedItem createSignItem() {
        return registerBlockItem(
                getSign(),
                VanillaItemCodecs.STANDING_AND_WALL_BLOCK_ITEM.create(() -> new VanillaItemCodecs.StandingAndWall(getWallSign().get(), Direction.DOWN)),
                builder -> builder
                        .properties(properties -> {
                            if (settings.isFlammable) properties.cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_ITEMS_LARGE);
                            properties.stacksTo(16);
                        })
                        .data(data -> data
                                .recipes((item, provider) -> provider.signBuilder(item, Ingredient.of(getPlanks()))
                                        .unlockedBy(RecipeProvider.getHasName(getPlanks()), provider.has(getPlanks()))
                                        .save(provider.output)
                                )
                        )
        );
    }
    private SuppliedItem createHangingSignItem() {
        return registerBlockItem(
                getHangingSign(),
                VanillaItemCodecs.HANGING_SIGN_ITEM.create(getWallHangingSign()),
                builder -> builder
                        .properties(properties -> {
                            if (settings.isFlammable) properties.cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_ITEMS_LARGE);
                            properties.stacksTo(16);
                        })
                        .data(data -> data
                                .recipes((item, provider) -> provider.hangingSignBuilder(item, Ingredient.of(getStrippedLog()))
                                        .unlockedBy(RecipeProvider.getHasName(getPlanks()), provider.has(getPlanks()))
                                        .save(provider.output)
                                )
                        )
        );
    }

    private Supplier<? extends EntityType<?>> createBoatEntity(){
        ExtensibleCodec.Entry<EntityType.Builder<?>> type;
        if (getSettings().getBoats() == Boats.RAFTS) type = VanillaEntityCodecs.RAFT.create(() -> () -> getBoatItem().get());
        else type = VanillaEntityCodecs.BOAT.create(() -> () -> getBoatItem().get());
        return registerEntity(
                this.getId().getPath() + "_" + getBoatName(),
                type,
                builder -> builder.data(data -> data.tag(EntityTypeTags.BOAT))
        );
    }
    private Supplier<? extends EntityType<?>> createChestBoatEntity(){
        ExtensibleCodec.Entry<EntityType.Builder<?>> type;
        if (getSettings().getBoats() == Boats.RAFTS) type = VanillaEntityCodecs.CHEST_RAFT.create(() -> () -> getChestBoatItem().get());
        else type = VanillaEntityCodecs.CHEST_BOAT.create(() -> () -> getChestBoatItem().get());
        return registerEntity(
                this.getId().getPath() + "_chest_" + getBoatName(),
                type,
                builder -> builder.data(data -> data.tag(EntityTypeTags.BOAT))
        );
    }
    private SuppliedItem createBoatItem() {
        return registerItem(
                this.getId().getPath() + "_" + getBoatName(),
                VanillaItemCodecs.BOAT.create(getBoat()),
                builder -> builder
                        .properties(properties -> {
                            if (settings.isFlammable) properties.cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_ITEMS_LARGE);
                        })
                        .assets(assets -> assets
                                .generated()
                        )
                        .data(data -> data
                                .recipes((item, provider) -> provider.woodenBoat(item, getPlanks()))
                                .tag(ItemTags.BOATS)
                        )
        );
    }
    private SuppliedItem createChestBoatItem() {
        return registerItem(
                this.getId().getPath() + "_chest_" + getBoatName(),
                VanillaItemCodecs.BOAT.create(getChestBoat()),
                builder -> builder
                        .properties(properties -> {
                            if (settings.isFlammable) properties.cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_ITEMS_LARGE);
                        })
                        .assets(assets -> assets
                                .generated()
                        )
                        .data(data -> data
                                .recipes((item, provider) -> provider.chestBoat(item, getBoatItem()))
                                .tag(ItemTags.CHEST_BOATS)
                        )
        );
    }

    public boolean hasSingleLeaves(){
        return hasAnyLeaves() && !hasColoredLeaves();
    }
    public boolean hasAnyLeaves(){
        return this.getSettings().leaves != null;
    }
    public boolean hasColoredLeaves(){
        return getSettings().leaves.size() > 1;
    }
    public boolean hasSapling(){
        return this.getSettings().sapling != null;
    }
    public boolean hasWood(){
        return this.getSettings().hasWood;
    }
    public boolean hasMosaic() {
        return this.getSettings().hasMosaic;
    }
    public boolean hasBoats() {
        return this.getSettings().boats != Boats.NONE;
    }

    private String getBoatName(){
        return this.getSettings().getBoats() == Boats.RAFTS ? "raft" : "boat";
    }

    public Supplier<WoodType> getWoodType() {
        if (this.woodType == null) {
            this.woodType = () -> new WoodType(
                    id.toString(),
                    new BlockSetType(
                            id.toString(),
                            this.settings.doorOpening.getFirst(),
                            this.settings.doorOpening.getSecond(),
                            this.settings.canArrowsActivateButton,
                            this.settings.pressurePlateSensitivity,
                            this.settings.woodSoundType.get(),
                            this.settings.doorSounds.getSecond().get(),
                            this.settings.doorSounds.getFirst().get(),
                            this.settings.trapdoorSounds.getSecond().get(),
                            this.settings.trapdoorSounds.getFirst().get(),
                            this.settings.pressurePlateSounds.getSecond().get(),
                            this.settings.pressurePlateSounds.getFirst().get(),
                            this.settings.buttonSounds.getSecond().get(),
                            this.settings.buttonSounds.getFirst().get()
                    ),
                    this.settings.woodSoundType.get(),
                    this.settings.hangingSignSoundType.get(),
                    this.settings.fenceGateSounds.getSecond().get(),
                    this.settings.fenceGateSounds.getFirst().get()
            );
        }
        return this.woodType;
    }

    public enum Boats {
        BOATS,
        RAFTS,
        NONE
    }

    public static class Settings implements Cloneable {
        private String logName = "log";
        private String woodName = "wood";
        private String saplingName = "sapling";
        private String leavesName = "leaves";
        private Set<Leaves> leaves = new HashSet<>();
        private @Nullable Triple<ExtensibleCodec.Entry<Function<BlockBehaviour.Properties, ? extends Block>>, MapColor, BlockAssets.PlantType> sapling = null;
        private Boats boats = Boats.BOATS;

        private boolean hasMosaic = false;
        private boolean hasWood = true;
        private boolean isFlammable = true;

        private Pair<Boolean, Boolean> doorOpening = Pair.of(true, true);
        private boolean canArrowsActivateButton = true;
        private BlockSetType.PressurePlateSensitivity pressurePlateSensitivity = BlockSetType.PressurePlateSensitivity.EVERYTHING;

        private Supplier<SoundType> leavesSoundType = () -> SoundType.GRASS;
        private Supplier<SoundType> woodSoundType = () -> SoundType.WOOD;
        private Supplier<SoundType> hangingSignSoundType = () -> SoundType.HANGING_SIGN;
        private Pair<Supplier<SoundEvent>, Supplier<SoundEvent>> buttonSounds = Pair.of(() -> SoundEvents.WOODEN_BUTTON_CLICK_ON, () -> SoundEvents.WOODEN_BUTTON_CLICK_OFF);
        private Pair<Supplier<SoundEvent>, Supplier<SoundEvent>> pressurePlateSounds = Pair.of(() -> SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_ON, () -> SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_OFF);
        private Pair<Supplier<SoundEvent>, Supplier<SoundEvent>> doorSounds = Pair.of(() -> SoundEvents.WOODEN_DOOR_OPEN, () -> SoundEvents.WOODEN_DOOR_CLOSE);
        private Pair<Supplier<SoundEvent>, Supplier<SoundEvent>> trapdoorSounds = Pair.of(() -> SoundEvents.WOODEN_TRAPDOOR_OPEN, () -> SoundEvents.WOODEN_TRAPDOOR_CLOSE);
        private Pair<Supplier<SoundEvent>, Supplier<SoundEvent>> fenceGateSounds = Pair.of(() -> SoundEvents.FENCE_GATE_OPEN, () -> SoundEvents.FENCE_GATE_CLOSE);

        private @Nullable PrecedingCreativeEntries precedingCreativeEntries = null;

        private boolean isOverworld = true;
        private BiConsumer<Item, RecipeProvider> logRecipe = (_, _) -> {};
        private BlockAsset<Void> logModel = BlockAssets.LOG;
        private int planksFromLog = 4;

        Settings() {}

        public boolean isFlammable() {
            return isFlammable;
        }

        public Boats getBoats() {
            return boats;
        }

        public Supplier<SoundType> getLeavesSoundType() {
            return leavesSoundType;
        }
        public Supplier<SoundType> getWoodSoundType() {
            return woodSoundType;
        }
        public Supplier<SoundType> getHangingSignSoundType() {
            return hangingSignSoundType;
        }
        public Pair<Supplier<SoundEvent>, Supplier<SoundEvent>> getButtonSounds() {
            return buttonSounds;
        }
        public Pair<Supplier<SoundEvent>, Supplier<SoundEvent>> getPressurePlateSounds() {
            return pressurePlateSounds;
        }
        public Pair<Supplier<SoundEvent>, Supplier<SoundEvent>> getDoorSounds() {
            return doorSounds;
        }
        public Pair<Supplier<SoundEvent>, Supplier<SoundEvent>> getTrapdoorSounds() {
            return trapdoorSounds;
        }
        public Pair<Supplier<SoundEvent>, Supplier<SoundEvent>> getFenceGateSounds() {
            return fenceGateSounds;
        }

        public Pair<Boolean, Boolean> getDoorOpening() {
            return doorOpening;
        }
        public boolean canArrowsActivateButton() {
            return canArrowsActivateButton;
        }
        public BlockSetType.PressurePlateSensitivity getPressurePlateSensitivity() {
            return pressurePlateSensitivity;
        }

        public String getLogName() {
            return logName;
        }
        public String getWoodName() {
            return woodName;
        }
        public String getSaplingName() {
            return saplingName;
        }
        public String getLeavesName() {
            return leavesName;
        }

        public Settings copy() {
            try {
                return (Settings) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new AssertionError(e);
            }
        }
    }

    public static class RegistryBuilder extends Builder<RegistryBuilder> {

        private final Identifier id;
        private final BlockItemTagId logTag;
        private final MapColor barkColor;
        private final MapColor plankColor;

        private final BlockGenerator blocks;
        private final ItemGenerator items;
        private final EntityGenerator entities;

        private final BlockSetTypeGenerator blockSetTypes;
        private final WoodTypeGenerator woodTypes;

        private final TagGenerator tags;
        private final CreativeEntryGenerator creativeEntries;
        private final BlockConversionGenerator blockConversions;

        public RegistryBuilder createLeaves(Leaves... leaves) {
            Set<Leaves> set = new HashSet<>(List.of(leaves));
            set.forEach(entry -> {
                if (entry.precedingCreativeItem != null) WoodSetProperties.LEAVES_CREATIVE_ENTRIES.put(id, entry.precedingCreativeItem);
            });
            settings.leaves = set;
            return self();
        }

        public RegistryBuilder createSapling(ExtensibleCodec.Entry<Function<BlockBehaviour.Properties, ? extends Block>> type, MapColor mapColor, BlockAssets.PlantType plantType) {
            settings.sapling = Triple.of(type, mapColor, plantType);
            return self();
        }
        public RegistryBuilder createSapling(ExtensibleCodec.Entry<Function<BlockBehaviour.Properties, ? extends Block>> type, MapColor mapColor, BlockAssets.PlantType plantType, Supplier<? extends ItemLike> precedingCreativeSapling) {
            WoodSetProperties.SAPLING_CREATIVE_ENTRIES.put(id, precedingCreativeSapling);
            return createSapling(type, mapColor, plantType);
        }

        public WoodSet build() {
            return new WoodSet(id, logTag, barkColor, plankColor, settings, blocks, items, entities, blockSetTypes, woodTypes, tags, creativeEntries, blockConversions);
        }

        public RegistryBuilder(Identifier id, BlockItemTagId logTag, MapColor barkColor, MapColor plankColor, WoodPreset preset, BlockGenerator blocks, ItemGenerator items, EntityGenerator entities, BlockSetTypeGenerator blockSetTypes, WoodTypeGenerator woodTypes, TagGenerator tags, CreativeEntryGenerator creativeEntries, BlockConversionGenerator blockConversions) {
            super(preset.settings.copy());

            this.id = id;
            this.logTag = logTag;
            this.barkColor = barkColor;
            this.plankColor = plankColor;
            this.blocks = blocks;
            this.items = items;
            this.entities = entities;
            this.blockSetTypes = blockSetTypes;
            this.woodTypes = woodTypes;
            this.tags = tags;
            this.creativeEntries = creativeEntries;
            this.blockConversions = blockConversions;
        }
    }

    public static class PresetBuilder extends WoodSet.Builder<PresetBuilder> {

        public PresetBuilder() {
            super();
        }

        public PresetBuilder(Settings settings) {
            super(settings);
        }

        public WoodPreset build() {
            return new WoodPreset(settings.copy());
        }
    }

    public static class Builder<T extends Builder<T>> {

        protected final Settings settings;

        public Builder() {
            this.settings = new Settings();
        }

        protected Builder(Settings settings) {
            this.settings = settings;
        }

        @SuppressWarnings("unchecked")
        protected T self() {
            return (T) this;
        }

        public T creativeInventoryPlacement(
                Supplier<? extends ItemLike> precedingBuildingItem,
                Supplier<? extends ItemLike> precedingNaturalItem,
                Supplier<? extends ItemLike> precedingFunctionalShelfItem,
                Supplier<? extends ItemLike> precedingFunctionalSignItem
        ) {
            settings.precedingCreativeEntries = new PrecedingCreativeEntries(
                    precedingBuildingItem,
                    precedingNaturalItem,
                    precedingFunctionalShelfItem,
                    precedingFunctionalSignItem,
                    null
            );
            return self();
        }

        public T creativeInventoryPlacement(
                Supplier<? extends ItemLike> precedingBuildingItem,
                Supplier<? extends ItemLike> precedingNaturalItem,
                Supplier<? extends ItemLike> precedingFunctionalShelfItem,
                Supplier<? extends ItemLike> precedingFunctionalSignItem,
                Supplier<? extends ItemLike> precedingUtilitiesItem
        ) {
            settings.precedingCreativeEntries = new PrecedingCreativeEntries(
                    precedingBuildingItem,
                    precedingNaturalItem,
                    precedingFunctionalShelfItem,
                    precedingFunctionalSignItem,
                    precedingUtilitiesItem
            );
            return self();
        }

        public T setLeavesSoundType(Supplier<SoundType> leavesSoundType) {
            settings.leavesSoundType = leavesSoundType;
            return self();
        }

        public T setWoodSoundType(Supplier<SoundType> woodSoundType) {
            settings.woodSoundType = woodSoundType;
            return self();
        }

        public T setHangingSignSoundType(Supplier<SoundType> hangingSignSoundType) {
            settings.hangingSignSoundType = hangingSignSoundType;
            return self();
        }

        public T setButtonSounds(Supplier<SoundEvent> on, Supplier<SoundEvent> off) {
            settings.buttonSounds = Pair.of(on, off);
            return self();
        }

        public T setPressurePlateSounds(Supplier<SoundEvent> on, Supplier<SoundEvent> off) {
            settings.pressurePlateSounds = Pair.of(on, off);
            return self();
        }

        public T setDoorSounds(Supplier<SoundEvent> open, Supplier<SoundEvent> close) {
            settings.doorSounds = Pair.of(open, close);
            return self();
        }

        public T setTrapdoorSounds(Supplier<SoundEvent> open, Supplier<SoundEvent> close) {
            settings.trapdoorSounds = Pair.of(open, close);
            return self();
        }

        public T setFenceGateSounds(Supplier<SoundEvent> open, Supplier<SoundEvent> close) {
            settings.fenceGateSounds = Pair.of(open, close);
            return self();
        }

        public T setWoodName(String woodName) {
            settings.woodName = woodName;
            return self();
        }

        public T setLogName(String logName) {
            settings.logName = logName;
            return self();
        }

        public T setSaplingName(String saplingName) {
            settings.saplingName = saplingName;
            return self();
        }

        public T setLeavesName(String leavesName) {
            settings.leavesName = leavesName;
            return self();
        }

        public T setBoats(Boats boats) {
            settings.boats = boats;
            return self();
        }

        public T hasMosaic(boolean hasMosaic) {
            settings.hasMosaic = hasMosaic;
            return self();
        }

        public T isFlammable(boolean isFlammable) {
            settings.isFlammable = isFlammable;
            return self();
        }

        public T hasWood(boolean hasWood) {
            settings.hasWood = hasWood;
            return self();
        }

        public T setDoorOpening(boolean canOpenByHand, boolean canOpenByWindCharge) {
            settings.doorOpening = Pair.of(canOpenByHand, canOpenByWindCharge);
            return self();
        }

        public T canArrowsActivateButton(boolean canArrowsActivateButton) {
            settings.canArrowsActivateButton = canArrowsActivateButton;
            return self();
        }

        public T setPressurePlateSensitivity(BlockSetType.PressurePlateSensitivity pressurePlateSensitivity) {
            settings.pressurePlateSensitivity = pressurePlateSensitivity;
            return self();
        }

        public T isOverworld(boolean isOverworld) {
            settings.isOverworld = isOverworld;
            return self();
        }

        public T logRecipe(BiConsumer<Item, RecipeProvider> consumer) {
            settings.logRecipe = consumer;
            return self();
        }

        public T logModel(BlockAsset<Void> logModel) {
            settings.logModel = logModel;
            return self();
        }

        public T planksFromLog(int planksFromLog) {
            settings.planksFromLog = planksFromLog;
            return self();
        }
    }

    public static class Leaves {

        private final String prefix;
        private final ExtensibleCodec.Entry<Function<BlockBehaviour.Properties, ? extends Block>> type;
        private final MapColor mapColor;
        private final BlockAsset<Void> model;
        private final BiFunction<Block, BlockLootProvider, LootTable.Builder> loot;
        private final @Nullable Either<String, Supplier<? extends ItemLike>> precedingCreativeItem;

        public Leaves base(ExtensibleCodec.Entry<Function<BlockBehaviour.Properties, ? extends Block>> type, MapColor mapColor, BlockAsset<Void> model, BiFunction<Block, BlockLootProvider, LootTable.Builder> loot) {
            return variant("",  type, mapColor, model, loot);
        }
        public Leaves base(ExtensibleCodec.Entry<Function<BlockBehaviour.Properties, ? extends Block>> type, MapColor mapColor, BlockAsset<Void> model, BiFunction<Block, BlockLootProvider, LootTable.Builder> loot, Supplier<? extends ItemLike> precedingCreativeItem) {
            return variant("", type, mapColor, model, loot, precedingCreativeItem);
        }

        public Leaves variant(String prefix, ExtensibleCodec.Entry<Function<BlockBehaviour.Properties, ? extends Block>> type, MapColor mapColor, BlockAsset<Void> model, BiFunction<Block, BlockLootProvider, LootTable.Builder> loot) {
            return new Leaves(prefix, type, mapColor,  model, loot, null);
        }
        public Leaves variant(String prefix, ExtensibleCodec.Entry<Function<BlockBehaviour.Properties, ? extends Block>> type, MapColor mapColor, BlockAsset<Void> model, BiFunction<Block, BlockLootProvider, LootTable.Builder> loot, String precedingLeavesVariant) {
            return new Leaves(prefix, type, mapColor, model, loot, Either.left(precedingLeavesVariant));
        }
        public Leaves variant(String prefix, ExtensibleCodec.Entry<Function<BlockBehaviour.Properties, ? extends Block>> type, MapColor mapColor, BlockAsset<Void> model, BiFunction<Block, BlockLootProvider, LootTable.Builder> loot, Supplier<? extends ItemLike> precedingCreativeItem) {
            return new Leaves(prefix, type, mapColor, model, loot, Either.right(precedingCreativeItem));
        }

        private Leaves(String prefix, ExtensibleCodec.Entry<Function<BlockBehaviour.Properties, ? extends Block>> type, MapColor mapColor, BlockAsset<Void> model, BiFunction<Block, BlockLootProvider, LootTable.Builder> loot, @Nullable Either<String, Supplier<? extends ItemLike>> precedingCreativeItem) {
            this.prefix = prefix;
            this.type = type;
            this.mapColor = mapColor;
            this.model = model;
            this.loot = loot;
            this.precedingCreativeItem = precedingCreativeItem;
        }

        public String getPrefix() {
            return prefix;
        }

        public ExtensibleCodec.Entry<Function<BlockBehaviour.Properties, ? extends Block>> getType() {
            return type;
        }

        public MapColor getMapColor() {
            return mapColor;
        }
    }

    public record PrecedingCreativeEntries(Supplier<? extends ItemLike> building, Supplier<? extends ItemLike> natural, Supplier<? extends ItemLike> functionalShelf, Supplier<? extends ItemLike> functionalSign, @Nullable Supplier<? extends ItemLike> utilities) {}
}
