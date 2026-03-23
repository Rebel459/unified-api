package net.rebel459.unified.tag;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.rebel459.unified.Unified;
import org.jetbrains.annotations.NotNull;

public class UnifiedItemTags {
    public static final TagKey<Item> PERSISTENT_COOLDOWNS = create("persistent_cooldowns");

    @NotNull
    private static TagKey<Item> create(@NotNull String path) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Unified.MOD_ID, path));
    }
}