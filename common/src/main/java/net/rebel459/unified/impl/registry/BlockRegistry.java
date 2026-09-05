package net.rebel459.unified.impl.registry;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.rebel459.unified.Unified;
import net.rebel459.unified.api.core.SuppliedBlock;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.api.codec.ExtensibleCodecs;
import net.rebel459.unified.api.codec.UnifiedCodecs;
import net.rebel459.unified.impl.core.DataRegistryClaims;
import net.rebel459.unified.api.core.RegistryResourceListener;
import net.rebel459.unified.api.core.UnifiedRegistries;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

public class BlockRegistry extends RegistryResourceListener<BlockRegistry.Definition> {
    private static final MapCodec<Optional<Supplier<BlockEntityType<?>>>> BLOCK_ENTITY_CODEC =
            UnifiedCodecs.supplied(BuiltInRegistries.BLOCK_ENTITY_TYPE).optionalFieldOf("block_entity");
    public static final Codec<Definition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtensibleCodecs.BLOCK_TYPES.mapCodec().forGetter(Definition::type),
            Properties.CODEC.optionalFieldOf("properties")
                    .xmap(properties -> properties.orElseGet(Properties::new), Optional::of).forGetter(Definition::properties),
            UnifiedCodecs.supplied(BuiltInRegistries.BLOCK_ENTITY_TYPE).optionalFieldOf("block_entity").forGetter(Definition::blockEntity)
            ).apply(instance, Definition::new));

    public static final Identifier ID = Unified.id("blocks");

    public BlockRegistry() {
        super(ID, CODEC, WoodTypeRegistry.ID);
    }

    @Override
    protected void register(Identifier id, DeferredDeclaration<Definition> declaration) {
        Optional<Supplier<BlockEntityType<?>>> blockEntity = declaration.decode(BLOCK_ENTITY_CODEC);
        DataRegistryClaims.registerBlock(id, () -> {
            UnifiedRegistries.Blocks blocks = UnifiedRegistries.Blocks.create(id.getNamespace());
            Function<BlockBehaviour.Properties, ? extends Block> factory = properties -> declaration.get().factory().apply(properties);
            Supplier<BlockBehaviour.Properties> properties = () -> createProperties(declaration.get().properties());
            if (blockEntity.isPresent()) {
                return register(blocks, id.getPath(), factory, properties, blockEntity.orElseThrow());
            }
            return blocks.registerWithoutItem(id.getPath(), factory, properties);
        });
    }

    public record Definition(
            ExtensibleCodec.Entry<Function<BlockBehaviour.Properties, ? extends Block>> type,
            Properties properties,
            Optional<Supplier<BlockEntityType<?>>> blockEntity
    ) {
        public Function<BlockBehaviour.Properties, ? extends Block> factory() {
            return type.get();
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static SuppliedBlock register(UnifiedRegistries.Blocks blocks, String path,
                                          Function<BlockBehaviour.Properties, ? extends Block> factory,
                                          Supplier<BlockBehaviour.Properties> properties,
                                          Supplier<BlockEntityType<?>> blockEntity) {
        Supplier type = blockEntity;
        return blocks.registerWithoutItem(path, factory, properties, type);
    }

    public static final class Properties {
        public Optional<Supplier<Block>> copyFrom = Optional.empty();
        public Optional<Either<MapColor, ExtensibleCodec.Entry<Function<BlockState, MapColor>>>> mapColor = Optional.empty();
        public Optional<Boolean> collision = Optional.empty();
        public Optional<Boolean> occlusion = Optional.empty();
        public Optional<Float> friction = Optional.empty();
        public Optional<Float> speedMultiplier = Optional.empty();
        public Optional<Float> jumpMultiplier = Optional.empty();
        public Optional<SoundType> soundType = Optional.empty();
        public Optional<Either<Integer, ExtensibleCodec.Entry<ToIntFunction<BlockState>>>> lightLevel = Optional.empty();
        public Optional<Float> destroyTime = Optional.empty();
        public Optional<Float> explosionResistance = Optional.empty();
        public Optional<Boolean> randomTicks = Optional.empty();
        public Optional<Boolean> dynamicShape = Optional.empty();
        public Optional<ResourceKey<LootTable>> lootTable = Optional.empty();
        public Optional<Boolean> ignitedByLava = Optional.empty();
        public Optional<Boolean> liquid = Optional.empty();
        public Optional<Boolean> solid = Optional.empty();
        public Optional<PushReaction> pushReaction = Optional.empty();
        public Optional<Boolean> air = Optional.empty();
        public Optional<ExtensibleCodec.Entry<BlockBehaviour.StateArgumentPredicate<EntityType<?>>>> validSpawn = Optional.empty();
        public Optional<ExtensibleCodec.Entry<BlockBehaviour.StatePredicate>> redstoneConductor = Optional.empty();
        public Optional<ExtensibleCodec.Entry<BlockBehaviour.StatePredicate>> suffocating = Optional.empty();
        public Optional<ExtensibleCodec.Entry<BlockBehaviour.StateArgumentPredicate<AABB>>> viewBlocking = Optional.empty();
        public Optional<ExtensibleCodec.Entry<BlockBehaviour.PostProcess>> postProcess = Optional.empty();
        public Optional<ExtensibleCodec.Entry<Predicate<BlockState>>> emissiveRendering = Optional.empty();
        public Optional<Boolean> requiresCorrectToolForDrops = Optional.empty();
        public Optional<BlockBehaviour.OffsetType> offset = Optional.empty();
        public Optional<Boolean> spawnTerrainParticles = Optional.empty();
        public Optional<NoteBlockInstrument> instrument = Optional.empty();
        public Optional<Boolean> replaceable = Optional.empty();
        public Optional<String> descriptionOverride = Optional.empty();
        public Optional<FeatureFlagSet> requiredFeatures = Optional.empty();
        public Optional<Boolean> noLootTable = Optional.empty();

        public Properties() {}

        private static final MapCodec<First> FIRST_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                        UnifiedCodecs.supplied(BuiltInRegistries.BLOCK).optionalFieldOf("copy_from").forGetter(First::copyFrom),
                        Codec.either(UnifiedCodecs.named(MapColor.class), ExtensibleCodecs.MAP_COLOR_TYPES.codec()).optionalFieldOf("map_color").forGetter(First::mapColor),
                        Codec.BOOL.optionalFieldOf("collision").forGetter(First::collision),
                        Codec.BOOL.optionalFieldOf("occlusion").forGetter(First::occlusion),
                        Codec.FLOAT.optionalFieldOf("friction").forGetter(First::friction),
                        Codec.FLOAT.optionalFieldOf("speed_multiplier").forGetter(First::speedMultiplier),
                        Codec.FLOAT.optionalFieldOf("jump_multiplier").forGetter(First::jumpMultiplier),
                        SoundType.CODEC.optionalFieldOf("sound_type").forGetter(First::soundType),
                        Codec.either(ExtraCodecs.NON_NEGATIVE_INT, ExtensibleCodecs.LIGHT_EMISSION_TYPES.codec()).optionalFieldOf("light_level").forGetter(First::lightLevel),
                        Codec.FLOAT.optionalFieldOf("destroy_time").forGetter(First::destroyTime),
                        Codec.FLOAT.optionalFieldOf("explosion_resistance").forGetter(First::explosionResistance),
                        Codec.BOOL.optionalFieldOf("random_ticks").forGetter(First::randomTicks),
                        Codec.BOOL.optionalFieldOf("dynamic_shape").forGetter(First::dynamicShape),
                        ResourceKey.codec(Registries.LOOT_TABLE).optionalFieldOf("loot_table").forGetter(First::lootTable),
                        Codec.BOOL.optionalFieldOf("ignited_by_lava").forGetter(First::ignitedByLava)
                ).apply(instance, First::new));

        private static final MapCodec<Second> SECOND_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                        Codec.BOOL.optionalFieldOf("liquid").forGetter(Second::liquid),
                        Codec.BOOL.optionalFieldOf("solid").forGetter(Second::solid),
                        UnifiedCodecs.named(PushReaction.class).optionalFieldOf("push_reaction").forGetter(Second::pushReaction),
                        Codec.BOOL.optionalFieldOf("air").forGetter(Second::air),
                        ExtensibleCodecs.ENTITY_PREDICATE_TYPES.codec().optionalFieldOf("valid_spawn").forGetter(Second::validSpawn),
                        ExtensibleCodecs.STATE_PREDICATE_TYPES.codec().optionalFieldOf("redstone_conductor").forGetter(Second::redstoneConductor),
                        ExtensibleCodecs.STATE_PREDICATE_TYPES.codec().optionalFieldOf("suffocating").forGetter(Second::suffocating),
                        ExtensibleCodecs.COLLISION_PREDICATE_TYPES.codec().optionalFieldOf("view_blocking").forGetter(Second::viewBlocking),
                        ExtensibleCodecs.POST_PROCESS_TYPES.codec().optionalFieldOf("post_process").forGetter(Second::postProcess),
                        ExtensibleCodecs.PREDICATE_TYPES.codec().optionalFieldOf("emissive_rendering").forGetter(Second::emissiveRendering),
                        Codec.BOOL.optionalFieldOf("requires_correct_tool_for_drops").forGetter(Second::requiresCorrectToolForDrops),
                        UnifiedCodecs.named(BlockBehaviour.OffsetType.class).optionalFieldOf("offset").forGetter(Second::offset),
                        Codec.BOOL.optionalFieldOf("spawn_terrain_particles").forGetter(Second::spawnTerrainParticles),
                        UnifiedCodecs.named(NoteBlockInstrument.class).optionalFieldOf("instrument").forGetter(Second::instrument),
                        Codec.BOOL.optionalFieldOf("replaceable").forGetter(Second::replaceable),
                        Codec.STRING.optionalFieldOf("description_override").forGetter(Second::descriptionOverride)
                ).apply(instance, Second::new));

        private static final MapCodec<Third> THIRD_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                        FeatureFlags.CODEC.optionalFieldOf("required_features").forGetter(Third::requiredFeatures),
                        Codec.BOOL.optionalFieldOf("no_loot_table").forGetter(Third::noLootTable)
                ).apply(instance, Third::new));

        public static final MapCodec<Properties> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                        FIRST_CODEC.forGetter(Properties::first),
                        SECOND_CODEC.forGetter(Properties::second),
                        THIRD_CODEC.forGetter(Properties::third)
                ).apply(instance, Properties::new));

        public static final Codec<Properties> CODEC = MAP_CODEC.codec();

        private Properties(First first, Second second, Third third) {
            copyFrom = first.copyFrom();
            mapColor = first.mapColor();
            collision = first.collision();
            occlusion = first.occlusion();
            friction = first.friction();
            speedMultiplier = first.speedMultiplier();
            jumpMultiplier = first.jumpMultiplier();
            soundType = first.soundType();
            lightLevel = first.lightLevel();
            destroyTime = first.destroyTime();
            explosionResistance = first.explosionResistance();
            randomTicks = first.randomTicks();
            dynamicShape = first.dynamicShape();
            lootTable = first.lootTable();
            ignitedByLava = first.ignitedByLava();
            liquid = second.liquid();
            solid = second.solid();
            pushReaction = second.pushReaction();
            air = second.air();
            validSpawn = second.validSpawn();
            redstoneConductor = second.redstoneConductor();
            suffocating = second.suffocating();
            viewBlocking = second.viewBlocking();
            postProcess = second.postProcess();
            emissiveRendering = second.emissiveRendering();
            requiresCorrectToolForDrops = second.requiresCorrectToolForDrops();
            offset = second.offset();
            spawnTerrainParticles = second.spawnTerrainParticles();
            instrument = second.instrument();
            replaceable = second.replaceable();
            descriptionOverride = second.descriptionOverride();
            requiredFeatures = third.requiredFeatures();
            noLootTable = third.noLootTable();
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
                    soundType,
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
                    suffocating,
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

        private Third third() {
            return new Third(requiredFeatures, noLootTable);
        }

        private record First(
                Optional<Supplier<Block>> copyFrom,
                Optional<Either<MapColor, ExtensibleCodec.Entry<Function<BlockState, MapColor>>>> mapColor,
                Optional<Boolean> collision,
                Optional<Boolean> occlusion,
                Optional<Float> friction,
                Optional<Float> speedMultiplier,
                Optional<Float> jumpMultiplier,
                Optional<SoundType> soundType,
                Optional<Either<Integer, ExtensibleCodec.Entry<ToIntFunction<BlockState>>>> lightLevel,
                Optional<Float> destroyTime,
                Optional<Float> explosionResistance,
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
                Optional<ExtensibleCodec.Entry<BlockBehaviour.StateArgumentPredicate<EntityType<?>>>> validSpawn,
                Optional<ExtensibleCodec.Entry<BlockBehaviour.StatePredicate>> redstoneConductor,
                Optional<ExtensibleCodec.Entry<BlockBehaviour.StatePredicate>> suffocating,
                Optional<ExtensibleCodec.Entry<BlockBehaviour.StateArgumentPredicate<AABB>>> viewBlocking,
                Optional<ExtensibleCodec.Entry<BlockBehaviour.PostProcess>> postProcess,
                Optional<ExtensibleCodec.Entry<Predicate<BlockState>>> emissiveRendering,
                Optional<Boolean> requiresCorrectToolForDrops,
                Optional<BlockBehaviour.OffsetType> offset,
                Optional<Boolean> spawnTerrainParticles,
                Optional<NoteBlockInstrument> instrument,
                Optional<Boolean> replaceable,
                Optional<String> descriptionOverride
        ) {}

        private record Third(
                Optional<FeatureFlagSet> requiredFeatures,
                Optional<Boolean> noLootTable
        ) {}
    }

    public record SoundType(float volume, float pitch, SoundEvent breakSound, SoundEvent stepSound, SoundEvent placeSound, SoundEvent hitSound, SoundEvent fallSound) {
        public static final Codec<SoundType> CODEC =
                RecordCodecBuilder.create(instance -> instance.group(
                        ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("volume", 1F).forGetter(SoundType::volume),
                        ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("pitch", 1F).forGetter(SoundType::pitch),
                        BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("break_sound").forGetter(SoundType::breakSound),
                        BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("step_sound").forGetter(SoundType::stepSound),
                        BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("place_sound").forGetter(SoundType::placeSound),
                        BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("hit_sound").forGetter(SoundType::hitSound),
                        BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("fall_sound").forGetter(SoundType::fallSound)
                ).apply(instance, SoundType::new));

        public net.minecraft.world.level.block.SoundType convert() {
            return new net.minecraft.world.level.block.SoundType(volume, pitch, breakSound, stepSound, placeSound, hitSound, fallSound);
        }

        public static SoundType create(net.minecraft.world.level.block.SoundType soundType) {
            return new SoundType(soundType.volume, soundType.pitch, soundType.getBreakSound(), soundType.getStepSound(), soundType.getPlaceSound(), soundType.getHitSound(), soundType.getFallSound());
        }
    }

    public static BlockBehaviour.Properties createProperties(Properties properties) {
        BlockBehaviour.Properties actual = properties.copyFrom.map(block -> BlockBehaviour.Properties.ofFullCopy(block.get())).orElseGet(BlockBehaviour.Properties::of);
        if (properties.mapColor.isPresent()) {
            if (properties.mapColor.get().left().isPresent()) actual.mapColor(properties.mapColor.get().left().get());
            else if (properties.mapColor.get().right().isPresent()) actual.mapColor(properties.mapColor.get().right().get().get());
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
        if (properties.soundType.isPresent()) {
            SoundType soundType = properties.soundType.get();
            actual.sound(soundType.convert());
        }
        if (properties.lightLevel.isPresent()) {
            if (properties.lightLevel.get().left().isPresent()) actual.lightLevel(_ -> properties.lightLevel.get().left().get());
            else if (properties.lightLevel.get().right().isPresent()) actual.lightLevel(properties.lightLevel.get().right().get().get());
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
        if (properties.noLootTable.orElse(false)) {
            actual.noLootTable();
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
            actual.isValidSpawn(properties.validSpawn.get().get());
        }
        if (properties.redstoneConductor.isPresent()) {
            actual.isRedstoneConductor(properties.redstoneConductor.get().get());
        }
        if (properties.suffocating.isPresent()) {
            actual.isSuffocating(properties.suffocating.get().get());
        }
        if (properties.viewBlocking.isPresent()) {
            actual.isViewBlocking(properties.viewBlocking.get().get());
        }
        if (properties.postProcess.isPresent()) {
            actual.postProcess(properties.postProcess.get().get());
        }
        if (properties.emissiveRendering.isPresent()) {
            actual.emissiveRendering(properties.emissiveRendering.get().get());
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
        if (properties.requiredFeatures.isPresent()) {
            actual.requiredFeatures = properties.requiredFeatures.get();
        }
        return actual;
    }
}
