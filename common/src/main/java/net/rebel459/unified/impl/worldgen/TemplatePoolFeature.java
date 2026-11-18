package net.rebel459.unified.impl.worldgen;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pools.*;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public record TemplatePoolFeature(
        Holder<StructureTemplatePool> startPool,
        Optional<Identifier> startJigsawName,
        int size,
        JigsawStructure.MaxDistance maxDistanceFromCenter,
        Optional<Rotation> rotation,
        LiquidSettings liquidSettings,
        boolean ignoreAir,
        boolean naturalLeaves,
        List<Holder<StructureProcessorList>> markedProcessors
) implements Feature {

    private static final Set<Identifier> MISSING_JIGSAWS = ConcurrentHashMap.newKeySet();

    public static final MapCodec<TemplatePoolFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(TemplatePoolFeature::startPool),
            Identifier.CODEC.optionalFieldOf("start_jigsaw_name").forGetter(TemplatePoolFeature::startJigsawName),
            Codec.intRange(0, 20).optionalFieldOf("size", 0).forGetter(TemplatePoolFeature::size),
            JigsawStructure.MaxDistance.CODEC.optionalFieldOf("max_distance_from_center", new JigsawStructure.MaxDistance(16, DimensionType.Y_SIZE)).forGetter(TemplatePoolFeature::maxDistanceFromCenter),
            Rotation.CODEC.optionalFieldOf("rotation").forGetter(TemplatePoolFeature::rotation),
            LiquidSettings.CODEC.optionalFieldOf("liquid_settings", JigsawStructure.DEFAULT_LIQUID_SETTINGS).forGetter(TemplatePoolFeature::liquidSettings),
            Codec.BOOL.optionalFieldOf("ignore_air", false).forGetter(TemplatePoolFeature::ignoreAir),
            Codec.BOOL.optionalFieldOf("natural_leaves", false).forGetter(TemplatePoolFeature::naturalLeaves),
            StructureProcessorType.LIST_CODEC.listOf().optionalFieldOf("marked_processors", List.of()).forGetter(TemplatePoolFeature::markedProcessors)
    ).apply(instance, TemplatePoolFeature::new));

    @Override
    public MapCodec<TemplatePoolFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        ServerLevel serverLevel = level.getLevel();
        StructureTemplateManager templates = serverLevel.getStructureTemplateManager();
        StructurePoolElement start = this.startPool.value().getRandomTemplate(random);
        if (start == EmptyPoolElement.INSTANCE) return false;

        Rotation rotation = this.rotation.orElseGet(() -> Rotation.getRandom(random));
        List<StructureTemplate.JigsawBlockInfo> jigsaws = start.getShuffledJigsawBlocks(templates, origin, rotation, random);
        BlockPos position;
        if (this.startJigsawName.isPresent()) {
            Identifier name = this.startJigsawName.get();
            BlockPos anchor = null;
            for (StructureTemplate.JigsawBlockInfo jigsaw : jigsaws) {
                if (name.equals(jigsaw.name())) {
                    anchor = jigsaw.pos();
                    break;
                }
            }
            if (anchor == null) {
                if (MISSING_JIGSAWS.add(name)) {
                    LogUtils.getLogger().error("No starting jigsaw {} found in start pool {}", name, this.startPool.unwrapKey().map(key -> key.identifier().toString()).orElse("<unregistered>"));
                }
                return false;
            }
            position = origin.subtract(anchor.subtract(origin));
        } else {
            BoundingBox box = start.getBoundingBox(templates, origin, rotation);
            position = origin.offset(
                    origin.getX() - Math.floorDiv(box.minX() + box.maxX(), 2),
                    origin.getY() - box.minY(),
                    origin.getZ() - Math.floorDiv(box.minZ() + box.maxZ(), 2)
            );
        }

        BoundingBox bounds = writableBounds(level);
        NaturalLeavesProcessor leafProcessor = this.naturalLeaves ? new NaturalLeavesProcessor() : null;
        if (this.size <= 0 || !canExpand(jigsaws)) {
            boolean placed = this.placeElement(start, level, chunkGenerator, templates, position, origin, rotation, bounds, random, leafProcessor);
            if (leafProcessor != null) leafProcessor.finish(level);
            return placed;
        }

        BoundingBox startBox = start.getBoundingBox(templates, position, rotation);
        PoolElementStructurePiece center = new PoolElementStructurePiece(templates, start, position, start.getGroundLevelDelta(), rotation, startBox, this.liquidSettings);
        List<PoolElementStructurePiece> pieces = new ArrayList<>();
        pieces.add(center);

        int horizontal = this.maxDistanceFromCenter.horizontal();
        int vertical = this.maxDistanceFromCenter.vertical();
        double minX = Math.max(origin.getX() - horizontal, bounds.minX());
        double minY = Math.max(origin.getY() - vertical, level.getMinY());
        double minZ = Math.max(origin.getZ() - horizontal, bounds.minZ());
        double maxX = Math.min(origin.getX() + horizontal, bounds.maxX()) + 1.0;
        double maxY = Math.min(origin.getY() + vertical, level.getMaxY()) + 1.0;
        double maxZ = Math.min(origin.getZ() + horizontal, bounds.maxZ()) + 1.0;
        if (minX < maxX && minY < maxY && minZ < maxZ) {
            VoxelShape free = Shapes.join(Shapes.create(new AABB(minX, minY, minZ, maxX, maxY, maxZ)), Shapes.create(AABB.of(startBox)), BooleanOp.ONLY_FIRST);
            JigsawPlacement.addPieces(
                    serverLevel.getChunkSource().randomState(),
                    this.size,
                    false,
                    chunkGenerator,
                    templates,
                    level,
                    random,
                    level.registryAccess().lookupOrThrow(Registries.TEMPLATE_POOL),
                    center,
                    pieces,
                    free,
                    PoolAliasLookup.EMPTY,
                    this.liquidSettings
            );
        }

        boolean placed = false;
        for (PoolElementStructurePiece piece : pieces) {
            placed |= this.placeElement(piece.getElement(), level, chunkGenerator, templates, piece.getPosition(), origin, piece.getRotation(), bounds, random, leafProcessor);
        }
        if (leafProcessor != null) leafProcessor.finish(level);
        return placed;
    }

    private boolean placeElement(StructurePoolElement element, WorldGenLevel level, ChunkGenerator chunkGenerator, StructureTemplateManager templates, BlockPos position, BlockPos origin, Rotation rotation, BoundingBox bounds, RandomSource random, @Nullable NaturalLeavesProcessor leafProcessor) {
        if (element instanceof SinglePoolElement single) {
            StructureTemplate template = single.getTemplate(templates);
            StructurePlaceSettings settings = single.getSettings(rotation, bounds, this.liquidSettings, false);
            if (this.ignoreAir) settings.getProcessors().addFirst(BlockIgnoreProcessor.AIR);
            if (!this.markedProcessors.isEmpty()) {
                MarkedProcessor processor = MarkedProcessor.create(template, this.markedProcessors);
                if (processor != null) settings.addProcessor(processor);
            }
            settings.addProcessor(KeepOriginalProcessor.INSTANCE);
            if (leafProcessor != null) settings.addProcessor(leafProcessor);
            return template.placeInWorld(level, position, origin, settings, random, 18);
        }
        if (element instanceof ListPoolElement list) {
            boolean placed = false;
            for (StructurePoolElement child : list.getElements()) {
                placed |= this.placeElement(child, level, chunkGenerator, templates, position, origin, rotation, bounds, random, leafProcessor);
            }
            return placed;
        }
        StructureManager structureManager = level.getLevel().structureManager();
        if (level instanceof WorldGenRegion region) structureManager = structureManager.forWorldGenRegion(region);
        return element.place(templates, level, structureManager, chunkGenerator, position, origin, rotation, bounds, random, this.liquidSettings, false);
    }

    private static boolean canExpand(List<StructureTemplate.JigsawBlockInfo> jigsaws) {
        for (StructureTemplate.JigsawBlockInfo jigsaw : jigsaws) {
            if (!Pools.EMPTY.equals(jigsaw.pool())) return true;
        }
        return false;
    }

    private static BoundingBox writableBounds(WorldGenLevel level) {
        if (!(level instanceof WorldGenRegion region)) return BoundingBox.infinite();
        ChunkPos center = region.getCenter();
        int radius = region.writeRadius;
        return new BoundingBox(
                (center.x() - radius) << 4,
                level.getMinY(),
                (center.z() - radius) << 4,
                ((center.x() + radius) << 4) + 15,
                level.getMaxY(),
                ((center.z() + radius) << 4) + 15
        );
    }
}
