package com.dog.morevaulting.objectives.speedrun;


import com.dog.morevaulting.MoreVaulting;
import com.dog.morevaulting.config.CustomVaultConfigRegistry;
import com.dog.morevaulting.config.SpeedrunConfig;
import com.google.gson.JsonObject;
import iskallia.vault.VaultMod;
import iskallia.vault.block.VaultCrateBlock;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.ClassicPortalLogic;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.*;
import iskallia.vault.core.vault.player.ClassicListenersLogic;
import iskallia.vault.core.vault.player.Listeners;
import iskallia.vault.core.world.roll.IntRoll;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.item.crystal.objective.BingoCrystalObjective;
import iskallia.vault.item.crystal.objective.CrystalObjective;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Unique;
import xyz.iwolfking.woldsvaults.objectives.BrutalBossesObjective;

import java.util.List;
import java.util.Optional;

public class SpeedrunCrystalObjective extends CrystalObjective {
    protected IntRoll target;
    protected IntRoll wave;
    protected float objectiveProbability;
    public SpeedrunCrystalObjective() {
    }

    public SpeedrunCrystalObjective(IntRoll target, IntRoll wave, float objectiveProbability) {
        this.target = target;
        this.wave = wave;
        this.objectiveProbability = objectiveProbability;
    }
    @Override
    public void configure(Vault vault, RandomSource random) {
        Object object = ((Listeners)vault.get(Vault.LISTENERS)).get(Listeners.LOGIC); if (object instanceof ClassicListenersLogic classic) {
            classic.set(ClassicListenersLogic.MAX_PLAYERS, Integer.valueOf(1));
        }

        int level = vault.get(Vault.LEVEL).get();
        vault.ifPresent(Vault.OBJECTIVES, objectives -> {

            SpeedrunConfig config = CustomVaultConfigRegistry.SPEEDRUN;
            String selectedObjective;
            if (config.getRandomObjectiveEnabled()) {
                List<String> list = config.getRandomObjectiveList();
                selectedObjective = list.get(random.nextInt(list.size()));
            } else {
                selectedObjective = config.getSetObjective();
            }

            switch (selectedObjective) {
                case "elixir" -> objectives.add(
                        ElixirObjective.create()
                                .add(LodestoneObjective.of(this.objectiveProbability)
                                        .add(AwardCrateObjective.ofConfig(VaultCrateBlock.Type.ELIXIR, "elixir", level, true))
                                        .add(VictoryObjective.of(300)))
                );

                case "scavenger" -> objectives.add(
                        ScavengerObjective.of(this.objectiveProbability, ScavengerObjective.Config.DEFAULT)
                                .add(AwardCrateObjective.ofConfig(VaultCrateBlock.Type.SCAVENGER, "scavenger", level, true))
                                .add(VictoryObjective.of(300))
                );

                case "bingo" -> ModConfigs.BINGO.generate(VaultMod.id("default"), level).ifPresent(task ->
                        objectives.add(
                                BingoObjective.of(task)
                                        .add(GridGatewayObjective.of(this.objectiveProbability)
                                                .add(AwardCrateObjective.ofConfig(VaultCrateBlock.Type.BINGO, "bingo", level, true)))
                                        .add(VictoryObjective.of(300))
                        )
                );

                case "monolith" -> objectives.add(
                        MonolithObjective.of(5, this.objectiveProbability,
                                        ModConfigs.MONOLITH.getStackModifierPool(level),
                                        ModConfigs.MONOLITH.getOverStackModifierPool(level),
                                        ModConfigs.MONOLITH.getOverStackLootTable(level))
                                .add(FindExitObjective.create(ClassicPortalLogic.EXIT))
                                .add(AwardCrateObjective.ofConfig(VaultCrateBlock.Type.MONOLITH, "monolith", level, true))
                );

                case "brutal_bosses" -> objectives.add(
                        BrutalBossesObjective.of(5, () -> 1, this.objectiveProbability)
                                .add(AwardCrateObjective.ofConfig(VaultCrateBlock.Type.BOSS, "brutal_bosses", level, true))
                                .add(VictoryObjective.of(300))
                );

                default -> {
                    MoreVaulting.LOGGER.error("Invalid random objective: " + selectedObjective + ". Using default monolith objective.");
                    objectives.add(
                            MonolithObjective.of(this.target.get(random), this.objectiveProbability,
                                            ModConfigs.MONOLITH.getStackModifierPool(level),
                                            ModConfigs.MONOLITH.getOverStackModifierPool(level),
                                            ModConfigs.MONOLITH.getOverStackLootTable(level))
                                    .add(FindExitObjective.create(ClassicPortalLogic.EXIT))
                                    .add(AwardCrateObjective.ofConfig(VaultCrateBlock.Type.MONOLITH, "monolith", level, true))
                    );
                }
            }
            objectives.add(DeathObjective.create(CustomVaultConfigRegistry.SPEEDRUN.getShouldTimerKill()));
            objectives.add(TrackSpeedrunSoloObjective.create());
            objectives.add(BailObjective.create(true, ClassicPortalLogic.EXIT));
            objectives.set(Objectives.KEY, CrystalData.OBJECTIVE.getType(this));
        });
    }

    @Override
    public void addText(List<Component> tooltip, int minIndex, TooltipFlag flag, float time) {
        SpeedrunConfig config = CustomVaultConfigRegistry.SPEEDRUN;
        if (!config.getRandomObjectiveEnabled()) {
            tooltip.add((new TextComponent("Objective: ")).append((new TextComponent(config.getSetObjective() + " Speedrun")).withStyle(Style.EMPTY.withColor(this.getColor(time).orElseThrow()))));
        } else {
            tooltip.add((new TextComponent("Objective: ")).append((new TextComponent("Speedrun")).withStyle(Style.EMPTY.withColor(this.getColor(time).orElseThrow()))));
        }
    }

    public Optional<Integer> getColor(float time) {
        return Optional.ofNullable(ChatFormatting.AQUA.getColor());
    }

    @Override
    public Optional<CompoundTag> writeNbt() {
        CompoundTag nbt = new CompoundTag();
        Adapters.INT_ROLL.writeNbt(this.target).ifPresent(target -> nbt.put("target", target));
        Adapters.INT_ROLL.writeNbt(this.wave).ifPresent(wave -> nbt.put("wave", wave));
        Adapters.FLOAT.writeNbt(this.objectiveProbability).ifPresent(tag -> nbt.put("objective_probability", tag));
        return Optional.of(nbt);
    }

    @Override
    public void readNbt(CompoundTag nbt) {
        this.target = Adapters.INT_ROLL.readNbt(nbt.getCompound("target")).orElse(null);
        this.wave = Adapters.INT_ROLL.readNbt(nbt.getCompound("wave")).orElse(IntRoll.ofConstant(3));
        this.objectiveProbability = Adapters.FLOAT.readNbt(nbt.get("objective_probability")).orElse(0.0F);
    }

    @Override
    public Optional<JsonObject> writeJson() {
        JsonObject json = new JsonObject();
        Adapters.INT_ROLL.writeJson(this.target).ifPresent(target -> json.add("target", target));
        Adapters.INT_ROLL.writeJson(this.wave).ifPresent(wave -> json.add("wave", wave));
        Adapters.FLOAT.writeJson(this.objectiveProbability).ifPresent(tag -> json.add("objective_probability", tag));
        return Optional.of(json);
    }

    @Override
    public void readJson(JsonObject json) {
        this.target = Adapters.INT_ROLL.readJson(json.getAsJsonObject("target")).orElse(null);
        this.wave = Adapters.INT_ROLL.readJson(json.getAsJsonObject("wave")).orElse(IntRoll.ofConstant(3));
        this.objectiveProbability = Adapters.FLOAT.readJson(json.get("objective_probability")).orElse(0.0F);
    }
}
