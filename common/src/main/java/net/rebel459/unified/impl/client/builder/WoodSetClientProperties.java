package net.rebel459.unified.impl.client.builder;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.model.object.boat.RaftModel;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.RaftRenderer;
import net.minecraft.resources.Identifier;
import net.rebel459.unified.api.client.core.UnifiedClientHelpers;
import net.rebel459.unified.api.builder.WoodSet;

import java.util.Objects;

public class WoodSetClientProperties {

    public static void init(boolean layers, boolean renderers) {
        for (WoodSet woodset : WoodSet.WOOD_SETS) {
            if (!woodset.hasBoats()) return;

            Identifier layerName = woodset.getId().withPrefix("boat/");
            Identifier chestLayerName = woodset.getId().withPrefix("chest_boat/");

            final ModelLayerLocation boatModelLayer = new ModelLayerLocation(layerName, "main");
            final ModelLayerLocation chestBoatModelLayer = new ModelLayerLocation(chestLayerName, "main");

            final boolean raft = Objects.equals(woodset.getSettings().getBoats(), WoodSet.Boats.RAFTS);

            if (layers) {
                UnifiedClientHelpers.ENTITY_RENDERERS.addLayerDefinition(boatModelLayer, raft ? RaftModel::createRaftModel : BoatModel::createBoatModel);
                UnifiedClientHelpers.ENTITY_RENDERERS.addLayerDefinition(chestBoatModelLayer, raft ? RaftModel::createChestRaftModel : BoatModel::createChestBoatModel);
            }
            if (renderers) {
                UnifiedClientHelpers.ENTITY_RENDERERS.addEntityRenderer(woodset.getBoat()::get, ctx -> raft ? new RaftRenderer(ctx, boatModelLayer) : new BoatRenderer(ctx, boatModelLayer));
                UnifiedClientHelpers.ENTITY_RENDERERS.addEntityRenderer(woodset.getChestBoat()::get, ctx -> raft ? new RaftRenderer(ctx, chestBoatModelLayer) : new BoatRenderer(ctx, chestBoatModelLayer));
            }
        }
    }
}
