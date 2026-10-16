package net.rebel459.unified.api.data.helper;

import net.rebel459.unified.api.codec.ExtensibleCodec;

import java.util.Optional;

public abstract class HelperGenerator {
    final String modId;
    final String namespace;
    final Optional<ExtensibleCodec.Entry<Boolean>> requirement;

    HelperGenerator(String modId, String namespace, Optional<ExtensibleCodec.Entry<Boolean>> requirement) {
        this.modId = modId;
        this.namespace = namespace;
        this.requirement = requirement;
    }

    public static abstract class Builder extends HelperGenerator {

        final String name;

        Builder(String name, String modId, String namespace, Optional<ExtensibleCodec.Entry<Boolean>> requirement) {
            super(modId, namespace, requirement);
            this.name = name;
        }
    }
}
