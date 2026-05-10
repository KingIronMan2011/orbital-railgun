package io.github.kingironman2011.orbital_railgun_enhanced.compat;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Lazily loads the correct {@link VersionAdapter} implementation for the
 * running Minecraft version using {@link Class#forName}.
 *
 * <p>The key safety guarantee: Java does not resolve a class's dependencies
 * until that class is actually loaded. By only calling {@code Class.forName}
 * for the matching version, classes referencing absent APIs (e.g.
 * {@code Registry} on 1.20.6, or {@code PayloadTypeRegistry} on 1.19.2) are
 * never touched on incompatible runtime versions.
 */
public final class AdapterLoader {

    private static final String BASE =
            "io.github.kingironman2011.orbital_railgun_enhanced.impl.";

    private static volatile VersionAdapter instance;

    private AdapterLoader() { }

    /** Returns the singleton {@link VersionAdapter} for the current MC version. */
    public static VersionAdapter get() {
        if (instance == null) {
            synchronized (AdapterLoader.class) {
                if (instance == null) {
                    instance = load();
                }
            }
        }
        return instance;
    }

    private static VersionAdapter load() {
        String mc = FabricLoader.getInstance()
                .getModContainer("minecraft")
                .orElseThrow(() -> new IllegalStateException("No minecraft mod container?"))
                .getMetadata()
                .getVersion()
                .getFriendlyString();

        String pkg = resolvePackage(mc);
        String className = BASE + pkg + ".VersionAdapterImpl";

        try {
            return (VersionAdapter) Class.forName(className)
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (Exception e) {
            throw new RuntimeException(
                    "[OrbitalRailgunEnhanced] Failed to load VersionAdapter for MC " + mc
                    + " (tried class: " + className + ")", e);
        }
    }

    /**
     * Maps an MC version string to the impl sub-package name.
     * Extend this method when adding new MC version subprojects.
     */
    public static String resolvePackage(String mc) {
        int[] parts = parseMcVersion(mc);
        int minor = parts[1];
        int patch  = parts[2];

        if (minor == 19 && patch >= 1 && patch <= 2) {
            return "v1192";
        } else if (minor == 20 && patch <= 4) {
            return "v1204";
        } else if (minor == 20 && patch == 5) {
            return "v1205";
        } else if (minor == 20 && patch == 6) {
            return "v1206";
        }
        // Fallback: use the latest known adapter for forward compatibility
        return "v1206";
    }

    /** Parses "1.20.6" → {1, 20, 6}; handles missing patch component. */
    private static int[] parseMcVersion(String mc) {
        String[] parts = mc.split("[.\\-]");
        int major = parts.length > 0 ? safeInt(parts[0]) : 1;
        int minor = parts.length > 1 ? safeInt(parts[1]) : 0;
        int patch  = parts.length > 2 ? safeInt(parts[2]) : 0;
        return new int[]{major, minor, patch};
    }

    private static int safeInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
