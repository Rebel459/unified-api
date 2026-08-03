package net.rebel459.unified.impl.helper;

import net.minecraft.resources.Identifier;

import java.util.Map;

public interface PlayerStructureMusic {

    Identifier getPieceStructure();
    Identifier getBoxStructure();

    boolean getReplaceCurrentMusic();

    Map<StructureMusicImpl.Target, StructureMusicImpl.Info> getStructureMusic();

    int getPlayerGroup();

    void setPieceStructure(Identifier id);
    void setBoxStructure(Identifier id);

    void setReplaceCurrentMusic(boolean replaceCurrentMusic);

    void setStructureMusic(Map<StructureMusicImpl.Target, StructureMusicImpl.Info> structureMusic);

    void setPlayerGroup(int playerGroup);
}
