package net.rebel459.unified.api.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.minecraft.world.entity.vehicle.boat.ChestRaft;
import net.minecraft.world.entity.vehicle.boat.Raft;
import net.minecraft.world.item.Item;
import net.rebel459.unified.api.codec.ExtensibleCodecs;
import net.rebel459.unified.api.codec.ExtensibleEntityCodec;
import net.rebel459.unified.api.codec.UnifiedCodecs;

import java.util.function.Supplier;

public class VanillaEntityCodecs {

    public static final ExtensibleEntityCodec.Complex<Boat, Supplier<Item>> BOAT = ExtensibleCodecs.ENTITY.register(
            Identifier.withDefaultNamespace("boat"),
            UnifiedCodecs.supplied(BuiltInRegistries.ITEM).fieldOf("drop_item"),
            dropItem -> EntityType.Builder.of(EntityTypes.boatFactory(dropItem), MobCategory.MISC)
                    .noLootTable()
                    .sized(1.375F, 0.5625F)
                    .eyeHeight(0.5625F)
                    .clientTrackingRange(10)
    );

    public static final ExtensibleEntityCodec.Complex<ChestBoat, Supplier<Item>> CHEST_BOAT = ExtensibleCodecs.ENTITY.register(
            Identifier.withDefaultNamespace("chest_boat"),
            UnifiedCodecs.supplied(BuiltInRegistries.ITEM).fieldOf("drop_item"),
            dropItem -> EntityType.Builder.of(EntityTypes.chestBoatFactory(dropItem), MobCategory.MISC)
                    .noLootTable()
                    .sized(1.375F, 0.5625F)
                    .eyeHeight(0.5625F)
                    .clientTrackingRange(10)
    );

    public static final ExtensibleEntityCodec.Complex<Raft, Supplier<Item>> RAFT = ExtensibleCodecs.ENTITY.register(
            Identifier.withDefaultNamespace("raft"),
            UnifiedCodecs.supplied(BuiltInRegistries.ITEM).fieldOf("drop_item"),
            dropItem -> EntityType.Builder.of(EntityTypes.raftFactory(dropItem), MobCategory.MISC)
                    .noLootTable()
                    .sized(1.375F, 0.5625F)
                    .eyeHeight(0.5625F)
                    .clientTrackingRange(10)
    );

    public static final ExtensibleEntityCodec.Complex<ChestRaft, Supplier<Item>> CHEST_RAFT = ExtensibleCodecs.ENTITY.register(
            Identifier.withDefaultNamespace("chest_raft"),
            UnifiedCodecs.supplied(BuiltInRegistries.ITEM).fieldOf("drop_item"),
            dropItem -> EntityType.Builder.of(EntityTypes.chestRaftFactory(dropItem), MobCategory.MISC)
                    .noLootTable()
                    .sized(1.375F, 0.5625F)
                    .eyeHeight(0.5625F)
                    .clientTrackingRange(10)
    );

    public static void init() {}
}


