package net.rebel459.unified.api.datagen;

import net.minecraft.resources.Identifier;
import net.rebel459.unified.Unified;

public final class ItemAssets {

    public static final ItemAsset<Void> GENERATED = create("generated");
    public static final ItemAsset<Void> HANDHELD = create("handheld");
    public static final ItemAsset<Void> MACE = create("mace");
    public static final ItemAsset<Void> SPEAR = create("spear");

    private ItemAssets() {}

    private static <T> ItemAsset<T> create(String path) {
        return new ItemAsset<>(Unified.id(path));
    }
}
