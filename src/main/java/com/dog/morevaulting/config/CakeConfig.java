package com.dog.morevaulting.config;

import com.dog.morevaulting.MoreVaulting;
import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CakeConfig extends Config {

    @Expose private String cakesDifficulty;

    @Override
    public String getName() {
        return "morevaulting_cakes";
    }

    @Override
    protected void reset() {
        this.cakesDifficulty = "player";
    }


    @Override
    public boolean isValid() {
        List<String> validDifficulties = List.of("player", "piece_of_cake", "easy", "normal", "hard", "impossible", "fragged");

        boolean valid = false;
        for (String difficulty : validDifficulties) {
            if (difficulty.equals(this.cakesDifficulty)) {
                valid = true;
            }
        }

        if (!valid) {
            MoreVaulting.LOGGER.error("Invalid difficulty: " + this.cakesDifficulty);
            MoreVaulting.LOGGER.error("Valid difficulties: " + validDifficulties);
            return false;
        }

        return valid;
    }

    public String getCakeDifficulty() {
        return this.cakesDifficulty;
    }


}
