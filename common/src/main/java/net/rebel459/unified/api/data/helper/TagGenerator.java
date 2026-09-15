package net.rebel459.unified.api.data.helper;

import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.impl.core.DataProviders;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class TagGenerator extends HelperGenerator {

    public TagGenerator(String modId, Optional<ExtensibleCodec.Entry<Boolean>> requirement) {
        super(modId, requirement);
    }

    public <T> Builder<T> create(TagKey<T> tag) {
        return new Builder<>(tag, modId, requirement);
    }

    public static final class Builder<T> extends HelperGenerator {
        private final List<ResourceKey<T>> entries = new ArrayList<>();
        private final List<ResourceKey<T>> optionalEntries = new ArrayList<>();

        Builder(TagKey<T> tag, String modId, Optional<ExtensibleCodec.Entry<Boolean>> requirement) {
            super(modId, requirement);
            DataProviders.TAGS.add(modId, new DataProviders.TagRequest<>(tag.registry(), provider -> {
                entries.forEach(entry -> provider.add(tag, entry));
                optionalEntries.forEach(entry -> provider.addOptional(tag, entry));
            }));
        }

        public void add(ResourceKey<T> entry) {
            entries.add(entry);
        }

        public void addOptional(ResourceKey<T> entry) {
            optionalEntries.add(entry);
        }
    }
}
