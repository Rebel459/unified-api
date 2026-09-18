package net.rebel459.unified.api.builder;

import net.minecraft.core.component.DataComponents;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;
import net.minecraft.world.level.storage.loot.providers.number.floats.ResolvableFloat;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;
import net.rebel459.unified.api.util.RecipeProvider;

public final class ColoredBlockPreset {

    final ColoredBlockSet.Settings settings;

    ColoredBlockPreset(ColoredBlockSet.Settings settings) {
        this.settings = settings;
    }

    public static final ColoredBlockPreset DEFAULT = new ColoredBlockSet.PresetBuilder()
            .build();

    public static final ColoredBlockPreset WOOL = create()
            .builder((_, builder) -> builder
                    .properties(properties -> properties
                            .copyFrom(Blocks.WOOL::white)
                            .flammable(30, 60)
                    )
                    .data(data -> data
                            .dropSelf()
                            .tag(BlockTags.SHEARS_MAJOR_BREAKING_SPEED)
                    )
                    .itemProperties(itemProperties -> itemProperties
                            .component(DataComponents.COOKING_FUEL, new CookingFuel(new ResolvableInt.Constant(100), ResolvableFloat.fromKey(ContextFloatProviders.COOKING_DEFAULT_SPEED_MULTIPLIER)))
                    )
            )
            .dyeRecipe((dye, otherBlocks, item, provider) -> {
                provider.shapeless(RecipeCategory.BUILDING_BLOCKS, item)
                        .requires(dye)
                        .requires(Ingredient.of(otherBlocks.stream()))
                        .unlockedBy("has_needed_dye", provider.has(dye))
                        .group("wool")
                        .save(provider.output, "dye_" + RecipeProvider.getItemName(item));
            })
            .build();

    public static final ColoredBlockPreset DYED_TERRACOTTA = create()
            .builder((_, builder) ->
                    builder.properties(properties -> properties
                                    .copyFrom(Blocks.DYED_TERRACOTTA::white)
                            )
                            .data(data -> data
                                    .dropSelf()
                                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                            )
            )
            .dyeRecipe((dye, _, item, provider) -> provider.coloredTerracottaFromTerracottaAndDye(item, dye))
            .build();

    public static final ColoredBlockPreset CONCRETE = create()
            .builder((_, builder) -> builder
                    .properties(properties -> properties
                            .copyFrom(Blocks.CONCRETE::white)
                    )
                    .data(data -> data
                            .dropSelf()
                            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    )
            )
            .build();

    public static final ColoredBlockPreset CONCRETE_POWDER = create()
            .builder((_, builder) -> builder
                    .properties(properties -> properties
                            .copyFrom(Blocks.CONCRETE_POWDER::white)
                    )
                    .data(data -> data
                            .dropSelf()
                            .tag(BlockTags.MINEABLE_WITH_SHOVEL)
                    )
            )
            .dyeRecipe((dye, _, item, provider) -> provider.concretePowder(item, dye))
            .build();

    public static ColoredBlockSet.PresetBuilder create() {
        return createFrom(DEFAULT);
    }

    public static ColoredBlockSet.PresetBuilder createFrom(ColoredBlockPreset preset) {
        return new ColoredBlockSet.PresetBuilder(preset.settings.copy());
    }
}
