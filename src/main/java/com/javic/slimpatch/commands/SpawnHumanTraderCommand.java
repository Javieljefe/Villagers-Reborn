package com.javic.slimpatch.commands;

import com.javic.slimpatch.ModEntities;
import com.javic.slimpatch.entity.HumanWanderingTraderEntity;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.horse.TraderLlama;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class SpawnHumanTraderCommand {

    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("spawn_human_trader_test")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> execute(context.getSource()))
        );
    }

    private static int execute(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        BlockPos pos = BlockPos.containing(source.getPosition());

        try {
            var traderType = ModEntities.HUMAN_WANDERING_TRADER.get();
            HumanWanderingTraderEntity trader = traderType.create(level);
            if (trader == null) {
                source.sendFailure(Component.literal("No se pudo crear el trader humano."));
                return 0;
            }

            trader.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, level.random.nextFloat() * 360F, 0F);
            trader.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null);
            if (!level.addFreshEntity(trader)) {
                source.sendFailure(Component.literal("No se pudo añadir el trader humano al mundo."));
                return 0;
            }
            trader.setDespawnDelay(48000);
            trader.setWanderTarget(pos);
            trader.restrictTo(pos, 16);

            for (int i = 0; i < 2; i++) {
                double offset = (i == 0 ? 2.0 : -2.0);
                BlockPos llamaPos = pos.offset((int) offset, 0, 0);
                TraderLlama llama = EntityType.TRADER_LLAMA.spawn(level, llamaPos, MobSpawnType.EVENT);
                if (llama != null) {
                    llama.setLeashedTo(trader, true);
                }
            }

            source.sendSuccess(() -> Component.literal("Trader humano spawneado con dos llamas atadas."), false);
            return 1;

        } catch (Exception e) {
            source.sendFailure(Component.literal("Error al crear el trader humano."));
            return 0;
        }
    }
}
