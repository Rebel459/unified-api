package net.rebel459.unified.impl.core;

import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

public final class DataProvider<T> {
    private static final Set<Identifier> IDS = new HashSet<>();

    private final Identifier id;
    private final Function<? super T, ?> key;
    private final Map<String, List<T>> requests = new LinkedHashMap<>();

    public static <T> DataProvider<T> create(Identifier id) {
        return new DataProvider<>(id, null);
    }

    public static <T> DataProvider<T> keyed(Identifier id, Function<? super T, ?> key) {
        return new DataProvider<>(id, Objects.requireNonNull(key, "key"));
    }

    private DataProvider(Identifier id, Function<? super T, ?> key) {
        this.id = Objects.requireNonNull(id, "id");
        this.key = key;
        synchronized (IDS) {
            if (!IDS.add(id)) throw new IllegalArgumentException("Duplicate data provider channel " + id);
        }
    }

    public Identifier id() {
        return id;
    }

    public synchronized void add(String modId, T request) {
        Objects.requireNonNull(modId, "modId");
        Objects.requireNonNull(request, "request");
        List<T> values = requests.computeIfAbsent(modId, ignored -> new ArrayList<>());
        if (key != null) {
            Object requestKey = Objects.requireNonNull(key.apply(request), "request key");
            if (values.stream().map(key).anyMatch(requestKey::equals)) {
                throw new IllegalArgumentException("Duplicate request " + requestKey + " in " + id + " for " + modId);
            }
        }
        values.add(request);
    }

    public synchronized List<T> requests(String modId) {
        return List.copyOf(requests.getOrDefault(modId, List.of()));
    }

    @Override
    public String toString() {
        return id.toString();
    }
}
