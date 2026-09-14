package net.rebel459.unified.api.data.helper;

import net.rebel459.unified.api.codec.ExtensibleCodec;

import java.util.Optional;

public abstract class HelperGenerator {
    final String modId;
    final Optional<ExtensibleCodec.Entry<Boolean>> requirement;

    HelperGenerator(String modId, Optional<ExtensibleCodec.Entry<Boolean>> requirement) {
        this.modId = modId;
        this.requirement = requirement;
    }

    public static abstract class Builder extends HelperGenerator {

        final String name;

        Builder(String name, String modId, Optional<ExtensibleCodec.Entry<Boolean>> requirement) {
            super(modId, requirement);
            this.name = name;
        }
    }
}
