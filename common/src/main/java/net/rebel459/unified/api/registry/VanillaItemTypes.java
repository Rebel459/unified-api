package net.rebel459.unified.api.registry;

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
import net.rebel459.unified.api.codec.BlockItemType;
import net.rebel459.unified.api.codec.ItemType;
import net.rebel459.unified.api.codec.ExtensibleCodecs;
import org.apache.commons.lang3.function.TriFunction;

import java.util.function.BiFunction;
import java.util.function.Function;

public class VanillaItemTypes {

    // Common Codecs

    private static final MapCodec<EntityType<?>> ENTITY_TYPE_CODEC = BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entity_type");
    private static final MapCodec<Fluid> FLUID_CODEC = BuiltInRegistries.FLUID.byNameCodec().fieldOf("fluid");
    private static final MapCodec<SoundEvent> PLACE_SOUND_CODEC = BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("place_sound");
    private static final MapCodec<Block> WALL_BLOCK_CODEC = BuiltInRegistries.BLOCK.byNameCodec().fieldOf("wall_block");

    // Registration Helpers

    private static ItemType.Simple simple(String id, Function<Item.Properties, ? extends Item> factory) {
        return ExtensibleCodecs.ITEM_TYPES.register(Identifier.withDefaultNamespace(id), () -> factory::apply);
    }

    private static <T> ItemType.Complex<T> complex(String id, MapCodec<T> codec, BiFunction<T, Item.Properties, ? extends Item> factory) {
        return ExtensibleCodecs.ITEM_TYPES.register(
                Identifier.withDefaultNamespace(id),
                codec,
                definition -> properties -> factory.apply(definition, properties)
        );
    }

    private static BlockItemType.Simple simpleBlock(String id, BiFunction<Block, Item.Properties, Item> factory) {
        return ExtensibleCodecs.BLOCK_ITEM_TYPES.register(Identifier.withDefaultNamespace(id), () -> factory);
    }
    private static <T> BlockItemType.Complex<T> complexBlock(String id, MapCodec<T> codec, TriFunction<T, Block, Item.Properties, ? extends Item> factory) {
        return ExtensibleCodecs.BLOCK_ITEM_TYPES.register(
                Identifier.withDefaultNamespace(id),
                codec,
                definition -> (block, properties) -> factory.apply(definition, block, properties)
        );
    }

    // Simple Items

    public static final ItemType.Simple ITEM = simple("item", Item::new);

    public static final ItemType.Simple ARMOR_STAND = simple("armor_stand", ArmorStandItem::new);
    public static final ItemType.Simple ARROW = simple("arrow", ArrowItem::new);
    public static final ItemType.Simple BONE_MEAL = simple("bone_meal", BoneMealItem::new);
    public static final ItemType.Simple BOTTLE = simple("bottle", BottleItem::new);
    public static final ItemType.Simple BOW = simple("bow", BowItem::new);
    public static final ItemType.Simple BRUSH = simple("brush", BrushItem::new);
    public static final ItemType.Simple BUNDLE = simple("bundle", BundleItem::new);
    public static final ItemType.Simple COMPASS = simple("compass", CompassItem::new);
    public static final ItemType.Simple CROSSBOW = simple("crossbow", CrossbowItem::new);
    public static final ItemType.Simple CUSHION = simple("cushion", CushionItem::new);
    public static final ItemType.Simple DISC_FRAGMENT = simple("disc_fragment", DiscFragmentItem::new);
    public static final ItemType.Simple DYE = simple("dye", DyeItem::new);
    public static final ItemType.Simple EGG = simple("egg", EggItem::new);
    public static final ItemType.Simple EMPTY_MAP = simple("empty_map", EmptyMapItem::new);
    public static final ItemType.Simple END_CRYSTAL = simple("end_crystal", EndCrystalItem::new);
    public static final ItemType.Simple ENDER_EYE = simple("ender_eye", EnderEyeItem::new);
    public static final ItemType.Simple ENDER_PEARL = simple("ender_pearl", EnderpearlItem::new);
    public static final ItemType.Simple EXPERIENCE_BOTTLE = simple("experience_bottle", ExperienceBottleItem::new);
    public static final ItemType.Simple FIRE_CHARGE = simple("fire_charge", FireChargeItem::new);
    public static final ItemType.Simple FIREWORK_ROCKET = simple("firework_rocket", FireworkRocketItem::new);
    public static final ItemType.Simple FISHING_ROD = simple("fishing_rod", FishingRodItem::new);
    public static final ItemType.Simple FLINT_AND_STEEL = simple("flint_and_steel", FlintAndSteelItem::new);
    public static final ItemType.Simple GLOW_INK_SAC = simple("glow_ink_sac", GlowInkSacItem::new);
    public static final ItemType.Simple HONEYCOMB = simple("honeycomb", HoneycombItem::new);
    public static final ItemType.Simple INK_SAC = simple("ink_sac", InkSacItem::new);
    public static final ItemType.Simple INSTRUMENT = simple("instrument", InstrumentItem::new);
    public static final ItemType.Simple KNOWLEDGE_BOOK = simple("knowledge_book", KnowledgeBookItem::new);
    public static final ItemType.Simple LEAD = simple("lead", LeadItem::new);
    public static final ItemType.Simple LINGERING_POTION = simple("lingering_potion", LingeringPotionItem::new);
    public static final ItemType.Simple MACE = simple("mace", MaceItem::new);
    public static final ItemType.Simple MAP = simple("map", MapItem::new);
    public static final ItemType.Simple NAME_TAG = simple("name_tag", NameTagItem::new);
    public static final ItemType.Simple POTION = simple("potion", PotionItem::new);
    public static final ItemType.Simple SHEARS = simple("shears", ShearsItem::new);
    public static final ItemType.Simple SHIELD = simple("shield", ShieldItem::new);
    public static final ItemType.Simple SNOWBALL = simple("snowball", SnowballItem::new);
    public static final ItemType.Simple SPAWN_EGG = simple("spawn_egg", SpawnEggItem::new);
    public static final ItemType.Simple SPECTRAL_ARROW = simple("spectral_arrow", SpectralArrowItem::new);
    public static final ItemType.Simple SPLASH_POTION = simple("splash_potion", SplashPotionItem::new);
    public static final ItemType.Simple SPYGLASS = simple("spyglass", SpyglassItem::new);
    public static final ItemType.Simple TIPPED_ARROW = simple("tipped_arrow", TippedArrowItem::new);
    public static final ItemType.Simple TRIDENT = simple("trident", TridentItem::new);
    public static final ItemType.Simple WIND_CHARGE = simple("wind_charge", WindChargeItem::new);
    public static final ItemType.Simple WRITABLE_BOOK = simple("writable_book", WritableBookItem::new);
    public static final ItemType.Simple WRITTEN_BOOK = simple("written_book", WrittenBookItem::new);

    // Complex Items

    public static final ItemType.Complex<EntityType<?>> BOAT = complex(
            "boat", BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("boat_entity"),
            (definition, properties) -> new BoatItem((EntityType<? extends AbstractBoat>) definition, properties));
    public static final ItemType.Complex<Fluid> BUCKET = complex(
            "bucket", FLUID_CODEC, BucketItem::new);
    public static final ItemType.Complex<FoodOnAStick> FOOD_ON_A_STICK = complex(
            "food_on_a_stick", FoodOnAStick.CODEC, (definition, properties) -> new FoodOnAStickItem<>((EntityType<? extends ItemSteerable>) definition.canInteractWith, definition.consumeItemDamage, properties));
    public static final ItemType.Complex<EntityType<?>> HANGING_ENTITY_ITEM = complex(
            "hanging_entity_item", ENTITY_TYPE_CODEC, (definition, properties) -> new HangingEntityItem((EntityType<? extends HangingEntity>) definition, properties));
    public static final ItemType.Complex<EntityType<?>> ITEM_FRAME = complex(
            "item_frame", ENTITY_TYPE_CODEC, (definition, properties) -> new ItemFrameItem((EntityType<? extends HangingEntity>) definition, properties));
    public static final ItemType.Complex<EntityType<?>> MINECART = complex(
            "minecart", ENTITY_TYPE_CODEC, (definition, properties) -> new MinecartItem((EntityType<? extends AbstractMinecart>) definition, properties));
    public static final ItemType.Complex<MobBucket> MOB_BUCKET = complex(
            "mob_bucket", MobBucket.CODEC, (definition, properties) -> new MobBucketItem((EntityType<? extends net.minecraft.world.entity.Mob>) definition.entityType, definition.fluid, definition.emptySound, properties));

    // Simple Block Items

    public static final BlockItemType.Simple BLOCK_ITEM = simpleBlock("block_item", BlockItem::new);
    public static final BlockItemType.Simple DOUBLE_HIGH_BLOCK_ITEM = simpleBlock(
            "double_high_block_item", DoubleHighBlockItem::new);
    public static final BlockItemType.Simple GAME_MASTER_BLOCK = simpleBlock(
            "game_master_block", GameMasterBlockItem::new);
    public static final BlockItemType.Simple PLACE_ON_WATER_BLOCK_ITEM = simpleBlock(
            "place_on_water_block_item", PlaceOnWaterBlockItem::new);
    public static final BlockItemType.Simple SCAFFOLDING_BLOCK_ITEM = simpleBlock(
            "scaffolding_block_item", ScaffoldingBlockItem::new);

    // Complex Block Items

    public static final BlockItemType.Complex<Block> BANNER_ITEM = complexBlock(
            "banner_item", WALL_BLOCK_CODEC,
            (wallBlock, block, properties) -> new BannerItem(block, wallBlock, properties));
    public static final BlockItemType.Complex<Block> HANGING_SIGN_ITEM = complexBlock(
            "hanging_sign_item", WALL_BLOCK_CODEC,
            (wallBlock, block, properties) -> new HangingSignItem(block, wallBlock, properties));
    public static final BlockItemType.Complex<Block> PLAYER_HEAD = complexBlock(
            "player_head", WALL_BLOCK_CODEC,
            (wallBlock, block, properties) -> new PlayerHeadItem(block, wallBlock, properties));
    public static final BlockItemType.Complex<SoundEvent> SOLID_BUCKET = complexBlock(
            "solid_bucket", PLACE_SOUND_CODEC,
            (placeSound, block, properties) -> new SolidBucketItem(block, placeSound, properties));
    public static final BlockItemType.Complex<StandingAndWall> STANDING_AND_WALL_BLOCK_ITEM = complexBlock(
            "standing_and_wall_block_item", StandingAndWall.CODEC,
            (definition, block, properties) -> new StandingAndWallBlockItem(
                    block, definition.wallBlock, definition.attachmentDirection, properties));

    // Codec Definitions

    public record MobBucket(EntityType<?> entityType, Fluid fluid, SoundEvent emptySound) {
        public static final MapCodec<MobBucket> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ENTITY_TYPE_CODEC.forGetter(MobBucket::entityType),
                FLUID_CODEC.forGetter(MobBucket::fluid),
                BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("empty_sound").forGetter(MobBucket::emptySound)
        ).apply(instance, MobBucket::new));
    }

    public record StandingAndWall(Block wallBlock, Direction attachmentDirection) {
        public static final MapCodec<StandingAndWall> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                WALL_BLOCK_CODEC.forGetter(StandingAndWall::wallBlock),
                Direction.CODEC.fieldOf("attachment_direction").forGetter(StandingAndWall::attachmentDirection)
        ).apply(instance, StandingAndWall::new));
    }

    public record FoodOnAStick(EntityType<?> canInteractWith, int consumeItemDamage) {
        public static final MapCodec<FoodOnAStick> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("can_interact_with").forGetter(FoodOnAStick::canInteractWith),
                ExtraCodecs.NON_NEGATIVE_INT.fieldOf("consume_item_damage").forGetter(FoodOnAStick::consumeItemDamage)
        ).apply(instance, FoodOnAStick::new));
    }

    public static void init() {}
}


