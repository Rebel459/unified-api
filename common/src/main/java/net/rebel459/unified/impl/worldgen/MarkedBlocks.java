package net.rebel459.unified.impl.worldgen;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.Level;
import net.rebel459.unified.Unified;
import net.rebel459.unified.api.core.UnifiedAttachments;
import net.rebel459.unified.api.core.UnifiedHelpers;
import net.rebel459.unified.api.core.UnifiedRegistries;
import net.rebel459.unified.impl.network.MarkedBlocksPacket;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.LongConsumer;
import java.util.stream.LongStream;

public final class MarkedBlocks {

    private MarkedBlocks() {}

    public static final String TEMPLATE_KEY = "unified_marked";
    public static final long[] EMPTY = new long[0];
    private static final int FALLBACK_RANGE = 32;
    private static final int SYNC_BATCH = 8192;
    private static final MarkedBlocksPacket PROBE = new MarkedBlocksPacket(Level.OVERWORLD, false, EMPTY, EMPTY);

    public static UnifiedAttachments.Level<LongSet> MARKED;

    private static final Set<ServerGamePacketListenerImpl> SUBSCRIBED = Collections.newSetFromMap(new WeakHashMap<>());
    private static final Map<ResourceKey<Level>, LongSet> CLIENT = new ConcurrentHashMap<>();
    private static final AtomicBoolean CLIENT_ACTIVE = new AtomicBoolean();
    private static volatile Runnable clientActivator;

    public static void initRegistries() {
        MARKED = UnifiedAttachments.Level.<LongSet>builder(Unified.id("marked_blocks"), () -> LongSets.EMPTY_SET)
                .persistent(Codec.LONG_STREAM.<LongSet>xmap(stream -> new LongOpenHashSet(stream.toArray()), set -> LongStream.of(set.toLongArray())).fieldOf("positions"))
                .build();
        UnifiedRegistries.DeferredRegistry.create(Unified.MOD_ID, BuiltInRegistries.FEATURE).register("template_pool", TemplatePoolFeature.Type::new);
    }

    public static void init() {
        UnifiedHelpers.NETWORKING.registerPlayToClient(MarkedBlocksPacket.TYPE, MarkedBlocksPacket.CODEC, (packet, player) -> receive(packet));
    }

    public static void setClientActivator(Runnable activator) {
        clientActivator = activator;
    }

    public static void reveal(ServerPlayer player) {
        if (player.connection == null || !canSync(player) || !SUBSCRIBED.add(player.connection)) return;
        for (ServerLevel level : player.level().getServer().getAllLevels()) {
            sendAll(level, player);
        }
    }

    public static boolean canSync(ServerPlayer player) {
        return UnifiedHelpers.NETWORKING.canSend(PROBE, player);
    }

    public static void showNearby(ServerLevel level, ServerPlayer player) {
        BlockPos origin = player.blockPosition();
        for (long packed : MARKED.get(level)) {
            int x = BlockPos.getX(packed);
            int y = BlockPos.getY(packed);
            int z = BlockPos.getZ(packed);
            if (Math.abs(x - origin.getX()) > FALLBACK_RANGE || Math.abs(y - origin.getY()) > FALLBACK_RANGE || Math.abs(z - origin.getZ()) > FALLBACK_RANGE) continue;
            level.sendParticles(player, ParticleTypes.HAPPY_VILLAGER, true, false, x + 0.5, y + 0.5, z + 0.5, 6, 0.35, 0.35, 0.35, 0.0);
        }
    }

    public static boolean toggle(ServerLevel level, BlockPos pos) {
        LongSet marked = new LongOpenHashSet(MARKED.get(level));
        long packed = pos.asLong();
        boolean added = marked.add(packed);
        LongArrayList removed = new LongArrayList();
        if (!added) {
            marked.remove(packed);
            removed.add(packed);
        }
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        LongIterator iterator = marked.iterator();
        while (iterator.hasNext()) {
            long other = iterator.nextLong();
            if (other == packed || !level.isLoaded(cursor.set(other)) || !level.getBlockState(cursor).isAir()) continue;
            iterator.remove();
            removed.add(other);
        }
        MARKED.set(level, marked);
        if (!SUBSCRIBED.isEmpty()) {
            MarkedBlocksPacket packet = new MarkedBlocksPacket(level.dimension(), false, added ? new long[]{packed} : EMPTY, removed.toLongArray());
            for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
                if (SUBSCRIBED.contains(player.connection)) UnifiedHelpers.NETWORKING.send(packet, player);
            }
        }
        return added;
    }

    public static long[] collect(Level level, BlockPos position, Vec3i size) {
        if (!(level instanceof ServerLevel serverLevel)) return EMPTY;
        LongSet marked = MARKED.get(serverLevel);
        if (marked.isEmpty()) return EMPTY;
        BlockPos corner = position.offset(size).offset(-1, -1, -1);
        int minX = Math.min(position.getX(), corner.getX());
        int minY = Math.min(position.getY(), corner.getY());
        int minZ = Math.min(position.getZ(), corner.getZ());
        int maxX = Math.max(position.getX(), corner.getX());
        int maxY = Math.max(position.getY(), corner.getY());
        int maxZ = Math.max(position.getZ(), corner.getZ());
        LongArrayList collected = new LongArrayList();
        for (long packed : marked) {
            int x = BlockPos.getX(packed);
            int y = BlockPos.getY(packed);
            int z = BlockPos.getZ(packed);
            if (x < minX || y < minY || z < minZ || x > maxX || y > maxY || z > maxZ) continue;
            collected.add(BlockPos.asLong(x - minX, y - minY, z - minZ));
        }
        return collected.isEmpty() ? EMPTY : collected.toLongArray();
    }

    public static void forEachClient(ResourceKey<Level> dimension, LongConsumer consumer) {
        LongSet marked = CLIENT.get(dimension);
        if (marked == null) return;
        synchronized (marked) {
            marked.forEach(consumer);
        }
    }

    public static void resetClient() {
        CLIENT.clear();
    }

    private static void receive(MarkedBlocksPacket packet) {
        Runnable activator = clientActivator;
        if (activator != null && CLIENT_ACTIVE.compareAndSet(false, true)) activator.run();
        LongSet marked = packet.reset() ? new LongOpenHashSet() : CLIENT.computeIfAbsent(packet.dimension(), _ -> new LongOpenHashSet());
        synchronized (marked) {
            for (long packed : packet.added()) marked.add(packed);
            for (long packed : packet.removed()) marked.remove(packed);
        }
        if (packet.reset()) CLIENT.put(packet.dimension(), marked);
    }

    private static void sendAll(ServerLevel level, ServerPlayer player) {
        long[] marked = MARKED.get(level).toLongArray();
        int start = 0;
        do {
            int end = Math.min(start + SYNC_BATCH, marked.length);
            UnifiedHelpers.NETWORKING.send(new MarkedBlocksPacket(level.dimension(), start == 0, Arrays.copyOfRange(marked, start, end), EMPTY), player);
            start = end;
        } while (start < marked.length);
    }
}
