package net.rebel459.unified.api.builder;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.rebel459.unified.api.core.SuppliedItem;
import net.rebel459.unified.api.core.UnifiedInstance;
import net.rebel459.unified.api.core.UnifiedRegistries;
import net.rebel459.unified.api.platform.ModLoader;
import net.rebel459.unified.impl.builder.EquipmentSetProperties;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class EquipmentSet {

    public static final List<EquipmentSet> EQUIPMENT_SETS = new CopyOnWriteArrayList<>();

    private final List<SuppliedItem> registeredItems = new ArrayList<>();

    private @Nullable ToolMaterial toolMaterial = null;
    private @Nullable ArmorMaterial armorMaterial = null;

    private final Identifier id;

    private final UnifiedRegistries.Items itemRegistry;

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
            helmet = createHelmet();
            chestplate = createChestplate();
            leggings = createLeggings();
            boots = createBoots();
        }
        if (hasAnimalArmor()) {
            horseArmor = createHorseArmor();
            nautilusArmor = createNautilusArmor();
        }
    }

    public EquipmentSet(Identifier id, Settings settings, UnifiedRegistries.Items itemRegistry) {
        this.settings = settings;
        this.id = id;
        this.itemRegistry = itemRegistry;
        registerItems();
        EQUIPMENT_SETS.add(this);
        EquipmentSetProperties.EQUIPMENT_COMPONENTS.put(id, settings.components);
        EquipmentSetProperties.EQUIPMENT_PROVIDED_COMPONENTS.put(id, settings.providedComponents);
        EquipmentSetProperties.EQUIPMENT_KEYED_COMPONENTS.put(id, settings.keyedComponents);
        EquipmentSetProperties.EQUIPMENT_ATTRIBUTES.put(id, settings.attributes);
        EquipmentSetProperties.CREATIVE_ARMOR_ENTRIES.put(id, settings.precedingArmorCreativeEntries);
        EquipmentSetProperties.CREATIVE_TOOL_ENTRIES.put(id, settings.precedingToolCreativeEntries);
        if (UnifiedInstance.getModLoader() == ModLoader.FABRIC) EquipmentSetProperties.init(List.of(this));
    }

    private SuppliedItem createItem(String path, Supplier<Item.Properties> settings){
        return createItem(path, Item::new, settings);
    }
	private SuppliedItem createItem(String path, Function<Item.Properties, Item> function, Supplier<Item.Properties> properties){
		SuppliedItem item = itemRegistry.register(path, function, properties);
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

    private SuppliedItem createSword() {
        return createItem(
                this.getId().getPath() + "_sword",
                () -> new Item.Properties()
                        .sword(getToolMaterial(), 3F, -2.4F)
        );
    }

    private SuppliedItem createSpear() {
        return createItem(
                this.getId().getPath() + "_spear",
                () -> new Item.Properties()
                        .spear(getToolMaterial(), getSettings().spearProperties.attackDuration, getSettings().spearProperties.damageMultiplier, getSettings().spearProperties.delay, getSettings().spearProperties.dismountTime, getSettings().spearProperties.dismountThreshold, getSettings().spearProperties.knockbackTime, getSettings().spearProperties.knockbackThreshold, getSettings().spearProperties.damageTime, getSettings().spearProperties.damageThreshold)
        );
    }

    private SuppliedItem createAxe() {
        return createItem(
                this.getId().getPath() + "_axe",
                Item::new,
                () -> new Item.Properties()
                        .axe(getToolMaterial(), 5F, -4F + getSettings().axeSwingSpeed)
        );
    }

    private SuppliedItem createPickaxe() {
        return createItem(
                this.getId().getPath() + "_pickaxe",
                () -> new Item.Properties()
                        .pickaxe(getToolMaterial(), 1F, -2.8F)
        );
    }

    private SuppliedItem createShovel() {
        return createItem(
                this.getId().getPath() + "_shovel",
                Item::new,
                () -> new Item.Properties()
                        .shovel(getToolMaterial(), 1.5F, -3F)
        );
    }

    private SuppliedItem createHoe() {
        return createItem(
                this.getId().getPath() + "_hoe",
                Item::new,
                () -> new Item.Properties()
                        .hoe(getToolMaterial(), -4F, 0F)
        );
    }

    private SuppliedItem createHelmet() {
        return createItem(
                this.getId().getPath() + "_helmet",
                () -> new Item.Properties()
                        .humanoidArmor(getArmorMaterial(), ArmorType.HELMET)
        );
    }

    private SuppliedItem createChestplate() {
        return createItem(
                this.getId().getPath() + "_chestplate",
                () -> new Item.Properties()
                        .humanoidArmor(getArmorMaterial(), ArmorType.CHESTPLATE)
        );
    }

    private SuppliedItem createLeggings() {
        return createItem(
                this.getId().getPath() + "_leggings",
                () -> new Item.Properties()
                        .humanoidArmor(getArmorMaterial(), ArmorType.LEGGINGS)
        );
    }

    private SuppliedItem createBoots() {
        return createItem(
                this.getId().getPath() + "_boots",
                () -> new Item.Properties()
                        .humanoidArmor(getArmorMaterial(), ArmorType.BOOTS)
        );
    }

    private SuppliedItem createHorseArmor() {
        return createItem(
                this.getId().getPath() + "_horse_armor",
                () -> new Item.Properties()
                        .horseArmor(getArmorMaterial())
        );
    }

    private SuppliedItem createNautilusArmor() {
        return createItem(
                this.getId().getPath() + "_nautilus_armor",
                () -> new Item.Properties()
                        .nautilusArmor(getArmorMaterial())
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
                this.settings.armorAsset
        );
        return this.armorMaterial;
    }

    public static class Settings implements Cloneable {

        private boolean hasTools = false;
        private boolean hasArmor = false;
        private boolean hasAnimalArmor = false;

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
        private ResourceKey<EquipmentAsset> armorAsset = null;

        private @Nullable TagKey<Item> repairMaterials = null;

        private @Nullable PrecedingToolCreativeEntries precedingToolCreativeEntries = null;
        private @Nullable PrecedingArmorCreativeEntries precedingArmorCreativeEntries = null;

        private List<Triple<Group, Supplier<? extends DataComponentType<?>>, ?>> components = new ArrayList<>();
        private List<Triple<Group, Supplier<? extends DataComponentType<?>>, DataComponentInitializers.SingleComponentInitializer<?>>> providedComponents = new ArrayList<>();
        private List<Triple<Group, Supplier<? extends DataComponentType<?>>, ResourceKey<?>>> keyedComponents = new ArrayList<>();
        private List<Pair<Group, ItemAttributeModifiers.Entry>> attributes = new ArrayList<>();

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

        public ResourceKey<EquipmentAsset> getArmorAsset() {
            return armorAsset;
        }

        public @Nullable TagKey<Item> getRepairMaterials() {
            return repairMaterials;
        }

        public Settings copy() {
            try {
                return (Settings) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new AssertionError(e);
            }
        }
    }

    public static class RegistryBuilder extends Builder<RegistryBuilder> {

        private final Identifier id;

        private final UnifiedRegistries.Items itemRegistry;

        public RegistryBuilder createTools() {
            settings.hasTools = true;
            return self();
        }
        public RegistryBuilder createArmor(ResourceKey<EquipmentAsset> armorAsset, boolean hasAnimalArmor) {
            settings.armorAsset = armorAsset;
            settings.hasArmor = true;
            settings.hasAnimalArmor = hasAnimalArmor;
            return self();
        }

        public EquipmentSet build() {
            return new EquipmentSet(id, settings, itemRegistry);
        }

        public RegistryBuilder(Identifier id, EquipmentPreset preset, UnifiedRegistries.Items itemRegistry) {
            super(preset.settings.copy());

            this.id = id;
            this.itemRegistry = itemRegistry;
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
    }

    public record SpearProperties(float attackDuration, float damageMultiplier, float delay, float dismountTime, float dismountThreshold, float knockbackTime, float knockbackThreshold, float damageTime, float damageThreshold) {}

    public record PrecedingToolCreativeEntries(Supplier<? extends ItemLike> utilities, Supplier<? extends ItemLike> combatSword, Supplier<? extends ItemLike> combatSpear, Supplier<? extends ItemLike> combatAxe) {}
    public record PrecedingArmorCreativeEntries(Supplier<? extends ItemLike> combatArmor, @Nullable Supplier<? extends ItemLike> combatHorseArmor, @Nullable Supplier<? extends ItemLike> combatNautilusArmor) {}
}