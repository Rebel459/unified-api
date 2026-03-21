package net.rebel459.unified.platform;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerLifecycleEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class NeoForgeUnifiedEvents {

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener((ModifyDefaultComponentsEvent event) -> {
            BuiltInRegistries.ITEM.forEach(item -> {
                event.modify(item, builder -> {
                    UnifiedEvents.ItemComponents.passModify(item, builder);
                });
            });
        });

        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            UnifiedEvents.Players.passOnJoin(event.getEntity());
        });

        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> {
            UnifiedEvents.Players.passOnLeave(event.getEntity());
        });

        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerRespawnEvent event) -> {
            UnifiedEvents.Players.passOnRespawn(event.getEntity());
        });

        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> {
            UnifiedEvents.Commands.passRegister(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
        });

        NeoForge.EVENT_BUS.addListener((TagsUpdatedEvent event) -> {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD && server != null) {
                UnifiedEvents.Servers.passOnDatapackLoad(server);
            }
        });

        NeoForge.EVENT_BUS.addListener((ServerStartedEvent event) -> {
            UnifiedEvents.Servers.passOnStart(event.getServer());
        });

        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> {
            UnifiedEvents.Servers.passOnStop(event.getServer());
        });

        NeoForge.EVENT_BUS.addListener((LootTableLoadEvent event) -> {
            UnifiedEvents.LootTables.passModify(event.getKey(), builder -> event.getTable().addPool(builder.build()), event.getRegistries());
        });
    }
}
