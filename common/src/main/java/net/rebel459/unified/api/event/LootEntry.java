package net.rebel459.unified.api.event;

import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;

import java.util.Optional;

public class LootEntry {

    private final Type type;
    private final Optional<LootPoolEntryContainer.Builder<?>> entry;

    private LootEntry(Type type, Optional<LootPoolEntryContainer.Builder<?>> entry) {
        this.type = type;
        this.entry = entry;
    }

    public Type getType() {
        return type;
    }
    public Optional<LootPoolEntryContainer.Builder<?>> getEntry() {
        return entry;
    }

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
