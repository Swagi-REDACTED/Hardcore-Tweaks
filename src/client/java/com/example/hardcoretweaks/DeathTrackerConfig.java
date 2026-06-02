package com.example.hardcoretweaks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public class DeathTrackerConfig {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("hardcore-tweaks.json");
   public static final float SCALE_MIN = 0.5F;
   public static final float SCALE_MAX = 5.0F;
   public static final float SCALE_STEP = 0.25F;
   public int x = 2;
   public int y = 2;
   public float scale = 1.0F;
   public String language = DeathTrackerLanguage.ENGLISH.id;
   public String font = DeathTrackerFont.DEFAULT.id;
   public String textCase = DeathTrackerTextCase.NORMAL.id;
   public int r = 255;
   public int g = 255;
   public int b = 255;

   public DeathTrackerLanguage getLanguage() {
      return DeathTrackerLanguage.fromId(this.language);
   }

   public void setLanguage(DeathTrackerLanguage lang) {
      this.language = lang.id;
   }

   public DeathTrackerFont getFont() {
      return DeathTrackerFont.fromId(this.font);
   }

   public void setFont(DeathTrackerFont f) {
      this.font = f.id;
   }

   public DeathTrackerTextCase getTextCase() {
      return DeathTrackerTextCase.fromId(this.textCase);
   }

   public void setTextCase(DeathTrackerTextCase c) {
      this.textCase = c.id;
   }

   public static DeathTrackerConfig load() {
      if (Files.exists(CONFIG_FILE)) {
         try (Reader r = Files.newBufferedReader(CONFIG_FILE)) {
            DeathTrackerConfig cfg = GSON.fromJson(r, DeathTrackerConfig.class);
            if (cfg != null) {
               cfg.scale = Math.max(0.5F, Math.min(5.0F, cfg.scale));
               if (cfg.language == null) {
                  cfg.language = DeathTrackerLanguage.ENGLISH.id;
               }

               if (cfg.font == null) {
                  cfg.font = DeathTrackerFont.DEFAULT.id;
               }

               if (cfg.textCase == null) {
                  cfg.textCase = DeathTrackerTextCase.NORMAL.id;
               }

               cfg.r = Math.max(0, Math.min(255, cfg.r));
               cfg.g = Math.max(0, Math.min(255, cfg.g));
               cfg.b = Math.max(0, Math.min(255, cfg.b));

               return cfg;
            }
         } catch (Exception var5) {
         }
      }

      return new DeathTrackerConfig();
   }

   public void save() {
      try (Writer w = Files.newBufferedWriter(CONFIG_FILE)) {
         GSON.toJson(this, w);
      } catch (Exception var6) {
      }
   }
}
