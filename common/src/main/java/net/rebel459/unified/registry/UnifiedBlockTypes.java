package net.rebel459.unified.registry;

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
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.util.valueproviders.IntProviders;
import net.rebel459.unified.util.codec.ExtensibleCodec;
import net.rebel459.unified.util.data.registry.BlockRegistry;

import java.util.function.BiFunction;
import java.util.function.Function;

public class UnifiedBlockTypes {

    // Common Codecs

    private static final Codec<FlowingFluid> FLOWING_FLUID_CODEC = BuiltInRegistries.FLUID.byNameCodec().flatXmap(
            fluid -> fluid instanceof FlowingFluid flowing
                    ? DataResult.success(flowing)
                    : DataResult.error(() -> "Fluid is not a flowing fluid: " + BuiltInRegistries.FLUID.getKey(fluid)),
            DataResult::success
    );
    private static final Codec<SimpleParticleType> SIMPLE_PARTICLE_CODEC = ParticleTypes.CODEC.flatXmap(
            particle -> particle instanceof SimpleParticleType simple
                    ? DataResult.success(simple)
                    : DataResult.error(() -> "Particle is not a simple particle type: " + particle),
            DataResult::success
    );

    private static final MapCodec<Block> BASE_BLOCK_FIELD = Block.CODEC.fieldOf("base_block");
    private static final MapCodec<Identifier> BLOCK_SET_TYPE_FIELD = Identifier.CODEC.fieldOf("block_set_type");
    private static final MapCodec<Block> DEAD_BLOCK_FIELD = Block.CODEC.fieldOf("dead_block");
    private static final MapCodec<DyeColor> DYE_COLOR_FIELD = DyeColor.CODEC.fieldOf("color");
    private static final MapCodec<ResourceKey<ConfiguredFeature<?, ?>>> FEATURE_FIELD = ResourceKey.codec(Registries.CONFIGURED_FEATURE).fieldOf("feature");
    private static final MapCodec<ResourceKey<Block>> FRUIT_FIELD = ResourceKey.codec(Registries.BLOCK).fieldOf("fruit");
    private static final MapCodec<Block> HOST_BLOCK_FIELD = Block.CODEC.fieldOf("host_block");
    private static final MapCodec<Float> LEAF_PARTICLE_CHANCE_FIELD = ExtraCodecs.floatRange(0.0F, 1.0F).fieldOf("leaf_particle_chance");
    private static final MapCodec<SoundEvent> OPEN_SOUND_FIELD = BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("open_sound");
    private static final MapCodec<SoundEvent> CLOSE_SOUND_FIELD = BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("close_sound");
    private static final MapCodec<ResourceKey<Item>> SEED_FIELD = ResourceKey.codec(Registries.ITEM).fieldOf("seed");
    private static final MapCodec<SimpleParticleType> SIMPLE_PARTICLE_FIELD = SIMPLE_PARTICLE_CODEC.fieldOf("flame_particle");
    private static final MapCodec<SkullBlock.Type> SKULL_TYPE_FIELD = SkullBlock.Type.CODEC.fieldOf("kind");
    private static final MapCodec<TagKey<Block>> SUPPORT_BLOCKS_FIELD = TagKey.codec(Registries.BLOCK).fieldOf("support_blocks");
    private static final MapCodec<SuspiciousStewEffects> SUSPICIOUS_STEW_EFFECTS_FIELD = SuspiciousStewEffects.CODEC.fieldOf("suspicious_stew_effects");
    private static final MapCodec<TreeGrower> TREE_FIELD = TreeGrower.CODEC.fieldOf("tree");
    private static final MapCodec<WeatheringCopper.WeatherState> WEATHERING_STATE_FIELD = WeatheringCopper.WeatherState.CODEC.fieldOf("weathering_state");
    private static final MapCodec<Identifier> WOOD_TYPE_FIELD = Identifier.CODEC.fieldOf("wood_type");

    // Registration Helpers

    private static ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> simple(String id, Function<BlockBehaviour.Properties, ? extends Block> factory) {
        return BlockRegistry.TYPES.register(Identifier.withDefaultNamespace(id), () -> factory);
    }

    private static <T> ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> complex(String id, MapCodec<T> codec, BiFunction<T, BlockBehaviour.Properties, ? extends Block> factory) {
        return BlockRegistry.TYPES.register(
                Identifier.withDefaultNamespace(id),
                codec,
                definition -> properties -> factory.apply(definition, properties)
        );
    }

    // Simple Blocks

    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> BLOCK = simple("block", Block::new);

    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> AIR = simple("air", AirBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> AMETHYST = simple("amethyst", AmethystBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> ANVIL = simple("anvil", AnvilBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> AZALEA = simple("azalea", AzaleaBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> BAMBOO_SAPLING = simple("bamboo_sapling", BambooSaplingBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> BAMBOO_STALK = simple("bamboo_stalk", BambooStalkBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> BARREL = simple("barrel", BarrelBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> BARRIER = simple("barrier", BarrierBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> BASE_CORAL_FAN = simple("base_coral_fan", BaseCoralFanBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> BASE_CORAL_PLANT = simple("base_coral_plant", BaseCoralPlantBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> BASE_CORAL_WALL_FAN = simple("base_coral_wall_fan", BaseCoralWallFanBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> BEACON = simple("beacon", BeaconBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> BEEHIVE = simple("beehive", BeehiveBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> BEETROOT = simple("beetroot", BeetrootBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> BELL = simple("bell", BellBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> BIG_DRIPLEAF = simple("big_dripleaf", BigDripleafBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> BIG_DRIPLEAF_STEM = simple("big_dripleaf_stem", BigDripleafStemBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> BLAST_FURNACE = simple("blast_furnace", BlastFurnaceBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> BREWING_STAND = simple("brewing_stand", BrewingStandBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> BUBBLE_COLUMN = simple("bubble_column", BubbleColumnBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> BUDDING_AMETHYST = simple("budding_amethyst", BuddingAmethystBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> BUSH = simple("bush", BushBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> CACTUS = simple("cactus", CactusBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> CACTUS_FLOWER = simple("cactus_flower", CactusFlowerBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> CAKE = simple("cake", CakeBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> CALIBRATED_SCULK_SENSOR = simple("calibrated_sculk_sensor", CalibratedSculkSensorBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> CANDLE = simple("candle", CandleBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> CARPET = simple("carpet", CarpetBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> CARROT = simple("carrot", CarrotBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> CARTOGRAPHY_TABLE = simple("cartography_table", CartographyTableBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> CARVED_PUMPKIN = simple("carved_pumpkin", CarvedPumpkinBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> CAULDRON = simple("cauldron", CauldronBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> CAVE_VINES = simple("cave_vines", CaveVinesBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> CAVE_VINES_PLANT = simple("cave_vines_plant", CaveVinesPlantBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> CHAIN = simple("chain", ChainBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> CHISELED_BOOKSHELF = simple("chiseled_bookshelf", ChiseledBookShelfBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> CHORUS_PLANT = simple("chorus_plant", ChorusPlantBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> COCOA = simple("cocoa", CocoaBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> COMPARATOR = simple("comparator", ComparatorBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> COMPOSTER = simple("composter", ComposterBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> CONDUIT = simple("conduit", ConduitBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> COPPER_BULB = simple("copper_bulb", CopperBulbBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> CRAFTER = simple("crafter", CrafterBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> CRAFTING_TABLE = simple("crafting_table", CraftingTableBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> CREAKING_HEART = simple("creaking_heart", CreakingHeartBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> CROP = simple("crop", CropBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> CRYING_OBSIDIAN = simple("crying_obsidian", CryingObsidianBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> DAYLIGHT_DETECTOR = simple("daylight_detector", DaylightDetectorBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> DECORATED_POT = simple("decorated_pot", DecoratedPotBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> DETECTOR_RAIL = simple("detector_rail", DetectorRailBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> DIRT_PATH = simple("dirt_path", DirtPathBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> DISPENSER = simple("dispenser", DispenserBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> DOUBLE_PLANT = simple("double_plant", DoublePlantBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> DRAGON_EGG = simple("dragon_egg", DragonEggBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> DRIED_GHAST = simple("dried_ghast", DriedGhastBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> DROPPER = simple("dropper", DropperBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> DRY_VEGETATION = simple("dry_vegetation", DryVegetationBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> ENCHANTING_TABLE = simple("enchanting_table", EnchantingTableBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> ENDER_CHEST = simple("ender_chest", EnderChestBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> END_GATEWAY = simple("end_gateway", EndGatewayBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> END_PORTAL = simple("end_portal", EndPortalBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> END_PORTAL_FRAME = simple("end_portal_frame", EndPortalFrameBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> END_ROD = simple("end_rod", EndRodBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> FARMLAND = simple("farmland", FarmlandBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> FENCE = simple("fence", FenceBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> FIRE = simple("fire", FireBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> FIREFLY_BUSH = simple("firefly_bush", FireflyBushBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> FLOWER_BED = simple("flower_bed", FlowerBedBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> FROGSPAWN = simple("frogspawn", FrogspawnBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> FROSTED_ICE = simple("frosted_ice", FrostedIceBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> FURNACE = simple("furnace", FurnaceBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> GLAZED_TERRACOTTA = simple("glazed_terracotta", GlazedTerracottaBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> GLOW_LICHEN = simple("glow_lichen", GlowLichenBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> GRASS = simple("grass", GrassBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> GRINDSTONE = simple("grindstone", GrindstoneBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> HALF_TRANSPARENT_BLOCK = simple("half_transparent_block", HalfTransparentBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> HANGING_MOSS = simple("hanging_moss", HangingMossBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> HANGING_ROOTS = simple("hanging_roots", HangingRootsBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> HAY = simple("hay", HayBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> HEAVY_CORE = simple("heavy_core", HeavyCoreBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> HONEY = simple("honey", HoneyBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> HOPPER = simple("hopper", HopperBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> HUGE_MUSHROOM = simple("huge_mushroom", HugeMushroomBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> ICE = simple("ice", IceBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> IRON_BARS = simple("iron_bars", IronBarsBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> JUKEBOX = simple("jukebox", JukeboxBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> KELP = simple("kelp", KelpBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> KELP_PLANT = simple("kelp_plant", KelpPlantBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> LADDER = simple("ladder", LadderBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> LANTERN = simple("lantern", LanternBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> LAVA_CAULDRON = simple("lava_cauldron", LavaCauldronBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> LEAF_LITTER = simple("leaf_litter", LeafLitterBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> LECTERN = simple("lectern", LecternBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> LEVER = simple("lever", LeverBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> LIGHT = simple("light", LightBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> LIGHTNING_ROD = simple("lightning_rod", LightningRodBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> LILY_PAD = simple("lily_pad", LilyPadBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> LOOM = simple("loom", LoomBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> MAGMA = simple("magma", MagmaBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> MANGROVE_ROOTS = simple("mangrove_roots", MangroveRootsBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> MOSSY_CARPET = simple("mossy_carpet", MossyCarpetBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> MOVING_PISTON = simple("moving_piston", MovingPistonBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> MUD = simple("mud", MudBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> MULTIFACE_BLOCK = simple("multiface_block", MultifaceBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> MYCELIUM = simple("mycelium", MyceliumBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> NETHER_PORTAL = simple("nether_portal", NetherPortalBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> NETHERRACK = simple("netherrack", NetherrackBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> NETHER_SPROUTS = simple("nether_sprouts", NetherSproutsBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> NETHER_WART = simple("nether_wart", NetherWartBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> NOTE_BLOCK = simple("note_block", NoteBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> NYLIUM = simple("nylium", NyliumBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> OBSERVER = simple("observer", ObserverBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> PIGLIN_WALL_SKULL = simple("piglin_wall_skull", PiglinWallSkullBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> PISTON_HEAD = simple("piston_head", PistonHeadBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> PITCHER_CROP = simple("pitcher_crop", PitcherCropBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> PLAYER_HEAD = simple("player_head", PlayerHeadBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> PLAYER_WALL_HEAD = simple("player_wall_head", PlayerWallHeadBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> POINTED_DRIPSTONE = simple("pointed_dripstone", PointedDripstoneBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> POTATO = simple("potato", PotatoBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> POWDER_SNOW = simple("powder_snow", PowderSnowBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> POWERED_BLOCK = simple("powered_block", PoweredBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> POWERED_RAIL = simple("powered_rail", PoweredRailBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> PUMPKIN = simple("pumpkin", PumpkinBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> RAIL = simple("rail", RailBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> REDSTONE_LAMP = simple("redstone_lamp", RedstoneLampBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> REDSTONE_ORE = simple("redstone_ore", RedStoneOreBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> REDSTONE_TORCH = simple("redstone_torch", RedstoneTorchBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> REDSTONE_WALL_TORCH = simple("redstone_wall_torch", RedstoneWallTorchBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> REDSTONE_WIRE = simple("redstone_wire", RedStoneWireBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> REPEATER = simple("repeater", RepeaterBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> RESPAWN_ANCHOR = simple("respawn_anchor", RespawnAnchorBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> ROOTED_DIRT = simple("rooted_dirt", RootedDirtBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> ROTATED_PILLAR_BLOCK = simple("rotated_pillar_block", RotatedPillarBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> SCAFFOLDING = simple("scaffolding", ScaffoldingBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> SCULK = simple("sculk", SculkBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> SCULK_CATALYST = simple("sculk_catalyst", SculkCatalystBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> SCULK_SENSOR = simple("sculk_sensor", SculkSensorBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> SCULK_SHRIEKER = simple("sculk_shrieker", SculkShriekerBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> SCULK_VEIN = simple("sculk_vein", SculkVeinBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> SEAGRASS = simple("seagrass", SeagrassBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> SEA_PICKLE = simple("sea_pickle", SeaPickleBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> SHELF = simple("shelf", ShelfBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> SHORT_DRY_GRASS = simple("short_dry_grass", ShortDryGrassBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> SLAB = simple("slab", SlabBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> SLIME_BLOCK = simple("slime_block", SlimeBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> SMALL_DRIPLEAF = simple("small_dripleaf", SmallDripleafBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> SMITHING_TABLE = simple("smithing_table", SmithingTableBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> SMOKER = simple("smoker", SmokerBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> SNIFFER_EGG = simple("sniffer_egg", SnifferEggBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> SNOW_LAYER = simple("snow_layer", SnowLayerBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> SNOWY_BLOCK = simple("snowy_block", SnowyBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> SOUL_FIRE = simple("soul_fire", SoulFireBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> SOUL_SAND = simple("soul_sand", SoulSandBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> SPAWNER = simple("spawner", SpawnerBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> SPONGE = simple("sponge", SpongeBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> SPORE_BLOSSOM = simple("spore_blossom", SporeBlossomBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> STONECUTTER = simple("stonecutter", StonecutterBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> SUGAR_CANE = simple("sugar_cane", SugarCaneBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> SWEET_BERRY_BUSH = simple("sweet_berry_bush", SweetBerryBushBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> TALL_DRY_GRASS = simple("tall_dry_grass", TallDryGrassBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> TALL_FLOWER = simple("tall_flower", TallFlowerBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> TALL_GRASS = simple("tall_grass", TallGrassBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> TALL_SEAGRASS = simple("tall_seagrass", TallSeagrassBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> TARGET = simple("target", TargetBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> TINTED_GLASS = simple("tinted_glass", TintedGlassBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> TNT = simple("tnt", TntBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> TORCHFLOWER_CROP = simple("torchflower_crop", TorchflowerCropBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> TRANSPARENT_BLOCK = simple("transparent_block", TransparentBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> TRAPPED_CHEST = simple("trapped_chest", TrappedChestBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> TRIAL_SPAWNER = simple("trial_spawner", TrialSpawnerBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> TRIPWIRE_HOOK = simple("tripwire_hook", TripWireHookBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> TURTLE_EGG = simple("turtle_egg", TurtleEggBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> TWISTING_VINES = simple("twisting_vines", TwistingVinesBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> TWISTING_VINES_PLANT = simple("twisting_vines_plant", TwistingVinesPlantBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> VAULT = simple("vault", VaultBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> VINE = simple("vine", VineBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> WALL = simple("wall", WallBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> WATERLOGGED_TRANSPARENT_BLOCK = simple("waterlogged_transparent_block", WaterloggedTransparentBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> WEB = simple("web", WebBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> WEEPING_VINES = simple("weeping_vines", WeepingVinesBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> WEEPING_VINES_PLANT = simple("weeping_vines_plant", WeepingVinesPlantBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> WET_SPONGE = simple("wet_sponge", WetSpongeBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> WITHER_SKULL = simple("wither_skull", WitherSkullBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> WITHER_WALL_SKULL = simple("wither_wall_skull", WitherWallSkullBlock::new);

    // Complex Blocks

    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> AMETHYST_CLUSTER = complex(
            "amethyst_cluster", Dimensions.CODEC, (definition, properties) -> new AmethystClusterBlock(definition.height, definition.width, properties));
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> ATTACHED_STEM = complex(
            "attached_stem", AttachedStem.CODEC, (definition, properties) -> new AttachedStemBlock(definition.stem, definition.fruit, definition.seed, definition.supportBlocks, properties));
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> BANNER = complex(
            "banner", DYE_COLOR_FIELD, BannerBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> BED = complex(
            "bed", DYE_COLOR_FIELD, BedBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> BONEMEALABLE_FEATURE_PLACER_BLOCK = complex(
            "bonemealable_feature_placer_block", FEATURE_FIELD, BonemealableFeaturePlacerBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> BRUSHABLE_BLOCK = complex(
            "brushable_block", Brushable.CODEC, (definition, properties) -> new BrushableBlock(definition.turnsInto, definition.brushSound, definition.brushCompletedSound, properties));
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> BUTTON = complex(
            "button", Button.CODEC, (definition, properties) -> new ButtonBlock(BlockSetType.TYPES.get(getSetName(definition.blockSetType)), definition.ticksToStayPressed, properties));
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> CAMPFIRE = complex(
            "campfire", Campfire.CODEC, (definition, properties) -> new CampfireBlock(definition.spawnParticles, definition.fireDamage, properties));
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> CANDLE_CAKE = complex(
            "candle_cake", BASE_BLOCK_FIELD, CandleCakeBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> CEILING_HANGING_SIGN = complex(
            "ceiling_hanging_sign", WOOD_TYPE_FIELD, (definition, properties) -> new CeilingHangingSignBlock(WoodType.TYPES.get(getSetName(definition)), properties));
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> CHEST = complex(
            "chest", Chest.CODEC, (definition, properties) -> new ChestBlock(() -> BlockEntityType.CHEST, definition.openSound, definition.closeSound, properties));
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> CHORUS_FLOWER = complex(
            "chorus_flower", Block.CODEC.fieldOf("plant"), ChorusFlowerBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> COLORED_FALLING_BLOCK = complex(
            "colored_falling_block", ColorRGBA.CODEC.fieldOf("color"), ColoredFallingBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> CONCRETE_POWDER = complex(
            "concrete_powder", Block.CODEC.fieldOf("concrete"), ConcretePowderBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> COPPER_CHEST = complex(
            "copper_chest", WeatheringChest.CODEC, (definition, properties) -> new CopperChestBlock(definition.weatherState, definition.openSound, definition.closeSound, properties));
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> COPPER_GOLEM_STATUE = complex(
            "copper_golem_statue", WEATHERING_STATE_FIELD, CopperGolemStatueBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> CORAL = complex(
            "coral", DEAD_BLOCK_FIELD, CoralBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> CORAL_FAN = complex(
            "coral_fan", DEAD_BLOCK_FIELD, CoralFanBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> CORAL_PLANT = complex(
            "coral_plant", DEAD_BLOCK_FIELD, CoralPlantBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> CORAL_WALL_FAN = complex(
            "coral_wall_fan", DEAD_BLOCK_FIELD, CoralWallFanBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> DOOR = complex(
            "door", BLOCK_SET_TYPE_FIELD, (definition, properties) -> new DoorBlock(BlockSetType.TYPES.get(getSetName(definition)), properties));
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> DROP_EXPERIENCE_BLOCK = complex(
            "drop_experience_block", IntProviders.codec(0, 10).fieldOf("experience"), DropExperienceBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> EYEBLOSSOM = complex(
            "eyeblossom", Codec.BOOL.fieldOf("open"), EyeblossomBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> FENCE_GATE = complex(
            "fence_gate", WOOD_TYPE_FIELD, (definition, properties) -> new FenceGateBlock(WoodType.TYPES.get(getSetName(definition)), properties));
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> FLOWER = complex(
            "flower", SUSPICIOUS_STEW_EFFECTS_FIELD, FlowerBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> FLOWER_POT = complex(
            "flower_pot", Block.CODEC.fieldOf("potted"), FlowerPotBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> INFESTED_BLOCK = complex(
            "infested_block", HOST_BLOCK_FIELD, InfestedBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> INFESTED_ROTATED_PILLAR = complex(
            "infested_rotated_pillar", HOST_BLOCK_FIELD, InfestedRotatedPillarBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> LAYERED_CAULDRON = complex(
            "layered_cauldron", LayeredCauldron.CODEC, (definition, properties) -> new LayeredCauldronBlock(definition.precipitation, definition.interactions, properties));
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> LIQUID = complex(
            "liquid", FLOWING_FLUID_CODEC.fieldOf("fluid"), LiquidBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> MANGROVE_LEAVES = complex(
            "mangrove_leaves", LEAF_PARTICLE_CHANCE_FIELD, MangroveLeavesBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> MANGROVE_PROPAGULE = complex(
            "mangrove_propagule", TREE_FIELD, MangrovePropaguleBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> MUSHROOM = complex(
            "mushroom", FEATURE_FIELD, MushroomBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> NETHER_FUNGUS = complex(
            "nether_fungus", NetherFungus.CODEC, (definition, properties) -> new NetherFungusBlock(definition.feature, definition.requiredBlock, definition.supportBlocks, properties));
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> NETHER_ROOTS = complex(
            "nether_roots", SUPPORT_BLOCKS_FIELD, NetherRootsBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> PISTON_BASE = complex(
            "piston_base", Codec.BOOL.fieldOf("sticky"), PistonBaseBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> PRESSURE_PLATE = complex(
            "pressure_plate", BLOCK_SET_TYPE_FIELD, (definition, properties) -> new PressurePlateBlock(BlockSetType.TYPES.get(getSetName(definition)), properties));
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> SAND = complex(
            "sand", ColorRGBA.CODEC.fieldOf("falling_dust_color"), SandBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> SAPLING = complex(
            "sapling", TREE_FIELD, SaplingBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> SHULKER_BOX = complex(
            "shulker_box", DyeColor.CODEC.optionalFieldOf("color"), (color, properties) -> new ShulkerBoxBlock(color.orElse(null), properties));
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> SKULL = complex(
            "skull", SKULL_TYPE_FIELD, SkullBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> STAINED_GLASS = complex(
            "stained_glass", DYE_COLOR_FIELD, StainedGlassBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> STAINED_GLASS_PANE = complex(
            "stained_glass_pane", DYE_COLOR_FIELD, StainedGlassPaneBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> STAIRS = complex(
            "stairs", BASE_BLOCK_FIELD, (definition, properties) -> new StairBlock(definition.defaultBlockState(), properties));
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> STANDING_SIGN = complex(
            "standing_sign", WOOD_TYPE_FIELD, (definition, properties) -> new StandingSignBlock(WoodType.TYPES.get(getSetName(definition)), properties));
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> STEM = complex(
            "stem", Stem.CODEC, (definition, properties) -> new StemBlock(definition.fruit, definition.attachedStem, definition.seed, definition.stemSupportBlocks, definition.fruitSupportBlocks, properties));
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> TINTED_PARTICLE_LEAVES = complex(
            "tinted_particle_leaves", LEAF_PARTICLE_CHANCE_FIELD, TintedParticleLeavesBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> TORCH = complex(
            "torch", SIMPLE_PARTICLE_FIELD, TorchBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> TRAPDOOR = complex(
            "trapdoor", BLOCK_SET_TYPE_FIELD, (definition, properties) -> new TrapDoorBlock(BlockSetType.TYPES.get(getSetName(definition)), properties));
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> TRIPWIRE = complex(
            "tripwire", Block.CODEC.fieldOf("hook"), TripWireBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> UNTINTED_PARTICLE_LEAVES = complex(
            "untinted_particle_leaves", ParticleLeaves.CODEC, (definition, properties) -> new UntintedParticleLeavesBlock(definition.chance, definition.particle, properties));
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> WALL_BANNER = complex(
            "wall_banner", DYE_COLOR_FIELD, WallBannerBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> WALL_HANGING_SIGN = complex(
            "wall_hanging_sign", WOOD_TYPE_FIELD, (definition, properties) -> new WallHangingSignBlock(WoodType.TYPES.get(getSetName(definition)), properties));
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> WALL_SIGN = complex(
            "wall_sign", WOOD_TYPE_FIELD, (definition, properties) -> new WallSignBlock(WoodType.TYPES.get(getSetName(definition)), properties));
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> WALL_SKULL = complex(
            "wall_skull", SKULL_TYPE_FIELD, WallSkullBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> WALL_TORCH = complex(
            "wall_torch", SIMPLE_PARTICLE_FIELD, WallTorchBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> WEATHERING_COPPER_BARS = complex("weathering_copper_bars", WEATHERING_STATE_FIELD, WeatheringCopperBarsBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> WEATHERING_COPPER_BULB = complex("weathering_copper_bulb", WEATHERING_STATE_FIELD, WeatheringCopperBulbBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> WEATHERING_COPPER_CHAIN = complex("weathering_copper_chain", WEATHERING_STATE_FIELD, WeatheringCopperChainBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> WEATHERING_COPPER_CHEST = complex(
            "weathering_copper_chest", WeatheringChest.CODEC, (definition, properties) -> new WeatheringCopperChestBlock(definition.weatherState, definition.openSound, definition.closeSound, properties));
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> WEATHERING_COPPER_DOOR = complex(
            "weathering_copper_door", WeatheringSet.CODEC, (definition, properties) -> new WeatheringCopperDoorBlock(BlockSetType.TYPES.get(getSetName(definition.blockSetType)), definition.weatherState, properties));
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> WEATHERING_COPPER_FULL_BLOCK = complex("weathering_copper_full_block", WEATHERING_STATE_FIELD, WeatheringCopperFullBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> WEATHERING_COPPER_GOLEM_STATUE = complex("weathering_copper_golem_statue", WEATHERING_STATE_FIELD, WeatheringCopperGolemStatueBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> WEATHERING_COPPER_GRATE = complex("weathering_copper_grate", WEATHERING_STATE_FIELD, WeatheringCopperGrateBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> WEATHERING_COPPER_SLAB = complex("weathering_copper_slab", WEATHERING_STATE_FIELD, WeatheringCopperSlabBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> WEATHERING_COPPER_STAIRS = complex(
            "weathering_copper_stairs", WeatheringStairs.CODEC, (definition, properties) -> new WeatheringCopperStairBlock(definition.weatherState, definition.baseState, properties));
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> WEATHERING_COPPER_TRAPDOOR = complex(
            "weathering_copper_trapdoor", WeatheringSet.CODEC, (definition, properties) -> new WeatheringCopperTrapDoorBlock(BlockSetType.TYPES.get(getSetName(definition.blockSetType)), definition.weatherState, properties));
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> WEATHERING_LANTERN = complex("weathering_lantern", WEATHERING_STATE_FIELD, WeatheringLanternBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> WEATHERING_LIGHTNING_ROD = complex("weathering_lightning_rod", WEATHERING_STATE_FIELD, WeatheringLightningRodBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> WEIGHTED_PRESSURE_PLATE = complex(
            "weighted_pressure_plate", WeightedPlate.CODEC, (definition, properties) -> new WeightedPressurePlateBlock(definition.maxWeight, BlockSetType.TYPES.get(getSetName(definition.blockSetType)), properties));
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> WITHER_ROSE = complex(
            "wither_rose", SUSPICIOUS_STEW_EFFECTS_FIELD, WitherRoseBlock::new);
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> WOOL_CARPET = complex(
            "wool_carpet", DYE_COLOR_FIELD, WoolCarpetBlock::new);

    // Codec Definitions

    public record Chest(SoundEvent openSound, SoundEvent closeSound) {
        public static final MapCodec<Chest> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                OPEN_SOUND_FIELD.forGetter(Chest::openSound),
                CLOSE_SOUND_FIELD.forGetter(Chest::closeSound)
        ).apply(instance, Chest::new));
    }

    public record Brushable(Block turnsInto, SoundEvent brushSound, SoundEvent brushCompletedSound) {
        public static final MapCodec<Brushable> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Block.CODEC.fieldOf("turns_into").forGetter(Brushable::turnsInto),
                BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("brush_sound").forGetter(Brushable::brushSound),
                BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("brush_completed_sound").forGetter(Brushable::brushCompletedSound)
        ).apply(instance, Brushable::new));
    }

    public record Button(Identifier blockSetType, int ticksToStayPressed) {
        public static final MapCodec<Button> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                BLOCK_SET_TYPE_FIELD.forGetter(Button::blockSetType),
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

    public record AttachedStem(ResourceKey<Block> stem, ResourceKey<Block> fruit, ResourceKey<Item> seed, TagKey<Block> supportBlocks) {
        public static final MapCodec<AttachedStem> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ResourceKey.codec(Registries.BLOCK).fieldOf("stem").forGetter(AttachedStem::stem),
                FRUIT_FIELD.forGetter(AttachedStem::fruit),
                SEED_FIELD.forGetter(AttachedStem::seed),
                SUPPORT_BLOCKS_FIELD.forGetter(AttachedStem::supportBlocks)
        ).apply(instance, AttachedStem::new));
    }

    public record WeatheringChest(WeatheringCopper.WeatherState weatherState, SoundEvent openSound, SoundEvent closeSound) {
        public static final MapCodec<WeatheringChest> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                WEATHERING_STATE_FIELD.forGetter(WeatheringChest::weatherState),
                OPEN_SOUND_FIELD.forGetter(WeatheringChest::openSound),
                CLOSE_SOUND_FIELD.forGetter(WeatheringChest::closeSound)
        ).apply(instance, WeatheringChest::new));
    }

    public record LayeredCauldron(Biome.Precipitation precipitation, CauldronInteraction.Dispatcher interactions) {
        public static final MapCodec<LayeredCauldron> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Biome.Precipitation.CODEC.fieldOf("precipitation").forGetter(LayeredCauldron::precipitation),
                CauldronInteractions.CODEC.fieldOf("interactions").forGetter(LayeredCauldron::interactions)
        ).apply(instance, LayeredCauldron::new));
    }

    public record NetherFungus(ResourceKey<ConfiguredFeature<?, ?>> feature, Block requiredBlock, TagKey<Block> supportBlocks) {
        public static final MapCodec<NetherFungus> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                FEATURE_FIELD.forGetter(NetherFungus::feature),
                Block.CODEC.fieldOf("grows_on").forGetter(NetherFungus::requiredBlock),
                SUPPORT_BLOCKS_FIELD.forGetter(NetherFungus::supportBlocks)
        ).apply(instance, NetherFungus::new));
    }

    public record Stem(ResourceKey<Block> fruit, ResourceKey<Block> attachedStem, ResourceKey<Item> seed,
                       TagKey<Block> stemSupportBlocks, TagKey<Block> fruitSupportBlocks) {
        public static final MapCodec<Stem> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                FRUIT_FIELD.forGetter(Stem::fruit),
                ResourceKey.codec(Registries.BLOCK).fieldOf("attached_stem").forGetter(Stem::attachedStem),
                SEED_FIELD.forGetter(Stem::seed),
                TagKey.codec(Registries.BLOCK).fieldOf("stem_support_blocks").forGetter(Stem::stemSupportBlocks),
                TagKey.codec(Registries.BLOCK).fieldOf("fruit_support_blocks").forGetter(Stem::fruitSupportBlocks)
        ).apply(instance, Stem::new));
    }

    public record ParticleLeaves(float chance, ParticleOptions particle) {
        public static final MapCodec<ParticleLeaves> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                LEAF_PARTICLE_CHANCE_FIELD.forGetter(ParticleLeaves::chance),
                ParticleTypes.CODEC.fieldOf("leaf_particle").forGetter(ParticleLeaves::particle)
        ).apply(instance, ParticleLeaves::new));
    }

    public record WeatheringSet(Identifier blockSetType, WeatheringCopper.WeatherState weatherState) {
        public static final MapCodec<WeatheringSet> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                BLOCK_SET_TYPE_FIELD.forGetter(WeatheringSet::blockSetType),
                WEATHERING_STATE_FIELD.forGetter(WeatheringSet::weatherState)
        ).apply(instance, WeatheringSet::new));
    }

    public record WeatheringStairs(WeatheringCopper.WeatherState weatherState, BlockState baseState) {
        public static final MapCodec<WeatheringStairs> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                WEATHERING_STATE_FIELD.forGetter(WeatheringStairs::weatherState),
                BlockState.CODEC.fieldOf("base_state").forGetter(WeatheringStairs::baseState)
        ).apply(instance, WeatheringStairs::new));
    }

    public record WeightedPlate(int maxWeight, Identifier blockSetType) {
        public static final MapCodec<WeightedPlate> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.intRange(1, 1024).fieldOf("max_weight").forGetter(WeightedPlate::maxWeight),
                BLOCK_SET_TYPE_FIELD.forGetter(WeightedPlate::blockSetType)
        ).apply(instance, WeightedPlate::new));
    }

    private static String getSetName(Identifier id) {
        if (id.getNamespace().equals("minecraft")) return id.getPath();
        else return id.toString();
    }

    public static void init() {}
}
