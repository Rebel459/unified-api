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

public class UnifiedHelpers {

    public static HelpersImpl.Platform PLATFORM = PlatformHelperImpl.INSTANCE.getPlatform();
    public static HelpersImpl.FurnaceFuels FURNACE_FUELS = PlatformHelperImpl.INSTANCE.getFurnaceFuels();
    public static HelpersImpl.CreativeEntries CREATIVE_ENTRIES = PlatformHelperImpl.INSTANCE.getCreativeEntries();
    public static HelpersImpl.Packs PACKS = PlatformHelperImpl.INSTANCE.getPacks();
    public static HelpersImpl.LootTables LOOT_TABLES = PlatformHelperImpl.INSTANCE.getLootTables();
    public static HelpersImpl.StrippableBlocks STRIPPABLE_BLOCKS = PlatformHelperImpl.INSTANCE.getStrippableBlocks();
    public static HelpersImpl.Networking NETWORKING = PlatformHelperImpl.INSTANCE.getNetworkPayloads();
}