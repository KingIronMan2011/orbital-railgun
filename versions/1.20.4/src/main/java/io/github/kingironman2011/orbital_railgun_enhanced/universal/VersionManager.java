package io.github.kingironman2011.orbital_railgun_enhanced.universal;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Utility class for detecting the running Minecraft version at runtime.
 * Used by the universal JAR to select the correct version-specific implementation.
 */
public final class VersionManager {

    private VersionManager() {}

    /**
     * Returns the human-friendly Minecraft version string (e.g. "1.20.1").
     */
    public static String getMcVersion() {
        return FabricLoader.getInstance()
                .getModContainer("minecraft")
                .map(c -> c.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
    }

    /**
     * Returns true when running on Minecraft 1.19.x (uses the 1.19.2 implementation).
     */
    public static boolean isVersion1_19() {
        return getMcVersion().startsWith("1.19");
    }

    /**
     * Returns true when running on Minecraft 1.20.5 or 1.20.6 (uses the 1.20.6 implementation).
     */
    public static boolean isVersion1_20_6() {
        String v = getMcVersion();
        return v.startsWith("1.20.5") || v.startsWith("1.20.6");
    }

    /**
     * Returns true for all other 1.20.x versions (uses the 1.20.4 implementation).
     */
    public static boolean isVersion1_20_4() {
        return !isVersion1_19() && !isVersion1_20_6();
    }
}
