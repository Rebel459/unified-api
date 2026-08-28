package net.rebel459.unified.util.data.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.rebel459.unified.Unified;
import net.rebel459.unified.util.codec.CodecUtils;
import net.rebel459.unified.util.registry.RegistryResourceListener;

public class BlockSetTypeRegistry extends RegistryResourceListener<BlockSetTypeRegistry.Definition> {
    public static final Codec<Definition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("can_open_by_hand", true).forGetter(Definition::canOpenByHand),
            Codec.BOOL.optionalFieldOf("can_open_by_wind_charge", true).forGetter(Definition::canOpenByWindCharge),
            Codec.BOOL.optionalFieldOf("can_button_be_activated_by_arrows", true).forGetter(Definition::canButtonBeActivatedByArrows),
            CodecUtils.named(BlockSetType.PressurePlateSensitivity.class).optionalFieldOf("pressure_plate_sensitivity", BlockSetType.PressurePlateSensitivity.EVERYTHING).forGetter(Definition::pressurePlateSensitivity),
            BlockRegistry.SoundType.CODEC.optionalFieldOf("sound_type", BlockRegistry.SoundType.create(SoundType.WOOD)).forGetter(Definition::soundType),
            BuiltInRegistries.SOUND_EVENT.byNameCodec().optionalFieldOf("door_close", SoundEvents.WOODEN_DOOR_CLOSE).forGetter(Definition::doorClose),
            BuiltInRegistries.SOUND_EVENT.byNameCodec().optionalFieldOf("door_open", SoundEvents.WOODEN_DOOR_OPEN).forGetter(Definition::doorOpen),
            BuiltInRegistries.SOUND_EVENT.byNameCodec().optionalFieldOf("trapdoor_close", SoundEvents.WOODEN_TRAPDOOR_CLOSE).forGetter(Definition::trapdoorClose),
            BuiltInRegistries.SOUND_EVENT.byNameCodec().optionalFieldOf("trapdoor_open", SoundEvents.WOODEN_TRAPDOOR_OPEN).forGetter(Definition::trapdoorOpen),
            BuiltInRegistries.SOUND_EVENT.byNameCodec().optionalFieldOf("pressure_plate_click_off", SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_OFF).forGetter(Definition::pressurePlateClickOff),
            BuiltInRegistries.SOUND_EVENT.byNameCodec().optionalFieldOf("pressure_plate_click_on", SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_ON).forGetter(Definition::pressurePlateClickOn),
            BuiltInRegistries.SOUND_EVENT.byNameCodec().optionalFieldOf("button_click_off", SoundEvents.WOODEN_BUTTON_CLICK_OFF).forGetter(Definition::buttonClickOff),
            BuiltInRegistries.SOUND_EVENT.byNameCodec().optionalFieldOf("button_click_on", SoundEvents.WOODEN_BUTTON_CLICK_ON).forGetter(Definition::buttonClickOn)
    ).apply(instance, Definition::new));

    public static final Identifier ID = Identifier.fromNamespaceAndPath(Unified.MOD_ID, "block_set_types");

    public BlockSetTypeRegistry() {
        super(ID, CODEC, SoundEventRegistry.ID);
    }

    @Override
    protected void register(Identifier id, BlockSetTypeRegistry.Definition type) {
        BlockSetType.register(new BlockSetType(
                id.toString(),
                type.canOpenByHand,
                type.canOpenByWindCharge,
                type.canButtonBeActivatedByArrows,
                type.pressurePlateSensitivity,
                type.soundType.convert(),
                type.doorClose,
                type.doorOpen,
                type.trapdoorClose,
                type.trapdoorOpen,
                type.pressurePlateClickOff,
                type.pressurePlateClickOn,
                type.buttonClickOff,
                type.buttonClickOn
        ));
    }

    public record Definition(
            boolean canOpenByHand,
            boolean canOpenByWindCharge,
            boolean canButtonBeActivatedByArrows,
            BlockSetType.PressurePlateSensitivity pressurePlateSensitivity,
            BlockRegistry.SoundType soundType,
            SoundEvent doorClose,
            SoundEvent doorOpen,
            SoundEvent trapdoorClose,
            SoundEvent trapdoorOpen,
            SoundEvent pressurePlateClickOff,
            SoundEvent pressurePlateClickOn,
            SoundEvent buttonClickOff,
            SoundEvent buttonClickOn
    ) {}
}
