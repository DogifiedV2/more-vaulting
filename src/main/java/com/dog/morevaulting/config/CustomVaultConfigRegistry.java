package com.dog.morevaulting.config;

public class CustomVaultConfigRegistry {

    public static SpeedrunConfig SPEEDRUN;
    public static CakeConfig CAKES;
    public static MobLootConfig MOB_LOOT;


    public static void registerConfigs() {
        SPEEDRUN = new SpeedrunConfig().readConfig();
        CAKES = new CakeConfig().readConfig();
        MOB_LOOT = new MobLootConfig().readConfig();
    }

}
