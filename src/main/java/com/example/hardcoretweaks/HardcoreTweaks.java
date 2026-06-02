package com.example.hardcoretweaks;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HardcoreTweaks implements ModInitializer {
    public static final String MOD_ID = "hardcore-tweaks";
    public static final Logger LOGGER = LoggerFactory.getLogger("HardcoreTweaks");

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Hardcore Tweaks");

        PayloadTypeRegistry.serverboundPlay().register(UpdateHardcoreDataPacket.TYPE, UpdateHardcoreDataPacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SyncHardcoreDataPacket.TYPE, SyncHardcoreDataPacket.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(UpdateHardcoreDataPacket.TYPE, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayer player = context.player();
                if (player.level().getServer().getPlayerList().isOp(player.nameAndId()) || player.level().getServer().isSingleplayerOwner(player.nameAndId())) { // Require OP permission to edit world settings
                    HardcoreWorldData data = HardcoreWorldData.get(player.level());
                    
                    // If death limit is newly toggled ON, or the value changed while active, update the base!
                    boolean newlyActivated = payload.deathLimitActive() && !data.deathLimitActive;
                    boolean valueChangedWhileActive = payload.deathLimitActive() && data.deathLimitActive && payload.deathLimitValue() != data.deathLimitValue;
                    
                    if (newlyActivated || valueChangedWhileActive) {
                        data.deathLimitBase = data.getPlayerDeaths(player.getUUID());
                    }

                    data.spawnPenalty = payload.spawnPenalty();
                    data.deathLimitActive = payload.deathLimitActive();
                    data.deathTrackerActive = payload.deathTrackerActive();
                    data.allowSetSpawn = payload.allowSetSpawn();
                    data.spawnPenaltyRange = payload.spawnPenaltyRange();
                    data.deathLimitValue = payload.deathLimitValue();
                    data.canEditDeathLimit = payload.canEditDeathLimit();
                    data.firstTimeSetupDone = payload.firstTimeSetupDone();
                    data.setDirty();

                    syncDataToPlayer(player);
                }
            });
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            syncDataToPlayer(handler.getPlayer());
        });
    }

    public static void syncDataToPlayer(ServerPlayer player) {
        try {
            HardcoreWorldData data = HardcoreWorldData.get(player.level());
            ServerPlayNetworking.send(player, new SyncHardcoreDataPacket(
                data.spawnPenalty, data.deathLimitActive, data.deathTrackerActive, data.allowSetSpawn,
                data.spawnPenaltyRange, data.deathLimitValue, data.deathLimitBase, data.canEditDeathLimit,
                data.firstTimeSetupDone, data.getPlayerDeaths(player.getUUID())
            ));
        } catch (Exception e) {
            LOGGER.error("Failed to sync HardcoreWorldData to player {}", player.getName().getString(), e);
        }
    }
}
