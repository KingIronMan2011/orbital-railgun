package io.github.kingironman2011.orbital_railgun_enhanced.network;

import io.github.kingironman2011.orbital_railgun_enhanced.OrbitalRailgun;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record StopAnimationPayload() implements CustomPayload {
    public static final Id<StopAnimationPayload> ID = new Id<>(OrbitalRailgun.STOP_ANIMATION_PACKET_ID);
    public static final PacketCodec<PacketByteBuf, StopAnimationPayload> CODEC = PacketCodec.unit(new StopAnimationPayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
