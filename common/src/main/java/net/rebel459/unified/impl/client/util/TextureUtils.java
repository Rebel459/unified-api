package net.rebel459.unified.impl.client.util;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.rebel459.unified.impl.data.helper.MobVariants;
import net.rebel459.unified.impl.data.registry.EntityCopier;

import java.util.List;
import java.util.Optional;

public class TextureUtils {

    public static <S extends LivingEntityRenderState> Identifier getVariantTexture(S state, Identifier originalTexture, boolean isOuterLayer) {
        return ((LivingEntityRenderStateVariant) state).getVariant().filter(
                variant -> variant.value().target()
                        .map(EntityType.getKey(state.entityType)::equals)
                        .orElseGet(() -> EntityCopier.isDefaultVariant(state.entityType, variant))
        ).flatMap(variant -> {
            List<MobVariants.TextureReplacement> replacements = variant.value().textureReplacements();

            Optional<MobVariants.TextureReplacement> specific = replacements.stream().filter(
                    replacement -> replacement.textureReplacementTypes() instanceof MobVariants.TextureReplacementTypes.ReplaceTexture type && getTexturePath(type.original()).equals(originalTexture)
            ).findFirst();
            if (specific.isPresent()) return specific;

            return replacements.stream().filter(replacement -> !isOuterLayer &&
                    (state.isBaby ? replacement.textureReplacementTypes() instanceof MobVariants.TextureReplacementTypes.ReplaceBabyTexture : replacement.textureReplacementTypes() instanceof MobVariants.TextureReplacementTypes.ReplaceAdultTexture)
            ).findFirst();
        }).map(replacement -> getTexturePath(replacement.replacement())).orElse(originalTexture);
    }

    public static Identifier getTexturePath(Identifier id) {
        return Identifier.fromNamespaceAndPath(id.getNamespace(), "textures/" + id.getPath() + ".png");
    }
}
