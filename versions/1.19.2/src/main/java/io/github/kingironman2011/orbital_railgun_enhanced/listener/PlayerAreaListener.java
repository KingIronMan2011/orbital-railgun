package io.github.kingironman2011.orbital_railgun_enhanced.listener;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.kingironman2011.orbital_railgun_enhanced.config.ServerConfig;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerAreaListener {
    private static final Logger LOGGER = LoggerFactory.getLogger("OrbitalRailgunEnhanced");
    private static final Map<UUID, AreaState> playerStates = new ConcurrentHashMap<>();
    private static Consumer<AreaChangeEvent> areaChangeCallback = null;

    /**
     * Tracks state for a player in relation to a laser impact area
     */
    private static class AreaState {
        boolean isInside;
        double lastLaserX;
        double lastLaserZ;
        long fireTimestamp; // When the railgun was fired (in milliseconds)

        AreaState(boolean isInside, double laserX, double laserZ, long fireTimestamp) {
            this.isInside = isInside;
            this.lastLaserX = laserX;
            this.lastLaserZ = laserZ;
            this.fireTimestamp = fireTimestamp;
        }
    }

    /**
     * Checks if a player is within the sound range of a laser impact location. Uses circular distance
     * check (more accurate than square bounds).
     *
     * @param player The player to check
     * @param laserX The X coordinate of the laser impact
     * @param laserZ The Z coordinate of the laser impact
     * @return true if the player is inside the range, false otherwise
     */
    public static boolean isPlayerInRange(ServerPlayerEntity player, double laserX, double laserZ) {
        double soundRange = ServerConfig.INSTANCE.getSoundRange();

        double playerX = player.getX();
        double playerZ = player.getZ();

        double dx = playerX - laserX;
        double dz = playerZ - laserZ;
        double distanceSquared = dx * dx + dz * dz;
        double rangeSquared = soundRange * soundRange;

        return distanceSquared <= rangeSquared;
    }

    /**
     * Handles player area check and tracks state changes (entering/leaving area). Returns information
     * about whether the player's state changed.
     *
     * @param player The player to check
     * @param laserX The X coordinate of the laser impact
     * @param laserZ The Z coordinate of the laser impact
     * @return An AreaCheckResult containing state information
     */
    public static AreaCheckResult handlePlayerAreaCheck(
            ServerPlayerEntity player, double laserX, double laserZ) {
        return handlePlayerAreaCheck(player, laserX, laserZ, System.currentTimeMillis());
    }

    /**
     * Handles player area check and tracks state changes (entering/leaving area) with timestamp.
     * Returns information about whether the player's state changed.
     *
     * @param player        The player to check
     * @param laserX        The X coordinate of the laser impact
     * @param laserZ        The Z coordinate of the laser impact
     * @param fireTimestamp The timestamp when the railgun was fired (in milliseconds)
     * @return An AreaCheckResult containing state information
     */
    public static AreaCheckResult handlePlayerAreaCheck(
            ServerPlayerEntity player, double laserX, double laserZ, long fireTimestamp) {
        UUID playerId = player.getUuid();
        boolean currentlyInside = isPlayerInRange(player, laserX, laserZ);

        AreaState previousState = playerStates.get(playerId);
        boolean wasInside = previousState != null && previousState.isInside;

        boolean isNewLocation =
                previousState == null
                        || previousState.lastLaserX != laserX
                        || previousState.lastLaserZ != laserZ;

        // Use the existing timestamp if this is the same location, otherwise use the new one
        long timestamp = isNewLocation ? fireTimestamp : previousState.fireTimestamp;

        playerStates.put(playerId, new AreaState(currentlyInside, laserX, laserZ, timestamp));

        AreaCheckResult result = new AreaCheckResult();
        result.isInside = currentlyInside;
        result.wasInside = wasInside;
        result.isNewLocation = isNewLocation;
        result.fireTimestamp = timestamp;

        if (ServerConfig.INSTANCE.isDebugMode()) {
            if (isNewLocation) {
                LOGGER.debug(
                        "[AREA] New laser location: ({}, {}) for player {} at time {}",
                        laserX,
                        laserZ,
                        player.getName().getString(),
                        timestamp);
            }
            if (!wasInside && currentlyInside) {
                long elapsedMs = System.currentTimeMillis() - timestamp;
                LOGGER.debug(
                        "[AREA] Player {} entered sound range at ({}, {}) - elapsed: {}ms",
                        player.getName().getString(),
                        laserX,
                        laserZ,
                        elapsedMs);
            } else if (wasInside && !currentlyInside) {
                LOGGER.debug(
                        "[AREA] Player {} left sound range at ({}, {})",
                        player.getName().getString(),
                        laserX,
                        laserZ);
            }
        }

        return result;
    }

    /**
     * Clears the state for a specific player (useful when player disconnects)
     */
    public static void clearPlayerState(UUID playerId) {
        playerStates.remove(playerId);
    }

    /**
     * Sets a callback to be invoked when a player's area state changes.
     */
    public static void setAreaChangeCallback(Consumer<AreaChangeEvent> callback) {
        areaChangeCallback = callback;
    }

    /**
     * Periodically checks if a player's position has changed relative to tracked laser locations.
     * This is called from a tick event to detect when players move out of range.
     */
    public static void checkPlayerPosition(ServerPlayerEntity player) {
        UUID playerId = player.getUuid();
        AreaState state = playerStates.get(playerId);

        if (state == null) {
            return;
        }

        boolean currentlyInside = isPlayerInRange(player, state.lastLaserX, state.lastLaserZ);

        if (state.isInside != currentlyInside) {
            if (ServerConfig.INSTANCE.isDebugMode()) {
                LOGGER.debug(
                        "[AREA] Player {} position changed relative to laser at ({}, {})",
                        player.getName().getString(),
                        state.lastLaserX,
                        state.lastLaserZ);
            }

            AreaCheckResult result = handlePlayerAreaCheck(player, state.lastLaserX, state.lastLaserZ);

            if (areaChangeCallback != null && result.hasStateChanged()) {
                areaChangeCallback.accept(
                        new AreaChangeEvent(player, result, state.lastLaserX, state.lastLaserZ));
            }
        }
    }

    /**
     * Event data for area state changes
     */
    public record AreaChangeEvent(
            ServerPlayerEntity player, AreaCheckResult result, double laserX, double laserZ) {
    }

    /**
     * Result of an area check, containing state information
     */
    public static class AreaCheckResult {
        public boolean isInside;
        public boolean wasInside;
        public boolean isNewLocation;
        public long fireTimestamp; // When the railgun was fired

        /**
         * @return true if the player just entered the area
         */
        public boolean hasEntered() {
            return isInside && !wasInside;
        }

        /**
         * @return true if the player just left the area
         */
        public boolean hasLeft() {
            return !isInside && wasInside;
        }

        /**
         * @return true if the player's state changed
         */
        public boolean hasStateChanged() {
            return hasEntered() || hasLeft();
        }
    }
}
