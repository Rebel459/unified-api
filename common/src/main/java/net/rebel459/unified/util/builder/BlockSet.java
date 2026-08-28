package net.rebel459.unified.util.builder;

import com.mojang.datafixers.util.Pair;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.MapColor;
import net.rebel459.unified.platform.UnifiedPlatform;
import net.rebel459.unified.platform.UnifiedDataRegistries;
import net.rebel459.unified.platform.UnifiedRegistries;
import net.rebel459.unified.registry.VanillaBlockTypes;
import net.rebel459.unified.util.LoaderType;
import net.rebel459.unified.util.RecipeProvider;
import net.rebel459.unified.util.codec.ExtensibleCodec;
import net.rebel459.unified.util.datagen.BlockAsset;
import net.rebel459.unified.util.datagen.BlockAssets;
import net.rebel459.unified.util.datagen.impl.DataRegistry;
import net.rebel459.unified.util.data.registry.BlockRegistry;
import net.rebel459.unified.util.data.registry.BlockSetTypeRegistry;
import net.rebel459.unified.util.registry.SuppliedBlock;
import net.rebel459.unified.util.builder.impl.BlockSetImpl;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class BlockSet {

    public static final List<BlockSet> BLOCK_SETS = new CopyOnWriteArrayList<>();

    private final List<SuppliedBlock> registeredBlocks = new ArrayList<>();

    private final Identifier id;
    private final MapColor color;

    private final UnifiedDataRegistries.Blocks blocks;
    @Deprecated private final UnifiedRegistries.Blocks blockRegistry;

    private SuppliedBlock base;
    private @Nullable SuppliedBlock stairs;
    private @Nullable SuppliedBlock slab;
    private @Nullable SuppliedBlock chiseled;
    private @Nullable SuppliedBlock cracked;
    private @Nullable SuppliedBlock pillar;
    private @Nullable SuppliedBlock wall;
    private @Nullable SuppliedBlock fence;
    private @Nullable SuppliedBlock pressurePlate;
    private @Nullable SuppliedBlock button;

    private final Settings settings;

    private Supplier<BlockSetType> blockSetType = null;

    private void registerBlocks() {
        base = createBase();
        if (hasStairs()) stairs = createStairs();
        if (hasSlab()) slab = createSlab();
        if (hasChiseled()) chiseled = createChiseled();
        if (hasCracked()) cracked = createCracked();
        if (hasPillar()) pillar = createPillar();
        if (hasWall()) wall = createWall();
        if (hasFence()) fence = createFence();
        if (hasPressurePlate()) pressurePlate = createPressurePlate();
        if (hasButton()) button = createButton();
    }

    public BlockSet(Identifier id, MapColor color, Settings settings, UnifiedDataRegistries.Blocks blocks, UnifiedRegistries.Blocks blockRegistry){
        this.settings = settings;
        this.id = id;
        this.color = color;
        this.blockRegistry = blockRegistry;
        this.blocks = blocks;
        if (blocks != null) registerBlockSetTypeDefinition();
        registerBlocks();
        BLOCK_SETS.add(this);
        BlockSetImpl.CREATIVE_ENTRIES.put(id, getSettings().precedingCreativeEntries);
        if (UnifiedPlatform.getLoader() == LoaderType.FABRIC) BlockSetImpl.init(List.of(this));
    }

    private void registerBlockSetTypeDefinition() {
        DataRegistry.addBlockSetType(id, () -> new BlockSetTypeRegistry.Definition(
                true,
                true,
                settings.canArrowsActivateButton,
                settings.pressurePlateSensitivity,
                BlockRegistry.SoundType.create(settings.soundType.get()),
                SoundEvents.IRON_DOOR_CLOSE,
                SoundEvents.IRON_DOOR_OPEN,
                SoundEvents.IRON_TRAPDOOR_CLOSE,
                SoundEvents.IRON_TRAPDOOR_OPEN,
                settings.pressurePlateSounds.getSecond().get(),
                settings.pressurePlateSounds.getFirst().get(),
                settings.buttonSounds.getSecond().get(),
                settings.buttonSounds.getFirst().get()
        ));
    }

    @Deprecated
    private SuppliedBlock createBlockWithItem(String blockID, Supplier<BlockBehaviour.Properties> settings){
        return createBlockWithItem(blockID, Block::new, settings);
    }
    @Deprecated
    private SuppliedBlock createBlockWithItem(String blockID, Function<BlockBehaviour.Properties, Block> factory, Supplier<BlockBehaviour.Properties> settings){
        SuppliedBlock block = blockRegistry.register(blockID, factory, settings);
        registeredBlocks.add(block);
        return block;
    }

    private SuppliedBlock createBlock(String path, ExtensibleCodec.Entry<Function<BlockBehaviour.Properties, ? extends Block>> type, Consumer<UnifiedDataRegistries.Blocks.Builder> builder){
        SuppliedBlock block = blocks.register(path, type, builder);
        registeredBlocks.add(block);
        return block;
    }

    private SuppliedBlock createBlock(String path, Supplier<ExtensibleCodec.Entry<Function<BlockBehaviour.Properties, ? extends Block>>> type, Consumer<UnifiedDataRegistries.Blocks.Builder> builder){
        SuppliedBlock block = blocks.register(path, type, builder);
        registeredBlocks.add(block);
        return block;
    }

    public Settings getSettings() {
        return settings;
    }

    public Identifier getId() {
        return id;
    }

    public @Nullable SuppliedBlock getButton() {
        return button;
    }

    public @Nullable SuppliedBlock getFence() {
        return fence;
    }

    public SuppliedBlock getBase() {
        return base;
    }

    public @Nullable SuppliedBlock getSlab() {
        return slab;
    }

    public @Nullable SuppliedBlock getStairs() {
        return stairs;
    }

    public @Nullable SuppliedBlock getPressurePlate() {
        return pressurePlate;
    }

    public @Nullable SuppliedBlock getWall() {
        return wall;
    }

    public @Nullable SuppliedBlock getCracked() {
        return cracked;
    }

    public @Nullable SuppliedBlock getChiseled() {
        return chiseled;
    }

    public @Nullable SuppliedBlock getPillar() {
        return pillar;
    }

    public List<SuppliedBlock> getRegisteredBlocks() {
        return registeredBlocks;
    }

    private SuppliedBlock createBase(){
        String name = this.getId().getPath();
        if (getSettings().baseBlockSuffix.isPresent()) name = name + "_" + getSettings().baseBlockSuffix.get();
        if (blocks != null) return createBlock(name, getSettings().baseBlockType, builder -> builder
                .properties(properties -> properties
                        .copyFrom(Blocks.STONE.builtInRegistryHolder().key())
                        .soundType(getSettings().soundType.get())
                        .mapColor(color)
                        .strength(getSettings().destroyTime, getSettings().explosionResistance)
                )
                .assets(getSettings().baseBlockModel)
                .data(data -> {
                            getSettings().baseBlockTag.ifPresent(data::tag);
                            data.dropSelf();
                            data.tag(BlockTags.MINEABLE_WITH_PICKAXE);
                        }
                )
        );
        return createBlockWithItem(name, getSettings().baseBlockFunction, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).sound(getSettings().getSoundType().get()).mapColor(color).strength(getSettings().destroyTime, getSettings().explosionResistance));
    }
    private SuppliedBlock createStairs() {
        if (blocks != null) return createBlock(this.getFormattedName() + "_stairs", () -> VanillaBlockTypes.STAIRS.create(getBase().key()), builder -> builder
                .properties(properties -> properties.copyFrom(getBase().key()))
                .assets(assets -> assets.model(BlockAssets.STAIRS, getBase().get()))
                .data(data -> data
                        .dropSelf()
                        .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                        .recipes((item, provider) -> provider.stairBuilder(item, Ingredient.of(getBase().get()))
                                .unlockedBy(RecipeProvider.getHasName(getBase()), provider.has(getBase()))
                                .save(provider.output))
                        .tag(BlockTags.STAIRS)
                        .itemTag(ItemTags.STAIRS)
                )
        );
        return createBlockWithItem(this.getFormattedName() + "_stairs", settings -> new StairBlock(getBase().defaultBlockState(), settings), () -> BlockBehaviour.Properties.ofFullCopy(getBase().get()));
    }
    private SuppliedBlock createSlab(){
        Supplier<BlockBehaviour.Properties> blockProperties = () -> BlockBehaviour.Properties.ofFullCopy(getBase().get());
        if (getSettings().hasLegacySlab) blockProperties = () -> BlockBehaviour.Properties.ofFullCopy(getBase().get()).strength(2F, 6F);
        if (blocks != null) return createBlock(this.getFormattedName() + "_slab", VanillaBlockTypes.SLAB::create, builder -> builder
                .properties(properties -> {
                    properties.copyFrom(getBase().key());
                    if (getSettings().hasLegacySlab) properties.strength(2F, 6F);
                })
                .assets(assets -> assets.model(BlockAssets.SLAB, getBase().get()))
                .data(data -> data
                        .dropSelf()
                        .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                        .recipes((item, provider) -> provider.slabBuilder(RecipeCategory.BUILDING_BLOCKS, item, Ingredient.of(getBase().get()))
                                .unlockedBy(RecipeProvider.getHasName(getBase()), provider.has(getBase()))
                                .save(provider.output))
                        .tag(BlockTags.SLABS)
                        .itemTag(ItemTags.SLABS)
                )
        );
        return createBlockWithItem(this.getFormattedName() + "_slab", SlabBlock::new, blockProperties);
    }
    private SuppliedBlock createFence(){
        if (blocks != null) return createBlock(this.getFormattedName() + "_fence", VanillaBlockTypes.FENCE::create, builder -> builder
                .properties(properties -> properties.copyFrom(getBase().key()))
                .assets(assets -> assets.model(BlockAssets.FENCE, getBase().get()))
                .data(data -> data
                        .dropSelf()
                        .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                        .recipes((item, provider) -> provider.shaped(RecipeCategory.DECORATIONS, item, getSettings().fenceStickReplacement.getSecond()).define('W', getBase()).define('#', getSettings().fenceStickReplacement.getFirst().get()).pattern("W#W").pattern("W#W")
                                .unlockedBy(RecipeProvider.getHasName(getBase()), provider.has(getBase()))
                                .save(provider.output))
                        .tag(BlockTags.FENCES)
                        .itemTag(ItemTags.FENCES)
                )
        );
        return createBlockWithItem(this.getFormattedName() + "_fence", FenceBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(getBase().get()));
    }
    private SuppliedBlock createPressurePlate(){
        if (blocks != null) return createBlock(this.getFormattedName() + "_pressure_plate", () -> VanillaBlockTypes.PRESSURE_PLATE.create(this.getBlockSetType().get()), builder -> builder
                .properties(properties -> properties.copyFrom(getBase().key()))
                .assets(assets -> assets.model(BlockAssets.PRESSURE_PLATE, getBase().get()))
                .data(data -> data
                        .dropSelf()
                        .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                        .recipes((item, provider) -> provider.pressurePlateBuilder(RecipeCategory.BUILDING_BLOCKS, item, Ingredient.of(getBase().get()))
                                .unlockedBy(RecipeProvider.getHasName(getBase()), provider.has(getBase()))
                                .save(provider.output))
                        .tag(BlockTags.PRESSURE_PLATES)
                )
        );
        return createBlockWithItem(this.getFormattedName() + "_pressure_plate", settings -> new PressurePlateBlock(this.getBlockSetType().get(), settings), () -> BlockBehaviour.Properties.ofFullCopy(getBase().get()));
    }
    private SuppliedBlock createButton(){
        if (blocks != null) return createBlock(this.getFormattedName() + "_button", () -> VanillaBlockTypes.BUTTON.create(new VanillaBlockTypes.Button(this.getBlockSetType().get(), 30)), builder -> builder
                .properties(properties -> properties.copyFrom(getBase().key()))
                .assets(assets -> assets.model(BlockAssets.BUTTON, getBase().get()))
                .data(data -> data
                        .dropSelf()
                        .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                        .recipes((item, provider) -> provider.buttonBuilder(item, Ingredient.of(getBase().get()))
                                .unlockedBy(RecipeProvider.getHasName(getBase()), provider.has(getBase()))
                                .save(provider.output))
                        .tag(BlockTags.BUTTONS)
                        .itemTag(ItemTags.BUTTONS)
                )
        );
        return createBlockWithItem(this.getFormattedName() + "_button", settings -> new ButtonBlock(this.getBlockSetType().get(), 30, settings), () -> BlockBehaviour.Properties.ofFullCopy(getBase().get()));
    }
    private SuppliedBlock createChiseled(){
        if (blocks != null) return createBlock("chiseled_" + this.getId().getPath(), VanillaBlockTypes.BLOCK::create, builder -> builder
                .properties(properties -> properties.copyFrom(getBase().key()))
                .assets(assets -> assets.model(BlockAssets.SIMPLE_CUBE))
                .data(data -> data
                        .dropSelf()
                        .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                        .recipes((item, provider) -> provider.chiseledBuilder(RecipeCategory.BUILDING_BLOCKS, item, Ingredient.of(getBase().get()))
                                .unlockedBy(RecipeProvider.getHasName(getBase()), provider.has(getBase()))
                                .save(provider.output))
                )
        );
        return createBlockWithItem("chiseled_" + this.getId().getPath(), () -> BlockBehaviour.Properties.ofFullCopy(this.getBase().get()));
    }
    private SuppliedBlock createCracked(){
        if (blocks != null) return createBlock("cracked_" + this.getId().getPath(), VanillaBlockTypes.BLOCK::create, builder -> builder
                .properties(properties -> properties.copyFrom(getBase().key()))
                .assets(assets -> assets.model(BlockAssets.SIMPLE_CUBE))
                .data(data -> data
                        .dropSelf()
                        .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                        .recipes((item, provider) -> provider.smeltingResultFromBase(item, getBase()))
                )
        );
        return createBlockWithItem("cracked_" + this.getId().getPath(), () -> BlockBehaviour.Properties.ofFullCopy(this.getBase().get()));
    }
    private SuppliedBlock createPillar(){
        if (blocks != null) return createBlock(this.getFormattedName() + "_pillar", VanillaBlockTypes.ROTATED_PILLAR_BLOCK::create, builder -> builder
                .properties(properties -> properties.copyFrom(getBase().key()))
                .assets(assets -> assets.model(BlockAssets.ROTATED_PILLAR))
                .data(data -> data
                        .dropSelf()
                        .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                        .recipes((item, provider) -> {
                            if (hasSlab()) {
                                provider.shaped(RecipeCategory.BUILDING_BLOCKS, item)
                                        .define('#', Ingredient.of(getSlab()))
                                        .pattern("#")
                                        .pattern("#")
                                        .unlockedBy(RecipeProvider.getHasName(getBase()), provider.has(getBase()));
                            }
                        })
                )
        );
        return createBlockWithItem(this.getFormattedName() + "_pillar", RotatedPillarBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(this.getBase().get()));
    }
    private SuppliedBlock createWall(){
        if (blocks != null) return createBlock(this.getFormattedName() + "_wall", VanillaBlockTypes.WALL::create, builder -> builder
                .properties(properties -> properties.copyFrom(getBase().key()))
                .assets(assets -> assets.model(BlockAssets.WALL, getBase().get()))
                .data(data -> data
                        .dropSelf()
                        .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                        .recipes((item, provider) -> provider.smeltingResultFromBase(item, getBase()))
                        .tag(BlockTags.WALLS)
                        .itemTag(ItemTags.WALLS)
                )
        );
        return createBlockWithItem(this.getFormattedName() + "_wall", WallBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(getBase().get()));
    }

    public boolean hasStairs(){
        return this.getSettings().hasStairs;
    }
    public boolean hasSlab(){
        return this.getSettings().hasSlab;
    }
    public boolean hasWall(){
        return this.getSettings().hasWall;
    }
    public boolean hasChiseled(){
        return this.getSettings().hasChiseled;
    }
    public boolean hasCracked(){
        return this.getSettings().hasCracked;
    }
    public boolean hasPillar(){
        return this.getSettings().hasPillar;
    }
    public boolean hasFence(){
        return this.getSettings().hasFence;
    }
    public boolean hasPressurePlate(){
        return this.getSettings().hasPressurePlate;
    }
    public boolean hasButton(){
        return this.getSettings().hasButton;
    }

    private String getFormattedName() {
        String path = this.getId().getPath();
        if (path.endsWith("bricks") || path.endsWith("tiles") || getSettings().hasPluralName) path = path.substring(0, path.length() - "s".length());
        return path;
    }

    public Supplier<BlockSetType> getBlockSetType() {
        if (this.blockSetType == null) {
            this.blockSetType = () -> new BlockSetType(
                    id.toString(),
                    true,
                    true,
                    this.settings.canArrowsActivateButton,
                    this.settings.pressurePlateSensitivity,
                    this.settings.soundType.get(),
                    SoundEvents.IRON_DOOR_OPEN,
                    SoundEvents.IRON_DOOR_CLOSE,
                    SoundEvents.IRON_TRAPDOOR_OPEN,
                    SoundEvents.IRON_TRAPDOOR_CLOSE,
                    this.settings.pressurePlateSounds.getSecond().get(),
                    this.settings.pressurePlateSounds.getFirst().get(),
                    this.settings.buttonSounds.getSecond().get(),
                    this.settings.buttonSounds.getFirst().get()
            );
        }
        return this.blockSetType;
    }

    public static class Settings implements Cloneable {

        private float destroyTime = 1.5F;
        private float explosionResistance = 6F;

        private boolean hasStairs = true;
        private boolean hasSlab = true;
        private boolean hasWall = true;
        private boolean hasChiseled = false;
        private boolean hasCracked = false;
        private boolean hasFence = false;
        private boolean hasPressurePlate = false;
        private boolean hasButton = false;
        private boolean hasPillar = false;

        private boolean hasPluralName = false;
        private boolean hasLegacySlab = false;

        private boolean canArrowsActivateButton = true;
        private BlockSetType.PressurePlateSensitivity pressurePlateSensitivity = BlockSetType.PressurePlateSensitivity.EVERYTHING;

        private Pair<Supplier<? extends ItemLike>, Integer> fenceStickReplacement = Pair.of(() -> Items.STICK, 3);

        @Deprecated private Function<BlockBehaviour.Properties, Block> baseBlockFunction = Block::new;

        private ExtensibleCodec.Entry<Function<BlockBehaviour.Properties, ? extends Block>> baseBlockType = VanillaBlockTypes.BLOCK.create();
        private Consumer<UnifiedDataRegistries.Blocks.Assets> baseBlockModel = assets -> assets.model(BlockAssets.SIMPLE_CUBE);
        private Optional<TagKey<Block>> baseBlockTag = Optional.empty();
        private Optional<String> baseBlockSuffix = Optional.empty();

        private Supplier<SoundType> soundType = () -> SoundType.STONE;
        private Pair<Supplier<SoundEvent>, Supplier<SoundEvent>> buttonSounds = Pair.of(() -> SoundEvents.WOODEN_BUTTON_CLICK_ON, () -> SoundEvents.WOODEN_BUTTON_CLICK_OFF);
        private Pair<Supplier<SoundEvent>, Supplier<SoundEvent>> pressurePlateSounds = Pair.of(() -> SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_ON, () -> SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_OFF);

        private @Nullable PrecedingCreativeEntries precedingCreativeEntries = null;

        Settings() {}

        public float getDestroyTime() {
            return destroyTime;
        }

        public float getExplosionResistance() {
            return explosionResistance;
        }

        public Supplier<SoundType> getSoundType() {
            return soundType;
        }
        public Pair<Supplier<SoundEvent>, Supplier<SoundEvent>> getButtonSounds() {
            return buttonSounds;
        }
        public Pair<Supplier<SoundEvent>, Supplier<SoundEvent>> getPressurePlateSounds() {
            return pressurePlateSounds;
        }

        public boolean canArrowsActivateButton() {
            return canArrowsActivateButton;
        }
        public BlockSetType.PressurePlateSensitivity getPressurePlateSensitivity() {
            return pressurePlateSensitivity;
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
        private final MapColor color;

        private final UnifiedDataRegistries.Blocks blocks;
        @Deprecated private final UnifiedRegistries.Blocks blockRegistry;

        public BlockSet build() {
            return new BlockSet(id, color, settings, blocks, blockRegistry);
        }

        public RegistryBuilder(Identifier id, MapColor color, BlockPreset preset, UnifiedDataRegistries.Blocks blocks, UnifiedRegistries.Blocks blockRegistry) {
            super(preset.settings.copy());

            this.id = id;
            this.color = color;
            this.blockRegistry = blockRegistry;
            this.blocks = blocks;
        }

        public RegistryBuilder(Identifier id, MapColor color, float hardness, float blastResistance, BlockPreset preset, UnifiedRegistries.Blocks blockRegistry) {
            Settings settings = preset.settings.copy();
            settings.destroyTime = hardness;
            settings.explosionResistance = blastResistance;
            super(settings.copy());

            this.id = id;
            this.color = color;
            this.blockRegistry = blockRegistry;
            this.blocks = null;
        }
    }

    public static class PresetBuilder extends BlockSet.Builder<PresetBuilder> {

        public PresetBuilder() {
            super();
        }

        public PresetBuilder(Settings settings) {
            super(settings);
        }

        public BlockPreset build() {
            return new BlockPreset(settings.copy());
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

        public T creativeInventoryPlacement(Supplier<? extends ItemLike> precedingBuildingItem) {
            settings.precedingCreativeEntries = new PrecedingCreativeEntries(precedingBuildingItem, null);
            return self();
        }
        public T creativeInventoryPlacement(Supplier<? extends ItemLike> precedingBuildingItem, Supplier<? extends ItemLike> precedingNaturalItem) {
            settings.precedingCreativeEntries = new PrecedingCreativeEntries(
                    precedingBuildingItem,
                    precedingNaturalItem
            );
            return self();
        }

        public T setDestroyTime(float destroyTime) {
            settings.destroyTime = destroyTime;
            return self();
        }

        public T setExplosionResistance(float explosionResistance) {
            settings.explosionResistance = explosionResistance;
            return self();
        }

        public T setSoundType(Supplier<SoundType> soundType) {
            settings.soundType = soundType;
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

        public T hasChiseled(boolean hasChiseled) {
            settings.hasChiseled = hasChiseled;
            return self();
        }

        public T hasButton(boolean hasButton) {
            settings.hasButton = hasButton;
            return self();
        }

        public T hasCracked(boolean hasCracked) {
            settings.hasCracked = hasCracked;
            return self();
        }

        public T hasFence(boolean hasFence) {
            settings.hasFence = hasFence;
            return self();
        }

        public T hasPillar(boolean hasPillar) {
            settings.hasPillar = hasPillar;
            return self();
        }

        public T hasPressurePlate(boolean hasPressurePlate) {
            settings.hasPressurePlate = hasPressurePlate;
            return self();
        }

        public T hasSlab(boolean hasSlab) {
            settings.hasSlab = hasSlab;
            return self();
        }

        public T hasStairs(boolean hasStairs) {
            settings.hasStairs = hasStairs;
            return self();
        }

        public T hasWall(boolean hasWall) {
            settings.hasWall = hasWall;
            return self();
        }

        public T hasPluralName(boolean hasPluralName) {
            settings.hasPluralName = hasPluralName;
            return self();
        }

        public T hasLegacySlab(boolean hasLegacySlab) {
            settings.hasLegacySlab = hasLegacySlab;
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

        public T alternateFenceRecipe(Supplier<? extends ItemLike> stickReplacement) {
            return alternateFenceRecipe(stickReplacement, 3);
        }
        public T alternateFenceRecipe(Supplier<? extends ItemLike> stickReplacement, int output) {
            settings.fenceStickReplacement = Pair.of(stickReplacement, output);
            return self();
        }

        @Deprecated
        public T baseBlockFunction(Function<BlockBehaviour.Properties, Block> baseBlockFunction) {
            settings.baseBlockFunction = baseBlockFunction;
            return self();
        }

        public T baseBlockType(ExtensibleCodec.Entry<Function<BlockBehaviour.Properties, ? extends Block>> type, BlockAsset<Void> model) {
            settings.baseBlockType = type;
            settings.baseBlockModel = assets -> assets.model(model);
            return self();
        }
        public <Y> T baseBlockType(ExtensibleCodec.Entry<Function<BlockBehaviour.Properties, ? extends Block>> type, BlockAsset<Y> model, Y value) {
            settings.baseBlockType = type;
            settings.baseBlockModel = assets -> assets.model(model, value);
            return self();
        }

        public <Y> T baseBlockTag(ExtensibleCodec.Entry<Function<BlockBehaviour.Properties, ? extends Block>> type, BlockAsset<Y> model, Y value) {
            settings.baseBlockType = type;
            settings.baseBlockModel = assets -> assets.model(model, value);
            return self();
        }

        public T baseBlockSuffix(Optional<String> baseBlockSuffix) {
            settings.baseBlockSuffix = baseBlockSuffix;
            return self();
        }
    }

    public record PrecedingCreativeEntries(Supplier<? extends ItemLike> building, @Nullable Supplier<? extends ItemLike> natural) {}
}
