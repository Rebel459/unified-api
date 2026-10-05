package net.rebel459.unified.impl.client.registry;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.model.object.boat.RaftModel;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.RaftRenderer;
import net.minecraft.resources.Identifier;
import net.rebel459.unified.api.client.core.UnifiedClientHelpers;
import net.rebel459.unified.api.registry.VanillaEntityCodecs;

public class VanillaEntityRenderers {

    public static void init() {
        VanillaEntityCodecs.BOAT.bind((entity, id) -> {
            ModelLayerLocation location = new ModelLayerLocation(getLayerName(id, "boat"), "main");
            UnifiedClientHelpers.ENTITY_RENDERERS.addLayerDefinition(location, BoatModel::createBoatModel);
            UnifiedClientHelpers.ENTITY_RENDERERS.addEntityRenderer(entity::get, ctx -> new BoatRenderer(ctx, location));
        });
        VanillaEntityCodecs.CHEST_BOAT.bind((entity, id) -> {
            ModelLayerLocation location = new ModelLayerLocation(getLayerName(id, "chest_boat"), "main");
            UnifiedClientHelpers.ENTITY_RENDERERS.addLayerDefinition(location, BoatModel::createChestBoatModel);
            UnifiedClientHelpers.ENTITY_RENDERERS.addEntityRenderer(entity::get, ctx -> new BoatRenderer(ctx, location));
        });
        VanillaEntityCodecs.RAFT.bind((entity, id) -> {
            ModelLayerLocation location = new ModelLayerLocation(getLayerName(id, "raft"), "main");
            UnifiedClientHelpers.ENTITY_RENDERERS.addLayerDefinition(location, RaftModel::createRaftModel);
            UnifiedClientHelpers.ENTITY_RENDERERS.addEntityRenderer(entity::get, ctx -> new RaftRenderer(ctx, location));
        });
        VanillaEntityCodecs.CHEST_RAFT.bind((entity, id) -> {
            ModelLayerLocation location = new ModelLayerLocation(getLayerName(id, "chest_raft"), "main");
            UnifiedClientHelpers.ENTITY_RENDERERS.addLayerDefinition(location, RaftModel::createChestRaftModel);
            UnifiedClientHelpers.ENTITY_RENDERERS.addEntityRenderer(entity::get, ctx -> new RaftRenderer(ctx, location));
        });
    }

    private static Identifier getLayerName(Identifier id, String type) {
        String path = id.getPath();
        int suffixStart = path.lastIndexOf('_');
        String name = suffixStart >= 0 ? path.substring(0, suffixStart) : path;
        return Identifier.fromNamespaceAndPath(id.getNamespace(), type + "/" + name);
    }
}
