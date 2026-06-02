package com.example.hardcoretweaks;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2fStack;

@Environment(EnvType.CLIENT)
public class DeathTrackerClient {
   public static final String MOD_ID = "hardcore-tweaks";
   public static DeathTrackerConfig CONFIG;

   public static void init() {
      CONFIG = DeathTrackerConfig.load();
      HudElementRegistry.attachElementAfter(
         VanillaHudElements.HOTBAR, Identifier.fromNamespaceAndPath("hardcore-tweaks", "death_tracker"), DeathTrackerClient::renderDeathTracker
      );
   }

   private static void renderDeathTracker(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
      Minecraft client = Minecraft.getInstance();
      if (client.level != null && !client.options.hideGui && HardcoreTweaksClient.LATEST_SYNC_DATA != null) {
         SyncHardcoreDataPacket data = HardcoreTweaksClient.LATEST_SYNC_DATA;
         if (!data.deathTrackerActive()) return;

         int deaths = data.playerDeaths();
         String baseText = "Deaths: " + deaths;
         
         if (data.deathLimitActive()) {
             int remaining = (data.deathLimitBase() + data.deathLimitValue()) - deaths;
             baseText += "(" + remaining + ")";
         }

         String text = CONFIG.getTextCase().apply(baseText);
         float s = CONFIG.scale;
         Matrix3x2fStack ps = graphics.pose();
         ps.pushMatrix();
         ps.translate(CONFIG.x, CONFIG.y);
         ps.scale(s, s);
         int color = 0xFF000000 | (CONFIG.r << 16) | (CONFIG.g << 8) | CONFIG.b;
         graphics.text(CONFIG.getFont().getFont(client.font, client.fontFilterFishy), text, 0, 0, color, true);
         ps.popMatrix();
      }
   }
}
