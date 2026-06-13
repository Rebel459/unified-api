package net.rebel459.unified.util.registry.builder;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.rebel459.unified.platform.UnifiedRegistries;
import net.rebel459.unified.util.fabric.WoodTypeBuilder;
import net.rebel459.unified.util.registry.SuppliedBlock;
import net.rebel459.unified.util.registry.SuppliedItem;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public class Woodset {

    public static final List<Woodset> WOODSETS = new ArrayList<>();
    public static final HashMap<Woodset, PrecedingCreativeEntries> WOODSET_CREATIVE_ENTRIES = new HashMap<>();

    private final List<SuppliedBlock> registeredBlocksList = new ArrayList<>();
    private final List<SuppliedItem> registeredItemsList = new ArrayList<>();

    private static final List<SuppliedBlock> signBlocks = new ArrayList<>();
    private static final List<SuppliedBlock> hangingSignBlocks = new ArrayList<>();

    private final Identifier name;
    private final MapColor barkColor;
    private final MapColor plankColor;
    private BlockSetType blockSetType;
    private WoodType woodType;
    private SoundType leaveSounds;

    private final UnifiedRegistries.Items itemRegistry;
    private final UnifiedRegistries.Blocks blockRegistry;
    private final UnifiedRegistries.EntityTypes entityRegistry;

    private SuppliedBlock log;
    private SuppliedBlock strippedLog;
    private @Nullable SuppliedBlock wood;
    private @Nullable SuppliedBlock strippedWood;
    private @Nullable SuppliedBlock leaves = null;
    private @Nullable SuppliedBlock sapling = null;
    private @Nullable SuppliedBlock pottedSapling = null;
    private SuppliedBlock planks;
    private SuppliedBlock stairs;
    private SuppliedBlock slab;
    private SuppliedBlock mosaic;
    private SuppliedBlock mosaicStairs;
    private SuppliedBlock mosaicSlab;
    private SuppliedBlock fence;
    private SuppliedBlock fenceGate;
    private SuppliedBlock pressurePlate;
    private SuppliedBlock button;
    private SuppliedBlock door;
    private SuppliedBlock trapDoor;
    private SuppliedBlock sign;
    private SuppliedBlock wallSign;
    private SuppliedBlock hangingSign;
    private SuppliedBlock wallHangingSign;
    private SuppliedBlock shelf;

    private SuppliedItem signItem;
    private SuppliedItem hangingSignItem;
    private @Nullable SuppliedItem boatItem;
    private @Nullable SuppliedItem chestBoatItem;

    private @Nullable Supplier<EntityType<Boat>> boat;
    private @Nullable Supplier<EntityType<ChestBoat>> chestBoat;

    private BlockFamily.Builder blockFamily = null;
    private final Settings woodsetSettings;

    private void registerWood() {
        blockSetType = createBlockSetType();
        woodType = WoodTypeBuilder.copyOf(woodsetSettings.woodPreset.getWoodType()).register(this.getId(), getBlockSetType());
        planks = createPlanks();

        log = createLog();
        strippedLog = createStrippedLog();
		if (this.hasWood()) {
			wood = createWood();
			strippedWood = createStrippedWood();
		}

        if (hasMosaic()){
            mosaic = createMosaic();
            mosaicStairs = createMosaicStairs();
            mosaicSlab = createMosaicSlab();
        }
        if (this.woodsetSettings.leaf != null){
            leaves = createLeaves();
        }
        if (this.woodsetSettings.sapling != null){
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
        trapDoor = createTrapDoor();
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

        signBlocks.add(sign);
        signBlocks.add(wallSign);

        hangingSignBlocks.add(hangingSign);
        hangingSignBlocks.add(wallHangingSign);
    }

    public Woodset(Identifier name, MapColor sideColor, MapColor plankColor, Settings settings, UnifiedRegistries.Items itemRegistry, UnifiedRegistries.Blocks blockRegistry, UnifiedRegistries.EntityTypes entityRegistry){
        this.woodsetSettings = settings;
        this.name = name;
        this.barkColor = sideColor;
        this.plankColor = plankColor;
        this.itemRegistry = itemRegistry;
        this.blockRegistry = blockRegistry;
        this.entityRegistry = entityRegistry;
        registerWood();
        WOODSETS.add(this);
        WOODSET_CREATIVE_ENTRIES.put(this, this.getWoodsetSettings().creativeEntries);
    }

    private ResourceKey<Item> itemKey(String id) {
        return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(this.getId().getNamespace(), id));
    }
    private ResourceKey<Block> blockKey(String id) {
        return ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(this.getId().getNamespace(), id));
    }
    private SuppliedBlock createBlockWithItem(String blockID, Supplier<BlockBehaviour.Properties> settings){
        return createBlockWithItem(blockID, Block::new, settings);
    }
	private SuppliedBlock createBlockWithItem(String blockID, Function<BlockBehaviour.Properties, Block> factory, Supplier<BlockBehaviour.Properties> settings){
		SuppliedBlock block = blockRegistry.register(blockID, factory, settings);
		registeredBlocksList.add(block);
		return block;
	}
	private SuppliedBlock createBlockWithItem(String blockID, Function<BlockBehaviour.Properties, Block> factory, Supplier<BlockBehaviour.Properties> settings, BlockEntityType<?> blockEntity){
		SuppliedBlock block = blockRegistry.register(blockID, factory, settings, blockEntity);
		registeredBlocksList.add(block);
		return block;
	}
	private SuppliedBlock createBlockWithoutItem(String blockID, Function<BlockBehaviour.Properties, Block> factory, Supplier<BlockBehaviour.Properties> settings){
		SuppliedBlock block = blockRegistry.registerWithoutItem(blockID, factory, settings);
		registeredBlocksList.add(block);
		return block;
	}
	private SuppliedBlock createBlockWithoutItem(String blockID, Function<BlockBehaviour.Properties, Block> factory, Supplier<BlockBehaviour.Properties> settings, BlockEntityType<?> blockEntity){
		SuppliedBlock block = blockRegistry.registerWithoutItem(blockID, factory, settings, blockEntity);
		registeredBlocksList.add(block);
		return block;
	}
    private SuppliedItem createItem(String blockID, Function<Item.Properties, Item> factory, Supplier<Item.Properties> settings){
		SuppliedItem item = itemRegistry.register(blockID, factory, settings);
        registeredItemsList.add(item);
        return item;
    }
    private SuppliedItem createBlockItem(String blockID, Supplier<Block> block, Supplier<Item.Properties> settings){
		SuppliedItem item = itemRegistry.registerBlockItem(blockID, block, settings);
		registeredItemsList.add(item);
		return item;
	}
    private ResourceKey<EntityType<?>> entityKey(String id) {
        return ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(this.getId().getNamespace(), id));
    }

	public <T extends Entity> Supplier<EntityType<T>> register(String name, EntityType.Builder<T> type){
		return entityRegistry.register(name, type);
	}

    private Supplier<BlockBehaviour.Properties> createLogBlock(MapColor topMapColor, MapColor sideMapColor) {
        return () -> BlockBehaviour.Properties.of().mapColor(state -> state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? topMapColor : sideMapColor).strength(2.0F).sound(this.getWoodType().soundType());
    }

    public Settings getWoodsetSettings() {
        return woodsetSettings;
    }

    public static List<WoodType> getAllWoodTypes(){
        if (WOODSETS.isEmpty()){
            return null;
        }
        List<WoodType> types = new ArrayList<>();
        for (Woodset set : WOODSETS){
            types.add(set.getWoodType());
        }
        return types;
    }
    public Identifier getId() {
        return name;
    }

    public BlockSetType getBlockSetType() {
        return blockSetType;
    }

    public WoodPreset getWoodPreset() {
        return woodsetSettings.woodPreset;
    }

    public MapColor getBarkColor() {
        return barkColor;
    }

    public MapColor getPlankColor() {
        return plankColor;
    }

    public WoodType getWoodType() {
        return woodType;
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

    public SuppliedBlock getTrapDoor() {
        return trapDoor;
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
        return leaves;
    }

    public @Nullable SuppliedBlock getSapling() {
        return sapling;
    }

    public @Nullable SuppliedBlock getPottedSapling() {
        return pottedSapling;
    }

    public @Nullable Supplier<EntityType<Boat>> getBoat() {
        return boat;
    }

    public @Nullable Supplier<EntityType<ChestBoat>> getChestBoat() {
        return chestBoat;
    }

    public @Nullable SuppliedItem getBoatItem() {
        return boatItem;
    }

    public @Nullable SuppliedItem getChestBoatItem() {
        return chestBoatItem;
    }

    public List<SuppliedBlock> getRegisteredBlocksList() {
        return registeredBlocksList;
    }

    public List<SuppliedItem> getRegisteredItemsList() {
        return registeredItemsList;
    }

    public static List<SuppliedBlock> getAllSigns(){
        return signBlocks;
    }
    public static List<SuppliedBlock> getAllHangingSigns(){
        return hangingSignBlocks;
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
            blockFamily.trapdoor(trapDoor.get());
            blockFamily.sign(sign.get(), wallSign.get());
            blockFamily.button(button.get());
            blockFamily.pressurePlate(pressurePlate.get());
        }
        return blockFamily.getFamily();
    }
    private SuppliedBlock createLog() {
        return createBlockWithItem(this.getId().getPath() + "_" + woodsetSettings.getLogName(), RotatedPillarBlock::new, createLogBlock(this.getBarkColor(), this.getPlankColor()));
    }
    private SuppliedBlock createStrippedLog() {
        return createBlockWithItem("stripped_" + this.getId().getPath() + "_" + woodsetSettings.getLogName(), RotatedPillarBlock::new, createLogBlock(this.getBarkColor(), this.getPlankColor()));
    }
    private SuppliedBlock createWood() {
        return createBlockWithItem(this.getId().getPath() + "_" +woodsetSettings.getWoodName(), RotatedPillarBlock::new, createLogBlock(this.getBarkColor(), this.getBarkColor()));
    }
    private SuppliedBlock createStrippedWood() {
        return createBlockWithItem("stripped_" + this.getId().getPath() + "_" +woodsetSettings.getWoodName(), RotatedPillarBlock::new, createLogBlock(this.getPlankColor(), this.getPlankColor()));
    }
    private SuppliedBlock createLeaves() {
        Function<BlockBehaviour.Properties, Block> properties = this.woodsetSettings.leaf.getFirst();
        return createBlockWithItem(this.getId().getPath() + "_leaves", properties, createLeavesBlock(this.woodsetSettings.leaf.getSecond()));
    }
    private SuppliedBlock createSapling() {
        Function<BlockBehaviour.Properties, Block> properties = this.woodsetSettings.sapling.getFirst();
        return createBlockWithItem(this.getId().getPath() + "_sapling", properties, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING).mapColor(this.woodsetSettings.sapling.getSecond()));
    }
    private SuppliedBlock createPottedSapling(SuppliedBlock sapling) {
        return createBlockWithoutItem("potted_" + this.getId().getPath() + "_sapling", properties -> new FlowerPotBlock(sapling.get(), properties), Blocks::flowerPotProperties);
    }
    private SuppliedBlock createPlanks(){
        return createBlockWithItem(this.getId().getPath() + "_planks", () -> BlockBehaviour.Properties.ofFullCopy(getBase()).sound(getBlockSetType().soundType()).mapColor(getPlankColor()));
    }
    private SuppliedBlock createStairs(){
        return createBlockWithItem(this.getId().getPath() + "_stairs", settings -> new StairBlock(getBase().defaultBlockState(), settings), () -> BlockBehaviour.Properties.ofFullCopy(getBase()).sound(getBlockSetType().soundType()).mapColor(getPlankColor()));
    }
    private SuppliedBlock createSlab(){
        return createBlockWithItem(this.getId().getPath() + "_slab", SlabBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(getBase()).sound(getBlockSetType().soundType()).mapColor(getPlankColor()));
    }
    private SuppliedBlock createMosaic(){
        return createBlockWithItem(this.getId().getPath() + "_mosaic", () -> BlockBehaviour.Properties.ofFullCopy(getBase()).sound(getBlockSetType().soundType()).mapColor(getPlankColor()));
    }
    private SuppliedBlock createMosaicStairs(){
        return createBlockWithItem(this.getId().getPath() + "_mosaic_stairs", settings -> new StairBlock(getBase().defaultBlockState(), settings), () -> BlockBehaviour.Properties.ofFullCopy(getBase()).sound(getBlockSetType().soundType()).mapColor(getPlankColor()));
    }
    private SuppliedBlock createMosaicSlab(){
        return createBlockWithItem(this.getId().getPath() + "_mosaic_slab", SlabBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(getBase()).sound(getBlockSetType().soundType()).mapColor(getPlankColor()));
    }
    private SuppliedBlock createFence(){
        return createBlockWithItem(this.getId().getPath() + "_fence", FenceBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(getBase()).sound(getBlockSetType().soundType()).mapColor(getPlankColor()));
    }
    private SuppliedBlock createFenceGate(){
        return createBlockWithItem(this.getId().getPath() + "_fence_gate", settings -> new FenceGateBlock(this.getWoodType(), settings), () -> BlockBehaviour.Properties.ofFullCopy(getBase()).sound(getBlockSetType().soundType()).mapColor(getPlankColor()));
    }
    private SuppliedBlock createPressurePlate(){
        return createBlockWithItem(this.getId().getPath() + "_pressure_plate", settings -> new PressurePlateBlock(this.getBlockSetType(), settings), () -> BlockBehaviour.Properties.ofFullCopy(getBase()).sound(getBlockSetType().soundType()).mapColor(getPlankColor()));
    }
    private SuppliedBlock createButton(){
        return createBlockWithItem(this.getId().getPath() + "_button", settings -> new ButtonBlock(this.getBlockSetType(), 30, settings), () -> BlockBehaviour.Properties.ofFullCopy(getBase()).sound(getBlockSetType().soundType()).mapColor(getPlankColor()));
    }
    private SuppliedBlock createDoor(){
        return createBlockWithItem(this.getId().getPath() + "_door", settings -> new DoorBlock(this.getBlockSetType(), settings), () -> BlockBehaviour.Properties.ofFullCopy(getBase()).sound(getBlockSetType().soundType()).mapColor(getPlankColor()).noOcclusion());
    }
    private SuppliedBlock createTrapDoor(){
        return createBlockWithItem(this.getId().getPath() + "_trapdoor", settings -> new TrapDoorBlock(this.getBlockSetType(), settings), () -> BlockBehaviour.Properties.ofFullCopy(getBase()).sound(getBlockSetType().soundType()).mapColor(getPlankColor()).noOcclusion());
    }
    private SuppliedBlock createSign(){
        return createBlockWithoutItem(this.getId().getPath() + "_sign", settings -> new StandingSignBlock(
                        this.woodType, settings),
			() -> BlockBehaviour.Properties.ofFullCopy(getSignBase()).mapColor(this.getPlankColor()),
			BlockEntityType.SIGN
		);
    }
    private SuppliedBlock createWallSign(){
        return createBlockWithoutItem(this.getId().getPath() + "_wall_sign", settings -> new WallSignBlock(
                        this.woodType, settings),
			() -> BlockBehaviour.Properties.ofFullCopy(getSignBase()).mapColor(this.getPlankColor()).overrideLootTable(sign.get().getLootTable()),
			BlockEntityType.SIGN
		);
    }

    private SuppliedBlock createHangingSign(){
        return createBlockWithoutItem(this.getId().getPath() + "_hanging_sign", settings -> new CeilingHangingSignBlock(
                        this.woodType, settings),
			() -> BlockBehaviour.Properties.ofFullCopy(getHangingSignBase()).mapColor(this.getPlankColor()),
			BlockEntityType.HANGING_SIGN
		);
    }
    private SuppliedBlock createWallHangingSign(){
        return createBlockWithoutItem(this.getId().getPath() + "_wall_hanging_sign", settings -> new WallHangingSignBlock(
                        this.woodType, settings),
			() -> BlockBehaviour.Properties.ofFullCopy(getHangingSignBase()).mapColor(this.getPlankColor()).overrideLootTable(hangingSign.get().getLootTable()),
			BlockEntityType.HANGING_SIGN
		);
    }

    private SuppliedBlock createShelf(){
        return createBlockWithItem(this.getId().getPath() + "_shelf", ShelfBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.CHERRY_SHELF).mapColor(plankColor), BlockEntityType.SHELF);
    }

    private SuppliedItem createSignItem(){
        return createItem(this.getId().getPath() + "_sign", settings -> new SignItem(this.getSign().get(), this.getWallSign().get(), settings), () -> new Item.Properties().stacksTo(16));
    }
    private SuppliedItem createHangingSignItem(){
        return createItem(this.getId().getPath() + "_hanging_sign", settings -> new HangingSignItem(this.getHangingSign().get(), this.getWallHangingSign().get(), settings), () -> new Item.Properties().stacksTo(16));
    }

    private Supplier<EntityType<Boat>> createBoatEntity(){
        return register(this.getId().getPath() + "_" + woodsetSettings.getBoatName(), EntityType.Builder.of(EntityType.boatFactory(() -> boatItem.get()), MobCategory.MISC).noLootTable().sized(1.375F, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10));
    }
    private Supplier<EntityType<ChestBoat>> createChestBoatEntity(){
        return register(this.getId().getPath() + "_chest_" + woodsetSettings.getBoatName(), EntityType.Builder.of(EntityType.chestBoatFactory(() -> chestBoatItem.get()), MobCategory.MISC).noLootTable().sized(1.375F, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10));
    }
    private SuppliedItem createBoatItem(){
        return createItem(this.getId().getPath() + "_" + woodsetSettings.getBoatName(), settings -> new BoatItem(boat.get(), settings), () -> new Item.Properties().stacksTo(1));
    }
    private SuppliedItem createChestBoatItem(){
        return createItem(this.getId().getPath() + "_chest_" + woodsetSettings.getBoatName(), settings -> new BoatItem(chestBoat.get(), settings), () -> new Item.Properties().stacksTo(1));
    }

    private Block getBase(){
        WoodPreset preset = getWoodPreset();
        if (preset == WoodPreset.NETHER) return Blocks.CRIMSON_PLANKS;
        if (preset == WoodPreset.BAMBOO) return Blocks.BAMBOO_PLANKS;
        if (preset == WoodPreset.FANCY) return Blocks.CHERRY_PLANKS;
        else return Blocks.OAK_PLANKS;
    }
    private Block getSignBase(){
        WoodPreset preset = getWoodPreset();
        if (preset == WoodPreset.NETHER) return Blocks.CRIMSON_SIGN;
        if (preset == WoodPreset.BAMBOO) return Blocks.BAMBOO_SIGN;
        if (preset == WoodPreset.FANCY) return Blocks.CHERRY_SIGN;
        else return Blocks.OAK_SIGN;
    }
    private Block getHangingSignBase(){
        WoodPreset preset = getWoodPreset();
        if (preset == WoodPreset.NETHER) return Blocks.CRIMSON_HANGING_SIGN;
        if (preset == WoodPreset.BAMBOO) return Blocks.BAMBOO_HANGING_SIGN;
        if (preset == WoodPreset.FANCY) return Blocks.CHERRY_HANGING_SIGN;
        else return Blocks.OAK_HANGING_SIGN;
    }

    private BlockSetType createBlockSetType(){
        return getWoodPreset().blockSetType();
    }

    public boolean hasLeaves(){
        return this.getWoodsetSettings().leaf != null;
    }
    public boolean hasSapling(){
        return this.getWoodsetSettings().sapling != null;
    }
    public boolean hasWood(){
        return this.getWoodPreset() != WoodPreset.BAMBOO;
    }
    public boolean hasMosaic() {
        return this.getWoodsetSettings().hasMosaic;
    }
    public boolean hasBoats() {
        return this.getWoodsetSettings().boatType != BoatType.NONE;
    }

    private Supplier<BlockBehaviour.Properties> createLeavesBlock(MapColor color) {
        return () -> BlockBehaviour.Properties.of().mapColor(color).strength(0.2F).randomTicks().sound(woodsetSettings.leafSoundType.get()).noOcclusion().isValidSpawn(Blocks::ocelotOrParrot).isSuffocating((_, _, _) -> false).isViewBlocking((_, _, _) -> false).ignitedByLava().pushReaction(PushReaction.DESTROY).isRedstoneConductor((_, _, _) -> false);
    }

    public enum WoodPreset {
        DEFAULT(WoodType.OAK),
        FANCY(WoodType.CHERRY),
        NETHER(WoodType.CRIMSON),
        BAMBOO(WoodType.BAMBOO);

        private final WoodType woodType;

        WoodPreset(WoodType type){
            this.woodType = type;
        }

        public WoodType getWoodType() {
            return woodType;
        }

        public BlockSetType blockSetType(){
            return woodType.setType();
        }
    }

    public enum BoatType {
        BOAT,
        RAFT,
        NONE
    }

    public static class Settings{
        private String logName = null;
        private String woodName = null;
        private Pair<Function<BlockBehaviour.Properties, Block>, MapColor> leaf = null;
        private Pair<Function<BlockBehaviour.Properties, Block>, MapColor> sapling = null;
        private BoatType boatType = BoatType.BOAT;

        private boolean hasMosaic = false;
        private boolean unburnable = false;
        private WoodPreset woodPreset = WoodPreset.DEFAULT;

        private Supplier<SoundType> leafSoundType = () -> SoundType.GRASS;
        private Supplier<SoundType> woodSoundType = () -> null;
        private Supplier<SoundType> hangingSignSoundType = () -> null;
        private Pair<Supplier<SoundEvent>, Supplier<SoundEvent>> buttonSounds = null;
        private Pair<Supplier<SoundEvent>, Supplier<SoundEvent>> pressurePlateSounds = null;
        private Pair<Supplier<SoundEvent>, Supplier<SoundEvent>> doorSounds = null;
        private Pair<Supplier<SoundEvent>, Supplier<SoundEvent>> trapdoorSounds = null;
        private Pair<Supplier<SoundEvent>, Supplier<SoundEvent>> fenceGateSounds = null;

        private PrecedingCreativeEntries creativeEntries = null;

        private Settings() {
        }

        public boolean canBurn() {
            return !unburnable;
        }

        public BoatType getBoatType() {
            return boatType;
        }

        public Supplier<SoundType> getLeafSoundType() {
            return leafSoundType;
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

        public String getLogName() {
            return logName;
        }

        public String getWoodName() {
            return woodName;
        }

        public WoodPreset getWoodPreset() {
            return woodPreset;
        }

        private String getBoatName(){
            return this.getBoatType() == BoatType.RAFT ? "raft" : "boat";
        }
    }

    public static class Builder {

        private final Identifier id;
        private final MapColor barkColor;
        private final MapColor plankColor;
        private final Settings settings = new Settings();

        private final UnifiedRegistries.Items itemRegistry;
        private final UnifiedRegistries.Blocks blockRegistry;
        private final UnifiedRegistries.EntityTypes entityRegistry;

        public Woodset build() {
            settings.woodName = getWoodName();
            settings.logName = getLogName();
            WoodType type = settings.woodPreset.getWoodType();
            BlockSetType set = type.setType();
            if (settings.woodSoundType == null) settings.woodSoundType = type::soundType;
            if (settings.hangingSignSoundType == null) settings.hangingSignSoundType = type::hangingSignSoundType;
            if (settings.buttonSounds == null) settings.buttonSounds = Pair.of(set::buttonClickOn, set::buttonClickOff);
            if (settings.pressurePlateSounds == null) settings.pressurePlateSounds = Pair.of(set::pressurePlateClickOn, set::pressurePlateClickOff);
            if (settings.doorSounds == null) settings.doorSounds = Pair.of(set::doorOpen, set::doorClose);
            if (settings.trapdoorSounds == null) settings.trapdoorSounds = Pair.of(set::trapdoorOpen, set::trapdoorClose);
            if (settings.fenceGateSounds == null) settings.fenceGateSounds = Pair.of(type::fenceGateOpen, type::fenceGateClose);
            return new Woodset(id, barkColor, plankColor, settings, itemRegistry, blockRegistry, entityRegistry);
        }

        public Builder(Identifier id, MapColor barkColor, MapColor plankColor, UnifiedRegistries.Items itemRegistry, UnifiedRegistries.Blocks blockRegistry, UnifiedRegistries.EntityTypes entityRegistry) {
            this.id = id;
            this.barkColor = barkColor;
            this.plankColor = plankColor;
            this.itemRegistry = itemRegistry;
            this.blockRegistry = blockRegistry;
            this.entityRegistry = entityRegistry;
        }

        public Builder creativeInventoryPlacement(ItemLike precedingBuildingItem, ItemLike precedingNaturalItem, ItemLike precedingFunctionalItem, ItemLike precedingUtilitiesItem) {
            settings.creativeEntries = new PrecedingCreativeEntries(precedingBuildingItem, precedingNaturalItem, precedingFunctionalItem, precedingUtilitiesItem);
            return this;
        }

        public Builder leafSoundType(Supplier<SoundType> leafSoundType) {
            settings.leafSoundType = leafSoundType;
            return this;
        }
        public Builder woodSoundType(Supplier<SoundType> woodSoundType) {
            settings.woodSoundType = woodSoundType;
            return this;
        }
        public Builder hangingSignSoundType(Supplier<SoundType> hangingSignSoundType) {
            settings.hangingSignSoundType = hangingSignSoundType;
            return this;
        }

        public Builder buttonSounds(Supplier<SoundEvent> buttonOnSound, Supplier<SoundEvent> buttonOffSound) {
            settings.buttonSounds = Pair.of(buttonOnSound, buttonOffSound);
            return this;
        }
        public Builder pressurePlateSounds(Supplier<SoundEvent> pressurePlateOnSound, Supplier<SoundEvent> pressurePlateOffSound) {
            settings.pressurePlateSounds = Pair.of(pressurePlateOnSound, pressurePlateOffSound);
            return this;
        }
        public Builder doorSounds(Supplier<SoundEvent> doorOpenSound, Supplier<SoundEvent> doorCloseSound) {
            settings.doorSounds = Pair.of(doorOpenSound, doorCloseSound);
            return this;
        }
        public Builder trapdoorSounds(Supplier<SoundEvent> trapdoorOpenSound, Supplier<SoundEvent> trapdoorCloseSound) {
            settings.trapdoorSounds = Pair.of(trapdoorOpenSound, trapdoorCloseSound);
            return this;
        }
        public Builder fenceGateSounds(Supplier<SoundEvent> fenceGateOpenSound, Supplier<SoundEvent> fenceGateCloseSound) {
            settings.fenceGateSounds = Pair.of(fenceGateOpenSound, fenceGateCloseSound);
            return this;
        }

        public Builder createLeaves(Function<BlockBehaviour.Properties, Block> leafProperties, MapColor mapColor) {
            settings.leaf = Pair.of(leafProperties, mapColor);
            return this;
        }

        public Builder createSapling(Function<BlockBehaviour.Properties, Block> saplingProperties, MapColor mapColor) {
            settings.sapling = Pair.of(saplingProperties, mapColor);
            return this;
        }

        public Builder woodName(String woodName) {
            settings.woodName = woodName;
            return this;
        }

        public Builder logName(String logName) {
            settings.logName = logName;
            return this;
        }

        public Builder setBoatType(BoatType type) {
            settings.boatType = BoatType.RAFT;
            return this;
        }

        public Builder woodPreset(WoodPreset woodPreset) {
            settings.woodPreset = woodPreset;
            return this;
        }

        public Builder hasMosaic(){
            settings.hasMosaic = true;
            return this;
        }

        public Builder unburnable(){
            settings.unburnable = true;
            return this;
        }

        private String getLogName(){
            if (settings.logName != null){
                return settings.logName;
            }
            if (settings.woodPreset == WoodPreset.NETHER) return "stem";
            if (settings.woodPreset == WoodPreset.BAMBOO) return "block";
            return "log";
        }

        private String getWoodName(){
            return Objects.requireNonNullElseGet(settings.woodName, () -> settings.woodPreset == WoodPreset.NETHER ? "hyphae" : "wood");
        }
    }

    public record PrecedingCreativeEntries(ItemLike building, ItemLike natural, ItemLike functional, ItemLike utilities) {}
}