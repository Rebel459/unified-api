package net.rebel459.unified.util.helper;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.Music;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.rebel459.unified.util.helper.impl.StructureMusicImpl;

public interface StructureMusic {

    default void add(Identifier structure, Music music) {
        add(structure, music, false);
    }
    default void add(ResourceKey<Structure> structure, Music music) {
        add(structure, music, false);
    }
    default void add(TagKey<Structure> structure, Music music) {
        add(structure, music, false);
    }

    default void add(Identifier structure, Music music, boolean fullBox) {
        StructureMusicImpl.addStructure(structure, music, fullBox);
    }
    default void add(ResourceKey<Structure> structure, Music music, boolean fullBox) {
        add(structure.identifier(), music, fullBox);
    }
    default void add(TagKey<Structure> structure, Music music, boolean fullBox) {
        StructureMusicImpl.addStructureTag(structure, music, fullBox);
    }
}
