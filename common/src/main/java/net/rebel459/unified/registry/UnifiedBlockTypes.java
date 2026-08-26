package net.rebel459.unified.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ColorRGBA;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.rebel459.unified.util.codec.ExtensibleCodec;
import net.rebel459.unified.util.data.BlockRegistry;

import java.util.function.Function;

public class UnifiedBlockTypes {
    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> BLOCK = BlockRegistry.TYPES.register(
            Identifier.withDefaultNamespace("block"),
            () -> Block::new
    );

    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> SLAB = BlockRegistry.TYPES.register(
            Identifier.withDefaultNamespace("slab"),
            () -> SlabBlock::new
    );

    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> STAIRS = BlockRegistry.TYPES.register(
            Identifier.withDefaultNamespace("stairs"),
            Block.CODEC.fieldOf("base_block"),
            definition -> properties -> new StairBlock(definition.defaultBlockState(), properties)
    );

    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> WALL = BlockRegistry.TYPES.register(
            Identifier.withDefaultNamespace("wall"),
            () -> WallBlock::new
    );

    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> FENCE = BlockRegistry.TYPES.register(
            Identifier.withDefaultNamespace("fence"),
            () -> FenceBlock::new
    );

    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> FENCE_GATE = BlockRegistry.TYPES.register(
            Identifier.withDefaultNamespace("fence_gate"),
            Identifier.CODEC.fieldOf("wood_type"),
            definition -> properties -> new FenceGateBlock(WoodType.TYPES.get(getSetName(definition)), properties)
    );

    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> DOOR = BlockRegistry.TYPES.register(
            Identifier.withDefaultNamespace("door"),
            Identifier.CODEC.fieldOf("block_set_type"),
            definition -> properties -> new DoorBlock(BlockSetType.TYPES.get(getSetName(definition)), properties)
    );

    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> BARREL = BlockRegistry.TYPES.register(
            Identifier.withDefaultNamespace("barrel"),
            () -> BarrelBlock::new
    );

    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> BRUSHABLE_BLOCK = BlockRegistry.TYPES.register(
            Identifier.withDefaultNamespace("brushable_block"),
            Brushable.CODEC,
            definition -> properties -> new BrushableBlock(definition.turnsInto, definition.brushSound, definition.brushCompletedSound, properties)
    );

    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> BUSH = BlockRegistry.TYPES.register(
            Identifier.withDefaultNamespace("bush"),
            () -> BushBlock::new
    );

    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> BUTTON = BlockRegistry.TYPES.register(
            Identifier.withDefaultNamespace("button"),
            Button.CODEC,
            definition -> properties -> new ButtonBlock(BlockSetType.TYPES.get(getSetName(definition.blockSetType)), definition.ticksToStayPressed, properties)
    );

    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> CACTUS = BlockRegistry.TYPES.register(
            Identifier.withDefaultNamespace("cactus"),
            () -> CactusBlock::new
    );

    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> CACTUS_FLOWER = BlockRegistry.TYPES.register(
            Identifier.withDefaultNamespace("cactus_flower"),
            () -> CactusFlowerBlock::new
    );

    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> CAMPFIRE = BlockRegistry.TYPES.register(
            Identifier.withDefaultNamespace("campfire"),
            Campfire.CODEC,
            definition -> properties -> new CampfireBlock(definition.spawnParticles, definition.fireDamage, properties)
    );

    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> CANDLE = BlockRegistry.TYPES.register(
            Identifier.withDefaultNamespace("candle"),
            () -> CandleBlock::new
    );

    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> CANDLE_CAKE = BlockRegistry.TYPES.register(
            Identifier.withDefaultNamespace("candle_cake"),
            Block.CODEC.fieldOf("base_block"),
            definition -> properties -> new CandleCakeBlock(definition, properties)
    );

    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> CARROT = BlockRegistry.TYPES.register(
            Identifier.withDefaultNamespace("carrot"),
            () -> CarrotBlock::new
    );

    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> CAVE_VINES = BlockRegistry.TYPES.register(
            Identifier.withDefaultNamespace("cave_vines"),
            () -> CaveVinesBlock::new
    );

    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> CAVE_VINES_PLANT = BlockRegistry.TYPES.register(
            Identifier.withDefaultNamespace("cave_vines_plant"),
            () -> CaveVinesPlantBlock::new
    );

    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> CEILING_HANGING_SIGN = BlockRegistry.TYPES.register(
            Identifier.withDefaultNamespace("ceiling_hanging_sign"),
            Identifier.CODEC.fieldOf("wood_type"),
            definition -> properties -> new CeilingHangingSignBlock(WoodType.TYPES.get(getSetName(definition)), properties)
    );

    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> WALL_HANGING_SIGN = BlockRegistry.TYPES.register(
            Identifier.withDefaultNamespace("wall_hanging_sign"),
            Identifier.CODEC.fieldOf("wood_type"),
            definition -> properties -> new WallHangingSignBlock(WoodType.TYPES.get(getSetName(definition)), properties)
    );

    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> CHAIN = BlockRegistry.TYPES.register(
            Identifier.withDefaultNamespace("chain"),
            () -> ChainBlock::new
    );

    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> CHEST = BlockRegistry.TYPES.register(
            Identifier.withDefaultNamespace("chest"),
            Chest.CODEC,
            definition -> properties -> new ChestBlock(BlockEntityType.CHEST, definition.openSound, definition.closeSound, properties)
    );

    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> CHISELED_BOOKSHELF = BlockRegistry.TYPES.register(
            Identifier.withDefaultNamespace("chiseled_bookshelf"),
            () -> ChiseledBookShelfBlock::new
    );

    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> COCOA = BlockRegistry.TYPES.register(
            Identifier.withDefaultNamespace("cocoa"),
            () -> CocoaBlock::new
    );

    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> COLORED_FALLING_BLOCK = BlockRegistry.TYPES.register(
            Identifier.withDefaultNamespace("colored_falling_block"),
            ColorRGBA.CODEC.fieldOf("color"),
            definition -> properties -> new ColoredFallingBlock(definition, properties)
    );

    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> COMPOSTER = BlockRegistry.TYPES.register(
            Identifier.withDefaultNamespace("composter"),
            () -> ComposterBlock::new
    );

    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> CONCRETE_POWDER = BlockRegistry.TYPES.register(
            Identifier.withDefaultNamespace("concrete_powder"),
            Block.CODEC.fieldOf("concrete"),
            definition -> properties -> new ConcretePowderBlock(definition, properties)
    );

    public static final ExtensibleCodec.Type<Function<BlockBehaviour.Properties, ? extends Block>> COPPER_BULB = BlockRegistry.TYPES.register(
            Identifier.withDefaultNamespace("copper_bulb"),
            () -> CopperBulbBlock::new
    );

    public record Chest(SoundEvent openSound, SoundEvent closeSound) {
        public static final MapCodec<Chest> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("open_sound").forGetter(Chest::openSound),
                BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("close_sound").forGetter(Chest::closeSound)
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
                Identifier.CODEC.fieldOf("block_set_type").forGetter(Button::blockSetType),
                ExtraCodecs.NON_NEGATIVE_INT.fieldOf("ticks_to_stay_pressed").forGetter(Button::ticksToStayPressed)
        ).apply(instance, Button::new));
    }

    public record Campfire(boolean spawnParticles, int fireDamage) {
        public static final MapCodec<Campfire> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.BOOL.fieldOf("spawn_particles").forGetter(Campfire::spawnParticles),
                ExtraCodecs.NON_NEGATIVE_INT.fieldOf("fire_damage").forGetter(Campfire::fireDamage)
        ).apply(instance, Campfire::new));
    }

    private static String getSetName(Identifier id) {
        if (id.getNamespace().equals("minecraft")) return id.getPath();
        else return id.toString();
    }

    public static void init() {}
}
