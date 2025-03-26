package com.dog.morevaulting.data;

import com.dog.morevaulting.MoreVaulting;
import com.google.common.collect.Maps;
import iskallia.vault.VaultMod;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.Objective;
import iskallia.vault.core.vault.objective.Objectives;
import iskallia.vault.core.vault.stat.StatCollector;
import iskallia.vault.core.vault.stat.StatsCollector;
import iskallia.vault.core.vault.player.Completion;
import iskallia.vault.core.vault.player.Runner;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.stream.Collectors;

public class SpeedrunData extends SavedData {
    private static final String DATA_NAME = "morevaulting_SpeedrunData";

    // Map of player UUID to their speedrun stats
    private final Map<UUID, PlayerSpeedrunStats> playerStats = Maps.newHashMap();

    public SpeedrunData() {
        // Default constructor
    }

    public static SpeedrunData get(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(ServerLevel.OVERWORLD);
        if (overworld == null) {
            throw new IllegalStateException("Overworld not found!");
        }

        return overworld.getDataStorage().computeIfAbsent(
                SpeedrunData::load,
                SpeedrunData::new,
                DATA_NAME
        );
    }

    public static SpeedrunData get(ServerLevel level) {
        return get(level.getServer());
    }

    public static SpeedrunData load(CompoundTag nbt) {
        SpeedrunData data = new SpeedrunData();
        ListTag playerList = nbt.getList("PlayerStats", Tag.TAG_COMPOUND);

        for (int i = 0; i < playerList.size(); i++) {
            CompoundTag playerTag = playerList.getCompound(i);
            UUID playerId = playerTag.getUUID("PlayerId");
            PlayerSpeedrunStats stats = PlayerSpeedrunStats.fromNBT(playerTag);
            data.playerStats.put(playerId, stats);
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        ListTag playerList = new ListTag();

        for (Map.Entry<UUID, PlayerSpeedrunStats> entry : this.playerStats.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("PlayerId", entry.getKey());
            entry.getValue().writeToNBT(playerTag);
            playerList.add(playerTag);
        }

        nbt.put("PlayerStats", playerList);
        return nbt;
    }

    public PlayerSpeedrunStats getPlayerStats(UUID playerId) {
        return this.playerStats.computeIfAbsent(playerId, id -> new PlayerSpeedrunStats());
    }

    public void recordSpeedrun(UUID playerId, String playerName, Vault vault, int timeInTicks) {
        PlayerSpeedrunStats stats = getPlayerStats(playerId);
        SpeedrunEntry entry = new SpeedrunEntry(playerId, playerName, timeInTicks, System.currentTimeMillis());

        // Get vault objective type
        String objectiveType = "unknown";
        if (vault.has(Vault.OBJECTIVES) && vault.get(Vault.OBJECTIVES).has(iskallia.vault.core.vault.objective.Objectives.KEY)) {
            objectiveType = vault.get(Vault.OBJECTIVES).get(iskallia.vault.core.vault.objective.Objectives.KEY);
        }

        if (objectiveType.toLowerCase().contains("speedrun")) {
            Objectives objectives = vault.get(Vault.OBJECTIVES);
            List<Objective> allObjectives = objectives.getAll(Objective.class);
            Objective obj = allObjectives.get(0);
            MoreVaulting.LOGGER.info("Objective type new: " + obj.getKey().getId().toString());
            objectiveType = obj.getKey().getId().toString().toLowerCase().replace("the_vault:", "");
        }

        MoreVaulting.LOGGER.info("Objective typev2: " + objectiveType);


        stats.addRun(objectiveType, entry);
        this.setDirty();
    }

    public List<SpeedrunEntry> getFastestRuns(int count) {
        return this.playerStats.values().stream()
                .flatMap(stats -> stats.getAllRuns().stream())
                .sorted(Comparator.comparingInt(SpeedrunEntry::getTimeInTicks))
                .limit(count)
                .collect(Collectors.toList());
    }

    public List<SpeedrunEntry> getFastestRunsByObjective(String objectiveType, int count) {
        return this.playerStats.values().stream()
                .flatMap(stats -> stats.getRunsByObjective(objectiveType).stream())
                .sorted(Comparator.comparingInt(SpeedrunEntry::getTimeInTicks))
                .limit(count)
                .collect(Collectors.toList());
    }

    /**
     * Inner class for stats
     */
    public static class PlayerSpeedrunStats {
        private final Map<String, List<SpeedrunEntry>> runsByObjectiveType = Maps.newHashMap();

        public void addRun(String objectiveType, SpeedrunEntry entry) {
            List<SpeedrunEntry> runs = runsByObjectiveType.computeIfAbsent(objectiveType, k -> new ArrayList<>());
            runs.add(entry);
        }

        public List<SpeedrunEntry> getRunsByObjective(String objectiveType) {
            return runsByObjectiveType.getOrDefault(objectiveType, Collections.emptyList());
        }

        public List<SpeedrunEntry> getAllRuns() {
            return runsByObjectiveType.values().stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        }

        public void writeToNBT(CompoundTag nbt) {
            CompoundTag objectivesTag = new CompoundTag();

            for (Map.Entry<String, List<SpeedrunEntry>> entry : runsByObjectiveType.entrySet()) {
                ListTag runsList = new ListTag();

                for (SpeedrunEntry run : entry.getValue()) {
                    CompoundTag runTag = new CompoundTag();
                    run.writeToNBT(runTag);
                    runsList.add(runTag);
                }

                objectivesTag.put(entry.getKey(), runsList);
            }

            nbt.put("Objectives", objectivesTag);
        }

        public static PlayerSpeedrunStats fromNBT(CompoundTag nbt) {
            PlayerSpeedrunStats stats = new PlayerSpeedrunStats();
            CompoundTag objectivesTag = nbt.getCompound("Objectives");

            for (String key : objectivesTag.getAllKeys()) {
                ListTag runsList = objectivesTag.getList(key, Tag.TAG_COMPOUND);
                List<SpeedrunEntry> runs = new ArrayList<>();

                for (int i = 0; i < runsList.size(); i++) {
                    CompoundTag runTag = runsList.getCompound(i);
                    runs.add(SpeedrunEntry.fromNBT(runTag));
                }

                stats.runsByObjectiveType.put(key, runs);
            }

            return stats;
        }
    }

    /**
     * Speedrun Entry
     */
    public static class SpeedrunEntry {
        private final UUID playerUUID;
        private final String playerName; // Decorative only
        private final int timeInTicks;
        private final long timestamp;

        public SpeedrunEntry(UUID playerUUID, String playerName, int timeInTicks, long timestamp) {
            this.playerUUID = playerUUID;
            this.playerName = playerName;
            this.timeInTicks = timeInTicks;
            this.timestamp = timestamp;
        }

        public UUID getPlayerUUID() {
            return playerUUID;
        }

        public String getPlayerName() {
            return playerName;
        }

        public int getTimeInTicks() {
            return timeInTicks;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void writeToNBT(CompoundTag nbt) {
            nbt.putUUID("PlayerUUID", playerUUID);
            nbt.putString("PlayerName", playerName);
            nbt.putInt("TimeInTicks", timeInTicks);
            nbt.putLong("Timestamp", timestamp);
        }

        public static SpeedrunEntry fromNBT(CompoundTag nbt) {
            UUID playerUUID = nbt.getUUID("PlayerUUID");
            String playerName = nbt.getString("PlayerName");
            int timeInTicks = nbt.getInt("TimeInTicks");
            long timestamp = nbt.getLong("Timestamp");

            return new SpeedrunEntry(playerUUID, playerName, timeInTicks, timestamp);
        }
    }
}