package net.rebel459.unified.impl.client.helper;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
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
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.rebel459.unified.Unified;
import net.rebel459.unified.api.client.core.UnifiedClientHelpers;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.*;

public final class SimpleBabyArmorImpl {

    private static final Identifier ID = Identifier.fromNamespaceAndPath(Unified.MOD_ID, "legacy_baby_armor");

    public static HashMap<ResourceKey<EquipmentAsset>, Pair<Boolean, Integer>> LEGACY_BABY_ARMOR_EQUIPMENT = new HashMap<>();

    public static final ArmorModelSet<ModelLayerLocation> LEGACY_BABY_ARMOR = new ArmorModelSet<>(
            new ModelLayerLocation(ID, "helmet"),
            new ModelLayerLocation(ID, "chestplate"),
            new ModelLayerLocation(ID, "leggings"),
            new ModelLayerLocation(ID, "boots")
    );

    public static void init() {
        UnifiedClientHelpers.ENTITY_RENDERERS.addLayerDefinition(LEGACY_BABY_ARMOR.head(), SimpleBabyArmorImpl::headLayer);
        UnifiedClientHelpers.ENTITY_RENDERERS.addLayerDefinition(LEGACY_BABY_ARMOR.chest(), SimpleBabyArmorImpl::chestLayer);
        UnifiedClientHelpers.ENTITY_RENDERERS.addLayerDefinition(LEGACY_BABY_ARMOR.legs(), SimpleBabyArmorImpl::legsLayer);
        UnifiedClientHelpers.ENTITY_RENDERERS.addLayerDefinition(LEGACY_BABY_ARMOR.feet(), SimpleBabyArmorImpl::feetLayer);
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
    private static ResourceManager cachedResourceManager;
    private static final Map<Pair<Identifier, ResourceKey<EquipmentAsset>>, Identifier> CACHED_RESIZED_TEXTURES = new HashMap<>();
    private static final Set<Identifier> REGISTERED_RESIZED_TEXTURES = new HashSet<>();

    private SimpleBabyArmorImpl() {}

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

    public static boolean isResizable(ResourceKey<EquipmentAsset> asset) {
        return LEGACY_BABY_ARMOR_EQUIPMENT.containsKey(asset) && LEGACY_BABY_ARMOR_EQUIPMENT.get(asset).getFirst();
    }

    public static Identifier getResizedTexture(Identifier adultTexture, ResourceKey<EquipmentAsset> asset) {
        Minecraft minecraft = Minecraft.getInstance();
        ResourceManager resourceManager = minecraft.getResourceManager();
        if (cachedResourceManager != resourceManager) {
            clearResizedTextureCache(minecraft.getTextureManager());
            cachedResourceManager = resourceManager;
        }

        Pair<Identifier, ResourceKey<EquipmentAsset>> cacheKey = Pair.of(adultTexture, asset);
        return CACHED_RESIZED_TEXTURES.computeIfAbsent(cacheKey, key -> SimpleBabyArmorImpl.createResizedTexture(key.getFirst(), key.getSecond()));
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

    private static Identifier createResizedTexture(Identifier adultTexture, ResourceKey<EquipmentAsset> asset) {
        Identifier resizedTexture = Identifier.fromNamespaceAndPath(
                Unified.MOD_ID,
                "textures/entity/equipment/legacy_baby/" + asset.identifier().getNamespace() + "/" + asset.identifier().getPath() + "/" + adultTexture.getNamespace() + "/" + adultTexture.getPath()
        );

        try (InputStream input = Minecraft.getInstance().getResourceManager().open(adultTexture);
             NativeImage source = NativeImage.read(input)) {
            NativeImage resized = resizeTexture(source, asset);
            Minecraft.getInstance().getTextureManager().register(resizedTexture, new DynamicTexture(() -> "legacy_baby_armor/" + adultTexture, resized));
            REGISTERED_RESIZED_TEXTURES.add(resizedTexture);
            return resizedTexture;
        } catch (IOException exception) {
            LogUtils.getLogger().warn("Failed to resize legacy baby armor texture {}", adultTexture, exception);
            return adultTexture;
        }
    }

    private static void clearResizedTextureCache(TextureManager textureManager) {
        for (Identifier texture : REGISTERED_RESIZED_TEXTURES) {
            textureManager.release(texture);
        }

        REGISTERED_RESIZED_TEXTURES.clear();
        CACHED_RESIZED_TEXTURES.clear();
    }

    private static NativeImage resizeTexture(NativeImage source, ResourceKey<EquipmentAsset> asset) {
        int downscaledWidth = Math.max(1, source.getWidth() / 2);
        int downscaledHeight = Math.max(1, source.getHeight() / 2);
        NativeImage downscaled = scaleBicubic(source, downscaledWidth, downscaledHeight);
        NativeImage upscaled = scaleNearest(downscaled, source.getWidth(), source.getHeight());
        downscaled.close();
        thresholdAlpha(upscaled, asset);
        return upscaled;
    }

    private static NativeImage scaleBicubic(NativeImage source, int width, int height) {
        NativeImage scaled = new NativeImage(width, height, false);

        for (int y = 0; y < height; y++) {
            float sourceY = ((y + 0.5F) * source.getHeight() / height) - 0.5F;
            for (int x = 0; x < width; x++) {
                float sourceX = ((x + 0.5F) * source.getWidth() / width) - 0.5F;
                scaled.setPixel(x, y, sampleBicubic(source, sourceX, sourceY));
            }
        }

        return scaled;
    }

    private static NativeImage scaleNearest(NativeImage source, int width, int height) {
        NativeImage scaled = new NativeImage(width, height, false);

        for (int y = 0; y < height; y++) {
            int sourceY = Math.min(source.getHeight() - 1, y * source.getHeight() / height);
            for (int x = 0; x < width; x++) {
                int sourceX = Math.min(source.getWidth() - 1, x * source.getWidth() / width);
                scaled.setPixel(x, y, source.getPixel(sourceX, sourceY));
            }
        }

        return scaled;
    }

    private static void thresholdAlpha(NativeImage image,  ResourceKey<EquipmentAsset> asset) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getPixel(x, y);
                int alpha = ARGB.alpha(pixel);
                image.setPixel(x, y, alpha >= LEGACY_BABY_ARMOR_EQUIPMENT.get(asset).getSecond() ? ARGB.color(255, pixel) : 0);
            }
        }
    }

    private static int sampleBicubic(NativeImage source, float sourceX, float sourceY) {
        int baseX = (int) Math.floor(sourceX);
        int baseY = (int) Math.floor(sourceY);
        float alpha = 0.0F;
        float red = 0.0F;
        float green = 0.0F;
        float blue = 0.0F;
        float totalWeight = 0.0F;

        for (int y = baseY - 1; y <= baseY + 2; y++) {
            float weightY = cubicWeight(sourceY - y);
            if (weightY == 0.0F) continue;

            int sampleY = Math.clamp(y, 0, source.getHeight() - 1);
            for (int x = baseX - 1; x <= baseX + 2; x++) {
                float weight = weightY * cubicWeight(sourceX - x);
                if (weight == 0.0F) continue;

                int sampleX = Math.clamp(x, 0, source.getWidth() - 1);
                float[] premultiplied = premultiplied(source.getPixel(sampleX, sampleY));
                alpha += premultiplied[0] * weight;
                red += premultiplied[1] * weight;
                green += premultiplied[2] * weight;
                blue += premultiplied[3] * weight;
                totalWeight += weight;
            }
        }

        if (totalWeight == 0.0F) {
            return 0;
        }

        return fromPremultiplied(
                clamp01(alpha / totalWeight),
                clamp01(red / totalWeight),
                clamp01(green / totalWeight),
                clamp01(blue / totalWeight)
        );
    }

    private static float cubicWeight(float value) {
        float distance = Math.abs(value);
        if (distance >= 2.0F) {
            return 0.0F;
        }

        float x2 = distance * distance;
        float x3 = x2 * distance;
        if (distance < 1.0F) {
            return (7.0F * x3 - 12.0F * x2 + 16.0F) / 18.0F;
        }
        return (-7.0F * x3 + 36.0F * x2 - 60.0F * distance + 32.0F) / 18.0F;
    }

    private static float[] premultiplied(int color) {
        float alpha = ARGB.alpha(color) / 255.0F;
        return new float[]{
                alpha,
                ARGB.red(color) / 255.0F * alpha,
                ARGB.green(color) / 255.0F * alpha,
                ARGB.blue(color) / 255.0F * alpha
        };
    }

    private static int fromPremultiplied(float alpha, float red, float green, float blue) {
        if (alpha <= 0.0F) {
            return 0;
        }

        float scale = 1.0F / alpha;
        return ARGB.color(
                ARGB.as8BitChannel(alpha),
                ARGB.as8BitChannel(clamp01(red * scale)),
                ARGB.as8BitChannel(clamp01(green * scale)),
                ARGB.as8BitChannel(clamp01(blue * scale))
        );
    }

    private static float clamp01(float value) {
        return Math.clamp(value, 0.0F, 1.0F);
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
