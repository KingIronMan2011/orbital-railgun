package io.github.kingironman2011.orbital_railgun_enhanced.client.impl.v1205.mixin;

import io.github.kingironman2011.orbital_railgun_enhanced.client.impl.v1205.OrbitalRailgunGuiShader;
import io.github.kingironman2011.orbital_railgun_enhanced.client.impl.v1205.OrbitalRailgunShader;
import io.github.kingironman2011.orbital_railgun_enhanced.impl.v1205.OrbitalRailgunItem;
import io.github.kingironman2011.orbital_railgun_enhanced.network.ShootPayload;
import io.github.kingironman2011.orbital_railgun_enhanced.impl.v1205.SoundsRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.option.GameOptions;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Shadow
    @Final
    public GameOptions options;

    @Shadow
    @Nullable
    public ClientPlayerEntity player;

    @Shadow
    @Nullable
    public ClientPlayerInteractionManager interactionManager;

    @Inject(
            method = "handleInputEvents",
            at =
            @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"))
    public void shootOnAttack(CallbackInfo ci) {
        if (player.getActiveItem().getItem() instanceof OrbitalRailgunItem orbitalRailgun
                && this.options.attackKey.isPressed()
                && OrbitalRailgunShader.INSTANCE.blockPosition == null) {
            HitResult hitResult = OrbitalRailgunGuiShader.INSTANCE.hitResult;
            if (hitResult.getType() != HitResult.Type.MISS
                    && hitResult instanceof BlockHitResult blockHitResult) {
                this.interactionManager.stopUsingItem(this.player);
                orbitalRailgun.shoot(this.player);
                OrbitalRailgunShader.INSTANCE.blockPosition =
                        blockHitResult.getBlockPos().toCenterPos().toVector3f();
                OrbitalRailgunShader.INSTANCE.dimension = player.getWorld().getRegistryKey();

                // Play shoot sound locally for the shooter so they always hear it
                try {
                    player.playSound(SoundsRegistry.RAILGUN_SHOOT, 1.0f, 1.0f);
                } catch (Exception ignored) {
                    // Ignored
                }

                ClientPlayNetworking.send(new ShootPayload(
                        orbitalRailgun.getDefaultStack(),
                        blockHitResult.getBlockPos()
                ));
            }
        }
    }
}
