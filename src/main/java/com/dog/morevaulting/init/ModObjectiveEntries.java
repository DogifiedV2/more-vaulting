package com.dog.morevaulting.init;

import com.dog.morevaulting.objectives.speedrun.SpeedrunCrystalObjective;
import com.dog.morevaulting.objectives.speedrun.TrackSpeedrunSoloObjective;
import iskallia.vault.init.ModBlocks;
import net.minecraftforge.event.RegistryEvent;
import xyz.iwolfking.vhapi.api.registry.objective.CustomObjectiveRegistryEntry;
import xyz.iwolfking.woldsvaults.objectives.BrutalBossesObjective;

public class ModObjectiveEntries {
    public static final CustomObjectiveRegistryEntry SPEEDRUN_OBJECTIVE = new CustomObjectiveRegistryEntry.CustomObjectiveBuilder("speedrun_solo", "Solo Speedrun", SpeedrunCrystalObjective.class, SpeedrunCrystalObjective::new, TrackSpeedrunSoloObjective.KEY, TrackSpeedrunSoloObjective.class).build();

    public static void registerCustomObjectives(RegistryEvent.Register<CustomObjectiveRegistryEntry> event) {
        event.getRegistry().register(SPEEDRUN_OBJECTIVE);
    }

}
