package com.example.hardcoretweaks;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class HardcoreSettingsScreen extends Screen {
    private final Screen parent;
    private final SyncHardcoreDataPacket data;

    // Toggles
    private boolean spawnPenalty;
    private boolean deathLimitActive;
    private boolean deathTrackerActive;
    private boolean allowSetSpawn;

    // Values
    private int spawnPenaltyRange;
    private int deathLimitValue;
    
    // Status
    private boolean canEditDeathLimit;
    private EditBox spawnPenaltyBox;

    public HardcoreSettingsScreen(Screen parent) {
        super(Component.literal("Hardcore Settings"));
        this.parent = parent;
        this.data = HardcoreTweaksClient.LATEST_SYNC_DATA;

        this.spawnPenalty = data.spawnPenalty();
        this.deathLimitActive = data.deathLimitActive();
        this.deathTrackerActive = data.deathTrackerActive();
        this.allowSetSpawn = data.allowSetSpawn();
        this.spawnPenaltyRange = data.spawnPenaltyRange();
        this.deathLimitValue = data.deathLimitValue();
        this.canEditDeathLimit = data.canEditDeathLimit();
    }

    @Override
    protected void init() {
        boolean isHardcore = this.minecraft != null && this.minecraft.level != null && this.minecraft.level.getLevelData().isHardcore();
        
        int centerY = this.height / 2;
        int btnW = 150;
        int btnH = 20;
        int startX = this.width / 2 + 10;
        int labelX = this.width / 2 - 140;

        // Enter First Time Setup Button
        Button setupBtn = Button.builder(Component.literal("Enter First Time Setup"), b -> {
            if (this.minecraft != null) {
                this.minecraft.setScreen(new FirstTimeSetupScreen(this));
            }
        }).bounds(this.width / 2 - 100, centerY - 105, 200, 20).build();
        setupBtn.active = this.canEditDeathLimit && isHardcore;
        this.addRenderableWidget(setupBtn);

        // Toggles
        Button spawnPenaltyBtn = Button.builder(Component.literal(this.spawnPenalty ? "ON" : "OFF"), b -> {
            this.spawnPenalty = !this.spawnPenalty;
            b.setMessage(Component.literal(this.spawnPenalty ? "ON" : "OFF"));
        }).bounds(startX, centerY - 80, btnW, btnH).build();
        spawnPenaltyBtn.active = isHardcore;
        this.addRenderableWidget(spawnPenaltyBtn);

        Button allowSetSpawnBtn = Button.builder(Component.literal(this.allowSetSpawn ? "ON" : "OFF"), b -> {
            this.allowSetSpawn = !this.allowSetSpawn;
            b.setMessage(Component.literal(this.allowSetSpawn ? "ON" : "OFF"));
        }).bounds(startX, centerY - 55, btnW, btnH).build();
        allowSetSpawnBtn.active = isHardcore;
        this.addRenderableWidget(allowSetSpawnBtn);

        Button deathLimitBtn = Button.builder(Component.literal(this.deathLimitActive ? "ON" : "OFF"), b -> {
            if (!this.canEditDeathLimit) return;
            this.deathLimitActive = !this.deathLimitActive;
            b.setMessage(Component.literal(this.deathLimitActive ? "ON" : "OFF"));
        }).bounds(startX, centerY - 30, btnW, btnH).build();
        deathLimitBtn.active = this.canEditDeathLimit && isHardcore;
        this.addRenderableWidget(deathLimitBtn);

        this.addRenderableWidget(Button.builder(Component.literal(this.deathTrackerActive ? "ON" : "OFF"), b -> {
            this.deathTrackerActive = !this.deathTrackerActive;
            b.setMessage(Component.literal(this.deathTrackerActive ? "ON" : "OFF"));
        }).bounds(startX, centerY - 5, btnW, btnH).build());

        // Text input for Spawn Penalty Range
        this.spawnPenaltyBox = new EditBox(this.font, startX, centerY + 20, btnW, btnH, Component.literal("Spawn Penalty Range"));
        this.spawnPenaltyBox.setValue(String.valueOf(this.spawnPenaltyRange));
        this.spawnPenaltyBox.setResponder(s -> {
            try {
                this.spawnPenaltyRange = Integer.parseInt(s);
            } catch (NumberFormatException ignored) {}
        });
        this.spawnPenaltyBox.active = isHardcore;
        this.addRenderableWidget(this.spawnPenaltyBox);

        AbstractSliderButton deathSlider = new AbstractSliderButton(startX, centerY + 45, btnW, btnH, Component.literal(String.valueOf(this.deathLimitValue)), this.deathLimitValue / 100.0) {
            @Override
            protected void updateMessage() {
                this.setMessage(Component.literal(String.valueOf((int)(this.value * 100.0))));
            }
            @Override
            protected void applyValue() {
                HardcoreSettingsScreen.this.deathLimitValue = (int)(this.value * 100.0);
            }
        };
        deathSlider.active = this.canEditDeathLimit && isHardcore;
        this.addRenderableWidget(deathSlider);

        // Edit Death Tracker Button
        this.addRenderableWidget(Button.builder(Component.literal("Edit Death Tracker"), b -> {
            if (this.minecraft != null) {
                this.minecraft.setScreen(new DeathTrackerSettingsScreen(this, DeathTrackerClient.CONFIG));
            }
        }).bounds(this.width / 2 - 100, centerY + 70, 200, 20).build());

        // Done
        this.addRenderableWidget(Button.builder(Component.literal("Done"), b -> {
            this.onClose();
        }).bounds(this.width / 2 - 100, centerY + 95, 200, 20).build());
    }

    public void extractRenderState(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float delta) {
        
        int labelColor = 0xFFFFFFFF;
        int centerY = this.height / 2;
        int labelX = this.width / 2 - 10;
        
        gfx.centeredText(this.font, "Hardcore Tweaks Settings", this.width / 2, 20, labelColor);

        gfx.text(this.font, "Spawn Penalty", this.width / 2 - 150, centerY - 75, labelColor, true);
        gfx.text(this.font, "Allow Set Spawn", this.width / 2 - 150, centerY - 50, labelColor, true);
        gfx.text(this.font, "Death Limit Active", this.width / 2 - 150, centerY - 25, labelColor, true);
        gfx.text(this.font, "Death Tracker", this.width / 2 - 150, centerY, labelColor, true);
        
        gfx.text(this.font, "Spawn Penalty Range", this.width / 2 - 150, centerY + 25, labelColor, true);
        gfx.text(this.font, "Death Limit Value", this.width / 2 - 150, centerY + 50, labelColor, true);

        super.extractRenderState(gfx, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            // Save and sync
            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(new UpdateHardcoreDataPacket(
                this.spawnPenalty, this.deathLimitActive, this.deathTrackerActive, this.allowSetSpawn,
                this.spawnPenaltyRange, this.deathLimitValue, this.canEditDeathLimit, true
            ));
            this.minecraft.setScreen(this.parent);
        }
    }

    public void updateFromSetup(boolean deathLimitActive, int deathLimitValue, boolean canEditDeathLimit) {
        this.deathLimitActive = deathLimitActive;
        this.deathLimitValue = deathLimitValue;
        this.canEditDeathLimit = canEditDeathLimit;
        this.clearWidgets();
        this.init();
    }
}
