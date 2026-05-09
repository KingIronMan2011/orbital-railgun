package io.github.kingironman2011.orbital_railgun_enhanced.network;

import io.github.kingironman2011.orbital_railgun_enhanced.OrbitalRailgun;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record ShootPayload(ItemStack itemStack, BlockPos blockPos) implements CustomPayload {
    public static final Id<ShootPayload> ID = new Id<>(OrbitalRailgun.SHOOT_PACKET_ID);
    public static final PacketCodec<RegistryByteBuf, ShootPayload> CODEC = PacketCodec.tuple(
            ItemStack.PACKET_CODEC, ShootPayload::itemStack,
            BlockPos.PACKET_CODEC, ShootPayload::blockPos,
            ShootPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
