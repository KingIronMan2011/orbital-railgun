package io.github.kingironman2011.orbital_railgun_enhanced.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.kingironman2011.orbital_railgun_enhanced.config.ServerConfig;
import net.minecraft.util.math.BlockPos;

public class SoundLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger("OrbitalRailgunEnhanced");

    public static void logSoundEvent(String soundName, BlockPos location, double range) {
        if (ServerConfig.INSTANCE.isDebugMode()) {
            LOGGER.info("[SOUND] Event: {} | Location: {} | Range: {}", soundName, location, range);
        }
    }

    public static void logPlayerEnterRange(String playerName, double distance) {
        if (ServerConfig.INSTANCE.isDebugMode()) {
            LOGGER.info("[SOUND] Player {} entered sound range (distance: {})", playerName, distance);
        }
    }

    public static void logPlayerExitRange(String playerName) {
        if (ServerConfig.INSTANCE.isDebugMode()) {
            LOGGER.info("[SOUND] Player {} exited sound range", playerName);
        }
    }

    public static void logSoundPlayed(String playerName, String soundId, float volume, float pitch) {
        if (ServerConfig.INSTANCE.isDebugMode()) {
            LOGGER.info(
                    "[SOUND] Playing to {}: {} (volume: {}, pitch: {})", playerName, soundId, volume, pitch);
        }
    }

    public static void logSoundStopped(String playerName, String soundId) {
        if (ServerConfig.INSTANCE.isDebugMode()) {
            LOGGER.info("[SOUND] Stopping for {}: {}", playerName, soundId);
        }
    }
}
