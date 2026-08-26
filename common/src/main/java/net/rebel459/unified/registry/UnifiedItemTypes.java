package net.rebel459.unified.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ItemSteerable;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.rebel459.unified.util.codec.ExtensibleCodec;
import net.rebel459.unified.util.data.ItemRegistry;

import java.util.function.Function;

public class UnifiedItemTypes {
    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> ITEM = ItemRegistry.TYPES.register(
            Identifier.withDefaultNamespace("item"),
            () -> Item::new
    );

    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> BLOCK_ITEM = ItemRegistry.TYPES.register(
            Identifier.withDefaultNamespace("block_item"),
            Block.CODEC.fieldOf("block"),
            definition -> properties -> new BlockItem(definition, properties)
    );

    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> DOUBLE_HIGH_BLOCK_ITEM = ItemRegistry.TYPES.register(
            Identifier.withDefaultNamespace("double_high_block_item"),
            Block.CODEC.fieldOf("block"),
            definition -> properties -> new DoubleHighBlockItem(definition, properties)
    );

    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> SIGN_ITEM = ItemRegistry.TYPES.register(
            Identifier.withDefaultNamespace("sign_item"),
            Sign.CODEC.fieldOf("blocks"),
            definition -> properties -> new SignItem(definition.sign, definition.wallSign, properties)
    );

    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> AXE = ItemRegistry.TYPES.register(
            Identifier.withDefaultNamespace("axe"),
            Tool.CODEC,
            definition -> properties -> new AxeItem(definition.material, definition.attackDamage, definition.attackSpeed, properties)
    );

    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> SHOVEL = ItemRegistry.TYPES.register(
            Identifier.withDefaultNamespace("shovel"),
            Tool.CODEC,
            definition -> properties -> new ShovelItem(definition.material, definition.attackDamage, definition.attackSpeed, properties)
    );

    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> HOE = ItemRegistry.TYPES.register(
            Identifier.withDefaultNamespace("hoe"),
            Tool.CODEC,
            definition -> properties -> new HoeItem(definition.material, definition.attackDamage, definition.attackSpeed, properties)
    );

    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> BOW = ItemRegistry.TYPES.register(
            Identifier.withDefaultNamespace("bow"),
            () -> BowItem::new
    );

    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> CROSSBOW = ItemRegistry.TYPES.register(
            Identifier.withDefaultNamespace("crossbow"),
            () -> CrossbowItem::new
    );

    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> FISHING_ROD = ItemRegistry.TYPES.register(
            Identifier.withDefaultNamespace("fishing_rod"),
            () -> FishingRodItem::new
    );

    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> MACE = ItemRegistry.TYPES.register(
            Identifier.withDefaultNamespace("mace"),
            () -> MaceItem::new
    );

    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> TRIDENT = ItemRegistry.TYPES.register(
            Identifier.withDefaultNamespace("trident"),
            () -> TridentItem::new
    );

    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> INSTRUMENT = ItemRegistry.TYPES.register(
            Identifier.withDefaultNamespace("instrument"),
            () -> InstrumentItem::new
    );

    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> BOAT = ItemRegistry.TYPES.register(
            Identifier.withDefaultNamespace("boat"),
            Identifier.CODEC.fieldOf("boat_entity"),
            definition -> properties -> new BoatItem((EntityType<? extends AbstractBoat>) BuiltInRegistries.ENTITY_TYPE.getValue(definition), properties)
    );

    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> BRUSH = ItemRegistry.TYPES.register(
            Identifier.withDefaultNamespace("brush"),
            () -> BrushItem::new
    );

    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> FOOD_ON_A_STICK = ItemRegistry.TYPES.register(
            Identifier.withDefaultNamespace("food_on_a_stick"),
            FoodOnAStick.CODEC,
            definition -> properties -> new FoodOnAStickItem<>((EntityType<? extends ItemSteerable>) BuiltInRegistries.ENTITY_TYPE.getValue(definition.canInteractWith), definition.consumeItemDamage, properties)
    );

    public static final ExtensibleCodec.Type<Function<Item.Properties, Item>> SPAWN_EGG = ItemRegistry.TYPES.register(
            Identifier.withDefaultNamespace("spawn_egg"),
            () -> SpawnEggItem::new
    );

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
