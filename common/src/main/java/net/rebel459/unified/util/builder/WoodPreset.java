package net.rebel459.unified.util.builder;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;

public final class WoodPreset {

    final WoodSet.Settings settings;

    WoodPreset(WoodSet.Settings settings) {
        this.settings = settings;
    }

    public static final WoodPreset DEFAULT = new WoodSet.PresetBuilder()
            .build();

    public static final WoodPreset NETHER = create()
            .logName("stem")
            .woodName("hyphae")
            .isFlammable(false)
            .setBoats(WoodSet.Boats.NONE)
            .buttonSounds(() -> SoundEvents.NETHER_WOOD_BUTTON_CLICK_ON, () -> SoundEvents.NETHER_WOOD_BUTTON_CLICK_OFF)
            .pressurePlateSounds(() -> SoundEvents.NETHER_WOOD_PRESSURE_PLATE_CLICK_ON, () -> SoundEvents.NETHER_WOOD_PRESSURE_PLATE_CLICK_OFF)
            .trapdoorSounds(() -> SoundEvents.NETHER_WOOD_TRAPDOOR_OPEN, () -> SoundEvents.NETHER_WOOD_TRAPDOOR_CLOSE)
            .doorSounds(() -> SoundEvents.NETHER_WOOD_DOOR_OPEN, () -> SoundEvents.NETHER_WOOD_DOOR_CLOSE)
            .fenceGateSounds(() -> SoundEvents.NETHER_WOOD_FENCE_GATE_OPEN, () -> SoundEvents.NETHER_WOOD_FENCE_GATE_CLOSE)
            .hangingSignSoundType(() -> SoundType.NETHER_WOOD_HANGING_SIGN)
            .woodSoundType(() -> SoundType.NETHER_WOOD)
            .build();

    public static final WoodPreset BAMBOO = create()
            .logName("block")
            .hasWood(false)
            .hasMosaic(true)
            .setBoats(WoodSet.Boats.RAFTS)
            .buttonSounds(() -> SoundEvents.BAMBOO_WOOD_BUTTON_CLICK_ON, () -> SoundEvents.BAMBOO_WOOD_BUTTON_CLICK_OFF)
            .pressurePlateSounds(() -> SoundEvents.BAMBOO_WOOD_PRESSURE_PLATE_CLICK_ON, () -> SoundEvents.BAMBOO_WOOD_PRESSURE_PLATE_CLICK_OFF)
            .trapdoorSounds(() -> SoundEvents.BAMBOO_WOOD_TRAPDOOR_OPEN, () -> SoundEvents.BAMBOO_WOOD_TRAPDOOR_CLOSE)
            .doorSounds(() -> SoundEvents.BAMBOO_WOOD_DOOR_OPEN, () -> SoundEvents.BAMBOO_WOOD_DOOR_CLOSE)
            .fenceGateSounds(() -> SoundEvents.BAMBOO_WOOD_FENCE_GATE_OPEN, () -> SoundEvents.BAMBOO_WOOD_FENCE_GATE_CLOSE)
            .hangingSignSoundType(() -> SoundType.BAMBOO_WOOD_HANGING_SIGN)
            .woodSoundType(() -> SoundType.BAMBOO_WOOD)
            .build();

    public static final WoodPreset CHERRY = create()
            .buttonSounds(() -> SoundEvents.CHERRY_WOOD_BUTTON_CLICK_ON, () -> SoundEvents.CHERRY_WOOD_BUTTON_CLICK_OFF)
            .pressurePlateSounds(() -> SoundEvents.CHERRY_WOOD_PRESSURE_PLATE_CLICK_ON, () -> SoundEvents.CHERRY_WOOD_PRESSURE_PLATE_CLICK_OFF)
            .trapdoorSounds(() -> SoundEvents.CHERRY_WOOD_TRAPDOOR_OPEN, () -> SoundEvents.CHERRY_WOOD_TRAPDOOR_CLOSE)
            .doorSounds(() -> SoundEvents.CHERRY_WOOD_DOOR_OPEN, () -> SoundEvents.CHERRY_WOOD_DOOR_CLOSE)
            .fenceGateSounds(() -> SoundEvents.CHERRY_WOOD_FENCE_GATE_OPEN, () -> SoundEvents.CHERRY_WOOD_FENCE_GATE_CLOSE)
            .hangingSignSoundType(() -> SoundType.CHERRY_WOOD_HANGING_SIGN)
            .woodSoundType(() -> SoundType.CHERRY_WOOD)
            .build();

    public static WoodSet.PresetBuilder create() {
        return createFrom(DEFAULT);
    }

    public static WoodSet.PresetBuilder createFrom(WoodPreset preset) {
        return new WoodSet.PresetBuilder(preset.settings.copy());
    }
}