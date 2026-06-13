package net.rebel459.unified.util.registry.builder;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.MapColor;
import net.rebel459.unified.platform.UnifiedRegistries;

public class BlockBuilders {

    private final String modId;
    private final UnifiedRegistries.Items itemRegistry;
    private final UnifiedRegistries.Blocks blockRegistry;
    private final UnifiedRegistries.EntityTypes entityRegistry;

    public BlockBuilders(String modId){
        this.modId = modId;
        this.itemRegistry = UnifiedRegistries.Items.create(modId);
        this.blockRegistry = UnifiedRegistries.Blocks.create(modId);
        this.entityRegistry = UnifiedRegistries.EntityTypes.create(modId);
    }

    public Woodset.Builder woodsetBuilder(String name, MapColor barkColor, MapColor plankColor) {
        return new Woodset.Builder(Identifier.fromNamespaceAndPath(this.modId, name), barkColor, plankColor, this.itemRegistry, this.blockRegistry, this.entityRegistry);
    }
}