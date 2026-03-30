package net.rebel459.unified.util.helper;

import net.minecraft.resources.Identifier;
import net.minecraft.sounds.Music;

public interface StructureMusic {

    default void add(Identifier structure, Music music) {
        add(structure, music, false);
    }

    default void add(Identifier structure, Music music, boolean fullBox) {
        StructureMusicImpl.STRUCTURE_MUSIC.put(structure, new StructureMusicImpl.Record(music, fullBox));
    }
}
