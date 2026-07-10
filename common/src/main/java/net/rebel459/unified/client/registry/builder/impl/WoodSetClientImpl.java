package net.rebel459.unified.client.registry.builder.impl;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.model.object.boat.RaftModel;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.RaftRenderer;
import net.minecraft.resources.Identifier;
import net.rebel459.unified.platform.client.UnifiedClientHelpers;
import net.rebel459.unified.util.builder.WoodSet;

import java.util.Objects;

public class WoodSetClientImpl {
    
    public static void init() {
        for (WoodSet woodset : WoodSet.WOOD_SETS) {
            registerBoatModels(woodset);
        }
    }

    private static void registerBoatModels(WoodSet woodset) {
        if (!woodset.hasBoats()) return;

        Identifier layerName = woodset.getId().withPrefix("boat/");
        Identifier chestLayerName = woodset.getId().withPrefix("chest_boat/");

        final ModelLayerLocation boatModelLayer = new ModelLayerLocation(layerName, "main");
        final ModelLayerLocation chestBoatModelLayer = new ModelLayerLocation(chestLayerName, "main");

        final boolean raft = Objects.equals(woodset.getSettings().getBoats(), WoodSet.Boats.RAFTS);

        UnifiedClientHelpers.ENTITY_RENDERERS.addLayerDefinition(boatModelLayer, raft ? RaftModel::createRaftModel : BoatModel::createBoatModel);
        UnifiedClientHelpers.ENTITY_RENDERERS.addEntityRenderer(woodset.getBoat()::get, ctx -> raft ? new RaftRenderer(ctx, boatModelLayer) : new BoatRenderer(ctx, boatModelLayer));

        UnifiedClientHelpers.ENTITY_RENDERERS.addLayerDefinition(chestBoatModelLayer, raft ? RaftModel::createChestRaftModel : BoatModel::createChestBoatModel);
        UnifiedClientHelpers.ENTITY_RENDERERS.addEntityRenderer(woodset.getChestBoat()::get, ctx -> raft ? new RaftRenderer(ctx, chestBoatModelLayer) : new BoatRenderer(ctx, chestBoatModelLayer));
    }
}
