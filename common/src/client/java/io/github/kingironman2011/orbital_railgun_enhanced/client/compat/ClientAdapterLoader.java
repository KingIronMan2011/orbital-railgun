package io.github.kingironman2011.orbital_railgun_enhanced.client.compat;

import io.github.kingironman2011.orbital_railgun_enhanced.compat.AdapterLoader;

/**
 * Lazily loads the correct {@link ClientVersionAdapter} for the running
 * Minecraft version. Mirrors the server-side {@link AdapterLoader} mechanism.
 */
public final class ClientAdapterLoader {

    private static final String BASE =
            "io.github.kingironman2011.orbital_railgun_enhanced.client.impl.";

    private static volatile ClientVersionAdapter instance;

    private ClientAdapterLoader() { }

    /** Returns the singleton {@link ClientVersionAdapter} for the current MC version. */
    public static ClientVersionAdapter get() {
        if (instance == null) {
            synchronized (ClientAdapterLoader.class) {
                if (instance == null) {
                    instance = load();
                }
            }
        }
        return instance;
    }

    private static ClientVersionAdapter load() {
        // Reuse AdapterLoader's version-resolution logic
        String pkg = resolvePackage();
        String className = BASE + pkg + ".ClientVersionAdapterImpl";

        try {
            return (ClientVersionAdapter) Class.forName(className)
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (Exception e) {
            throw new RuntimeException(
                    "[OrbitalRailgunEnhanced] Failed to load ClientVersionAdapter"
                    + " (tried class: " + className + ")", e);
        }
    }

    private static String resolvePackage() {
        // Delegate version-string parsing to AdapterLoader to keep logic in one place
        try {
            String mc = net.fabricmc.loader.api.FabricLoader.getInstance()
                    .getModContainer("minecraft")
                    .orElseThrow()
                    .getMetadata()
                    .getVersion()
                    .getFriendlyString();
            return AdapterLoader.resolvePackage(mc);
        } catch (Exception e) {
            return "v1206"; // safe fallback
        }
    }
}
