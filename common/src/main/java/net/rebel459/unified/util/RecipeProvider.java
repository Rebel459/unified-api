package net.rebel459.unified.util;

import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SuspiciousEffectHolder;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class RecipeProvider extends net.minecraft.data.recipes.RecipeProvider {
    
    private final net.minecraft.data.recipes.RecipeProvider recipeProvider;
    
    public RecipeProvider(net.minecraft.data.recipes.RecipeProvider recipeProvider, HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
        this.recipeProvider = recipeProvider;
    }

    public void generateForEnabledBlockFamilies(FeatureFlagSet flagSet) {
        super.generateForEnabledBlockFamilies(flagSet);
    }

    public void oneToOneConversionRecipe(ItemLike product, ItemLike resource, @Nullable String group) {
        super.oneToOneConversionRecipe(product, resource, group);
    }

    public void oneToOneConversionRecipe(ItemLike product, ItemLike resource, @Nullable String group, int productCount) {
        super.oneToOneConversionRecipe(product, resource, group, productCount);
    }

    public void oreSmelting(List<ItemLike> smeltables, RecipeCategory craftingCategory, CookingBookCategory cookingCategory, ItemLike result, float experience, int cookingTime, String group) {
        super.oreSmelting(smeltables, craftingCategory, cookingCategory, result, experience, cookingTime, group);
    }

    public void oreBlasting(List<ItemLike> smeltables, RecipeCategory craftingCategory, CookingBookCategory cookingCategory, ItemLike result, float experience, int cookingTime, String group) {
        super.oreBlasting(smeltables, craftingCategory, cookingCategory, result, experience, cookingTime, group);
    }

    public <T extends AbstractCookingRecipe> void oreCooking(AbstractCookingRecipe.Factory<T> factory, List<ItemLike> smeltables, RecipeCategory craftingCategory, CookingBookCategory cookingCategory, ItemLike result, float experience, int cookingTime, String group, String fromDesc) {
        recipeProvider.oreCooking(factory, smeltables, craftingCategory, cookingCategory, result, experience, cookingTime, group, fromDesc);
    }

    public void netheriteSmithing(Item base, RecipeCategory category, Item result) {
        super.netheriteSmithing(base, category, result);
    }

    public void trimSmithing(Item trimTemplate, ResourceKey<TrimPattern> patternId, ResourceKey<Recipe<?>> id) {
        super.trimSmithing(trimTemplate, patternId, id);
    }

    public void twoByTwoPacker(RecipeCategory category, ItemLike result, ItemLike ingredient) {
        super.twoByTwoPacker(category, result, ingredient);
    }

    public void threeByThreePacker(RecipeCategory category, ItemLike result, ItemLike ingredient, String unlockedBy) {
        super.threeByThreePacker(category, result, ingredient, unlockedBy);
    }

    public void threeByThreePacker(RecipeCategory category, ItemLike result, ItemLike ingredient) {
        super.threeByThreePacker(category, result, ingredient);
    }

    public void planksFromLog(ItemLike result, TagKey<Item> logs, int count) {
        super.planksFromLog(result, logs, count);
    }

    public void planksFromLogs(ItemLike result, TagKey<Item> logs, int count) {
        super.planksFromLogs(result, logs, count);
    }

    public void woodFromLogs(ItemLike result, ItemLike log) {
        super.woodFromLogs(result, log);
    }

    public void woodenBoat(ItemLike result, ItemLike planks) {
        super.woodenBoat(result, planks);
    }

    public void chestBoat(ItemLike chestBoat, ItemLike boat) {
        super.chestBoat(chestBoat, boat);
    }

    public RecipeBuilder buttonBuilder(ItemLike result, Ingredient base) {
        return recipeProvider.buttonBuilder(result, base);
    }

    public RecipeBuilder doorBuilder(ItemLike result, Ingredient base) {
        return super.doorBuilder(result, base);
    }

    public RecipeBuilder fenceBuilder(ItemLike result, Ingredient base) {
        return recipeProvider.fenceBuilder(result, base);
    }

    public RecipeBuilder fenceGateBuilder(ItemLike result, Ingredient planks) {
        return recipeProvider.fenceGateBuilder(result, planks);
    }

    public void pressurePlate(ItemLike result, ItemLike base) {
        super.pressurePlate(result, base);
    }

    public RecipeBuilder pressurePlateBuilder(RecipeCategory category, ItemLike result, Ingredient base) {
        return recipeProvider.pressurePlateBuilder(category, result, base);
    }

    public void slab(RecipeCategory category, ItemLike result, ItemLike base) {
        super.slab(category, result, base);
    }

    public void shelf(ItemLike result, ItemLike strippedLogs) {
        super.shelf(result, strippedLogs);
    }

    public RecipeBuilder slabBuilder(RecipeCategory category, ItemLike result, Ingredient base) {
        return super.slabBuilder(category, result, base);
    }

    public RecipeBuilder stairBuilder(ItemLike result, Ingredient base) {
        return super.stairBuilder(result, base);
    }

    public RecipeBuilder trapdoorBuilder(ItemLike result, Ingredient base) {
        return super.trapdoorBuilder(result, base);
    }

    public RecipeBuilder signBuilder(ItemLike result, Ingredient planks) {
        return recipeProvider.signBuilder(result, planks);
    }

    public void hangingSign(ItemLike result, ItemLike ingredient) {
        super.hangingSign(result, ingredient);
    }

    public void colorItemWithDye(List<Item> dyes, List<Item> items, String groupName, RecipeCategory category) {
        super.colorItemWithDye(dyes, items, groupName, category);
    }

    public void colorWithDye(List<Item> dyes, List<Item> dyedItems, @Nullable Item uncoloredItem, String groupName, RecipeCategory category) {
        super.colorWithDye(dyes, dyedItems, uncoloredItem, groupName, category);
    }

    public void carpet(ItemLike result, ItemLike sourceItem) {
        super.carpet(result, sourceItem);
    }

    public void bedFromPlanksAndWool(ItemLike result, ItemLike wool) {
        super.bedFromPlanksAndWool(result, wool);
    }

    public void banner(ItemLike result, ItemLike wool) {
        super.banner(result, wool);
    }

    public void stainedGlassFromGlassAndDye(ItemLike result, ItemLike dye) {
        super.stainedGlassFromGlassAndDye(result, dye);
    }

    public void dryGhast(ItemLike result) {
        super.dryGhast(result);
    }

    public void harness(ItemLike result, ItemLike wool) {
        super.harness(result, wool);
    }

    public void stainedGlassPaneFromStainedGlass(ItemLike result, ItemLike stainedGlass) {
        super.stainedGlassPaneFromStainedGlass(result, stainedGlass);
    }

    public void stainedGlassPaneFromGlassPaneAndDye(ItemLike result, ItemLike dye) {
        super.stainedGlassPaneFromGlassPaneAndDye(result, dye);
    }

    public void coloredTerracottaFromTerracottaAndDye(ItemLike result, ItemLike dye) {
        super.coloredTerracottaFromTerracottaAndDye(result, dye);
    }

    public void concretePowder(ItemLike result, ItemLike dye) {
        super.concretePowder(result, dye);
    }

    public void candle(ItemLike result, ItemLike dye) {
        super.candle(result, dye);
    }

    public void wall(RecipeCategory category, ItemLike result, ItemLike base) {
        super.wall(category, result, base);
    }

    public RecipeBuilder wallBuilder(RecipeCategory category, ItemLike result, Ingredient base) {
        return recipeProvider.wallBuilder(category, result, base);
    }

    public RecipeBuilder bricksBuilder(RecipeCategory category, ItemLike result, Ingredient base) {
        return recipeProvider.bricksBuilder(category, result, base);
    }

    public RecipeBuilder tilesBuilder(RecipeCategory category, ItemLike result, Ingredient base) {
        return recipeProvider.tilesBuilder(category, result, base);
    }

    public void polished(RecipeCategory category, ItemLike result, ItemLike base) {
        super.polished(category, result, base);
    }

    public RecipeBuilder polishedBuilder(RecipeCategory category, ItemLike result, Ingredient base) {
        return recipeProvider.polishedBuilder(category, result, base);
    }

    public void cut(RecipeCategory category, ItemLike result, ItemLike base) {
        super.cut(category, result, base);
    }

    public ShapedRecipeBuilder cutBuilder(RecipeCategory category, ItemLike result, Ingredient base) {
        return recipeProvider.cutBuilder(category, result, base);
    }

    public void chiseled(RecipeCategory category, ItemLike result, ItemLike base) {
        super.chiseled(category, result, base);
    }

    public void mosaicBuilder(RecipeCategory category, ItemLike result, ItemLike base) {
        super.mosaicBuilder(category, result, base);
    }

    public ShapedRecipeBuilder chiseledBuilder(RecipeCategory category, ItemLike result, Ingredient base) {
        return super.chiseledBuilder(category, result, base);
    }

    public void stonecutterResultFromBase(RecipeCategory category, ItemLike result, ItemLike base) {
        super.stonecutterResultFromBase(category, result, base);
    }

    public void stonecutterResultFromBase(RecipeCategory category, ItemLike result, ItemLike base, int count) {
        super.stonecutterResultFromBase(category, result, base, count);
    }

    public void smeltingResultFromBase(ItemLike result, ItemLike base) {
        recipeProvider.smeltingResultFromBase(result, base);
    }

    public void nineBlockStorageRecipes(RecipeCategory unpackedFormCategory, ItemLike unpackedForm, RecipeCategory packedFormCategory, ItemLike packedForm) {
        super.nineBlockStorageRecipes(unpackedFormCategory, unpackedForm, packedFormCategory, packedForm);
    }

    public void nineBlockStorageRecipesWithCustomPacking(RecipeCategory unpackedFormCategory, ItemLike unpackedForm, RecipeCategory packedFormCategory, ItemLike packedForm, String packingRecipeId, String packingRecipeGroup) {
        super.nineBlockStorageRecipesWithCustomPacking(unpackedFormCategory, unpackedForm, packedFormCategory, packedForm, packingRecipeId, packingRecipeGroup);
    }

    public void nineBlockStorageRecipesRecipesWithCustomUnpacking(RecipeCategory unpackedFormCategory, ItemLike unpackedForm, RecipeCategory packedFormCategory, ItemLike packedForm, String unpackingRecipeId, String unpackingRecipeGroup) {
        super.nineBlockStorageRecipesRecipesWithCustomUnpacking(unpackedFormCategory, unpackedForm, packedFormCategory, packedForm, unpackingRecipeId, unpackingRecipeGroup);
    }

    public void nineBlockStorageRecipes(RecipeCategory unpackedFormCategory, ItemLike unpackedForm, RecipeCategory packedFormCategory, ItemLike packedForm, String packingRecipeId, @Nullable String packingRecipeGroup, String unpackingRecipeId, @Nullable String unpackingRecipeGroup) {
        recipeProvider.nineBlockStorageRecipes(unpackedFormCategory, unpackedForm, packedFormCategory, packedForm, packingRecipeId, packingRecipeGroup, unpackingRecipeId, unpackingRecipeGroup);
    }

    public void copySmithingTemplate(ItemLike smithingTemplate, ItemLike baseMaterial) {
        super.copySmithingTemplate(smithingTemplate, baseMaterial);
    }

    public void copySmithingTemplate(ItemLike smithingTemplate, Ingredient baseMaterials) {
        super.copySmithingTemplate(smithingTemplate, baseMaterials);
    }

    public <T extends AbstractCookingRecipe> void cookRecipes(String source, AbstractCookingRecipe.Factory<T> factory, int cookingTime) {
        super.cookRecipes(source, factory, cookingTime);
    }

    public <T extends AbstractCookingRecipe> void simpleCookingRecipe(String source, AbstractCookingRecipe.Factory<T> factory, int cookingTime, ItemLike base, ItemLike result, float experience) {
        recipeProvider.simpleCookingRecipe(source, factory, cookingTime, base, result, experience);
    }

    public void waxRecipes(FeatureFlagSet flagSet) {
        super.waxRecipes(flagSet);
    }

    public void grate(Block grateBlock, Block material) {
        super.grate(grateBlock, material);
    }

    public void copperBulb(Block copperBulb, Block copperMaterial) {
        super.copperBulb(copperBulb, copperMaterial);
    }

    public void waxedChiseled(Block result, Block material) {
        super.waxedChiseled(result, material);
    }

    public void suspiciousStew(Item item, SuspiciousEffectHolder effectHolder) {
        super.suspiciousStew(item, effectHolder);
    }

    public void dyedItem(Item target, String group) {
        super.dyedItem(target, group);
    }

    public void dyedShulkerBoxRecipe(Item dye, Item dyedResult) {
        super.dyedShulkerBoxRecipe(dye, dyedResult);
    }

    public void dyedBundleRecipe(Item dye, Item dyedResult) {
        super.dyedBundleRecipe(dye, dyedResult);
    }

    public void generateRecipes(BlockFamily family, FeatureFlagSet flagSet) {
        super.generateRecipes(family, flagSet);
    }

    public void generateCraftingRecipe(BlockFamily family, BlockFamily.Variant variant, Block result, ItemLike base) {
        recipeProvider.generateCraftingRecipe(family, variant, result, base);
    }

    public void generateStonecutterRecipe(BlockFamily family, BlockFamily.Variant variant, Block base) {
        recipeProvider.generateStonecutterRecipe(family, variant, base);
    }

    public Block getBaseBlockForCrafting(BlockFamily family, BlockFamily.Variant variant) {
        return recipeProvider.getBaseBlockForCrafting(family, variant);
    }

    public static Criterion<EnterBlockTrigger.TriggerInstance> insideOf(Block block) {
        return net.minecraft.data.recipes.RecipeProvider.insideOf(block);
    }

    public Criterion<BredAnimalsTrigger.TriggerInstance> bredAnimal() {
        return super.bredAnimal();
    }

    public Criterion<InventoryChangeTrigger.TriggerInstance> has(MinMaxBounds.Ints count, ItemLike item) {
        return recipeProvider.has(count, item);
    }

    public Criterion<InventoryChangeTrigger.TriggerInstance> has(ItemLike item) {
        return super.has(item);
    }

    public Criterion<InventoryChangeTrigger.TriggerInstance> has(TagKey<Item> tag) {
        return super.has(tag);
    }

    public static Criterion<InventoryChangeTrigger.TriggerInstance> inventoryTrigger(ItemPredicate.Builder... predicates) {
        return net.minecraft.data.recipes.RecipeProvider.inventoryTrigger(predicates);
    }

    public static Criterion<InventoryChangeTrigger.TriggerInstance> inventoryTrigger(ItemPredicate... predicates) {
        return net.minecraft.data.recipes.RecipeProvider.inventoryTrigger(predicates);
    }

    public static String getHasName(ItemLike baseBlock) {
        return net.minecraft.data.recipes.RecipeProvider.getHasName(baseBlock);
    }

    public static String getItemName(ItemLike itemLike) {
        return net.minecraft.data.recipes.RecipeProvider.getItemName(itemLike);
    }

    public static String getSimpleRecipeName(ItemLike itemLike) {
        return net.minecraft.data.recipes.RecipeProvider.getSimpleRecipeName(itemLike);
    }

    public static String getConversionRecipeName(ItemLike product, ItemLike material) {
        return net.minecraft.data.recipes.RecipeProvider.getConversionRecipeName(product, material);
    }

    public static String getSmeltingRecipeName(ItemLike product) {
        return net.minecraft.data.recipes.RecipeProvider.getSmeltingRecipeName(product);
    }

    public static String getBlastingRecipeName(ItemLike product) {
        return net.minecraft.data.recipes.RecipeProvider.getBlastingRecipeName(product);
    }

    public Ingredient tag(TagKey<Item> id) {
        return super.tag(id);
    }

    public ShapedRecipeBuilder shaped(RecipeCategory category, ItemLike item) {
        return super.shaped(category, item);
    }

    public ShapedRecipeBuilder shaped(RecipeCategory category, ItemLike item, int count) {
        return super.shaped(category, item, count);
    }

    public ShapelessRecipeBuilder shapeless(RecipeCategory category, ItemStackTemplate result) {
        return super.shapeless(category, result);
    }

    public ShapelessRecipeBuilder shapeless(RecipeCategory category, ItemLike item) {
        return super.shapeless(category, item);
    }

    public ShapelessRecipeBuilder shapeless(RecipeCategory category, ItemLike item, int count) {
        return super.shapeless(category, item, count);
    }
}
