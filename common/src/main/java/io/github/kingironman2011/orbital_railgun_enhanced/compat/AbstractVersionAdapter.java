package io.github.kingironman2011.orbital_railgun_enhanced.compat;

import net.minecraft.server.network.ServerPlayerEntity;

import io.github.kingironman2011.orbital_railgun_enhanced.OrbitalRailgun;
import io.github.kingironman2011.orbital_railgun_enhanced.config.ServerConfig;
import io.github.kingironman2011.orbital_railgun_enhanced.listener.PlayerAreaListener;
import io.github.kingironman2011.orbital_railgun_enhanced.logger.SoundLogger;

/**
 * Abstract base class for all {@link VersionAdapter} implementations.
 *
 * <p>Contains shared server-side logic (area-state handling, enter/leave sound
 * orchestration) that is identical across all MC version subprojects.
 * Version-specific actions — playing sounds, sending network packets — are delegated
 * to abstract hook methods implemented by each concrete subclass.
 */
public abstract class AbstractVersionAdapter implements VersionAdapter {

    // ── Shared area-state machine ─────────────────────────────────────────────

    /**
     * Reacts to a player entering or leaving the orbital-strike sound area.
     * Call this from the networking handler in each concrete adapter whenever
     * a player's area state changes.
     */
    protected void handleAreaStateChange(
            ServerPlayerEntity player,
            PlayerAreaListener.AreaCheckResult result,
            double laserX,
            double laserZ) {

        if (result.hasEntered()) {
            long elapsedMs = System.currentTimeMillis() - result.fireTimestamp;

            if (ServerConfig.INSTANCE.isDebugMode()) {
                OrbitalRailgun.LOGGER.info(
                        "[AREA] Player {} entered sound range at ({}, {})"
                        + " - elapsed: {}ms, duration: {}ms",
                        player.getName().getString(),
                        laserX, laserZ,
                        elapsedMs,
                        OrbitalRailgun.RAILGUN_SOUND_DURATION_MS);
            }

            SoundLogger.logPlayerEnterRange(
                    player.getName().getString(),
                    Math.sqrt(player.squaredDistanceTo(laserX, player.getY(), laserZ)));

            if (elapsedMs < OrbitalRailgun.RAILGUN_SOUND_DURATION_MS) {
                playRailgunSoundToPlayer(player, laserX, laserZ, elapsedMs);
            } else if (ServerConfig.INSTANCE.isDebugMode()) {
                OrbitalRailgun.LOGGER.info(
                        "[AREA] Sound already ended ({}ms > {}ms) - not playing for player {}",
                        elapsedMs,
                        OrbitalRailgun.RAILGUN_SOUND_DURATION_MS,
                        player.getName().getString());
            }

        } else if (result.hasLeft()) {
            if (ServerConfig.INSTANCE.isDebugMode()) {
                OrbitalRailgun.LOGGER.info(
                        "[AREA] Player {} left sound range at ({}, {})"
                        + " - stopping sounds and animation",
                        player.getName().getString(),
                        laserX, laserZ);
            }

            SoundLogger.logPlayerExitRange(player.getName().getString());
            stopAreaSoundsForPlayer(player);
            stopAnimationForPlayer(player);

        } else if (result.isInside && ServerConfig.INSTANCE.isDebugMode()) {
            OrbitalRailgun.LOGGER.debug(
                    "[AREA] Player {} remains in sound range at ({}, {})",
                    player.getName().getString(),
                    laserX, laserZ);
        }
    }

    // ── Abstract hooks (implemented per MC version) ───────────────────────────

    /**
     * Plays the railgun shoot sound to {@code player}, offset by {@code elapsedMs}
     * to account for late entry into the sound range.
     *
     * <p>Implementations must use the version-appropriate {@code player.playSound()}
     * overload and reference the version-specific {@code SoundsRegistry.RAILGUN_SHOOT}.
     */
    protected abstract void playRailgunSoundToPlayer(
            ServerPlayerEntity player, double laserX, double laserZ, long elapsedMs);

    /**
     * Sends a packet/payload to {@code player} instructing the client to stop
     * area-based looping sounds.
     */
    protected abstract void stopAreaSoundsForPlayer(ServerPlayerEntity player);

    /**
     * Sends a packet/payload to {@code player} instructing the client to stop
     * the strike animation / shader.
     */
    protected abstract void stopAnimationForPlayer(ServerPlayerEntity player);
}
