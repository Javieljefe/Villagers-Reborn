package com.javic.slimpatch.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public interface CommandableVillager {
    VillagerCombatMode getCombatMode();

    void setCombatMode(VillagerCombatMode combatMode);

    VillagerFollowMode getFollowMode();

    void setFollowMode(VillagerFollowMode followMode);

    boolean isArmorHidden();

    void setArmorHidden(boolean hidden);

    boolean isMuted();

    void setMuted(boolean muted);

    VillagerCommandState getCommandState();

    void setCommandState(VillagerCommandState commandState);

    UUID getCommandTargetUuid();

    void setCommandTargetUuid(UUID commandTargetUuid);

    UUID getCommandOwnerUuid();

    void setCommandOwnerUuid(UUID commandOwnerUuid);

    String getCommandOwnerName();

    void setCommandOwnerName(String commandOwnerName);

    BlockPos getStayPos();

    void setStayPos(BlockPos stayPos);

    Vec3 getStayAnchorPos();

    void setStayAnchorPos(Vec3 stayAnchorPos);

    boolean hasHome();

    void setHome(BlockPos homePos, ResourceKey<Level> homeDimension);

    void clearHome();

    BlockPos getHomePos();

    ResourceKey<Level> getHomeDimension();

    long getLastCommandTeleportTick();

    void setLastCommandTeleportTick(long tick);

    int getCombatLockTicks();

    void setCombatLockTicks(int ticks);

    int getLastDamageTick();

    void setLastDamageTick(int tick);

    int getLastAggressiveScanTick();

    void setLastAggressiveScanTick(int tick);

    int getLastBowAttackTick();

    void setLastBowAttackTick(int tick);

    int getBowChargeStartTick();

    void setBowChargeStartTick(int tick);

    int getLastCrossbowAttackTick();

    void setLastCrossbowAttackTick(int tick);

    int getCrossbowChargeStartTick();

    void setCrossbowChargeStartTick(int tick);

    int getLastShieldBlockTick();

    void setLastShieldBlockTick(int tick);

    int getShieldRaiseUntilTick();

    void setShieldRaiseUntilTick(int tick);

    float getBonusHealth();

    void setBonusHealth(float bonusHealth);

    double getBaseMaxHealth();

    void setBaseMaxHealth(double baseMaxHealth);
}
