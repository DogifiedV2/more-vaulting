package com.dog.morevaulting.init;

import com.dog.morevaulting.MoreVaulting;
import iskallia.vault.item.ItemVaultCrystalSeal;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Random;

@Mod.EventBusSubscriber(modid = MoreVaulting.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "morevaulting");
    public static final Random rand = new Random();


    public static ItemVaultCrystalSeal CRYSTAL_SEAL_SPEEDRUN;

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        registry.register(CRYSTAL_SEAL_SPEEDRUN);
    }

    static {
        CRYSTAL_SEAL_SPEEDRUN = new ItemVaultCrystalSeal(MoreVaulting.id("crystal_seal_speedrun"));
    }

}
