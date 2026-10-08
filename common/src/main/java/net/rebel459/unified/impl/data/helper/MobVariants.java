package net.rebel459.unified.impl.data.helper;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
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
import net.minecraft.world.level.storage.loot.LootTable;
import net.rebel459.unified.Unified;
import net.rebel459.unified.api.codec.UnifiedCodecs;
import net.rebel459.unified.api.core.UnifiedAttachments;
import net.rebel459.unified.api.core.UnifiedHelpers;

import java.util.List;
import java.util.Optional;

public class MobVariants {

    private static final class RegistryKeyHolder {
        private static final ResourceKey<Registry<Definition>> KEY = ResourceKey.createRegistryKey(Unified.id("mob_variants"));
    }

    public static final ResourceKey<Registry<Definition>> KEY = RegistryKeyHolder.KEY;

    public static void init() {
        UnifiedHelpers.DATA_REGISTRIES.registerSynced(
                KEY,
                UnifiedCodecs.loadRequirements(Definition.CODEC, () -> new Definition(
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Sounds.EMPTY
                )),
                UnifiedCodecs.loadRequirements(Definition.NETWORK_CODEC, () -> new Definition(
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Sounds.EMPTY
                ))
        );
    }

    public static UnifiedAttachments.Entity<Optional<Holder<Definition>>> MOB_VARIANT = UnifiedAttachments.Entity.builder(Unified.id("mob_variant"), Optional::<Holder<Definition>>empty)
            .persistent(Definition.REGISTRY_CODEC.optionalFieldOf("mob_variant"))
            .synced(Definition.STREAM_CODEC)
            .copyOnDeath()
            .build();

    public static final UnifiedAttachments.Entity<Boolean> MOB_VARIANT_ATTEMPTED = UnifiedAttachments.Entity.builder(Unified.id("mob_variant_attempted"), () -> false)
            .persistent(Codec.BOOL.fieldOf("mob_variant_attempted"))
            .build();

    public record Definition(
            Optional<Identifier> target,
            Optional<TextureReplacement> texture,
            Optional<TextureReplacement> babyTexture,
            Sounds sounds,
            SpawnPrioritySelectors spawnConditions,
            float spawnChance,
            List<AttributeEntry> attributes,
            List<MobEffectInstance> attackEffects,
            Optional<Boolean> burnInDaylight,
            Optional<ResourceKey<LootTable>> lootTable
    ) implements PriorityProvider<SpawnContext, SpawnCondition> {

        public static final Codec<Definition> CODEC = create(
                Identifier.CODEC.xmap(Optional::of, target -> target.orElseThrow(() -> new IllegalStateException("Mob Variant target is required"))).fieldOf("target"),
                SpawnPrioritySelectors.CODEC.optionalFieldOf("spawn_conditions", SpawnPrioritySelectors.EMPTY),
                ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("spawn_chance", 1F),
                ResourceKey.codec(Registries.LOOT_TABLE).optionalFieldOf("loot_table"),
                AttributeEntry.CODEC.listOf().optionalFieldOf("attributes", List.of())
        ).codec();

        public static final Definition EMPTY = new Definition(
                Optional.empty(), Optional.empty(), Optional.empty(), Sounds.EMPTY,
                SpawnPrioritySelectors.EMPTY, 1F, List.of(), List.of(), Optional.empty(), Optional.empty()
        );
        public static final MapCodec<Definition> PROPERTIES_CODEC = create(MapCodec.unit(Optional.empty()), MapCodec.unit(SpawnPrioritySelectors.EMPTY), MapCodec.unit(1F), MapCodec.unit(Optional.empty()), MapCodec.unit(List.of()));

        private static MapCodec<Definition> create(MapCodec<Optional<Identifier>> target, MapCodec<SpawnPrioritySelectors> spawnConditions, MapCodec<Float> spawnChance, MapCodec<Optional<ResourceKey<LootTable>>> lootTable, MapCodec<List<AttributeEntry>> attributes) {
            return RecordCodecBuilder.mapCodec(instance -> instance.group(
                    target.forGetter(Definition::target),
                    TextureReplacement.CODEC.optionalFieldOf("texture").forGetter(Definition::texture),
                    TextureReplacement.CODEC.optionalFieldOf("baby_texture").forGetter(Definition::babyTexture),
                    Sounds.CODEC.optionalFieldOf("sounds", Sounds.EMPTY).forGetter(Definition::sounds),
                    spawnConditions.forGetter(Definition::spawnConditions),
                    spawnChance.forGetter(Definition::spawnChance),
                    attributes.forGetter(Definition::attributes),
                    MobEffectInstance.CODEC.listOf().optionalFieldOf("attack_effects", List.of()).forGetter(Definition::attackEffects),
                    Codec.BOOL.optionalFieldOf("burn_in_daylight").forGetter(Definition::burnInDaylight),
                    lootTable.forGetter(Definition::lootTable)
            ).apply(instance, Definition::new));
        }

        private Definition(Optional<Identifier> target, Optional<TextureReplacement> texture, Optional<TextureReplacement> babyTexture, Sounds sounds) {
            this(target, texture, babyTexture, sounds, SpawnPrioritySelectors.EMPTY, 1F, List.of(), List.of(), Optional.empty(), Optional.empty());
        }

        public static final Codec<Definition> NETWORK_CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                Identifier.CODEC.optionalFieldOf("target").forGetter(Definition::target),
                TextureReplacement.CODEC.optionalFieldOf("texture").forGetter(Definition::texture),
                TextureReplacement.CODEC.optionalFieldOf("baby_texture").forGetter(Definition::babyTexture),
                Sounds.CODEC.optionalFieldOf("sounds", new Sounds(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).forGetter(Definition::sounds)
        ).apply(instance, Definition::new));
        public static final Codec<Holder<Definition>> REGISTRY_CODEC = RegistryFixedCodec.create(RegistryKeyHolder.KEY);
        public static final StreamCodec<RegistryFriendlyByteBuf, Optional<Holder<Definition>>> STREAM_CODEC = ByteBufCodecs.optional(
                ByteBufCodecs.holder(RegistryKeyHolder.KEY, ByteBufCodecs.fromCodecWithRegistries(NETWORK_CODEC))
        );

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

    public record Sounds(Optional<SoundEvent> ambientSound, Optional<SoundEvent> hurtSound, Optional<SoundEvent> eatSound, Optional<SoundEvent> deathSound, Optional<SoundEvent> stepSound) {
        public static final Codec<Sounds> CODEC =  RecordCodecBuilder.create(instance -> instance.group(
                        BuiltInRegistries.SOUND_EVENT.byNameCodec().optionalFieldOf("ambient_sound").forGetter(Sounds::ambientSound),
                        BuiltInRegistries.SOUND_EVENT.byNameCodec().optionalFieldOf("hurt_sound").forGetter(Sounds::hurtSound),
                        BuiltInRegistries.SOUND_EVENT.byNameCodec().optionalFieldOf("eat_sound").forGetter(Sounds::eatSound),
                        BuiltInRegistries.SOUND_EVENT.byNameCodec().optionalFieldOf("death_sound").forGetter(Sounds::deathSound),
                        BuiltInRegistries.SOUND_EVENT.byNameCodec().optionalFieldOf("step_sound").forGetter(Sounds::stepSound))
                .apply(instance, Sounds::new)
        );

        public static final Sounds EMPTY = new Sounds(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }
}
