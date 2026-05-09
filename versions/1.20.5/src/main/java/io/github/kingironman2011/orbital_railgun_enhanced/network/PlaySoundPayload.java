package io.github.kingironman2011.orbital_railgun_enhanced.network;

import io.github.kingironman2011.orbital_railgun_enhanced.OrbitalRailgun;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record PlaySoundPayload(Identifier soundId, BlockPos blockPos, float volume, float pitch) implements CustomPayload {
    public static final Id<PlaySoundPayload> ID = new Id<>(OrbitalRailgun.PLAY_SOUND_PACKET_ID);
    public static final PacketCodec<PacketByteBuf, PlaySoundPayload> CODEC = PacketCodec.tuple(
            Identifier.PACKET_CODEC, PlaySoundPayload::soundId,
            BlockPos.PACKET_CODEC, PlaySoundPayload::blockPos,
            PacketCodecs.FLOAT, PlaySoundPayload::volume,
            PacketCodecs.FLOAT, PlaySoundPayload::pitch,
            PlaySoundPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
