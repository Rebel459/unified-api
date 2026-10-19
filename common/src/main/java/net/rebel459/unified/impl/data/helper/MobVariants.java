package net.rebel459.unified.impl.data.helper;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryFixedCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
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
                UnifiedCodecs.loadRequirements(Definition.CODEC, () -> Definition.EMPTY),
                UnifiedCodecs.loadRequirements(Definition.NETWORK_CODEC, () -> Definition.EMPTY)
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
            List<TextureReplacement> textureReplacements,
            Sounds sounds,
            SpawnPrioritySelectors spawnConditions,
            float spawnChance,
            List<AttributeEntry> attributes,
            List<MobEffectInstance> attackEffects,
            Optional<Boolean> burnInDaylight,
            Optional<ResourceKey<LootTable>> lootTable
    ) implements PriorityProvider<SpawnContext, SpawnCondition> {

        public static final Codec<Definition> CODEC = create(
                Identifier.CODEC.xmap(Optional::of, target -> target.orElseThrow(() -> new IllegalStateException("Mob Variant targets is required"))).fieldOf("targets"),
                SpawnPrioritySelectors.CODEC.optionalFieldOf("spawn_conditions", SpawnPrioritySelectors.EMPTY),
                ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("spawn_chance", 1F),
                ResourceKey.codec(Registries.LOOT_TABLE).optionalFieldOf("loot_table"),
                AttributeEntry.CODEC.listOf().optionalFieldOf("attributes", List.of())
        ).codec();

        public static final Definition EMPTY = new Definition(Optional.empty(), List.of(), Sounds.EMPTY, SpawnPrioritySelectors.EMPTY, 1F, List.of(), List.of(), Optional.empty(), Optional.empty());

        public static final MapCodec<Definition> PROPERTIES_CODEC = create(MapCodec.unit(Optional.empty()), MapCodec.unit(SpawnPrioritySelectors.EMPTY), MapCodec.unit(1F), MapCodec.unit(Optional.empty()), MapCodec.unit(List.of()));

        private static MapCodec<Definition> create(MapCodec<Optional<Identifier>> target, MapCodec<SpawnPrioritySelectors> spawnConditions, MapCodec<Float> spawnChance, MapCodec<Optional<ResourceKey<LootTable>>> lootTable, MapCodec<List<AttributeEntry>> attributes) {
            return RecordCodecBuilder.mapCodec(instance -> instance.group(
                    target.forGetter(Definition::target),
                    UnifiedCodecs.compactList(TextureReplacement.CODEC).optionalFieldOf("texture_replacements", List.of()).forGetter(Definition::textureReplacements),
                    Sounds.CODEC.optionalFieldOf("sounds", Sounds.EMPTY).forGetter(Definition::sounds),
                    spawnConditions.forGetter(Definition::spawnConditions),
                    spawnChance.forGetter(Definition::spawnChance),
                    attributes.forGetter(Definition::attributes),
                    MobEffectInstance.CODEC.listOf().optionalFieldOf("attack_effects", List.of()).forGetter(Definition::attackEffects),
                    Codec.BOOL.optionalFieldOf("burn_in_daylight").forGetter(Definition::burnInDaylight),
                    lootTable.forGetter(Definition::lootTable)
            ).apply(instance, Definition::new));
        }

        private Definition(Optional<Identifier> target, List<TextureReplacement> textureReplacements, Sounds sounds) {
            this(target, textureReplacements, sounds, SpawnPrioritySelectors.EMPTY, 1F, List.of(), List.of(), Optional.empty(), Optional.empty());
        }

        public static final Codec<Definition> NETWORK_CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                Identifier.CODEC.optionalFieldOf("targets").forGetter(Definition::target),
                UnifiedCodecs.compactList(TextureReplacement.CODEC).optionalFieldOf("texture_replacements", List.of()).forGetter(Definition::textureReplacements),
                Sounds.CODEC.optionalFieldOf("sounds", Sounds.EMPTY).forGetter(Definition::sounds)
        ).apply(instance, Definition::new));
        public static final Codec<Holder<Definition>> REGISTRY_CODEC = RegistryFixedCodec.create(RegistryKeyHolder.KEY);
        public static final StreamCodec<RegistryFriendlyByteBuf, Optional<Holder<Definition>>> STREAM_CODEC = ByteBufCodecs.optional(
                ByteBufCodecs.holder(RegistryKeyHolder.KEY, ByteBufCodecs.fromCodecWithRegistries(NETWORK_CODEC))
        );

        public List<Selector<SpawnContext, SpawnCondition>> selectors() {
            return this.spawnConditions.selectors();
        }
    }

    public record TextureReplacement(TextureReplacementTypes textureReplacementTypes, Identifier replacement) {
        private static final Codec<TextureReplacement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                TextureReplacementTypes.CODEC.forGetter(TextureReplacement::textureReplacementTypes),
                Identifier.CODEC.fieldOf("replacement").forGetter(TextureReplacement::replacement)
        ).apply(instance, TextureReplacement::new));
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

    public sealed interface TextureReplacementTypes {

        MapCodec<TextureReplacementTypes> CODEC = Type.CODEC.optionalFieldOf("type", Type.REPLACE_TEXTURE).dispatchMap(TextureReplacementTypes::type, TextureReplacementTypes.Type::codec);

        Type type();

        record ReplaceAdultTexture() implements TextureReplacementTypes {
            public static final MapCodec<ReplaceAdultTexture> CODEC = MapCodec.unit(ReplaceAdultTexture::new);

            @Override
            public Type type() {
                return Type.REPLACE_ADULT_TEXTURE;
            }
        }

        record ReplaceBabyTexture() implements TextureReplacementTypes {
            public static final MapCodec<ReplaceBabyTexture> CODEC = MapCodec.unit(ReplaceBabyTexture::new);

            @Override
            public Type type() {
                return Type.REPLACE_BABY_TEXTURE;
            }
        }

        record ReplaceTexture(Identifier original) implements TextureReplacementTypes {
            public static final MapCodec<ReplaceTexture> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Identifier.CODEC.fieldOf("original").forGetter(ReplaceTexture::original)
            ).apply(instance, ReplaceTexture::new));

            @Override
            public Type type() {
                return Type.REPLACE_TEXTURE;
            }
        }

        enum Type implements StringRepresentable {
            REPLACE_ADULT_TEXTURE("replace_adult_texture", ReplaceAdultTexture.CODEC),
            REPLACE_BABY_TEXTURE("replace_baby_texture", ReplaceBabyTexture.CODEC),
            REPLACE_TEXTURE("replace_texture", ReplaceTexture.CODEC);

            public static final Codec<TextureReplacementTypes.Type> CODEC = StringRepresentable.fromEnum(TextureReplacementTypes.Type::values);

            private final String name;
            private final MapCodec<? extends TextureReplacementTypes> codec;

            Type(String name, MapCodec<? extends TextureReplacementTypes> codec) {
                this.name = name;
                this.codec = codec;
            }

            public MapCodec<? extends TextureReplacementTypes> codec() {
                return codec;
            }

            @Override
            public String getSerializedName() {
                return name;
            }
        }
    }
}
