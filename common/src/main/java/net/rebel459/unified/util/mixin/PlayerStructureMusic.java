package net.rebel459.unified.util.mixin;

import net.minecraft.resources.Identifier;

public interface PlayerStructureMusic {

    Identifier getPieceStructure();
    Identifier getBoxStructure();

    void setPieceStructure(Identifier id);
    void setBoxStructure(Identifier id);
}
