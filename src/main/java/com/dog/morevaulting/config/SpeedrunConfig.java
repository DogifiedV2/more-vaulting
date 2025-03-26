package com.dog.morevaulting.config;

import com.dog.morevaulting.MoreVaulting;
import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SpeedrunConfig extends Config {

    @Expose private boolean randomObjective;
    @Expose private List<String> randomObjectiveList;
    @Expose private boolean copyFastestTime;
    @Expose private boolean shouldTimerKill;
    @Expose private String setObjective;
    @Expose private Map<String, Integer> defaultTime;
    @Expose private int globalDefaultTime;
    @Expose private boolean broadcastRecords;
    @Expose private boolean notifyPersonalBest;

    @Override
    public String getName() {
        return "morevaulting_speedruns";
    }

    @Override
    protected void reset() {
        this.randomObjective = true;
        this.randomObjectiveList = List.of("elixir", "scavenger", "monolith", "bingo", "brutal_bosses");
        this.copyFastestTime = true;
        this.shouldTimerKill = false;
        this.setObjective = "monolith";
        this.defaultTime = new LinkedHashMap<>();

        this.defaultTime.put("elixir", 30000);
        this.defaultTime.put("scavenger", 30000);
        this.defaultTime.put("bingo", 30000);
        this.defaultTime.put("monolith", 12000);
        this.defaultTime.put("brutal_bosses", 12000);
        this.globalDefaultTime = 30000;

        this.broadcastRecords = true;
        this.notifyPersonalBest = true;
    }

    @Override
    public boolean isValid() {
        List<String> validObjectives = List.of("elixir", "scavenger", "monolith", "bingo", "brutal_bosses");

        for (String objective : randomObjectiveList) {
            if (!validObjectives.contains(objective)) {
                MoreVaulting.LOGGER.error("Invalid random objective: " + objective);
                MoreVaulting.LOGGER.error("Valid objectives: " + validObjectives);
                return false;
            }
        }

        return true;
    }

    public boolean getRandomObjectiveEnabled() {
        return this.randomObjective;
    }

    public List<String> getRandomObjectiveList() {
        return this.randomObjectiveList;
    }

    public String getSetObjective() {
        return this.setObjective;
    }

    public Map<String, Integer> getDefaultTime() {
        return this.defaultTime;
    }

    public int getDefaultTimeFromMap(String objective) {
        return this.defaultTime.getOrDefault(objective, getGlobalDefaultTime());
    }

    public int getGlobalDefaultTime() {
        return this.globalDefaultTime;
    }

    public boolean getBroadcastRecords() {
        return this.broadcastRecords;
    }

    public boolean getNotifyPersonalBest() {
        return this.notifyPersonalBest;
    }

    public boolean getCopyFastestTime() {
        return this.copyFastestTime;
    }

    public boolean getShouldTimerKill() {
        return this.shouldTimerKill;
    }

}
