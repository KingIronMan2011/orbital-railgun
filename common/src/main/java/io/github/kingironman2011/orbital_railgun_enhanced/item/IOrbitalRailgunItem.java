package io.github.kingironman2011.orbital_railgun_enhanced.item;

import net.minecraft.entity.player.PlayerEntity;

/**
 * Marker interface implemented by every version-specific {@code OrbitalRailgunItem}.
 *
 * <p>Common code (e.g. mixins) that needs to identify or interact with the railgun
 * item without importing a version-specific class uses this interface.
 */
public interface IOrbitalRailgunItem {

    /**
     * Triggers the railgun shot sequence on the given player.
     * Called from {@code MinecraftClientMixin} on the client when the player
     * left-clicks while aiming.
     */
    void shoot(PlayerEntity player);
}
