package com.javic.slimpatch.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.UUID;

public class VillagerCommandGoal extends Goal {

    private static final double FOLLOW_SPEED = 0.90D;
    private static final double RELAXED_WANDER_SPEED = 0.45D;
    private static final double FOLLOW_START_DISTANCE_SQR = 16.0D;
    private static final int FOLLOW_REPATH_TICKS = 10;
    private static final int FOLLOW_CLOSE_DOOR_TICKS = 8;
    private static final double RELAXED_WANDER_MIN_RADIUS = 3.0D;
    private static final double RELAXED_WANDER_MAX_RADIUS = 6.0D;
    private static final double RELAXED_WANDER_MAX_DISTANCE_SQR = 64.0D;
    private static final double RELAXED_TARGET_REACHED_DISTANCE_SQR = 2.25D;
    private static final double RELAXED_MOVE_AWAY_SPEED_SQR = 0.01D;
    private static final double RELAXED_MOVE_AWAY_DOT = 0.08D;
    private static final int RELAXED_WANDER_MIN_TICKS = 80;
    private static final int RELAXED_WANDER_MAX_TICKS = 140;
    private static final int RELAXED_RECENT_DAMAGE_TICKS = 60;

    private final Villager villager;
    private final CommandableVillager commandableVillager;
    private int nextRepathTick;
    private Vec3 lastTargetPos;
    private BlockPos openedDoorPos;
    private int closeDoorTick;
    private int relaxedWanderCooldown;
    private Vec3 relaxedTargetPos;

    public VillagerCommandGoal(Villager villager, CommandableVillager commandableVillager) {
        this.villager = villager;
        this.commandableVillager = commandableVillager;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return commandableVillager.getCommandState() != VillagerCommandState.NONE;
    }

    @Override
    public boolean canContinueToUse() {
        return commandableVillager.getCommandState() != VillagerCommandState.NONE;
    }

    @Override
    public void start() {
        this.nextRepathTick = 0;
        this.lastTargetPos = null;
        this.openedDoorPos = null;
        this.closeDoorTick = 0;
        this.relaxedWanderCooldown = 0;
        this.relaxedTargetPos = null;
        this.villager.getNavigation().stop();
    }

    @Override
    public void tick() {
        VillagerCommandState commandState = commandableVillager.getCommandState();
        if (commandState == VillagerCommandState.STAY) {
            this.tickOpenedDoor();
            this.villager.getNavigation().stop();
            return;
        }

        if (commandState != VillagerCommandState.FOLLOW) {
            return;
        }

        this.tickOpenedDoor();

        UUID targetUuid = commandableVillager.getCommandTargetUuid();
        if (targetUuid == null) {
            this.villager.getNavigation().stop();
            return;
        }

        Player player = this.villager.level().getPlayerByUUID(targetUuid);
        if (player == null || !player.isAlive() || player.level() != this.villager.level()) {
            this.villager.getNavigation().stop();
            return;
        }

        if (this.commandableVillager.getFollowMode() == VillagerFollowMode.RELAXED) {
            this.tickRelaxedFollow(player);
            return;
        }

        this.tickCloseFollow(player);
    }

    @Override
    public void stop() {
        this.nextRepathTick = 0;
        this.lastTargetPos = null;
        this.openedDoorPos = null;
        this.closeDoorTick = 0;
        this.relaxedWanderCooldown = 0;
        this.relaxedTargetPos = null;
        this.villager.getNavigation().stop();
    }

    private void tickCloseFollow(Player player) {
        this.villager.getLookControl().setLookAt(player, 30.0F, 30.0F);
        if (this.villager.distanceToSqr(player) > FOLLOW_START_DISTANCE_SQR) {
            this.tryOpenNearbyWoodenDoor();
            Vec3 targetPos = player.position();
            boolean shouldRepath = this.nextRepathTick <= 0
                    || this.villager.getNavigation().isDone()
                    || this.lastTargetPos == null
                    || this.lastTargetPos.distanceToSqr(targetPos) > 1.0D;
            if (shouldRepath) {
                this.villager.getNavigation().moveTo(player, FOLLOW_SPEED);
                this.nextRepathTick = FOLLOW_REPATH_TICKS;
                this.lastTargetPos = targetPos;
            } else {
                this.nextRepathTick--;
            }
        } else {
            this.nextRepathTick = 0;
            this.lastTargetPos = null;
            this.villager.getNavigation().stop();
        }
    }

    private void tickRelaxedFollow(Player player) {
        double distanceToPlayerSqr = this.villager.distanceToSqr(player);
        if (distanceToPlayerSqr > RELAXED_WANDER_MAX_DISTANCE_SQR || distanceToPlayerSqr > FOLLOW_START_DISTANCE_SQR && this.isPlayerMovingAwayClearly(player)) {
            this.clearRelaxedWander();
            this.relaxedWanderCooldown = 0;
            this.tickCloseFollow(player);
            return;
        }

        if (this.shouldSuspendRelaxedWander()) {
            this.clearRelaxedWander();
            if (distanceToPlayerSqr > FOLLOW_START_DISTANCE_SQR) {
                this.tickCloseFollow(player);
            } else {
                this.villager.getNavigation().stop();
            }
            return;
        }

        this.nextRepathTick = 0;
        this.lastTargetPos = null;
        if (this.relaxedTargetPos != null) {
            if (this.villager.position().distanceToSqr(this.relaxedTargetPos) <= RELAXED_TARGET_REACHED_DISTANCE_SQR || this.villager.getNavigation().isDone()) {
                this.clearRelaxedWander();
                this.relaxedWanderCooldown = this.getNextRelaxedWanderCooldown();
            }
        }

        if (this.relaxedWanderCooldown > 0) {
            this.relaxedWanderCooldown--;
        }

        if (this.relaxedTargetPos == null) {
            this.villager.getNavigation().stop();
            if (this.relaxedWanderCooldown <= 0) {
                this.tryStartRelaxedWander(player);
            }
        }
    }

    private boolean shouldSuspendRelaxedWander() {
        return this.villager.getTarget() != null
                || this.villager.isAggressive()
                || this.villager.isUsingItem()
                || this.villager.tickCount - this.commandableVillager.getLastDamageTick() < RELAXED_RECENT_DAMAGE_TICKS
                || this.villager.isSleeping()
                || this.villager.isInWaterOrBubble()
                || this.villager.isInLava();
    }

    private boolean isPlayerMovingAwayClearly(Player player) {
        Vec3 motion = player.getDeltaMovement();
        double horizontalSpeedSqr = motion.x * motion.x + motion.z * motion.z;
        if (horizontalSpeedSqr < RELAXED_MOVE_AWAY_SPEED_SQR) {
            return false;
        }

        Vec3 toPlayer = player.position().subtract(this.villager.position());
        double horizontalLength = Math.sqrt(toPlayer.x * toPlayer.x + toPlayer.z * toPlayer.z);
        if (horizontalLength < 1.0E-4D) {
            return false;
        }

        double dot = (motion.x * toPlayer.x + motion.z * toPlayer.z) / horizontalLength;
        return dot > RELAXED_MOVE_AWAY_DOT;
    }

    private void tryStartRelaxedWander(Player player) {
        for (int attempt = 0; attempt < 10; attempt++) {
            double angle = this.villager.getRandom().nextDouble() * (Math.PI * 2.0D);
            double radius = Mth.lerp(this.villager.getRandom().nextDouble(), RELAXED_WANDER_MIN_RADIUS, RELAXED_WANDER_MAX_RADIUS);
            int offsetX = Mth.floor(Math.cos(angle) * radius);
            int offsetZ = Mth.floor(Math.sin(angle) * radius);
            BlockPos origin = player.blockPosition().offset(offsetX, 0, offsetZ);

            for (int yOffset = 1; yOffset >= -2; yOffset--) {
                BlockPos candidate = origin.offset(0, yOffset, 0);
                if (!this.isValidRelaxedTarget(candidate, player)) {
                    continue;
                }

                Vec3 targetPos = Vec3.atBottomCenterOf(candidate);
                this.tryOpenNearbyWoodenDoor();
                this.villager.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, RELAXED_WANDER_SPEED);
                this.relaxedTargetPos = targetPos;
                this.relaxedWanderCooldown = this.getNextRelaxedWanderCooldown();
                return;
            }
        }

        this.relaxedWanderCooldown = this.getNextRelaxedWanderCooldown();
    }

    private boolean isValidRelaxedTarget(BlockPos candidate, Player player) {
        if (candidate.closerToCenterThan(this.villager.position(), 1.5D)) {
            return false;
        }
        if (candidate.closerToCenterThan(player.position(), RELAXED_WANDER_MIN_RADIUS - 0.5D)) {
            return false;
        }

        BlockState feetState = this.villager.level().getBlockState(candidate);
        BlockState headState = this.villager.level().getBlockState(candidate.above());
        BlockState floorState = this.villager.level().getBlockState(candidate.below());
        if (!feetState.canBeReplaced() || !headState.canBeReplaced()) {
            return false;
        }
        if (floorState.isAir() || !floorState.blocksMotion()) {
            return false;
        }

        Vec3 targetPos = Vec3.atBottomCenterOf(candidate);
        return this.villager.level().noCollision(this.villager, this.villager.getBoundingBox().move(targetPos.x - this.villager.getX(), targetPos.y - this.villager.getY(), targetPos.z - this.villager.getZ()));
    }

    private int getNextRelaxedWanderCooldown() {
        return RELAXED_WANDER_MIN_TICKS + this.villager.getRandom().nextInt(RELAXED_WANDER_MAX_TICKS - RELAXED_WANDER_MIN_TICKS + 1);
    }

    private void clearRelaxedWander() {
        this.relaxedTargetPos = null;
    }

    private void tryOpenNearbyWoodenDoor() {
        BlockPos origin = this.villager.blockPosition();
        BlockPos forward = origin.relative(this.villager.getDirection());
        BlockPos[] positions = new BlockPos[] {
                origin,
                origin.above(),
                forward,
                forward.above(),
                origin.north(),
                origin.north().above(),
                origin.south(),
                origin.south().above(),
                origin.east(),
                origin.east().above(),
                origin.west(),
                origin.west().above()
        };

        for (BlockPos pos : positions) {
            BlockState state = this.villager.level().getBlockState(pos);
            Block block = state.getBlock();
            if (!(block instanceof DoorBlock doorBlock)) {
                continue;
            }
            if (!state.is(BlockTags.WOODEN_DOORS) || state.getValue(DoorBlock.OPEN)) {
                continue;
            }

            doorBlock.setOpen(this.villager, this.villager.level(), state, pos, true);
            this.openedDoorPos = pos.immutable();
            this.closeDoorTick = FOLLOW_CLOSE_DOOR_TICKS;
            break;
        }
    }

    private void tickOpenedDoor() {
        if (this.openedDoorPos == null) {
            this.closeDoorTick = 0;
            return;
        }

        if (this.closeDoorTick > 0) {
            this.closeDoorTick--;
            return;
        }

        this.tryCloseOpenedDoor();
    }

    private void tryCloseOpenedDoor() {
        if (this.openedDoorPos == null) {
            return;
        }

        BlockState state = this.villager.level().getBlockState(this.openedDoorPos);
        Block block = state.getBlock();
        if (!(block instanceof DoorBlock doorBlock) || !state.is(BlockTags.WOODEN_DOORS)) {
            this.openedDoorPos = null;
            this.closeDoorTick = 0;
            return;
        }

        if (!state.getValue(DoorBlock.OPEN)) {
            this.openedDoorPos = null;
            this.closeDoorTick = 0;
            return;
        }

        Vec3 doorCenter = Vec3.atBottomCenterOf(this.openedDoorPos);
        if (this.villager.position().distanceToSqr(doorCenter) <= 2.25D) {
            this.closeDoorTick = 2;
            return;
        }

        doorBlock.setOpen(this.villager, this.villager.level(), state, this.openedDoorPos, false);
        this.openedDoorPos = null;
        this.closeDoorTick = 0;
    }
}
