package net.rebel459.unified.api.data.set;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.SmithingTransformRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.rebel459.unified.api.asset.ItemAssets;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.api.core.SuppliedItem;
import net.rebel459.unified.api.core.UnifiedPlatform;
import net.rebel459.unified.api.data.helper.CreativeEntryGenerator;
import net.rebel459.unified.api.data.helper.EquipmentAssetGenerator;
import net.rebel459.unified.api.data.helper.RecipeGenerator;
import net.rebel459.unified.api.data.helper.SimpleBabyArmorGenerator;
import net.rebel459.unified.api.data.helper.TagGenerator;
import net.rebel459.unified.api.data.registry.ItemGenerator;
import net.rebel459.unified.api.platform.ModLoader;
import net.rebel459.unified.api.registry.VanillaItemCodecs;
import net.rebel459.unified.api.util.RecipeProvider;
import net.rebel459.unified.impl.data.set.EquipmentSetProperties;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class EquipmentSet {

    public static final List<EquipmentSet> EQUIPMENT_SETS = new CopyOnWriteArrayList<>();

    private final List<SuppliedItem> registeredItems = new ArrayList<>();

    private @Nullable ToolMaterial toolMaterial = null;
    private @Nullable ArmorMaterial armorMaterial = null;

    private final Identifier id;

    private final ItemGenerator items;
    private final RecipeGenerator recipes;
    private final TagGenerator tags;
    private final EquipmentAssetGenerator equipmentAssets;
    private final SimpleBabyArmorGenerator simpleBabyArmor;

    private @Nullable SuppliedItem sword;
    private @Nullable SuppliedItem spear;
    private @Nullable SuppliedItem axe;
    private @Nullable SuppliedItem pickaxe;
    private @Nullable SuppliedItem shovel;
    private @Nullable SuppliedItem hoe;
    private @Nullable SuppliedItem helmet;
    private @Nullable SuppliedItem chestplate;
    private @Nullable SuppliedItem leggings;
    private @Nullable SuppliedItem boots;
    private @Nullable SuppliedItem horseArmor;
    private @Nullable SuppliedItem nautilusArmor;

    private @Nullable ResourceKey<EquipmentAsset> equipmentAsset = null;

    public enum Target {
        SWORD,
        SPEAR,
        AXE,
        PICKAXE,
        SHOVEL,
        HOE,
        HELMET,
        CHESTPLATE,
        LEGGINGS,
        BOOTS,
        HORSE_ARMOR,
        NAUTILUS_ARMOR
    }

    public static class Group {

        final Set<Target> targets;

        public Group(Target... targets) {
            this.targets = Arrays.stream(targets).collect(Collectors.toSet());
        }

        public Set<Target> getTargets() {
            return targets;
        }

        public static Group ALL = new Group(Target.SWORD, Target.SPEAR, Target.AXE, Target.PICKAXE, Target.SHOVEL, Target.HOE, Target.HELMET, Target.CHESTPLATE, Target.LEGGINGS, Target.BOOTS, Target.HORSE_ARMOR, Target.NAUTILUS_ARMOR);
        public static Group TOOLS = new Group(Target.SWORD, Target.SPEAR, Target.AXE, Target.PICKAXE, Target.SHOVEL, Target.HOE);
        public static Group TOOLS_NO_SPEAR = new Group(Target.SWORD, Target.AXE, Target.PICKAXE, Target.SHOVEL, Target.HOE);
        public static Group ARMOR = new Group(Target.HELMET, Target.CHESTPLATE, Target.LEGGINGS, Target.BOOTS, Target.HORSE_ARMOR, Target.NAUTILUS_ARMOR);
        public static Group HUMANOID_ARMOR = new Group(Target.HELMET, Target.CHESTPLATE, Target.LEGGINGS, Target.BOOTS);
        public static Group ANIMAL_ARMOR = new Group(Target.HORSE_ARMOR, Target.NAUTILUS_ARMOR);
    }

    private final Settings settings;

    private void registerItems() {
        if (hasTools()) {
            sword = createSword();
            spear = createSpear();
            axe = createAxe();
            pickaxe = createPickaxe();
            shovel = createShovel();
            hoe = createHoe();
        }

        if (hasArmor()) {
            EquipmentClientInfo.Builder info = EquipmentClientInfo.builder();

            helmet = createHelmet();
            chestplate = createChestplate();
            leggings = createLeggings();
            boots = createBoots();
            if (settings.hasBabyArmorTextures) {
                info.addHumanoidLayers(id, settings.isDyeable);
            }
            else {
                info.addLayers(EquipmentClientInfo.LayerType.HUMANOID, EquipmentClientInfo.Layer.leatherDyeable(id, settings.isDyeable));
                info.addLayers(EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS, EquipmentClientInfo.Layer.leatherDyeable(id, settings.isDyeable));
                simpleBabyArmor.add("equipment_sets/" + id.getPath(), getEquipmentAsset());
            }

            if (hasAnimalArmor()) {
                horseArmor = createHorseArmor();
                nautilusArmor = createNautilusArmor();
                info.addLayers(EquipmentClientInfo.LayerType.HORSE_BODY, EquipmentClientInfo.Layer.leatherDyeable(id, settings.isDyeable));
                info.addLayers(EquipmentClientInfo.LayerType.NAUTILUS_BODY, EquipmentClientInfo.Layer.leatherDyeable(id, settings.isDyeable));
            }

            equipmentAssets.add(getEquipmentAsset(), info.build());
        }

        if (settings.craftingMaterial != null) {
            recipes.add(provider -> {
                TagKey<Item> material = settings.craftingMaterial;
                String hasName = "has_" + id.getPath();

                if (hasTools()) {
                    provider.shaped(RecipeCategory.TOOLS, getAxe()).define('#', Items.STICK).define('X', material).pattern("XX").pattern("X#").pattern(" #").unlockedBy(hasName, provider.has(material)).save(provider.output);
                    provider.shaped(RecipeCategory.TOOLS, getHoe()).define('#', Items.STICK).define('X', material).pattern("XX").pattern(" #").pattern(" #").unlockedBy(hasName, provider.has(material)).save(provider.output);
                    provider.shaped(RecipeCategory.TOOLS, getPickaxe()).define('#', Items.STICK).define('X', material).pattern("XXX").pattern(" # ").pattern(" # ").unlockedBy(hasName, provider.has(material)).save(provider.output);
                    provider.shaped(RecipeCategory.TOOLS, getShovel()).define('#', Items.STICK).define('X', material).pattern("X").pattern("#").pattern("#").unlockedBy(hasName, provider.has(material)).save(provider.output);
                    provider.shaped(RecipeCategory.COMBAT, getSword()).define('#', Items.STICK).define('X', material).pattern("X").pattern("X").pattern("#").unlockedBy(hasName, provider.has(material)).save(provider.output);
                    provider.shaped(RecipeCategory.COMBAT, getSpear()).define('#', Items.STICK).define('X', material).pattern("  X").pattern(" # ").pattern("#  ").unlockedBy(hasName, provider.has(material)).save(provider.output);
                }

                if (hasArmor()) {
                    provider.shaped(RecipeCategory.COMBAT, getBoots()).define('X', material).pattern("X X").pattern("X X").unlockedBy(hasName, provider.has(material)).save(provider.output);
                    provider.shaped(RecipeCategory.COMBAT, getChestplate()).define('X', material).pattern("X X").pattern("XXX").pattern("XXX").unlockedBy(hasName, provider.has(material)).save(provider.output);
                    provider.shaped(RecipeCategory.COMBAT, getHelmet()).define('X', material).pattern("XXX").pattern("X X").unlockedBy(hasName, provider.has(material)).save(provider.output);
                    provider.shaped(RecipeCategory.COMBAT, getLeggings()).define('X', material).pattern("XXX").pattern("X X").pattern("X X").unlockedBy(hasName, provider.has(material)).save(provider.output);
                }
            });
        }
        if (settings.smithingRecipes != null) {
            recipes.add(provider -> {
                smithing(provider, settings.smithingRecipes.sword, RecipeCategory.COMBAT, getSword());
                smithing(provider, settings.smithingRecipes.spear, RecipeCategory.COMBAT, getSpear());
                smithing(provider, settings.smithingRecipes.axe, RecipeCategory.TOOLS, getAxe());
                smithing(provider, settings.smithingRecipes.pickaxe, RecipeCategory.TOOLS, getPickaxe());
                smithing(provider, settings.smithingRecipes.shovel, RecipeCategory.TOOLS, getShovel());
                smithing(provider, settings.smithingRecipes.hoe, RecipeCategory.TOOLS, getHoe());

                smithing(provider, settings.smithingRecipes.helmet, RecipeCategory.COMBAT, getHelmet());
                smithing(provider, settings.smithingRecipes.chestplate, RecipeCategory.COMBAT, getChestplate());
                smithing(provider, settings.smithingRecipes.leggings, RecipeCategory.COMBAT, getLeggings());
                smithing(provider, settings.smithingRecipes.boots, RecipeCategory.COMBAT, getBoots());
                smithing(provider, settings.smithingRecipes.horseArmor, RecipeCategory.COMBAT, getHorseArmor());
                smithing(provider, settings.smithingRecipes.nautilusArmor, RecipeCategory.COMBAT, getNautilusArmor());
            });
        }

        settings.tags.forEach(pair -> {
            var currentTag = tags.create(pair.getSecond());
            for (Target target : pair.getFirst().getTargets()) {
                SuppliedItem item = getItemFromTarget(target);
                if (item != null) currentTag.add(item.key());
            }
        });
    }

    private void smithing(RecipeProvider provider, @Nullable Supplier<? extends ItemLike> base, RecipeCategory category, @Nullable ItemLike result) {
        if (base == null || result == null) return;
        SmithingTransformRecipeBuilder.smithing(Ingredient.of(settings.smithingRecipes.template.get()), Ingredient.of(base.get()), provider.tag(settings.smithingRecipes.material), category, result.asItem()).unlocks("has_" + id.getPath(), provider.has(settings.smithingRecipes.material)).save(provider.output, RecipeProvider.getItemName(result) + "_smithing");
    }

    public EquipmentSet(Identifier id, Settings settings, ItemGenerator items, RecipeGenerator recipes, TagGenerator tags, EquipmentAssetGenerator equipmentAssets, SimpleBabyArmorGenerator simpleBabyArmor, CreativeEntryGenerator creativeEntries) {
        this.settings = settings;
        this.id = id;
        this.items = items;
        this.recipes = recipes;
        this.tags = tags;
        this.equipmentAssets = equipmentAssets;
        this.simpleBabyArmor = simpleBabyArmor;
        registerItems();
        EQUIPMENT_SETS.add(this);
        EquipmentSetProperties.CREATIVE_ARMOR_ENTRIES.put(id, settings.precedingArmorCreativeEntries);
        EquipmentSetProperties.CREATIVE_TOOL_ENTRIES.put(id, settings.precedingToolCreativeEntries);
        EquipmentSetProperties.CREATIVE_ENTRY_GENERATORS.put(id, creativeEntries);
        if (UnifiedPlatform.getModLoader() == ModLoader.FABRIC) {
            UnifiedPlatform.executeAfter(Registries.ITEM, () -> EquipmentSetProperties.init(List.of(this)));
        }
    }

	private SuppliedItem register(String path, ExtensibleCodec.Entry<Function<Item.Properties, Item>> type, Consumer<ItemGenerator.Builder> builder) {
        SuppliedItem item = items.register(path, type, builder);
		registeredItems.add(item);
		return item;
	}

    public Settings getSettings() {
        return settings;
    }

    public Identifier getId() {
        return id;
    }

    public @Nullable SuppliedItem getSword() {
        return sword;
    }

    public @Nullable SuppliedItem getSpear() {
        return spear;
    }

    public @Nullable SuppliedItem getAxe() {
        return axe;
    }

    public @Nullable SuppliedItem getPickaxe() {
        return pickaxe;
    }

    public @Nullable SuppliedItem getShovel() {
        return shovel;
    }

    public @Nullable SuppliedItem getHoe() {
        return hoe;
    }

    public @Nullable SuppliedItem getHelmet() {
        return helmet;
    }

    public @Nullable SuppliedItem getChestplate() {
        return chestplate;
    }

    public @Nullable SuppliedItem getLeggings() {
        return leggings;
    }

    public @Nullable SuppliedItem getBoots() {
        return boots;
    }

    public @Nullable SuppliedItem getHorseArmor() {
        return horseArmor;
    }

    public @Nullable SuppliedItem getNautilusArmor() {
        return nautilusArmor;
    }

    public List<SuppliedItem> getRegisteredItems() {
        return registeredItems;
    }

    public @Nullable SuppliedItem getItemFromTarget(EquipmentSet.Target target) {
        return switch (target) {
            case SWORD -> this.getSword();
            case SPEAR -> this.getSpear();
            case AXE -> this.getAxe();
            case PICKAXE -> this.getPickaxe();
            case SHOVEL -> this.getShovel();
            case HOE -> this.getHoe();
            case HELMET -> this.getHelmet();
            case CHESTPLATE -> this.getChestplate();
            case LEGGINGS -> this.getLeggings();
            case BOOTS -> this.getBoots();
            case HORSE_ARMOR -> this.getHorseArmor();
            case NAUTILUS_ARMOR -> this.getNautilusArmor();
        };
    }

    @SuppressWarnings("unchecked")
    private <T, Y> void applyComponents(Item.Properties properties, Target target) {
        boolean replacesAttributes = false;

        for (var component : settings.components) {
            if (component.getLeft().getTargets().contains(target)) {
                DataComponentType<?> type = component.getMiddle().get();
                properties.component((DataComponentType<T>) type, (T) component.getRight());
                if (type == DataComponents.ATTRIBUTE_MODIFIERS) replacesAttributes = true;
            }
        }
        for (var component : settings.providedComponents) {
            if (component.getLeft().getTargets().contains(target)) {
                properties.delayedComponent((DataComponentType<T>) component.getMiddle().get(), (DataComponentInitializers.SingleComponentInitializer<T>) component.getRight());
            }
        }
        for (var component : settings.keyedComponents) {
            if (component.getLeft().getTargets().contains(target)) {
                properties.delayedHolderComponent((DataComponentType<Holder<Y>>) component.getMiddle().get(), (ResourceKey<Y>) component.getRight());
            }
        }

        if (!replacesAttributes) {
            List<ItemAttributeModifiers.Entry> modifiers = settings.attributes.stream()
                    .filter(entry -> entry.getFirst().getTargets().contains(target))
                    .map(Pair::getSecond)
                    .toList();
            if (modifiers.isEmpty()) return;

            DataComponentInitializers.Initializer<Item> original = properties.componentInitializer;
            properties.componentInitializer = (components, provider, key) -> {
                DataComponentMap.Builder originalComponents = DataComponentMap.builder();
                original.run(originalComponents, provider, key);
                DataComponentMap builtComponents = originalComponents.build();
                components.addAll(builtComponents);

                ItemAttributeModifiers.Builder attributes = ItemAttributeModifiers.builder();
                Set<Holder<Attribute>> replacedAttributes = new HashSet<>();
                for (ItemAttributeModifiers.Entry modifier : modifiers) {
                    attributes.add(modifier.attribute(), modifier.modifier(), modifier.slot(), modifier.display());
                    replacedAttributes.add(modifier.attribute());
                }

                ItemAttributeModifiers originalAttributes = builtComponents.get(DataComponents.ATTRIBUTE_MODIFIERS);
                if (originalAttributes != null) {
                    for (ItemAttributeModifiers.Entry modifier : originalAttributes.modifiers()) {
                        if (!replacedAttributes.contains(modifier.attribute())) {
                            attributes.add(modifier.attribute(), modifier.modifier(), modifier.slot(), modifier.display());
                        }
                    }
                }
                components.set(DataComponents.ATTRIBUTE_MODIFIERS, attributes.build());
            };
        }
    }

    private SuppliedItem createSword() {
        return register(
                this.getId().getPath() + "_sword",
                VanillaItemCodecs.ITEM.create(),
                builder -> builder
                        .properties(properties -> {
                            properties.sword(getToolMaterial(), 3F, -2.4F);
                            applyComponents(properties, Target.SWORD);
                        })
                        .assets(ItemGenerator.Assets::handheld)
                        .data(data -> data
                                .tag(ItemTags.SWORDS)
                        )
        );
    }

    private SuppliedItem createSpear() {
        return register(
                this.getId().getPath() + "_spear",
                VanillaItemCodecs.ITEM.create(),
                builder -> builder
                        .properties(properties -> {
                            properties.spear(getToolMaterial(), getSettings().spearProperties.attackDuration, getSettings().spearProperties.damageMultiplier, getSettings().spearProperties.delay, getSettings().spearProperties.dismountTime, getSettings().spearProperties.dismountThreshold, getSettings().spearProperties.knockbackTime, getSettings().spearProperties.knockbackThreshold, getSettings().spearProperties.damageTime, getSettings().spearProperties.damageThreshold);
                            applyComponents(properties, Target.SPEAR);
                        })
                        .assets(
                                assets -> assets.model(ItemAssets.SPEAR)
                        )
                        .data(data -> data
                                .tag(ItemTags.SPEARS)
                        )
        );
    }

    private SuppliedItem createAxe() {
        return register(
                this.getId().getPath() + "_axe",
                VanillaItemCodecs.AXE.create(() -> new VanillaItemCodecs.Tool(getToolMaterial(), 5F, -4F + getSettings().axeSwingSpeed)),
                builder -> builder
                        .properties(properties -> {
                            properties.axe(getToolMaterial(), 5F, -4F + getSettings().axeSwingSpeed);
                            applyComponents(properties, Target.AXE);
                        })
                        .assets(ItemGenerator.Assets::handheld)
                        .data(data -> data
                                .tag(ItemTags.AXES)
                        )
        );
    }

    private SuppliedItem createPickaxe() {
        return register(
                this.getId().getPath() + "_pickaxe",
                VanillaItemCodecs.ITEM.create(),
                builder -> builder
                        .properties(properties -> {
                            properties.pickaxe(getToolMaterial(), 1F, -2.8F);
                            applyComponents(properties, Target.PICKAXE);
                        })
                        .assets(ItemGenerator.Assets::handheld)
                        .data(data -> data
                                .tag(ItemTags.PICKAXES)
                        )
        );
    }

    private SuppliedItem createShovel() {
        return register(
                this.getId().getPath() + "_shovel",
                VanillaItemCodecs.SHOVEL.create(() -> new VanillaItemCodecs.Tool(getToolMaterial(), 1.5F, -3F)),
                builder -> builder
                        .properties(properties -> {
                            properties.shovel(getToolMaterial(), 1.5F, -3F);
                            applyComponents(properties, Target.SHOVEL);
                        })
                        .assets(ItemGenerator.Assets::handheld)
                        .data(data -> data
                                .tag(ItemTags.SHOVELS)
                        )
        );
    }

    private SuppliedItem createHoe() {
        return register(
                this.getId().getPath() + "_hoe",
                VanillaItemCodecs.HOE.create(() -> new VanillaItemCodecs.Tool(getToolMaterial(), -4F, 0F)),
                builder -> builder
                        .properties(properties -> {
                            properties.hoe(getToolMaterial(), -4F, 0F);
                            applyComponents(properties, Target.HOE);
                        })
                        .assets(ItemGenerator.Assets::handheld)
                        .data(data -> data
                                .tag(ItemTags.HOES)
                        )
        );
    }

    private SuppliedItem createHelmet() {
        return register(
                this.getId().getPath() + "_helmet",
                VanillaItemCodecs.ITEM.create(),
                builder -> builder
                        .properties(properties -> {
                            properties.humanoidArmor(getArmorMaterial(), ArmorType.HELMET);
                            applyComponents(properties, Target.HELMET);
                        })
                        .assets(ItemGenerator.Assets::generated)
                        .data(data -> data
                                .tag(ItemTags.HEAD_ARMOR)
                        )
        );
    }

    private SuppliedItem createChestplate() {
        return register(
                this.getId().getPath() + "_chestplate",
                VanillaItemCodecs.ITEM.create(),
                builder -> builder
                        .properties(properties -> {
                            properties.humanoidArmor(getArmorMaterial(), ArmorType.CHESTPLATE);
                            applyComponents(properties, Target.CHESTPLATE);
                        })
                        .assets(ItemGenerator.Assets::generated)
                        .data(data -> data
                                .tag(ItemTags.CHEST_ARMOR)
                        )
        );
    }

    private SuppliedItem createLeggings() {
        return register(
                this.getId().getPath() + "_leggings",
                VanillaItemCodecs.ITEM.create(),
                builder -> builder
                        .properties(properties -> {
                            properties.humanoidArmor(getArmorMaterial(), ArmorType.LEGGINGS);
                            applyComponents(properties, Target.LEGGINGS);
                        })
                        .assets(ItemGenerator.Assets::generated)
                        .data(data -> data
                                .tag(ItemTags.LEG_ARMOR)
                        )
        );
    }

    private SuppliedItem createBoots() {
        return register(
                this.getId().getPath() + "_boots",
                VanillaItemCodecs.ITEM.create(),
                builder -> builder
                        .properties(properties -> {
                            properties.humanoidArmor(getArmorMaterial(), ArmorType.BOOTS);
                            applyComponents(properties, Target.BOOTS);
                        })
                        .assets(ItemGenerator.Assets::generated)
                        .data(data -> data
                                .tag(ItemTags.FOOT_ARMOR)
                        )
        );
    }

    private SuppliedItem createHorseArmor() {
        return register(
                this.getId().getPath() + "_horse_armor",
                VanillaItemCodecs.ITEM.create(),
                builder -> builder
                        .properties(properties -> {
                            properties.horseArmor(getArmorMaterial());
                            applyComponents(properties, Target.HORSE_ARMOR);
                        })
                        .assets(ItemGenerator.Assets::generated)
        );
    }

    private SuppliedItem createNautilusArmor() {
        return register(
                this.getId().getPath() + "_nautilus_armor",
                VanillaItemCodecs.ITEM.create(),
                builder -> builder
                        .properties(properties -> {
                            properties.nautilusArmor(getArmorMaterial());
                            applyComponents(properties, Target.NAUTILUS_ARMOR);
                        })
                        .assets(ItemGenerator.Assets::generated)
        );
    }

    public boolean hasTools() {
        return this.getSettings().hasTools;
    }
    public boolean hasArmor() {
        return this.getSettings().hasArmor;
    }
    public boolean hasAnimalArmor() {
        return this.getSettings().hasAnimalArmor;
    }

    public ToolMaterial getToolMaterial() {
        if (this.toolMaterial == null) this.toolMaterial = new ToolMaterial(
                this.settings.incorrectBlocksForDrops,
                this.settings.toolDurability,
                this.settings.miningSpeed,
                this.settings.damageBonus,
                this.settings.toolEnchantingPower,
                this.settings.repairMaterials
        );
        return this.toolMaterial;
    }

    public ArmorMaterial getArmorMaterial() {
        if (this.armorMaterial == null) this.armorMaterial = new ArmorMaterial(
                this.settings.armorDurabilityFactor,
                this.settings.armorDefense,
                this.settings.armorEnchantingPower,
                this.settings.armorEquipSound,
                this.settings.armorToughness,
                this.settings.knockbackResistance,
                this.settings.repairMaterials,
                this.equipmentAsset
        );
        return this.armorMaterial;
    }

    public ResourceKey<EquipmentAsset> getEquipmentAsset() {
        if (this.equipmentAsset == null) this.equipmentAsset = ResourceKey.create(ResourceKey.createRegistryKey(Identifier.withDefaultNamespace("equipment_asset")), id);
        return this.equipmentAsset;
    }

    public static class Settings implements Cloneable {

        private boolean hasTools = false;
        private boolean hasArmor = false;
        private boolean hasAnimalArmor = false;

        private boolean isDyeable = false;
        private boolean hasBabyArmorTextures = true;

        private float damageBonus = ToolMaterial.NETHERITE.attackDamageBonus();
        private float miningSpeed = ToolMaterial.NETHERITE.speed();
        private float axeSwingSpeed = 1F;
        private int toolDurability = ToolMaterial.NETHERITE.durability();
        private int toolEnchantingPower = ToolMaterial.NETHERITE.enchantmentValue();
        private TagKey<Block> incorrectBlocksForDrops = BlockTags.INCORRECT_FOR_NETHERITE_TOOL;
        private SpearProperties spearProperties = new SpearProperties(1.15F, 1.2F, 0.4F, 2.5F, 9.0F, 5.5F, 5.1F, 8.75F, 4.6F);

        private int armorDurabilityFactor = ArmorMaterials.NETHERITE.durability();
        private int armorEnchantingPower = ArmorMaterials.NETHERITE.enchantmentValue();
        private Map<ArmorType, Integer> armorDefense = ArmorMaterials.NETHERITE.defense();
        private float armorToughness = ArmorMaterials.NETHERITE.toughness();
        private float knockbackResistance = 0F;
        private Holder<SoundEvent> armorEquipSound = SoundEvents.ARMOR_EQUIP_GENERIC;

        private @Nullable TagKey<Item> repairMaterials = null;

        private @Nullable PrecedingToolCreativeEntries precedingToolCreativeEntries = null;
        private @Nullable PrecedingArmorCreativeEntries precedingArmorCreativeEntries = null;

        private @Nullable TagKey<Item> craftingMaterial = null;
        private @Nullable SmithingRecipes smithingRecipes = null;

        private List<Triple<Group, Supplier<? extends DataComponentType<?>>, ?>> components = new ArrayList<>();
        private List<Triple<Group, Supplier<? extends DataComponentType<?>>, DataComponentInitializers.SingleComponentInitializer<?>>> providedComponents = new ArrayList<>();
        private List<Triple<Group, Supplier<? extends DataComponentType<?>>, ResourceKey<?>>> keyedComponents = new ArrayList<>();
        private List<Pair<Group, ItemAttributeModifiers.Entry>> attributes = new ArrayList<>();

        private List<Pair<Group, TagKey<Item>>> tags = new ArrayList<>();

        Settings() {}

        public float getDamageBonus() {
            return damageBonus;
        }

        public float getMiningSpeed() {
            return miningSpeed;
        }

        public float getAxeSwingSpeed() {
            return axeSwingSpeed;
        }

        public int getToolDurability() {
            return toolDurability;
        }

        public int getToolEnchantingPower() {
            return toolEnchantingPower;
        }

        public TagKey<Block> getIncorrectBlocksForDrops() {
            return incorrectBlocksForDrops;
        }

        public SpearProperties getSpearProperties() {
            return spearProperties;
        }

        public int getArmorDurabilityFactor() {
            return armorDurabilityFactor;
        }

        public int getArmorEnchantingPower() {
            return armorEnchantingPower;
        }

        public Map<ArmorType, Integer> getArmorDefense() {
            return armorDefense;
        }

        public float getArmorToughness() {
            return armorToughness;
        }

        public float getKnockbackResistance() {
            return knockbackResistance;
        }

        public Holder<SoundEvent> getArmorEquipSound() {
            return armorEquipSound;
        }

        public @Nullable TagKey<Item> getRepairMaterials() {
            return repairMaterials;
        }

        public boolean isDyeable() {
            return isDyeable;
        }

        public boolean usesSimpleBabyArmor() {
            return !hasBabyArmorTextures;
        }

        Settings copy() {
            try {
                return (Settings) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new AssertionError(e);
            }
        }
    }

    public static class RegistryBuilder extends Builder<RegistryBuilder> {

        private final Identifier id;

        private final ItemGenerator items;
        private final RecipeGenerator recipes;
        private final TagGenerator tags;
        private final EquipmentAssetGenerator equipmentAssets;
        private final SimpleBabyArmorGenerator simpleBabyArmor;
        private final CreativeEntryGenerator creativeEntries;

        public RegistryBuilder createTools() {
            settings.hasTools = true;
            return self();
        }
        public RegistryBuilder createArmor(boolean hasAnimalArmor) {
            settings.hasArmor = true;
            settings.hasAnimalArmor = hasAnimalArmor;
            return self();
        }

        public EquipmentSet build() {
            return new EquipmentSet(id, settings, items, recipes, tags, equipmentAssets, simpleBabyArmor, creativeEntries);
        }

        public RegistryBuilder(Identifier id, EquipmentPreset preset, ItemGenerator items, RecipeGenerator recipes, TagGenerator tags, EquipmentAssetGenerator equipmentAssets, SimpleBabyArmorGenerator simpleBabyArmor, CreativeEntryGenerator creativeEntries) {
            super(preset.settings.copy());

            this.id = id;
            this.items = items;
            this.recipes = recipes;
            this.tags = tags;
            this.equipmentAssets = equipmentAssets;
            this.simpleBabyArmor = simpleBabyArmor;
            this.creativeEntries = creativeEntries;
        }
    }

    public static class PresetBuilder extends EquipmentSet.Builder<PresetBuilder> {

        public PresetBuilder() {
            super();
        }

        public PresetBuilder(Settings settings) {
            super(settings);
        }

        public EquipmentPreset build() {
            return new EquipmentPreset(settings.copy());
        }
    }

    public static class Builder<T extends Builder<T>> {

        protected final Settings settings;

        public Builder() {
            this.settings = new Settings();
        }

        protected Builder(Settings settings) {
            this.settings = settings;
        }

        @SuppressWarnings("unchecked")
        protected T self() {
            return (T) this;
        }

        public T creativeArmorPlacement(Supplier<? extends ItemLike> precedingCombatArmor) {
            settings.precedingArmorCreativeEntries = new PrecedingArmorCreativeEntries(precedingCombatArmor, null, null);
            return self();
        }
        public T creativeArmorPlacement(Supplier<? extends ItemLike> precedingCombatArmor, Supplier<? extends ItemLike> precedingCombatHorseArmor, Supplier<? extends ItemLike> precedingCombatNautilusArmor) {
            settings.precedingArmorCreativeEntries = new PrecedingArmorCreativeEntries(precedingCombatArmor, precedingCombatHorseArmor, precedingCombatNautilusArmor);
            return self();
        }
        public T creativeToolPlacement(Supplier<? extends ItemLike> precedingUtilitiesItem, Supplier<? extends ItemLike> precedingCombatSword, Supplier<? extends ItemLike> precedingCombatSpear, Supplier<? extends ItemLike> precedingCombatAxe) {
            settings.precedingToolCreativeEntries = new PrecedingToolCreativeEntries(precedingUtilitiesItem, precedingCombatSword, precedingCombatSpear, precedingCombatAxe);
            return self();
        }

        public T setDamageBonus(float damageBonus) {
            settings.damageBonus = damageBonus;
            return self();
        }
        public T setMiningSpeed(float miningSpeed) {
            settings.miningSpeed = miningSpeed;
            return self();
        }
        public T setAxeSwingSpeed(float axeSwingSpeed) {
            settings.axeSwingSpeed = axeSwingSpeed;
            return self();
        }
        public T setToolDurability(int durability) {
            settings.toolDurability = durability;
            return self();
        }
        public T setIncorrectBlocksForDrops(TagKey<Block> incorrectBlocksForDrops) {
            settings.incorrectBlocksForDrops = incorrectBlocksForDrops;
            return self();
        }
        public T setSpearProperties(float attackDuration, float damageMultiplier, float delay, float dismountTime, float dismountThreshold, float knockbackTime, float knockbackThreshold, float damageTime, float damageThreshold) {
            settings.spearProperties = new SpearProperties(attackDuration, damageMultiplier, delay, dismountTime, dismountThreshold, knockbackTime, knockbackThreshold, damageTime, damageThreshold);
            return self();
        }

        public T setArmorDurabilityFactor(int durabilityFactor) {
            settings.armorDurabilityFactor = durabilityFactor;
            return self();
        }
        public T setArmorDefense(int helmet, int chestplate, int leggings, int boots) {
            settings.armorDefense = Maps.newEnumMap(Map.of(ArmorType.HELMET, helmet, ArmorType.CHESTPLATE, chestplate, ArmorType.LEGGINGS, leggings, ArmorType.BOOTS, boots, ArmorType.BODY, settings.armorDefense.get(ArmorType.BODY)));
            return self();
        }
        public T setArmorDefense(int helmet, int chestplate, int leggings, int boots, int animal) {
            settings.armorDefense = Maps.newEnumMap(Map.of(ArmorType.HELMET, helmet, ArmorType.CHESTPLATE, chestplate, ArmorType.LEGGINGS, leggings, ArmorType.BOOTS, boots, ArmorType.BODY, animal));
            return self();
        }
        public T setArmorToughness(float toughness) {
            settings.armorToughness = toughness;
            return self();
        }
        public T setKnockbackResistance(float knockbackResistance) {
            settings.knockbackResistance = knockbackResistance;
            return self();
        }
        public T setArmorEquipSound(Holder<SoundEvent> equipSound) {
            settings.armorEquipSound = equipSound;
            return self();
        }

        public T setEnchantingPower(int enchantingPower) {
            settings.toolEnchantingPower = enchantingPower;
            settings.armorEnchantingPower = enchantingPower;
            return self();
        }
        public T setEnchantingPower(int tools, int armor) {
            settings.toolEnchantingPower = tools;
            settings.armorEnchantingPower = armor;
            return self();
        }
        public T setRepairMaterials(@Nullable TagKey<Item> repairMaterials) {
            settings.repairMaterials = repairMaterials;
            return self();
        }

        public <Y> T setComponent(Target target, Supplier<DataComponentType<Y>> type, Y value) {
            return setComponent(new Group(target), type, value);
        }
        public <Y> T setComponent(Group target, Supplier<DataComponentType<Y>> type, Y value) {
            var components = settings.components;
            components.add(Triple.of(target, type, value));
            settings.components = components;
            return self();
        }
        public <Y> T setComponentWithProvider(Target target, Supplier<DataComponentType<Y>> type, DataComponentInitializers.SingleComponentInitializer<Y> initializer) {
            return setComponentWithProvider(new Group(target), type, initializer);
        }
        public <Y> T setComponentWithProvider(Group target, Supplier<DataComponentType<Y>> type, DataComponentInitializers.SingleComponentInitializer<Y> initializer) {
            var providedComponents = settings.providedComponents;
            providedComponents.add(Triple.of(target, type, initializer));
            settings.providedComponents = providedComponents;
            return self();
        }
        public <Y> T setComponentWithKey(Target target, Supplier<DataComponentType<Holder<Y>>> type, ResourceKey<Y> valueKey) {
            return setComponentWithKey(new Group(target), type, valueKey);
        }
        public <Y> T setComponentWithKey(Group target, Supplier<DataComponentType<Holder<Y>>> type, ResourceKey<Y> valueKey) {
            var keyedComponents = settings.keyedComponents;
            keyedComponents.add(Triple.of(target, type, valueKey));
            settings.keyedComponents = keyedComponents;
            return self();
        }

        public T setAttribute(Target target, ItemAttributeModifiers.Entry attribute) {
            return setAttribute(new Group(target), attribute);
        }
        public T setAttribute(Group target, ItemAttributeModifiers.Entry attribute) {
            var attributes = settings.attributes;
            attributes.add(Pair.of(target, attribute));
            settings.attributes = attributes;
            return self();
        }

        public T craftingRecipe(TagKey<Item> material) {
            settings.craftingMaterial = material;
            return self();
        }

        public T smithingRecipeToolsOnly(
                Supplier<? extends ItemLike> template,
                TagKey<Item> material,
                Supplier<? extends ItemLike> baseSword,
                Supplier<? extends ItemLike> baseSpear,
                Supplier<? extends ItemLike> baseAxe,
                Supplier<? extends ItemLike> basePickaxe,
                Supplier<? extends ItemLike> baseShovel,
                Supplier<? extends ItemLike> baseHoe
        ) {
            return smithingRecipes(template, material, baseSword, baseSpear, baseAxe, basePickaxe, baseShovel, baseHoe, null, null, null, null, null, null);
        }

        public T smithingRecipeArmorOnly(
                Supplier<? extends ItemLike> template,
                TagKey<Item> material,
                Supplier<? extends ItemLike> baseHelmet,
                Supplier<? extends ItemLike> baseChestplate,
                Supplier<? extends ItemLike> baseLeggings,
                Supplier<? extends ItemLike> baseBoots
        ) {
            return smithingRecipeArmorOnly(template, material, baseHelmet, baseChestplate, baseLeggings, baseBoots, null, null);
        }

        public T smithingRecipeArmorOnly(
                Supplier<? extends ItemLike> template,
                TagKey<Item> material,
                Supplier<? extends ItemLike> baseHelmet,
                Supplier<? extends ItemLike> baseChestplate,
                Supplier<? extends ItemLike> baseLeggings,
                Supplier<? extends ItemLike> baseBoots,
                Supplier<? extends ItemLike> baseHorseArmor,
                Supplier<? extends ItemLike> baseNautilusArmor
        ) {
            return smithingRecipes(template, material, null, null, null, null, null, null, baseHelmet, baseChestplate, baseLeggings, baseBoots, baseHorseArmor, baseNautilusArmor);
        }

        public T smithingRecipes(Supplier<? extends ItemLike> template, TagKey<Item> material, EquipmentSet baseSet) {
            settings.smithingRecipes = new SmithingRecipes(
                    template,
                    material,
                    baseSet::getSword,
                    baseSet::getSpear,
                    baseSet::getAxe,
                    baseSet::getPickaxe,
                    baseSet::getShovel,
                    baseSet::getHoe,
                    baseSet::getHelmet,
                    baseSet::getChestplate,
                    baseSet::getLeggings,
                    baseSet::getBoots,
                    baseSet::getHorseArmor,
                    baseSet::getNautilusArmor
            );
            return self();
        }

        public T smithingRecipes(
                Supplier<? extends ItemLike> template,
                TagKey<Item> material,
                Supplier<? extends ItemLike> baseSword,
                Supplier<? extends ItemLike> baseSpear,
                Supplier<? extends ItemLike> baseAxe,
                Supplier<? extends ItemLike> basePickaxe,
                Supplier<? extends ItemLike> baseShovel,
                Supplier<? extends ItemLike> baseHoe,
                Supplier<? extends ItemLike> baseHelmet,
                Supplier<? extends ItemLike> baseChestplate,
                Supplier<? extends ItemLike> baseLeggings,
                Supplier<? extends ItemLike> baseBoots,
                Supplier<? extends ItemLike> baseHorseArmor,
                Supplier<? extends ItemLike> baseNautilusArmor
        ) {
            settings.smithingRecipes = new SmithingRecipes(template, material, baseSword, baseSpear, baseAxe, basePickaxe, baseShovel, baseHoe, baseHelmet, baseChestplate, baseLeggings, baseBoots, baseHorseArmor, baseNautilusArmor);
            return self();
        }

        public T addTag(Target target, TagKey<Item> tag) {
            return addTag(new Group(target), tag);
        }
        public T addTag(Group target, TagKey<Item> tag) {
            var tags = settings.tags;
            tags.add(Pair.of(target, tag));
            settings.tags = tags;
            return self();
        }

        public T isDyeable(boolean isDyeable) {
            settings.isDyeable = isDyeable;
            return self();
        }

        public T useSimpleBabyArmor(boolean useSimpleBabyArmor) {
            settings.hasBabyArmorTextures = !useSimpleBabyArmor;
            return self();
        }
    }

    public record SpearProperties(float attackDuration, float damageMultiplier, float delay, float dismountTime, float dismountThreshold, float knockbackTime, float knockbackThreshold, float damageTime, float damageThreshold) {}

    public record PrecedingToolCreativeEntries(Supplier<? extends ItemLike> utilities, Supplier<? extends ItemLike> combatSword, Supplier<? extends ItemLike> combatSpear, Supplier<? extends ItemLike> combatAxe) {}
    public record PrecedingArmorCreativeEntries(Supplier<? extends ItemLike> combatArmor, @Nullable Supplier<? extends ItemLike> combatHorseArmor, @Nullable Supplier<? extends ItemLike> combatNautilusArmor) {}

    public record SmithingRecipes(
            Supplier<? extends ItemLike> template,
            TagKey<Item> material,
            @Nullable Supplier<? extends ItemLike> sword,
            @Nullable Supplier<? extends ItemLike> spear,
            @Nullable Supplier<? extends ItemLike> axe,
            @Nullable Supplier<? extends ItemLike> pickaxe,
            @Nullable Supplier<? extends ItemLike> shovel,
            @Nullable Supplier<? extends ItemLike> hoe,
            @Nullable Supplier<? extends ItemLike> helmet,
            @Nullable Supplier<? extends ItemLike> chestplate,
            @Nullable Supplier<? extends ItemLike> leggings,
            @Nullable Supplier<? extends ItemLike> boots,
            @Nullable Supplier<? extends ItemLike> horseArmor,
            @Nullable Supplier<? extends ItemLike> nautilusArmor
    ) {}
}
