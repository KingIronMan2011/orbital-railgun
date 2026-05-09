package io.github.kingironman2011.orbital_railgun_enhanced;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.kingironman2011.orbital_railgun_enhanced.compat.AdapterLoader;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

/**
 * Common mod entry point. All version-specific initialization is delegated
 * to the {@link io.github.kingironman2011.orbital_railgun_enhanced.compat.VersionAdapter}
 * resolved by {@link AdapterLoader} at runtime.
 */
public class OrbitalRailgun implements ModInitializer {

    public static final String MOD_ID = "orbital_railgun_enhanced";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /** Total duration of the railgun shoot sound in milliseconds (~53 s). */
    public static final long RAILGUN_SOUND_DURATION_MS = 52992L;

    // Network packet IDs (used by all version adapters)
    public static final Identifier SHOOT_PACKET_ID = new Identifier(MOD_ID, "shoot_packet");
    public static final Identifier CLIENT_SYNC_PACKET_ID = new Identifier(MOD_ID, "client_sync_packet");
    public static final Identifier PLAY_SOUND_PACKET_ID = new Identifier(MOD_ID, "play_sound");
    public static final Identifier STOP_ANIMATION_PACKET_ID = new Identifier(MOD_ID, "stop_animation");
    public static final Identifier STOP_AREA_SOUND_PACKET_ID = new Identifier(MOD_ID, "stop_area_sound");

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Orbital Railgun Enhanced...");
        AdapterLoader.get().initialize();
        LOGGER.info("Orbital Railgun Enhanced initialization complete!");
    }
}
