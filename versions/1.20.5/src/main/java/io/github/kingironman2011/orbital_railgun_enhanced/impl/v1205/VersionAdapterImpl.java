package io.github.kingironman2011.orbital_railgun_enhanced.impl.v1205;

import io.github.kingironman2011.orbital_railgun_enhanced.logger.SoundLogger;
import io.github.kingironman2011.orbital_railgun_enhanced.config.ServerConfig;
import io.github.kingironman2011.orbital_railgun_enhanced.listener.PlayerAreaListener;
import io.github.kingironman2011.orbital_railgun_enhanced.network.PlaySoundPayload;
import io.github.kingironman2011.orbital_railgun_enhanced.network.ShootPayload;
import io.github.kingironman2011.orbital_railgun_enhanced.network.ClientSyncPayload;
import io.github.kingironman2011.orbital_railgun_enhanced.network.StopAreaSoundPayload;
import io.github.kingironman2011.orbital_railgun_enhanced.network.StopAnimationPayload;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.registry.Registries;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Box;
import net.minecraft.util.Identifier;

import java.util.List;

import io.github.kingironman2011.orbital_railgun_enhanced.compat.AbstractVersionAdapter;
import io.github.kingironman2011.orbital_railgun_enhanced.OrbitalRailgun;

public class VersionAdapterImpl extends AbstractVersionAdapter {




    public void initialize() {
        // version-specific init

        ServerConfig.INSTANCE.loadConfig();
        if (ServerConfig.INSTANCE.isDebugMode()) {
            OrbitalRailgun.LOGGER.info("[DEBUG] Debug mode is enabled");
        }

        SoundsRegistry.initialize();
        OrbitalRailgun.LOGGER.info("Sounds registry initialized");

        CommandRegistry.registerCommands();
        OrbitalRailgun.LOGGER.info("Commands registered: /ore and /orbitalrailgun");

        OrbitalRailgunItems.initialize();
        OrbitalRailgun.LOGGER.info("Items registered");

        OrbitalRailgunStrikeManager.initialize();
        OrbitalRailgun.LOGGER.info("Strike manager initialized");

        // Register C2S (Client-to-Server) payload types
        PayloadTypeRegistry.playC2S().register(PlaySoundPayload.ID, PlaySoundPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ShootPayload.ID, ShootPayload.CODEC);
        OrbitalRailgun.LOGGER.info("Client-to-server network payloads registered");

        // Register S2C (Server-to-Client) payload types
        PayloadTypeRegistry.playS2C().register(ClientSyncPayload.ID, ClientSyncPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(StopAreaSoundPayload.ID, StopAreaSoundPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(StopAnimationPayload.ID, StopAnimationPayload.CODEC);
        OrbitalRailgun.LOGGER.info("Server-to-client network payloads registered");

        PlayerAreaListener.setAreaChangeCallback(
                event ->
                        handleAreaStateChange(event.player(), event.result(), event.laserX(), event.laserZ()));

        if (ServerConfig.INSTANCE.isDebugMode()) {
            OrbitalRailgun.LOGGER.info("Registered player area change callback");
        }

        ServerPlayConnectionEvents.DISCONNECT.register(
                (handler, server) -> {
                    PlayerAreaListener.clearPlayerState(handler.getPlayer().getUuid());
                    if (ServerConfig.INSTANCE.isDebugMode()) {
                        OrbitalRailgun.LOGGER.info(
                                "[NETWORK] Cleared area state for disconnected player: {}",
                                handler.getPlayer().getName().getString());
                    }
                });

        ServerPlayNetworking.registerGlobalReceiver(
                PlaySoundPayload.ID,
                (payload, context) -> {
                    Identifier soundId = payload.soundId();
                    SoundEvent sound = Registries.SOUND_EVENT.get(soundId);
                    BlockPos blockPos = payload.blockPos();
                    float volumeShoot = payload.volume();
                    float pitchShoot = payload.pitch();

                    long fireTimestamp = System.currentTimeMillis();

                    context.player().server.execute(
                            () -> {
                                if (sound == null) {
                                    OrbitalRailgun.LOGGER.warn(
                                            "[NETWORK] Received unknown sound id: {}", soundId.toString());
                                    return;
                                }

                                double range = ServerConfig.INSTANCE.getSoundRange();
                                double rangeSquared = range * range;
                                double laserX = blockPos.getX() + 0.5;
                                double laserZ = blockPos.getZ() + 0.5;

                                if (ServerConfig.INSTANCE.isDebugMode()) {
                                    OrbitalRailgun.LOGGER.info(
                                            "[NETWORK] Received PLAY_SOUND_PACKET from player: {}",
                                            context.player().getName().getString());
                                    OrbitalRailgun.LOGGER.info(
                                            "[NETWORK] Playing sound {} at BlockPos: {} with range: {} at time {}",
                                            soundId,
                                            blockPos,
                                            range,
                                            fireTimestamp);
                                }

                                // Check all players and track state changes
                                context.player().server
                                        .getPlayerManager()
                                        .getPlayerList()
                                        .forEach(
                                                nearbyPlayer -> {
                                                    double distanceSquared =
                                                            nearbyPlayer.squaredDistanceTo(
                                                                    blockPos.getX() + 0.5,
                                                                    blockPos.getY() + 0.5,
                                                                    blockPos.getZ() + 0.5);

                                                    PlayerAreaListener.AreaCheckResult result =
                                                            PlayerAreaListener.handlePlayerAreaCheck(
                                                                    nearbyPlayer, laserX, laserZ, fireTimestamp);
                                                    if (distanceSquared <= rangeSquared) {
                                                        // Use PlayerAreaListener to track state changes with timestamp

                                                        if (result.isInside) {
                                                            // Only play sound if player is in range
                                                            nearbyPlayer.playSound(
                                                                    sound, volumeShoot, pitchShoot);
                                                            SoundLogger.logSoundEvent(soundId.toString(), blockPos, range);
                                                            SoundLogger.logSoundPlayed(
                                                                    nearbyPlayer.getName().getString(),
                                                                    soundId.toString(),
                                                                    volumeShoot,
                                                                    pitchShoot);

                                                            if (ServerConfig.INSTANCE.isDebugMode()) {
                                                                OrbitalRailgun.LOGGER.info("[SOUND] Played {} to {} (distance {})",
                                                                        soundId, nearbyPlayer.getName().getString(), Math.sqrt(distanceSquared));
                                                            }
                                                            if (ServerConfig.INSTANCE.isDebugMode()) {
                                                                OrbitalRailgun.LOGGER.info(
                                                                        "[SOUND] Playing sound to player {} (distance: {})",
                                                                        nearbyPlayer.getName().getString(),
                                                                        Math.sqrt(distanceSquared));
                                                            }
                                                        }

                                                        // Handle state changes (enter/leave detection)
                                                        handleAreaStateChange(nearbyPlayer, result, laserX, laserZ);
                                                    } else {
                                                        // Player is outside range - check if they left the zone

                                                        if (result.hasLeft()) {
                                                            // Player just left the range
                                                            handleAreaStateChange(nearbyPlayer, result, laserX, laserZ);
                                                        }
                                                    }
                                                });
                            });
                });

        ServerPlayNetworking.registerGlobalReceiver(
                ShootPayload.ID,
                (payload, context) -> {
                    OrbitalRailgunItem orbitalRailgun = (OrbitalRailgunItem) payload.itemStack().getItem();
                    BlockPos blockPos = payload.blockPos();

                    if (ServerConfig.INSTANCE.isDebugMode()) {
                        OrbitalRailgun.LOGGER.info("========================================");
                        OrbitalRailgun.LOGGER.info(
                                "[NETWORK] SHOOT_PACKET received from player: {}", context.player().getName().getString());
                        OrbitalRailgun.LOGGER.info("[STRIKE] Impact location: {}", blockPos);
                    }

                    context.player().server.execute(
                            () -> {
                                double laserX = blockPos.getX() + 0.5;
                                double laserZ = blockPos.getZ() + 0.5;

                                orbitalRailgun.shoot(context.player());

                                if (ServerConfig.INSTANCE.isDebugMode()) {
                                    OrbitalRailgun.LOGGER.info("[STRIKE] Orbital railgun fired at ({}, {})", laserX, laserZ);
                                }

                                double range = ServerConfig.INSTANCE.getSoundRange();

                                List<Entity> nearby = context.player().getWorld().getOtherEntities(null, Box.of(blockPos.toCenterPos(), range, range, range));
                                OrbitalRailgunStrikeManager.activeStrikes.put(
                                        new Pair<>(blockPos, nearby),
                                        new Pair<>(context.player().server.getTicks(), context.player().getWorld().getRegistryKey()));

                                if (ServerConfig.INSTANCE.isDebugMode()) {
                                    OrbitalRailgun.LOGGER.info("[STRIKE] Registered strike with {} nearby entities within range {}", nearby.size(), range);
                                }

                                nearby.forEach(entity -> {
                                    if (entity instanceof ServerPlayerEntity serverPlayer) {
                                        if (PlayerAreaListener.isPlayerInRange(serverPlayer, laserX, laserZ)) {
                                            serverPlayer.playSound(SoundsRegistry.RAILGUN_SHOOT, 1.0f, 1.0f);
                                            ServerPlayNetworking.send(serverPlayer, new ClientSyncPayload(blockPos));
                                            if (ServerConfig.INSTANCE.isDebugMode()) {
                                                double distSq = serverPlayer.squaredDistanceTo(laserX, serverPlayer.getY(), laserZ);
                                                OrbitalRailgun.LOGGER.debug("[NETWORK] Sent CLIENT_SYNC + played sound to {} (distSq={})", serverPlayer.getName().getString(), distSq);
                                            }
                                        } else {
                                            if (ServerConfig.INSTANCE.isDebugMode()) {
                                                OrbitalRailgun.LOGGER.debug("[NETWORK] Skipped CLIENT_SYNC for {} (outside range {})", serverPlayer.getName().getString(), range);
                                            }
                                        }
                                    }
                                });

                                int totalPlayers = context.player().server.getPlayerManager().getPlayerList().size();
                                if (ServerConfig.INSTANCE.isDebugMode()) {
                                    OrbitalRailgun.LOGGER.info("[STRIKE] Checking {} players on server for range", totalPlayers);
                                }

                                context.player().server
                                        .getPlayerManager()
                                        .getPlayerList()
                                        .forEach(
                                                serverPlayer -> {
                                                    PlayerAreaListener.AreaCheckResult result =
                                                            PlayerAreaListener.handlePlayerAreaCheck(
                                                                    serverPlayer, laserX, laserZ);

                                                    handleAreaStateChange(serverPlayer, result, laserX, laserZ);
                                                });

                                if (ServerConfig.INSTANCE.isDebugMode()) {
                                    OrbitalRailgun.LOGGER.info("========================================");
                                }
                            });
                });

        ServerTickEvents.END_SERVER_TICK.register(
                server -> {
                    if (server.getTicks() % 20 == 0) {
                        server
                                .getPlayerManager()
                                .getPlayerList()
                                .forEach(PlayerAreaListener::checkPlayerPosition);
                    }
                });

        ServerTickEvents.END_SERVER_TICK.register(OrbitalRailgunStrikeManager::tick);

        // done
    }


    /**
     * Plays the railgun shoot sound to a specific player at the laser impact location.
     *
     * @param elapsedMs How many milliseconds have elapsed since the sound started (for syncing)
     */
    @Override
    protected void playRailgunSoundToPlayer(
            ServerPlayerEntity player, double laserX, double laserZ, long elapsedMs) {
        // Use the railgun shoot sound from the registry
        SoundEvent shootSound = SoundsRegistry.RAILGUN_SHOOT;

        if (shootSound != null) {
            // Play the sound at the laser impact location
            player.playSound(
                    shootSound,
                    1.0f, // volume
                    1.0f // pitch
            );

            SoundLogger.logSoundPlayed(
                    player.getName().getString(), SoundsRegistry.RAILGUN_SHOOT_ID.toString(), 1.0f, 1.0f);

            if (ServerConfig.INSTANCE.isDebugMode()) {
                OrbitalRailgun.LOGGER.debug("[SOUND] playRailgunSoundToPlayer called for {} at ({}, {}) with offset {}ms",
                        player.getName().getString(), laserX, laserZ, elapsedMs);
            }
        } else {
            OrbitalRailgun.LOGGER.warn("[SOUND] Railgun shoot sound not found in registry");
        }
    }

    /**
     * Sends a packet to the client to stop area-based sounds.
     */
    @Override
    protected void stopAreaSoundsForPlayer(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, new StopAreaSoundPayload(SoundsRegistry.RAILGUN_SHOOT_ID));

        SoundLogger.logSoundStopped(
                player.getName().getString(), SoundsRegistry.RAILGUN_SHOOT_ID.toString());

        if (ServerConfig.INSTANCE.isDebugMode()) {
            OrbitalRailgun.LOGGER.info("[NETWORK] Sent stop sound packet to player {}", player.getName().getString());
        }
    }

    /**
     * Sends a packet to the client to stop the orbital railgun animation/shader.
     */
    @Override
    protected void stopAnimationForPlayer(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, new StopAnimationPayload());

        if (ServerConfig.INSTANCE.isDebugMode()) {
            OrbitalRailgun.LOGGER.info("[NETWORK] Sent stop animation packet to player {}", player.getName().getString());
        }
    }
}
