package net.rebel459.unified.api.core;

import net.minecraft.resources.Identifier;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.api.codec.ExtensibleCodecs;
import net.rebel459.unified.api.data.helper.*;
import net.rebel459.unified.api.data.registry.*;
import net.rebel459.unified.impl.core.DataProviders;

import java.util.*;
import java.util.function.Supplier;

public final class UnifiedData {

    private final Registries registries;
    private final Helpers helpers;
    private final Sets sets;

    private UnifiedData(String modId, String namespace, DataProviders.GenerationSettings settings) {
        this.helpers = new Helpers(modId, settings.metadata().requirement());
        this.registries = new Registries(modId, namespace, settings, this.helpers.tags(), this.helpers.recipes());
        this.sets = new Sets(this.registries, this.helpers);
        settings.injectedTranslations().ifPresent(ignored -> DataProviders.LANGUAGES.add(modId, new DataProviders.LanguageRequest(settings, Optional.empty())));
    }

    public Registries registries() {
        return registries;
    }

    public Helpers helpers() {
        return helpers;
    }

    public Sets sets() {
        return sets;
    }

    public static Builder create(String modId) {
        return new Builder(modId);
    }

    public static final class Builder {
        private final String modId;
        private String namespace;
        private int priority;
        private Optional<ExtensibleCodec.Entry<Boolean>> requirement = Optional.empty();
        private boolean autoName;
        private String language = "en_us";
        private Optional<String> injectedTranslations = Optional.empty();

        private Builder(String modId) {
            this.modId = modId;
            this.namespace = modId;
        }

        public Builder namespace(String namespace) {
            this.namespace = namespace;
            return this;
        }

        public Builder priority(int value) {
            priority = value; return this;
        }
        public Builder requirement(String path, Supplier<Boolean> value) {
            requirement = Optional.of(ExtensibleCodecs.LOAD_REQUIREMENT
                    .register(Identifier.fromNamespaceAndPath(modId, path), value)
                    .create());
            return this;
        }
        public Builder requirement(ExtensibleCodec.Entry<Boolean> requirement) {
            this.requirement = Optional.of(requirement);
            return this;
        }
        public Builder autoName() {
            autoName = true; injectedTranslations = Optional.empty(); return this;
        }
        public Builder autoName(String injectedTranslations) {
            autoName = true;
            this.injectedTranslations = Optional.of(injectedTranslations);
            return this;
        }
        public Builder language(String language) {
            this.language = language; return this;
        }

        public UnifiedData build() {
            return new UnifiedData(modId, namespace, new DataProviders.GenerationSettings(
                    new DataProviders.PriorityAndRequirement(priority, requirement),
                    autoName,
                    language,
                    injectedTranslations
            ));
        }
    }

    public static final class Registries {
        private final ItemGenerator items;
        private final BlockGenerator blocks;
        private final BlockSetTypeGenerator blockSetTypes;
        private final CreativeTabGenerator creativeTabs;
        private final EntityGenerator entities;
        private final SoundEventGenerator soundEvents;
        private final WoodTypeGenerator woodTypes;

        private Registries(String modId, String namespace, DataProviders.GenerationSettings settings, TagGenerator tags, RecipeGenerator recipes) {
            this.items = new ItemGenerator(modId, namespace, settings, tags, recipes);
            this.blocks = new BlockGenerator(modId, namespace, settings, this.items, tags, recipes);
            this.blockSetTypes = new BlockSetTypeGenerator(modId, namespace, settings);
            this.creativeTabs = new CreativeTabGenerator(modId, namespace, settings);
            this.entities = new EntityGenerator(modId, namespace, settings, tags);
            this.soundEvents = new SoundEventGenerator(modId, namespace, settings);
            this.woodTypes = new WoodTypeGenerator(modId, namespace, settings);
        }

        public BlockGenerator blocks() {
            return blocks;
        }

        public BlockSetTypeGenerator blockSetTypes() {
            return blockSetTypes;
        }

        public CreativeTabGenerator creativeTabs() {
            return creativeTabs;
        }

        public EntityGenerator entities() {
            return entities;
        }

        public ItemGenerator items() {
            return items;
        }

        public SoundEventGenerator soundEvents() {
            return soundEvents;
        }

        public WoodTypeGenerator woodTypes() {
            return woodTypes;
        }
    }

    public static final class Helpers {
        private final BiomeModifierGenerator biomeModifiers;
        private final BlockConversionGenerator blockConversions;
        private final ComponentModifierGenerator componentModifiers;
        private final CreativeEntryGenerator creativeEntries;
        private final LootInjectionGenerator lootInjections;
        private final MobVariantGenerator mobVariants;
        private final RecipeGenerator recipes;
        private final TagGenerator tags;

        private Helpers(String modId, Optional<ExtensibleCodec.Entry<Boolean>> requirement) {
            this.biomeModifiers = new BiomeModifierGenerator(modId, requirement);
            this.blockConversions = new BlockConversionGenerator(modId, requirement);
            this.componentModifiers = new ComponentModifierGenerator(modId, requirement);
            this.creativeEntries = new CreativeEntryGenerator(modId, requirement);
            this.lootInjections = new LootInjectionGenerator(modId, requirement);
            this.mobVariants = new MobVariantGenerator(modId, requirement);
            this.recipes = new RecipeGenerator(modId, requirement);
            this.tags = new TagGenerator(modId, requirement);
        }

        public BiomeModifierGenerator biomeModifiers() {
            return biomeModifiers;
        }

        public BlockConversionGenerator blockConversions() {
            return blockConversions;
        }

        public ComponentModifierGenerator componentModifiers() {
            return componentModifiers;
        }

        public CreativeEntryGenerator creativeEntries() {
            return creativeEntries;
        }

        public LootInjectionGenerator lootInjections() {
            return lootInjections;
        }

        public MobVariantGenerator mobVariants() {
            return mobVariants;
        }

        public RecipeGenerator recipes() {
            return recipes;
        }

        public TagGenerator tags() {
            return tags;
        }
    }

    public static final class Sets {

        private final Registries registries;
        private final Helpers helpers;

        private Sets(Registries registries, Helpers helpers) {
            this.registries = registries;
            this.helpers = helpers;
        }
    }
}
