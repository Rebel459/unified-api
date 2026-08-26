package net.rebel459.unified.util.data;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootTable;
import net.rebel459.unified.Unified;
import net.rebel459.unified.platform.UnifiedRegistries;
import net.rebel459.unified.registry.UnifiedBlockTypes;
import net.rebel459.unified.util.codec.CodecUtils;
import net.rebel459.unified.util.codec.ExtensibleCodec;
import net.rebel459.unified.util.registry.RegistryResourceListener;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class BlockRegistry extends RegistryResourceListener<BlockRegistry.Definition> {
    public static final ExtensibleCodec<Function<BlockBehaviour.Properties, ? extends Block>> TYPES = new ExtensibleCodec<>("type");

    public static final Codec<Definition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TYPES.codec(Identifier.withDefaultNamespace("block")).forGetter(Definition::type),
            Codec.BOOL.optionalFieldOf("register_item", true).forGetter(Definition::registerItem),
            CodecUtils.supplied(Properties.CODEC, () -> RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY), BlockRegistry::createProperties)
                    .optionalFieldOf("properties").xmap(properties -> properties.orElse(BlockBehaviour.Properties::of), Optional::of).forGetter(Definition::properties),
            ResourceKey.codec(Registries.BLOCK_ENTITY_TYPE).optionalFieldOf("block_entity").forGetter(Definition::blockEntity)
            ).apply(instance, Definition::new));

    public BlockRegistry() {
        super(Identifier.fromNamespaceAndPath(Unified.MOD_ID, "blocks"), CODEC);
    }

    @Override
    protected void register(Identifier id, Definition definition) {
        if (definition.registerItem) {
            if (definition.blockEntity.isPresent()) {
                UnifiedRegistries.Blocks.create(id.getNamespace()).register(
                        id.getPath(),
                        definition.factory(),
                        definition.properties(),
                        () -> BuiltInRegistries.BLOCK_ENTITY_TYPE.getValueOrThrow(definition.blockEntity().orElseThrow())
                );
            } else {
                UnifiedRegistries.Blocks.create(id.getNamespace()).register(
                        id.getPath(),
                        definition.factory(),
                        definition.properties()
                );
            }
        } else {
            if (definition.blockEntity.isPresent()) {
                UnifiedRegistries.Blocks.create(id.getNamespace()).registerWithoutItem(
                        id.getPath(),
                        definition.factory(),
                        definition.properties(),
                        () -> BuiltInRegistries.BLOCK_ENTITY_TYPE.getValueOrThrow(definition.blockEntity().orElseThrow())
                );
            } else {
                UnifiedRegistries.Blocks.create(id.getNamespace()).registerWithoutItem(
                        id.getPath(),
                        definition.factory(),
                        definition.properties()
                );
            }
        }
    }

    public record Definition(
            ExtensibleCodec.Entry<Function<BlockBehaviour.Properties, ? extends Block>> type,
            boolean registerItem,
            Supplier<BlockBehaviour.Properties> properties,
            Optional<ResourceKey<BlockEntityType<? extends BlockEntity>>> blockEntity
    ) {
        public Function<BlockBehaviour.Properties, ? extends Block> factory() {
            return type.value();
        }
    }

    public record Properties(
            Optional<Identifier> copyFrom,
            Optional<MapColor> mapColor,
            Optional<Boolean> collision,
            Optional<Boolean> occlusion,
            Optional<Float> friction,
            Optional<Float> speedMultiplier,
            Optional<Float> jumpMultiplier,
            Optional<SoundType> sounds,
            Optional<Integer> lightLevel,
            Optional<Integer> destroyTime,
            Optional<Integer> explosionResistance,
            Optional<Boolean> randomTicks,
            Optional<Boolean> dynamicShape,
            Optional<ResourceKey<LootTable>> lootTable,
            Optional<Boolean> ignitedByLava,
            Optional<Boolean> liquid,
            Optional<Boolean> solid,
            Optional<PushReaction> pushReaction,
            Optional<Boolean> air,
            Optional<Either<BlockRegistry.Predicate, HolderSet<EntityType<?>>>> validSpawn,
            Optional<BlockRegistry.Predicate> redstoneConductor,
            Optional<BlockRegistry.Predicate> viewBlocking,
            Optional<Position> postProcess,
            Optional<BlockRegistry.Predicate> emissiveRendering,
            Optional<Boolean> requiresCorrectToolForDrops,
            Optional<BlockBehaviour.OffsetType> offset,
            Optional<Boolean> spawnTerrainParticles,
            Optional<NoteBlockInstrument> instrument,
            Optional<Boolean> replaceable,
            Optional<String> descriptionOverride
    ) {

        private static final MapCodec<First> FIRST_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                        Identifier.CODEC.optionalFieldOf("copy_from").forGetter(First::copyFrom),
                        CodecUtils.named(MapColor.class).optionalFieldOf("map_color").forGetter(First::mapColor),
                        Codec.BOOL.optionalFieldOf("collision").forGetter(First::collision),
                        Codec.BOOL.optionalFieldOf("occlusion").forGetter(First::occlusion),
                        Codec.FLOAT.optionalFieldOf("friction").forGetter(First::friction),
                        Codec.FLOAT.optionalFieldOf("speed_multiplier").forGetter(First::speedMultiplier),
                        Codec.FLOAT.optionalFieldOf("jump_multiplier").forGetter(First::jumpMultiplier),
                        SoundType.CODEC.optionalFieldOf("sounds").forGetter(First::sounds),
                        Codec.INT.optionalFieldOf("light_level").forGetter(First::lightLevel),
                        Codec.INT.optionalFieldOf("destroy_time").forGetter(First::destroyTime),
                        Codec.INT.optionalFieldOf("explosion_resistance").forGetter(First::explosionResistance),
                        Codec.BOOL.optionalFieldOf("random_ticks").forGetter(First::randomTicks),
                        Codec.BOOL.optionalFieldOf("dynamic_shape").forGetter(First::dynamicShape),
                        ResourceKey.codec(Registries.LOOT_TABLE).optionalFieldOf("loot_table").forGetter(First::lootTable),
                        Codec.BOOL.optionalFieldOf("ignited_by_lava").forGetter(First::ignitedByLava)
                ).apply(instance, First::new));

        private static final MapCodec<Second> SECOND_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                        Codec.BOOL.optionalFieldOf("liquid").forGetter(Second::liquid),
                        Codec.BOOL.optionalFieldOf("solid").forGetter(Second::solid),
                        CodecUtils.named(PushReaction.class).optionalFieldOf("push_reaction").forGetter(Second::pushReaction),
                        Codec.BOOL.optionalFieldOf("air").forGetter(Second::air),
                        Codec.either(BlockRegistry.Predicate.CODEC, RegistryCodecs.homogeneousList(Registries.ENTITY_TYPE)).optionalFieldOf("valid_spawn").forGetter(Second::validSpawn),
                        predicateCodec("redstone_conductor").forGetter(Second::redstoneConductor),
                        predicateCodec("view_blocking").forGetter(Second::viewBlocking),
                        positionCodec("post_process").forGetter(Second::postProcess),
                        predicateCodec("emissive_rendering").forGetter(Second::emissiveRendering),
                        Codec.BOOL.optionalFieldOf("requires_correct_tool_for_drops").forGetter(Second::requiresCorrectToolForDrops),
                        CodecUtils.named(BlockBehaviour.OffsetType.class).optionalFieldOf("offset").forGetter(Second::offset),
                        Codec.BOOL.optionalFieldOf("spawn_terrain_particles").forGetter(Second::spawnTerrainParticles),
                        CodecUtils.named(NoteBlockInstrument.class).optionalFieldOf("instrument").forGetter(Second::instrument),
                        Codec.BOOL.optionalFieldOf("replaceable").forGetter(Second::replaceable),
                        Codec.STRING.optionalFieldOf("description_override").forGetter(Second::descriptionOverride)
                ).apply(instance, Second::new));

        private static MapCodec<Optional<BlockRegistry.Predicate>> predicateCodec(String name) {
            return Codec.STRING.optionalFieldOf(name).xmap(
                    value -> value.map(BlockRegistry.Predicate::new),
                    value -> value.map(BlockRegistry.Predicate::predicate)
            );
        }

        private static MapCodec<Optional<BlockRegistry.Position>> positionCodec(String name) {
            return Codec.STRING.optionalFieldOf(name).xmap(
                    value -> value.map(BlockRegistry.Position::new),
                    value -> value.map(BlockRegistry.Position::position)
            );
        }

        public static final MapCodec<Properties> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                        FIRST_CODEC.forGetter(Properties::first),
                        SECOND_CODEC.forGetter(Properties::second)
                ).apply(instance, Properties::new));

        public static final Codec<Properties> CODEC = MAP_CODEC.codec();

        private Properties(First first, Second second) {
            this(
                    first.copyFrom(),
                    first.mapColor(),
                    first.collision(),
                    first.occlusion(),
                    first.friction(),
                    first.speedMultiplier(),
                    first.jumpMultiplier(),
                    first.sounds(),
                    first.lightLevel(),
                    first.destroyTime(),
                    first.explosionResistance(),
                    first.randomTicks(),
                    first.dynamicShape(),
                    first.lootTable(),
                    first.ignitedByLava(),
                    second.liquid(),
                    second.solid(),
                    second.pushReaction(),
                    second.air(),
                    second.validSpawn(),
                    second.redstoneConductor(),
                    second.viewBlocking(),
                    second.postProcess(),
                    second.emissiveRendering(),
                    second.requiresCorrectToolForDrops(),
                    second.offset(),
                    second.spawnTerrainParticles(),
                    second.instrument(),
                    second.replaceable(),
                    second.descriptionOverride()
            );
        }

        private First first() {
            return new First(
                    copyFrom,
                    mapColor,
                    collision,
                    occlusion,
                    friction,
                    speedMultiplier,
                    jumpMultiplier,
                    sounds,
                    lightLevel,
                    destroyTime,
                    explosionResistance,
                    randomTicks,
                    dynamicShape,
                    lootTable,
                    ignitedByLava
            );
        }

        private Second second() {
            return new Second(
                    liquid,
                    solid,
                    pushReaction,
                    air,
                    validSpawn,
                    redstoneConductor,
                    viewBlocking,
                    postProcess,
                    emissiveRendering,
                    requiresCorrectToolForDrops,
                    offset,
                    spawnTerrainParticles,
                    instrument,
                    replaceable,
                    descriptionOverride
            );
        }

        private record First(
                Optional<Identifier> copyFrom,
                Optional<MapColor> mapColor,
                Optional<Boolean> collision,
                Optional<Boolean> occlusion,
                Optional<Float> friction,
                Optional<Float> speedMultiplier,
                Optional<Float> jumpMultiplier,
                Optional<SoundType> sounds,
                Optional<Integer> lightLevel,
                Optional<Integer> destroyTime,
                Optional<Integer> explosionResistance,
                Optional<Boolean> randomTicks,
                Optional<Boolean> dynamicShape,
                Optional<ResourceKey<LootTable>> lootTable,
                Optional<Boolean> ignitedByLava
        ) {}

        private record Second(
                Optional<Boolean> liquid,
                Optional<Boolean> solid,
                Optional<PushReaction> pushReaction,
                Optional<Boolean> air,
                Optional<Either<BlockRegistry.Predicate, HolderSet<EntityType<?>>>> validSpawn,
                Optional<BlockRegistry.Predicate> redstoneConductor,
                Optional<BlockRegistry.Predicate> viewBlocking,
                Optional<Position> postProcess,
                Optional<BlockRegistry.Predicate> emissiveRendering,
                Optional<Boolean> requiresCorrectToolForDrops,
                Optional<BlockBehaviour.OffsetType> offset,
                Optional<Boolean> spawnTerrainParticles,
                Optional<NoteBlockInstrument> instrument,
                Optional<Boolean> replaceable,
                Optional<String> descriptionOverride
        ) {}
    }

    public record SoundType(float volume, float pitch, SoundEvent breakSound, SoundEvent stepSound, SoundEvent placeSound, SoundEvent hitSound, SoundEvent fallSound) {
        private static final Codec<SoundType> CODEC =
                RecordCodecBuilder.create(instance -> instance.group(
                        ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("volume", 1F).forGetter(SoundType::volume),
                        ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("pitch", 1F).forGetter(SoundType::pitch),
                        BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("break_sound").forGetter(SoundType::breakSound),
                        BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("step_sound").forGetter(SoundType::stepSound),
                        BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("place_sound").forGetter(SoundType::placeSound),
                        BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("hit_sound").forGetter(SoundType::hitSound),
                        BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("fall_sound").forGetter(SoundType::fallSound)
                ).apply(instance, SoundType::new));
    }

    private static BlockBehaviour.Properties createProperties(Properties properties) {
        BlockBehaviour.Properties actual = properties.copyFrom.map(identifier -> BlockBehaviour.Properties.ofFullCopy(BuiltInRegistries.BLOCK.getValue(identifier))).orElseGet(BlockBehaviour.Properties::of);
        if (properties.mapColor.isPresent()) {
            actual.mapColor(properties.mapColor.get());
        }
        if (properties.collision.isPresent()) {
            actual.hasCollision = properties.collision.get();
        }
        if (properties.occlusion.isPresent()) {
            actual.canOcclude = properties.occlusion.get();
        }
        if (properties.friction.isPresent()) {
            actual.friction(properties.friction.get());
        }
        if (properties.speedMultiplier.isPresent()) {
            actual.speedFactor(properties.speedMultiplier.get());
        }
        if (properties.jumpMultiplier.isPresent()) {
            actual.jumpFactor(properties.jumpMultiplier.get());
        }
        if (properties.sounds.isPresent()) {
            SoundType sounds = properties.sounds.get();
            actual.sound(new net.minecraft.world.level.block.SoundType(sounds.volume, sounds.pitch, sounds.breakSound, sounds.stepSound, sounds.placeSound, sounds.hitSound, sounds.fallSound));
        }
        if (properties.lightLevel.isPresent()) {
            actual.lightLevel(_ -> properties.lightLevel.get());
        }
        if (properties.destroyTime.isPresent()) {
            actual.destroyTime(properties.destroyTime.get());
        }
        if (properties.explosionResistance.isPresent()) {
            actual.explosionResistance(properties.explosionResistance.get());
        }
        if (properties.randomTicks.isPresent()) {
            actual.isRandomlyTicking = properties.randomTicks.get();
        }
        if (properties.dynamicShape.isPresent()) {
            actual.dynamicShape = properties.dynamicShape.get();
        }
        if (properties.lootTable.isPresent()) {
            actual.overrideLootTable(properties.lootTable);
        }
        if (properties.ignitedByLava.isPresent()) {
            actual.ignitedByLava = properties.ignitedByLava.get();
        }
        if (properties.liquid.isPresent()) {
            actual.liquid = properties.liquid.get();
        }
        if (properties.solid.isPresent()) {
            if (properties.solid.get()) {
                actual.forceSolidOn = true;
                actual.forceSolidOff = false;
            } else {
                actual.forceSolidOn = false;
                actual.forceSolidOff = true;
            }
        }
        if (properties.pushReaction.isPresent()) {
            actual.pushReaction(properties.pushReaction.get());
        }
        if (properties.air.isPresent()) {
            actual.isAir = properties.air.get();
        }
        if (properties.validSpawn.isPresent()) {
            Optional<Predicate> left = properties.validSpawn.get().left();
            Optional<HolderSet<EntityType<?>>> right = properties.validSpawn.get().right();
            if (left.isPresent()) {
                actual.isValidSpawn((state, getter, pos, _) -> left.get().validate(state, getter, pos));
            } else if (right.isPresent()) {
                actual.isValidSpawn((_, _, _, entity) -> right.get().stream().anyMatch(holder -> holder.value() == entity));
            }
        }
        if (properties.redstoneConductor.isPresent()) {
            actual.isRedstoneConductor((state, getter, pos) -> properties.redstoneConductor.get().validate(state, getter, pos));
        }
        if (properties.viewBlocking.isPresent()) {
            actual.isViewBlocking((state, getter, pos) -> properties.viewBlocking.get().validate(state, getter, pos));
        }
        if (properties.postProcess.isPresent()) {
            actual.postProcess((state, getter, pos) -> properties.postProcess.get().validate(state, getter, pos));
        }
        if (properties.emissiveRendering.isPresent()) {
            actual.emissiveRendering((state, getter, pos) -> properties.emissiveRendering.get().validate(state, getter, pos));
        }
        if (properties.requiresCorrectToolForDrops.isPresent()) {
            actual.requiresCorrectToolForDrops = properties.requiresCorrectToolForDrops.get();
        }
        if (properties.offset.isPresent()) {
            actual.offsetType(properties.offset.get());
        }
        if (properties.spawnTerrainParticles.isPresent()) {
            actual.spawnTerrainParticles = properties.spawnTerrainParticles.get();
        }
        if (properties.instrument.isPresent()) {
            actual.instrument(properties.instrument.get());
        }
        if (properties.replaceable.isPresent()) {
            actual.replaceable = properties.replaceable.get();
        }
        if (properties.descriptionOverride.isPresent()) {
            actual.overrideDescription(properties.descriptionOverride.get());
        }
        return actual;
    }

    private record Predicate(String predicate) {
        private static final Codec<BlockRegistry.Predicate> CODEC = Codec.STRING.xmap(BlockRegistry.Predicate::new, BlockRegistry.Predicate::predicate);

        private boolean validate(BlockState state, BlockGetter getter, BlockPos pos) {
            return switch (predicate) {
                case "never" -> false;
                case "always" -> true;
                case "not_closed_shulker" -> Blocks.NOT_CLOSED_SHULKER.test(state, getter, pos);
                case "not_extended_piston" -> Blocks.NOT_EXTENDED_PISTON.test(state, getter, pos);
                default -> throw new IllegalStateException("Unknown predicate: " + predicate);
            };
        }
    }

    private record Position(String position) {
        private BlockPos validate(BlockState state, BlockGetter getter, BlockPos pos) {
            return switch (position) {
                case "self" -> pos;
                case "above" -> pos.above();
                case "below" -> pos.below();
                case "north" -> pos.north();
                case "east" -> pos.east();
                case "south" -> pos.south();
                case "west" -> pos.west();
                default -> throw new IllegalStateException("Unknown post process: " + position);
            };
        }
    }
}
