package net.rebel459.unified.util.data;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.variant.PriorityProvider;
import net.minecraft.world.entity.variant.SpawnCondition;
import net.minecraft.world.entity.variant.SpawnContext;
import net.minecraft.world.entity.variant.SpawnPrioritySelectors;
import net.rebel459.unified.Unified;
import net.rebel459.unified.platform.UnifiedAttachments;
import net.rebel459.unified.platform.UnifiedHelpers;

import java.util.List;
import java.util.Optional;

public class MobVariants {

    public static final ResourceKey<Registry<Variant>> KEY = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(Unified.MOD_ID, "mob_variants"));

    public static void init() {
        UnifiedHelpers.DATA_REGISTRIES.registerSynced(KEY, Variant.CODEC, Variant.NETWORK_CODEC);
    }

    public static UnifiedAttachments.Entity<Optional<Holder<Variant>>> MOB_VARIANT = UnifiedAttachments.Entity.builder(Identifier.fromNamespaceAndPath(Unified.MOD_ID, "mob_variant"), Optional::<Holder<Variant>>empty)
            .persistent(Variant.REGISTRY_CODEC.optionalFieldOf("mob_variant"))
            .synced(Variant.STREAM_CODEC)
            .copyOnDeath()
            .build();

    public static final UnifiedAttachments.Entity<Boolean> MOB_VARIANT_ATTEMPTED = UnifiedAttachments.Entity.builder(Identifier.fromNamespaceAndPath(Unified.MOD_ID, "mob_variant_attempted"), () -> false)
            .persistent(Codec.BOOL.fieldOf("mob_variant_attempted"))
            .build();

    public record Variant(
            Identifier target,
            Optional<TextureReplacement> texture,
            Optional<TextureReplacement> babyTexture,
            SoundVariants sounds,
            SpawnPrioritySelectors spawnConditions,
            float spawnChance,
            List<AttributeEntry> attributes,
            List<MobEffectInstance> attackEffects,
            Optional<Boolean> burnInDaylight,
            Optional<Identifier> lootTable
    ) implements PriorityProvider<SpawnContext, SpawnCondition> {

        public static final Codec<Variant> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("target").forGetter(Variant::target),
                TextureReplacement.CODEC.optionalFieldOf("texture").forGetter(Variant::texture),
                TextureReplacement.CODEC.optionalFieldOf("baby_texture").forGetter(Variant::babyTexture),
                SoundVariants.CODEC.optionalFieldOf("sounds", new SoundVariants(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).forGetter(Variant::sounds),
                SpawnPrioritySelectors.CODEC.optionalFieldOf("spawn_conditions", SpawnPrioritySelectors.EMPTY).forGetter(Variant::spawnConditions),
                ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("spawn_chance", 1F).forGetter(Variant::spawnChance),
                AttributeEntry.CODEC.listOf().optionalFieldOf("attributes", List.of()).forGetter(Variant::attributes),
                MobEffectInstance.CODEC.listOf().optionalFieldOf("attack_effects", List.of()).forGetter(Variant::attackEffects),
                Codec.BOOL.optionalFieldOf("burn_in_daylight").forGetter(Variant::burnInDaylight),
                Identifier.CODEC.optionalFieldOf("loot_table").forGetter(Variant::lootTable)
        ).apply(instance, Variant::new));

        private Variant(Identifier target, Optional<TextureReplacement> texture, Optional<TextureReplacement> babyTexture, SoundVariants sounds) {
            this(target, texture, babyTexture, sounds, SpawnPrioritySelectors.EMPTY, 1F, List.of(), List.of(), Optional.empty(), Optional.empty());
        }

        public static final Codec<Variant> NETWORK_CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                Identifier.CODEC.fieldOf("target").forGetter(Variant::target),
                TextureReplacement.CODEC.optionalFieldOf("texture").forGetter(Variant::texture),
                TextureReplacement.CODEC.optionalFieldOf("baby_texture").forGetter(Variant::babyTexture),
                SoundVariants.CODEC.optionalFieldOf("sounds", new SoundVariants(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).forGetter(Variant::sounds)
        ).apply(instance, Variant::new));
        public static final RegistryFixedCodec<Variant> REGISTRY_CODEC = RegistryFixedCodec.create(KEY);
        public static final StreamCodec<RegistryFriendlyByteBuf, Optional<Holder<Variant>>> STREAM_CODEC = ByteBufCodecs.optional(ByteBufCodecs.holderRegistry(KEY));

        public List<Selector<SpawnContext, SpawnCondition>> selectors() {
            return this.spawnConditions.selectors();
        }
    }

    public record TextureReplacement(Optional<Identifier> original, Identifier replacement) {
        private static final Codec<TextureReplacement> OBJECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.optionalFieldOf("original").forGetter(TextureReplacement::original),
                Identifier.CODEC.fieldOf("replacement").forGetter(TextureReplacement::replacement)
        ).apply(instance, TextureReplacement::new));

        public static final Codec<TextureReplacement> CODEC = Codec.either(Identifier.CODEC, OBJECT_CODEC).xmap(
                either -> either.map(replacement -> new TextureReplacement(Optional.empty(), replacement), textureReplacement -> textureReplacement),
                textureReplacement -> textureReplacement.original.isPresent() ? Either.right(textureReplacement) : Either.left(textureReplacement.replacement)
        );
    }

    public record AttributeEntry(Holder<Attribute> attribute, AttributeModifier modifier) {
        public static final Codec<AttributeEntry> CODEC = RecordCodecBuilder.create(
                i -> i.group(
                                Attribute.CODEC.fieldOf("attribute").forGetter(AttributeEntry::attribute),
                                AttributeModifier.MAP_CODEC.forGetter(AttributeEntry::modifier)
                        )
                        .apply(i, AttributeEntry::new)
        );
    }

    public record SoundVariants(Optional<Holder<SoundEvent>> ambientSound, Optional<Holder<SoundEvent>> hurtSound, Optional<Holder<SoundEvent>> eatSound, Optional<Holder<SoundEvent>> deathSound, Optional<Holder<SoundEvent>> stepSound) {
        public static final Codec<SoundVariants> CODEC =  RecordCodecBuilder.create(instance -> instance.group(
                        SoundEvent.CODEC.optionalFieldOf("ambient_sound").forGetter(SoundVariants::ambientSound),
                        SoundEvent.CODEC.optionalFieldOf("hurt_sound").forGetter(SoundVariants::hurtSound),
                        SoundEvent.CODEC.optionalFieldOf("eat_sound").forGetter(SoundVariants::eatSound),
                        SoundEvent.CODEC.optionalFieldOf("death_sound").forGetter(SoundVariants::deathSound),
                        SoundEvent.CODEC.optionalFieldOf("step_sound").forGetter(SoundVariants::stepSound))
                .apply(instance, SoundVariants::new)
        );
    }
}
