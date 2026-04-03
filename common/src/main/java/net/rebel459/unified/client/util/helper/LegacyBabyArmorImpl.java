package net.rebel459.unified.client.util.helper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.rebel459.unified.Unified;
import net.rebel459.unified.platform.client.UnifiedClientHelpers;

import java.lang.reflect.Constructor;
import java.util.*;

public final class LegacyBabyArmorImpl {

    private static final Identifier ID = Identifier.fromNamespaceAndPath(Unified.MOD_ID, "legacy_baby_armor");

    public static List<ResourceKey<EquipmentAsset>> LEGACY_BABY_ARMOR_EQUIPMENT = new ArrayList<>();

    public static final ArmorModelSet<ModelLayerLocation> LEGACY_BABY_ARMOR = new ArmorModelSet<>(
            new ModelLayerLocation(ID, "helmet"),
            new ModelLayerLocation(ID, "chestplate"),
            new ModelLayerLocation(ID, "leggings"),
            new ModelLayerLocation(ID, "boots")
    );

    public static void init() {
        UnifiedClientHelpers.ENTITY_RENDERERS.addLayerDefinition(LEGACY_BABY_ARMOR.head(), LegacyBabyArmorImpl::headLayer);
        UnifiedClientHelpers.ENTITY_RENDERERS.addLayerDefinition(LEGACY_BABY_ARMOR.chest(), LegacyBabyArmorImpl::chestLayer);
        UnifiedClientHelpers.ENTITY_RENDERERS.addLayerDefinition(LEGACY_BABY_ARMOR.legs(), LegacyBabyArmorImpl::legsLayer);
        UnifiedClientHelpers.ENTITY_RENDERERS.addLayerDefinition(LEGACY_BABY_ARMOR.feet(), LegacyBabyArmorImpl::feetLayer);
    }

    private static final Map<EquipmentSlot, Set<String>> ARMOR_PARTS_PER_SLOT = Map.of(
            EquipmentSlot.HEAD, Set.of("head"),
            EquipmentSlot.CHEST, Set.of("body", "left_arm", "right_arm"),
            EquipmentSlot.LEGS, Set.of("body", "left_leg", "right_leg"),
            EquipmentSlot.FEET, Set.of("left_leg", "right_leg")
    );
    private static final ArmorModelSet<LayerDefinition> LAYER_DEFINITIONS = createArmorMeshSet(
            new CubeDeformation(0.5F),
            new CubeDeformation(1.0F)
    ).map(layer -> LayerDefinition.create(layer, 64, 32));

    private static EntityModelSet cachedModelSet;
    private static final Map<Class<?>, ArmorModelSet<?>> CACHED_ARMOR_MODELS = new IdentityHashMap<>();

    private LegacyBabyArmorImpl() {}

    public static LayerDefinition headLayer() {
        return LAYER_DEFINITIONS.head();
    }

    public static LayerDefinition chestLayer() {
        return LAYER_DEFINITIONS.chest();
    }

    public static LayerDefinition legsLayer() {
        return LAYER_DEFINITIONS.legs();
    }

    public static LayerDefinition feetLayer() {
        return LAYER_DEFINITIONS.feet();
    }

    @SuppressWarnings("unchecked")
    public static <M extends HumanoidModel<?>> ArmorModelSet<M> get(Class<? extends M> modelClass) {
        EntityModelSet entityModels = Minecraft.getInstance().getEntityModels();
        if (cachedModelSet != entityModels) {
            cachedModelSet = entityModels;
            CACHED_ARMOR_MODELS.clear();
        }

        return (ArmorModelSet<M>) CACHED_ARMOR_MODELS.computeIfAbsent(modelClass, key -> bake(entityModels, (Class<? extends HumanoidModel<?>>) key));
    }

    private static ArmorModelSet<MeshDefinition> createArmorMeshSet(CubeDeformation innerDeformation, CubeDeformation outerDeformation) {
        MeshDefinition head = createBaseArmorMesh(outerDeformation);
        head.getRoot().retainPartsAndChildren(ARMOR_PARTS_PER_SLOT.get(EquipmentSlot.HEAD));

        MeshDefinition chest = createBaseArmorMesh(outerDeformation);
        chest.getRoot().retainExactParts(ARMOR_PARTS_PER_SLOT.get(EquipmentSlot.CHEST));

        MeshDefinition legs = createBaseArmorMesh(innerDeformation);
        legs.getRoot().retainExactParts(ARMOR_PARTS_PER_SLOT.get(EquipmentSlot.LEGS));

        MeshDefinition feet = createBaseArmorMesh(outerDeformation);
        feet.getRoot().retainExactParts(ARMOR_PARTS_PER_SLOT.get(EquipmentSlot.FEET));

        return new ArmorModelSet<>(head, chest, legs, feet);
    }

    private static MeshDefinition createBaseArmorMesh(CubeDeformation deformation) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild(
                "head",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, deformation),
                new PartPose(0.0F, 15.25F, 0.0F, 0.0F, 0.0F, 0.0F, 0.75F, 0.75F, 0.75F)
        );
        head.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

        root.addOrReplaceChild(
                "body",
                CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, -6.0F, -2.0F, 8.0F, 12.0F, 4.0F, deformation),
                new PartPose(0.0F, 17.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.5F, 5.0F / 12.0F, 0.5F)
        );
        root.addOrReplaceChild(
                "right_arm",
                CubeListBuilder.create().texOffs(40, 16).addBox(-2.0F, -1.2F, -2.0F, 4.0F, 12.0F, 4.0F, deformation),
                new PartPose(-3.0F, 15.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.5F, 5.0F / 12.0F, 0.5F)
        );
        root.addOrReplaceChild(
                "left_arm",
                CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-2.0F, -1.2F, -2.0F, 4.0F, 12.0F, 4.0F, deformation),
                new PartPose(3.0F, 15.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.5F, 5.0F / 12.0F, 0.5F)
        );
        root.addOrReplaceChild(
                "right_leg",
                CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, deformation.extend(-0.1F)),
                new PartPose(-1.0F, 20.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.5F, 1.0F / 3.0F, 0.5F)
        );
        root.addOrReplaceChild(
                "left_leg",
                CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, deformation.extend(-0.1F)),
                new PartPose(1.0F, 20.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.5F, 1.0F / 3.0F, 0.5F)
        );
        return mesh;
    }

    private static ArmorModelSet<?> bake(EntityModelSet entityModels, Class<? extends HumanoidModel<?>> modelClass) {
        Constructor<? extends HumanoidModel<?>> constructor = getConstructor(modelClass);
        return LEGACY_BABY_ARMOR.map(location -> instantiate(constructor, entityModels.bakeLayer(location)));
    }

    private static Constructor<? extends HumanoidModel<?>> getConstructor(Class<? extends HumanoidModel<?>> modelClass) {
        try {
            Constructor<? extends HumanoidModel<?>> constructor = modelClass.getConstructor(ModelPart.class);
            constructor.setAccessible(true);
            return constructor;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to access armor model constructor for " + modelClass.getName(), exception);
        }
    }

    private static HumanoidModel<?> instantiate(Constructor<? extends HumanoidModel<?>> constructor, ModelPart root) {
        try {
            return constructor.newInstance(root);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to create legacy baby armor model via " + constructor.getDeclaringClass().getName(), exception);
        }
    }
}