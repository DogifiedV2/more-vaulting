package com.dog.morevaulting.config;

public class CustomVaultConfigRegistry {

    public static SpeedrunConfig SPEEDRUN;
    public static CakeConfig CAKES;


    public static void registerConfigs() {
        SPEEDRUN = new SpeedrunConfig().readConfig();
        CAKES = new CakeConfig().readConfig();
    }

}
