package io.github.kingironman2011.orbital_railgun_enhanced.client.impl.v1204;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.kingironman2011.orbital_railgun_enhanced.OrbitalRailgun;
import io.github.kingironman2011.orbital_railgun_enhanced.impl.v1204.OrbitalRailgunItems;
import ladysnake.satin.api.event.PostWorldRenderCallback;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.util.math.BlockPos;
import software.bernie.geckolib.animatable.client.RenderProvider;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;

import io.github.kingironman2011.orbital_railgun_enhanced.client.compat.ClientVersionAdapter;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;

public class ClientVersionAdapterImpl implements ClientVersionAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger("OrbitalRailgunEnhanced");


    public void initialize() {
        LOGGER.info("Initializing Orbital Railgun Enhanced client...");

        LOGGER.info("Client configuration loaded");

        SoundsHandler sounds = new SoundsHandler();
        sounds.initializeClient();

        OrbitalRailgunItems.ORBITAL_RAILGUN.renderProviderHolder.setValue(
                new RenderProvider() {
                    private OrbitalRailgunRenderer renderer;


                    public BuiltinModelItemRenderer getCustomRenderer() {
                        if (this.renderer == null) {
                            this.renderer = new OrbitalRailgunRenderer();
                            LOGGER.info("Orbital railgun renderer created");
                        }

                        return this.renderer;
                    }
                });

        ClientPlayNetworking.registerGlobalReceiver(
                OrbitalRailgun.CLIENT_SYNC_PACKET_ID,
                ((minecraftClient, clientPlayNetworkHandler, packetByteBuf, packetSender) -> {
                    BlockPos blockPos = packetByteBuf.readBlockPos();

                    minecraftClient.execute(
                            () -> {
                                OrbitalRailgunShader.INSTANCE.blockPosition = blockPos.toCenterPos().toVector3f();
                                OrbitalRailgunShader.INSTANCE.dimension = minecraftClient.world.getRegistryKey();
                                LOGGER.debug("[CLIENT] Synced strike position: {}", blockPos);
                            });
                }));

        ClientPlayNetworking.registerGlobalReceiver(
                OrbitalRailgun.STOP_AREA_SOUND_PACKET_ID,
                (client, handler, buf, responseSender) -> {
                    Identifier soundId = buf.readIdentifier();

                    client.execute(
                            () -> {
                                // Stop all instances of this sound for the player
                                MinecraftClient.getInstance()
                                        .getSoundManager()
                                        .stopSounds(soundId, SoundCategory.PLAYERS);
                                LOGGER.debug("[CLIENT] Stopped area sound: {}", soundId);
                            });
                });

        // Register handler for stopping animation when player leaves range
        ClientPlayNetworking.registerGlobalReceiver(OrbitalRailgun.STOP_ANIMATION_PACKET_ID,
                (client, handler, buf, responseSender) -> {
                    client.execute(() -> {
                        // Stop the orbital railgun shader animation
                        OrbitalRailgunShader.INSTANCE.stopAnimation();
                        LOGGER.debug("[CLIENT] Stopped animation due to leaving range");
                    });
                });

        ClientTickEvents.END_CLIENT_TICK.register(OrbitalRailgunGuiShader.INSTANCE);
        PostWorldRenderCallback.EVENT.register(OrbitalRailgunGuiShader.INSTANCE);

        ClientTickEvents.END_CLIENT_TICK.register(OrbitalRailgunShader.INSTANCE);
        PostWorldRenderCallback.EVENT.register(OrbitalRailgunShader.INSTANCE);

        // done
    }


    @Override
    public boolean isAimActive() {
        return OrbitalRailgunShader.INSTANCE.blockPosition != null;
    }

    @Override
    public net.minecraft.util.hit.HitResult getGuiHitResult() {
        return OrbitalRailgunGuiShader.INSTANCE.hitResult;
    }

    @Override
    public void onShootFired(net.minecraft.util.math.BlockPos blockPos,
                             net.minecraft.client.network.ClientPlayerEntity player) {
        OrbitalRailgunShader.INSTANCE.blockPosition = blockPos.toCenterPos().toVector3f();
        OrbitalRailgunShader.INSTANCE.dimension = player.getWorld().getRegistryKey();
    }

    @Override
    public void sendShootPacket(ItemStack stack, BlockPos blockPos, ClientPlayerEntity player) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeItemStack(stack);
        buf.writeBlockPos(blockPos);
        ClientPlayNetworking.send(OrbitalRailgun.SHOOT_PACKET_ID, buf);
    }
}
