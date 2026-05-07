package io.github.kingironman2011.orbital_railgun_enhanced.client.compat;

/**
 * Version-specific client-side initialisation delegate.
 *
 * @see io.github.kingironman2011.orbital_railgun_enhanced.compat.VersionAdapter
 */
public interface ClientVersionAdapter {

    /**
     * Performs all client-side, version-specific initialisation:
     * renderer registration, networking handler registration,
     * shader setup, sound handler setup, etc.
     *
     * <p>Called exactly once from {@code OrbitalRailgunClient#onInitializeClient()}.
     */
    void initialize();
}
