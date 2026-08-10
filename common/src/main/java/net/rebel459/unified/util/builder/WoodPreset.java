package net.rebel459.unified.util.builder;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.WoodType;

public final class WoodPreset {

    final WoodSet.Settings settings;

    WoodPreset(WoodSet.Settings settings) {
        this.settings = settings;
    }

    public static final WoodPreset DEFAULT = new WoodSet.PresetBuilder()
            .build();

    public static final WoodPreset NETHER = create()
            .setLogName("stem")
            .setWoodName("hyphae")
            .isFlammable(false)
            .setBoats(WoodSet.Boats.NONE)
            .setButtonSounds(() -> SoundEvents.NETHER_WOOD_BUTTON_CLICK_ON, () -> SoundEvents.NETHER_WOOD_BUTTON_CLICK_OFF)
            .setPressurePlateSounds(() -> SoundEvents.NETHER_WOOD_PRESSURE_PLATE_CLICK_ON, () -> SoundEvents.NETHER_WOOD_PRESSURE_PLATE_CLICK_OFF)
            .setTrapdoorSounds(() -> SoundEvents.NETHER_WOOD_TRAPDOOR_OPEN, () -> SoundEvents.NETHER_WOOD_TRAPDOOR_CLOSE)
            .setDoorSounds(() -> SoundEvents.NETHER_WOOD_DOOR_OPEN, () -> SoundEvents.NETHER_WOOD_DOOR_CLOSE)
            .setFenceGateSounds(() -> SoundEvents.NETHER_WOOD_FENCE_GATE_OPEN, () -> SoundEvents.NETHER_WOOD_FENCE_GATE_CLOSE)
            .setHangingSignSoundType(() -> SoundType.NETHER_WOOD_HANGING_SIGN)
            .setWoodSoundType(() -> SoundType.NETHER_WOOD)
            .build();

    public static final WoodPreset BAMBOO = createFrom(WoodType.BAMBOO)
            .setLogName("block")
            .hasWood(false)
            .hasMosaic(true)
            .setBoats(WoodSet.Boats.RAFTS)
            .build();

    public static final WoodPreset CHERRY = createFrom(WoodType.CHERRY)
            .setLeavesSoundType(() -> SoundType.CHERRY_LEAVES)
            .build();

    public static final WoodPreset PALE_OAK = createFrom(WoodType.PALE_OAK)
            .build();

    public static final WoodPreset MANGROVE = createFrom(WoodType.MANGROVE)
            .setSaplingName("propagule")
            .build();

    public static WoodSet.PresetBuilder create() {
        return createFrom(DEFAULT);
    }

    public static WoodSet.PresetBuilder createFrom(WoodPreset preset) {
        return new WoodSet.PresetBuilder(preset.settings.copy());
    }

    public static WoodSet.PresetBuilder createFrom(WoodType woodType) {
        return create()
                .setButtonSounds(() -> woodType.setType().buttonClickOn(), () -> woodType.setType().buttonClickOff())
                .setPressurePlateSounds(() -> woodType.setType().pressurePlateClickOn(), () -> woodType.setType().pressurePlateClickOff())
                .setTrapdoorSounds(() -> woodType.setType().trapdoorOpen(), () -> woodType.setType().trapdoorClose())
                .setDoorSounds(() -> woodType.setType().doorOpen(), () -> woodType.setType().doorClose())
                .setFenceGateSounds(woodType::fenceGateOpen, woodType::fenceGateClose)
                .setHangingSignSoundType(woodType::hangingSignSoundType)
                .setWoodSoundType(woodType::soundType)
                .setDoorOpening(woodType.setType().canOpenByHand(), woodType.setType().canOpenByWindCharge())
                .canArrowsActivateButton(woodType.setType().canButtonBeActivatedByArrows())
                .setPressurePlateSensitivity(woodType.setType().pressurePlateSensitivity());
    }
}