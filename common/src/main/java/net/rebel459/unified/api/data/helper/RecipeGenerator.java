package net.rebel459.unified.api.data.helper;

import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.api.util.RecipeProvider;
import net.rebel459.unified.impl.core.DataProviders;

import java.util.Optional;
import java.util.function.Consumer;

public final class RecipeGenerator extends HelperGenerator {

    public RecipeGenerator(String modId, Optional<ExtensibleCodec.Entry<Boolean>> requirement) {
        super(modId, requirement);
    }

    public void add(Consumer<RecipeProvider> recipes) {
        DataProviders.RECIPES.add(this.modId, new DataProviders.RecipeRequest(requirement, recipes));
    }
}
