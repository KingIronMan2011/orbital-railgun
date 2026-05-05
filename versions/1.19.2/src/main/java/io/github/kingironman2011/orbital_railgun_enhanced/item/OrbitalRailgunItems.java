package io.github.kingironman2011.orbital_railgun_enhanced.item;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.kingironman2011.orbital_railgun_enhanced.OrbitalRailgun;
import io.github.kingironman2011.orbital_railgun_enhanced.config.ServerConfig;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.Identifier;

public class OrbitalRailgunItems {
    public static final String MOD_ID = "orbital_railgun_enhanced";
    private static final Logger LOGGER = LoggerFactory.getLogger("OrbitalRailgunEnhanced");
    public static final OrbitalRailgunItem ORBITAL_RAILGUN =
            (OrbitalRailgunItem) register(new OrbitalRailgunItem(), "orbital_railgun");

    public static Item register(Item item, String id) {
        Identifier itemID = new Identifier(OrbitalRailgun.MOD_ID, id);
        Item registeredItem = Registry.register(Registry.ITEM, itemID, item);
        if (ServerConfig.INSTANCE.isDebugMode()) {
            LOGGER.debug("[REGISTRY] Registered item: {}", itemID);
        }
        return registeredItem;
    }

    public static void initialize() {
        LOGGER.info("Registering items...");
    }
}
