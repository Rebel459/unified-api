package net.rebel459.unified.util.mixin;

import net.minecraft.resources.Identifier;
import net.rebel459.unified.util.helper.StructureMusicImpl;

import java.util.HashMap;
import java.util.Map;

public interface PlayerStructureMusic {

    Identifier getPieceStructure();
    Identifier getBoxStructure();

    boolean getReplaceCurrentMusic();

    Map<Identifier, StructureMusicImpl.Record> getStructureMusic();

    int getPlayerGroup();

    void setPieceStructure(Identifier id);
    void setBoxStructure(Identifier id);

    void setReplaceCurrentMusic(boolean replaceCurrentMusic);

    void setStructureMusic(Map<Identifier, StructureMusicImpl.Record> structureMusic);

    void setPlayerGroup(int playerGroup);
}
