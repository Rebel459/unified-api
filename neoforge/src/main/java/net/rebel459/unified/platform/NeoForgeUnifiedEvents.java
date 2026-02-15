package net.rebel459.unified.platform;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class NeoForgeUnifiedEvents {

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener((ModifyDefaultComponentsEvent event) -> {
            BuiltInRegistries.ITEM.forEach(item -> {
                event.modify(item, builder -> {
                    UnifiedEvents.ModifyItemComponents.pass(item, wrapperBuilder(builder));
                });
            });
        });

        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            UnifiedEvents.Player.passJoin(event.getEntity());
        });

        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> {
            UnifiedEvents.Player.passLeave(event.getEntity());
        });

        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerRespawnEvent event) -> {
            UnifiedEvents.Player.passRespawn(event.getEntity());
        });

        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> {
            UnifiedEvents.CommandRegistration.pass(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
        });
    }

    private static UnifiedEvents.ModifyItemComponents.Builder wrapperBuilder(DataComponentPatch.Builder neoBuilder) {
        return neoBuilder::set;
    }
}
