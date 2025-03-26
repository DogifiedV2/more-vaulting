package com.dog.morevaulting.objectives.speedrun;

import com.dog.morevaulting.MoreVaulting;
import com.dog.morevaulting.config.CustomVaultConfigRegistry;
import com.dog.morevaulting.data.SpeedrunData;
import com.dog.morevaulting.data.SpeedrunDataHelper;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.core.Version;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.data.key.FieldKey;
import iskallia.vault.core.data.key.SupplierKey;
import iskallia.vault.core.data.key.registry.FieldRegistry;
import iskallia.vault.core.data.key.registry.KeyRegistry;
import iskallia.vault.core.data.sync.handler.SyncHandler;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.Objective;
import iskallia.vault.core.vault.objective.Objectives;
import iskallia.vault.core.vault.player.*;
import iskallia.vault.core.vault.stat.StatCollector;
import iskallia.vault.core.vault.stat.StatsCollector;
import iskallia.vault.core.vault.time.TickClock;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.item.crystal.data.adapter.IBitAdapter;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class TrackSpeedrunSoloObjective extends Objective {
    public static final SupplierKey<Objective> KEY = (SupplierKey<Objective>)SupplierKey.of("speedrun_solo", Objective.class)
            .with(Version.v1_1, TrackSpeedrunSoloObjective::new);

    public static final FieldRegistry FIELDS = (FieldRegistry)Objective.FIELDS.merge((KeyRegistry)new FieldRegistry());

    public static final FieldKey<Void> INITIALIZED = (FieldKey<Void>)FieldKey.of("initialized", Void.class)
            .with(Version.v1_1, (IBitAdapter)Adapters.ofVoid(), (SyncHandler)DISK.all())
            .register((KeyRegistry)FIELDS);

    public TrackSpeedrunSoloObjective() {
    }

    public static TrackSpeedrunSoloObjective create() {
        return new TrackSpeedrunSoloObjective();
    }

    public SupplierKey<Objective> getKey() {
        return KEY;
    }

    public FieldRegistry getFields() {
        return FIELDS;
    }

    public void initServer(VirtualWorld world, Vault vault) {
        CommonEvents.LISTENER_LEAVE.register(this, (event) -> {
            if (event.getVault() != vault) {
                return;
            }

            Listener listener = event.getListener();
            if (!(listener instanceof Runner runner)) {
                return;
            }

            // Check if the vault was completed
            if (!vault.has(Vault.STATS)) {
                return;
            }

            StatCollector playerStats = ((StatsCollector)vault.get(Vault.STATS)).get(runner.getId());
            if (playerStats == null || playerStats.getCompletion() != Completion.COMPLETED) {
                return;
            }

            SpeedrunDataHelper.recordVaultCompletion(vault, runner);
        });

        ((ListenersLogic)((Listeners)vault.get(Vault.LISTENERS)).get(Listeners.LOGIC)).set(ClassicListenersLogic.ADDED_BONUS_TIME);
    }

    public void tickServer(VirtualWorld world, Vault vault) {
        if (!has(INITIALIZED) && vault.has(Vault.CLOCK)) {
            SpeedrunData speedrunData = SpeedrunData.get(world.getServer());

            String objectiveType = "unknown";
            if (vault.has(Vault.OBJECTIVES) && vault.get(Vault.OBJECTIVES).has(iskallia.vault.core.vault.objective.Objectives.KEY)) {
                objectiveType = vault.get(Vault.OBJECTIVES).get(iskallia.vault.core.vault.objective.Objectives.KEY);
            }

            if (objectiveType.toLowerCase().contains("speedrun")) {
                Objectives objectives = vault.get(Vault.OBJECTIVES);
                List<Objective> allObjectives = objectives.getAll(Objective.class);
                Objective obj = allObjectives.get(0);
                objectiveType = obj.getKey().getId().toString().toLowerCase().replace("the_vault:", "");
            }

            List<SpeedrunData.SpeedrunEntry> fastestRuns = speedrunData.getFastestRunsByObjective(objectiveType, 1);

            if (CustomVaultConfigRegistry.SPEEDRUN.getCopyFastestTime() && !fastestRuns.isEmpty()) {
                int targetTime = fastestRuns.get(0).getTimeInTicks();
                ((TickClock)vault.get(Vault.CLOCK)).set(TickClock.DISPLAY_TIME, Integer.valueOf(targetTime));
            } else {
                ((TickClock)vault.get(Vault.CLOCK)).set(TickClock.DISPLAY_TIME, Integer.valueOf(CustomVaultConfigRegistry.SPEEDRUN.getDefaultTimeFromMap(objectiveType)));
            }

            set(INITIALIZED);
        }

        super.tickServer(world, vault);
    }

    @OnlyIn(Dist.CLIENT)
    public boolean render(Vault vault, PoseStack matrixStack, Window window, float partialTicks, Player player) {
        return false;
    }

    public boolean isActive(VirtualWorld world, Vault vault, Objective objective) {
        return objective == this;
    }


}