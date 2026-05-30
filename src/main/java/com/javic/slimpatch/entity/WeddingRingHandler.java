package com.javic.slimpatch.entity;

import com.javic.slimpatch.Config;
import com.javic.slimpatch.familytree.FamilyTreeSavedData;
import com.javic.slimpatch.familytree.FamilyTreeVillagerSnapshot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import com.javic.slimpatch.item.ModItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public final class WeddingRingHandler {

    private WeddingRingHandler() {
    }

    public static boolean isWeddingRing(ItemStack stack) {
        return stack.is(ModItems.IRON_WEDDING_RING.get())
                || stack.is(ModItems.GOLD_WEDDING_RING.get())
                || stack.is(ModItems.EMERALD_WEDDING_RING.get())
                || stack.is(ModItems.DIAMOND_WEDDING_RING.get());
    }

    public static String getMarriageFailureMessage(FamilyVillager familyVillager) {
        if (familyVillager.getRelationshipStage() == VillagerRelationshipStage.MARRIED || familyVillager.hasSpouse()) {
            return "slimpatch.message.marriage_already_married";
        }
        if (familyVillager.getAgeStage() != VillagerAgeStage.ADULT) {
            return "slimpatch.message.marriage_requires_adult";
        }
        if (familyVillager.getRelationshipStage() != VillagerRelationshipStage.DATING) {
            return "slimpatch.message.marriage_requires_dating";
        }
        if (familyVillager.getGoldenRelationship() < 5.0F) {
            return "slimpatch.message.marriage_requires_golden_hearts";
        }
        return null;
    }

    public static String getMarriageFailureMessage(FamilyVillager familyVillager, Villager villager, ServerPlayer player) {
        if (familyVillager.getRelationshipStage() == VillagerRelationshipStage.MARRIED || familyVillager.hasSpouse()) {
            return "slimpatch.message.marriage_already_married";
        }
        if (VillagerFamilyData.isUnderage(familyVillager)) {
            return "slimpatch.message.marriage_requires_adult";
        }
        if (!VillagerFamilyData.canUseRomanticInteraction(villager, player)) {
            return "slimpatch.message.parent_romance_blocked";
        }
        float goldenRelationship = VillagerRelationshipData.getGoldenRelationshipForPlayer(villager, player.getUUID(), familyVillager.getGoldenRelationship());
        if (goldenRelationship <= 0.0F) {
            return "slimpatch.message.marriage_requires_dating";
        }
        if (goldenRelationship < 5.0F) {
            return "slimpatch.message.marriage_requires_golden_hearts";
        }
        if (!Config.ALLOW_MULTIPLE_PLAYER_SPOUSES.get() && playerHasLivingVillagerSpouse(player, villager.getUUID())) {
            return "slimpatch.message.marriage_player_already_has_spouse";
        }
        return null;
    }

    private static boolean playerHasLivingVillagerSpouse(ServerPlayer player, UUID targetVillagerUuid) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return false;
        }
        UUID playerUuid = player.getUUID();
        FamilyTreeSavedData data = FamilyTreeSavedData.get(server);
        for (FamilyTreeVillagerSnapshot snapshot : data.getVillagers()) {
            if (snapshot == null || !playerUuid.equals(snapshot.getSpousePlayerUuid())) {
                continue;
            }
            UUID villagerUuid = snapshot.getVillagerUuid();
            if (villagerUuid == null || villagerUuid.equals(targetVillagerUuid)) {
                continue;
            }
            if (isVillagerAlive(server, snapshot)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isVillagerAlive(MinecraftServer server, FamilyTreeVillagerSnapshot snapshot) {
        if (server == null || snapshot == null || snapshot.getVillagerUuid() == null) {
            return false;
        }
        for (ServerLevel level : server.getAllLevels()) {
            Entity entity = level.getEntity(snapshot.getVillagerUuid());
            if (entity instanceof Villager villager) {
                return villager.isAlive();
            }
        }
        return snapshot.isAlive();
    }

    public static ItemStack findWeddingRingStack(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        if (isWeddingRing(mainHand)) {
            return mainHand;
        }
        ItemStack offHand = player.getOffhandItem();
        if (isWeddingRing(offHand)) {
            return offHand;
        }
        return ItemStack.EMPTY;
    }
}
