package com.javic.slimpatch.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public final class VillagerCommandHandler {

    private static final String COMMAND_TAG = "SlimPatchCommand";
    private static final String TARGET_UUID_TAG = "SlimPatchCommandTarget";
    private static final String STAY_POS_TAG = "SlimPatchStayPos";
    private static final String HOME_POS_TAG = "SlimPatchHomePos";
    private static final String HOME_DIMENSION_TAG = "SlimPatchHomeDimension";
    private static final String OWNER_UUID_TAG = "SlimPatchCommandOwnerUuid";
    private static final String OWNER_NAME_TAG = "SlimPatchCommandOwnerName";
    private static final double STAY_MAX_DRIFT_SQR = 0.25D;
    private static final double FOLLOW_TELEPORT_DISTANCE_SQR = 48.0D * 48.0D;
    private static final long FOLLOW_TELEPORT_COOLDOWN_TICKS = 100L;

    private VillagerCommandHandler() {
    }

    public static void applyCommand(Villager villager, CommandableVillager commandableVillager, VillagerCommandState commandState, ServerPlayer player) {
        assignOwner(commandableVillager, player);
        commandableVillager.setCommandState(commandState);
        if (commandState == VillagerCommandState.FOLLOW) {
            commandableVillager.setCommandTargetUuid(player.getUUID());
            commandableVillager.setStayPos(null);
            commandableVillager.setStayAnchorPos(null);
        } else if (commandState == VillagerCommandState.STAY) {
            commandableVillager.setCommandTargetUuid(null);
            commandableVillager.setStayPos(villager.blockPosition());
            commandableVillager.setStayAnchorPos(villager.position());
        } else {
            resetCommand(villager, commandableVillager);
            return;
        }

        commandableVillager.setLastCommandTeleportTick(0L);
        updateNavigationForCommand(villager, commandableVillager.getCommandState());
        villager.getNavigation().stop();
    }

    public static void resetCommand(Villager villager, CommandableVillager commandableVillager) {
        commandableVillager.setCommandState(VillagerCommandState.NONE);
        commandableVillager.setCommandTargetUuid(null);
        commandableVillager.setStayPos(null);
        commandableVillager.setStayAnchorPos(null);
        commandableVillager.setLastCommandTeleportTick(0L);
        updateNavigationForCommand(villager, VillagerCommandState.NONE);
        villager.getNavigation().stop();
        releaseOwnerIfPossible(villager, commandableVillager);
    }

    public static void setHome(Villager villager, CommandableVillager commandableVillager, ServerPlayer player) {
        assignOwner(commandableVillager, player);
        commandableVillager.setHome(villager.blockPosition(), villager.level().dimension());
    }

    public static void clearHome(Villager villager, CommandableVillager commandableVillager) {
        commandableVillager.clearHome();
        releaseOwnerIfPossible(villager, commandableVillager);
    }

    public static void stopFollowing(Villager villager, CommandableVillager commandableVillager) {
        if (commandableVillager.getCommandState() == VillagerCommandState.FOLLOW) {
            commandableVillager.setCommandState(VillagerCommandState.NONE);
            commandableVillager.setCommandTargetUuid(null);
            commandableVillager.setLastCommandTeleportTick(0L);
            updateNavigationForCommand(villager, VillagerCommandState.NONE);
            villager.getNavigation().stop();
            releaseOwnerIfPossible(villager, commandableVillager);
        }
    }

    public static void moveFreely(Villager villager, CommandableVillager commandableVillager) {
        if (commandableVillager.getCommandState() == VillagerCommandState.STAY) {
            commandableVillager.setCommandState(VillagerCommandState.NONE);
            commandableVillager.setStayPos(null);
            commandableVillager.setStayAnchorPos(null);
            commandableVillager.setLastCommandTeleportTick(0L);
            updateNavigationForCommand(villager, VillagerCommandState.NONE);
            villager.getNavigation().stop();
            releaseOwnerIfPossible(villager, commandableVillager);
        }
    }

    public static boolean canUseProtectedAction(Villager villager, CommandableVillager commandableVillager, ServerPlayer player) {
        if (villager instanceof FamilyVillager familyVillager
                && familyVillager.getRelationshipStage() == VillagerRelationshipStage.MARRIED
                && familyVillager.getSpousePlayerUuid() != null) {
            if (familyVillager.getSpousePlayerUuid().equals(player.getUUID())) {
                if (!familyVillager.getSpousePlayerName().equals(player.getGameProfile().getName())) {
                    familyVillager.setSpousePlayerName(player.getGameProfile().getName());
                }
                return true;
            }

            String spouseName = familyVillager.getSpousePlayerName();
            if (spouseName == null || spouseName.isEmpty()) {
                player.displayClientMessage(net.minecraft.network.chat.Component.translatable("slimpatch.message.villager_married"), true);
            } else {
                player.displayClientMessage(net.minecraft.network.chat.Component.translatable("slimpatch.message.villager_married_to", spouseName), true);
            }
            return false;
        }

        if (!usesMultiplayerOwnership(villager)) {
            return true;
        }

        UUID ownerUuid = commandableVillager.getCommandOwnerUuid();
        if (ownerUuid == null) {
            assignOwner(commandableVillager, player);
            return true;
        }

        if (ownerUuid.equals(player.getUUID())) {
            commandableVillager.setCommandOwnerName(player.getGameProfile().getName());
            return true;
        }

        String ownerName = commandableVillager.getCommandOwnerName();
        if (ownerName == null || ownerName.isEmpty()) {
            ownerName = "another player";
        }
        player.displayClientMessage(net.minecraft.network.chat.Component.translatable("slimpatch.message.villager_controlled", ownerName), true);
        return false;
    }

    public static void tick(Villager villager, CommandableVillager commandableVillager) {
        VillagerCommandState commandState = commandableVillager.getCommandState();
        if (commandState == VillagerCommandState.NONE) {
            updateNavigationForCommand(villager, VillagerCommandState.NONE);
            return;
        }

        updateNavigationForCommand(villager, commandState);
        villager.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        villager.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        if (villager.isSleeping()) {
            villager.stopSleeping();
        }

        if (commandState == VillagerCommandState.STAY) {
            villager.getNavigation().stop();
            Vec3 stayAnchorPos = commandableVillager.getStayAnchorPos();
            if (stayAnchorPos == null) {
                BlockPos stayPos = commandableVillager.getStayPos();
                if (stayPos != null) {
                    commandableVillager.setStayAnchorPos(resolveAnchorPos(villager, stayPos));
                    stayAnchorPos = commandableVillager.getStayAnchorPos();
                }
            }
            if (stayAnchorPos == null) {
                commandableVillager.setStayPos(villager.blockPosition());
                commandableVillager.setStayAnchorPos(villager.position());
                return;
            }

            if (villager.position().distanceToSqr(stayAnchorPos) > STAY_MAX_DRIFT_SQR) {
                villager.teleportTo(stayAnchorPos.x, stayAnchorPos.y, stayAnchorPos.z);
            }
            villager.setDeltaMovement(Vec3.ZERO);
            return;
        }

        if (commandState != VillagerCommandState.FOLLOW) {
            return;
        }

        if (commandableVillager.getCommandTargetUuid() == null) {
            return;
        }

        if (!(villager.level().getPlayerByUUID(commandableVillager.getCommandTargetUuid()) instanceof ServerPlayer player)) {
            return;
        }

        if (!player.isAlive() || player.level() != villager.level()) {
            return;
        }

        if (villager.distanceToSqr(player) < FOLLOW_TELEPORT_DISTANCE_SQR) {
            return;
        }

        long gameTime = villager.level().getGameTime();
        if (gameTime - commandableVillager.getLastCommandTeleportTick() < FOLLOW_TELEPORT_COOLDOWN_TICKS) {
            return;
        }

        if (tryTeleportNearPlayer(villager, player)) {
            commandableVillager.setLastCommandTeleportTick(gameTime);
        }
    }

    public static void holdForDialogue(Villager villager, Vec3 holdPos) {
        villager.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        villager.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        villager.getNavigation().stop();
        if (villager.isSleeping()) {
            villager.stopSleeping();
        }

        if (holdPos != null) {
            if (villager.position().distanceToSqr(holdPos) > STAY_MAX_DRIFT_SQR) {
                villager.teleportTo(holdPos.x, holdPos.y, holdPos.z);
            }
        }

        villager.setDeltaMovement(Vec3.ZERO);
    }

    public static TemporaryCommandStateSnapshot createTemporaryStaySnapshot(CommandableVillager commandableVillager) {
        return new TemporaryCommandStateSnapshot(
                commandableVillager.getCommandState(),
                commandableVillager.getCommandTargetUuid(),
                commandableVillager.getStayPos(),
                commandableVillager.getStayAnchorPos(),
                commandableVillager.getLastCommandTeleportTick()
        );
    }

    public static boolean beginTemporaryStay(Villager villager, CommandableVillager commandableVillager) {
        if (commandableVillager.getCommandState() == VillagerCommandState.STAY) {
            return false;
        }

        commandableVillager.setCommandState(VillagerCommandState.STAY);
        commandableVillager.setCommandTargetUuid(null);
        commandableVillager.setStayPos(villager.blockPosition());
        commandableVillager.setStayAnchorPos(villager.position());
        commandableVillager.setLastCommandTeleportTick(0L);
        updateNavigationForCommand(villager, VillagerCommandState.STAY);
        holdForDialogue(villager, commandableVillager.getStayAnchorPos());
        return true;
    }

    public static void restoreTemporaryStay(Villager villager, CommandableVillager commandableVillager, TemporaryCommandStateSnapshot snapshot) {
        if (snapshot == null) {
            return;
        }

        commandableVillager.setCommandState(snapshot.commandState());
        commandableVillager.setCommandTargetUuid(snapshot.commandTargetUuid());
        commandableVillager.setStayPos(snapshot.stayPos());
        commandableVillager.setStayAnchorPos(snapshot.stayAnchorPos());
        commandableVillager.setLastCommandTeleportTick(snapshot.lastCommandTeleportTick());
        updateNavigationForCommand(villager, snapshot.commandState());
        villager.getNavigation().stop();
    }

    public static void suppressBedBehaviorForHomeAtNight(Villager villager, CommandableVillager commandableVillager) {
        if (!commandableVillager.hasHome()) {
            return;
        }
        if (commandableVillager.getHomePos() == null || commandableVillager.getHomeDimension() == null) {
            return;
        }
        if (villager.level().dimension() != commandableVillager.getHomeDimension()) {
            return;
        }
        if (!isNightOrDusk(villager)) {
            return;
        }

        villager.getBrain().eraseMemory(MemoryModuleType.HOME);
        if (villager.isSleeping()) {
            villager.stopSleeping();
        }
    }

    public static boolean shouldIgnoreDamage(Villager villager, CommandableVillager commandableVillager, DamageSource source) {
        if (commandableVillager.getCommandState() != VillagerCommandState.FOLLOW) {
            return false;
        }

        if (source.is(DamageTypes.FALL) || source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.FLY_INTO_WALL) || source.is(DamageTypes.CRAMMING)) {
            return true;
        }

        if (commandableVillager.getCommandTargetUuid() == null) {
            return false;
        }

        Entity attacker = source.getEntity();
        if (attacker != null && commandableVillager.getCommandTargetUuid().equals(attacker.getUUID())) {
            return true;
        }

        Entity directAttacker = source.getDirectEntity();
        return directAttacker != null && commandableVillager.getCommandTargetUuid().equals(directAttacker.getUUID());
    }

    private static boolean tryTeleportNearPlayer(Villager villager, ServerPlayer player) {
        BlockPos origin = player.blockPosition();
        for (int radius = 1; radius <= 2; radius++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    double targetX = origin.getX() + x + 0.5D;
                    double targetY = player.getY();
                    double targetZ = origin.getZ() + z + 0.5D;
                    if (!villager.level().noCollision(villager, villager.getBoundingBox().move(targetX - villager.getX(), targetY - villager.getY(), targetZ - villager.getZ()))) {
                        continue;
                    }

                    villager.teleportTo(targetX, targetY, targetZ);
                    villager.getNavigation().stop();
                    villager.setDeltaMovement(Vec3.ZERO);
                    return true;
                }
            }
        }

        return false;
    }

    public static void load(CompoundTag tag, CommandableVillager commandableVillager) {
        if (tag.contains(COMMAND_TAG)) {
            try {
                commandableVillager.setCommandState(VillagerCommandState.valueOf(tag.getString(COMMAND_TAG)));
            } catch (IllegalArgumentException e) {
                commandableVillager.setCommandState(VillagerCommandState.NONE);
            }
        } else {
            commandableVillager.setCommandState(VillagerCommandState.NONE);
        }

        commandableVillager.setCommandTargetUuid(tag.hasUUID(TARGET_UUID_TAG) ? tag.getUUID(TARGET_UUID_TAG) : null);
        commandableVillager.setCommandOwnerUuid(tag.hasUUID(OWNER_UUID_TAG) ? tag.getUUID(OWNER_UUID_TAG) : null);
        commandableVillager.setCommandOwnerName(tag.contains(OWNER_NAME_TAG) ? tag.getString(OWNER_NAME_TAG) : "");
        commandableVillager.setStayPos(tag.contains(STAY_POS_TAG) ? BlockPos.of(tag.getLong(STAY_POS_TAG)) : null);
        commandableVillager.setStayAnchorPos(null);
        if (tag.contains(HOME_POS_TAG) && tag.contains(HOME_DIMENSION_TAG)) {
            ResourceLocation homeDimensionId = ResourceLocation.tryParse(tag.getString(HOME_DIMENSION_TAG));
            if (homeDimensionId != null) {
                commandableVillager.setHome(BlockPos.of(tag.getLong(HOME_POS_TAG)), ResourceKey.create(Registries.DIMENSION, homeDimensionId));
            } else {
                commandableVillager.clearHome();
            }
        } else {
            commandableVillager.clearHome();
        }
        commandableVillager.setLastCommandTeleportTick(0L);
    }

    public static void updateNavigationForCommand(Villager villager, VillagerCommandState commandState) {
        boolean canOpenDoors = commandState == VillagerCommandState.FOLLOW;
        if (villager.getNavigation() instanceof GroundPathNavigation navigation) {
            navigation.setCanOpenDoors(true);
            navigation.setCanPassDoors(true);
        }
    }

    private static boolean isNightOrDusk(Villager villager) {
        long timeOfDay = villager.level().getDayTime() % 24000L;
        return timeOfDay >= 12000L && timeOfDay < 23000L;
    }

    private static Vec3 resolveAnchorPos(Villager villager, BlockPos stayPos) {
        double x = stayPos.getX() + 0.5D;
        double z = stayPos.getZ() + 0.5D;
        double y = villager.getY();
        if (villager.onGround()) {
            y = Math.max(y, villager.level().getBlockFloorHeight(stayPos) + stayPos.getY());
        }
        return new Vec3(x, y, z);
    }

    public static void save(CompoundTag tag, CommandableVillager commandableVillager) {
        tag.putString(COMMAND_TAG, commandableVillager.getCommandState().name());

        if (commandableVillager.getCommandTargetUuid() != null) {
            tag.putUUID(TARGET_UUID_TAG, commandableVillager.getCommandTargetUuid());
        } else {
            tag.remove(TARGET_UUID_TAG);
        }

        if (commandableVillager.getCommandOwnerUuid() != null) {
            tag.putUUID(OWNER_UUID_TAG, commandableVillager.getCommandOwnerUuid());
        } else {
            tag.remove(OWNER_UUID_TAG);
        }

        if (commandableVillager.getCommandOwnerName() != null && !commandableVillager.getCommandOwnerName().isEmpty()) {
            tag.putString(OWNER_NAME_TAG, commandableVillager.getCommandOwnerName());
        } else {
            tag.remove(OWNER_NAME_TAG);
        }

        if (commandableVillager.getStayPos() != null) {
            tag.putLong(STAY_POS_TAG, commandableVillager.getStayPos().asLong());
        } else {
            tag.remove(STAY_POS_TAG);
        }

        if (commandableVillager.hasHome() && commandableVillager.getHomePos() != null && commandableVillager.getHomeDimension() != null) {
            tag.putLong(HOME_POS_TAG, commandableVillager.getHomePos().asLong());
            tag.putString(HOME_DIMENSION_TAG, commandableVillager.getHomeDimension().location().toString());
        } else {
            tag.remove(HOME_POS_TAG);
            tag.remove(HOME_DIMENSION_TAG);
        }
    }

    private static void assignOwner(CommandableVillager commandableVillager, ServerPlayer player) {
        commandableVillager.setCommandOwnerUuid(player.getUUID());
        commandableVillager.setCommandOwnerName(player.getGameProfile().getName());
    }

    private static void releaseOwnerIfPossible(Villager villager, CommandableVillager commandableVillager) {
        if (!usesMultiplayerOwnership(villager)) {
            return;
        }

        if (commandableVillager.getCommandState() == VillagerCommandState.NONE && !commandableVillager.hasHome()) {
            commandableVillager.setCommandOwnerUuid(null);
            commandableVillager.setCommandOwnerName("");
        }
    }

    private static boolean usesMultiplayerOwnership(Villager villager) {
        return villager.getServer() != null && (villager.getServer().isDedicatedServer() || villager.getServer().isPublished());
    }

    public record TemporaryCommandStateSnapshot(
            VillagerCommandState commandState,
            UUID commandTargetUuid,
            BlockPos stayPos,
            Vec3 stayAnchorPos,
            long lastCommandTeleportTick
    ) {
    }
}
