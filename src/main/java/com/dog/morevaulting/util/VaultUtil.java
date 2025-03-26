package com.dog.morevaulting.util;

import iskallia.vault.core.vault.Vault;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.Optional;
import java.util.UUID;

public class VaultUtil {

    public static ServerPlayer getPlayerByUUID(UUID uuid) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server.getPlayerList().getPlayer(uuid);
    }
}
