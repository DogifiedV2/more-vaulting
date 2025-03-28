package com.dog.morevaulting.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MobLootConfig extends Config {

    @Expose
    private Map<String, EntityLootTable> vaultLoot = new HashMap<>();

    @Expose
    private Map<String, EntityLootTable> overworldLoot = new HashMap<>();

    @Expose
    private Map<String, EntityLootTable> vaultTags = new HashMap<>();

    @Expose
    private Map<String, EntityLootTable> overworldTags = new HashMap<>();

    @Override
    public String getName() {
        return "morevaulting_mobs_loot";
    }

    @Override
    protected void reset() {
        List<LeveledLootTable> pigVaultLootTables = new ArrayList<>();
        pigVaultLootTables.add(new LeveledLootTable(0, "the_vault:champion_loot_lvl0", 1.0F));
        pigVaultLootTables.add(new LeveledLootTable(50, "the_vault:champion_loot_lvl50", 1.0F));
        pigVaultLootTables.add(new LeveledLootTable(100, "the_vault:champion_loot_lvl50", 1.0F));

        EntityLootTable pigVaultConfig = new EntityLootTable(pigVaultLootTables);
        vaultLoot.put("the_vault:your_mob_here", pigVaultConfig);

        // overworld loot for pig
        List<LeveledLootTable> pigOverworldLootTables = new ArrayList<>();
        pigOverworldLootTables.add(new LeveledLootTable(0, "the_vault:champion_loot_lvl0", 0.5F));

        EntityLootTable pigOverworldConfig = new EntityLootTable(pigOverworldLootTables);
        overworldLoot.put("minecraft:your_mob_here", pigOverworldConfig);

        // tag-based loot for vaults
        List<LeveledLootTable> hostileVaultLootTables = new ArrayList<>();
        hostileVaultLootTables.add(new LeveledLootTable(0, "the_vault:champion_loot_lvl0", 0.2F));

        EntityLootTable hostileVaultConfig = new EntityLootTable(hostileVaultLootTables);
        vaultTags.put("custom_mob", hostileVaultConfig);

        // tag-based loot for overworld
        List<LeveledLootTable> passiveOverworldLootTables = new ArrayList<>();
        passiveOverworldLootTables.add(new LeveledLootTable(0, "the_vault:champion_loot_lvl0", 0.1F));

        EntityLootTable passiveOverworldConfig = new EntityLootTable(passiveOverworldLootTables);
        overworldTags.put("custom_mob", passiveOverworldConfig);
    }

    public LootTableInfo getLootTableForEntity(String entityId, int vaultLevel, boolean inVault, List<String> entityTags) {
        // entity-specific loot table
        Map<String, EntityLootTable> lootMap = inVault ? vaultLoot : overworldLoot;
        EntityLootTable config = lootMap.get(entityId);

        if (config != null && !config.lootTables.isEmpty()) {
            LeveledLootTable highestApplicable = findHighestApplicableLootTable(config.lootTables, vaultLevel);
            if (highestApplicable != null) {
                return new LootTableInfo(highestApplicable.lootTable, highestApplicable.probability);
            }
        }

        // If no entity-specific loot table, tag time
        Map<String, EntityLootTable> tagMap = inVault ? vaultTags : overworldTags;
        for (String tag : entityTags) {
            config = tagMap.get(tag);
            if (config != null && !config.lootTables.isEmpty()) {
                LeveledLootTable highestApplicable = findHighestApplicableLootTable(config.lootTables, vaultLevel);
                if (highestApplicable != null) {
                    return new LootTableInfo(highestApplicable.lootTable, highestApplicable.probability);
                }
            }
        }

        return null;
    }

    private LeveledLootTable findHighestApplicableLootTable(List<LeveledLootTable> lootTables, int vaultLevel) {
        // check vault level against min level
        LeveledLootTable highestApplicable = null;

        for (LeveledLootTable lootTable : lootTables) {
            if (lootTable.minLevel <= vaultLevel &&
                    (highestApplicable == null || lootTable.minLevel > highestApplicable.minLevel)) {
                highestApplicable = lootTable;
            }
        }

        return highestApplicable;
    }

    public static class EntityLootTable {
        @Expose
        public List<LeveledLootTable> lootTables;

        public EntityLootTable() {
            this.lootTables = new ArrayList<>();
        }

        public EntityLootTable(List<LeveledLootTable> lootTables) {
            this.lootTables = lootTables;
        }
    }

    public static class LeveledLootTable {
        @Expose
        public int minLevel;

        @Expose
        public String lootTable;

        @Expose
        public float probability;

        public LeveledLootTable() {}

        public LeveledLootTable(int minLevel, String lootTable, float probability) {
            this.minLevel = minLevel;
            this.lootTable = lootTable;
            this.probability = probability;
        }
    }

    public static class LootTableInfo {
        public final String lootTableId;
        public final float probability;

        public LootTableInfo(String lootTableId, float probability) {
            this.lootTableId = lootTableId;
            this.probability = probability;
        }
    }
}