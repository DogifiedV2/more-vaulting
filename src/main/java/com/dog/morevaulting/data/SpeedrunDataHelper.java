package com.dog.morevaulting.data;

import com.dog.morevaulting.MoreVaulting;
import com.dog.morevaulting.data.SpeedrunData;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultLevel;
import iskallia.vault.core.vault.player.Completion;
import iskallia.vault.core.vault.player.Runner;
import iskallia.vault.core.vault.stat.StatCollector;
import iskallia.vault.core.vault.stat.StatsCollector;
import iskallia.vault.core.vault.time.TickClock;
import iskallia.vault.core.vault.objective.Objective;
import iskallia.vault.core.vault.objective.Objectives;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;

import java.util.List;
import java.util.UUID;

public class SpeedrunDataHelper {

    /**
     * Records a completed vault run in the speedrun stats.
     */
    public static void recordVaultCompletion(Vault vault, Runner runner) {
        if (!vault.has(Vault.STATS) || !vault.has(Vault.CLOCK)) {
            return;
        }

        StatsCollector statsCollector = (StatsCollector) vault.get(Vault.STATS);
        StatCollector playerStats = statsCollector.get(runner.getId());

        if (playerStats == null || playerStats.getCompletion() != Completion.COMPLETED) {
            return;
        }

        TickClock clock = (TickClock) vault.get(Vault.CLOCK);
        int timeTaken = clock.get(TickClock.LOGICAL_TIME);
        MoreVaulting.LOGGER.info("Tick Time:" + timeTaken);
        int level = ((VaultLevel) vault.get(Vault.LEVEL)).get();

        runner.getPlayer().ifPresent(player -> {
            // Get vault objective type
            String objectiveType = "unknown";
            if (vault.has(Vault.OBJECTIVES) && vault.get(Vault.OBJECTIVES).has(Objectives.KEY)) {
                objectiveType = vault.get(Vault.OBJECTIVES).get(Objectives.KEY);
            }

            if (objectiveType.toLowerCase().contains("speedrun")) {
                Objectives objectives = vault.get(Vault.OBJECTIVES);
                List<Objective> allObjectives = objectives.getAll(Objective.class);
                if (!allObjectives.isEmpty()) {
                    Objective obj = allObjectives.get(0);
                    objectiveType = obj.getKey().getId().toString().toLowerCase().replace("the_vault:", "");
                }
            }

            MoreVaulting.LOGGER.info("Objective type: " + objectiveType);

            SpeedrunData stats = SpeedrunData.get(player.getServer());
            UUID playerUUID = runner.getId();
            String playerName = player.getGameProfile().getName();

            stats.recordSpeedrun(
                    playerUUID,
                    playerName,
                    vault,
                    timeTaken
            );

            SpeedrunData.PlayerSpeedrunStats playerSpeedrunStats = stats.getPlayerStats(playerUUID);
            List<SpeedrunData.SpeedrunEntry> playerRuns = playerSpeedrunStats.getRunsByObjective(objectiveType);

            String finalObjectiveType = objectiveType;
            playerRuns.stream()
                    .filter(run -> run.getTimestamp() < System.currentTimeMillis() - 1000) // Filter out the run we just added
                    .min(java.util.Comparator.comparingInt(SpeedrunData.SpeedrunEntry::getTimeInTicks))
                    .ifPresent(personalBest -> {
                        // If this run is a new personal best
                        if (timeTaken <= personalBest.getTimeInTicks()) {
                            sendPersonalBestMessage(player, timeTaken, personalBest.getTimeInTicks(), finalObjectiveType);
                        }
                    });

            // Get global top times FOR THIS OBJECTIVE TYPE
            List<SpeedrunData.SpeedrunEntry> fastestRuns = stats.getFastestRunsByObjective(objectiveType, 3);
            if (!fastestRuns.isEmpty() && (fastestRuns.size() == 1 || fastestRuns.get(0).getTimeInTicks() >= timeTaken)) {
                sendWorldRecordMessage(player, timeTaken, objectiveType);
            }
        });
    }

    /**
     * Sends a message to the player about achieving a new personal best.
     */
    private static void sendPersonalBestMessage(ServerPlayer player, int newTime, int oldTime, String objectiveType) {
        Component message = new TextComponent("New Personal Best for ")
                .withStyle(ChatFormatting.GREEN)
                .append(new TextComponent(objectiveType)
                        .withStyle(ChatFormatting.AQUA))
                .append(new TextComponent(": ")
                        .withStyle(ChatFormatting.GREEN))
                .append(new TextComponent(formatTime(newTime))
                        .withStyle(ChatFormatting.GOLD))
                .append(new TextComponent(" (previous: ")
                        .withStyle(ChatFormatting.GRAY))
                .append(new TextComponent(formatTime(oldTime))
                        .withStyle(ChatFormatting.GRAY))
                .append(new TextComponent(")")
                        .withStyle(ChatFormatting.GRAY));

        player.sendMessage(message, player.getUUID());
    }

    /**
     * Sends a message to the player about achieving a new world record.
     */
    private static void sendWorldRecordMessage(ServerPlayer player, int time, String objectiveType) {
        Component message = new TextComponent("NEW RECORD for ")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
                .append(new TextComponent(objectiveType)
                        .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .append(new TextComponent("! ")
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .append(new TextComponent(formatTime(time))
                        .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));

        // Send message to all players
        player.getServer().getPlayerList().broadcastMessage(message, net.minecraft.network.chat.ChatType.SYSTEM, player.getUUID());
    }

    /**
     * Formats a time in ticks to a readable string.
     */
    public static String formatTime(int ticks) {
        int seconds = ticks / 20;
        int minutes = seconds / 60;
        seconds %= 60;

        return String.format("%d:%02d", minutes, seconds);
    }
}