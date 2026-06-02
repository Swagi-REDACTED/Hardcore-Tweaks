package com.example.hardcoretweaks.mixin.client;

import com.example.hardcoretweaks.DeathTrackerClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.joml.Matrix3x2fStack;

@Mixin(DeathScreen.class)
public abstract class DeathScreenMixin extends Screen {

    protected DeathScreenMixin(Component title) {
        super(title);
    }

    @Unique
    private int hardcoreTweaks$ticksRemaining = 100;

    @Unique
    private boolean hardcoreTweaks$canRespawn() {
        var data = com.example.hardcoretweaks.HardcoreTweaksClient.LATEST_SYNC_DATA;
        if (data == null) return false;
        if (!data.deathLimitActive()) return true;
        
        int remaining = (data.deathLimitBase() + data.deathLimitValue()) - data.playerDeaths();
        return remaining > 0;
    }

    @Unique
    private boolean hardcoreTweaks$wasAbleToRespawn = true;

    @Inject(method = "init", at = @At("TAIL"))
    private void hardcoreTweaks$hideWidgets(CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        boolean canRespawn = client.level != null && client.level.getLevelData().isHardcore() && hardcoreTweaks$canRespawn();
        hardcoreTweaks$wasAbleToRespawn = canRespawn;
        if (canRespawn) {
            this.clearWidgets();
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void hardcoreTweaks$tick(CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        boolean canRespawn = client.level != null && client.level.getLevelData().isHardcore() && hardcoreTweaks$canRespawn();
        
        if (hardcoreTweaks$wasAbleToRespawn && !canRespawn) {
            hardcoreTweaks$wasAbleToRespawn = false;
            this.rebuildWidgets();
        }

        if (canRespawn) {
            if (hardcoreTweaks$ticksRemaining > 0) {
                hardcoreTweaks$ticksRemaining--;
                if (hardcoreTweaks$ticksRemaining <= 0) {
                    client.player.respawn();
                }
            }
        }
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void hardcoreTweaks$render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (client.level != null && client.level.getLevelData().isHardcore() && hardcoreTweaks$canRespawn()) {
            float exactTicks = hardcoreTweaks$ticksRemaining - delta;
            if (exactTicks < 0) exactTicks = 0;

            int color;
            if (exactTicks > 60) {
                float progress = (exactTicks - 60) / 40.0f;
                color = hardcoreTweaks$interpolateColor(0xFFFF55, 0x55FF55, progress);
            } else {
                float progress = exactTicks / 60.0f;
                color = hardcoreTweaks$interpolateColor(0xFF5555, 0xFFFF55, progress);
            }

            int seconds = (int) Math.ceil(exactTicks / 20.0f);
            String text = "Respawning in " + seconds + "...";
            
            // Format using DeathTracker logic
            if (DeathTrackerClient.CONFIG != null && DeathTrackerClient.CONFIG.getTextCase() != null) {
                text = DeathTrackerClient.CONFIG.getTextCase().apply(text);
            }

            float s = 4.5f;
            Matrix3x2fStack ps = graphics.pose();
            ps.pushMatrix();
            ps.scale(s, s);
            graphics.centeredText(this.font, text, (int)(this.width / 2 / s), (int)((this.height / 2) / s), color);
            ps.popMatrix();
        }
    }

    @Unique
    private int hardcoreTweaks$interpolateColor(int start, int end, float progress) {
        int r1 = (start >> 16) & 0xFF;
        int g1 = (start >> 8) & 0xFF;
        int b1 = start & 0xFF;

        int r2 = (end >> 16) & 0xFF;
        int g2 = (end >> 8) & 0xFF;
        int b2 = end & 0xFF;

        int r = (int) (r1 + (r2 - r1) * progress);
        int g = (int) (g1 + (g2 - g1) * progress);
        int b = (int) (b1 + (b2 - b1) * progress);

        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }
}
