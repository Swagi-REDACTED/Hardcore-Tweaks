package com.example.hardcoretweaks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HardcoreWorldData extends SavedData {

    public static final Codec<HardcoreWorldData> CODEC = RecordCodecBuilder.create(
        i -> i.group(
            Codec.BOOL.optionalFieldOf("spawnPenalty", true).forGetter(d -> d.spawnPenalty),
            Codec.BOOL.optionalFieldOf("deathLimitActive", false).forGetter(d -> d.deathLimitActive),
            Codec.BOOL.optionalFieldOf("deathTrackerActive", true).forGetter(d -> d.deathTrackerActive),
            Codec.BOOL.optionalFieldOf("allowSetSpawn", true).forGetter(d -> d.allowSetSpawn),
            Codec.INT.optionalFieldOf("spawnPenaltyRange", 1000).forGetter(d -> d.spawnPenaltyRange),
            Codec.INT.optionalFieldOf("deathLimitValue", 1).forGetter(d -> d.deathLimitValue),
            Codec.INT.optionalFieldOf("deathLimitBase", 0).forGetter(d -> d.deathLimitBase),
            Codec.BOOL.optionalFieldOf("canEditDeathLimit", true).forGetter(d -> d.canEditDeathLimit),
            Codec.BOOL.optionalFieldOf("firstTimeSetupDone", false).forGetter(d -> d.firstTimeSetupDone),
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.INT).optionalFieldOf("deathCounts", new HashMap<>()).forGetter(d -> d.deathCounts)
        ).apply(i, HardcoreWorldData::new)
    );

    public boolean spawnPenalty = true;
    public boolean deathLimitActive = false;
    public boolean deathTrackerActive = true;
    public boolean allowSetSpawn = true;
    public int spawnPenaltyRange = 1000;
    public int deathLimitValue = 1;
    public int deathLimitBase = 0;
    public boolean canEditDeathLimit = true;
    public boolean firstTimeSetupDone = false;

    public final Map<UUID, Integer> deathCounts;

    public HardcoreWorldData() {
        this.deathCounts = new HashMap<>();
    }

    private HardcoreWorldData(boolean spawnPenalty, boolean deathLimitActive, boolean deathTrackerActive, boolean allowSetSpawn, int spawnPenaltyRange, int deathLimitValue, int deathLimitBase, boolean canEditDeathLimit, boolean firstTimeSetupDone, Map<UUID, Integer> deathCounts) {
        this.spawnPenalty = spawnPenalty;
        this.deathLimitActive = deathLimitActive;
        this.deathTrackerActive = deathTrackerActive;
        this.allowSetSpawn = allowSetSpawn;
        this.spawnPenaltyRange = spawnPenaltyRange;
        this.deathLimitValue = deathLimitValue;
        this.deathLimitBase = deathLimitBase;
        this.canEditDeathLimit = canEditDeathLimit;
        this.firstTimeSetupDone = firstTimeSetupDone;
        this.deathCounts = new HashMap<>(deathCounts);
    }

    public static SavedDataType<HardcoreWorldData> type() {
        return new SavedDataType<>(
            Identifier.withDefaultNamespace("hardcore_tweaks_data"),
            HardcoreWorldData::new,
            CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE // Reusing an existing DataFixType is usually fine for custom mods without datafixers
        );
    }

    public static HardcoreWorldData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(type());
    }

    public int getPlayerDeaths(UUID uuid) {
        return this.deathCounts.getOrDefault(uuid, 0);
    }

    public void addPlayerDeath(UUID uuid) {
        this.deathCounts.put(uuid, this.getPlayerDeaths(uuid) + 1);
        this.setDirty();
    }
}
