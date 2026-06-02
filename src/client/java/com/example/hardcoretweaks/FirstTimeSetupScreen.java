package com.example.hardcoretweaks;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class FirstTimeSetupScreen extends Screen {

    private boolean allowEditDeathLimit = true;
    private boolean deathLimitActive = true;
    private int deathLimitValue = 1;

    public FirstTimeSetupScreen() {
        super(Component.literal("Hardcore First Time Setup"));
    }

    @Override
    protected void init() {
        int centerY = this.height / 2;
        int btnW = 150;
        int btnH = 20;

        // Death Limit Toggle
        this.addRenderableWidget(Button.builder(Component.literal(this.deathLimitActive ? "Death Limit: ON" : "Death Limit: OFF"), b -> {
            this.deathLimitActive = !this.deathLimitActive;
            b.setMessage(Component.literal(this.deathLimitActive ? "Death Limit: ON" : "Death Limit: OFF"));
        }).bounds(this.width / 2 - 160, centerY - 20, btnW, btnH).build());

        // Death Limit Slider
        this.addRenderableWidget(new AbstractSliderButton(this.width / 2 + 10, centerY - 20, btnW, btnH, Component.literal("Limit: " + this.deathLimitValue), this.deathLimitValue / 100.0) {
            @Override
            protected void updateMessage() {
                this.setMessage(Component.literal("Limit: " + (int)(this.value * 100.0)));
            }
            @Override
            protected void applyValue() {
                FirstTimeSetupScreen.this.deathLimitValue = (int)(this.value * 100.0);
            }
        });

        // Allow Edit Later
        this.addRenderableWidget(Button.builder(Component.literal(this.allowEditDeathLimit ? "Allow Editing Later: YES" : "Allow Editing Later: NO"), b -> {
            this.allowEditDeathLimit = !this.allowEditDeathLimit;
            b.setMessage(Component.literal(this.allowEditDeathLimit ? "Allow Editing Later: YES" : "Allow Editing Later: NO"));
        }).bounds(this.width / 2 - btnW / 2, centerY + 30, btnW, btnH).build());

        // Confirm
        this.addRenderableWidget(Button.builder(Component.literal("Confirm Setup"), b -> {
            this.onClose();
        }).bounds(this.width / 2 - 100, centerY + 80, 200, 20).build());
    }

    public void extractRenderState(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float delta) {
        
        int labelColor = 0xFFFFFFFF;
        
        gfx.centeredText(this.font, "Welcome to Hardcore Tweaks!", this.width / 2, 30, 0xFFFF5555);
        gfx.centeredText(this.font, "You can configure a Death Limit below.", this.width / 2, 50, labelColor);
        gfx.centeredText(this.font, "If you disable editing later, these settings are locked permanently!", this.width / 2, 65, 0xFFFF5555);
        
        super.extractRenderState(gfx, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false; // Force them to click Confirm!
    }

    @Override
    public void onClose() {
        if (this.minecraft != null && HardcoreTweaksClient.LATEST_SYNC_DATA != null) {
            SyncHardcoreDataPacket old = HardcoreTweaksClient.LATEST_SYNC_DATA;
            
            // Save and sync
            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(new UpdateHardcoreDataPacket(
                old.spawnPenalty(), this.deathLimitActive, old.deathTrackerActive(), old.allowSetSpawn(),
                old.spawnPenaltyRange(), this.deathLimitValue, this.allowEditDeathLimit, true
            ));
            
            // Remove the screen
            this.minecraft.setScreen(null);
        }
    }
}
