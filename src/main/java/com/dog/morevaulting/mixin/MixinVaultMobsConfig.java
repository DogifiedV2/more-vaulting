package com.dog.morevaulting.mixin;

import com.dog.morevaulting.MoreVaulting;
import com.dog.morevaulting.config.CakeConfig;
import com.dog.morevaulting.config.CustomVaultConfigRegistry;
import com.dog.morevaulting.util.VaultUtil;
import iskallia.vault.config.Config;
import iskallia.vault.config.VaultMobsConfig;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.Objectives;
import iskallia.vault.world.VaultDifficulty;
import iskallia.vault.world.data.ServerVaults;
import iskallia.vault.world.data.WorldSettings;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;
import java.util.UUID;

@Mixin(VaultMobsConfig.class)
public class MixinVaultMobsConfig {

    @Redirect(method = "scale", at = @At(value = "INVOKE", target = "Liskallia/vault/world/data/WorldSettings;getPlayerDifficulty(Ljava/util/UUID;)Liskallia/vault/world/VaultDifficulty;"), remap = false)
    private static VaultDifficulty scale(WorldSettings instance, UUID playerId) {

        ServerPlayer player = VaultUtil.getPlayerByUUID(playerId);
        Optional<Vault> optVault = ServerVaults.get(player.level);

        if (!optVault.isPresent()) {
            return instance.getPlayerDifficulty(playerId);
        }

        Vault vault = optVault.get();
        if (!vault.get(Vault.OBJECTIVES).get(Objectives.KEY).equals("cake")) {
            return instance.getPlayerDifficulty(playerId);
        }

        CakeConfig CAKES = CustomVaultConfigRegistry.CAKES;
        String cakeDifficulty = CAKES.getCakeDifficulty();

        return switch (cakeDifficulty) {
            case "easy" -> VaultDifficulty.EASY;
            case "normal" -> VaultDifficulty.NORMAL;
            case "hard" -> VaultDifficulty.HARD;
            case "impossible" -> VaultDifficulty.IMPOSSIBLE;
            case "fragged" -> VaultDifficulty.FRAGGED;
            case "piece_of_cake" -> VaultDifficulty.PIECE_OF_CAKE;
            default -> instance.getPlayerDifficulty(playerId);
        };
    }
}
