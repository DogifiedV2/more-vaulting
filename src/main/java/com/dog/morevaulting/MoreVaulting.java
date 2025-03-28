package com.dog.morevaulting;

import com.dog.morevaulting.init.ModCreativeTab;
import com.dog.morevaulting.init.ModItems;
import iskallia.vault.init.ModConfigs;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import com.dog.morevaulting.init.ModObjectiveEntries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.iwolfking.vhapi.api.registry.objective.CustomObjectiveRegistryEntry;

@Mod("morevaulting")
public class MoreVaulting {

    public static final String MODID = "morevaulting";
    public static final Logger LOGGER = LoggerFactory.getLogger("MoreVaulting");
    public static final ModCreativeTab MORE_OBJECTIVES_TAB = new ModCreativeTab(MODID);


    public MoreVaulting() {
        LOGGER.info("MoreVaulting Started");
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::clientSetup);

        ModItems.ITEMS.register(modEventBus);
        modEventBus.addGenericListener(CustomObjectiveRegistryEntry.class, ModObjectiveEntries::registerCustomObjectives);

        MinecraftForge.EVENT_BUS.register(this);

    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("MoreVaulting initialized");

    }

    private void clientSetup(final FMLClientSetupEvent event) {

    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MODID, path);
    }
}
