package net.rebel459.unified.util.registry;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.rebel459.unified.platform.InternalHandlerImpl;
import net.rebel459.unified.platform.UnifiedPlatform;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** An extensible class for creating Registry listeners */
public abstract class RegistryResourceListener<T> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String JSON_EXTENSION = ".json";

    private final String directory;
    private final Codec<T> codec;
    private boolean initialized;

    protected RegistryResourceListener(Identifier id, Codec<T> codec) {
        this.directory = normalizeDirectory(id.getNamespace() + "/registry/" + id.getPath());
        this.codec = codec;
    }

    /** Must be called once during registration */
    public final synchronized void init() {
        if (initialized) return;
        initialized = true;

        List<PackResources> packs = openPacks();
        try (MultiPackResourceManager resources = new MultiPackResourceManager(PackType.SERVER_DATA, packs)) {
            Map<Identifier, Resource> declarations = resources.listResources(directory,
                    id -> id.getPath().endsWith(JSON_EXTENSION));

            declarations.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> decodeAndRegister(entry.getKey(), entry.getValue()));

            LOGGER.info("Loaded {} static registry declarations from {}", declarations.size(), directory);
        }
    }

    /** Run code for registration of each data-driven registry entry */
    protected abstract void register(Identifier id, T declaration);

    protected Identifier registryId(Identifier resourceId) {
        String path = resourceId.getPath();
        String prefix = directory + "/";
        if (!path.startsWith(prefix) || !path.endsWith(JSON_EXTENSION)) {
            throw new IllegalArgumentException("Resource is outside " + directory + ": " + resourceId);
        }
        return Identifier.fromNamespaceAndPath(resourceId.getNamespace(),
                path.substring(prefix.length(), path.length() - JSON_EXTENSION.length()));
    }

    private void decodeAndRegister(Identifier resourceId, Resource resource) {
        try (Reader reader = resource.openAsReader()) {
            JsonElement json = JsonParser.parseReader(reader);
            T declaration = codec.parse(JsonOps.INSTANCE, json)
                    .getOrThrow(error -> new IllegalArgumentException(resourceId + ": " + error));
            Identifier registryId = registryId(resourceId);
            InternalHandlerImpl.INSTANCE.impl().prepareRegistryNamespace(registryId.getNamespace());
            register(registryId, declaration);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to init static registry declaration " + resourceId, exception);
        }
    }

    private List<PackResources> openPacks() {
        List<PackResources> packs = new ArrayList<>();
        int index = 0;
        for (Path root : InternalHandlerImpl.INSTANCE.impl().getModResourceRoots()) {
            addPack(packs, root, "mod-" + index++);
        }

        Path gameDirectory = InternalHandlerImpl.INSTANCE.impl().getGameDirectory();
        if (UnifiedPlatform.isModLoaded("simpleresourceloader")) {
            index = addPackDirectory(packs, gameDirectory.resolve("resources/common/required"), "srl-common-", index);
            addPackDirectory(packs, gameDirectory.resolve("resources/datapack/required"), "srl-data-", index);
        }
        if (UnifiedPlatform.isModLoaded("paxi")) {
            addPaxiPacks(packs, gameDirectory);
        }
        return packs;
    }

    private static int addPackDirectory(List<PackResources> packs, Path directory, String idPrefix, int index) {
        if (!Files.isDirectory(directory)) return index;
        try (var children = Files.list(directory)) {
            for (Path child : children.sorted(Comparator.comparing(path -> path.getFileName().toString())).toList()) {
                if (isPack(child)) addPack(packs, child, idPrefix + index++);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to inspect static pack directory " + directory, exception);
        }
        return index;
    }

    private static void addPaxiPacks(List<PackResources> packs, Path gameDirectory) {
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
            if (path != null) addPack(packs, path, "paxi-" + index++);
        }
        for (Path path : discovered.values()) addPack(packs, path, "paxi-" + index++);
    }

    private static List<String> readPaxiOrder(Path orderFile) {
        if (!Files.isRegularFile(orderFile)) return List.of();
        try (Reader reader = Files.newBufferedReader(orderFile)) {
            JsonElement root = JsonParser.parseReader(reader);
            JsonElement values = root.isJsonArray() ? root
                    : root.isJsonObject() && root.getAsJsonObject().has("loadOrder")
                    ? root.getAsJsonObject().get("loadOrder") : null;
            if (values == null || !values.isJsonArray()) return List.of();
            List<String> result = new ArrayList<>();
            values.getAsJsonArray().forEach(value -> result.add(value.getAsString()));
            return result;
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to read Paxi init order " + orderFile, exception);
        }
    }

    private static boolean isPack(Path path) {
        return Files.isDirectory(path)
                || Files.isRegularFile(path) && path.getFileName().toString().endsWith(".zip");
    }

    private static void addPack(List<PackResources> packs, Path path, String id) {
        PackLocationInfo info = new PackLocationInfo(id, Component.literal(id), PackSource.BUILT_IN, Optional.empty());
        if (Files.isDirectory(path)) {
            packs.add(new PathPackResources(info, path));
        } else if (Files.isRegularFile(path)) {
            packs.add(new FilePackResources.FileResourcesSupplier(path).openPrimary(info));
        }
    }

    private static String normalizeDirectory(String path) {
        String normalized = path.replace('\\', '/');
        while (normalized.startsWith("/")) normalized = normalized.substring(1);
        while (normalized.endsWith("/")) normalized = normalized.substring(0, normalized.length() - 1);
        if (normalized.isEmpty()) throw new IllegalArgumentException("Registry resource path cannot be empty");
        return normalized;
    }
}
