package net.rebel459.unified.api.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.cauldron.CauldronInteractions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ColorRGBA;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.sounds.AmbientLeavesBlockSoundPlayer;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.rebel459.unified.api.codec.BlockType;
import net.rebel459.unified.api.codec.ExtensibleCodecs;
import net.rebel459.unified.api.codec.UnifiedCodecs;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.Optional;

public class VanillaBlockTypes {

    // Common Codecs

    private static final Codec<FlowingFluid> FLOWING_FLUID_CODEC = BuiltInRegistries.FLUID.byNameCodec().flatXmap(
            fluid -> fluid instanceof FlowingFluid flowing ? DataResult.success(flowing) : DataResult.error(() -> "Fluid is not a flowing fluid: " + BuiltInRegistries.FLUID.getKey(fluid)),
            DataResult::success
    );
    private static final Codec<SimpleParticleType> SIMPLE_PARTICLE_CODEC = ParticleTypes.CODEC.flatXmap(
            particle -> particle instanceof SimpleParticleType simple ? DataResult.success(simple) : DataResult.error(() -> "Particle is not a simple particle type: " + particle),
            DataResult::success
    );

    private static final MapCodec<Block> BASE_BLOCK_CODEC = BuiltInRegistries.BLOCK.byNameCodec().fieldOf("base_block");
    private static final MapCodec<BlockSetType> BLOCK_SET_TYPE_CODEC = UnifiedCodecs.BLOCK_SET_TYPE.fieldOf("block_set_type");
    private static final MapCodec<Block> DEAD_BLOCK_CODEC = BuiltInRegistries.BLOCK.byNameCodec().fieldOf("dead_block");
    private static final MapCodec<DyeColor> DYE_COLOR_CODEC = DyeColor.CODEC.fieldOf("color");
    private static final MapCodec<ResourceKey<Feature>> FEATURE_CODEC = ResourceKey.codec(Registries.FEATURE).fieldOf("feature");
    private static final MapCodec<Block> FRUIT_CODEC = BuiltInRegistries.BLOCK.byNameCodec().fieldOf("fruit");
    private static final MapCodec<Block> HOST_BLOCK_CODEC = BuiltInRegistries.BLOCK.byNameCodec().fieldOf("host_block");
    private static final MapCodec<Float> LEAF_PARTICLE_CHANCE_CODEC = ExtraCodecs.floatRange(0.0F, 1.0F).fieldOf("leaf_particle_chance");
    private static final MapCodec<SoundEvent> OPEN_SOUND_CODEC = BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("open_sound");
    private static final MapCodec<SoundEvent> CLOSE_SOUND_CODEC = BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("close_sound");
    private static final MapCodec<Item> SEED_CODEC = BuiltInRegistries.ITEM.byNameCodec().fieldOf("seed");
    private static final MapCodec<SimpleParticleType> FLAME_PARTICLE_CODEC = SIMPLE_PARTICLE_CODEC.fieldOf("flame_particle");
    private static final MapCodec<SkullBlock.Type> SKULL_TYPE_CODEC = SkullBlock.Type.CODEC.fieldOf("kind");
    private static final MapCodec<TagKey<Block>> SUPPORT_BLOCKS_CODEC = TagKey.codec(Registries.BLOCK).fieldOf("support_blocks");
    private static final MapCodec<SuspiciousStewEffects> SUSPICIOUS_STEW_EFFECTS_CODEC = SuspiciousStewEffects.CODEC.fieldOf("suspicious_stew_effects");
    private static final MapCodec<TreeGrower> TREE_CODEC = TreeGrower.CODEC.fieldOf("tree");
    private static final MapCodec<WeatheringCopper.WeatherState> WEATHERING_STATE_CODEC = WeatheringCopper.WeatherState.CODEC.fieldOf("weathering_state");
    private static final MapCodec<WoodType> WOOD_TYPE_CODEC = UnifiedCodecs.WOOD_TYPE.fieldOf("wood_type");

    // Registration Helpers

    private static BlockType.Simple simple(String id, Function<BlockBehaviour.Properties, ? extends Block> factory) {
        return ExtensibleCodecs.BLOCK_TYPES.register(Identifier.withDefaultNamespace(id), () -> factory);
    }

    private static <T> BlockType.Complex<T> complex(String id, MapCodec<T> codec, BiFunction<T, BlockBehaviour.Properties, ? extends Block> factory) {
        return ExtensibleCodecs.BLOCK_TYPES.register(
                Identifier.withDefaultNamespace(id),
                codec,
                definition -> properties -> factory.apply(definition, properties)
        );
    }

    // Simple Blocks

    public static final BlockType.Simple BLOCK = simple("block", Block::new);

    public static final BlockType.Simple AIR = simple("air", AirBlock::new);
    public static final BlockType.Simple AMETHYST = simple("amethyst", AmethystBlock::new);
    public static final BlockType.Simple ANVIL = simple("anvil", AnvilBlock::new);
    public static final BlockType.Simple AZALEA = simple("azalea", AzaleaBlock::new);
    public static final BlockType.Simple BAMBOO_SAPLING = simple("bamboo_sapling", BambooSaplingBlock::new);
    public static final BlockType.Simple BAMBOO_STALK = simple("bamboo_stalk", BambooStalkBlock::new);
    public static final BlockType.Simple BARREL = simple("barrel", BarrelBlock::new);
    public static final BlockType.Simple BARRIER = simple("barrier", BarrierBlock::new);
    public static final BlockType.Simple BASE_CORAL_FAN = simple("base_coral_fan", BaseCoralFanBlock::new);
    public static final BlockType.Simple BASE_CORAL_PLANT = simple("base_coral_plant", BaseCoralPlantBlock::new);
    public static final BlockType.Simple BASE_CORAL_WALL_FAN = simple("base_coral_wall_fan", BaseCoralWallFanBlock::new);
    public static final BlockType.Simple BEACON = simple("beacon", BeaconBlock::new);
    public static final BlockType.Simple BEEHIVE = simple("beehive", BeehiveBlock::new);
    public static final BlockType.Simple BEETROOT = simple("beetroot", BeetrootBlock::new);
    public static final BlockType.Simple BELL = simple("bell", BellBlock::new);
    public static final BlockType.Simple BIG_DRIPLEAF = simple("big_dripleaf", BigDripleafBlock::new);
    public static final BlockType.Simple BIG_DRIPLEAF_STEM = simple("big_dripleaf_stem", BigDripleafStemBlock::new);
    public static final BlockType.Simple BLAST_FURNACE = simple("blast_furnace", BlastFurnaceBlock::new);
    public static final BlockType.Simple BREWING_STAND = simple("brewing_stand", BrewingStandBlock::new);
    public static final BlockType.Simple BUBBLE_COLUMN = simple("bubble_column", BubbleColumnBlock::new);
    public static final BlockType.Simple BUDDING_AMETHYST = simple("budding_amethyst", BuddingAmethystBlock::new);
    public static final BlockType.Simple BUSH = simple("bush", BushBlock::new);
    public static final BlockType.Simple CACTUS = simple("cactus", CactusBlock::new);
    public static final BlockType.Simple CACTUS_FLOWER = simple("cactus_flower", CactusFlowerBlock::new);
    public static final BlockType.Simple CAKE = simple("cake", CakeBlock::new);
    public static final BlockType.Simple CALIBRATED_SCULK_SENSOR = simple("calibrated_sculk_sensor", CalibratedSculkSensorBlock::new);
    public static final BlockType.Simple CANDLE = simple("candle", CandleBlock::new);
    public static final BlockType.Simple CARPET = simple("carpet", CarpetBlock::new);
    public static final BlockType.Simple CARROT = simple("carrot", CarrotBlock::new);
    public static final BlockType.Simple CARTOGRAPHY_TABLE = simple("cartography_table", CartographyTableBlock::new);
    public static final BlockType.Simple CARVED_PUMPKIN = simple("carved_pumpkin", CarvedPumpkinBlock::new);
    public static final BlockType.Simple CAULDRON = simple("cauldron", CauldronBlock::new);
    public static final BlockType.Simple CAVE_VINES = simple("cave_vines", CaveVinesBlock::new);
    public static final BlockType.Simple CAVE_VINES_PLANT = simple("cave_vines_plant", CaveVinesPlantBlock::new);
    public static final BlockType.Simple CHAIN = simple("chain", ChainBlock::new);
    public static final BlockType.Simple CHISELED_BOOKSHELF = simple("chiseled_bookshelf", ChiseledBookShelfBlock::new);
    public static final BlockType.Simple CHORUS_PLANT = simple("chorus_plant", ChorusPlantBlock::new);
    public static final BlockType.Simple COCOA = simple("cocoa", CocoaBlock::new);
    public static final BlockType.Simple COMPARATOR = simple("comparator", ComparatorBlock::new);
    public static final BlockType.Simple COMPOSTER = simple("composter", ComposterBlock::new);
    public static final BlockType.Simple CONDUIT = simple("conduit", ConduitBlock::new);
    public static final BlockType.Simple COPPER_BULB = simple("copper_bulb", CopperBulbBlock::new);
    public static final BlockType.Simple CRAFTER = simple("crafter", CrafterBlock::new);
    public static final BlockType.Simple CRAFTING_TABLE = simple("crafting_table", CraftingTableBlock::new);
    public static final BlockType.Simple CREAKING_HEART = simple("creaking_heart", CreakingHeartBlock::new);
    public static final BlockType.Simple CROP = simple("crop", CropBlock::new);
    public static final BlockType.Simple CRYING_OBSIDIAN = simple("crying_obsidian", CryingObsidianBlock::new);
    public static final BlockType.Simple DAYLIGHT_DETECTOR = simple("daylight_detector", DaylightDetectorBlock::new);
    public static final BlockType.Simple DECORATED_POT = simple("decorated_pot", DecoratedPotBlock::new);
    public static final BlockType.Simple DETECTOR_RAIL = simple("detector_rail", DetectorRailBlock::new);
    public static final BlockType.Simple DISPENSER = simple("dispenser", DispenserBlock::new);
    public static final BlockType.Simple DOUBLE_PLANT = simple("double_plant", DoublePlantBlock::new);
    public static final BlockType.Simple DRAGON_EGG = simple("dragon_egg", DragonEggBlock::new);
    public static final BlockType.Simple DRIED_GHAST = simple("dried_ghast", DriedGhastBlock::new);
    public static final BlockType.Simple DROPPER = simple("dropper", DropperBlock::new);
    public static final BlockType.Simple DRY_VEGETATION = simple("dry_vegetation", DryVegetationBlock::new);
    public static final BlockType.Simple ENCHANTING_TABLE = simple("enchanting_table", EnchantingTableBlock::new);
    public static final BlockType.Simple ENDER_CHEST = simple("ender_chest", EnderChestBlock::new);
    public static final BlockType.Simple END_GATEWAY = simple("end_gateway", EndGatewayBlock::new);
    public static final BlockType.Simple END_PORTAL = simple("end_portal", EndPortalBlock::new);
    public static final BlockType.Simple END_PORTAL_FRAME = simple("end_portal_frame", EndPortalFrameBlock::new);
    public static final BlockType.Simple END_ROD = simple("end_rod", EndRodBlock::new);
    public static final BlockType.Simple FENCE = simple("fence", FenceBlock::new);
    public static final BlockType.Simple FIRE = simple("fire", FireBlock::new);
    public static final BlockType.Simple FIREFLY_BUSH = simple("firefly_bush", FireflyBushBlock::new);
    public static final BlockType.Simple FROGSPAWN = simple("frogspawn", FrogspawnBlock::new);
    public static final BlockType.Simple FROSTED_ICE = simple("frosted_ice", FrostedIceBlock::new);
    public static final BlockType.Simple FURNACE = simple("furnace", FurnaceBlock::new);
    public static final BlockType.Simple GLAZED_TERRACOTTA = simple("glazed_terracotta", GlazedTerracottaBlock::new);
    public static final BlockType.Simple GLOW_LICHEN = simple("glow_lichen", GlowLichenBlock::new);
    public static final BlockType.Simple GRASS = simple("grass", GrassBlock::new);
    public static final BlockType.Simple GRINDSTONE = simple("grindstone", GrindstoneBlock::new);
    public static final BlockType.Simple HALF_TRANSPARENT_BLOCK = simple("half_transparent_block", HalfTransparentBlock::new);
    public static final BlockType.Simple HANGING_MOSS = simple("hanging_moss", HangingMossBlock::new);
    public static final BlockType.Simple HANGING_ROOTS = simple("hanging_roots", HangingRootsBlock::new);
    public static final BlockType.Simple HAY = simple("hay", HayBlock::new);
    public static final BlockType.Simple HEAVY_CORE = simple("heavy_core", HeavyCoreBlock::new);
    public static final BlockType.Simple HONEY = simple("honey", HoneyBlock::new);
    public static final BlockType.Simple HOPPER = simple("hopper", HopperBlock::new);
    public static final BlockType.Simple HUGE_MUSHROOM = simple("huge_mushroom", HugeMushroomBlock::new);
    public static final BlockType.Simple ICE = simple("ice", IceBlock::new);
    public static final BlockType.Simple IRON_BARS = simple("iron_bars", IronBarsBlock::new);
    public static final BlockType.Simple JUKEBOX = simple("jukebox", JukeboxBlock::new);
    public static final BlockType.Simple KELP = simple("kelp", KelpBlock::new);
    public static final BlockType.Simple KELP_PLANT = simple("kelp_plant", KelpPlantBlock::new);
    public static final BlockType.Simple LADDER = simple("ladder", LadderBlock::new);
    public static final BlockType.Simple LANTERN = simple("lantern", LanternBlock::new);
    public static final BlockType.Simple LAVA_CAULDRON = simple("lava_cauldron", LavaCauldronBlock::new);
    public static final BlockType.Simple LEAF_LITTER = simple("leaf_litter", LeafLitterBlock::new);
    public static final BlockType.Simple LECTERN = simple("lectern", LecternBlock::new);
    public static final BlockType.Simple LEVER = simple("lever", LeverBlock::new);
    public static final BlockType.Simple LIGHT = simple("light", LightBlock::new);
    public static final BlockType.Simple LIGHTNING_ROD = simple("lightning_rod", LightningRodBlock::new);
    public static final BlockType.Simple LILY_PAD = simple("lily_pad", LilyPadBlock::new);
    public static final BlockType.Simple LOOM = simple("loom", LoomBlock::new);
    public static final BlockType.Simple MAGMA = simple("magma", MagmaBlock::new);
    public static final BlockType.Simple MANGROVE_ROOTS = simple("mangrove_roots", MangroveRootsBlock::new);
    public static final BlockType.Simple MOSSY_CARPET = simple("mossy_carpet", MossyCarpetBlock::new);
    public static final BlockType.Simple MOVING_PISTON = simple("moving_piston", MovingPistonBlock::new);
    public static final BlockType.Simple MUD = simple("mud", MudBlock::new);
    public static final BlockType.Simple MULTIFACE_BLOCK = simple("multiface_block", MultifaceBlock::new);
    public static final BlockType.Simple MYCELIUM = simple("mycelium", MyceliumBlock::new);
    public static final BlockType.Simple NETHER_PORTAL = simple("nether_portal", NetherPortalBlock::new);
    public static final BlockType.Simple NETHERRACK = simple("netherrack", NetherrackBlock::new);
    public static final BlockType.Simple NETHER_SPROUTS = simple("nether_sprouts", NetherSproutsBlock::new);
    public static final BlockType.Simple NETHER_WART = simple("nether_wart", NetherWartBlock::new);
    public static final BlockType.Simple NOTE_BLOCK = simple("note_block", NoteBlock::new);
    public static final BlockType.Simple NYLIUM = simple("nylium", NyliumBlock::new);
    public static final BlockType.Simple OBSERVER = simple("observer", ObserverBlock::new);
    public static final BlockType.Simple PIGLIN_WALL_SKULL = simple("piglin_wall_skull", PiglinWallSkullBlock::new);
    public static final BlockType.Simple PISTON_HEAD = simple("piston_head", PistonHeadBlock::new);
    public static final BlockType.Simple PITCHER_CROP = simple("pitcher_crop", PitcherCropBlock::new);
    public static final BlockType.Simple PLAYER_HEAD = simple("player_head", PlayerHeadBlock::new);
    public static final BlockType.Simple PLAYER_WALL_HEAD = simple("player_wall_head", PlayerWallHeadBlock::new);
    public static final BlockType.Simple POTATO = simple("potato", PotatoBlock::new);
    public static final BlockType.Simple POTENT_SULFUR = simple("potent_sulfur", PotentSulfurBlock::new);
    public static final BlockType.Simple POWDER_SNOW = simple("powder_snow", PowderSnowBlock::new);
    public static final BlockType.Simple POWERED_BLOCK = simple("powered_block", PoweredBlock::new);
    public static final BlockType.Simple POWERED_RAIL = simple("powered_rail", PoweredRailBlock::new);
    public static final BlockType.Simple PUMPKIN = simple("pumpkin", PumpkinBlock::new);
    public static final BlockType.Simple RAIL = simple("rail", RailBlock::new);
    public static final BlockType.Simple REDSTONE_LAMP = simple("redstone_lamp", RedstoneLampBlock::new);
    public static final BlockType.Simple REDSTONE_ORE = simple("redstone_ore", RedStoneOreBlock::new);
    public static final BlockType.Simple REDSTONE_TORCH = simple("redstone_torch", RedstoneTorchBlock::new);
    public static final BlockType.Simple REDSTONE_WALL_TORCH = simple("redstone_wall_torch", RedstoneWallTorchBlock::new);
    public static final BlockType.Simple REDSTONE_WIRE = simple("redstone_wire", RedstoneWireBlock::new);
    public static final BlockType.Simple REPEATER = simple("repeater", RepeaterBlock::new);
    public static final BlockType.Simple RESPAWN_ANCHOR = simple("respawn_anchor", RespawnAnchorBlock::new);
    public static final BlockType.Simple ROOTED_DIRT = simple("rooted_dirt", RootedDirtBlock::new);
    public static final BlockType.Simple ROTATED_PILLAR_BLOCK = simple("rotated_pillar_block", RotatedPillarBlock::new);
    public static final BlockType.Simple SCAFFOLDING = simple("scaffolding", ScaffoldingBlock::new);
    public static final BlockType.Simple SCULK = simple("sculk", SculkBlock::new);
    public static final BlockType.Simple SCULK_CATALYST = simple("sculk_catalyst", SculkCatalystBlock::new);
    public static final BlockType.Simple SCULK_SENSOR = simple("sculk_sensor", SculkSensorBlock::new);
    public static final BlockType.Simple SCULK_SHRIEKER = simple("sculk_shrieker", SculkShriekerBlock::new);
    public static final BlockType.Simple SCULK_VEIN = simple("sculk_vein", SculkVeinBlock::new);
    public static final BlockType.Simple SEAGRASS = simple("seagrass", SeagrassBlock::new);
    public static final BlockType.Simple SEA_PICKLE = simple("sea_pickle", SeaPickleBlock::new);
    public static final BlockType.Simple SHELF = simple("shelf", ShelfBlock::new);
    public static final BlockType.Simple SHELF_MUSHROOM = simple("shelf_mushroom", ShelfMushroomBlock::new);
    public static final BlockType.Simple STRAW_BED = simple("straw_bed", StrawBedBlock::new);
    public static final BlockType.Simple SHORT_DRY_GRASS = simple("short_dry_grass", ShortDryGrassBlock::new);
    public static final BlockType.Simple SLAB = simple("slab", SlabBlock::new);
    public static final BlockType.Simple SLIME_BLOCK = simple("slime_block", SlimeBlock::new);
    public static final BlockType.Simple SMALL_DRIPLEAF = simple("small_dripleaf", SmallDripleafBlock::new);
    public static final BlockType.Simple SMITHING_TABLE = simple("smithing_table", SmithingTableBlock::new);
    public static final BlockType.Simple SMOKER = simple("smoker", SmokerBlock::new);
    public static final BlockType.Simple SNIFFER_EGG = simple("sniffer_egg", SnifferEggBlock::new);
    public static final BlockType.Simple SNOW_LAYER = simple("snow_layer", SnowLayerBlock::new);
    public static final BlockType.Simple SNOWY_BLOCK = simple("snowy_block", SnowyBlock::new);
    public static final BlockType.Simple SOUL_FIRE = simple("soul_fire", SoulFireBlock::new);
    public static final BlockType.Simple SOUL_SAND = simple("soul_sand", SoulSandBlock::new);
    public static final BlockType.Simple SPAWNER = simple("spawner", SpawnerBlock::new);
    public static final BlockType.Simple SPONGE = simple("sponge", SpongeBlock::new);
    public static final BlockType.Simple SPORE_BLOSSOM = simple("spore_blossom", SporeBlossomBlock::new);
    public static final BlockType.Simple STONECUTTER = simple("stonecutter", StonecutterBlock::new);
    public static final BlockType.Simple SUGAR_CANE = simple("sugar_cane", SugarCaneBlock::new);
    public static final BlockType.Simple SWEET_BERRY_BUSH = simple("sweet_berry_bush", SweetBerryBushBlock::new);
    public static final BlockType.Simple TALL_DRY_GRASS = simple("tall_dry_grass", TallDryGrassBlock::new);
    public static final BlockType.Simple TALL_FLOWER = simple("tall_flower", TallFlowerBlock::new);
    public static final BlockType.Simple TALL_GRASS = simple("tall_grass", TallGrassBlock::new);
    public static final BlockType.Simple TALL_SEAGRASS = simple("tall_seagrass", TallSeagrassBlock::new);
    public static final BlockType.Simple TARGET = simple("target", TargetBlock::new);
    public static final BlockType.Simple TINTED_GLASS = simple("tinted_glass", TintedGlassBlock::new);
    public static final BlockType.Simple TNT = simple("tnt", TntBlock::new);
    public static final BlockType.Simple TORCHFLOWER_CROP = simple("torchflower_crop", TorchflowerCropBlock::new);
    public static final BlockType.Simple TRANSPARENT_BLOCK = simple("transparent_block", TransparentBlock::new);
    public static final BlockType.Simple TRAPPED_CHEST = simple("trapped_chest", TrappedChestBlock::new);
    public static final BlockType.Simple TRIAL_SPAWNER = simple("trial_spawner", TrialSpawnerBlock::new);
    public static final BlockType.Simple TRIPWIRE_HOOK = simple("tripwire_hook", TripWireHookBlock::new);
    public static final BlockType.Simple TURTLE_EGG = simple("turtle_egg", TurtleEggBlock::new);
    public static final BlockType.Simple TWISTING_VINES = simple("twisting_vines", TwistingVinesBlock::new);
    public static final BlockType.Simple TWISTING_VINES_PLANT = simple("twisting_vines_plant", TwistingVinesPlantBlock::new);
    public static final BlockType.Simple VAULT = simple("vault", VaultBlock::new);
    public static final BlockType.Simple VINE = simple("vine", VineBlock::new);
    public static final BlockType.Simple WALL = simple("wall", WallBlock::new);
    public static final BlockType.Simple WATERLOGGED_TRANSPARENT_BLOCK = simple("waterlogged_transparent_block", WaterloggedTransparentBlock::new);
    public static final BlockType.Simple WEB = simple("web", WebBlock::new);
    public static final BlockType.Simple WEEPING_VINES = simple("weeping_vines", WeepingVinesBlock::new);
    public static final BlockType.Simple WEEPING_VINES_PLANT = simple("weeping_vines_plant", WeepingVinesPlantBlock::new);
    public static final BlockType.Simple WET_SPONGE = simple("wet_sponge", WetSpongeBlock::new);
    public static final BlockType.Simple WITHER_SKULL = simple("wither_skull", WitherSkullBlock::new);
    public static final BlockType.Simple WITHER_WALL_SKULL = simple("wither_wall_skull", WitherWallSkullBlock::new);

    // Complex Blocks

    public static final BlockType.Complex<Dimensions> AMETHYST_CLUSTER = complex(
            "amethyst_cluster", Dimensions.CODEC, (definition, properties) -> new AmethystClusterBlock(definition.height, definition.width, properties));
    public static final BlockType.Complex<AttachedStem> ATTACHED_STEM = complex(
            "attached_stem", AttachedStem.CODEC, (definition, properties) -> new AttachedStemBlock(
                    definition.stem.builtInRegistryHolder().key(), definition.fruit.builtInRegistryHolder().key(),
                    definition.seed.builtInRegistryHolder().key(), definition.supportBlocks, properties));
    public static final BlockType.Complex<DyeColor> BANNER = complex(
            "banner", DYE_COLOR_CODEC, BannerBlock::new);
    public static final BlockType.Complex<DyeColor> BED = complex(
            "bed", DYE_COLOR_CODEC, BedBlock::new);
    public static final BlockType.Complex<ResourceKey<Feature>> BONEMEALABLE_FEATURE_PLACER_BLOCK = complex(
            "bonemealable_feature_placer_block", FEATURE_CODEC, BonemealableFeaturePlacerBlock::new);
    public static final BlockType.Complex<Brushable> BRUSHABLE_BLOCK = complex(
            "brushable_block", Brushable.CODEC, (definition, properties) -> new BrushableBlock(definition.turnsInto, definition.brushSound, definition.brushCompletedSound, properties));
    public static final BlockType.Complex<Button> BUTTON = complex(
            "button", Button.CODEC, (definition, properties) -> new ButtonBlock(definition.blockSetType, definition.ticksToStayPressed, properties));
    public static final BlockType.Complex<Campfire> CAMPFIRE = complex(
            "campfire", Campfire.CODEC, (definition, properties) -> new CampfireBlock(definition.spawnParticles, definition.fireDamage, properties));
    public static final BlockType.Complex<Block> CANDLE_CAKE = complex(
            "candle_cake", BASE_BLOCK_CODEC, CandleCakeBlock::new);
    public static final BlockType.Complex<WoodType> CEILING_HANGING_SIGN = complex(
            "ceiling_hanging_sign", WOOD_TYPE_CODEC, CeilingHangingSignBlock::new);
    public static final BlockType.Complex<Chest> CHEST = complex(
            "chest", Chest.CODEC, (definition, properties) -> new ChestBlock(() -> BlockEntityTypes.CHEST, definition.openSound, definition.closeSound, properties));
    public static final BlockType.Complex<Block> CHORUS_FLOWER = complex(
            "chorus_flower", BuiltInRegistries.BLOCK.byNameCodec().fieldOf("plant"), ChorusFlowerBlock::new);
    public static final BlockType.Complex<ColorRGBA> COLORED_FALLING_BLOCK = complex(
            "colored_falling_block", ColorRGBA.CODEC.fieldOf("color"), ColoredFallingBlock::new);
    public static final BlockType.Complex<Block> CONCRETE_POWDER = complex(
            "concrete_powder", BuiltInRegistries.BLOCK.byNameCodec().fieldOf("concrete"), ConcretePowderBlock::new);
    public static final BlockType.Complex<WeatheringChest> COPPER_CHEST = complex(
            "copper_chest", WeatheringChest.CODEC, (definition, properties) -> new CopperChestBlock(definition.weatherState, definition.openSound, definition.closeSound, properties));
    public static final BlockType.Complex<WeatheringCopper.WeatherState> COPPER_GOLEM_STATUE = complex(
            "copper_golem_statue", WEATHERING_STATE_CODEC, CopperGolemStatueBlock::new);
    public static final BlockType.Complex<Block> CORAL = complex("coral", DEAD_BLOCK_CODEC, CoralBlock::new);
    public static final BlockType.Complex<Block> CORAL_FAN = complex("coral_fan", DEAD_BLOCK_CODEC, CoralFanBlock::new);
    public static final BlockType.Complex<Block> CORAL_PLANT = complex("coral_plant", DEAD_BLOCK_CODEC, CoralPlantBlock::new);
    public static final BlockType.Complex<Block> CORAL_WALL_FAN = complex("coral_wall_fan", DEAD_BLOCK_CODEC, CoralWallFanBlock::new);
    public static final BlockType.Complex<BlockSetType> DOOR = complex(
            "door", BLOCK_SET_TYPE_CODEC, DoorBlock::new);
    public static final BlockType.Complex<IntProvider> DROP_EXPERIENCE_BLOCK = complex(
            "drop_experience_block", IntProviders.codec(0, 10).fieldOf("experience"), DropExperienceBlock::new);
    public static final BlockType.Complex<Boolean> EYEBLOSSOM = complex(
            "eyeblossom", Codec.BOOL.fieldOf("open"), EyeblossomBlock::new);
    public static final BlockType.Complex<Block> FARMLAND = complex("farmland", BASE_BLOCK_CODEC, FarmlandBlock::new);
    public static final BlockType.Complex<WoodType> FENCE_GATE = complex(
            "fence_gate", WOOD_TYPE_CODEC, FenceGateBlock::new);
    public static final BlockType.Complex<SuspiciousStewEffects> FLOWER = complex(
            "flower", SUSPICIOUS_STEW_EFFECTS_CODEC, FlowerBlock::new);
    public static final BlockType.Complex<Integer> FLOWER_BED = complex(
            "flower_bed", ExtraCodecs.NON_NEGATIVE_INT.fieldOf("height"), (definition, properties) -> new FlowerBedBlock(properties, definition));
    public static final BlockType.Complex<Block> FLOWER_POT = complex(
            "flower_pot", BuiltInRegistries.BLOCK.byNameCodec().fieldOf("potted"), FlowerPotBlock::new);
    public static final BlockType.Complex<Block> INFESTED_BLOCK = complex("infested_block", HOST_BLOCK_CODEC, InfestedBlock::new);
    public static final BlockType.Complex<Block> INFESTED_ROTATED_PILLAR = complex("infested_rotated_pillar", HOST_BLOCK_CODEC, InfestedRotatedPillarBlock::new);
    public static final BlockType.Complex<LayeredCauldron> LAYERED_CAULDRON = complex(
            "layered_cauldron", LayeredCauldron.CODEC, (definition, properties) -> new LayeredCauldronBlock(definition.precipitation, definition.interactions, properties));
    public static final BlockType.Complex<FlowingFluid> LIQUID = complex(
            "liquid", FLOWING_FLUID_CODEC.fieldOf("fluid"), LiquidBlock::new);
    public static final BlockType.Complex<Float> MANGROVE_LEAVES = complex(
            "mangrove_leaves", LEAF_PARTICLE_CHANCE_CODEC, MangroveLeavesBlock::new);
    public static final BlockType.Complex<TreeGrower> MANGROVE_PROPAGULE = complex(
            "mangrove_propagule", TREE_CODEC, MangrovePropaguleBlock::new);
    public static final BlockType.Complex<ResourceKey<Feature>> MUSHROOM = complex(
            "mushroom", FEATURE_CODEC, MushroomBlock::new);
    public static final BlockType.Complex<NetherFungus> NETHER_FUNGUS = complex(
            "nether_fungus", NetherFungus.CODEC, (definition, properties) -> new NetherFungusBlock(definition.feature, definition.requiredBlock, definition.supportBlocks, properties));
    public static final BlockType.Complex<TagKey<Block>> NETHER_ROOTS = complex(
            "nether_roots", SUPPORT_BLOCKS_CODEC, NetherRootsBlock::new);
    public static final BlockType.Complex<Block> PATH_BLOCK = complex("path_block", BASE_BLOCK_CODEC, PathBlock::new);
    public static final BlockType.Complex<Boolean> PISTON_BASE = complex(
            "piston_base", Codec.BOOL.fieldOf("sticky"), PistonBaseBlock::new);
    public static final BlockType.Complex<Block> POINTED_DRIPSTONE = complex(
            "pointed_dripstone", BASE_BLOCK_CODEC, (definition, properties) -> new PointedDripstoneBlock(definition.defaultBlockState(), properties));
    public static final BlockType.Complex<BlockSetType> PRESSURE_PLATE = complex(
            "pressure_plate", BLOCK_SET_TYPE_CODEC, PressurePlateBlock::new);
    public static final BlockType.Complex<ColorRGBA> SAND = complex(
            "sand", ColorRGBA.CODEC.fieldOf("falling_dust_color"), SandBlock::new);
    public static final BlockType.Complex<TreeGrower> SAPLING = complex(
            "sapling", TREE_CODEC, SaplingBlock::new);
    public static final BlockType.Complex<Optional<DyeColor>> SHULKER_BOX = complex(
            "shulker_box", DyeColor.CODEC.optionalFieldOf("color"), (color, properties) -> new ShulkerBoxBlock(color.orElse(null), properties));
    public static final BlockType.Complex<SkullBlock.Type> SKULL = complex(
            "skull", SKULL_TYPE_CODEC, SkullBlock::new);
    public static final BlockType.Complex<DyeColor> STAINED_GLASS = complex(
            "stained_glass", DYE_COLOR_CODEC, StainedGlassBlock::new);
    public static final BlockType.Complex<DyeColor> STAINED_GLASS_PANE = complex(
            "stained_glass_pane", DYE_COLOR_CODEC, StainedGlassPaneBlock::new);
    public static final BlockType.Complex<Block> STAIRS = complex(
            "stairs", BASE_BLOCK_CODEC, (block, properties) -> new StairBlock(block.defaultBlockState(), properties));
    public static final BlockType.Complex<WoodType> STANDING_SIGN = complex(
            "standing_sign", WOOD_TYPE_CODEC, StandingSignBlock::new);
    public static final BlockType.Complex<Stem> STEM = complex(
            "stem", Stem.CODEC, (definition, properties) -> new StemBlock(
                    definition.fruit.builtInRegistryHolder().key(), definition.attachedStem.builtInRegistryHolder().key(),
                    definition.seed.builtInRegistryHolder().key(), definition.stemSupportBlocks,
                    definition.fruitSupportBlocks, properties));
    public static final BlockType.Complex<BlockState> SULFUR_SPIKE = complex(
            "sulfur_spike", BlockState.CODEC.fieldOf("block_state_to_grow_on"), SulfurSpikeBlock::new);
    public static final BlockType.Complex<Float> TINTED_PARTICLE_LEAVES = complex(
            "tinted_particle_leaves", LEAF_PARTICLE_CHANCE_CODEC, TintedParticleLeavesBlock::new);
    public static final BlockType.Complex<SimpleParticleType> TORCH = complex(
            "torch", FLAME_PARTICLE_CODEC, TorchBlock::new);
    public static final BlockType.Complex<BlockSetType> TRAPDOOR = complex(
            "trapdoor", BLOCK_SET_TYPE_CODEC, TrapDoorBlock::new);
    public static final BlockType.Complex<Block> TRIPWIRE = complex(
            "tripwire", BuiltInRegistries.BLOCK.byNameCodec().fieldOf("hook"), TripWireBlock::new);
    public static final BlockType.Complex<ParticleLeaves> UNTINTED_PARTICLE_LEAVES = complex(
            "untinted_particle_leaves", ParticleLeaves.CODEC, (definition, properties) -> new UntintedParticleLeavesBlock(definition.chance, definition.particle, definition.player, properties));
    public static final BlockType.Complex<DyeColor> WALL_BANNER = complex(
            "wall_banner", DYE_COLOR_CODEC, WallBannerBlock::new);
    public static final BlockType.Complex<WoodType> WALL_HANGING_SIGN = complex(
            "wall_hanging_sign", WOOD_TYPE_CODEC, WallHangingSignBlock::new);
    public static final BlockType.Complex<WoodType> WALL_SIGN = complex(
            "wall_sign", WOOD_TYPE_CODEC, WallSignBlock::new);
    public static final BlockType.Complex<SkullBlock.Type> WALL_SKULL = complex(
            "wall_skull", SKULL_TYPE_CODEC, WallSkullBlock::new);
    public static final BlockType.Complex<SimpleParticleType> WALL_TORCH = complex(
            "wall_torch", FLAME_PARTICLE_CODEC, WallTorchBlock::new);
    public static final BlockType.Complex<WeatheringCopper.WeatherState> WEATHERING_COPPER_BARS = complex("weathering_copper_bars", WEATHERING_STATE_CODEC, WeatheringCopperBarsBlock::new);
    public static final BlockType.Complex<WeatheringCopper.WeatherState> WEATHERING_COPPER_BULB = complex("weathering_copper_bulb", WEATHERING_STATE_CODEC, WeatheringCopperBulbBlock::new);
    public static final BlockType.Complex<WeatheringCopper.WeatherState> WEATHERING_COPPER_CHAIN = complex("weathering_copper_chain", WEATHERING_STATE_CODEC, WeatheringCopperChainBlock::new);
    public static final BlockType.Complex<WeatheringChest> WEATHERING_COPPER_CHEST = complex(
            "weathering_copper_chest", WeatheringChest.CODEC, (definition, properties) -> new WeatheringCopperChestBlock(definition.weatherState, definition.openSound, definition.closeSound, properties));
    public static final BlockType.Complex<WeatheringSet> WEATHERING_COPPER_DOOR = complex(
            "weathering_copper_door", WeatheringSet.CODEC, (definition, properties) -> new WeatheringCopperDoorBlock(definition.blockSetType, definition.weatherState, properties));
    public static final BlockType.Complex<WeatheringCopper.WeatherState> WEATHERING_COPPER_FULL_BLOCK = complex("weathering_copper_full_block", WEATHERING_STATE_CODEC, WeatheringCopperFullBlock::new);
    public static final BlockType.Complex<WeatheringCopper.WeatherState> WEATHERING_COPPER_GOLEM_STATUE = complex("weathering_copper_golem_statue", WEATHERING_STATE_CODEC, WeatheringCopperGolemStatueBlock::new);
    public static final BlockType.Complex<WeatheringCopper.WeatherState> WEATHERING_COPPER_GRATE = complex("weathering_copper_grate", WEATHERING_STATE_CODEC, WeatheringCopperGrateBlock::new);
    public static final BlockType.Complex<WeatheringCopper.WeatherState> WEATHERING_COPPER_SLAB = complex("weathering_copper_slab", WEATHERING_STATE_CODEC, WeatheringCopperSlabBlock::new);
    public static final BlockType.Complex<WeatheringStairs> WEATHERING_COPPER_STAIRS = complex(
            "weathering_copper_stairs", WeatheringStairs.CODEC, (definition, properties) -> new WeatheringCopperStairBlock(definition.weatherState, definition.baseState, properties));
    public static final BlockType.Complex<WeatheringSet> WEATHERING_COPPER_TRAPDOOR = complex(
            "weathering_copper_trapdoor", WeatheringSet.CODEC, (definition, properties) -> new WeatheringCopperTrapDoorBlock(definition.blockSetType, definition.weatherState, properties));
    public static final BlockType.Complex<WeatheringCopper.WeatherState> WEATHERING_LANTERN = complex("weathering_lantern", WEATHERING_STATE_CODEC, WeatheringLanternBlock::new);
    public static final BlockType.Complex<WeatheringCopper.WeatherState> WEATHERING_LIGHTNING_ROD = complex("weathering_lightning_rod", WEATHERING_STATE_CODEC, WeatheringLightningRodBlock::new);
    public static final BlockType.Complex<WeightedPlate> WEIGHTED_PRESSURE_PLATE = complex(
            "weighted_pressure_plate", WeightedPlate.CODEC, (definition, properties) -> new WeightedPressurePlateBlock(definition.maxWeight, definition.blockSetType, properties));
    public static final BlockType.Complex<SuspiciousStewEffects> WITHER_ROSE = complex(
            "wither_rose", SUSPICIOUS_STEW_EFFECTS_CODEC, WitherRoseBlock::new);
    public static final BlockType.Complex<DyeColor> WOOL_CARPET = complex(
            "wool_carpet", DYE_COLOR_CODEC, WoolCarpetBlock::new);

    // Codec Definitions

    public record Chest(SoundEvent openSound, SoundEvent closeSound) {
        public static final MapCodec<Chest> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                OPEN_SOUND_CODEC.forGetter(Chest::openSound),
                CLOSE_SOUND_CODEC.forGetter(Chest::closeSound)
        ).apply(instance, Chest::new));
    }

    public record Brushable(Block turnsInto, SoundEvent brushSound, SoundEvent brushCompletedSound) {
        public static final MapCodec<Brushable> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                BuiltInRegistries.BLOCK.byNameCodec().fieldOf("turns_into").forGetter(Brushable::turnsInto),
                BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("brush_sound").forGetter(Brushable::brushSound),
                BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("brush_completed_sound").forGetter(Brushable::brushCompletedSound)
        ).apply(instance, Brushable::new));
    }

    public record Button(BlockSetType blockSetType, int ticksToStayPressed) {
        public static final MapCodec<Button> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                BLOCK_SET_TYPE_CODEC.forGetter(Button::blockSetType),
                ExtraCodecs.NON_NEGATIVE_INT.fieldOf("ticks_to_stay_pressed").forGetter(Button::ticksToStayPressed)
        ).apply(instance, Button::new));
    }

    public record Campfire(boolean spawnParticles, int fireDamage) {
        public static final MapCodec<Campfire> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.BOOL.fieldOf("spawn_particles").forGetter(Campfire::spawnParticles),
                ExtraCodecs.NON_NEGATIVE_INT.fieldOf("fire_damage").forGetter(Campfire::fireDamage)
        ).apply(instance, Campfire::new));
    }

    public record Dimensions(float height, float width) {
        public static final MapCodec<Dimensions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.FLOAT.fieldOf("height").forGetter(Dimensions::height),
                Codec.FLOAT.fieldOf("width").forGetter(Dimensions::width)
        ).apply(instance, Dimensions::new));
    }

    public record AttachedStem(Block stem, Block fruit, Item seed, TagKey<Block> supportBlocks) {
        public static final MapCodec<AttachedStem> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                BuiltInRegistries.BLOCK.byNameCodec().fieldOf("stem").forGetter(AttachedStem::stem),
                FRUIT_CODEC.forGetter(AttachedStem::fruit),
                SEED_CODEC.forGetter(AttachedStem::seed),
                SUPPORT_BLOCKS_CODEC.forGetter(AttachedStem::supportBlocks)
        ).apply(instance, AttachedStem::new));
    }

    public record WeatheringChest(WeatheringCopper.WeatherState weatherState, SoundEvent openSound, SoundEvent closeSound) {
        public static final MapCodec<WeatheringChest> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                WEATHERING_STATE_CODEC.forGetter(WeatheringChest::weatherState),
                OPEN_SOUND_CODEC.forGetter(WeatheringChest::openSound),
                CLOSE_SOUND_CODEC.forGetter(WeatheringChest::closeSound)
        ).apply(instance, WeatheringChest::new));
    }

    public record LayeredCauldron(Biome.Precipitation precipitation, CauldronInteraction.Dispatcher interactions) {
        public static final MapCodec<LayeredCauldron> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Biome.Precipitation.CODEC.fieldOf("precipitation").forGetter(LayeredCauldron::precipitation),
                CauldronInteractions.CODEC.fieldOf("interactions").forGetter(LayeredCauldron::interactions)
        ).apply(instance, LayeredCauldron::new));
    }

    public record NetherFungus(ResourceKey<Feature> feature, Block requiredBlock, TagKey<Block> supportBlocks) {
        public static final MapCodec<NetherFungus> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                FEATURE_CODEC.forGetter(NetherFungus::feature),
                BuiltInRegistries.BLOCK.byNameCodec().fieldOf("grows_on").forGetter(NetherFungus::requiredBlock),
                SUPPORT_BLOCKS_CODEC.forGetter(NetherFungus::supportBlocks)
        ).apply(instance, NetherFungus::new));
    }

    public record Stem(Block fruit, Block attachedStem, Item seed,
                       TagKey<Block> stemSupportBlocks, TagKey<Block> fruitSupportBlocks) {
        public static final MapCodec<Stem> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                FRUIT_CODEC.forGetter(Stem::fruit),
                BuiltInRegistries.BLOCK.byNameCodec().fieldOf("attached_stem").forGetter(Stem::attachedStem),
                SEED_CODEC.forGetter(Stem::seed),
                TagKey.codec(Registries.BLOCK).fieldOf("stem_support_blocks").forGetter(Stem::stemSupportBlocks),
                TagKey.codec(Registries.BLOCK).fieldOf("fruit_support_blocks").forGetter(Stem::fruitSupportBlocks)
        ).apply(instance, Stem::new));
    }

    public record ParticleLeaves(float chance, ParticleOptions particle, AmbientLeavesBlockSoundPlayer player) {
        public static final MapCodec<ParticleLeaves> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                LEAF_PARTICLE_CHANCE_CODEC.forGetter(ParticleLeaves::chance),
                ParticleTypes.CODEC.fieldOf("leaf_particle").forGetter(ParticleLeaves::particle),
                AmbientLeavesBlockSoundPlayer.CODEC.fieldOf("sound_player").forGetter(ParticleLeaves::player)
        ).apply(instance, ParticleLeaves::new));
    }

    public record WeatheringSet(BlockSetType blockSetType, WeatheringCopper.WeatherState weatherState) {
        public static final MapCodec<WeatheringSet> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                BLOCK_SET_TYPE_CODEC.forGetter(WeatheringSet::blockSetType),
                WEATHERING_STATE_CODEC.forGetter(WeatheringSet::weatherState)
        ).apply(instance, WeatheringSet::new));
    }

    public record WeatheringStairs(WeatheringCopper.WeatherState weatherState, BlockState baseState) {
        public static final MapCodec<WeatheringStairs> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                WEATHERING_STATE_CODEC.forGetter(WeatheringStairs::weatherState),
                BlockState.CODEC.fieldOf("base_state").forGetter(WeatheringStairs::baseState)
        ).apply(instance, WeatheringStairs::new));
    }

    public record WeightedPlate(int maxWeight, BlockSetType blockSetType) {
        public static final MapCodec<WeightedPlate> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.intRange(1, 1024).fieldOf("max_weight").forGetter(WeightedPlate::maxWeight),
                BLOCK_SET_TYPE_CODEC.forGetter(WeightedPlate::blockSetType)
        ).apply(instance, WeightedPlate::new));
    }

    public static void init() {}
}


