package io.github.kingironman2011.orbital_railgun_enhanced.client.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.kingironman2011.orbital_railgun_enhanced.client.OrbitalRailgunClient;
import io.github.kingironman2011.orbital_railgun_enhanced.registry.SoundsRegistry;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;

public class SoundsHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("OrbitalRailgunEnhanced");
    private static final Identifier ORBITAL_RAILGUN_ITEM_ID =
            new Identifier("orbital_railgun_enhanced", "orbital_railgun");

    private boolean wasUsing = false;
    private int lastSelectedSlot = -1;
    private boolean lastCooldownActive = false;
    private Item railgunItem;

    private PositionedSoundInstance scopeSoundInstance;

    public void initializeClient() {
        railgunItem = Registries.ITEM.get(ORBITAL_RAILGUN_ITEM_ID);
        ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);
        LOGGER.info("Client sounds handler initialized");
    }

    private void onEndTick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null) {
            return;
        }

        boolean focused = client.isWindowFocused();
        float volumeScope = focused ? (float) OrbitalRailgunClient.CONFIG.scopeVolume() : 0.0f;
        float volumeShoot = focused ? (float) OrbitalRailgunClient.CONFIG.shootVolume() : 0.0f;
        float volumeEquip = focused ? (float) OrbitalRailgunClient.CONFIG.equipVolume() : 0.0f;

        handleRailgunUsage(client, player, volumeScope);
        handleRailgunCooldown(player, volumeShoot);
        handleHotbarSwitch(player, volumeEquip);

        PacketByteBuf areaBuf = new PacketByteBuf(Unpooled.buffer());
        ClientPlayNetworking.send(SoundsRegistry.AREA_CHECK_PACKET_ID, areaBuf);
    }

    private void handleRailgunUsage(
            MinecraftClient client, ClientPlayerEntity player, float volumeScope) {
        Item currentItem = player.getActiveItem().getItem();
        boolean isUsingRailgun = !player.getActiveItem().isEmpty() && currentItem == railgunItem;

        if (isUsingRailgun) {
            if (!wasUsing && OrbitalRailgunClient.CONFIG.enableScopeSound()) {
                scopeSoundInstance =
                        new PositionedSoundInstance(
                                SoundsRegistry.SCOPE_ON.getId(),
                                SoundCategory.MASTER,
                                volumeScope,
                                1.0f,
                                SoundInstance.createRandom(),
                                false,
                                0,
                                SoundInstance.AttenuationType.NONE,
                                0.0,
                                0.0,
                                0.0,
                                true);
                client.getSoundManager().play(scopeSoundInstance);
            }
        } else {
            if (wasUsing && scopeSoundInstance != null) {
                client.getSoundManager().stop(scopeSoundInstance);
                scopeSoundInstance = null;
            }
        }

        wasUsing = isUsingRailgun;
    }

    private void handleRailgunCooldown(ClientPlayerEntity player, float volumeShoot) {
        if (railgunItem != null) {
            boolean cooldownNow = player.getItemCooldownManager().isCoolingDown(railgunItem);
            float pitchShoot = 1.0f;

            if (!lastCooldownActive && cooldownNow && OrbitalRailgunClient.CONFIG.enableShootSound()) {
                PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                buf.writeIdentifier(Registries.SOUND_EVENT.getId(SoundsRegistry.RAILGUN_SHOOT));
                buf.writeBlockPos(player.getBlockPos());
                buf.writeFloat(volumeShoot);
                buf.writeFloat(pitchShoot);

                ClientPlayNetworking.send(SoundsRegistry.PLAY_SOUND_PACKET_ID, buf);
            }

            lastCooldownActive = cooldownNow;
        }
    }

    private void handleHotbarSwitch(ClientPlayerEntity player, float volumeEquip) {
        int selected = player.getInventory().selectedSlot;
        if (lastSelectedSlot != selected) {
            Item heldItem = player.getMainHandStack().getItem();

            if (heldItem == railgunItem && OrbitalRailgunClient.CONFIG.enableEquipSound()) {
                player.playSound(SoundsRegistry.EQUIP, volumeEquip, 1.0f);
            }

            lastSelectedSlot = selected;
        }
    }
}
