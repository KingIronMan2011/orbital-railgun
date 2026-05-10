package io.github.kingironman2011.orbital_railgun_enhanced.client.compat;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

/**
 * Version-specific client-side initialisation delegate.
 *
 * <p>Also exposes per-version shader state and networking operations so the
 * common {@code MinecraftClientMixin} can interact with them without importing
 * any version-specific classes directly.
 *
 * @see ClientAdapterLoader
 */
public interface ClientVersionAdapter {

    /**
     * Performs all client-side, version-specific initialization:
     * renderer registration, networking handler registration,
     * shader setup, sound handler setup, etc.
     *
     * <p>Called exactly once from {@code OrbitalRailgunClient#onInitializeClient()}.
     */
    void initialize();

    // ── Mixin hooks ──────────────────────────────────────────────────────────

    /**
     * Returns {@code true} when the strike shader is currently tracking an
     * active aim (i.e. the shader's BlockPosition field is non-null).
     * Used by {@code MinecraftClientMixin} to gate shoot input.
     */
    boolean isAimActive();

    /**
     * Returns the current GUI targeting hit-result computed by the GUI shader's
     * tick callback. Used by {@code MinecraftClientMixin} to resolve the aimed
     * block position.
     */
    HitResult getGuiHitResult();

    /**
     * Called immediately after the player fires so each version can update its
     * shader state (block position and world dimension) using the appropriate API.
     *
     * @param blockPos  the aimed block position
     * @param player    the firing client player (used to retrieve world key)
     */
    void onShootFired(BlockPos blockPos, ClientPlayerEntity player);

    /**
     * Sends the shoot packet/payload to the server using the version-appropriate
     * networking API (raw {@code PacketByteBuf} on 1.20.4, typed
     * {@code ShootPayload} on 1.20.5+).
     *
     * @param stack    the railgun item stack
     * @param blockPos the aimed block position
     * @param player   the firing client player
     */
    void sendShootPacket(ItemStack stack, BlockPos blockPos, ClientPlayerEntity player);
}
