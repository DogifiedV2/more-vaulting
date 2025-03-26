package com.dog.morevaulting.mixin;

import com.dog.morevaulting.MoreVaulting;
import iskallia.vault.core.data.key.ThemeKey;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Modifiers;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultRegistry;
import iskallia.vault.core.vault.WorldManager;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.vault.objective.Objective;
import iskallia.vault.core.vault.objective.Objectives;
import iskallia.vault.core.vault.player.ClassicListenersLogic;
import iskallia.vault.core.vault.player.Completion;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.core.vault.player.Runner;
import iskallia.vault.core.vault.stat.StatCollector;
import iskallia.vault.core.vault.stat.StatsCollector;
import iskallia.vault.core.world.storage.VirtualWorld;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.network.chat.*;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(ClassicListenersLogic.class)
public abstract class VaultOnTeleport {
    @Shadow public abstract String getVaultObjective(String key);

    @Unique
    public String getSpeedrunObjective(String key) {
        key = key == null ? "" : key.toLowerCase().replace("the_vault:", "");

        return switch (key) {
            case "boss" -> "Hunt the Guardians";
            case "monolith" -> "Brazier";
            case "elixir" -> "Elixir";
            case "scavenger" -> "Scavenger Hunt";
            case "brutal_bosses" -> "Brutal Bosses";
            case "bingo" -> "Bingo";
            case "empty", "" -> "";
            default -> key.substring(0, 1).toUpperCase() + key.substring(1);
        };
    }

    @Inject(method = "onTeleport",
            at = @At("TAIL"),
            remap = false)
    private void modifyOnTeleport(VirtualWorld world, Vault vault, ServerPlayer player, CallbackInfo ci) {
        String objective = getVaultObjective(((Objectives) vault.get(Vault.OBJECTIVES)).get(Objectives.KEY));

        MoreVaulting.LOGGER.info("Objective: " + objective);
        if (objective.toLowerCase().contains("speedrun")) {
            Objectives objectives = vault.get(Vault.OBJECTIVES);
            List<Objective> allObjectives = objectives.getAll(Objective.class);
            Objective obj = allObjectives.get(0);
            objective = getSpeedrunObjective(obj.getKey().getId().toString()) + " Speedrun";
        }

        ResourceLocation theme = ((WorldManager) vault.get(Vault.WORLD)).get(WorldManager.THEME);
        Optional<ThemeKey> themeKey = Optional.ofNullable((ThemeKey)VaultRegistry.THEME.getKey(theme));

        MutableComponent title = new TextComponent(objective)
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(14536734)));

        MutableComponent subtitle = new TextComponent(themeKey.map(ThemeKey::getName).orElse("Unknown"))
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(themeKey.map(ThemeKey::getColor).orElse(16777215))));

        player.connection.send(new ClientboundSetTitleTextPacket(title));
        player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
    }

    @Inject(method = "printJoinMessage",
            at = @At("HEAD"),
            remap = false, cancellable = true)
    private void printNewJoinMessage(VirtualWorld world, Vault vault, ServerPlayer player, CallbackInfo ci) {
        TextComponent text = new TextComponent("");
        AtomicBoolean startsWithVowel = new AtomicBoolean(false);
        ObjectIterator<Object2IntMap.Entry<VaultModifier<?>>> it = ((Modifiers)vault.get(Vault.MODIFIERS)).getDisplayGroup().object2IntEntrySet().iterator();

        while(it.hasNext()) {
            Object2IntMap.Entry<VaultModifier<?>> entry = (Object2IntMap.Entry)it.next();
            text.append(((VaultModifier)entry.getKey()).getChatDisplayNameComponent(entry.getIntValue()));
            if (it.hasNext()) {
                text.append(new TextComponent(", "));
            } else {
                text.append(new TextComponent(" "));
            }
        }

        TextComponent prefix = new TextComponent(startsWithVowel.get() ? " entered an " : " entered a ");
        String objective = this.getVaultObjective((String)((Objectives)vault.get(Vault.OBJECTIVES)).get(Objectives.KEY));
        if (!objective.isEmpty()) {
            objective = objective + " ";
        }

        if (objective.contains("Speedrun")) {
            Objectives objectives = vault.get(Vault.OBJECTIVES);
            List<Objective> allObjectives = objectives.getAll(Objective.class);
            Objective obj = allObjectives.get(0);
            objective = getSpeedrunObjective(obj.getKey().getId().toString()) + " Speedrun ";
        }

        text.append(objective + "Vault").append("!");
        prefix.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(16777215)));
        text.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(16777215)));
        MutableComponent playerName = player.getDisplayName().copy();
        playerName.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(9974168)));
        world.getServer().getPlayerList().broadcastMessage(playerName.append(prefix).append(text), ChatType.CHAT, player.getUUID());
        ci.cancel();
    }

    @Inject(method = "initServer",
            at = @At(value = "TAIL"),
            remap = false)
    private void modifyListenerLeaveEvent(VirtualWorld world, Vault vault, CallbackInfo ci) {
        CommonEvents.LISTENER_LEAVE.release(this);

        // Register our custom handler
        CommonEvents.LISTENER_LEAVE.register(this, (data) -> {
            ServerPlayer player = (ServerPlayer)data.getListener().getPlayer().orElse(null);
            if (data.getVault() == vault && data.getListener() instanceof Runner && player != null) {
                StatCollector stats = ((StatsCollector)vault.get(Vault.STATS)).get((UUID)data.getListener().get(Listener.ID));
                if (stats != null) {
                    Completion completion = stats.getCompletion();
                    String objective = getVaultObjective((String)((Objectives)vault.get(Vault.OBJECTIVES)).get(Objectives.KEY));

                    if (objective.toLowerCase().contains("speedrun")) {
                        Objectives objectives = vault.get(Vault.OBJECTIVES);
                        List<Objective> allObjectives = objectives.getAll(Objective.class);
                        Objective obj = allObjectives.get(0);
                        objective = getSpeedrunObjective(obj.getKey().getId().toString()) + " Speedrun";
                    }

                    if (!objective.isEmpty()) {
                        objective = objective + " ";
                    }

                    TextComponent prefix;
                    switch (completion) {
                        case COMPLETED -> prefix = new TextComponent(" completed a " + objective + "Vault!");
                        case BAILED -> prefix = new TextComponent(" survived a " + objective + "Vault.");
                        case FAILED -> prefix = new TextComponent(" was defeated in a " + objective + "Vault.");
                        default -> throw new IncompatibleClassChangeError();
                    }

                    prefix.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(16777215)));
                    MutableComponent playerName = player.getDisplayName().copy();
                    playerName.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(9974168)));
                    world.getServer().getPlayerList().broadcastMessage(playerName.append(prefix), ChatType.CHAT, player.getUUID());
                }
            }
        });
    }

}