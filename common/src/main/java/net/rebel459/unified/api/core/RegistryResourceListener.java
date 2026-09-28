package net.rebel459.unified.api.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.common.base.Suppliers;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.flag.FeatureFlags;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.impl.core.DataProviders;
import net.rebel459.unified.impl.platform.PlatformLoader;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

/** An extensible class for creating Registry listeners */
public abstract class RegistryResourceListener<T> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String JSON_EXTENSION = ".json";
    private static final Map<Identifier, RegistryResourceListener<?>> LISTENERS = new LinkedHashMap<>();
    private static ResourceIndex resourceIndex;
    private static boolean acceptingListeners = true;

    private final Identifier id;
    private final String directory;
    private final Codec<T> codec;
    private final ResourceKey<? extends Registry<?>> registry;
    private final List<Identifier> loadAfter;
    private State state = State.UNREGISTERED;

    protected RegistryResourceListener(Identifier id, Codec<T> codec, Identifier... loadAfter) {
        this(id, codec, null, loadAfter);
    }

    protected RegistryResourceListener(Identifier id, Codec<T> codec,
            ResourceKey<? extends Registry<?>> registry, Identifier... loadAfter) {
        this.id = id;
        this.directory = normalizeDirectory(id.getNamespace() + "/registry/" + id.getPath());
        this.codec = codec;
        this.registry = registry;
        this.loadAfter = List.copyOf(List.of(loadAfter));
    }

    /** Adds this listener to the bootstrap queue. */
    public final void init() {
        synchronized (RegistryResourceListener.class) {
            if (state != State.UNREGISTERED) return;
            if (!acceptingListeners) {
                throw new IllegalStateException("Registry resource listener " + id
                        + " was registered after bootstrap; register it from a RegistryResourceInitializer service provider");
            }

            RegistryResourceListener<?> existing = LISTENERS.putIfAbsent(id, this);
            if (existing != null && existing != this) throw new IllegalStateException("Duplicate registry resource listener: " + id);

            state = State.QUEUED;
        }
    }

    public static void completeRegistration() {
        synchronized (RegistryResourceListener.class) {
            if (!acceptingListeners) throw new IllegalStateException("Registry resource listener bootstrap already completed");
            acceptingListeners = false;
            processQueue();

            List<String> unresolved = LISTENERS.values().stream()
                    .filter(listener -> listener.state == State.QUEUED)
                    .map(listener -> listener.id + " after " + listener.loadAfter)
                    .toList();
            if (!unresolved.isEmpty()) {
                throw new IllegalStateException("Unresolved registry resource listener dependencies: " + unresolved);
            }
        }
    }

    /** Queues the registration code for a data-driven registry entry. */
    protected abstract void register(Identifier id, DeferredDeclaration<T> declaration);

    /** A declaration whose complete codec is evaluated only when its registry factory needs it. */
    public static final class DeferredDeclaration<T> implements java.util.function.Supplier<T> {
        private final Identifier resourceId;
        private final String source;
        private final JsonObject definition;
        private final java.util.function.Supplier<T> value;

        private DeferredDeclaration(Identifier resourceId, String source, JsonObject definition, Codec<T> codec) {
            this.resourceId = resourceId;
            this.source = source;
            this.definition = definition.deepCopy();
            this.value = Suppliers.memoize(() -> decodeFull(codec));
        }

        @Override
        public T get() {
            return value.get();
        }

        /** Decodes only priority & dependency metadata which must be known before registry factories are queued. */
        public <R> R decode(com.mojang.serialization.MapCodec<R> codec) {
            return decodeMetadata(codec.codec());
        }

        private <R> R decodeMetadata(Codec<R> codec) {
            return codec.parse(JsonOps.INSTANCE, definition)
                    .getOrThrow(error -> new IllegalArgumentException(resourceId + " from " + source + ": " + error));
        }

        private <R> R decodeFull(Codec<R> codec) {
            RegistryAccess registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
            return codec.parse(RegistryOps.create(JsonOps.INSTANCE, registries), definition)
                    .getOrThrow(error -> new IllegalArgumentException(resourceId + " from " + source + ": " + error));
        }
    }

    protected Identifier registryId(Identifier resourceId) {
        String path = resourceId.getPath();
        String prefix = directory + "/";
        if (!path.startsWith(prefix) || !path.endsWith(JSON_EXTENSION)) {
            throw new IllegalArgumentException("Resource is outside " + directory + ": " + resourceId);
        }
        return Identifier.fromNamespaceAndPath(resourceId.getNamespace(), path.substring(prefix.length(), path.length() - JSON_EXTENSION.length()));
    }

    private boolean dependenciesLoaded() {
        for (Identifier dependencyId : loadAfter) {
            RegistryResourceListener<?> dependency = LISTENERS.get(dependencyId);
            if (dependency == null || dependency.state != State.LOADED) return false;
        }
        return true;
    }

    private static void processQueue() {
        while (true) {
            RegistryResourceListener<?> next = null;
            for (RegistryResourceListener<?> listener : LISTENERS.values()) {
                if (listener.state == State.QUEUED && listener.dependenciesLoaded()) {
                    next = listener;
                    break;
                }
            }
            if (next == null) return;

            next.state = State.LOADING;
            try {
                if (resourceIndex == null) resourceIndex = ResourceIndex.create();
                next.load(resourceIndex);
                if (next.registry != null) StagedRegistry.finish(next.registry);
                next.state = State.LOADED;
            } catch (RuntimeException | Error exception) {
                next.state = State.FAILED;
                throw exception;
            }
        }
    }

    private void load(ResourceIndex index) {
        Map<Identifier, Candidate> declarations = index.forDirectory(directory);
        declarations.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .forEach(entry -> decodeAndRegister(entry.getKey(), entry.getValue()));
        LOGGER.info("Loaded {} static registry declarations from {}", declarations.size(), directory);
    }

    private void decodeAndRegister(Identifier resourceId, Candidate candidate) {
        try {
            if (candidate.requirement().isPresent() && !candidate.requirement().get().get()) {
                LOGGER.debug("Skipping {} because its load requirement was not met", resourceId);
                return;
            }
            Identifier registryId = registryId(resourceId);
            PlatformLoader.INSTANCE.internal().prepareRegistryNamespace(registryId.getNamespace());
            register(registryId, new DeferredDeclaration<>(resourceId, candidate.source(), candidate.definition(), codec));
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to init static registry declaration " + resourceId + " from " + candidate.source(), exception);
        }
    }

    private static List<PackEntry> openPacks() {
        List<PackEntry> packs = new ArrayList<>();
        int index = 0;
        for (Path root : PlatformLoader.INSTANCE.internal().getModResourceRoots()) addPack(packs, root, "mod-" + index++, false);

        Path gameDirectory = UnifiedPlatform.getGameDirectory();
        if (UnifiedPlatform.isModLoaded("simpleresourceloader")) {
            index = addPackDirectory(packs, gameDirectory.resolve("resources/common/required"), "srl-common-", index);
            addPackDirectory(packs, gameDirectory.resolve("resources/datapack/required"), "srl-data-", index);
        }
        if (UnifiedPlatform.isModLoaded("paxi")) addPaxiPacks(packs, gameDirectory);
        return packs;
    }

    private static int addPackDirectory(List<PackEntry> packs, Path directory, String idPrefix, int index) {
        if (!Files.isDirectory(directory)) return index;
        try (var children = Files.list(directory)) {
            for (Path child : children.sorted(Comparator.comparing(path -> path.getFileName().toString())).toList()) {
                if (isPack(child)) addPack(packs, child, idPrefix + index++, true);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to inspect static pack directory " + directory, exception);
        }
        return index;
    }

    private static void addPaxiPacks(List<PackEntry> packs, Path gameDirectory) {
        Path paxiDirectory = gameDirectory.resolve("config/paxi");
        Path packsDirectory = paxiDirectory.resolve("datapacks");
        if (!Files.isDirectory(packsDirectory)) return;

        Map<String, Path> discovered = new LinkedHashMap<>();
        try (var children = Files.list(packsDirectory)) {
            children.filter(RegistryResourceListener::isPack)
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .forEach(path -> discovered.put(path.getFileName().toString(), path));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to inspect Paxi packs in " + packsDirectory, exception);
        }

        int index = 0;
        for (String orderedName : readPaxiOrder(paxiDirectory.resolve("datapack_load_order.json"))) {
            Path path = discovered.remove(orderedName);
            if (path == null) {
                Path relative = gameDirectory.resolve(orderedName).normalize();
                if (relative.startsWith(gameDirectory.normalize()) && isPack(relative)) path = relative;
            }
            if (path != null) addPack(packs, path, "paxi-" + index++, true);
        }
        for (Path path : discovered.values()) addPack(packs, path, "paxi-" + index++, true);
    }

    private static List<String> readPaxiOrder(Path orderFile) {
        if (!Files.isRegularFile(orderFile)) return List.of();
        try (Reader reader = Files.newBufferedReader(orderFile)) {
            JsonElement root = JsonParser.parseReader(reader);
            JsonElement values = root.isJsonArray() ? root : root.isJsonObject() && root.getAsJsonObject().has("loadOrder") ? root.getAsJsonObject().get("loadOrder") : null;
            if (values == null || !values.isJsonArray()) return List.of();

            List<String> result = new ArrayList<>();
            values.getAsJsonArray().forEach(value -> result.add(value.getAsString()));
            return result;
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to read Paxi init order " + orderFile, exception);
        }
    }

    private static boolean isPack(Path path) {
        return Files.isDirectory(path) || Files.isRegularFile(path) && path.getFileName().toString().endsWith(".zip");
    }

    private static void addPack(List<PackEntry> packs, Path path, String id, boolean external) {
        PackLocationInfo info = new PackLocationInfo(id, Component.literal(id), PackSource.BUILT_IN, Optional.empty());
        if (Files.isDirectory(path)) {
            packs.add(new PackEntry(new PathPackResources(info, path), external));
        } else if (Files.isRegularFile(path)) {
            packs.add(new PackEntry(
                    new FilePackResources.FileResourcesSupplier(path)
                            .openResources(info, new Pack.Metadata(
                                    Component.literal(id),
                                    PackCompatibility.COMPATIBLE,
                                    FeatureFlags.DEFAULT_FLAGS,
                                    List.of()
                            ))
                            .findFirst()
                            .orElseThrow(),
                    external
            ));
        }
    }

    private record PackEntry(PackResources resources, boolean external) {}

    private record Candidate(JsonObject definition, int priority, Optional<ExtensibleCodec.Entry<Boolean>> requirement, boolean external, long sequence, String source) {
        private boolean supersedes(Candidate other) {
            if (priority != other.priority) return priority > other.priority;
            if (external != other.external) return external;
            return sequence > other.sequence;
        }
    }

    private static final class ResourceIndex {
        private final Map<String, Map<Identifier, Candidate>> candidates = new HashMap<>();
        private final List<PackEntry> packs;

        private ResourceIndex(List<PackEntry> packs) {
            this.packs = packs;
        }

        private static ResourceIndex create() {
            return new ResourceIndex(openPacks());
        }

        private synchronized Map<Identifier, Candidate> forDirectory(String directory) {
            Map<Identifier, Candidate> existing = candidates.get(directory);
            if (existing != null) return existing;

            Map<Identifier, Candidate> indexed = new HashMap<>();
            long[] sequence = {0};
            for (PackEntry pack : packs) {
                for (String namespace : pack.resources().getNamespaces(PackType.SERVER_DATA)) {
                    pack.resources().listResources(PackType.SERVER_DATA, namespace, directory, (resourceId, supplier) -> {
                        if (!resourceId.getPath().endsWith(JSON_EXTENSION)) return;

                        try (Reader reader = new InputStreamReader(supplier.get())) {
                            JsonElement parsed = JsonParser.parseReader(reader);
                            if (!parsed.isJsonObject()) throw new IllegalArgumentException("Root must be a JSON object");
                            JsonObject definition = parsed.getAsJsonObject().deepCopy();
                            DataProviders.PriorityAndRequirement metadata = DataProviders.PriorityAndRequirement.CODEC.parse(JsonOps.INSTANCE, definition)
                                    .getOrThrow(error -> new IllegalArgumentException(resourceId + ": " + error));
                            definition.remove("priority");
                            definition.remove("load_requirements");
                            Candidate candidate = new Candidate(definition, metadata.priority(), metadata.requirement(), pack.external(), sequence[0]++, pack.resources().packId());
                            Candidate previous = indexed.get(resourceId);
                            if (previous == null || candidate.supersedes(previous)) indexed.put(resourceId, candidate);
                        } catch (Exception exception) {
                            throw new IllegalStateException("Failed to index static registry declaration " + resourceId + " from " + pack.resources().packId(), exception);
                        }
                    });
                }
            }
            Map<Identifier, Candidate> result = Map.copyOf(indexed);
            candidates.put(directory, result);
            return result;
        }
    }

    private static String normalizeDirectory(String path) {
        String normalized = path.replace('\\', '/');
        while (normalized.startsWith("/")) normalized = normalized.substring(1);
        while (normalized.endsWith("/")) normalized = normalized.substring(0, normalized.length() - 1);
        if (normalized.isEmpty()) throw new IllegalArgumentException("Registry resource path cannot be empty");
        return normalized;
    }

    private enum State {
        UNREGISTERED,
        QUEUED,
        LOADING,
        LOADED,
        FAILED
    }
}
