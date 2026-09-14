package net.rebel459.unified.api.core;

import net.minecraft.resources.Identifier;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.api.codec.ExtensibleCodecs;
import net.rebel459.unified.api.data.helper.CreativeEntryGenerator;
import net.rebel459.unified.api.data.helper.RecipeGenerator;
import net.rebel459.unified.api.data.registry.BlockGenerator;
import net.rebel459.unified.api.data.registry.ItemGenerator;
import net.rebel459.unified.impl.core.DataProviders;

import java.util.*;
import java.util.function.Supplier;

public final class UnifiedData {

    private final Registries registries;
    private final Helpers helpers;
    private final Sets sets;

    private UnifiedData(String modId, String namespace, DataProviders.GenerationSettings settings) {
        this.registries = new Registries(modId, namespace, settings);
        this.helpers = new Helpers(modId, settings.metadata().requirement());
        this.sets = new Sets(this.registries);
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
            requirement = Optional.of(ExtensibleCodecs.REQUIREMENT_TYPES
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

        public ItemGenerator items() {
            return items;
        }
        public BlockGenerator blocks() {
            return blocks;
        }

        private Registries(String modId, String namespace, DataProviders.GenerationSettings settings) {
            this.items = new ItemGenerator(modId, namespace, settings, UnifiedRegistries.Items.create(namespace));
            this.blocks = new BlockGenerator(modId, namespace, settings, UnifiedRegistries.Blocks.create(namespace), this.items);
        }
    }

    public static final class Helpers {
        private final String modId;
        private final Optional<ExtensibleCodec.Entry<Boolean>> requirement;

        private Helpers(String modId, Optional<ExtensibleCodec.Entry<Boolean>> requirement) {
            this.modId = modId;
            this.requirement = requirement;
        }

        public CreativeEntryGenerator creativeEntries() {
            return new CreativeEntryGenerator(modId, requirement);
        }

        public RecipeGenerator recipes() {
            return new RecipeGenerator(modId, requirement);
        }
    }

    public static final class Sets {

        private final UnifiedData.Registries registries;

        private Sets(UnifiedData.Registries registries) {
            this.registries = registries;
        }
    }
}
