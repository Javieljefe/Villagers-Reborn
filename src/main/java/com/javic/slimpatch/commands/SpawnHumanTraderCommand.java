package com.javic.slimpatch.commands;

import com.javic.slimpatch.ModEntities;
import com.javic.slimpatch.entity.HumanWanderingTraderEntity;
import com.javic.slimpatch.SlimPatch;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.TraderLlama;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class SpawnHumanTraderCommand {

    // 🔹 Registro del comando al evento correspondiente
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("spawn_human_trader_test")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> execute(context.getSource()))
        );
    }

    // 🔹 Ejecución real del comando
    private static int execute(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        BlockPos pos = BlockPos.containing(source.getPosition());

        try {
            // Crear el trader humano
            var traderType = ModEntities.HUMAN_WANDERING_TRADER.get();
            HumanWanderingTraderEntity trader = traderType.create(level);
            if (trader == null) {
                source.sendFailure(Component.literal("❌ No se pudo crear el trader humano."));
                return 0;
            }

            trader.moveTo(pos.getX(), pos.getY(), pos.getZ(), level.random.nextFloat() * 360F, 0F);
            level.addFreshEntity(trader);

            // Crear y atar las dos llamas
            for (int i = 0; i < 2; i++) {
                TraderLlama llama = EntityType.TRADER_LLAMA.create(level);
                if (llama != null) {
                    double offset = (i == 0 ? 2.0 : -2.0);
                    llama.moveTo(pos.getX() + offset, pos.getY(), pos.getZ(), level.random.nextFloat() * 360F, 0F);
                    llama.setLeashedTo(trader, true);
                    level.addFreshEntity(llama);
                }
            }

            source.sendSuccess(() -> Component.literal("✅ Trader humano spawneado con dos llamas atadas."), false);
            SlimPatch.LOGGER.info("[SlimPatch] Comando ejecutado: trader humano + 2 llamas.");
            return 1;

        } catch (Exception e) {
            SlimPatch.LOGGER.error("[SlimPatch] Error al ejecutar /spawn_human_trader_test", e);
            source.sendFailure(Component.literal("❌ Error al crear el trader humano."));
            return 0;
        }
    }
}