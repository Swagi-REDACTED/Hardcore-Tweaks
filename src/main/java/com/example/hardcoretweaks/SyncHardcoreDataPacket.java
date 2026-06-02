package com.example.hardcoretweaks;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SyncHardcoreDataPacket(
    boolean spawnPenalty,
    boolean deathLimitActive,
    boolean deathTrackerActive,
    boolean allowSetSpawn,
    int spawnPenaltyRange,
    int deathLimitValue,
    int deathLimitBase,
    boolean canEditDeathLimit,
    boolean firstTimeSetupDone,
    int playerDeaths
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncHardcoreDataPacket> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("hardcore-tweaks", "sync_data"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncHardcoreDataPacket> STREAM_CODEC = StreamCodec.of(
        (buf, packet) -> packet.write(buf),
        SyncHardcoreDataPacket::new
    );

    public SyncHardcoreDataPacket(RegistryFriendlyByteBuf buf) {
        this(
            buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(),
            buf.readInt(), buf.readInt(), buf.readInt(), buf.readBoolean(),
            buf.readBoolean(), buf.readInt()
        );
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(this.spawnPenalty);
        buf.writeBoolean(this.deathLimitActive);
        buf.writeBoolean(this.deathTrackerActive);
        buf.writeBoolean(this.allowSetSpawn);
        buf.writeInt(this.spawnPenaltyRange);
        buf.writeInt(this.deathLimitValue);
        buf.writeInt(this.deathLimitBase);
        buf.writeBoolean(this.canEditDeathLimit);
        buf.writeBoolean(this.firstTimeSetupDone);
        buf.writeInt(this.playerDeaths);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
