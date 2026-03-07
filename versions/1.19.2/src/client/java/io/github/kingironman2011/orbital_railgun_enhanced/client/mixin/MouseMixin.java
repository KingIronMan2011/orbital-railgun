package io.github.kingironman2011.orbital_railgun_enhanced.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.kingironman2011.orbital_railgun_enhanced.item.OrbitalRailgunItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Mouse.class)
public class MouseMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @ModifyExpressionValue(
            method = "updateMouse",
            at =
            @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingSpyglass()Z"))
    public boolean smoothCursorOnAim(boolean original) {
        return original || this.client.player.getActiveItem().getItem() instanceof OrbitalRailgunItem;
    }
}
