package com.example.hardcoretweaks;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public class HardcoreTweaksClient implements ClientModInitializer {

    // Cache the latest received sync data for the client UI
    public static SyncHardcoreDataPacket LATEST_SYNC_DATA = null;

    @Override
    public void onInitializeClient() {
        DeathTrackerClient.init();

        ClientPlayNetworking.registerGlobalReceiver(SyncHardcoreDataPacket.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                try {
                    LATEST_SYNC_DATA = payload;

                    // Check if we need to show the First Time Setup
                    if (context.client().level != null && context.client().level.getLevelData().isHardcore()) {
                        if (!payload.firstTimeSetupDone()) {
                            // We must delay the screen opening slightly to ensure the world is fully joined,
                            // but opening it directly here often works in 1.21.
                            context.client().setScreen(new FirstTimeSetupScreen());
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Exception handling SyncHardcoreDataPacket: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            LATEST_SYNC_DATA = null;
        });
    }
}
