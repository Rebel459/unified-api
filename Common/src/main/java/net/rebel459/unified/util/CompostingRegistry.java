package net.rebel459.unified.util;

public interface CompostingRegistry extends Item2ObjectMap<Float> {
    CompostingRegistry INSTANCE = new CompostingRegistryImpl();
}