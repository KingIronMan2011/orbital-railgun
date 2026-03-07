package io.github.kingironman2011.orbital_railgun_enhanced.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.kingironman2011.orbital_railgun_enhanced.config.ServerConfig;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class SoundsRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger("OrbitalRailgunEnhanced");
    public static final String MOD_ID = "orbital_railgun_enhanced";
    public static final Identifier PLAY_SOUND_PACKET_ID = new Identifier(MOD_ID, "play_sound");
    public static final Identifier AREA_CHECK_PACKET_ID =
            new Identifier("orbital_railgun", "area_check_packet");

    public static final Identifier RAILGUN_SHOOT_ID = new Identifier(MOD_ID, "railgun_shoot");
    public static final Identifier SCOPE_ON_ID = new Identifier(MOD_ID, "scope_on");
    public static final Identifier EQUIP_ID = new Identifier(MOD_ID, "equip");

    public static final SoundEvent RAILGUN_SHOOT = registerSoundEvent(RAILGUN_SHOOT_ID);
    public static final SoundEvent SCOPE_ON = registerSoundEvent(SCOPE_ON_ID);
    public static final SoundEvent EQUIP = registerSoundEvent(EQUIP_ID);

    public static void initialize() {
        LOGGER.info("Registering sound events...");
        if (ServerConfig.INSTANCE.isDebugMode()) {
            LOGGER.debug("[REGISTRY] Registered the railgun shoot sound: {}", RAILGUN_SHOOT_ID);
            LOGGER.debug("[REGISTRY] Registered the scope on sound: {}", SCOPE_ON_ID);
            LOGGER.debug("[REGISTRY] Registered the equip sound: {}", EQUIP_ID);
        }
    }

    /**
     * Helper method to register a sound event.
     *
     * @param id The Identifier of the sound event.
     * @return The registered SoundEvent.
     */
    private static SoundEvent registerSoundEvent(Identifier id) {
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }
}
