package net.rebel459.unified.util.event;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;

import java.lang.reflect.Field;
import java.util.List;

public final class LootTableProvider {

    private static final Field IMMUTABLE_LIST_BUILDER_CONTENTS = findField(ImmutableList.Builder.class, "contents");
    private static final Field IMMUTABLE_LIST_BUILDER_SIZE = findField(ImmutableList.Builder.class, "size");

    private LootTableProvider() {}

    public static List<LootPoolEntryContainer> getEntries(LootPool.Builder pool) {
        try {
            ImmutableList.Builder<LootPoolEntryContainer> builder = pool.entries;
            Object[] contents = (Object[]) IMMUTABLE_LIST_BUILDER_CONTENTS.get(builder);
            int size = IMMUTABLE_LIST_BUILDER_SIZE.getInt(builder);

            return java.util.Arrays.stream(contents, 0, size)
                    .map(LootPoolEntryContainer.class::cast)
                    .toList();
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Unable to read loot pool entries", exception);
        }
    }

    private static Field findField(Class<?> owner, String name) {
        try {
            Field field = owner.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to access field " + owner.getName() + "#" + name, exception);
        }
    }

    public static <T> ImmutableList.Builder<T> immutableBuilder(List<T> values) {
        ImmutableList.Builder<T> builder = new ImmutableList.Builder<>();
        for (T value : values) {
            builder.add(value);
        }
        return builder;
    }
}
