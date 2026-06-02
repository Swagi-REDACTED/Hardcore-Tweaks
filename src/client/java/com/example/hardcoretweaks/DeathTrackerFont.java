package com.example.hardcoretweaks;

import net.minecraft.client.gui.Font;

public enum DeathTrackerFont {
   DEFAULT("default", "Default"),
   FILTERED("filtered", "Filtered");

   public final String id;
   public final String displayName;

   DeathTrackerFont(String id, String displayName) {
      this.id = id;
      this.displayName = displayName;
   }

   public Font getFont(Font defaultFont, Font filteredFont) {
      return this == DEFAULT ? defaultFont : filteredFont;
   }

   public static DeathTrackerFont fromId(String id) {
      for (DeathTrackerFont f : values()) {
         if (f.id.equalsIgnoreCase(id)) {
            return f;
         }
      }

      return DEFAULT;
   }

   public DeathTrackerFont next() {
      DeathTrackerFont[] v = values();
      return v[(this.ordinal() + 1) % v.length];
   }

   public DeathTrackerFont prev() {
      DeathTrackerFont[] v = values();
      return v[(this.ordinal() + v.length - 1) % v.length];
   }
}
