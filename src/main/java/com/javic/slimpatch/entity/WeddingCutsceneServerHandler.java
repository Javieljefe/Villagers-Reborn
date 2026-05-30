package com.javic.slimpatch.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class WeddingCutsceneServerHandler {

    private static final long CUTSCENE_DURATION_TICKS = 350L;
    private static final double CEREMONY_REPOSITION_DISTANCE_SQR = 0.0025D;
    private static final Map<UUID, CeremonyState> ACTIVE = new HashMap<>();

    private WeddingCutsceneServerHandler() {
    }

    public static void start(Villager villager, ServerPlayer player) {
        if (villager == null || player == null || villager.level() != player.level()) {
            return;
        }

        Vec3 holdPos = findCeremonyPosition(villager, player);
        if (holdPos == null) {
            holdPos = villager.position();
        } else {
            float yaw = getYawTowards(villager.position(), player.position());
            villager.moveTo(holdPos.x, holdPos.y, holdPos.z, yaw, villager.getXRot());
            villager.setYBodyRot(yaw);
            villager.setYHeadRot(yaw);
        }

        holdForCeremony(villager, holdPos);
        ACTIVE.put(villager.getUUID(), new CeremonyState(player.getUUID(), holdPos, villager.level().getGameTime() + CUTSCENE_DURATION_TICKS));
        villager.level().playSound(null, villager.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.7F, 1.0F);
        spawnParticles(villager, player, 8);
    }

    public static boolean tick(Villager villager) {
        CeremonyState state = ACTIVE.get(villager.getUUID());
        if (state == null) {
            return false;
        }
        if (villager.level().getGameTime() >= state.endsAt()) {
            finishCeremony(villager, state);
            ACTIVE.remove(villager.getUUID());
            return false;
        }

        if (!(villager.level().getPlayerByUUID(state.playerUuid()) instanceof ServerPlayer player) || !player.isAlive() || player.level() != villager.level()) {
            finishCeremony(villager, state);
            ACTIVE.remove(villager.getUUID());
            return false;
        }

        holdForCeremony(villager, state.holdPos());
        float yaw = getYawTowards(villager.position(), player.position());
        villager.setYRot(yaw);
        villager.setYBodyRot(yaw);
        villager.setYHeadRot(yaw);
        villager.getLookControl().setLookAt(player, 30.0F, 30.0F);

        if (villager.tickCount % 12 == 0) {
            spawnParticles(villager, player, 2);
        }
        return true;
    }

    private static void finishCeremony(Villager villager, CeremonyState state) {
        if (villager instanceof CommandableVillager commandableVillager
                && commandableVillager.getCommandState() == VillagerCommandState.STAY
                && state.holdPos() != null) {
            commandableVillager.setStayPos(BlockPos.containing(state.holdPos()));
            commandableVillager.setStayAnchorPos(state.holdPos());
        }
        villager.getNavigation().stop();
        villager.setDeltaMovement(Vec3.ZERO);
    }

    private static void holdForCeremony(Villager villager, Vec3 holdPos) {
        villager.setTarget(null);
        villager.setAggressive(false);
        villager.getBrain().eraseMemory(MemoryModuleType.NEAREST_HOSTILE);
        villager.getBrain().eraseMemory(MemoryModuleType.AVOID_TARGET);
        villager.getBrain().eraseMemory(MemoryModuleType.IS_PANICKING);
        villager.getBrain().eraseMemory(MemoryModuleType.HURT_BY);
        villager.getBrain().eraseMemory(MemoryModuleType.HURT_BY_ENTITY);
        VillagerCommandHandler.holdForDialogue(villager, holdPos);
        if (holdPos != null && villager.position().distanceToSqr(holdPos) > CEREMONY_REPOSITION_DISTANCE_SQR) {
            villager.moveTo(holdPos.x, holdPos.y, holdPos.z, villager.getYRot(), villager.getXRot());
            villager.setDeltaMovement(Vec3.ZERO);
        }
    }

    private static Vec3 findCeremonyPosition(Villager villager, ServerPlayer player) {
        Vec3 offset = villager.position().subtract(player.position());
        Vec3 horizontal = new Vec3(offset.x, 0.0D, offset.z);
        if (horizontal.lengthSqr() < 1.0E-4D) {
            Vec3 look = player.getLookAngle();
            horizontal = new Vec3(-look.x, 0.0D, -look.z);
        }
        if (horizontal.lengthSqr() < 1.0E-4D) {
            horizontal = new Vec3(0.0D, 0.0D, 1.0D);
        }
        horizontal = horizontal.normalize().scale(1.4D);

        Vec3 candidate = new Vec3(player.getX() + horizontal.x, villager.getY(), player.getZ() + horizontal.z);
        AABB movedBox = villager.getBoundingBox().move(candidate.x - villager.getX(), candidate.y - villager.getY(), candidate.z - villager.getZ());
        BlockPos below = BlockPos.containing(candidate.x, candidate.y - 0.2D, candidate.z).below();
        if (villager.level().noCollision(villager, movedBox) && !villager.level().getBlockState(below).isAir()) {
            return candidate;
        }
        return null;
    }

    private static float getYawTowards(Vec3 from, Vec3 to) {
        double dx = to.x - from.x;
        double dz = to.z - from.z;
        return Mth.wrapDegrees((float) (Math.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F);
    }

    private static void spawnParticles(Villager villager, ServerPlayer player, int count) {
        if (villager.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            Vec3 midpoint = villager.position().add(player.position()).scale(0.5D);
            serverLevel.sendParticles(ParticleTypes.CHERRY_LEAVES, midpoint.x, midpoint.y + 1.55D, midpoint.z, count, 0.38D, 0.3D, 0.38D, 0.015D);
            serverLevel.sendParticles(ParticleTypes.GLOW, midpoint.x, midpoint.y + 1.15D, midpoint.z, Math.max(1, count / 2), 0.3D, 0.25D, 0.3D, 0.01D);
        }
    }

    private record CeremonyState(UUID playerUuid, Vec3 holdPos, long endsAt) {
    }
}
