package com.dog.morevaulting.events;

import com.dog.morevaulting.MoreVaulting;
import com.dog.morevaulting.config.ConfigLoader;

public class SetupEvents {

    public static void addManualConfigs() {
        MoreVaulting.LOGGER.info("Loading objective configs");

        // Load tooltips config
        ConfigLoader.loadOrCreateConfig(
                "/vhapi_configs/tooltips.json",
                "morevaulting_tooltips.json",
                MoreVaulting.id("tooltips/more_tooltips")
        );

        // Load objective seals config
        ConfigLoader.loadOrCreateConfig(
                "/vhapi_configs/objective_seals.json",
                "morevaulting_seals.json",
                MoreVaulting.id("vault/crystal/more_objective_seals")
        );
    }
}