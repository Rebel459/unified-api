package net.rebel459.unified.util;

import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;

import java.util.Optional;

public record LootEntry(Type type, Optional<LootPoolEntryContainer.Builder<?>> entry) {

    public static LootEntry insert(LootPoolEntryContainer.Builder<?> entry) {
        return new LootEntry(Type.INSERT, Optional.of(entry));
    }
    public static LootEntry replace(LootPoolEntryContainer.Builder<?> entry) {
        return new LootEntry(Type.REPLACE, Optional.of(entry));
    }
    public static LootEntry remove() {
        return new LootEntry(Type.REMOVE, Optional.empty());
    }

    public enum Type {
        INSERT,
        REPLACE,
        REMOVE,
    }
}
