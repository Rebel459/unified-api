package net.rebel459.unified.util.datagen;

import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.resources.Identifier;
import net.rebel459.unified.Unified;

public final class ItemAssets {

    public static final ItemAsset<Void> GENERATED = create("generated");
    public static final ItemAsset<Void> HANDHELD = create("handheld");
    public static final ItemAsset<Void> MACE = create("mace");
    public static final ItemAsset<Void> SPEAR = create("spear");

    private ItemAssets() {}

    private static <T> ItemAsset<T> create(String path) {
        return new ItemAsset<>(Identifier.fromNamespaceAndPath(Unified.MOD_ID, path));
    }
}
