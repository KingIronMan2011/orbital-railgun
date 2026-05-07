package io.github.kingironman2011.orbital_railgun_enhanced.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.kingironman2011.orbital_railgun_enhanced.OrbitalRailgun;
import io.github.kingironman2011.orbital_railgun_enhanced.client.compat.ClientAdapterLoader;
import io.github.kingironman2011.orbital_railgun_enhanced.client.config.EnhancedConfigWrapper;
import net.fabricmc.api.ClientModInitializer;

/**
 * Common client entry point. All version-specific client initialisation
 * is delegated to the
 * {@link io.github.kingironman2011.orbital_railgun_enhanced.client.compat.ClientVersionAdapter}
 * resolved by {@link ClientAdapterLoader} at runtime.
 */
public class OrbitalRailgunClient implements ClientModInitializer {

    public static final Logger LOGGER =
            LoggerFactory.getLogger(OrbitalRailgun.MOD_ID + "-client");

    /** Loaded owo-lib client config — accessible from all version-specific code. */
    public static EnhancedConfigWrapper CONFIG;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing Orbital Railgun Enhanced (client)...");
        CONFIG = EnhancedConfigWrapper.createAndLoad();
        ClientAdapterLoader.get().initialize();
        LOGGER.info("Orbital Railgun Enhanced client initialization complete!");
    }
}
