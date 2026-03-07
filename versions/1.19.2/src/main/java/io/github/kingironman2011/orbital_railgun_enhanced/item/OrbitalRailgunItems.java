package io.github.kingironman2011.orbital_railgun_enhanced.item;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.kingironman2011.orbital_railgun_enhanced.OrbitalRailgun;
import io.github.kingironman2011.orbital_railgun_enhanced.config.ServerConfig;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.text.Text;

public class OrbitalRailgunItems {
    public static final String MOD_ID = "orbital_railgun_enhanced";
    private static final Logger LOGGER = LoggerFactory.getLogger("OrbitalRailgunEnhanced");
    public static final OrbitalRailgunItem ORBITAL_RAILGUN =
            (OrbitalRailgunItem) register(new OrbitalRailgunItem(), "orbital_railgun");
    public static final RegistryKey<ItemGroup> ORE_ITEM_GROUP_KEY = RegistryKey.of(Registries.ITEM_GROUP.getKey(), new Identifier(MOD_ID, "item_group"));
    public static final ItemGroup ORE_ITEM_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(ORBITAL_RAILGUN))
            .displayName(Text.translatable("itemGroup.orbital-railgun-enhanced"))
            .build();

    public static Item register(Item item, String id) {
        Identifier itemID = new Identifier(OrbitalRailgun.MOD_ID, id);
        Item registeredItem = Registry.register(Registries.ITEM, itemID, item);
        if (ServerConfig.INSTANCE.isDebugMode()) {
            LOGGER.debug("[REGISTRY] Registered item: {}", itemID);
        }
        return registeredItem;
    }

    public static void initialize() {
        LOGGER.info("Registering items...");
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
                .register(
                        fabricItemGroupEntries -> {
                            fabricItemGroupEntries.addAfter(Items.CROSSBOW, ORBITAL_RAILGUN);
                            if (ServerConfig.INSTANCE.isDebugMode()) {
                                LOGGER.debug("[REGISTRY] Added Orbital Railgun to COMBAT item group");
                            }
                        });
    }
}
