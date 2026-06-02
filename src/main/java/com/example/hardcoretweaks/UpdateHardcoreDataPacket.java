package com.example.hardcoretweaks;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record UpdateHardcoreDataPacket(
    boolean spawnPenalty,
    boolean deathLimitActive,
    boolean deathTrackerActive,
    boolean allowSetSpawn,
    int spawnPenaltyRange,
    int deathLimitValue,
    boolean canEditDeathLimit,
    boolean firstTimeSetupDone
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UpdateHardcoreDataPacket> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("hardcore-tweaks", "update_data"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateHardcoreDataPacket> STREAM_CODEC = StreamCodec.of(
        (buf, packet) -> packet.write(buf),
        UpdateHardcoreDataPacket::new
    );

    public UpdateHardcoreDataPacket(RegistryFriendlyByteBuf buf) {
        this(
            buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(),
            buf.readInt(), buf.readInt(), buf.readBoolean(), buf.readBoolean()
        );
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(this.spawnPenalty);
        buf.writeBoolean(this.deathLimitActive);
        buf.writeBoolean(this.deathTrackerActive);
        buf.writeBoolean(this.allowSetSpawn);
        buf.writeInt(this.spawnPenaltyRange);
        buf.writeInt(this.deathLimitValue);
        buf.writeBoolean(this.canEditDeathLimit);
        buf.writeBoolean(this.firstTimeSetupDone);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
