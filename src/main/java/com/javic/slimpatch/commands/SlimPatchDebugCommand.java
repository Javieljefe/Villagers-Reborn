package com.javic.slimpatch.commands;

import com.javic.slimpatch.entity.FamilyVillager;
import com.javic.slimpatch.entity.FamilyAgingHandler;
import com.javic.slimpatch.entity.FemaleVillagerEntity;
import com.javic.slimpatch.entity.MaleVillagerEntity;
import com.javic.slimpatch.entity.VillagerAgeStage;
import com.javic.slimpatch.entity.VillagerRelationshipData;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.javic.slimpatch.network.ModNetworking;
import com.javic.slimpatch.network.RelationshipSyncPacket;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.UUID;

public final class SlimPatchDebugCommand {

    private static final double RANGE = 8.0D;

    private SlimPatchDebugCommand() {
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("slimpatchdebug")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("relationship")
                                .then(Commands.literal("set")
                                        .then(Commands.argument("value", FloatArgumentType.floatArg())
                                                .executes(ctx -> setRelationship(ctx, FloatArgumentType.getFloat(ctx, "value"))))))
                        .then(Commands.literal("golden")
                                .then(Commands.literal("set")
                                        .then(Commands.argument("value", FloatArgumentType.floatArg())
                                                .executes(ctx -> setGolden(ctx, FloatArgumentType.getFloat(ctx, "value"))))))
                        .then(Commands.literal("family")
                                .then(Commands.literal("expecting")
                                        .then(Commands.literal("ready")
                                                .executes(SlimPatchDebugCommand::setExpectingReady))
                                        .then(Commands.literal("set")
                                                .then(Commands.argument("ticksRemaining", IntegerArgumentType.integer(0))
                                                        .executes(ctx -> setExpecting(ctx, IntegerArgumentType.getInteger(ctx, "ticksRemaining")))))
                                        .then(Commands.literal("clear")
                                                .executes(SlimPatchDebugCommand::clearExpecting)))
                                 .then(Commands.literal("age")
                                        .then(Commands.literal("ready")
                                                .executes(SlimPatchDebugCommand::setAgeReady))
                                        .then(Commands.literal("set")
                                                .then(Commands.literal("toddler")
                                                        .executes(ctx -> setAgeStage(ctx, VillagerAgeStage.TODDLER)))
                                                .then(Commands.literal("child")
                                                        .executes(ctx -> setAgeStage(ctx, VillagerAgeStage.CHILD)))
                                                .then(Commands.literal("teen")
                                                        .executes(ctx -> setAgeStage(ctx, VillagerAgeStage.TEEN)))
                                                .then(Commands.literal("adult")
                                                        .executes(ctx -> setAgeStage(ctx, VillagerAgeStage.ADULT))))))
        );
    }

    private static int setRelationship(CommandContext<CommandSourceStack> ctx, float value) {
        ServerPlayer player = getPlayer(ctx.getSource());
        if (player == null) {
            return 0;
        }

        Entity target = getLookedVillager(player);
        if (target == null) {
            ctx.getSource().sendFailure(Component.literal("Look at a Villagers Reborn villager first."));
            return 0;
        }

        float applied;
        if (target instanceof MaleVillagerEntity male) {
            UUID playerUuid = player.getUUID();
            if (VillagerRelationshipData.usesPerPlayerRelationships(male)) {
                applied = VillagerRelationshipData.setRelationshipForPlayer(male, playerUuid, value, male.getRelationship());
            } else {
                male.setRelationship(value);
                applied = male.getRelationship();
            }
        } else if (target instanceof FemaleVillagerEntity female) {
            UUID playerUuid = player.getUUID();
            if (VillagerRelationshipData.usesPerPlayerRelationships(female)) {
                applied = VillagerRelationshipData.setRelationshipForPlayer(female, playerUuid, value, female.getRelationship());
            } else {
                female.setRelationship(value);
                applied = female.getRelationship();
            }
        } else {
            ctx.getSource().sendFailure(Component.literal("Look at a Villagers Reborn villager first."));
            return 0;
        }

        ctx.getSource().sendSuccess(() -> Component.literal("Set relationship to " + applied + "."), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int setGolden(CommandContext<CommandSourceStack> ctx, float value) {
        ServerPlayer player = getPlayer(ctx.getSource());
        if (player == null) {
            return 0;
        }

        Entity target = getLookedVillager(player);
        if (!(target instanceof FamilyVillager familyVillager)) {
            ctx.getSource().sendFailure(Component.literal("Look at a Villagers Reborn villager first."));
            return 0;
        }

        float appliedGoldenRelationship;
        float relationship;
        if (target instanceof MaleVillagerEntity male) {
            if (VillagerRelationshipData.usesPerPlayerRelationships(male)) {
                appliedGoldenRelationship = VillagerRelationshipData.setGoldenRelationshipForPlayer(male, player.getUUID(), value, male.getGoldenRelationship());
                relationship = VillagerRelationshipData.getRelationshipForPlayer(male, player.getUUID(), male.getRelationship());
            } else {
                male.setGoldenRelationship(value);
                appliedGoldenRelationship = male.getGoldenRelationship();
                relationship = male.getRelationship();
            }
        } else if (target instanceof FemaleVillagerEntity female) {
            if (VillagerRelationshipData.usesPerPlayerRelationships(female)) {
                appliedGoldenRelationship = VillagerRelationshipData.setGoldenRelationshipForPlayer(female, player.getUUID(), value, female.getGoldenRelationship());
                relationship = VillagerRelationshipData.getRelationshipForPlayer(female, player.getUUID(), female.getRelationship());
            } else {
                female.setGoldenRelationship(value);
                appliedGoldenRelationship = female.getGoldenRelationship();
                relationship = female.getRelationship();
            }
        } else {
            familyVillager.setGoldenRelationship(value);
            appliedGoldenRelationship = familyVillager.getGoldenRelationship();
            relationship = 0.5F;
        }

        ModNetworking.sendToClient(new RelationshipSyncPacket(target.getId(), relationship, appliedGoldenRelationship), player);
        float finalAppliedGoldenRelationship = appliedGoldenRelationship;
        ctx.getSource().sendSuccess(() -> Component.literal("Set golden relationship to " + finalAppliedGoldenRelationship + "."), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int setExpectingReady(CommandContext<CommandSourceStack> ctx) {
        return setExpecting(ctx, 0);
    }

    private static int setExpecting(CommandContext<CommandSourceStack> ctx, int ticksRemaining) {
        ServerPlayer player = getPlayer(ctx.getSource());
        if (player == null) {
            return 0;
        }

        Entity target = getLookedVillager(player);
        if (!(target instanceof FamilyVillager familyVillager)) {
            ctx.getSource().sendFailure(Component.literal("Look at a Villagers Reborn family villager first."));
            return 0;
        }

        long gameTime = player.serverLevel().getGameTime();
        familyVillager.setExpectingChild(true);
        familyVillager.setExpectingParentPlayerUuid(player.getUUID());
        familyVillager.setExpectingParentPlayerName(player.getGameProfile().getName());
        familyVillager.setExpectingStartedAt(gameTime);
        familyVillager.setExpectingEndsAt(gameTime + ticksRemaining);
        ctx.getSource().sendSuccess(() -> Component.literal("Set expecting child with " + ticksRemaining + " ticks remaining."), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int clearExpecting(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = getPlayer(ctx.getSource());
        if (player == null) {
            return 0;
        }

        Entity target = getLookedVillager(player);
        if (!(target instanceof FamilyVillager familyVillager)) {
            ctx.getSource().sendFailure(Component.literal("Look at a Villagers Reborn family villager first."));
            return 0;
        }

        familyVillager.setExpectingChild(false);
        familyVillager.setExpectingParentPlayerUuid(null);
        familyVillager.setExpectingParentPlayerName("");
        familyVillager.setExpectingStartedAt(0L);
        familyVillager.setExpectingEndsAt(0L);
        ctx.getSource().sendSuccess(() -> Component.literal("Cleared expecting child state."), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int setAgeStage(CommandContext<CommandSourceStack> ctx, VillagerAgeStage ageStage) {
        ServerPlayer player = getPlayer(ctx.getSource());
        if (player == null) {
            return 0;
        }

        Entity target = getLookedVillager(player);
        if (!(target instanceof FamilyVillager familyVillager)) {
            ctx.getSource().sendFailure(Component.literal("Look at a Villagers Reborn family villager first."));
            return 0;
        }

        FamilyAgingHandler.setAgeStageWithTimer(familyVillager, ageStage, player.serverLevel().getGameTime());
        ctx.getSource().sendSuccess(() -> Component.literal("Set family age stage to " + ageStage.name().toLowerCase() + "."), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int setAgeReady(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = getPlayer(ctx.getSource());
        if (player == null) {
            return 0;
        }

        Entity target = getLookedVillager(player);
        if (!(target instanceof FamilyVillager familyVillager)) {
            ctx.getSource().sendFailure(Component.literal("Look at a Villagers Reborn family villager first."));
            return 0;
        }

        FamilyAgingHandler.setReadyForNextStage(familyVillager, player.serverLevel().getGameTime());
        ctx.getSource().sendSuccess(() -> Component.literal("Set family age transition ready for the next server tick."), false);
        return Command.SINGLE_SUCCESS;
    }

    private static ServerPlayer getPlayer(CommandSourceStack source) {
        try {
            return source.getPlayerOrException();
        } catch (CommandSyntaxException e) {
            source.sendFailure(Component.literal("This command requires a player looking at a villager."));
            return null;
        }
    }

    private static Entity getLookedVillager(ServerPlayer player) {
        Vec3 eyePosition = player.getEyePosition();
        Vec3 viewVector = player.getViewVector(1.0F);
        Vec3 targetPosition = eyePosition.add(viewVector.scale(RANGE));
        AABB searchBox = player.getBoundingBox().expandTowards(viewVector.scale(RANGE)).inflate(1.0D);
        EntityHitResult hitResult = ProjectileUtil.getEntityHitResult(
                player,
                eyePosition,
                targetPosition,
                searchBox,
                entity -> entity instanceof MaleVillagerEntity || entity instanceof FemaleVillagerEntity,
                RANGE * RANGE
        );
        return hitResult != null ? hitResult.getEntity() : null;
    }
}
