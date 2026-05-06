package io.github.kingironman2011.orbital_railgun_enhanced.universal;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Universal client-side initializer for the orbital railgun enhanced mod.
 * Detects the running Minecraft version at startup and delegates to the
 * appropriate version-specific client implementation bundled in the universal JAR.
 */
public class UniversalClientInitializer implements ClientModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("OrbitalRailgunEnhanced");

    // Per-version client entry-point class names (already in impl.* space so shadow
    // excludes them from double-relocation).
    private static final String V1_19_2 =
            "io.github.kingironman2011.orbital_railgun_enhanced.impl.v1_19_2.client.OrbitalRailgunClient";
    private static final String V1_20_4 =
            "io.github.kingironman2011.orbital_railgun_enhanced.impl.v1_20_4.client.OrbitalRailgunClient";
    private static final String V1_20_6 =
            "io.github.kingironman2011.orbital_railgun_enhanced.impl.v1_20_6.client.OrbitalRailgunClient";

    @Override
    public void onInitializeClient() {
        String mcVersion = VersionManager.getMcVersion();
        LOGGER.info("[Orbital Railgun Enhanced] Client init for Minecraft version: {}", mcVersion);

        String className;
        if (VersionManager.isVersion1_19()) {
            className = V1_19_2;
        } else if (VersionManager.isVersion1_20_6()) {
            className = V1_20_6;
        } else {
            className = V1_20_4;
        }

        LOGGER.info("[Orbital Railgun Enhanced] Loading client implementation: {}", className);

        try {
            Class<?> cls = Class.forName(className);
            ClientModInitializer impl =
                    (ClientModInitializer) cls.getDeclaredConstructor().newInstance();
            impl.onInitializeClient();
        } catch (Exception e) {
            throw new RuntimeException(
                    "[Orbital Railgun Enhanced] Failed to load client implementation for MC "
                            + mcVersion, e);
        }
    }
}
