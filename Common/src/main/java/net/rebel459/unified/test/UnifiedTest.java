package net.rebel459.unified.test;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.FallingLeavesParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.ShelfBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.rebel459.unified.Unified;
import net.rebel459.unified.platform.UnifiedEvents;
import net.rebel459.unified.platform.UnifiedHelpers;
import net.rebel459.unified.platform.UnifiedRegistries;
import net.rebel459.unified.platform.client.UnifiedClientEvents;
import net.rebel459.unified.platform.client.UnifiedClientHelpers;
import net.rebel459.unified.registry.UnifiedItemComponents;
import net.rebel459.unified.util.PackInfo;
import net.rebel459.unified.util.PacketContext;

import java.util.function.Supplier;

public class UnifiedTest {

    public static UnifiedRegistries.Items ITEMS = UnifiedRegistries.Items.create(Unified.MOD_ID);
    public static UnifiedRegistries.Blocks BLOCKS = UnifiedRegistries.Blocks.create(Unified.MOD_ID);
    public static UnifiedRegistries.CreativeTabs CREATIVE_TABS = UnifiedRegistries.CreativeTabs.create(Unified.MOD_ID);
    public static UnifiedRegistries.Particles PARTICLES = UnifiedRegistries.Particles.create(Unified.MOD_ID);

    public static UnifiedHelpers.CreativeEntries CREATIVE_EVENT = UnifiedHelpers.CreativeEntries.create();
    public static UnifiedHelpers.Packs PACK_EVENT = UnifiedHelpers.Packs.create();
    public static UnifiedHelpers.LootTables LOOT_EVENT = UnifiedHelpers.LootTables.create();
    public static UnifiedHelpers.StrippableBlocks STRIPPABLE_EVENT = UnifiedHelpers.StrippableBlocks.create();

    public static final Supplier<Item> TEST_ITEM = ITEMS.register(
            "test_item",
            Item::new,
            () -> new Item.Properties()
                    .rarity(Rarity.UNCOMMON)
                    .component(UnifiedItemComponents.FURNACE_FUEL.get(), 100)
    );

    public static final Supplier<RotatedPillarBlock> TEST_BLOCK = BLOCKS.register("test_block",
            RotatedPillarBlock::new,
            BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.OAK_LOG)
                    .strength(1.8F, 8F)
    );

    public static final Supplier<ShelfBlock> TEST_BLOCK_ENTITY = BLOCKS.register("test_block_entity",
            ShelfBlock::new,
            BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.BAMBOO_SHELF),
            BlockEntityType.SHELF
    );

    public static final Supplier<SimpleParticleType> TEST_PARTICLE = PARTICLES.register("test_particle", new SimpleParticleType(false) {});

    public static final ResourceKey<CreativeModeTab> TEST_TAB = CREATIVE_TABS.registerTab("test_tab", UnifiedTest.TEST_ITEM);

    public static void init() {}

    public static void afterInit() {
        PACK_EVENT.add(Identifier.fromNamespaceAndPath(Unified.MOD_ID, "test_pack"), PackInfo.REQUIRED_DATA);
        ComposterBlock.COMPOSTABLES.put(UnifiedTest.TEST_ITEM.get(), 0.2F);
        CREATIVE_EVENT.add(TEST_TAB, TEST_ITEM.get(), TEST_BLOCK.get(), TEST_BLOCK_ENTITY.get());
        LOOT_EVENT.addPool(
                BuiltInLootTables.SIMPLE_DUNGEON,
                LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(net.minecraft.world.item.Items.DIAMOND_BLOCK).setWeight(1))
        );
        FireBlock fireBlock = (FireBlock) net.minecraft.world.level.block.Blocks.FIRE;
        fireBlock.setFlammable(TEST_BLOCK.get(), 5, 20);
        STRIPPABLE_EVENT.add(TEST_BLOCK.get(), net.minecraft.world.level.block.Blocks.OAK_LOG);

        var network = UnifiedHelpers.NetworkPayloads.create();

        network.registerC2S(PingPayload.TYPE, PingPayload.CODEC, (payloadObject, contextObject) -> {
            PingPayload payload = (PingPayload) payloadObject;
            PacketContext context = (PacketContext) contextObject;

            context.player().sendSystemMessage(Component.literal("Server received ping: " + payload.message()));

            context.respond(new PongPayload("Pong! Echo: " + payload.message()));
        });

        network.registerS2C(PongPayload.TYPE, PongPayload.CODEC, payloadObject -> {
            PongPayload payload = (PongPayload) payloadObject;
            Minecraft.getInstance().execute(() -> {
                if (Minecraft.getInstance().player != null) {
                    Minecraft.getInstance().player.displayClientMessage(
                            Component.literal("Client received: " + payload.message()), true
                    );
                }
            });
        });

        UnifiedEvents.PlayerJoin.insert(player -> {
            if (player instanceof ServerPlayer serverPlayer) {
                var serverNet = UnifiedHelpers.NetworkPayloads.create();
                serverNet.send(new PongPayload("Auto pong on join! Welcome, " + serverPlayer.getName().getString()), serverPlayer);
            }
        });
    }

    public static void clientInit() {
        var particleProviders = UnifiedClientHelpers.ParticleProviders.create();
        particleProviders.add(TEST_PARTICLE.get(), FallingLeavesParticle.CherryProvider::new);
    }
}
