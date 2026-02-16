package net.rebel459.unified.platform;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.rebel459.unified.util.PackInfo;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class HelpersImpl {

    public interface Platform {

        /**
         * Gets the name of the current platform
         *
         * @return The name of the current platform.
         */
        net.rebel459.unified.util.Platform getPlatform();

        /**
         * Checks if a mod with the given id is loaded.
         *
         * @param modId The mod to check if it is loaded.
         * @return True if the mod is loaded, false otherwise.
         */
        boolean isModLoaded(String modId);
    }

    public interface FurnaceFuels {

        void add(ItemLike item, int ticks);

        static FurnaceFuels get() {
            return PlatformHelperImpl.INSTANCE.getFurnaceFuels();
        }
    }

    public interface CreativeEntries {

        void add(ResourceKey<CreativeModeTab> tab, ItemLike... items);
        void add(ResourceKey<CreativeModeTab> tab, ItemStack... items);
        void addAfter(ResourceKey<CreativeModeTab> tab, ItemLike existingItem, ItemLike... addedItems);
        void addAfter(ResourceKey<CreativeModeTab> tab, ItemLike existingItem, ItemStack... addedItems);
        void addBefore(ResourceKey<CreativeModeTab> tab, ItemLike existingItem, ItemLike... addedItems);
        void addBefore(ResourceKey<CreativeModeTab> tab, ItemLike existingItem, ItemStack... addedItems);
    }

    public interface Packs {

        void add(Identifier id, PackInfo info);
    }

    public interface LootTables {

        void addPool(ResourceKey<LootTable> table, LootPool.Builder... pools);
        void addPool(List<ResourceKey<LootTable>> tables, LootPool.Builder... pools);
        void addItem(ResourceKey<LootTable> table, ItemLike item, int chance);
        void addItem(List<ResourceKey<LootTable>> tables, ItemLike item, int chance);
    }

    public interface StrippableBlocks {

        void add(Block original, Block stripped);
    }

    public interface Networking {

        <T extends CustomPacketPayload, B extends FriendlyByteBuf> void registerC2S(CustomPacketPayload.Type<T> type, StreamCodec<? super B, T> codec);
        <T extends CustomPacketPayload, B extends FriendlyByteBuf> void registerS2C(CustomPacketPayload.Type<T> type, StreamCodec<? super B, T> codec);

        <T extends CustomPacketPayload> void registerC2S(CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec, BiConsumer<T, ServerPlayer> handler);
        <T extends CustomPacketPayload> void registerS2C(CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec, Consumer<T> handler);

        void send(CustomPacketPayload payload, ServerPlayer player);
    }
}