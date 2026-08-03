package net.rebel459.unified.util;

import net.minecraft.SharedConstants;

public record VanillaVersion(int year, int drop, int patch) implements Comparable<VanillaVersion> {

    public static VanillaVersion parse(String version) {
        String[] parts = version.split("\\.");

        int major = parts.length > 0 ? Integer.parseInt(parts[0]) : 0;
        int drop = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        int patch = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;

        return new VanillaVersion(major, drop, patch);
    }

    public static VanillaVersion getCurrentVersion() {
        return parse(SharedConstants.getCurrentVersion().name());
    }

    @Override
    public int compareTo(VanillaVersion other) {
        int c = Integer.compare(year, other.year);
        if (c != 0) return c;

        c = Integer.compare(drop, other.drop);
        if (c != 0) return c;

        return Integer.compare(patch, other.patch);
    }

    @Override
    public String toString() {
        return year + "." + drop + "." + patch;
    }
}