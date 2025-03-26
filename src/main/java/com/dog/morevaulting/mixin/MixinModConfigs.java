package com.dog.morevaulting.mixin;

import com.dog.morevaulting.config.CustomVaultConfigRegistry;
import com.dog.morevaulting.events.SetupEvents;
import iskallia.vault.init.ModConfigs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ModConfigs.class, remap = false)
public class MixinModConfigs {

    @Inject(method = "register", at = @At("TAIL"))
    private static void injectRegistries(CallbackInfo ci) {
        CustomVaultConfigRegistry.registerConfigs();
            SetupEvents.addManualConfigs();
    }
}