package io.github.kingironman2011.orbital_railgun_enhanced.compat;

/**
 * Version-specific server-side initialization delegate.
 *
 * <p>One implementation exists per supported Minecraft version range and is
 * loaded lazily at runtime by {@link AdapterLoader} so that class references
 * to version-specific Minecraft APIs are never resolved on incompatible versions.
 */
public interface VersionAdapter {

    /**
     * Performs all server-side, version-specific initialization:
     * sound &amp; item registration, command registration, networking
     * payload / packet-receiver registration, tick-event setup, etc.
     *
     * <p>Called exactly once from {@code OrbitalRailgun#onInitialize()}.
     */
    void initialize();
}
