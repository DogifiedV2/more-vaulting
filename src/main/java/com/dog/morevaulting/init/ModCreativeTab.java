package com.dog.morevaulting.init;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.world.item.Items.DIAMOND;

public class ModCreativeTab extends CreativeModeTab {

    public ModCreativeTab(String label) {
        super(label);
    }

    @Override
    public @NotNull ItemStack makeIcon() {
        return new ItemStack(DIAMOND);
    }
}