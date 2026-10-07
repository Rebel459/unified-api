package net.rebel459.unified.api.data.set;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockItemTagId;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.MapColor;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.api.core.UnifiedPlatform;
import net.rebel459.unified.api.core.SuppliedBlock;
import net.rebel459.unified.api.asset.BlockAsset;
import net.rebel459.unified.api.asset.BlockAssets;
import net.rebel459.unified.api.data.helper.CreativeEntryGenerator;
import net.rebel459.unified.api.data.registry.BlockGenerator;
import net.rebel459.unified.api.data.registry.BlockSetTypeGenerator;
import net.rebel459.unified.api.platform.ModLoader;
import net.rebel459.unified.api.registry.VanillaBlockCodecs;
import net.rebel459.unified.impl.data.set.StoneSetProperties;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class StoneSet {

    public static final List<StoneSet> BLOCK_SETS = new CopyOnWriteArrayList<>();

    private final List<SuppliedBlock> registeredBlocks = new ArrayList<>();

    private final Identifier id;
    private final MapColor color;

    private final BlockGenerator blocks;

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

    public StoneSet(Identifier id, MapColor color, Settings settings, BlockGenerator blocks, BlockSetTypeGenerator blockSetTypes, CreativeEntryGenerator creativeEntries) {
        this.settings = settings;
        this.id = id;
        this.color = color;
        this.blocks = blocks;
        blockSetTypes.register(id.getPath(), () -> new BlockSetType(
                id.toString(),
                true,
                true,
                settings.canArrowsActivateButton,
                settings.pressurePlateSensitivity,
                settings.soundType.get(),
                SoundEvents.IRON_DOOR_CLOSE,
                SoundEvents.IRON_DOOR_OPEN,
                SoundEvents.IRON_TRAPDOOR_CLOSE,
                SoundEvents.IRON_TRAPDOOR_OPEN,
                settings.pressurePlateSounds.getSecond().get(),
                settings.pressurePlateSounds.getFirst().get(),
                settings.buttonSounds.getSecond().get(),
                settings.buttonSounds.getFirst().get()
        ));
        registerBlocks();
        BLOCK_SETS.add(this);
        StoneSetProperties.CREATIVE_ENTRIES.put(id, getSettings().precedingCreativeEntries);
        StoneSetProperties.CREATIVE_ENTRY_GENERATORS.put(id, creativeEntries);
        if (UnifiedPlatform.getModLoader() == ModLoader.FABRIC) {
            UnifiedPlatform.executeAfter(Registries.ITEM, () -> StoneSetProperties.init(List.of(this)));
        }
    }

    private SuppliedBlock createBlock(String path, ExtensibleCodec.Entry<Function<BlockBehaviour.Properties, ? extends Block>> type, Consumer<BlockGenerator.Builder> builder){
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
        if (!getSettings().baseBlockSuffix.isEmpty()) name = name + "_" + getSettings().baseBlockSuffix;
        return createBlock(name, getSettings().baseBlockType, builder -> builder
                .properties(properties -> properties
                        .copyFrom(() -> Blocks.STONE)
                        .soundType(getSettings().soundType.get())
                        .mapColor(color)
                        .strength(getSettings().destroyTime, getSettings().explosionResistance)
                )
                .assets(getSettings().baseBlockModel)
                .data(data -> {
                    getSettings().baseBlockTag.ifPresent(data::tag);
                            getSettings().baseBlockTag.ifPresent(data::tag);
                            data.dropSelf();
                            data.tag(BlockTags.MINEABLE_WITH_PICKAXE);
                        }
                )
        );
    }
    private SuppliedBlock createStairs() {
        return createBlock(this.getFormattedName() + "_stairs", VanillaBlockCodecs.STAIRS.create(getBase()::get), builder -> builder
                .properties(properties -> properties.copyFrom(getBase()))
                .assets(assets -> assets.model(BlockAssets.STAIRS, getBase().get()))
                .data(data -> data
                        .dropSelf()
                        .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                        .recipes((item, provider) -> provider.stairBuilder(item, Ingredient.of(getBase().get()))
                                .unlockedBy(RecipeProvider.getHasName(getBase()), provider.has(getBase()))
                                .save(provider.output))
                        .tag(BlockTags.STAIRS)
                )
        );
    }
    private SuppliedBlock createSlab(){
        Supplier<BlockBehaviour.Properties> blockProperties = () -> BlockBehaviour.Properties.ofFullCopy(getBase().get());
        if (getSettings().hasLegacySlab) blockProperties = () -> BlockBehaviour.Properties.ofFullCopy(getBase().get()).strength(2F, 6F);
        return createBlock(this.getFormattedName() + "_slab", VanillaBlockCodecs.SLAB.create(), builder -> builder
                .properties(properties -> {
                    properties.copyFrom(getBase());
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
                )
        );
    }
    private SuppliedBlock createFence(){
        return createBlock(this.getFormattedName() + "_fence", VanillaBlockCodecs.FENCE.create(), builder -> builder
                .properties(properties -> properties.copyFrom(getBase()))
                .assets(assets -> assets.model(BlockAssets.FENCE, getBase().get()))
                .data(data -> data
                        .dropSelf()
                        .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                        .recipes((item, provider) -> provider.shaped(RecipeCategory.DECORATIONS, item, getSettings().fenceStickReplacement.getSecond()).define('W', getBase()).define('#', getSettings().fenceStickReplacement.getFirst().get()).pattern("W#W").pattern("W#W")
                                .unlockedBy(RecipeProvider.getHasName(getBase()), provider.has(getBase()))
                                .save(provider.output))
                        .tag(BlockTags.FENCES)
                )
        );
    }
    private SuppliedBlock createPressurePlate(){
        return createBlock(this.getFormattedName() + "_pressure_plate", VanillaBlockCodecs.PRESSURE_PLATE.create(() -> this.getBlockSetType().get()), builder -> builder
                .properties(properties -> properties.copyFrom(getBase()))
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
    }
    private SuppliedBlock createButton(){
        return createBlock(this.getFormattedName() + "_button", VanillaBlockCodecs.BUTTON.create(() -> new VanillaBlockCodecs.Button(this.getBlockSetType().get(), 30)), builder -> builder
                .properties(properties -> properties.copyFrom(getBase()))
                .assets(assets -> assets.model(BlockAssets.BUTTON, getBase().get()))
                .data(data -> data
                        .dropSelf()
                        .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                        .recipes((item, provider) -> provider.buttonBuilder(item, Ingredient.of(getBase().get()))
                                .unlockedBy(RecipeProvider.getHasName(getBase()), provider.has(getBase()))
                                .save(provider.output))
                        .tag(BlockTags.BUTTONS)
                )
        );
    }
    private SuppliedBlock createChiseled(){
        return createBlock("chiseled_" + this.getId().getPath(), VanillaBlockCodecs.BLOCK.create(), builder -> builder
                .properties(properties -> properties.copyFrom(getBase()))
                .assets(assets -> assets.model(BlockAssets.SIMPLE_CUBE))
                .data(data -> data
                        .dropSelf()
                        .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                        .recipes((item, provider) -> provider.chiseledBuilder(RecipeCategory.BUILDING_BLOCKS, item, Ingredient.of(getBase().get()))
                                .unlockedBy(RecipeProvider.getHasName(getBase()), provider.has(getBase()))
                                .save(provider.output))
                )
        );
    }
    private SuppliedBlock createCracked(){
        return createBlock("cracked_" + this.getId().getPath(), VanillaBlockCodecs.BLOCK.create(), builder -> builder
                .properties(properties -> properties.copyFrom(getBase()))
                .assets(assets -> assets.model(BlockAssets.SIMPLE_CUBE))
                .data(data -> data
                        .dropSelf()
                        .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                        .recipes((item, provider) -> provider.smeltingResultFromBase(item, getBase()))
                )
        );
    }
    private SuppliedBlock createPillar(){
        return createBlock(this.getFormattedName() + "_pillar", VanillaBlockCodecs.ROTATED_PILLAR_BLOCK.create(), builder -> builder
                .properties(properties -> properties.copyFrom(getBase()))
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
    }
    private SuppliedBlock createWall(){
        return createBlock(this.getFormattedName() + "_wall", VanillaBlockCodecs.WALL.create(), builder -> builder
                .properties(properties -> properties.copyFrom(getBase()))
                .assets(assets -> assets.model(BlockAssets.WALL, getBase().get()))
                .data(data -> data
                        .dropSelf()
                        .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                        .recipes((item, provider) -> provider.smeltingResultFromBase(item, getBase()))
                        .tag(BlockTags.WALLS)
                        .itemTag(ItemTags.WALLS)
                )
        );
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

        private ExtensibleCodec.Entry<Function<BlockBehaviour.Properties, ? extends Block>> baseBlockType = VanillaBlockCodecs.BLOCK.create();
        private Consumer<BlockGenerator.Assets> baseBlockModel = assets -> assets.model(BlockAssets.SIMPLE_CUBE);
        private Optional<BlockItemTagId> baseBlockTag = Optional.empty();
        private String baseBlockSuffix = "";

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

        Settings copy() {
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

        private final BlockGenerator blocks;
        private final BlockSetTypeGenerator blockSetTypes;
        private final CreativeEntryGenerator creativeEntries;

        public StoneSet build() {
            return new StoneSet(id, color, settings, blocks, blockSetTypes, creativeEntries);
        }

        public RegistryBuilder(Identifier id, MapColor color, StonePreset preset, BlockGenerator blocks, BlockSetTypeGenerator blockSetTypes, CreativeEntryGenerator creativeEntries) {
            super(preset.settings.copy());

            this.id = id;
            this.color = color;
            this.blocks = blocks;
            this.blockSetTypes = blockSetTypes;
            this.creativeEntries = creativeEntries;
        }
    }

    public static class PresetBuilder extends StoneSet.Builder<PresetBuilder> {

        public PresetBuilder() {
            super();
        }

        public PresetBuilder(Settings settings) {
            super(settings);
        }

        public StonePreset build() {
            return new StonePreset(settings.copy());
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

        public T baseBlockTag(Optional<BlockItemTagId> tag) {
            settings.baseBlockTag = tag;
            return self();
        }

        public T baseBlockSuffix(String baseBlockSuffix) {
            settings.baseBlockSuffix = baseBlockSuffix;
            return self();
        }
    }

    public record PrecedingCreativeEntries(Supplier<? extends ItemLike> building, @Nullable Supplier<? extends ItemLike> natural) {}
}
