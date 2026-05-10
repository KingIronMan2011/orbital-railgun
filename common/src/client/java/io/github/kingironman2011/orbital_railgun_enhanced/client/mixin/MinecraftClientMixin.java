package io.github.kingironman2011.orbital_railgun_enhanced.client.mixin;

import io.github.kingironman2011.orbital_railgun_enhanced.client.compat.ClientAdapterLoader;
import io.github.kingironman2011.orbital_railgun_enhanced.client.compat.ClientVersionAdapter;
import io.github.kingironman2011.orbital_railgun_enhanced.item.IOrbitalRailgunItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.option.GameOptions;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercepts {@code MinecraftClient#handleInputEvents} to detect a left-click
 * while the player is aiming the orbital railgun and trigger the shot sequence.
 *
 * <p>All version-specific behavior (shader state, networking) is delegated to
 * {@link ClientVersionAdapter} so this class compiles cleanly against all
 * supported MC versions.
 */
@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Shadow @Final public GameOptions options;
    @Shadow @Nullable public ClientPlayerEntity player;
    @Shadow @Nullable public ClientPlayerInteractionManager interactionManager;

    @Inject(
            method = "handleInputEvents",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"))
    public void shootOnAttack(CallbackInfo ci) {
        ItemStack activeStack = player.getActiveItem();
        if (!(activeStack.getItem() instanceof IOrbitalRailgunItem railgun)) {
            return;
        }

        // Bail out immediately if the item is already on cooldown — this prevents
        // re-entering the fire sequence when handleInputEvents re-triggers use()
        // in the same tick after stopUsingItem() while the right-click is still held.
        if (this.player.getItemCooldownManager().isCoolingDown(activeStack.getItem())) {
            return;
        }

        ClientVersionAdapter adapter = ClientAdapterLoader.get();
        if (!this.options.attackKey.isPressed()) {
            return;
        }
        if (adapter.isAimActive()) {
            return;
        }

        HitResult hitResult = adapter.getGuiHitResult();
        if (hitResult == null
                || hitResult.getType() == HitResult.Type.MISS
                || !(hitResult instanceof BlockHitResult blockHit)) {
            return;
        }

        // Apply the cooldown BEFORE stopUsingItem() so that when MC immediately
        // re-invokes use() (right-click still held), isCoolingDown() already
        // returns true and the item refuses to enter the aiming state again.
        railgun.shoot(this.player);
        this.interactionManager.stopUsingItem(this.player);
        adapter.onShootFired(blockHit.getBlockPos(), this.player);
        adapter.sendShootPacket(activeStack, blockHit.getBlockPos(), this.player);
    }
}
