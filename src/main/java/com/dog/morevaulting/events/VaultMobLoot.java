package com.dog.morevaulting.events;

import com.dog.morevaulting.MoreVaulting;
import com.dog.morevaulting.config.CustomVaultConfigRegistry;
import com.dog.morevaulting.config.MobLootConfig;
import iskallia.vault.core.Version;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultLevel;
import iskallia.vault.core.vault.VaultRegistry;
import iskallia.vault.core.world.loot.generator.LootTableGenerator;
import iskallia.vault.util.LootInitialization;
import iskallia.vault.world.data.PlayerVaultStatsData;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Mod.EventBusSubscriber
public class VaultMobLoot {
    private static final Random RANDOM = new Random();

    /**
     * Custom loot tables for mobs in or outside of a vault
     */
    @SubscribeEvent
    public static void onEntityDropsNew(LivingDropsEvent event) {
        LivingEntity entity = event.getEntityLiving();

        if (entity.getTags().contains("no_drops")) {
            return;
        }

        Entity source = event.getSource().getEntity();
        if (!(source instanceof ServerPlayer player)) {
            return;
        }

        // Entity info!
        String entityId = entity.getType().getRegistryName().toString();
        List<String> entityTags = new ArrayList<>(entity.getTags());

        Optional<Vault> optVault = ServerVaults.get(player.getLevel());
        boolean inVault = optVault.isPresent();

        // default vault level based on player
        int vaultLevel = PlayerVaultStatsData.get(player.getLevel())
                .getVaultStats(player)
                .getVaultLevel();

        // if in vault, update level
        if (inVault && optVault.get().has(Vault.LEVEL)) {
            vaultLevel = ((VaultLevel) optVault.get().get(Vault.LEVEL)).get();
        }

        MobLootConfig config = CustomVaultConfigRegistry.MOB_LOOT;
        MobLootConfig.LootTableInfo lootTableInfo = config.getLootTableForEntity(
                entityId,
                vaultLevel,
                inVault,
                entityTags
        );

        // validate table, chance stuff
        if (lootTableInfo == null || RANDOM.nextFloat() > lootTableInfo.probability) {
            return;
        }

        String lootTableId = lootTableInfo.lootTableId;

        // Prevent a giant fucking error
        if (VaultRegistry.LOOT_TABLE.getKey(lootTableId) == null) {
            MoreVaulting.LOGGER.error(
                    "No loot table found: {} for entity {} at vault level {}",
                    lootTableId,
                    entityId,
                    vaultLevel
            );
            return;
        }

        LootTableGenerator generator = new LootTableGenerator(
                Version.latest(),
                VaultRegistry.LOOT_TABLE.getKey(lootTableId),
                0.0F
        );


        generator.setSource(player);
        generator.generate(JavaRandom.ofNanoTime());

        int finalVaultLevel = vaultLevel;
        Vault vault = optVault.orElseGet(() -> createTemporaryVault(finalVaultLevel));

        addDropsToEvent(generator, vault, entity, event);
    }

    /**
     * Creates a vault object with the level... otherwise loot defaults to level 0
     */
    private static Vault createTemporaryVault(int vaultLevel) {
        Vault vault = new Vault();
        vault.set(Vault.LEVEL, new VaultLevel().set(VaultLevel.VALUE, vaultLevel));
        return vault;
    }


    private static void addDropsToEvent(
            LootTableGenerator generator,
            Vault vault,
            LivingEntity entity,
            LivingDropsEvent event) {

        generator.getItems().forEachRemaining(item -> {
            item = LootInitialization.initializeVaultLoot(item, vault, entity.blockPosition());
            event.getDrops().add(new ItemEntity(
                    entity.level,
                    entity.getX(),
                    entity.getY(),
                    entity.getZ(),
                    item
            ));
        });
    }
}