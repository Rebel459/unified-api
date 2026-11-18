package net.rebel459.unified.impl.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.phys.AABB;
import net.rebel459.unified.api.client.core.UnifiedClientEvents;
import net.rebel459.unified.api.event.EventTiming;
import net.rebel459.unified.impl.util.MarkedBlocks;
import net.rebel459.unified.impl.util.ProcessorStick;

public final class MarkedBlocksOverlay {

    private MarkedBlocksOverlay() {}

    private static final int RANGE = 64;
    private static final double PADDING = 0.005;
    private static final GizmoStyle STYLE = GizmoStyle.strokeAndFill(0xFF55FF55, 2.0F, 0x4055FF55);

    public static void init() {
        MarkedBlocks.setClientActivator(() -> Minecraft.getInstance().execute(() -> UnifiedClientEvents.Instance.onTick(EventTiming.POST, MarkedBlocksOverlay::tick)));
    }

    private static void tick(Minecraft minecraft) {
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null || !ProcessorStick.isHolding(player)) return;
        BlockPos origin = player.blockPosition();
        try (Gizmos.TemporaryCollection ignored = minecraft.collectPerTickGizmos()) {
            MarkedBlocks.forEachClient(minecraft.level.dimension(), packed -> {
                int x = BlockPos.getX(packed);
                int y = BlockPos.getY(packed);
                int z = BlockPos.getZ(packed);
                if (Math.abs(x - origin.getX()) > RANGE || Math.abs(y - origin.getY()) > RANGE || Math.abs(z - origin.getZ()) > RANGE) return;
                Gizmos.cuboid(new AABB(x - PADDING, y - PADDING, z - PADDING, x + 1 + PADDING, y + 1 + PADDING, z + 1 + PADDING), STYLE);
            });
        }
    }
}
