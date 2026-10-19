package net.rebel459.unified.api.registry;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.api.codec.ExtensibleCodecs;

public class VanillaEntityAttributeCodecs {

    public static final ExtensibleCodec.Simple<AttributeSupplier> LIVING_ENTITY = ExtensibleCodecs.ENTITY_ATTRIBUTES.register(
            Identifier.withDefaultNamespace("living_entity"),
            () -> LivingEntity.createLivingAttributes().build()
    );

    public static void init() {}
}