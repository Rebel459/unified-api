package net.rebel459.unified.util.builder;

public final class ColoredItemPreset {

    final ColoredItemSet.Settings settings;

    ColoredItemPreset(ColoredItemSet.Settings settings) {
        this.settings = settings;
    }

    public static final ColoredItemPreset DEFAULT = new ColoredItemSet.PresetBuilder()
            .build();

    public static ColoredItemSet.PresetBuilder create() {
        return createFrom(DEFAULT);
    }

    public static ColoredItemSet.PresetBuilder createFrom(ColoredItemPreset preset) {
        return new ColoredItemSet.PresetBuilder(preset.settings.copy());
    }
}