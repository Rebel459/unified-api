package net.rebel459.unified.util.builder;

public final class BlockPreset {

    final BlockSet.Settings settings;

    BlockPreset(BlockSet.Settings settings) {
        this.settings = settings;
    }

    public static final BlockPreset DEFAULT = new BlockSet.PresetBuilder()
            .build();

    public static final BlockPreset BASIC = create()
            .hasWall(false)
            .build();

    public static final BlockPreset CRACKED = create()
            .hasCracked(true)
            .build();

    public static final BlockPreset CHISELED = create()
            .hasChiseled(true)
            .build();

    public static final BlockPreset STONE = create()
            .hasButton(true)
            .hasPressurePlate(true)
            .hasWall(false)
            .build();

    public static final BlockPreset POLISHED_BLACKSTONE = create()
            .hasButton(true)
            .hasPressurePlate(true)
            .hasChiseled(true)
            .build();

    public static final BlockPreset NETHER_BRICKS = create()
            .hasCracked(true)
            .hasFence(true)
            .hasChiseled(true)
            .build();

    public static BlockSet.PresetBuilder create() {
        return createFrom(DEFAULT);
    }

    public static BlockSet.PresetBuilder createFrom(BlockPreset preset) {
        return new BlockSet.PresetBuilder(preset.settings.copy());
    }
}