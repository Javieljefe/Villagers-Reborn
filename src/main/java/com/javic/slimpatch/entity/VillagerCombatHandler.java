package com.javic.slimpatch.entity;

import com.javic.slimpatch.dialogue.DialogueManager;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.InteractionHand;

public final class VillagerCombatHandler {

    private static final double PLAYER_DEFENSE_RADIUS_SQR = 12.0D * 12.0D;
    private static final double AGGRESSIVE_ACQUIRE_RADIUS = 8.0D;
    private static final double AGGRESSIVE_ACQUIRE_RADIUS_SQR = AGGRESSIVE_ACQUIRE_RADIUS * AGGRESSIVE_ACQUIRE_RADIUS;
    private static final double MAX_RANGED_ATTACK_DISTANCE_SQR = 12.0D * 12.0D;
    private static final double MAX_TARGET_DISTANCE_SQR = 16.0D * 16.0D;
    private static final double MAX_STAY_TARGET_DISTANCE_SQR = 8.0D * 8.0D;
    private static final double PREVENTIVE_FEAR_RADIUS = 12.0D;
    private static final double SHIELD_BLOCK_DOT_THRESHOLD = 0.2D;
    private static final int BOW_CHARGE_TICKS = 20;
    private static final int BOW_ATTACK_COOLDOWN_TICKS = 35;
    private static final int CROSSBOW_CHARGE_TICKS = 25;
    private static final int CROSSBOW_ATTACK_COOLDOWN_TICKS = 50;
    private static final int SHIELD_BLOCK_COOLDOWN_TICKS = 30;
    private static final int SHIELD_RAISE_DURATION_TICKS = 12;
    private static final int RECENT_ATTACK_TICKS = 60;
    private static final int AGGRESSIVE_SCAN_COOLDOWN_TICKS = 20;
    private static final int COMBAT_LOCK_TICKS = 30;

    private VillagerCombatHandler() {
    }

    public static void tickCombat(Villager villager, CommandableVillager commandableVillager) {
        if (villager.level().isClientSide) {
            return;
        }
        if (commandableVillager.getCombatMode() == VillagerCombatMode.PASSIVE) {
            commandableVillager.setCombatLockTicks(0);
            stopUsingRangedWeapon(villager, commandableVillager);
            tickShieldUse(villager, commandableVillager, false);
            if (villager.getTarget() != null) {
                clearTarget(villager);
            }
            return;
        }
        if (DialogueManager.isInDialogue(villager) || !hasCombatWeapon(villager)) {
            commandableVillager.setCombatLockTicks(0);
            stopUsingRangedWeapon(villager, commandableVillager);
            tickShieldUse(villager, commandableVillager, false);
            if (villager.getTarget() != null) {
                clearTarget(villager);
            }
            return;
        }

        preventVanillaFleeing(villager);

        LivingEntity currentTarget = villager.getTarget();
        if (currentTarget != null && !isValidTarget(villager, commandableVillager, currentTarget)) {
            commandableVillager.setCombatLockTicks(0);
            clearTarget(villager);
            currentTarget = null;
        }

        if (currentTarget != null && commandableVillager.getCombatLockTicks() > 0) {
            commandableVillager.setCombatLockTicks(commandableVillager.getCombatLockTicks() - 1);
            maintainCombatLock(villager);
        }

        if (currentTarget == null && commandableVillager.getCombatMode() == VillagerCombatMode.AGGRESSIVE) {
            LivingEntity aggressiveTarget = findNearbyAggressiveTarget(villager, commandableVillager);
            if (aggressiveTarget != null) {
                engageTarget(villager, aggressiveTarget);
                currentTarget = aggressiveTarget;
            }
        }

        if (currentTarget == null) {
            LivingEntity selfDefenseTarget = getRecentHostileAttacker(villager, villager.getLastHurtByMob(), villager.getLastHurtByMobTimestamp());
            if (selfDefenseTarget != null && isValidTarget(villager, commandableVillager, selfDefenseTarget)) {
                engageTarget(villager, selfDefenseTarget);
                currentTarget = selfDefenseTarget;
            }
        }

        if (currentTarget == null) {
            LivingEntity playerDefenseTarget = findNearbyPlayerDefenseTarget(villager, commandableVillager);
            if (playerDefenseTarget != null && isValidTarget(villager, commandableVillager, playerDefenseTarget)) {
                engageTarget(villager, playerDefenseTarget);
                currentTarget = playerDefenseTarget;
            }
        }

        if (currentTarget != null && hasBowWeapon(villager)) {
            tickBowAttack(villager, commandableVillager, currentTarget);
        } else if (currentTarget != null && hasCrossbowWeapon(villager)) {
            tickCrossbowAttack(villager, commandableVillager, currentTarget);
        } else {
            stopUsingRangedWeapon(villager, commandableVillager);
        }

        tickShieldUse(villager, commandableVillager, currentTarget != null);
    }

    public static void onVillagerHurt(Villager villager, CommandableVillager commandableVillager, DamageSource source) {
        if (villager.level().isClientSide || commandableVillager.getCombatMode() == VillagerCombatMode.PASSIVE || DialogueManager.isInDialogue(villager) || !hasCombatWeapon(villager)) {
            return;
        }

        LivingEntity attacker = extractHostileAttacker(source);
        if (attacker != null && isValidTarget(villager, commandableVillager, attacker)) {
            engageTarget(villager, attacker);
        }
    }

    public static boolean tryBlockDamage(Villager villager, CommandableVillager commandableVillager, DamageSource source, float amount) {
        if (villager.level().isClientSide || amount <= 0.0F || !hasShield(villager)) {
            return false;
        }

        LivingEntity attacker = extractHostileAttacker(source);
        if (attacker == null) {
            return false;
        }
        if (villager.tickCount - commandableVillager.getLastShieldBlockTick() < SHIELD_BLOCK_COOLDOWN_TICKS) {
            return false;
        }
        if (!isDamageFromFront(villager, source)) {
            return false;
        }

        commandableVillager.setLastShieldBlockTick(villager.tickCount);
        commandableVillager.setShieldRaiseUntilTick(villager.tickCount + SHIELD_RAISE_DURATION_TICKS);
        if (!villager.isUsingItem()) {
            villager.startUsingItem(InteractionHand.OFF_HAND);
        }
        villager.level().playSound(null, villager.blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.NEUTRAL, 1.0F, 0.8F + villager.getRandom().nextFloat() * 0.4F);
        if (isValidTarget(villager, commandableVillager, attacker)) {
            engageTarget(villager, attacker);
        }
        return true;
    }

    public static boolean isActivelyDefending(Villager villager, CommandableVillager commandableVillager) {
        if (commandableVillager.getCombatMode() == VillagerCombatMode.PASSIVE) {
            return false;
        }
        LivingEntity target = villager.getTarget();
        return target != null && isValidTarget(villager, commandableVillager, target);
    }

    public static boolean canMeleeAttack(Villager villager, CommandableVillager commandableVillager) {
        return commandableVillager.getCombatMode() != VillagerCombatMode.PASSIVE
                && hasMeleeWeapon(villager)
                && isActivelyDefending(villager, commandableVillager)
                && !DialogueManager.isInDialogue(villager);
    }

    public static boolean hasCombatWeapon(Villager villager) {
        return hasMeleeWeapon(villager) || hasBowWeapon(villager) || hasCrossbowWeapon(villager);
    }

    public static boolean hasMeleeWeapon(Villager villager) {
        ItemStack stack = villager.getMainHandItem();
        return stack.getItem() instanceof SwordItem
                || stack.getItem() instanceof AxeItem
                || stack.getItem() instanceof DiggerItem
                || stack.getItem() instanceof TridentItem
                || stack.getItem() instanceof MaceItem;
    }

    public static boolean hasBowWeapon(Villager villager) {
        return villager.getMainHandItem().getItem() instanceof BowItem;
    }

    public static boolean hasCrossbowWeapon(Villager villager) {
        return villager.getMainHandItem().getItem() instanceof CrossbowItem;
    }

    public static boolean hasShield(Villager villager) {
        return villager.getOffhandItem().getItem() instanceof ShieldItem;
    }

    private static LivingEntity findNearbyPlayerDefenseTarget(Villager villager, CommandableVillager commandableVillager) {
        if (commandableVillager.getCommandState() == VillagerCommandState.FOLLOW && commandableVillager.getCommandTargetUuid() != null) {
            Player followedPlayer = villager.level().getPlayerByUUID(commandableVillager.getCommandTargetUuid());
            if (followedPlayer != null && villager.distanceToSqr(followedPlayer) <= PLAYER_DEFENSE_RADIUS_SQR) {
                LivingEntity target = getRecentHostileAttacker(followedPlayer, followedPlayer.getLastHurtByMob(), followedPlayer.getLastHurtByMobTimestamp());
                if (target != null) {
                    return target;
                }
            }
        }

        for (Player player : villager.level().players()) {
            if (villager.distanceToSqr(player) > PLAYER_DEFENSE_RADIUS_SQR) {
                continue;
            }
            LivingEntity target = getRecentHostileAttacker(player, player.getLastHurtByMob(), player.getLastHurtByMobTimestamp());
            if (target != null) {
                return target;
            }
        }

        return null;
    }

    private static LivingEntity findNearbyAggressiveTarget(Villager villager, CommandableVillager commandableVillager) {
        if (villager.tickCount - commandableVillager.getLastAggressiveScanTick() < AGGRESSIVE_SCAN_COOLDOWN_TICKS) {
            return null;
        }

        commandableVillager.setLastAggressiveScanTick(villager.tickCount);

        LivingEntity closestTarget = null;
        double closestDistance = Double.MAX_VALUE;
        for (LivingEntity candidate : villager.level().getEntitiesOfClass(LivingEntity.class,
                villager.getBoundingBox().inflate(AGGRESSIVE_ACQUIRE_RADIUS),
                target -> isValidAggressiveTarget(villager, commandableVillager, target))) {
            double distance = villager.distanceToSqr(candidate);
            if (distance <= AGGRESSIVE_ACQUIRE_RADIUS_SQR && distance < closestDistance) {
                closestTarget = candidate;
                closestDistance = distance;
            }
        }

        return closestTarget;
    }

    private static LivingEntity getRecentHostileAttacker(LivingEntity victim, LivingEntity attacker, int attackerTimestamp) {
        if (attacker == null || !attacker.isAlive()) {
            return null;
        }
        if (!(attacker instanceof Monster)) {
            return null;
        }
        return victim.tickCount - attackerTimestamp <= RECENT_ATTACK_TICKS ? attacker : null;
    }

    private static LivingEntity extractHostileAttacker(DamageSource source) {
        return source.getEntity() instanceof LivingEntity attacker && attacker instanceof Monster ? attacker : null;
    }

    private static boolean isValidAggressiveTarget(Villager villager, CommandableVillager commandableVillager, LivingEntity target) {
        if (!isHostileCombatTarget(villager, target)) {
            return false;
        }
        if (target instanceof Creeper) {
            return false;
        }
        if (!villager.hasLineOfSight(target)) {
            return false;
        }
        if (commandableVillager.getCommandState() == VillagerCommandState.STAY) {
            Vec3 stayAnchorPos = commandableVillager.getStayAnchorPos();
            if (stayAnchorPos != null && target.position().distanceToSqr(stayAnchorPos) > MAX_STAY_TARGET_DISTANCE_SQR) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidTarget(Villager villager, CommandableVillager commandableVillager, LivingEntity target) {
        if (!isHostileCombatTarget(villager, target)) {
            return false;
        }
        if (villager.distanceToSqr(target) > MAX_TARGET_DISTANCE_SQR) {
            return false;
        }
        if (commandableVillager.getCommandState() == VillagerCommandState.STAY) {
            Vec3 stayAnchorPos = commandableVillager.getStayAnchorPos();
            if (stayAnchorPos != null && target.position().distanceToSqr(stayAnchorPos) > MAX_STAY_TARGET_DISTANCE_SQR) {
                return false;
            }
        }
        if (commandableVillager.getCommandState() == VillagerCommandState.FOLLOW && commandableVillager.getCommandTargetUuid() != null) {
            Player followedPlayer = villager.level().getPlayerByUUID(commandableVillager.getCommandTargetUuid());
            if (followedPlayer != null && target.distanceToSqr(followedPlayer) > PLAYER_DEFENSE_RADIUS_SQR) {
                return false;
            }
        }
        return true;
    }

    private static boolean isHostileCombatTarget(Villager villager, LivingEntity target) {
        return hasCombatWeapon(villager)
                && target instanceof Enemy
                && target instanceof Monster
                && target != villager
                && target.isAlive()
                && !target.isSpectator()
                && !target.isInvulnerable()
                && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(target)
                && target.level() == villager.level();
    }

    private static void tickBowAttack(Villager villager, CommandableVillager commandableVillager, LivingEntity target) {
        villager.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (!villager.hasLineOfSight(target) || villager.distanceToSqr(target) > MAX_RANGED_ATTACK_DISTANCE_SQR) {
            stopUsingBow(villager, commandableVillager);
            return;
        }

        if (findArrowAmmo(villager).isEmpty()) {
            stopUsingBow(villager, commandableVillager);
            return;
        }

        if (!villager.isUsingItem()) {
            if (villager.tickCount - commandableVillager.getLastBowAttackTick() < BOW_ATTACK_COOLDOWN_TICKS) {
                return;
            }
            villager.startUsingItem(InteractionHand.MAIN_HAND);
            commandableVillager.setBowChargeStartTick(villager.tickCount);
            return;
        }

        if (commandableVillager.getBowChargeStartTick() < 0) {
            commandableVillager.setBowChargeStartTick(villager.tickCount);
            return;
        }

        if (villager.tickCount - commandableVillager.getBowChargeStartTick() < BOW_CHARGE_TICKS) {
            return;
        }

        if (performBowShot(villager, target)) {
            commandableVillager.setLastBowAttackTick(villager.tickCount);
        }
        stopUsingBow(villager, commandableVillager);
    }

    private static void tickCrossbowAttack(Villager villager, CommandableVillager commandableVillager, LivingEntity target) {
        villager.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (!villager.hasLineOfSight(target) || villager.distanceToSqr(target) > MAX_RANGED_ATTACK_DISTANCE_SQR) {
            stopUsingCrossbow(villager, commandableVillager);
            return;
        }

        if (findArrowAmmo(villager).isEmpty()) {
            stopUsingCrossbow(villager, commandableVillager);
            return;
        }

        if (!villager.isUsingItem()) {
            if (villager.tickCount - commandableVillager.getLastCrossbowAttackTick() < CROSSBOW_ATTACK_COOLDOWN_TICKS) {
                return;
            }
            villager.startUsingItem(InteractionHand.MAIN_HAND);
            commandableVillager.setCrossbowChargeStartTick(villager.tickCount);
            return;
        }

        if (commandableVillager.getCrossbowChargeStartTick() < 0) {
            commandableVillager.setCrossbowChargeStartTick(villager.tickCount);
            return;
        }

        if (villager.tickCount - commandableVillager.getCrossbowChargeStartTick() < CROSSBOW_CHARGE_TICKS) {
            return;
        }

        if (performCrossbowShot(villager, target)) {
            commandableVillager.setLastCrossbowAttackTick(villager.tickCount);
        }
        stopUsingCrossbow(villager, commandableVillager);
    }

    private static boolean performBowShot(Villager villager, LivingEntity target) {
        ArrowAmmoSource ammoSource = findArrowAmmo(villager);
        if (ammoSource.isEmpty()) {
            return false;
        }

        if (!spawnArrowProjectile(villager, target, ammoSource, 1.6F, 12.0F, 0.5D, false)) {
            return false;
        }
        villager.level().playSound(null, villager.blockPosition(), SoundEvents.ARROW_SHOOT, SoundSource.NEUTRAL, 1.0F, 1.0F / (villager.getRandom().nextFloat() * 0.4F + 1.2F) + 0.25F);
        return true;
    }

    private static boolean performCrossbowShot(Villager villager, LivingEntity target) {
        ArrowAmmoSource ammoSource = findArrowAmmo(villager);
        if (ammoSource.isEmpty()) {
            return false;
        }

        if (!spawnArrowProjectile(villager, target, ammoSource, 2.2F, 6.0F, 1.5D, false)) {
            return false;
        }
        villager.level().playSound(null, villager.blockPosition(), SoundEvents.CROSSBOW_SHOOT, SoundSource.NEUTRAL, 1.0F, 1.0F / (villager.getRandom().nextFloat() * 0.5F + 1.0F));
        return true;
    }

    private static void stopUsingBow(Villager villager, CommandableVillager commandableVillager) {
        if (villager.isUsingItem()) {
            villager.stopUsingItem();
        }
        commandableVillager.setBowChargeStartTick(-1);
    }

    private static void stopUsingCrossbow(Villager villager, CommandableVillager commandableVillager) {
        if (villager.isUsingItem()) {
            villager.stopUsingItem();
        }
        commandableVillager.setCrossbowChargeStartTick(-1);
    }

    private static void stopUsingRangedWeapon(Villager villager, CommandableVillager commandableVillager) {
        stopUsingBow(villager, commandableVillager);
        stopUsingCrossbow(villager, commandableVillager);
    }

    private static void tickShieldUse(Villager villager, CommandableVillager commandableVillager, boolean inCombat) {
        if (!hasShield(villager)) {
            if (villager.isUsingItem() && villager.getUsedItemHand() == InteractionHand.OFF_HAND) {
                villager.stopUsingItem();
            }
            return;
        }

        boolean shouldRaiseShield = inCombat && villager.tickCount <= commandableVillager.getShieldRaiseUntilTick();
        if (shouldRaiseShield) {
            if (!villager.isUsingItem()) {
                villager.startUsingItem(InteractionHand.OFF_HAND);
            }
            return;
        }

        if (villager.isUsingItem() && villager.getUsedItemHand() == InteractionHand.OFF_HAND) {
            villager.stopUsingItem();
        }
    }

    private static boolean spawnArrowProjectile(Villager villager, LivingEntity target, ArrowAmmoSource ammoSource, float velocity, float inaccuracy, double baseDamageBonus, boolean crit) {
        ItemStack weaponStack = villager.getMainHandItem();
        ItemStack arrowStack = ammoSource.stack();
        ArrowItem arrowItem = arrowStack.getItem() instanceof ArrowItem item ? item : (ArrowItem) Items.ARROW;
        AbstractArrow arrow = arrowItem.createArrow(villager.level(), arrowStack, villager, weaponStack);
        double dx = target.getX() - villager.getX();
        double dz = target.getZ() - villager.getZ();
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        double dy = target.getY(0.3333333333333333D) - arrow.getY() + horizontalDistance * 0.2D;
        arrow.shoot(dx, dy, dz, velocity, inaccuracy);
        arrow.setCritArrow(crit);
        arrow.setBaseDamage(arrow.getBaseDamage() + baseDamageBonus);

        if (!villager.level().addFreshEntity(arrow)) {
            return false;
        }

        consumeArrowAmmo(villager, ammoSource);
        return true;
    }

    private static boolean isDamageFromFront(Villager villager, DamageSource source) {
        Entity sourceEntity = source.getDirectEntity() != null ? source.getDirectEntity() : source.getEntity();
        if (sourceEntity == null) {
            return false;
        }

        Vec3 toAttacker = new Vec3(sourceEntity.getX() - villager.getX(), 0.0D, sourceEntity.getZ() - villager.getZ());
        if (toAttacker.lengthSqr() < 1.0E-6D) {
            return true;
        }

        Vec3 look = villager.getLookAngle();
        Vec3 flatLook = new Vec3(look.x, 0.0D, look.z);
        if (flatLook.lengthSqr() < 1.0E-6D) {
            return true;
        }

        return flatLook.normalize().dot(toAttacker.normalize()) > SHIELD_BLOCK_DOT_THRESHOLD;
    }

    private static ArrowAmmoSource findArrowAmmo(Villager villager) {
        ItemStack offhand = villager.getOffhandItem();
        if (isArrowAmmo(offhand)) {
            return new ArrowAmmoSource(ArrowAmmoLocation.OFFHAND, -1, offhand);
        }

        if (villager instanceof VillagerEquipmentHolder holder) {
            for (int i = 0; i < holder.getEquipmentInventory().getContainerSize(); i++) {
                ItemStack stack = holder.getEquipmentInventory().getItem(i);
                if (isArrowAmmo(stack)) {
                    return new ArrowAmmoSource(ArrowAmmoLocation.STORAGE, i, stack);
                }
            }
        }

        return ArrowAmmoSource.EMPTY;
    }

    private static boolean isArrowAmmo(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ArrowItem;
    }

    private static void consumeArrowAmmo(Villager villager, ArrowAmmoSource ammoSource) {
        if (ammoSource.location() == ArrowAmmoLocation.OFFHAND) {
            ItemStack offhand = villager.getOffhandItem();
            if (isArrowAmmo(offhand)) {
                offhand.shrink(1);
                villager.setItemInHand(InteractionHand.OFF_HAND, offhand);
            }
            return;
        }

        if (ammoSource.location() == ArrowAmmoLocation.STORAGE && villager instanceof VillagerEquipmentHolder holder) {
            ItemStack stack = holder.getEquipmentInventory().getItem(ammoSource.slot());
            if (isArrowAmmo(stack)) {
                stack.shrink(1);
                holder.getEquipmentInventory().setItem(ammoSource.slot(), stack);
            }
        }
    }

    private enum ArrowAmmoLocation {
        NONE,
        OFFHAND,
        STORAGE
    }

    private record ArrowAmmoSource(ArrowAmmoLocation location, int slot, ItemStack stack) {
        private static final ArrowAmmoSource EMPTY = new ArrowAmmoSource(ArrowAmmoLocation.NONE, -1, ItemStack.EMPTY);

        private boolean isEmpty() {
            return this.location == ArrowAmmoLocation.NONE || this.stack.isEmpty();
        }
    }

    private static void clearTarget(Villager villager) {
        villager.setTarget(null);
        villager.setAggressive(false);
        villager.getNavigation().stop();
    }

    private static void engageTarget(Villager villager, LivingEntity target) {
        if (!(villager instanceof CommandableVillager commandableVillager)) {
            return;
        }
        villager.setTarget(target);
        villager.setAggressive(true);
        commandableVillager.setCombatLockTicks(COMBAT_LOCK_TICKS);
        maintainCombatLock(villager);
        villager.getNavigation().stop();
    }

    private static void maintainCombatLock(Villager villager) {
        villager.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        villager.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        villager.getBrain().eraseMemory(MemoryModuleType.HURT_BY);
        villager.getBrain().eraseMemory(MemoryModuleType.HURT_BY_ENTITY);
        villager.getBrain().eraseMemory(MemoryModuleType.NEAREST_HOSTILE);
        if (villager.isSleeping()) {
            villager.stopSleeping();
        }
    }

    private static void preventVanillaFleeing(Villager villager) {
        if (villager.isSleeping() || villager.getTarget() != null || !hasPreventiveFearState(villager)) {
            return;
        }
        villager.getBrain().eraseMemory(MemoryModuleType.NEAREST_HOSTILE);
        villager.getBrain().eraseMemory(MemoryModuleType.AVOID_TARGET);
        villager.getBrain().eraseMemory(MemoryModuleType.IS_PANICKING);
        villager.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        villager.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        villager.getBrain().eraseMemory(MemoryModuleType.HURT_BY);
        villager.getBrain().eraseMemory(MemoryModuleType.HURT_BY_ENTITY);
        villager.getNavigation().stop();
    }

    private static boolean hasPreventiveFearState(Villager villager) {
        return villager.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_HOSTILE)
                || villager.getBrain().hasMemoryValue(MemoryModuleType.AVOID_TARGET)
                || villager.getBrain().hasMemoryValue(MemoryModuleType.IS_PANICKING)
                || villager.getBrain().hasMemoryValue(MemoryModuleType.HURT_BY)
                || villager.getBrain().hasMemoryValue(MemoryModuleType.HURT_BY_ENTITY)
                || !villager.level().getEntitiesOfClass(Monster.class, villager.getBoundingBox().inflate(PREVENTIVE_FEAR_RADIUS), LivingEntity::isAlive).isEmpty();
    }
}
