package io.github.kingironman2011.orbital_railgun_enhanced.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.authlib.GameProfile;
import io.github.kingironman2011.orbital_railgun_enhanced.item.OrbitalRailgunItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(net.minecraft.client.network.AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerEntity extends PlayerEntity {
    public AbstractClientPlayerEntity(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @ModifyExpressionValue(
            method = "getFovMultiplier",
            at =
            @At(
                    value = "INVOKE",
                    target =
                            "Lnet/minecraft/client/network/AbstractClientPlayerEntity;isUsingSpyglass()Z"))
    public boolean zoomInOnAim(boolean original) {
        return original || this.getActiveItem().getItem() instanceof OrbitalRailgunItem;
    }
}
