package com.javic.slimpatch.commands;

import com.javic.slimpatch.sounds.HumanVillagerSounds;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.network.chat.Component;

public class TestSoundCommand {

    public static void register(net.neoforged.neoforge.event.RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("testsound")
            .requires(cs -> cs.hasPermission(0))
            .executes(TestSoundCommand::playSound));
    }

    private static int playSound(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();

            // Obtenemos el evento desde HumanVillagerSounds
            SoundEvent sound = HumanVillagerSounds.maleClick();

            if (sound == null) {
                ctx.getSource().sendFailure(
                    Component.literal("⚠️ Error: SoundEvent 'male_click' no está registrado."));
                return 0;
            }

            // 🔊 reproducir sonido de prueba
            player.playNotifySound(
                sound,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F
            );

            ctx.getSource().sendSuccess(
                () -> Component.literal("✅ Reproduciendo sonido slimpatch:male_click"), false);

        } catch (CommandSyntaxException e) {
            ctx.getSource().sendFailure(
                Component.literal("No se pudo obtener el jugador."));
        }

        return Command.SINGLE_SUCCESS;
    }
}