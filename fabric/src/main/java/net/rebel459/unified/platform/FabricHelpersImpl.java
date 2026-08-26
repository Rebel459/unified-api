package net.rebel459.unified.platform;

import com.mojang.serialization.Codec;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityDataRegistry;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.rebel459.unified.Unified;
import net.rebel459.unified.util.PackType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class FabricHelpersImpl {

    public static class CreativeEntries implements HelpersImpl.CreativeEntries {

        @Override
        public final void insert(ResourceKey<CreativeModeTab> tab, ItemLike... items) {
            var itemList = Arrays.stream(items).toList();
            for (ItemLike itemLike : itemList) {
                insert(tab, new ItemStackTemplate(itemLike.asItem()));
            }
        }

        @SafeVarargs
        @Override
        public final void insert(ResourceKey<CreativeModeTab> tab, ItemStackTemplate... items) {
            var itemList = Arrays.stream(items).toList();
            for (int x = itemList.size() - 1; x >= 0; x--) {
                ItemStackTemplate template = itemList.get(x);
                CreativeModeTabEvents.modifyOutputEvent(tab).register(entries -> {
                    entries.accept(template.create());
                });
            }
        }

        @Override
        public void insert(List<ResourceKey<CreativeModeTab>> tabs, ItemLike... items) {
            for (ResourceKey<CreativeModeTab> tab : tabs) {
                insert(tab, items);
            }
        }

        @Override
        public void insert(List<ResourceKey<CreativeModeTab>> tabs, ItemStackTemplate... items) {
            for (ResourceKey<CreativeModeTab> tab : tabs) {
                insert(tab, items);
            }
        }

        @Override
        public final void insertAfter(ResourceKey<CreativeModeTab> tab, ItemLike existingItem, ItemLike... addedItems) {
            var itemList = Arrays.stream(addedItems).toList();
            for (int x = itemList.size() - 1; x >= 0; x--) {
                insertAfter(tab, existingItem, new ItemStackTemplate(itemList.get(x).asItem()));
            }
        }

        @Override
        public void insertAfter(ResourceKey<CreativeModeTab> tab, ItemLike existingItem, ItemStackTemplate... addedItems) {
            CreativeModeTabEvents.modifyOutputEvent(tab).register(entries -> {
                List<ItemStackTemplate> itemList = Arrays.stream(addedItems).toList();
                for (int x = itemList.size() - 1; x >= 0; x--) {
                    entries.insertAfter(existingItem, itemList.get(x).create());
                }
            });
        }

        @Override
        public void insertAfter(List<ResourceKey<CreativeModeTab>> tabs, ItemLike existingItem, ItemLike... addedItems) {
            for (ResourceKey<CreativeModeTab> tab : tabs) {
                insertAfter(tab, existingItem, addedItems);
            }
        }

        @Override
        public void insertAfter(List<ResourceKey<CreativeModeTab>> tabs, ItemLike existingItem, ItemStackTemplate... addedItems) {
            for (ResourceKey<CreativeModeTab> tab : tabs) {
                insertAfter(tab, existingItem, addedItems);
            }
        }

        @Override
        public final void insertBefore(ResourceKey<CreativeModeTab> tab, ItemLike existingItem, ItemLike... addedItems) {
            var itemList = Arrays.stream(addedItems).toList();
            for (ItemLike itemLike : itemList) {
                insertBefore(tab, existingItem, new ItemStackTemplate(itemLike.asItem()));
            }
        }

        @Override
        public void insertBefore(ResourceKey<CreativeModeTab> tab, ItemLike existingItem, ItemStackTemplate... addedItems) {
            CreativeModeTabEvents.modifyOutputEvent(tab).register(entries -> {
                List<ItemStackTemplate> itemList = Arrays.stream(addedItems).toList();
                ItemStackTemplate previousTemplate = null;
                for (ItemStackTemplate template : itemList) {
                    if (previousTemplate == null) {
                        entries.insertBefore(existingItem, template.create());
                    }
                    else {
                        entries.insertBefore(previousTemplate.create(), template.create());
                    }
                    previousTemplate = template;
                }
            });
        }

        @Override
        public void insertBefore(List<ResourceKey<CreativeModeTab>> tabs, ItemLike existingItem, ItemLike... addedItems) {
            for (ResourceKey<CreativeModeTab> tab : tabs) {
                insertBefore(tab, existingItem, addedItems);
            }
        }

        @Override
        public void insertBefore(List<ResourceKey<CreativeModeTab>> tabs, ItemLike existingItem, ItemStackTemplate... addedItems) {
            for (ResourceKey<CreativeModeTab> tab : tabs) {
                insertBefore(tab, existingItem, addedItems);
            }
        }
    }

    public static class Packs implements HelpersImpl.Packs {

        @Override
        public void add(Identifier id, PackType info) {
            if (FabricLoader.getInstance().getModContainer(id.getNamespace()).isEmpty()) return;
            ResourceLoader.registerBuiltinPack(
                    id, FabricLoader.getInstance().getModContainer(id.getNamespace()).get(),
                    Component.translatable("pack." + id.getNamespace() + "." + id.getPath()),
                    getActivationType(info)
            );
        }

        public static PackActivationType getActivationType(PackType info) {
            return switch (info) {
                case REQUIRED_DATA, REQUIRED_RESOURCES -> PackActivationType.ALWAYS_ENABLED;
                case OPTIONAL_DATA, OPTIONAL_RESOURCES -> PackActivationType.NORMAL;
            };
        }
    }

    public static class Networking implements HelpersImpl.Networking {

        @Override
        public void registerPlayToServer(CustomPacketPayload.Type type, StreamCodec codec) {
            PayloadTypeRegistry.serverboundPlay().register(type, codec);
        }

        @Override
        public void registerPlayToClient(CustomPacketPayload.Type type, StreamCodec codec) {
            PayloadTypeRegistry.clientboundPlay().register(type, codec);
        }

        @Override
        public void registerPlayToServer(CustomPacketPayload.Type type, StreamCodec codec, BiConsumer handler) {

            PayloadTypeRegistry.serverboundPlay().register(type, codec);

            ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> {
                handler.accept(payload, context.player());
            });
        }

        @Override
        public void registerPlayToClient(CustomPacketPayload.Type type, StreamCodec codec, BiConsumer handler) {

            PayloadTypeRegistry.clientboundPlay().register(type, codec);

            if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
                ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) -> {
                    handler.accept(payload, context.player());
                });
            }
        }

        @Override
        public void registerConfigToServer(CustomPacketPayload.Type type, StreamCodec codec) {
            PayloadTypeRegistry.serverboundConfiguration().register(type, codec);
        }

        @Override
        public void registerConfigToClient(CustomPacketPayload.Type type, StreamCodec codec) {
            PayloadTypeRegistry.clientboundConfiguration().register(type, codec);
        }

        @Override
        public void registerConfigToServer(CustomPacketPayload.Type type, StreamCodec codec, BiConsumer handler) {

            PayloadTypeRegistry.serverboundConfiguration().register(type, codec);

            ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> {
                handler.accept(payload, context.player());
            });
        }

        @Override
        public void registerConfigToClient(CustomPacketPayload.Type type, StreamCodec codec, BiConsumer handler) {

            PayloadTypeRegistry.clientboundConfiguration().register(type, codec);

            if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
                ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) -> {
                    handler.accept(payload, context.player());
                });
            }
        }

        @Override
        public boolean canSend(CustomPacketPayload payload, ServerPlayer player) {
            return ServerPlayNetworking.canSend(player, payload.type());
        }

        @Override
        public void send(CustomPacketPayload payload, ServerPlayer player) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    public static class ReloadListeners implements HelpersImpl.ReloadListeners {

        @Override
        public void addListener(Identifier id, PreparableReloadListener listener) {
            ResourceLoader.get(net.minecraft.server.packs.PackType.SERVER_DATA).registerReloadListener(id, listener);
        }

        @Override
        public void addOrdering(Identifier first, Identifier second) {
            ResourceLoader.get(net.minecraft.server.packs.PackType.SERVER_DATA).addListenerOrdering(first, second);
        }
    }

    public static class DataRegistries implements HelpersImpl.DataRegistries {

        @Override
        public <T> void register(ResourceKey<Registry<T>> key, Codec<T> codec) {
            DynamicRegistries.register(key, codec);
        }

        @Override
        public <T> void registerSynced(ResourceKey<Registry<T>> key, Codec<T> serverCodec, Codec<T> clientCodec) {
            DynamicRegistries.registerSynced(key, serverCodec, clientCodec);
        }
    }

    public static class EntityData implements HelpersImpl.EntityData {

        @Override
        public void registerSerializer(Identifier id, Supplier<EntityDataSerializer<?>> serializer) {
            FabricEntityDataRegistry.register(id, serializer.get());
        }
    }
}
