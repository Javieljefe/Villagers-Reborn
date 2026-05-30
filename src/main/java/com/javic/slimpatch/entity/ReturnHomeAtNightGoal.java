package com.javic.slimpatch.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class ReturnHomeAtNightGoal extends Goal {

    private static final double RETURN_SPEED = 0.6D;
    private static final double HOME_ARRIVAL_DISTANCE_SQR = 2.25D;
    private static final int REPATH_TICKS = 20;
    private static final int RETRY_COOLDOWN_TICKS = 60;

    private final Villager villager;
    private final CommandableVillager commandableVillager;
    private int nextRepathTick;
    private int retryCooldownTick;

    public ReturnHomeAtNightGoal(Villager villager, CommandableVillager commandableVillager) {
        this.villager = villager;
        this.commandableVillager = commandableVillager;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return this.shouldReturnHome();
    }

    @Override
    public boolean canContinueToUse() {
        return this.shouldReturnHome();
    }

    @Override
    public void start() {
        this.nextRepathTick = 0;
        this.retryCooldownTick = 0;
        this.tryMoveHome();
    }

    @Override
    public void tick() {
        if (this.nextRepathTick > 0) {
            this.nextRepathTick--;
        }
        if (this.retryCooldownTick > 0) {
            this.retryCooldownTick--;
            return;
        }

        if (this.villager.getNavigation().isDone() && this.nextRepathTick <= 0) {
            this.tryMoveHome();
        }
    }

    @Override
    public void stop() {
        this.nextRepathTick = 0;
        this.retryCooldownTick = 0;
        this.villager.getNavigation().stop();
    }

    private boolean shouldReturnHome() {
        if (this.villager.level().isClientSide) {
            return false;
        }
        if (this.commandableVillager.getCommandState() != VillagerCommandState.NONE) {
            return false;
        }
        if (!this.commandableVillager.hasHome()) {
            return false;
        }
        BlockPos homePos = this.commandableVillager.getHomePos();
        if (homePos == null) {
            return false;
        }
        if (this.commandableVillager.getHomeDimension() == null || this.villager.level().dimension() != this.commandableVillager.getHomeDimension()) {
            return false;
        }
        if (!this.isNightOrDusk()) {
            return false;
        }
        return this.villager.position().distanceToSqr(Vec3.atBottomCenterOf(homePos)) > HOME_ARRIVAL_DISTANCE_SQR;
    }

    private boolean isNightOrDusk() {
        long timeOfDay = this.villager.level().getDayTime() % 24000L;
        return timeOfDay >= 12000L && timeOfDay < 23000L;
    }

    private void tryMoveHome() {
        BlockPos homePos = this.commandableVillager.getHomePos();
        if (homePos == null) {
            this.villager.getNavigation().stop();
            return;
        }

        boolean started = this.villager.getNavigation().moveTo(homePos.getX() + 0.5D, homePos.getY(), homePos.getZ() + 0.5D, RETURN_SPEED);
        if (started) {
            this.nextRepathTick = REPATH_TICKS;
            this.retryCooldownTick = 0;
        } else {
            this.nextRepathTick = 0;
            this.retryCooldownTick = RETRY_COOLDOWN_TICKS;
        }
    }
}
