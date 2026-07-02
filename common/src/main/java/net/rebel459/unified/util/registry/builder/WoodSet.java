package net.rebel459.unified.util.registry.builder;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
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
import net.rebel459.unified.platform.UnifiedPlatform;
import net.rebel459.unified.platform.UnifiedRegistries;
import net.rebel459.unified.util.LoaderType;
import net.rebel459.unified.util.registry.SuppliedBlock;
import net.rebel459.unified.util.registry.SuppliedItem;
import net.rebel459.unified.util.registry.builder.impl.WoodSetImpl;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class WoodSet {

    public static final List<WoodSet> WOODSETS = new ArrayList<>();
    public static final HashMap<WoodSet, PrecedingCreativeEntries> WOODSET_CREATIVE_ENTRIES = new HashMap<>();

    private final List<SuppliedBlock> registeredBlocksList = new ArrayList<>();
    private final List<SuppliedItem> registeredItemsList = new ArrayList<>();

    private static final List<SuppliedBlock> signBlocks = new ArrayList<>();
    private static final List<SuppliedBlock> hangingSignBlocks = new ArrayList<>();

    private final Identifier id;
    private final MapColor barkColor;
    private final MapColor plankColor;

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

    private @Nullable Supplier<EntityType<Boat>> boat;
    private @Nullable Supplier<EntityType<ChestBoat>> chestBoat;

    private BlockFamily.Builder blockFamily = null;
    private final Settings settings;

    private void registerWood() {
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
        if (this.settings.leaf != null){
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

        signBlocks.add(sign);
        signBlocks.add(wallSign);

        hangingSignBlocks.add(hangingSign);
        hangingSignBlocks.add(wallHangingSign);
    }

    public WoodSet(Identifier id, MapColor sideColor, MapColor plankColor, Settings settings, UnifiedRegistries.Items itemRegistry, UnifiedRegistries.Blocks blockRegistry, UnifiedRegistries.EntityTypes entityRegistry){
        this.settings = settings;
        this.id = id;
        this.barkColor = sideColor;
        this.plankColor = plankColor;
        this.itemRegistry = itemRegistry;
        this.blockRegistry = blockRegistry;
        this.entityRegistry = entityRegistry;
        registerWood();
        WOODSETS.add(this);
        WOODSET_CREATIVE_ENTRIES.put(this, this.getSettings().precedingCreativeEntries);
        if (UnifiedPlatform.getLoader() == LoaderType.FABRIC) WoodSetImpl.init(List.of(this));
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
        return () -> BlockBehaviour.Properties.of().mapColor(state -> state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? topMapColor : sideMapColor).strength(2.0F).sound(this.getSettings().woodSoundType.get());
    }

    public Settings getSettings() {
        return settings;
    }

    public Identifier getId() {
        return id;
    }

    public MapColor getBarkColor() {
        return barkColor;
    }

    public MapColor getPlankColor() {
        return plankColor;
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
            blockFamily.trapdoor(trapdoor.get());
            blockFamily.sign(sign.get(), wallSign.get());
            blockFamily.button(button.get());
            blockFamily.pressurePlate(pressurePlate.get());
        }
        return blockFamily.getFamily();
    }
    private SuppliedBlock createLog() {
        return createBlockWithItem(this.getId().getPath() + "_" + settings.getLogName(), RotatedPillarBlock::new, createLogBlock(this.getBarkColor(), this.getPlankColor()));
    }
    private SuppliedBlock createStrippedLog() {
        return createBlockWithItem("stripped_" + this.getId().getPath() + "_" + settings.getLogName(), RotatedPillarBlock::new, createLogBlock(this.getBarkColor(), this.getPlankColor()));
    }
    private SuppliedBlock createWood() {
        return createBlockWithItem(this.getId().getPath() + "_" + settings.getWoodName(), RotatedPillarBlock::new, createLogBlock(this.getBarkColor(), this.getBarkColor()));
    }
    private SuppliedBlock createStrippedWood() {
        return createBlockWithItem("stripped_" + this.getId().getPath() + "_" + settings.getWoodName(), RotatedPillarBlock::new, createLogBlock(this.getPlankColor(), this.getPlankColor()));
    }
    private SuppliedBlock createLeaves() {
        Function<BlockBehaviour.Properties, Block> properties = this.settings.leaf.getFirst();
        return createBlockWithItem(this.getId().getPath() + "_leaves", properties, createLeavesBlock(this.settings.leaf.getSecond()));
    }
    private SuppliedBlock createSapling() {
        Function<BlockBehaviour.Properties, Block> properties = this.settings.sapling.getFirst();
        return createBlockWithItem(this.getId().getPath() + "_sapling", properties, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING).mapColor(this.settings.sapling.getSecond()));
    }
    private SuppliedBlock createPottedSapling(SuppliedBlock sapling) {
        return createBlockWithoutItem("potted_" + this.getId().getPath() + "_sapling", properties -> new FlowerPotBlock(sapling.get(), properties), Blocks::flowerPotProperties);
    }
    private SuppliedBlock createPlanks(){
        return createBlockWithItem(this.getId().getPath() + "_planks", () -> BlockBehaviour.Properties.ofFullCopy(getBase()).sound(getSettings().woodSoundType.get()).mapColor(getPlankColor()));
    }
    private SuppliedBlock createStairs(){
        return createBlockWithItem(this.getId().getPath() + "_stairs", settings -> new StairBlock(getBase().defaultBlockState(), settings), () -> BlockBehaviour.Properties.ofFullCopy(getBase()).sound(getSettings().woodSoundType.get()).mapColor(getPlankColor()));
    }
    private SuppliedBlock createSlab(){
        return createBlockWithItem(this.getId().getPath() + "_slab", SlabBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(getBase()).sound(getSettings().woodSoundType.get()).mapColor(getPlankColor()));
    }
    private SuppliedBlock createMosaic(){
        return createBlockWithItem(this.getId().getPath() + "_mosaic", () -> BlockBehaviour.Properties.ofFullCopy(getBase()).sound(getSettings().woodSoundType.get()).mapColor(getPlankColor()));
    }
    private SuppliedBlock createMosaicStairs(){
        return createBlockWithItem(this.getId().getPath() + "_mosaic_stairs", settings -> new StairBlock(getBase().defaultBlockState(), settings), () -> BlockBehaviour.Properties.ofFullCopy(getBase()).sound(getSettings().woodSoundType.get()).mapColor(getPlankColor()));
    }
    private SuppliedBlock createMosaicSlab(){
        return createBlockWithItem(this.getId().getPath() + "_mosaic_slab", SlabBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(getBase()).sound(getSettings().woodSoundType.get()).mapColor(getPlankColor()));
    }
    private SuppliedBlock createFence(){
        return createBlockWithItem(this.getId().getPath() + "_fence", FenceBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(getBase()).sound(getSettings().woodSoundType.get()).mapColor(getPlankColor()));
    }
    private SuppliedBlock createFenceGate(){
        return createBlockWithItem(this.getId().getPath() + "_fence_gate", settings -> new FenceGateBlock(this.getWoodType().get(), settings), () -> BlockBehaviour.Properties.ofFullCopy(getBase()).sound(getSettings().woodSoundType.get()).mapColor(getPlankColor()));
    }
    private SuppliedBlock createPressurePlate(){
        return createBlockWithItem(this.getId().getPath() + "_pressure_plate", settings -> new PressurePlateBlock(this.getWoodType().get().setType(), settings), () -> BlockBehaviour.Properties.ofFullCopy(getBase()).sound(getSettings().woodSoundType.get()).mapColor(getPlankColor()));
    }
    private SuppliedBlock createButton(){
        return createBlockWithItem(this.getId().getPath() + "_button", settings -> new ButtonBlock(this.getWoodType().get().setType(), 30, settings), () -> BlockBehaviour.Properties.ofFullCopy(getBase()).sound(getSettings().woodSoundType.get()).mapColor(getPlankColor()));
    }
    private SuppliedBlock createDoor(){
        return createBlockWithItem(this.getId().getPath() + "_door", settings -> new DoorBlock(this.getWoodType().get().setType(), settings), () -> BlockBehaviour.Properties.ofFullCopy(getBase()).sound(getSettings().woodSoundType.get()).mapColor(getPlankColor()).noOcclusion());
    }
    private SuppliedBlock createTrapDoor(){
        return createBlockWithItem(this.getId().getPath() + "_trapdoor", settings -> new TrapDoorBlock(this.getWoodType().get().setType(), settings), () -> BlockBehaviour.Properties.ofFullCopy(getBase()).sound(getSettings().woodSoundType.get()).mapColor(getPlankColor()).noOcclusion());
    }
    private SuppliedBlock createSign(){
        return createBlockWithoutItem(this.getId().getPath() + "_sign", settings -> new StandingSignBlock(
                        this.getWoodType().get(), settings),
			() -> BlockBehaviour.Properties.ofFullCopy(getSignBase()).mapColor(this.getPlankColor()).sound(getSettings().woodSoundType.get()),
			BlockEntityType.SIGN
		);
    }
    private SuppliedBlock createWallSign(){
        return createBlockWithoutItem(this.getId().getPath() + "_wall_sign", settings -> new WallSignBlock(
                        this.getWoodType().get(), settings),
			() -> BlockBehaviour.Properties.ofFullCopy(getSignBase()).mapColor(this.getPlankColor()).overrideLootTable(sign.get().getLootTable()).sound(getSettings().woodSoundType.get()),
			BlockEntityType.SIGN
		);
    }

    private SuppliedBlock createHangingSign(){
        return createBlockWithoutItem(this.getId().getPath() + "_hanging_sign", settings -> new CeilingHangingSignBlock(
                        this.getWoodType().get(), settings),
			() -> BlockBehaviour.Properties.ofFullCopy(getHangingSignBase()).mapColor(this.getPlankColor()).sound(getSettings().hangingSignSoundType.get()),
			BlockEntityType.HANGING_SIGN
		);
    }
    private SuppliedBlock createWallHangingSign(){
        return createBlockWithoutItem(this.getId().getPath() + "_wall_hanging_sign", settings -> new WallHangingSignBlock(
                        this.getWoodType().get(), settings),
			() -> BlockBehaviour.Properties.ofFullCopy(getHangingSignBase()).mapColor(this.getPlankColor()).overrideLootTable(hangingSign.get().getLootTable()).sound(getSettings().hangingSignSoundType.get()),
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
        return register(this.getId().getPath() + "_" + getBoatName(), EntityType.Builder.of(EntityType.boatFactory(() -> boatItem.get()), MobCategory.MISC).noLootTable().sized(1.375F, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10));
    }
    private Supplier<EntityType<ChestBoat>> createChestBoatEntity(){
        return register(this.getId().getPath() + "_chest_" + getBoatName(), EntityType.Builder.of(EntityType.chestBoatFactory(() -> chestBoatItem.get()), MobCategory.MISC).noLootTable().sized(1.375F, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10));
    }
    private SuppliedItem createBoatItem(){
        return createItem(this.getId().getPath() + "_" + getBoatName(), settings -> new BoatItem(boat.get(), settings), () -> new Item.Properties().stacksTo(1));
    }
    private SuppliedItem createChestBoatItem(){
        return createItem(this.getId().getPath() + "_chest_" + getBoatName(), settings -> new BoatItem(chestBoat.get(), settings), () -> new Item.Properties().stacksTo(1));
    }

    private Block getBase(){
        if (!this.getSettings().isFlammable) return Blocks.CRIMSON_PLANKS;
        else return Blocks.OAK_PLANKS;
    }
    private Block getSignBase(){
        if (!this.getSettings().isFlammable) return Blocks.CRIMSON_SIGN;
        else return Blocks.OAK_SIGN;
    }
    private Block getHangingSignBase(){
        if (!this.getSettings().isFlammable) return Blocks.CRIMSON_HANGING_SIGN;
        else return Blocks.OAK_HANGING_SIGN;
    }

    public boolean hasLeaves(){
        return this.getSettings().leaf != null;
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

    private Supplier<BlockBehaviour.Properties> createLeavesBlock(MapColor color) {
        return () -> BlockBehaviour.Properties.of().mapColor(color).strength(0.2F).randomTicks().sound(settings.leafSoundType.get()).noOcclusion().isValidSpawn(Blocks::ocelotOrParrot).isSuffocating((_, _, _) -> false).isViewBlocking((_, _, _) -> false).ignitedByLava().pushReaction(PushReaction.DESTROY).isRedstoneConductor((_, _, _) -> false);
    }

    private String getBoatName(){
        return this.getSettings().getBoats() == Boats.RAFTS ? "raft" : "boat";
    }

    public Supplier<WoodType> getWoodType() {
        return () -> new WoodType(
                id.toString(),
                new BlockSetType(
                        id.toString(),
                        this.settings.doorOpening.getFirst(),
                        this.settings.doorOpening.getSecond(),
                        this.settings.buttonArrowActivation,
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

    public enum Boats {
        BOATS,
        RAFTS,
        NONE
    }

    public static class Settings implements Cloneable {
        private String logName = "log";
        private String woodName = "wood";
        private Pair<Function<BlockBehaviour.Properties, Block>, MapColor> leaf = null;
        private Pair<Function<BlockBehaviour.Properties, Block>, MapColor> sapling = null;
        private Boats boats = Boats.BOATS;

        private boolean hasMosaic = false;
        private boolean hasWood = true;
        private boolean isFlammable = true;

        private Pair<Boolean, Boolean> doorOpening = Pair.of(true, true);
        private boolean buttonArrowActivation = true;
        private BlockSetType.PressurePlateSensitivity pressurePlateSensitivity = BlockSetType.PressurePlateSensitivity.EVERYTHING;

        private Supplier<SoundType> leafSoundType = () -> SoundType.GRASS;
        private Supplier<SoundType> woodSoundType = () -> SoundType.WOOD;
        private Supplier<SoundType> hangingSignSoundType = () -> SoundType.HANGING_SIGN;
        private Pair<Supplier<SoundEvent>, Supplier<SoundEvent>> buttonSounds = Pair.of(() -> SoundEvents.WOODEN_BUTTON_CLICK_ON, () -> SoundEvents.WOODEN_BUTTON_CLICK_OFF);
        private Pair<Supplier<SoundEvent>, Supplier<SoundEvent>> pressurePlateSounds = Pair.of(() -> SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_ON, () -> SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_OFF);
        private Pair<Supplier<SoundEvent>, Supplier<SoundEvent>> doorSounds = Pair.of(() -> SoundEvents.WOODEN_DOOR_OPEN, () -> SoundEvents.WOODEN_DOOR_CLOSE);
        private Pair<Supplier<SoundEvent>, Supplier<SoundEvent>> trapdoorSounds = Pair.of(() -> SoundEvents.WOODEN_TRAPDOOR_OPEN, () -> SoundEvents.WOODEN_TRAPDOOR_CLOSE);
        private Pair<Supplier<SoundEvent>, Supplier<SoundEvent>> fenceGateSounds = Pair.of(() -> SoundEvents.FENCE_GATE_OPEN, () -> SoundEvents.FENCE_GATE_CLOSE);

        private PrecedingCreativeEntries precedingCreativeEntries = null;

        Settings() {}

        public boolean isFlammable() {
            return isFlammable;
        }

        public Boats getBoats() {
            return boats;
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

        public Pair<Boolean, Boolean> getDoorOpening() {
            return doorOpening;
        }
        public boolean getButtonArrowActivation() {
            return buttonArrowActivation;
        }
        public BlockSetType.PressurePlateSensitivity getPressurePlateSensitivity() {
            return pressurePlateSensitivity;
        }

        public PrecedingCreativeEntries getPrecedingCreativeEntries() {
            return precedingCreativeEntries;
        }

        public String getLogName() {
            return logName;
        }

        public String getWoodName() {
            return woodName;
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
        private final MapColor barkColor;
        private final MapColor plankColor;

        private final UnifiedRegistries.Items itemRegistry;
        private final UnifiedRegistries.Blocks blockRegistry;
        private final UnifiedRegistries.EntityTypes entityRegistry;

        public WoodSet build() {
            return new WoodSet(id, barkColor, plankColor, settings, itemRegistry, blockRegistry, entityRegistry);
        }

        public RegistryBuilder(Identifier id, MapColor barkColor, MapColor plankColor, WoodPreset preset, UnifiedRegistries.Items itemRegistry, UnifiedRegistries.Blocks blockRegistry, UnifiedRegistries.EntityTypes entityRegistry) {
            super(preset.settings.copy());

            this.id = id;
            this.barkColor = barkColor;
            this.plankColor = plankColor;
            this.itemRegistry = itemRegistry;
            this.blockRegistry = blockRegistry;
            this.entityRegistry = entityRegistry;
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
                ItemLike precedingBuildingItem,
                ItemLike precedingNaturalItem,
                ItemLike precedingFunctionalItem,
                ItemLike precedingUtilitiesItem
        ) {
            settings.precedingCreativeEntries = new PrecedingCreativeEntries(
                    precedingBuildingItem,
                    precedingNaturalItem,
                    precedingFunctionalItem,
                    precedingUtilitiesItem
            );
            return self();
        }

        public T leafSoundType(Supplier<SoundType> leafSoundType) {
            settings.leafSoundType = leafSoundType;
            return self();
        }

        public T woodSoundType(Supplier<SoundType> woodSoundType) {
            settings.woodSoundType = woodSoundType;
            return self();
        }

        public T hangingSignSoundType(Supplier<SoundType> hangingSignSoundType) {
            settings.hangingSignSoundType = hangingSignSoundType;
            return self();
        }

        public T buttonSounds(Supplier<SoundEvent> on, Supplier<SoundEvent> off) {
            settings.buttonSounds = Pair.of(on, off);
            return self();
        }

        public T pressurePlateSounds(Supplier<SoundEvent> on, Supplier<SoundEvent> off) {
            settings.pressurePlateSounds = Pair.of(on, off);
            return self();
        }

        public T doorSounds(Supplier<SoundEvent> open, Supplier<SoundEvent> close) {
            settings.doorSounds = Pair.of(open, close);
            return self();
        }

        public T trapdoorSounds(Supplier<SoundEvent> open, Supplier<SoundEvent> close) {
            settings.trapdoorSounds = Pair.of(open, close);
            return self();
        }

        public T fenceGateSounds(Supplier<SoundEvent> open, Supplier<SoundEvent> close) {
            settings.fenceGateSounds = Pair.of(open, close);
            return self();
        }

        public T createLeaves(Function<BlockBehaviour.Properties, Block> properties, MapColor mapColor) {
            settings.leaf = Pair.of(properties, mapColor);
            return self();
        }

        public T createSapling(Function<BlockBehaviour.Properties, Block> properties, MapColor mapColor) {
            settings.sapling = Pair.of(properties, mapColor);
            return self();
        }

        public T woodName(String woodName) {
            settings.woodName = woodName;
            return self();
        }

        public T logName(String logName) {
            settings.logName = logName;
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

        public T setButtonArrowActivation(boolean arrowsActivateButton) {
            settings.buttonArrowActivation = arrowsActivateButton;
            return self();
        }

        public T setPressurePlateSensitivity(BlockSetType.PressurePlateSensitivity pressurePlateSensitivity) {
            settings.pressurePlateSensitivity = pressurePlateSensitivity;
            return self();
        }

        private String getLogName() {
            return settings.logName;
        }

        private String getWoodName() {
            return settings.woodName;
        }
    }

    public record PrecedingCreativeEntries(ItemLike building, ItemLike natural, ItemLike functional, ItemLike utilities) {}
}