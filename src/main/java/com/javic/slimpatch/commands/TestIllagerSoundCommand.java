package com.javic.slimpatch.commands;

import com.javic.slimpatch.sounds.HumanIllagerSounds;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class TestIllagerSoundCommand {

    public static void register(net.neoforged.neoforge.event.RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("testillager")
                        .requires(cs -> cs.hasPermission(0))
                        .then(Commands.argument("type", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    builder.suggest("male_ambient");
                                    builder.suggest("female_ambient");
                                    builder.suggest("male_hurt");
                                    builder.suggest("female_hurt");
                                    builder.suggest("male_death");
                                    builder.suggest("female_death");
                                    return builder.buildFuture();
                                })
                                .executes(TestIllagerSoundCommand::playSound))
        );
    }

    private static int playSound(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            String type = StringArgumentType.getString(ctx, "type");

            SoundEvent sound = switch (type) {
                case "male_ambient" -> HumanIllagerSounds.maleAmbient();
                case "female_ambient" -> HumanIllagerSounds.femaleAmbient();
                case "male_hurt" -> HumanIllagerSounds.maleHurt();
                case "female_hurt" -> HumanIllagerSounds.femaleHurt();
                case "male_death" -> HumanIllagerSounds.maleDeath();
                case "female_death" -> HumanIllagerSounds.femaleDeath();
                default -> null;
            };

            if (sound == null) {
                ctx.getSource().sendFailure(
                        Component.literal("Error: tipo desconocido o SoundEvent no registrado."));
                return 0;
            }

            player.playNotifySound(sound, SoundSource.HOSTILE, 1.0F, 1.0F);
            ctx.getSource().sendSuccess(
                    () -> Component.literal("Reproduciendo sonido: " + type), false);

        } catch (CommandSyntaxException e) {
            ctx.getSource().sendFailure(Component.literal("No se pudo obtener el jugador."));
        }

        return Command.SINGLE_SUCCESS;
    }
}