package net.rebel459.unified.util.builder;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.MapColor;
import net.rebel459.unified.platform.UnifiedPlatform;
import net.rebel459.unified.platform.UnifiedRegistries;
import net.rebel459.unified.util.LoaderType;
import net.rebel459.unified.util.registry.SuppliedBlock;
import net.rebel459.unified.util.builder.impl.BlockSetImpl;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class BlockSet {

    public static final List<BlockSet> BLOCK_SETS = new ArrayList<>();

    private final List<SuppliedBlock> registeredBlocks = new ArrayList<>();

    private final Identifier id;
    private final MapColor color;
    private final float hardness;
    private final float blastResistance;

    private final UnifiedRegistries.Blocks blockRegistry;

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

    public BlockSet(Identifier id, MapColor color, float hardness, float blastResistance, Settings settings, UnifiedRegistries.Blocks blockRegistry){
        this.settings = settings;
        this.id = id;
        this.color = color;
        this.hardness = hardness;
        this.blastResistance = blastResistance;
        this.blockRegistry = blockRegistry;
        registerBlocks();
        BLOCK_SETS.add(this);
        BlockSetImpl.CREATIVE_ENTRIES.put(id, getSettings().precedingCreativeEntries);
        if (UnifiedPlatform.getLoader() == LoaderType.FABRIC) BlockSetImpl.init(List.of(this));
    }

    private SuppliedBlock createBlockWithItem(String blockID, Supplier<BlockBehaviour.Properties> settings){
        return createBlockWithItem(blockID, Block::new, settings);
    }
	private SuppliedBlock createBlockWithItem(String blockID, Function<BlockBehaviour.Properties, Block> factory, Supplier<BlockBehaviour.Properties> settings){
		SuppliedBlock block = blockRegistry.register(blockID, factory, settings);
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
        return createBlockWithItem(this.getId().getPath(), getSettings().baseBlockFunction, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).sound(getSettings().getSoundType().get()).mapColor(color).strength(hardness, blastResistance));
    }
    private SuppliedBlock createStairs(){
        return createBlockWithItem(this.getFormattedName() + "_stairs", settings -> new StairBlock(getBase().defaultBlockState(), settings), () -> BlockBehaviour.Properties.ofFullCopy(getBase().get()));
    }
    private SuppliedBlock createSlab(){
        return createBlockWithItem(this.getFormattedName() + "_slab", SlabBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(getBase().get()));
    }
    private SuppliedBlock createFence(){
        return createBlockWithItem(this.getFormattedName() + "_fence", FenceBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(getBase().get()));
    }
    private SuppliedBlock createPressurePlate(){
        return createBlockWithItem(this.getFormattedName() + "_pressure_plate", settings -> new PressurePlateBlock(this.getBlockSetType().get(), settings), () -> BlockBehaviour.Properties.ofFullCopy(getBase().get()));
    }
    private SuppliedBlock createButton(){
        return createBlockWithItem(this.getFormattedName() + "_button", settings -> new ButtonBlock(this.getBlockSetType().get(), 30, settings), () -> BlockBehaviour.Properties.ofFullCopy(getBase().get()));
    }
    private SuppliedBlock createChiseled(){
        return createBlockWithItem("chiseled_" + this.getId().getPath(), () -> BlockBehaviour.Properties.ofFullCopy(this.getBase().get()));
    }
    private SuppliedBlock createCracked(){
        return createBlockWithItem("cracked_" + this.getId().getPath(), () -> BlockBehaviour.Properties.ofFullCopy(this.getBase().get()));
    }
    private SuppliedBlock createPillar(){
        return createBlockWithItem(this.getFormattedName() + "_pillar", RotatedPillarBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(this.getBase().get()));
    }
    private SuppliedBlock createWall(){
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
        return () -> new BlockSetType(
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

    public static class Settings implements Cloneable {

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

        private boolean canArrowsActivateButton = true;
        private BlockSetType.PressurePlateSensitivity pressurePlateSensitivity = BlockSetType.PressurePlateSensitivity.EVERYTHING;
        private Function<BlockBehaviour.Properties, Block> baseBlockFunction = Block::new;

        private Supplier<SoundType> soundType = () -> SoundType.STONE;
        private Pair<Supplier<SoundEvent>, Supplier<SoundEvent>> buttonSounds = Pair.of(() -> SoundEvents.WOODEN_BUTTON_CLICK_ON, () -> SoundEvents.WOODEN_BUTTON_CLICK_OFF);
        private Pair<Supplier<SoundEvent>, Supplier<SoundEvent>> pressurePlateSounds = Pair.of(() -> SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_ON, () -> SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_OFF);

        private @Nullable PrecedingCreativeEntries precedingCreativeEntries = null;

        Settings() {}

        public Supplier<SoundType> getSoundType() {
            return soundType;
        }
        public Pair<Supplier<SoundEvent>, Supplier<SoundEvent>> getButtonSounds() {
            return buttonSounds;
        }
        public Pair<Supplier<SoundEvent>, Supplier<SoundEvent>> getPressurePlateSounds() {
            return pressurePlateSounds;
        }

        public boolean getCanArrowsActivateButton() {
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
        private final float hardness;
        private final float blastResistance;

        private final UnifiedRegistries.Blocks blockRegistry;

        public BlockSet build() {
            return new BlockSet(id, color, hardness, blastResistance, settings, blockRegistry);
        }

        public RegistryBuilder(Identifier id, MapColor color, float hardness, float blastResistance, BlockPreset preset, UnifiedRegistries.Blocks blockRegistry) {
            super(preset.settings.copy());

            this.id = id;
            this.color = color;
            this.hardness = hardness;
            this.blastResistance = blastResistance;
            this.blockRegistry = blockRegistry;
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

        public T creativeInventoryPlacement(Supplier<ItemLike> precedingBuildingItem) {
            settings.precedingCreativeEntries = new PrecedingCreativeEntries(precedingBuildingItem, null);
            return self();
        }
        public T creativeInventoryPlacement(Supplier<ItemLike> precedingBuildingItem, Supplier<ItemLike> precedingNaturalItem) {
            settings.precedingCreativeEntries = new PrecedingCreativeEntries(
                    precedingBuildingItem,
                    precedingNaturalItem
            );
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

        public T canArrowsActivateButton(boolean canArrowsActivateButton) {
            settings.canArrowsActivateButton = canArrowsActivateButton;
            return self();
        }

        public T setPressurePlateSensitivity(BlockSetType.PressurePlateSensitivity pressurePlateSensitivity) {
            settings.pressurePlateSensitivity = pressurePlateSensitivity;
            return self();
        }

        public T baseBlockFunction(Function<BlockBehaviour.Properties, Block> baseBlockFunction) {
            settings.baseBlockFunction = baseBlockFunction;
            return self();
        }
    }

    public record PrecedingCreativeEntries(Supplier<ItemLike> building, @Nullable Supplier<ItemLike> natural) {}
}