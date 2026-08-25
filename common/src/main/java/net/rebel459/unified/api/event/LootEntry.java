package net.rebel459.unified.api.event;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.rebel459.unified.util.event.LootTableProvider;

import java.util.Optional;
import java.util.function.Function;

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

    public enum Type implements StringRepresentable {
        INSERT("insert"),
        REPLACE("replace"),
        REMOVE("remove"),;

        private final String name;

        Type(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        private static final Codec<Type> CODEC = StringRepresentable.fromEnum(LootEntry.Type::values);
    }

    private static LootEntry createForCodec(Type type, LootPoolEntryContainer entry) {
        return new LootEntry(type, Optional.of(LootTableProvider.entryBuilder(entry)));
    }

    private static final MapCodec<LootPoolEntryContainer> ENTRY_CODEC = BuiltInRegistries.LOOT_POOL_ENTRY_TYPE.byNameCodec().dispatch(LootPoolEntryContainer::codec, Function.identity()).fieldOf("entry");

    private static final MapCodec<LootEntry> INSERT_CODEC = ENTRY_CODEC.xmap(container -> createForCodec(Type.INSERT, container), entry -> entry.entry().orElseThrow().build());

    private static final MapCodec<LootEntry> REPLACE_CODEC = ENTRY_CODEC.xmap(container -> createForCodec(Type.REPLACE, container), entry -> entry.entry().orElseThrow().build());

    private static final MapCodec<LootEntry> REMOVE_CODEC = MapCodec.unit(LootEntry::remove);

    public static final MapCodec<LootEntry> MAP_CODEC = Type.CODEC.dispatchMap(
            "type",
            lootEntry -> lootEntry.type,
            type -> switch (type) {
                case INSERT -> INSERT_CODEC;
                case REPLACE -> REPLACE_CODEC;
                case REMOVE -> REMOVE_CODEC;
            }
    );
}
