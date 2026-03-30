package net.rebel459.unified.util.helper;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.Music;
import net.minecraft.world.level.levelgen.structure.Structure;

public interface StructureMusic {

    default void add(Identifier structure, Music music) {
        add(structure, music, false);
    }
    default void add(ResourceKey<Structure> structure, Music music) {
        add(structure.identifier(), music);
    }

    default void add(Identifier structure, Music music, boolean fullBox) {
        StructureMusicImpl.STRUCTURE_MUSIC.put(structure, new StructureMusicImpl.Record(music, fullBox));
    }
    default void add(ResourceKey<Structure> structure, Music music, boolean fullBox) {
        add(structure.identifier(), music, fullBox);
    }
}
