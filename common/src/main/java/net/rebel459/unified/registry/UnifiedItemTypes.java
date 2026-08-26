package net.rebel459.unified.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ItemSteerable;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.rebel459.unified.util.codec.ExtensibleCodec;
import net.rebel459.unified.util.data.ItemRegistry;

import java.util.function.BiFunction;
import java.util.function.Function;

public class UnifiedItemTypes {

    // Common Codecs

    private static final MapCodec<Block> BLOCK_FIELD = Block.CODEC.fieldOf("block");
    private static final MapCodec<Identifier> ENTITY_TYPE_FIELD = Identifier.CODEC.fieldOf("entity_type");
    private static final MapCodec<Fluid> FLUID_FIELD = BuiltInRegistries.FLUID.byNameCodec().fieldOf("fluid");
    private static final MapCodec<SoundEvent> PLACE_SOUND_FIELD = BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("place_sound");
    private static final MapCodec<Block> WALL_BLOCK_FIELD = Block.CODEC.fieldOf("wall_block");

    // Registration Helpers

    private static ExtensibleCodec.Type<Function<Item.Properties, Item>> simple(String id, Function<Item.Properties, ? extends Item> factory) {
        return ItemRegistry.TYPES.register(Identifier.withDefaultNamespace(id), () -> properties -> factory.apply(properties));
    }

    private static <T> ExtensibleCodec.Type<Function<Item.Properties, Item>> complex(String id, MapCodec<T> codec, BiFunction<T, Item.Properties, ? extends Item> factory) {
        return ItemRegistry.TYPES.register(
                Identifier.withDefaultNamespace(id),
                codec,
                definition -> properties -> factory.apply(definition, properties)
        );
    }

    // Simple Items

    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> ITEM = simple("item", Item::new);

    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> ARMOR_STAND = simple("armor_stand", ArmorStandItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> ARROW = simple("arrow", ArrowItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> BONE_MEAL = simple("bone_meal", BoneMealItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> BOTTLE = simple("bottle", BottleItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> BOW = simple("bow", BowItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> BRUSH = simple("brush", BrushItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> BUNDLE = simple("bundle", BundleItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> COMPASS = simple("compass", CompassItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> CROSSBOW = simple("crossbow", CrossbowItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> DISC_FRAGMENT = simple("disc_fragment", DiscFragmentItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> DYE = simple("dye", DyeItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> EGG = simple("egg", EggItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> EMPTY_MAP = simple("empty_map", EmptyMapItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> END_CRYSTAL = simple("end_crystal", EndCrystalItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> ENDER_EYE = simple("ender_eye", EnderEyeItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> ENDER_PEARL = simple("ender_pearl", EnderpearlItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> EXPERIENCE_BOTTLE = simple("experience_bottle", ExperienceBottleItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> FIRE_CHARGE = simple("fire_charge", FireChargeItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> FIREWORK_ROCKET = simple("firework_rocket", FireworkRocketItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> FISHING_ROD = simple("fishing_rod", FishingRodItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> FLINT_AND_STEEL = simple("flint_and_steel", FlintAndSteelItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> GLOW_INK_SAC = simple("glow_ink_sac", GlowInkSacItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> HONEYCOMB = simple("honeycomb", HoneycombItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> INK_SAC = simple("ink_sac", InkSacItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> INSTRUMENT = simple("instrument", InstrumentItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> KNOWLEDGE_BOOK = simple("knowledge_book", KnowledgeBookItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> LEAD = simple("lead", LeadItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> LINGERING_POTION = simple("lingering_potion", LingeringPotionItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> MACE = simple("mace", MaceItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> MAP = simple("map", MapItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> NAME_TAG = simple("name_tag", NameTagItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> POTION = simple("potion", PotionItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> SHEARS = simple("shears", ShearsItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> SHIELD = simple("shield", ShieldItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> SNOWBALL = simple("snowball", SnowballItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> SPAWN_EGG = simple("spawn_egg", SpawnEggItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> SPECTRAL_ARROW = simple("spectral_arrow", SpectralArrowItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> SPLASH_POTION = simple("splash_potion", SplashPotionItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> SPYGLASS = simple("spyglass", SpyglassItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> TIPPED_ARROW = simple("tipped_arrow", TippedArrowItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> TRIDENT = simple("trident", TridentItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> WIND_CHARGE = simple("wind_charge", WindChargeItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> WRITABLE_BOOK = simple("writable_book", WritableBookItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> WRITTEN_BOOK = simple("written_book", WrittenBookItem::new);

    // Complex Items

    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> AXE = complex(
            "axe", Tool.CODEC, (definition, properties) -> new AxeItem(definition.material, definition.attackDamage, definition.attackSpeed, properties));
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> BANNER_ITEM = complex(
            "banner_item", BlockAndWall.CODEC, (definition, properties) -> new BannerItem(definition.block, definition.wallBlock, properties));
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> BED_ITEM = complex(
            "bed_item", BLOCK_FIELD, BedItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> BLOCK_ITEM = complex(
            "block_item", BLOCK_FIELD, BlockItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> BOAT = complex(
            "boat", Identifier.CODEC.fieldOf("boat_entity"), (definition, properties) -> new BoatItem((EntityType<? extends AbstractBoat>) BuiltInRegistries.ENTITY_TYPE.getValue(definition), properties));
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> BUCKET = complex(
            "bucket", FLUID_FIELD, BucketItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> DOUBLE_HIGH_BLOCK_ITEM = complex(
            "double_high_block_item", BLOCK_FIELD, DoubleHighBlockItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> FOOD_ON_A_STICK = complex(
            "food_on_a_stick", FoodOnAStick.CODEC, (definition, properties) -> new FoodOnAStickItem<>((EntityType<? extends ItemSteerable>) BuiltInRegistries.ENTITY_TYPE.getValue(definition.canInteractWith), definition.consumeItemDamage, properties));
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> GAME_MASTER_BLOCK = complex(
            "game_master_block", BLOCK_FIELD, GameMasterBlockItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> HANGING_ENTITY_ITEM = complex(
            "hanging_entity_item", ENTITY_TYPE_FIELD, (definition, properties) -> new HangingEntityItem((EntityType<? extends HangingEntity>) BuiltInRegistries.ENTITY_TYPE.getValue(definition), properties));
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> HANGING_SIGN_ITEM = complex(
            "hanging_sign_item", HangingSign.CODEC, (definition, properties) -> new HangingSignItem(definition.hangingSign, definition.wallHangingSign, properties));
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> HOE = complex(
            "hoe", Tool.CODEC, (definition, properties) -> new HoeItem(definition.material, definition.attackDamage, definition.attackSpeed, properties));
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> ITEM_FRAME = complex(
            "item_frame", ENTITY_TYPE_FIELD, (definition, properties) -> new ItemFrameItem((EntityType<? extends HangingEntity>) BuiltInRegistries.ENTITY_TYPE.getValue(definition), properties));
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> MINECART = complex(
            "minecart", ENTITY_TYPE_FIELD, (definition, properties) -> new MinecartItem((EntityType<? extends AbstractMinecart>) BuiltInRegistries.ENTITY_TYPE.getValue(definition), properties));
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> MOB_BUCKET = complex(
            "mob_bucket", MobBucket.CODEC, (definition, properties) -> new MobBucketItem((EntityType<? extends net.minecraft.world.entity.Mob>) BuiltInRegistries.ENTITY_TYPE.getValue(definition.entityType), definition.fluid, definition.emptySound, properties));
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> PLACE_ON_WATER_BLOCK_ITEM = complex(
            "place_on_water_block_item", BLOCK_FIELD, PlaceOnWaterBlockItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> PLAYER_HEAD = complex(
            "player_head", BlockAndWall.CODEC, (definition, properties) -> new PlayerHeadItem(definition.block, definition.wallBlock, properties));
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> SCAFFOLDING_BLOCK_ITEM = complex(
            "scaffolding_block_item", BLOCK_FIELD, ScaffoldingBlockItem::new);
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> SHOVEL = complex(
            "shovel", Tool.CODEC, (definition, properties) -> new ShovelItem(definition.material, definition.attackDamage, definition.attackSpeed, properties));
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> SIGN_ITEM = complex(
            "sign_item", Sign.CODEC, (definition, properties) -> new SignItem(definition.sign, definition.wallSign, properties));
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> SOLID_BUCKET = complex(
            "solid_bucket", SolidBucket.CODEC, (definition, properties) -> new SolidBucketItem(definition.block, definition.placeSound, properties));
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> STANDING_AND_WALL_BLOCK_ITEM = complex(
            "standing_and_wall_block_item", StandingAndWall.CODEC, (definition, properties) -> new StandingAndWallBlockItem(definition.block, definition.wallBlock, definition.attachmentDirection, properties));

    // Codec Definitions

    public record BlockAndWall(Block block, Block wallBlock) {
        public static final MapCodec<BlockAndWall> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                BLOCK_FIELD.forGetter(BlockAndWall::block),
                WALL_BLOCK_FIELD.forGetter(BlockAndWall::wallBlock)
        ).apply(instance, BlockAndWall::new));
    }

    public record HangingSign(Block hangingSign, Block wallHangingSign) {
        public static final MapCodec<HangingSign> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Block.CODEC.fieldOf("hanging_sign").forGetter(HangingSign::hangingSign),
                Block.CODEC.fieldOf("wall_hanging_sign").forGetter(HangingSign::wallHangingSign)
        ).apply(instance, HangingSign::new));
    }

    public record MobBucket(Identifier entityType, Fluid fluid, SoundEvent emptySound) {
        public static final MapCodec<MobBucket> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ENTITY_TYPE_FIELD.forGetter(MobBucket::entityType),
                FLUID_FIELD.forGetter(MobBucket::fluid),
                BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("empty_sound").forGetter(MobBucket::emptySound)
        ).apply(instance, MobBucket::new));
    }

    public record SolidBucket(Block block, SoundEvent placeSound) {
        public static final MapCodec<SolidBucket> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                BLOCK_FIELD.forGetter(SolidBucket::block),
                PLACE_SOUND_FIELD.forGetter(SolidBucket::placeSound)
        ).apply(instance, SolidBucket::new));
    }

    public record StandingAndWall(Block block, Block wallBlock, Direction attachmentDirection) {
        public static final MapCodec<StandingAndWall> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                BLOCK_FIELD.forGetter(StandingAndWall::block),
                WALL_BLOCK_FIELD.forGetter(StandingAndWall::wallBlock),
                Direction.CODEC.fieldOf("attachment_direction").forGetter(StandingAndWall::attachmentDirection)
        ).apply(instance, StandingAndWall::new));
    }

    public record Tool(ToolMaterial material, float attackDamage, float attackSpeed) {
        public static final MapCodec<ToolMaterial> TOOL_MATERIAL_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                        TagKey.codec(Registries.BLOCK).fieldOf("incorrect_blocks_for_drops").forGetter(ToolMaterial::incorrectBlocksForDrops),
                        ExtraCodecs.POSITIVE_INT.fieldOf("durability").forGetter(ToolMaterial::durability),
                        ExtraCodecs.NON_NEGATIVE_FLOAT.fieldOf("speed").forGetter(ToolMaterial::speed),
                        Codec.FLOAT.fieldOf("attack_damage_bonus").forGetter(ToolMaterial::attackDamageBonus),
                        ExtraCodecs.NON_NEGATIVE_INT.fieldOf("enchantment_value").forGetter(ToolMaterial::enchantmentValue),
                        TagKey.codec(Registries.ITEM).fieldOf("repair_items").forGetter(ToolMaterial::repairItems)
                ).apply(instance, ToolMaterial::new));

        public static final MapCodec<Tool> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                        TOOL_MATERIAL_CODEC.fieldOf("material").forGetter(Tool::material),
                        Codec.FLOAT.fieldOf("attack_damage").forGetter(Tool::attackDamage),
                        Codec.FLOAT.fieldOf("attack_speed").forGetter(Tool::attackSpeed)
                ).apply(instance, Tool::new));
    }

    public record FoodOnAStick(Identifier canInteractWith, int consumeItemDamage) {
        public static final MapCodec<FoodOnAStick> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("can_interact_with").forGetter(FoodOnAStick::canInteractWith),
                ExtraCodecs.NON_NEGATIVE_INT.fieldOf("consume_item_damage").forGetter(FoodOnAStick::consumeItemDamage)
        ).apply(instance, FoodOnAStick::new));
    }

    public record Sign(Block sign, Block wallSign) {
        public static final MapCodec<Sign> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Block.CODEC.fieldOf("sign").forGetter(Sign::sign),
                Block.CODEC.fieldOf("wall_sign").forGetter(Sign::wallSign)
        ).apply(instance, Sign::new));
    }

    public static void init() {}
}
