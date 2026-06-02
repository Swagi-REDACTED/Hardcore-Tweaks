package com.example.hardcoretweaks;

public enum DeathTrackerTextCase {
   NORMAL("normal", "Normal"),
   UPPER("upper", "ALL CAPS"),
   LOWER("lower", "lowercase");

   public final String id;
   public final String displayName;

   DeathTrackerTextCase(String id, String displayName) {
      this.id = id;
      this.displayName = displayName;
   }

   public String apply(String text) {
      return switch (this) {
         case NORMAL -> text;
         case UPPER -> text.toUpperCase();
         case LOWER -> text.toLowerCase();
      };
   }

   public static DeathTrackerTextCase fromId(String id) {
      for (DeathTrackerTextCase c : values()) {
         if (c.id.equalsIgnoreCase(id)) {
            return c;
         }
      }

      return NORMAL;
   }

   public DeathTrackerTextCase next() {
      DeathTrackerTextCase[] v = values();
      return v[(this.ordinal() + 1) % v.length];
   }

   public DeathTrackerTextCase prev() {
      DeathTrackerTextCase[] v = values();
      return v[(this.ordinal() + v.length - 1) % v.length];
   }
}
