package net.rebel459.unified.api.data.helper;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.variant.PriorityProvider;
import net.minecraft.world.entity.variant.SpawnCondition;
import net.minecraft.world.entity.variant.SpawnContext;
import net.minecraft.world.entity.variant.SpawnPrioritySelectors;
import net.minecraft.world.level.storage.loot.LootTable;
import net.rebel459.unified.api.codec.CodecGenerator;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.impl.data.helper.MobVariants;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class MobVariantGenerator extends HelperGenerator {

    public MobVariantGenerator(String modId, Optional<ExtensibleCodec.Entry<Boolean>> requirement) {
        super(modId, requirement);
    }

    public Builder create(String name, EntityType<?> target) {
        return new Builder(name, target, modId, requirement);
    }
    
    public static final class Builder extends HelperGenerator.Builder {

        private Optional<MobVariants.TextureReplacement> texture = Optional.empty();
        private Optional<MobVariants.TextureReplacement> babyTexture = Optional.empty();
        private Optional<SoundEvent> ambientSound = Optional.empty();
        private Optional<SoundEvent> hurtSound = Optional.empty();
        private Optional<SoundEvent> eatSound = Optional.empty();
        private Optional<SoundEvent> deathSound = Optional.empty();
        private Optional<SoundEvent> stepSound = Optional.empty();
        private List<PriorityProvider.Selector<SpawnContext, SpawnCondition>> spawnConditions = new ArrayList<>();
        private float spawnChance = 1F;
        private final List<MobVariants.AttributeEntry> attributes = new ArrayList<>();
        private final List<MobEffectInstance> attackEffects = new ArrayList<>();
        private Optional<Boolean> burnInDaylight = Optional.empty();
        private Optional<ResourceKey<LootTable>> lootTable = Optional.empty();

        Builder(String name, EntityType<?> target, String modId, Optional<ExtensibleCodec.Entry<Boolean>> requirement) {
            super(name, modId, requirement);
            CodecGenerator.data(modId, Identifier.fromNamespaceAndPath(modId, "unified/mob_variants/" + name), requirement, MobVariants.Definition.CODEC,
                    () -> new MobVariants.Definition(Optional.of(BuiltInRegistries.ENTITY_TYPE.getKey(target)), texture, babyTexture, new MobVariants.Sounds(ambientSound, hurtSound, eatSound, deathSound, stepSound), new SpawnPrioritySelectors(spawnConditions), spawnChance, attributes, attackEffects, burnInDaylight, lootTable));
        }

        public Builder setTexture(Identifier replacement) {
            texture = Optional.of(new MobVariants.TextureReplacement(Optional.empty(), replacement));
            return this;
        }
        public Builder setTexture(Identifier target, Identifier replacement) {
            texture = Optional.of(new MobVariants.TextureReplacement(Optional.of(target), replacement));
            return this;
        }

        public Builder setBabyTexture(Identifier replacement) {
            babyTexture = Optional.of(new MobVariants.TextureReplacement(Optional.empty(), replacement));
            return this;
        }
        public Builder setBabyTexture(Identifier target, Identifier replacement) {
            babyTexture = Optional.of(new MobVariants.TextureReplacement(Optional.of(target), replacement));
            return this;
        }

        public Builder setAmbientSound(SoundEvent sound) {
            ambientSound = Optional.of(sound);
            return this;
        }
        public Builder setHurtSound(SoundEvent sound) {
            hurtSound = Optional.of(sound);
            return this;
        }
        public Builder setEatSound(SoundEvent sound) {
            eatSound = Optional.of(sound);
            return this;
        }
        public Builder setDeathSound(SoundEvent sound) {
            deathSound = Optional.of(sound);
            return this;
        }
        public Builder setStepSound(SoundEvent sound) {
            stepSound = Optional.of(sound);
            return this;
        }

        public Builder setSpawnConditions(List<PriorityProvider.Selector<SpawnContext, SpawnCondition>> conditions) {
            spawnConditions = new ArrayList<>(conditions);
            return this;
        }
        public Builder addSpawnCondition(PriorityProvider.Selector<SpawnContext, SpawnCondition> condition) {
            spawnConditions.add(condition);
            return this;
        }

        public Builder setSpawnChance(float chance) {
            spawnChance = chance;
            return this;
        }

        public Builder addAttribute(Holder<Attribute> attribute, AttributeModifier modifier) {
            attributes.add(new MobVariants.AttributeEntry(attribute, modifier));
            return this;
        }

        public Builder addAttackEffect(MobEffectInstance effect) {
            attackEffects.add(effect);
            return this;
        }

        public Builder shouldBurnInDaylight(boolean burnInDaylight) {
            this.burnInDaylight = Optional.of(burnInDaylight);
            return this;
        }

        public Builder setLootTable(ResourceKey<LootTable> lootTable) {
            this.lootTable = Optional.of(lootTable);
            return this;
        }
    }
}
