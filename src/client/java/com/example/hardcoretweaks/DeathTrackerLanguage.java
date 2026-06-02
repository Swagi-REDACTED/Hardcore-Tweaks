package com.example.hardcoretweaks;

public enum DeathTrackerLanguage {
   ENGLISH("english", "English", "Deaths"),
   SPANISH("spanish", "Español", "Muertes"),
   PORTUGUESE("portuguese", "Português", "Mortes"),
   FRENCH("french", "Français", "Morts"),
   RUSSIAN("russian", "Русский", "Смерти"),
   CHINESE("chinese", "中文", "死亡");

   public final String id;
   public final String displayName;
   public final String dayWord;

   DeathTrackerLanguage(String id, String displayName, String dayWord) {
      this.id = id;
      this.displayName = displayName;
      this.dayWord = dayWord;
   }

   public String format(long day) {
      return this.dayWord + ": " + day;
   }

   public static DeathTrackerLanguage fromId(String id) {
      for (DeathTrackerLanguage l : values()) {
         if (l.id.equalsIgnoreCase(id)) {
            return l;
         }
      }

      return ENGLISH;
   }

   public DeathTrackerLanguage next() {
      DeathTrackerLanguage[] v = values();
      return v[(this.ordinal() + 1) % v.length];
   }

   public DeathTrackerLanguage prev() {
      DeathTrackerLanguage[] v = values();
      return v[(this.ordinal() + v.length - 1) % v.length];
   }
}
