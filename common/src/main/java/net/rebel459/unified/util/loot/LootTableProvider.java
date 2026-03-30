package net.rebel459.unified.util.loot;

import com.google.common.collect.ImmutableList;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

public final class LootTableProvider {

    private static final Field LOOT_POOL_BUILDER_ENTRIES = findField(LootPool.Builder.class, "entries");
    private static final Field LOOT_POOL_BUILDER_CONDITIONS = findField(LootPool.Builder.class, "conditions");
    private static final Field LOOT_POOL_BUILDER_FUNCTIONS = findField(LootPool.Builder.class, "functions");
    private static final Field LOOT_POOL_BUILDER_ROLLS = findField(LootPool.Builder.class, "rolls");
    private static final Field LOOT_POOL_BUILDER_BONUS_ROLLS = findField(LootPool.Builder.class, "bonusRolls");
    private static final Field LOOT_ITEM_ITEM = findField(LootItem.class, "item");
    private static final Field IMMUTABLE_LIST_BUILDER_CONTENTS = findField(ImmutableList.Builder.class, "contents");
    private static final Field IMMUTABLE_LIST_BUILDER_SIZE = findField(ImmutableList.Builder.class, "size");
    private static final Field LOOT_TABLE_POOLS = findField(LootTable.class, "pools");
    private static final Field LOOT_TABLE_FUNCTIONS = findField(LootTable.class, "functions");
    private static final Field LOOT_TABLE_RANDOM_SEQUENCE = findField(LootTable.class, "randomSequence");
    private static final Field LOOT_TABLE_BUILDER_FUNCTIONS = findField(LootTable.Builder.class, "functions");
    private static final Field LOOT_POOL_ENTRIES = findField(LootPool.class, "entries");
    private static final Field LOOT_POOL_CONDITIONS = findField(LootPool.class, "conditions");
    private static final Field LOOT_POOL_FUNCTIONS = findField(LootPool.class, "functions");
    private static final Field LOOT_POOL_ROLLS = findField(LootPool.class, "rolls");
    private static final Field LOOT_POOL_BONUS_ROLLS = findField(LootPool.class, "bonusRolls");

    private LootTableProvider() {}

    @SuppressWarnings("unchecked")
    public static List<LootPoolEntryContainer> getEntries(LootPool.Builder pool) {
        try {
            ImmutableList.Builder<LootPoolEntryContainer> builder = (ImmutableList.Builder<LootPoolEntryContainer>) LOOT_POOL_BUILDER_ENTRIES.get(pool);
            Object[] contents = (Object[]) IMMUTABLE_LIST_BUILDER_CONTENTS.get(builder);
            int size = IMMUTABLE_LIST_BUILDER_SIZE.getInt(builder);

            return java.util.Arrays.stream(contents, 0, size)
                    .map(LootPoolEntryContainer.class::cast)
                    .toList();
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Unable to read loot pool entries", exception);
        }
    }

    public static void setEntries(LootPool.Builder pool, List<LootPoolEntryContainer> entries) {
        try {
            LOOT_POOL_BUILDER_ENTRIES.set(pool, immutableBuilder(entries));
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Unable to replace loot pool entries", exception);
        }
    }

    public static void setConditions(LootPool.Builder pool, List<LootItemCondition> conditions) {
        try {
            LOOT_POOL_BUILDER_CONDITIONS.set(pool, immutableBuilder(conditions));
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Unable to replace loot pool conditions", exception);
        }
    }

    public static void setFunctions(LootPool.Builder pool, List<LootItemFunction> functions) {
        try {
            LOOT_POOL_BUILDER_FUNCTIONS.set(pool, immutableBuilder(functions));
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Unable to replace loot pool functions", exception);
        }
    }

    public static void setRolls(LootPool.Builder pool, NumberProvider rolls) {
        try {
            LOOT_POOL_BUILDER_ROLLS.set(pool, rolls);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Unable to replace loot pool rolls", exception);
        }
    }

    public static void setBonusRolls(LootPool.Builder pool, NumberProvider bonusRolls) {
        try {
            LOOT_POOL_BUILDER_BONUS_ROLLS.set(pool, bonusRolls);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Unable to replace loot pool bonus rolls", exception);
        }
    }

    public static Object getItem(LootPoolEntryContainer entry) {
        if (!(entry instanceof LootItem lootItem)) {
            return null;
        }

        try {
            return LOOT_ITEM_ITEM.get(lootItem);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Unable to read loot item entry", exception);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<LootPool> getPools(LootTable table) {
        try {
            return (List<LootPool>) LOOT_TABLE_POOLS.get(table);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Unable to read loot table pools", exception);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<LootItemFunction> getFunctions(LootTable table) {
        try {
            return (List<LootItemFunction>) LOOT_TABLE_FUNCTIONS.get(table);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Unable to read loot table functions", exception);
        }
    }

    @SuppressWarnings("unchecked")
    public static Optional<Identifier> getRandomSequence(LootTable table) {
        try {
            return (Optional<Identifier>) LOOT_TABLE_RANDOM_SEQUENCE.get(table);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Unable to read loot table random sequence", exception);
        }
    }

    public static void setFunctions(LootTable.Builder table, List<LootItemFunction> functions) {
        try {
            LOOT_TABLE_BUILDER_FUNCTIONS.set(table, immutableBuilder(functions));
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Unable to replace loot table functions", exception);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<LootPoolEntryContainer> getEntries(LootPool pool) {
        try {
            return (List<LootPoolEntryContainer>) LOOT_POOL_ENTRIES.get(pool);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Unable to read loot pool entries", exception);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<LootItemCondition> getConditions(LootPool pool) {
        try {
            return (List<LootItemCondition>) LOOT_POOL_CONDITIONS.get(pool);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Unable to read loot pool conditions", exception);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<LootItemFunction> getFunctions(LootPool pool) {
        try {
            return (List<LootItemFunction>) LOOT_POOL_FUNCTIONS.get(pool);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Unable to read loot pool functions", exception);
        }
    }

    public static NumberProvider getRolls(LootPool pool) {
        try {
            return (NumberProvider) LOOT_POOL_ROLLS.get(pool);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Unable to read loot pool rolls", exception);
        }
    }

    public static NumberProvider getBonusRolls(LootPool pool) {
        try {
            return (NumberProvider) LOOT_POOL_BONUS_ROLLS.get(pool);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Unable to read loot pool bonus rolls", exception);
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

    private static <T> ImmutableList.Builder<T> immutableBuilder(List<T> values) {
        ImmutableList.Builder<T> builder = new ImmutableList.Builder<>();
        for (T value : values) {
            builder.add(value);
        }
        return builder;
    }
}
