package io.github.kingironman2011.orbital_railgun_enhanced.universal;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Universal server-side initializer for the orbital railgun enhanced mod.
 * Detects the running Minecraft version at startup and delegates to the
 * appropriate version-specific implementation bundled inside the universal JAR.
 */
public class UniversalModInitializer implements ModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("OrbitalRailgunEnhanced");

    // Base implementation package prefix (already includes the "impl." segment so shadow
    // will not double-relocate these literals when it excludes "...impl.**" patterns).
    private static final String V1_19_2 =
            "io.github.kingironman2011.orbital_railgun_enhanced.impl.v1_19_2.OrbitalRailgun";
    private static final String V1_20_4 =
            "io.github.kingironman2011.orbital_railgun_enhanced.impl.v1_20_4.OrbitalRailgun";
    private static final String V1_20_6 =
            "io.github.kingironman2011.orbital_railgun_enhanced.impl.v1_20_6.OrbitalRailgun";

    @Override
    public void onInitialize() {
        String mcVersion = VersionManager.getMcVersion();
        LOGGER.info("[Orbital Railgun Enhanced] Detected Minecraft version: {}", mcVersion);

        String className;
        if (VersionManager.isVersion1_19()) {
            className = V1_19_2;
        } else if (VersionManager.isVersion1_20_6()) {
            className = V1_20_6;
        } else {
            className = V1_20_4;
        }

        LOGGER.info("[Orbital Railgun Enhanced] Loading implementation: {}", className);

        try {
            Class<?> cls = Class.forName(className);
            ModInitializer impl = (ModInitializer) cls.getDeclaredConstructor().newInstance();
            impl.onInitialize();
        } catch (Exception e) {
            throw new RuntimeException(
                    "[Orbital Railgun Enhanced] Failed to load implementation for MC " + mcVersion, e);
        }
    }
}
