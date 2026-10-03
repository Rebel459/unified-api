package net.rebel459.unified.fabric.datagen;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ItemModelOutput;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelDispatcher;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

final class ModelGroup extends FabricModelProvider {
    final List<FabricModelProvider> contributions = new ArrayList<>();
    private final Map<Object, Definition> definitions = new LinkedHashMap<>();
    private final Map<Item, Item> copies = new LinkedHashMap<>();
    private ItemModelGenerators items;
    private FabricModelProvider current;

    ModelGroup(FabricPackOutput output) { super(output); }

    @Override public void generateBlockStateModels(BlockModelGenerators original) {
        definitions.clear();
        copies.clear();
        BiConsumer<Identifier, ModelInstance> modelOutput = (id, model) -> {
            if (accept("model " + id, model.get())) original.modelOutput.accept(id, model);
        };
        ItemModelOutput itemOutput = new ItemModelOutput() {
            @Override public void accept(Item item, ItemModel.Unbaked model, ClientItem.Properties properties) {
                if (copies.containsKey(item)) throw new IllegalStateException("Item " + item + " has both a model and a copy definition");
                JsonElement json = ClientItem.CODEC.encodeStart(JsonOps.INSTANCE, new ClientItem(model, properties)).getOrThrow();
                if (ModelGroup.this.accept(item, json)) original.itemModelOutput.accept(item, model, properties);
            }

            @Override public void copy(Item source, Item target) {
                if (definitions.containsKey(target)) throw new IllegalStateException("Item " + target + " has both a model and a copy definition");
                Item previous = copies.putIfAbsent(target, source);
                if (previous != null && previous != source) throw new IllegalStateException("Conflicting copied model for " + target);
                if (previous == null) original.itemModelOutput.copy(source, target);
            }
        };
        BlockModelGenerators blocks = new BlockModelGenerators(generator -> {
            JsonElement json = BlockStateModelDispatcher.CODEC.encodeStart(JsonOps.INSTANCE, generator.create()).getOrThrow();
            if (accept(generator.block(), json)) original.blockStateOutput.accept(generator);
        }, itemOutput, modelOutput);
        items = new ItemModelGenerators(itemOutput, modelOutput);
        for (FabricModelProvider provider : contributions) {
            current = provider;
            provider.generateBlockStateModels(blocks);
        }
    }

    @Override public void generateItemModels(ItemModelGenerators original) {
        for (FabricModelProvider provider : contributions) {
            current = provider;
            provider.generateItemModels(items);
        }
    }

    private boolean accept(Object key, JsonElement json) {
        Definition previous = definitions.putIfAbsent(key, new Definition(json, current.getClass().getName()));
        if (previous == null) return true;
        if (!previous.json().equals(json)) throw new IllegalStateException("Conflicting " + key + " from "
                + previous.source() + " and " + current.getClass().getName());
        return false;
    }

    private record Definition(JsonElement json, String source) {}
}
