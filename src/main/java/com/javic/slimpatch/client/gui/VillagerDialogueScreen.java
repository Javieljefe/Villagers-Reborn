package com.javic.slimpatch.client.gui;

import com.javic.slimpatch.Config;
import com.javic.slimpatch.client.camera.DialogueThirdPersonCameraHandler;
import com.javic.slimpatch.client.key.ModKeyBindings;
import com.javic.slimpatch.dialogue.DialogueManager;
import com.javic.slimpatch.entity.MaleVillagerEntity;
import com.javic.slimpatch.entity.CommandableVillager;
import com.javic.slimpatch.entity.FemaleVillagerEntity;
import com.javic.slimpatch.entity.FamilyVillager;
import com.javic.slimpatch.entity.VillagerAgeStage;
import com.javic.slimpatch.entity.VillagerFamilyData;
import com.javic.slimpatch.entity.VillagerRelationshipStage;
import com.javic.slimpatch.entity.VillagerRelationshipData;
import com.javic.slimpatch.entity.VillagerPersonality;
import com.javic.slimpatch.client.gui.VillagerPersonalityIcons;
import com.javic.slimpatch.client.gui.VillagerProfessionIcons;
import com.javic.slimpatch.network.DialogueStatePacket;
import com.javic.slimpatch.network.FamilyStatusPacket;
import com.javic.slimpatch.network.RequestFamilyTreePacket;
import com.javic.slimpatch.network.RelationshipPacket;
import com.javic.slimpatch.network.StartFamilyPacket;
import com.javic.slimpatch.network.VillagerCommandPacket;
import com.javic.slimpatch.network.VillagerCooldownsStorage;
import com.javic.slimpatch.network.VillagerEquipmentPacket;
import com.javic.slimpatch.network.GiftPacket;
import com.javic.slimpatch.network.QuestPacket;
import com.javic.slimpatch.network.RequestBirthScreenPacket;
import com.javic.slimpatch.entity.VillagerCommandState;
import com.javic.slimpatch.quests.Quest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.CameraType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class VillagerDialogueScreen extends Screen {

    private enum MenuMode {
        SOCIAL,
        ACTIONS,
        FAMILY,
        FAMILY_CONFIRM
    }	

    private final Villager villager;
    private final int optionWidth = 200;
    private final int optionHeight = 16;
    private final int optionSpacing = 4;
    private String fullLine = "";
    private int visibleChars = 0;
    private long lastCharTime = 0L;
    private static final int CHAR_INTERVAL_MS = 30;
    private static final ResourceLocation HEART_EMPTY = ResourceLocation.fromNamespaceAndPath("slimpatch", "textures/gui/heart_empty.png");
    private static final ResourceLocation HEART_HALF  = ResourceLocation.fromNamespaceAndPath("slimpatch", "textures/gui/heart_half.png");
    private static final ResourceLocation HEART_FULL  = ResourceLocation.fromNamespaceAndPath("slimpatch", "textures/gui/heart_full.png");
    private static final ResourceLocation HEART_GOLD_HALF = ResourceLocation.fromNamespaceAndPath("slimpatch", "textures/gui/heart_gold_half.png");
    private static final ResourceLocation HEART_GOLD_FULL = ResourceLocation.fromNamespaceAndPath("slimpatch", "textures/gui/heart_gold_full.png");
    private static final ResourceLocation GIFT_ICON_COLOR = ResourceLocation.fromNamespaceAndPath("slimpatch", "textures/gui/gift_icon_color.png");
    private static final ResourceLocation GIFT_ICON_GRAY = ResourceLocation.fromNamespaceAndPath("slimpatch", "textures/gui/gift_icon_gray.png");
    private static final ResourceLocation EDIT_BUTTON_TEXTURE = ResourceLocation.fromNamespaceAndPath("slimpatch", "textures/gui/edit.png");
    private static final ResourceLocation EQUIPMENT_BUTTON_TEXTURE = ResourceLocation.fromNamespaceAndPath("slimpatch", "textures/gui/equipment.png");
    private static final int HEART_SIZE = 16;
    private static final double EDGE_PAN_ZONE = 72.0D;
    private static final float MAX_YAW_STEP = 1.25F;
    private static final float MAX_PITCH_STEP = 0.95F;
    private static final float MAX_PITCH = 90.0F;
    private static final int SAFE_ZONE_PADDING = 8;
    private static final int SIDE_EDGE_PAN_HEIGHT_DIVISOR = 4;
    private static final int TOP_EDGE_PAN_WIDTH_DIVISOR = 4;
    private static final int BOTTOM_EDGE_PAN_WIDTH_DIVISOR = 10;
    private static final int BOTTOM_EDGE_PAN_HEIGHT = 10;
    private static final float EDGE_PAN_TICKS_PER_SECOND = 20.0F;
    private static final float MAX_EDGE_PAN_DELTA_TICKS = 2.0F;
    private static final long EXIT_HINT_DURATION_MS = 3200L;
    private static final long EXIT_HINT_FADE_OUT_MS = 700L;

    private Quest villagerQuest;
    private boolean hasQuest = false;
    private boolean questAccepted = false;
    private boolean questCompleted = false;
    private long lastEdgePanTimeNanos;
    private MenuMode menuMode = MenuMode.SOCIAL;
    private int actionsButtonX;
    private int actionsButtonY;
    private int actionsButtonWidth;
    private int actionsButtonHeight;
    private int familyButtonY;
    private long exitHintStartTime = 0L;
    private CameraType previousCameraType;
    private boolean restoreCameraOnClose;
    private boolean birthReadyConfirmationPending;

    private boolean isQuestUiEnabled() {
        return this.minecraft != null
                && this.minecraft.getSingleplayerServer() != null
                && !this.minecraft.getSingleplayerServer().isPublished();
    }

    public VillagerDialogueScreen(Villager villager) {
        super(Component.literal("Dialogue"));
        this.villager = villager;
    }

    @Override
    protected void init() {
        super.init();

        if (isQuestUiEnabled() && villager instanceof com.javic.slimpatch.entity.MaleVillagerEntity male) {
            hasQuest = male.hasQuest();
            if (hasQuest) {
                String questId = male.getQuestId();
                villagerQuest = com.javic.slimpatch.quests.QuestRegistry.getById(questId);
            }
        } else if (isQuestUiEnabled() && villager instanceof com.javic.slimpatch.entity.FemaleVillagerEntity female) {
            hasQuest = female.hasQuest();
            if (hasQuest) {
                String questId = female.getQuestId();
                villagerQuest = com.javic.slimpatch.quests.QuestRegistry.getById(questId);
            }
        } else if (isQuestUiEnabled() && villager.getPersistentData().getBoolean("HasQuest")) {
            hasQuest = true;
            String questId = villager.getPersistentData().getString("QuestId");
            villagerQuest = com.javic.slimpatch.quests.QuestRegistry.getById(questId);
        }

        questAccepted = false;
        questCompleted = false;

        if (villagerQuest != null) {
            com.javic.slimpatch.quests.data.PlayerQuestData clientData = com.javic.slimpatch.client.ClientQuestDataAccess.getClientInstance();
            if (clientData != null) {
                if (clientData.isActive(villagerQuest.getId())) {
                    questAccepted = true;
                } else if (clientData.isCompleted(villagerQuest.getId())) {
                    questCompleted = true;
                }
            }
        }

        if (hasQuest) {
            String questId = "";
            if (villager instanceof com.javic.slimpatch.entity.MaleVillagerEntity male) {
                questId = male.getQuestId();
            } else if (villager instanceof com.javic.slimpatch.entity.FemaleVillagerEntity female) {
                questId = female.getQuestId();
            } else if (villager.getPersistentData().contains("QuestId")) {
                questId = villager.getPersistentData().getString("QuestId");
            }

            if (questId != null && !questId.isEmpty()) {
                String intro = DialogueManager.getQuestIntro(questId);
                fullLine = (intro != null && !intro.equals("...")) ? intro : DialogueManager.getGenericQuestLine("QuestIntro");
            } else {
                fullLine = DialogueManager.getGenericQuestLine("QuestIntro");
            }
        } else {
            fullLine = "";
        }

        visibleChars = 0;
        lastCharTime = System.currentTimeMillis();
        lastEdgePanTimeNanos = System.nanoTime();
        exitHintStartTime = System.currentTimeMillis();
        applyDialogueCameraPreference();

        UUID uuid = villager.getUUID();
        UUID playerUuid = this.minecraft != null && this.minecraft.player != null ? this.minecraft.player.getUUID() : null;
        Map<String, Integer> existing = VillagerCooldownsStorage.getCooldowns(uuid, playerUuid);
        if (existing.isEmpty()) {
            Map<String, Integer> initial = new HashMap<>();
            initial.put("Friendly", 0);
            initial.put("Mean", 0);
            initial.put("Joke", 0);
            initial.put("Flirt", 0);
            VillagerCooldownsStorage.setCooldowns(uuid, playerUuid, initial);
        }

        rebuildMenuWidgets();
    }

    private void rebuildMenuWidgets() {
        this.clearWidgets();

        List<String> options = getCurrentMenuOptions();
        int optionCount = switch (menuMode) {
            case SOCIAL, ACTIONS -> Math.max(1, options.size());
            case FAMILY, FAMILY_CONFIRM -> Math.max(1, options.size());
        };
        int totalHeight = (optionHeight + optionSpacing) * optionCount;
        int startY = this.height - totalHeight - 40;
        int centerX = this.width / 2 - optionWidth / 2;

        if (menuMode == MenuMode.SOCIAL) {
            for (int i = 0; i < options.size(); i++) {
                this.addRenderableWidget(new DialogueOption(centerX, startY + i * (optionHeight + optionSpacing), options.get(i)));
            }
        } else {
            for (int i = 0; i < options.size(); i++) {
                this.addRenderableWidget(new ActionOption(centerX, startY + i * (optionHeight + optionSpacing), options.get(i)));
            }
        }

        int buttonX = this.width - 40;
        int buttonY = 10;
        int buttonWidth = 20;
        int buttonHeight = 20;
        int buttonSpacing = 4;

        this.addRenderableWidget(new EditIconButton(buttonX, buttonY, buttonWidth, buttonHeight));
        this.addRenderableWidget(new EquipmentIconButton(buttonX, buttonY + buttonHeight + buttonSpacing, buttonWidth, buttonHeight));

        int actionsButtonWidth = 76;
        int actionsButtonHeight = 20;
        int actionsButtonX = this.width - actionsButtonWidth - 16;
        int actionsButtonY = this.height - actionsButtonHeight - 16;
        int familyButtonY = actionsButtonY - actionsButtonHeight - buttonSpacing;

        this.actionsButtonX = actionsButtonX;
        this.actionsButtonY = actionsButtonY;
        this.actionsButtonWidth = actionsButtonWidth;
        this.actionsButtonHeight = actionsButtonHeight;
        this.familyButtonY = familyButtonY;

        this.addRenderableWidget(new FamilyButton(actionsButtonX, familyButtonY, actionsButtonWidth, actionsButtonHeight));
        this.addRenderableWidget(new ActionsButton(actionsButtonX, actionsButtonY, actionsButtonWidth, actionsButtonHeight));

        if (hasQuest && menuMode == MenuMode.SOCIAL) {
            int questY = startY + 4 * (optionHeight + optionSpacing) + 6;
            int centerXLeft = this.width / 2 - optionWidth - 10;
            int centerXRight = this.width / 2 + 10;

            if (!questAccepted && !questCompleted) {
                this.addRenderableWidget(new QuestOption(centerXLeft, questY, "Accept"));
                this.addRenderableWidget(new QuestOption(centerXRight, questY, "Decline"));
            } else if (questAccepted && !questCompleted) {
                this.addRenderableWidget(new QuestOption(centerX, questY, "Cancel"));
            } else if (questCompleted) {
                this.addRenderableWidget(new QuestOption(centerX, questY, "Complete"));
            }
        }
    }

    private void setMenuMode(MenuMode menuMode) {
        this.menuMode = menuMode;
        rebuildMenuWidgets();
    }

    private void onActionClicked(String action) {
        if (menuMode == MenuMode.FAMILY || menuMode == MenuMode.FAMILY_CONFIRM) {
            onFamilyActionClicked(action);
            return;
        }

        if (action.equals("Back")) {
            setMenuMode(MenuMode.SOCIAL);
            return;
        }

        if (!canUseProtectedCommandAction(action)) {
            showOwnerDeniedMessage();
            fullLine = "I can't follow your order right now.";
            visibleChars = 0;
            lastCharTime = System.currentTimeMillis();
            return;
        }

        String homeActionLabel = getHomeActionLabel();
        if (action.equals(homeActionLabel)) {
            if (this.minecraft != null && this.minecraft.player != null && this.villager instanceof com.javic.slimpatch.entity.CommandableVillager commandableVillager) {
                if (commandableVillager.hasHome()) {
                    commandableVillager.clearHome();
                    net.neoforged.neoforge.network.PacketDistributor.sendToServer(new VillagerCommandPacket(villager.getId(), VillagerCommandPacket.Action.CLEAR_HOME));
                    fullLine = "I will no longer live here.";
                } else {
                    commandableVillager.setHome(this.villager.blockPosition(), this.villager.level().dimension());
                    net.neoforged.neoforge.network.PacketDistributor.sendToServer(new VillagerCommandPacket(villager.getId(), VillagerCommandPacket.Action.SET_HOME));
                    fullLine = "I will live here now.";
                }
                rebuildMenuWidgets();
                visibleChars = 0;
                lastCharTime = System.currentTimeMillis();
            }
            return;
        }

        String followActionLabel = getFollowActionLabel();
        if (action.equals(followActionLabel)) {
            if (this.minecraft != null && this.minecraft.player != null && this.villager instanceof com.javic.slimpatch.entity.CommandableVillager commandableVillager) {
                if (commandableVillager.getCommandState() == VillagerCommandState.FOLLOW) {
                    commandableVillager.setCommandState(VillagerCommandState.NONE);
                    commandableVillager.setCommandTargetUuid(null);
                    net.neoforged.neoforge.network.PacketDistributor.sendToServer(new VillagerCommandPacket(villager.getId(), VillagerCommandPacket.Action.STOP_FOLLOWING));
                    fullLine = "I will stop following you.";
                } else {
                    commandableVillager.setCommandState(VillagerCommandState.FOLLOW);
                    net.neoforged.neoforge.network.PacketDistributor.sendToServer(new VillagerCommandPacket(villager.getId(), VillagerCommandState.FOLLOW));
                    fullLine = "I will follow you.";
                }
                rebuildMenuWidgets();
                visibleChars = 0;
                lastCharTime = System.currentTimeMillis();
            }
            return;
        }

        String stayActionLabel = getStayActionLabel();
        if (action.equals(stayActionLabel)) {
            if (this.minecraft != null && this.minecraft.player != null && this.villager instanceof com.javic.slimpatch.entity.CommandableVillager commandableVillager) {
                if (commandableVillager.getCommandState() == VillagerCommandState.STAY) {
                    commandableVillager.setCommandState(VillagerCommandState.NONE);
                    commandableVillager.setStayPos(null);
                    net.neoforged.neoforge.network.PacketDistributor.sendToServer(new VillagerCommandPacket(villager.getId(), VillagerCommandPacket.Action.MOVE_FREELY));
                    fullLine = "I will move freely again.";
                } else {
                    commandableVillager.setCommandState(VillagerCommandState.STAY);
                    net.neoforged.neoforge.network.PacketDistributor.sendToServer(new VillagerCommandPacket(villager.getId(), VillagerCommandState.STAY));
                    fullLine = "I will stay here.";
                }
                rebuildMenuWidgets();
                visibleChars = 0;
                lastCharTime = System.currentTimeMillis();
            }
            return;
        }

        VillagerCommandState commandState = switch (action) {
            default -> null;
        };

        if (commandState != null && this.minecraft != null && this.minecraft.player != null) {
            if (this.villager instanceof com.javic.slimpatch.entity.CommandableVillager commandableVillager) {
                commandableVillager.setCommandState(commandState);
            }
            net.neoforged.neoforge.network.PacketDistributor.sendToServer(new VillagerCommandPacket(villager.getId(), commandState));
        }

        fullLine = switch (action) {
            default -> fullLine;
        };
        visibleChars = 0;
        lastCharTime = System.currentTimeMillis();
    }

    private String getHomeActionLabel() {
        if (this.villager instanceof com.javic.slimpatch.entity.CommandableVillager commandableVillager && commandableVillager.hasHome()) {
            return "Clear Home";
        }
        return "Set Home";
    }

    private String getFollowActionLabel() {
        if (getCommandState() == VillagerCommandState.FOLLOW) {
            return "Stop Following";
        }
        return "Follow";
    }

    private String getStayActionLabel() {
        if (getCommandState() == VillagerCommandState.STAY) {
            return "Move Freely";
        }
        return "Stay";
    }

    private String getActionsButtonLabel() {
        if (menuMode != MenuMode.ACTIONS) {
            return "Actions";
        }
        return "Dialogue";
    }

    private String getFamilyButtonLabel() {
        if (menuMode == MenuMode.FAMILY || menuMode == MenuMode.FAMILY_CONFIRM) {
            return "Dialogue";
        }
        return Component.translatable("slimpatch.dialogue.action.family").getString();
    }

    private List<String> getCurrentMenuOptions() {
        List<String> options = new ArrayList<>();
        if (menuMode == MenuMode.ACTIONS) {
            options.add(getHomeActionLabel());
            options.add(getFollowActionLabel());
            options.add(getStayActionLabel());
            options.add("Back");
            return options;
        }
        if (menuMode == MenuMode.SOCIAL) {
            options.add("Friendly");
            options.add("Mean");
            options.add("Joke");
            if (canShowFlirtOption()) {
                options.add("Flirt");
            }
            return options;
        }
        if (menuMode == MenuMode.FAMILY) {
            if (canShowHaveChildOption()) {
                options.add(Component.translatable("slimpatch.dialogue.action.have_child").getString());
            }
            if (canShowAskAboutBabyOption()) {
                options.add(Component.translatable("slimpatch.dialogue.action.ask_about_baby").getString());
            }
            if (this.villager instanceof MaleVillagerEntity || this.villager instanceof FemaleVillagerEntity) {
                options.add(Component.translatable("slimpatch.dialogue.action.family_tree").getString());
            }
            options.add("Back");
            return options;
        }
        if (menuMode == MenuMode.FAMILY_CONFIRM) {
            options.add(Component.translatable("slimpatch.dialogue.action.yes").getString());
            options.add(Component.translatable("slimpatch.dialogue.action.no").getString());
        }
        return options;
    }

    private void onFamilyActionClicked(String action) {
        String haveChildLabel = Component.translatable("slimpatch.dialogue.action.have_child").getString();
        String askAboutBabyLabel = Component.translatable("slimpatch.dialogue.action.ask_about_baby").getString();
        String familyTreeLabel = Component.translatable("slimpatch.dialogue.action.family_tree").getString();
        String yesLabel = Component.translatable("slimpatch.dialogue.action.yes").getString();
        String noLabel = Component.translatable("slimpatch.dialogue.action.no").getString();

        if (menuMode == MenuMode.FAMILY) {
            if (action.equals("Back")) {
                this.birthReadyConfirmationPending = false;
                setMenuMode(MenuMode.SOCIAL);
                return;
            }
            if (action.equals(haveChildLabel) && canShowHaveChildOption()) {
                this.birthReadyConfirmationPending = false;
                setMenuMode(MenuMode.FAMILY_CONFIRM);
                setDialogueLine(Component.translatable("slimpatch.dialogue.family.confirm").getString());
                return;
            }
            if (action.equals(askAboutBabyLabel) && canShowAskAboutBabyOption()) {
                net.neoforged.neoforge.network.PacketDistributor.sendToServer(new FamilyStatusPacket(villager.getId()));
                return;
            }
            if (action.equals(familyTreeLabel) && (this.villager instanceof MaleVillagerEntity || this.villager instanceof FemaleVillagerEntity)) {
                net.neoforged.neoforge.network.PacketDistributor.sendToServer(new RequestFamilyTreePacket(villager.getId()));
                this.onClose();
            }
            return;
        }

        if (menuMode != MenuMode.FAMILY_CONFIRM) {
            return;
        }

        if (action.equals(noLabel)) {
            if (this.birthReadyConfirmationPending) {
                this.birthReadyConfirmationPending = false;
                setMenuMode(MenuMode.FAMILY);
                setDialogueLine(Component.translatable("slimpatch.dialogue.family.birth_decline").getString());
                return;
            }
            setMenuMode(MenuMode.FAMILY);
            setDialogueLine(Component.translatable("slimpatch.dialogue.family.decline").getString());
            return;
        }

        if (action.equals(yesLabel)) {
            if (this.birthReadyConfirmationPending) {
                this.birthReadyConfirmationPending = false;
                net.neoforged.neoforge.network.PacketDistributor.sendToServer(new RequestBirthScreenPacket(villager.getId()));
                this.onClose();
                return;
            }
            net.neoforged.neoforge.network.PacketDistributor.sendToServer(new StartFamilyPacket(villager.getId()));
            this.onClose();
        }
    }

    private void setDialogueLine(String line) {
        this.fullLine = line;
        this.visibleChars = 0;
        this.lastCharTime = System.currentTimeMillis();
    }

    private boolean canShowHaveChildOption() {
        if (!(this.villager instanceof FamilyVillager familyVillager)) {
            return false;
        }
        return familyVillager.getRelationshipStage() == VillagerRelationshipStage.MARRIED
                && familyVillager.getAgeStage() == com.javic.slimpatch.entity.VillagerAgeStage.ADULT
                && !familyVillager.isExpectingChild()
                && isLocalPlayerSpouse(familyVillager);
    }

    private boolean canShowAskAboutBabyOption() {
        if (!(this.villager instanceof FamilyVillager familyVillager)) {
            return false;
        }
        return familyVillager.getRelationshipStage() == VillagerRelationshipStage.MARRIED
                && familyVillager.getAgeStage() == com.javic.slimpatch.entity.VillagerAgeStage.ADULT
                && familyVillager.isExpectingChild()
                && isLocalPlayerSpouse(familyVillager);
    }

    private boolean isLocalPlayerSpouse(FamilyVillager familyVillager) {
        UUID playerUuid = this.minecraft != null && this.minecraft.player != null ? this.minecraft.player.getUUID() : null;
        String playerName = this.minecraft != null && this.minecraft.player != null ? this.minecraft.player.getGameProfile().getName() : "";
        boolean isSpouse = playerUuid != null && playerUuid.equals(familyVillager.getSpousePlayerUuid());
        if (!isSpouse && !playerName.isEmpty()) {
            isSpouse = playerName.equals(familyVillager.getSpousePlayerName());
        }
        return isSpouse;
    }

    private float getDisplayedRelationship() {
        if (villager instanceof MaleVillagerEntity male) {
            return VillagerRelationshipData.getDisplayedRelationship(male, male.getRelationship());
        }
        if (villager instanceof FemaleVillagerEntity female) {
            return VillagerRelationshipData.getDisplayedRelationship(female, female.getRelationship());
        }
        return 0.5f;
    }

    private float getDisplayedGoldenRelationship() {
        if (villager instanceof MaleVillagerEntity male) {
            float fallback = shouldUseGlobalDatingState() ? male.getGoldenRelationship() : 0.0F;
            return VillagerRelationshipData.getDisplayedGoldenRelationship(male, fallback);
        }
        if (villager instanceof FemaleVillagerEntity female) {
            float fallback = shouldUseGlobalDatingState() ? female.getGoldenRelationship() : 0.0F;
            return VillagerRelationshipData.getDisplayedGoldenRelationship(female, fallback);
        }
        return 0.0F;
    }

    private boolean shouldUseGlobalDatingState() {
        return this.minecraft != null
                && this.minecraft.getSingleplayerServer() != null
                && !this.minecraft.getSingleplayerServer().isPublished();
    }

    private boolean canUseProtectedCommandAction(String action) {
        if (action.equals("Back")) {
            return true;
        }

        String homeActionLabel = getHomeActionLabel();
        String followActionLabel = getFollowActionLabel();
        String stayActionLabel = getStayActionLabel();
        if (!action.equals(homeActionLabel) && !action.equals(followActionLabel) && !action.equals(stayActionLabel)) {
            return true;
        }

        return canUseSensitiveAction();
    }

    private VillagerCommandState getCommandState() {
        if (this.villager instanceof com.javic.slimpatch.entity.CommandableVillager commandableVillager) {
            return commandableVillager.getCommandState();
        }
        return VillagerCommandState.NONE;
    }

    private void openEditVillagerScreen() {
        if (!canUseSensitiveAction()) {
            showOwnerDeniedMessage();
            return;
        }
        Minecraft.getInstance().setScreen(new VillagerEditScreen(villager));
    }

    private void openVillagerEquipmentScreen() {
        if (this.minecraft != null && this.minecraft.player != null) {
            if (!canUseSensitiveAction()) {
                showOwnerDeniedMessage();
                return;
            }
            if (this.villager instanceof net.minecraft.world.entity.LivingEntity livingEntity) {
                VillagerEquipmentScreen.setPreviewVillager(livingEntity);
            }
            net.neoforged.neoforge.network.PacketDistributor.sendToServer(new VillagerEquipmentPacket(villager.getId()));
            this.onClose();
        }
    }

    private void onQuestAction(String action) {
        if (!isQuestUiEnabled()) {
            return;
        }
        String questId = "";
        if (villager instanceof com.javic.slimpatch.entity.MaleVillagerEntity male) {
            questId = male.getQuestId();
        } else if (villager instanceof com.javic.slimpatch.entity.FemaleVillagerEntity female) {
            questId = female.getQuestId();
        } else if (villager.getPersistentData().contains("QuestId")) {
            questId = villager.getPersistentData().getString("QuestId");
        }

        if (questId != null && !questId.isEmpty()) {
            if (this.minecraft == null || this.minecraft.level == null || this.minecraft.isLocalServer()) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.getConnection() != null) {
                    mc.getConnection().send(new QuestPacket(questId, action));
                }
            } else {
                net.neoforged.neoforge.network.PacketDistributor.sendToServer(new QuestPacket(questId, action));
            }
        }

        switch (action.toLowerCase()) {
            case "accept" -> {
                fullLine = DialogueManager.getGenericQuestLine("QuestAccept");
                questAccepted = true;
                playPositiveSound();
                this.clearWidgets();
                this.init();
            }
            case "decline" -> {
                fullLine = DialogueManager.getGenericQuestLine("QuestDecline");
                questAccepted = false;
                questCompleted = false;
                hasQuest = true;
                this.clearWidgets();
                this.init();
            }
            case "cancel" -> {
                fullLine = DialogueManager.getGenericQuestLine("QuestCancel");
                questAccepted = false;
                questCompleted = false;
                hasQuest = true;
                this.clearWidgets();
                this.init();
            }
            case "complete" -> {
                fullLine = DialogueManager.getGenericQuestLine("QuestComplete");
                playPositiveSound();
                questAccepted = false;
                questCompleted = true;
                hasQuest = false;
                this.clearWidgets();
                this.init();
            }
        }

        visibleChars = 0;
        lastCharTime = System.currentTimeMillis();
    }

    private void playPositiveSound() {
        if (!Config.CUSTOM_VILLAGER_SOUNDS.get()) return;
        if (villager instanceof MaleVillagerEntity) {
            villager.level().playSound(null, villager.blockPosition(),
                    com.javic.slimpatch.sounds.HumanVillagerSounds.maleReactionPositive(),
                    net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, VillagerFamilyData.getAgeStagePitch((MaleVillagerEntity) villager));
        } else if (villager instanceof FemaleVillagerEntity) {
            villager.level().playSound(null, villager.blockPosition(),
                    com.javic.slimpatch.sounds.HumanVillagerSounds.femaleReactionPositive(),
                    net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, VillagerFamilyData.getAgeStagePitch((FemaleVillagerEntity) villager));
        }
    }

    @Override
    public void tick() {
        super.tick();
    }

    private void applyEdgePan() {
        if (this.minecraft == null || this.minecraft.player == null || this.minecraft.getWindow() == null) {
            lastEdgePanTimeNanos = System.nanoTime();
            return;
        }

        long now = System.nanoTime();
        if (lastEdgePanTimeNanos == 0L) {
            lastEdgePanTimeNanos = now;
            return;
        }

        float deltaTicks = (float) ((now - lastEdgePanTimeNanos) / 1_000_000_000.0D) * EDGE_PAN_TICKS_PER_SECOND;
        lastEdgePanTimeNanos = now;

        if (deltaTicks <= 0.0F) {
            return;
        }

        deltaTicks = Math.min(deltaTicks, MAX_EDGE_PAN_DELTA_TICKS);

        float sensitivity = Config.DIALOGUE_CAMERA_SENSITIVITY.get().floatValue();
        boolean firstPerson = this.minecraft.options == null || this.minecraft.options.getCameraType().isFirstPerson();

        double mouseX = this.minecraft.mouseHandler.xpos() * (double) this.width / (double) this.minecraft.getWindow().getScreenWidth();
        double mouseY = this.minecraft.mouseHandler.ypos() * (double) this.height / (double) this.minecraft.getWindow().getScreenHeight();

        if (!isMouseInAllowedEdgePanZone(mouseX, mouseY)) {
            if (!firstPerson) {
                DialogueThirdPersonCameraHandler.update(0.0F, 0.0F, sensitivity, deltaTicks);
            }
            return;
        }

        float yawDelta = 0.0F;
        if (isMouseInAllowedLeftEdgePanZone(mouseX, mouseY) || isMouseInAllowedRightEdgePanZone(mouseX, mouseY)) {
            yawDelta = getEdgePanDelta(mouseX, this.width, MAX_YAW_STEP);
        }

        float pitchDelta = 0.0F;
        if (isMouseInAllowedTopEdgePanZone(mouseX, mouseY) || isMouseInAllowedBottomEdgePanZone(mouseX, mouseY)) {
            pitchDelta = getEdgePanDelta(mouseY, this.height, MAX_PITCH_STEP);
        }

        if (!firstPerson) {
            if (Config.INVERT_DIALOGUE_CAMERA_X.get()) {
                yawDelta = -yawDelta;
            }
            if (Config.INVERT_DIALOGUE_CAMERA_Y.get()) {
                pitchDelta = -pitchDelta;
            }
            DialogueThirdPersonCameraHandler.update(yawDelta, pitchDelta, sensitivity, deltaTicks);
            return;
        }

        if (yawDelta == 0.0F && pitchDelta == 0.0F) {
            return;
        }

        var player = this.minecraft.player;
        float newYaw = player.getYRot() + yawDelta * sensitivity * deltaTicks;
        float newPitch = Mth.clamp(player.getXRot() + pitchDelta * sensitivity * deltaTicks, -MAX_PITCH, MAX_PITCH);

        player.setYRot(newYaw);
        player.setYHeadRot(newYaw);
        player.setXRot(newPitch);
    }

    private float getEdgePanDelta(double mousePosition, int size, float maxStep) {
        if (mousePosition <= EDGE_PAN_ZONE) {
            double strength = (EDGE_PAN_ZONE - mousePosition) / EDGE_PAN_ZONE;
            return (float) (-maxStep * strength * strength);
        }

        double farEdge = size - EDGE_PAN_ZONE;
        if (mousePosition >= farEdge) {
            double strength = (mousePosition - farEdge) / EDGE_PAN_ZONE;
            return (float) (maxStep * strength * strength);
        }

        return 0.0F;
    }

    private boolean isMouseInAllowedEdgePanZone(double mouseX, double mouseY) {
        return isMouseInAllowedLeftEdgePanZone(mouseX, mouseY)
                || isMouseInAllowedRightEdgePanZone(mouseX, mouseY)
                || isMouseInAllowedTopEdgePanZone(mouseX, mouseY)
                || isMouseInAllowedBottomEdgePanZone(mouseX, mouseY);
    }

    private boolean isMouseInAllowedLeftEdgePanZone(double mouseX, double mouseY) {
        return isPointInRect(mouseX, mouseY,
                0,
                getCenteredSideEdgeTop(),
                (int) EDGE_PAN_ZONE,
                getCenteredSideEdgeBottom());
    }

    private boolean isMouseInAllowedRightEdgePanZone(double mouseX, double mouseY) {
        return isPointInRect(mouseX, mouseY,
                this.width - (int) EDGE_PAN_ZONE,
                getCenteredSideEdgeTop(),
                this.width,
                getCenteredSideEdgeBottom());
    }

    private boolean isMouseInAllowedTopEdgePanZone(double mouseX, double mouseY) {
        return isPointInRect(mouseX, mouseY,
                getCenteredTopEdgeLeft(),
                0,
                getCenteredTopEdgeRight(),
                (int) EDGE_PAN_ZONE);
    }

    private boolean isMouseInAllowedBottomEdgePanZone(double mouseX, double mouseY) {
        return isPointInRect(mouseX, mouseY,
                getCenteredBottomEdgeLeft(),
                this.height - BOTTOM_EDGE_PAN_HEIGHT,
                getCenteredBottomEdgeRight(),
                this.height);
    }

    private int getCenteredSideEdgeTop() {
        int allowedHeight = this.height / SIDE_EDGE_PAN_HEIGHT_DIVISOR;
        return (this.height - allowedHeight) / 2;
    }

    private int getCenteredSideEdgeBottom() {
        int allowedHeight = this.height / SIDE_EDGE_PAN_HEIGHT_DIVISOR;
        return getCenteredSideEdgeTop() + allowedHeight;
    }

    private int getCenteredTopEdgeLeft() {
        int allowedWidth = this.width / TOP_EDGE_PAN_WIDTH_DIVISOR;
        return (this.width - allowedWidth) / 2;
    }

    private int getCenteredTopEdgeRight() {
        int allowedWidth = this.width / TOP_EDGE_PAN_WIDTH_DIVISOR;
        return getCenteredTopEdgeLeft() + allowedWidth;
    }

    private int getCenteredBottomEdgeLeft() {
        int allowedWidth = this.width / BOTTOM_EDGE_PAN_WIDTH_DIVISOR;
        return (this.width - allowedWidth) / 2;
    }

    private int getCenteredBottomEdgeRight() {
        int allowedWidth = this.width / BOTTOM_EDGE_PAN_WIDTH_DIVISOR;
        return getCenteredBottomEdgeLeft() + allowedWidth;
    }

    private int getBottomCenterSafeTop() {
        int totalHeight = (optionHeight + optionSpacing) * 4;
        int startY = this.height - totalHeight - 40;
        return Math.min(this.height - 165, startY) - SAFE_ZONE_PADDING;
    }

    private int getBottomCenterSafeHalfWidth() {
        return optionWidth + 20 + SAFE_ZONE_PADDING;
    }

    private boolean isPointInRect(double mouseX, double mouseY, int minX, int minY, int maxX, int maxY) {
        return mouseX >= minX && mouseX <= maxX && mouseY >= minY && mouseY <= maxY;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        applyEdgePan();
        this.renderBackground(graphics, mouseX, mouseY, partialTicks);
        graphics.fill(0, this.height - 150, this.width, this.height, 0x88000000);
        super.render(graphics, mouseX, mouseY, partialTicks);
        renderExitHint(graphics);
        int heartX = 8;
        int topY = 10;
        float relationship = getDisplayedRelationship();
        for (int i = 0; i < 5; i++) {
            int x = heartX + i * (HEART_SIZE + 2);
            if (i + 1 <= (int) relationship) {
                graphics.blit(HEART_FULL, x, topY, 0, 0, HEART_SIZE, HEART_SIZE, HEART_SIZE, HEART_SIZE);
            } else if (i + 0.5 <= relationship) {
                graphics.blit(HEART_HALF, x, topY, 0, 0, HEART_SIZE, HEART_SIZE, HEART_SIZE, HEART_SIZE);
            } else {
                graphics.blit(HEART_EMPTY, x, topY, 0, 0, HEART_SIZE, HEART_SIZE, HEART_SIZE, HEART_SIZE);
            }
        }
        float goldenRelationship = Mth.clamp(getDisplayedGoldenRelationship(), 0.0F, 5.0F);
        if (goldenRelationship > 0.0F) {
            for (int i = 0; i < 5; i++) {
                int x = heartX + i * (HEART_SIZE + 2);
                if (i + 1 <= goldenRelationship) {
                    graphics.blit(HEART_GOLD_FULL, x, topY, 0, 0, HEART_SIZE, HEART_SIZE, HEART_SIZE, HEART_SIZE);
                } else if (i == 0 && goldenRelationship > 0.0F) {
                    graphics.blit(HEART_GOLD_HALF, x, topY, 0, 0, HEART_SIZE, HEART_SIZE, HEART_SIZE, HEART_SIZE);
                } else if (i + 0.5 <= goldenRelationship) {
                    graphics.blit(HEART_GOLD_HALF, x, topY, 0, 0, HEART_SIZE, HEART_SIZE, HEART_SIZE, HEART_SIZE);
                }
            }
        }
        int leftX = 10;
        int textY = topY + HEART_SIZE + 8;
        String villagerName = this.villager.hasCustomName()
                ? this.villager.getCustomName().getString()
                : this.villager.getName().getString();
        String displayedVillagerName = getDisplayedVillagerName(villagerName);
        drawTextWithIcon(graphics, displayedVillagerName, leftX, textY, 0xFFFFFF, null, mouseX, mouseY, null);
        int nameWidth = this.font.width(displayedVillagerName);
        int nameX1 = leftX;
        int nameX2 = leftX + nameWidth;
        int nameY1 = textY;
        int nameY2 = nameY1 + this.font.lineHeight;
        if (mouseX >= nameX1 && mouseX <= nameX2 && mouseY >= nameY1 && mouseY <= nameY2) {
            graphics.renderTooltip(this.font, getNameRelationshipLabel(), mouseX, mouseY);
        }
        textY += 20;
        VillagerPersonality personality = null;
        if (this.villager instanceof MaleVillagerEntity male) {
            personality = male.getPersonality();
        } else if (this.villager instanceof FemaleVillagerEntity female) {
            personality = female.getPersonality();
        }
        if (personality != null) {
            String personalityName = VillagerPersonalityIcons.getName(personality);
            ResourceLocation personalityIcon = VillagerPersonalityIcons.getIcon(personality);
            drawTextWithIcon(graphics, personalityName, leftX, textY, 0xFFFFFF,
                    Component.translatable("slimpatch.dialogue.hover.personality"), mouseX, mouseY, personalityIcon);
        } else {
            drawTextWithIcon(graphics, "DEBUG_NULL", leftX, textY, 0xFF0000,
                    Component.translatable("slimpatch.dialogue.hover.personality"), mouseX, mouseY, null);
        }
        textY += 20;
        VillagerProfession profession = this.villager.getVillagerData().getProfession();
        String professionName = VillagerProfessionIcons.getName(profession);
        ResourceLocation professionIcon = VillagerProfessionIcons.getIcon(profession);
        drawTextWithIcon(graphics, professionName, leftX, textY, 0xFFFFFF,
                Component.translatable("slimpatch.dialogue.hover.job"), mouseX, mouseY, professionIcon);

        textY += 20;
        UUID playerUuid = this.minecraft != null && this.minecraft.player != null ? this.minecraft.player.getUUID() : null;
        boolean hasCooldown = VillagerCooldownsStorage.hasGiftCooldown(villager.getUUID(), playerUuid);
        boolean canReceiveGift = relationship >= 5.0f && !hasCooldown;
        ResourceLocation icon = canReceiveGift ? GIFT_ICON_COLOR : GIFT_ICON_GRAY;
        int iconSize = 18;
        int iconX = leftX - 2;
        int iconY = textY;
        graphics.blit(icon, iconX, iconY, 0, 0, iconSize, iconSize, iconSize, iconSize);

        boolean hoveringGift = mouseX >= iconX && mouseX <= iconX + iconSize && mouseY >= iconY && mouseY <= iconY + iconSize;
        if (hoveringGift) {
            int glowSize = iconSize + 2;
            graphics.setColor(1.0F, 1.0F, 1.0F, 0.10F);
            graphics.blit(icon, iconX - 1, iconY - 1, 0, 0, glowSize, glowSize, glowSize, glowSize);
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
        graphics.blit(icon, iconX, iconY, 0, 0, iconSize, iconSize, iconSize, iconSize);

        if (mouseX >= iconX && mouseX <= iconX + iconSize && mouseY >= iconY && mouseY <= iconY + iconSize) {
            Component tooltip = hasCooldown ? Component.translatable("slimpatch.dialogue.gift.cooldown")
                    : canReceiveGift ? Component.translatable("slimpatch.dialogue.gift.available")
                    : Component.translatable("slimpatch.dialogue.gift.locked");
            graphics.renderTooltip(this.font, tooltip, mouseX, mouseY);
        }

        long now = System.currentTimeMillis();
        if (visibleChars < fullLine.length() && now - lastCharTime > CHAR_INTERVAL_MS) {
            visibleChars++;
            lastCharTime = now;
        }
        String visibleText = fullLine.substring(0, Math.min(visibleChars, fullLine.length()));
        String villagerLine = villagerName + ": " + visibleText;
        drawCenteredBorderedString(graphics, villagerLine, this.width / 2, this.height - 165, 0xFFF8E3);
        int totalHeartWidth = (HEART_SIZE + 2) * 5;
        if (mouseX >= heartX && mouseX <= heartX + totalHeartWidth &&
            mouseY >= topY && mouseY <= topY + HEART_SIZE) {
            graphics.renderComponentTooltip(this.font, getRelationshipTooltip(), mouseX, mouseY);
        }

        String commandStatus = this.getCommandStatusText();
        if (commandStatus != null) {
            int commandStatusY = this.familyButtonY > 0
                    ? this.familyButtonY - this.font.lineHeight - 2
                    : this.actionsButtonY - this.font.lineHeight - 2;
            graphics.drawCenteredString(this.font, commandStatus,
                    this.actionsButtonX + this.actionsButtonWidth / 2,
                    commandStatusY,
                    0xFFFFFF);
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (Config.DIALOGUE_SCREEN_BLUR.get()) {
            super.renderBackground(graphics, mouseX, mouseY, partialTick);
        }
    }

    private void renderExitHint(GuiGraphics graphics) {
        long elapsed = System.currentTimeMillis() - exitHintStartTime;
        if (elapsed >= EXIT_HINT_DURATION_MS) {
            return;
        }

        float alpha = 1.0F;
        long fadeStart = EXIT_HINT_DURATION_MS - EXIT_HINT_FADE_OUT_MS;
        if (elapsed > fadeStart) {
            alpha = 1.0F - (float) (elapsed - fadeStart) / (float) EXIT_HINT_FADE_OUT_MS;
        }

        alpha = Mth.clamp(alpha, 0.0F, 1.0F);

        float pulse = 0.94F + 0.06F * Mth.sin(elapsed / 180.0F);
        int alphaValue = Mth.clamp((int) (255.0F * alpha * pulse), 0, 255);
        if (alphaValue <= 3) {
            return;
        }

        int textColor = alphaValue << 24 | 0xF8E3C0;
        int shadowColor = alphaValue << 24;
        int x = this.width / 2;
        int y = 12;
        String exitHintText = Component.translatable("slimpatch.dialogue.exit_hint", ModKeyBindings.getOpenDialogueKeyMessage()).getString();

        graphics.drawCenteredString(this.font, exitHintText, x + 1, y, shadowColor);
        graphics.drawCenteredString(this.font, exitHintText, x - 1, y, shadowColor);
        graphics.drawCenteredString(this.font, exitHintText, x, y + 1, shadowColor);
        graphics.drawCenteredString(this.font, exitHintText, x, y - 1, shadowColor);
        graphics.drawCenteredString(this.font, exitHintText, x, y, textColor);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (ModKeyBindings.OPEN_DIALOGUE != null && ModKeyBindings.OPEN_DIALOGUE.matches(keyCode, scanCode)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int leftX = 10;
        int topY = 10;
        int textY = topY + HEART_SIZE + 8 + 20 + 20 + 20;
        int iconX = leftX - 2;
        int iconY = textY;
        int iconSize = 18;

        UUID playerUuid = this.minecraft != null && this.minecraft.player != null ? this.minecraft.player.getUUID() : null;
        boolean hasCooldown = VillagerCooldownsStorage.hasGiftCooldown(villager.getUUID(), playerUuid);
        float relationship = getDisplayedRelationship();
        boolean canReceiveGift = relationship >= 5.0f && !hasCooldown;

        if (mouseX >= iconX && mouseX <= iconX + iconSize && mouseY >= iconY && mouseY <= iconY + iconSize) {
            this.minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F));
            if (canReceiveGift && this.minecraft != null) {
                net.neoforged.neoforge.network.PacketDistributor.sendToServer(new GiftPacket(villager.getId()));
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void drawTextWithIcon(GuiGraphics graphics, String text, int x, int y, int color,
                                  Component hover, int mouseX, int mouseY, ResourceLocation icon) {
        int textStartX = x;
        graphics.drawString(this.font, text, textStartX + 1, y, 0x000000);
        graphics.drawString(this.font, text, textStartX - 1, y, 0x000000);
        graphics.drawString(this.font, text, textStartX, y + 1, 0x000000);
        graphics.drawString(this.font, text, textStartX, y - 1, 0x000000);
        graphics.drawString(this.font, text, textStartX, y, color);
        int textWidth = this.font.width(text);
        int hoverX1 = textStartX;
        int hoverX2 = textStartX + textWidth;
        int hoverY1 = y;
        int hoverY2 = y + this.font.lineHeight;
        if (icon != null) {
            int iconX = textStartX + textWidth + 3;
            int iconY = y + ((this.font.lineHeight - 15) / 2);
            graphics.blit(icon, iconX, iconY, 0, 0, 15, 15, 15, 15);
            hoverX2 = iconX + 15;
            hoverY1 = Math.min(hoverY1, iconY);
            hoverY2 = Math.max(hoverY2, iconY + 15);
        }
        if (hover != null) {
            if (mouseX >= hoverX1 && mouseX <= hoverX2 && mouseY >= hoverY1 && mouseY <= hoverY2) {
                graphics.renderTooltip(this.font, hover, mouseX, mouseY);
            }
        }
    }

    private void drawCenteredBorderedString(GuiGraphics graphics, String text, int x, int y, int color) {
        int shadow = 0x000000;
        graphics.drawCenteredString(this.font, text, x + 1, y, shadow);
        graphics.drawCenteredString(this.font, text, x - 1, y, shadow);
        graphics.drawCenteredString(this.font, text, x, y + 1, shadow);
        graphics.drawCenteredString(this.font, text, x, y - 1, shadow);
        graphics.drawCenteredString(this.font, text, x, y, color);
    }

    private String getDisplayedVillagerName(String villagerName) {
        if (!(this.villager instanceof FamilyVillager familyVillager)) {
            return villagerName;
        }
        VillagerAgeStage ageStage = familyVillager.getAgeStage();
        if (ageStage == VillagerAgeStage.ADULT) {
            return villagerName;
        }
        return villagerName + " (" + getLifeStageLabel(ageStage) + ")";
    }

    private String getLifeStageLabel(VillagerAgeStage ageStage) {
        return Component.translatable("slimpatch.life_stage." + ageStage.name().toLowerCase(java.util.Locale.ROOT)).getString();
    }	

    private List<Component> getRelationshipTooltip() {
        boolean canUseRomanticInteraction = canUseRomanticInteraction();
        if (this.villager instanceof FamilyVillager familyVillager) {
            if (familyVillager.isMarried()) {
                String spousePlayerName = familyVillager.getSpousePlayerName();
                if (!spousePlayerName.isEmpty()) {
                    return java.util.List.of(Component.translatable("slimpatch.dialogue.hover.relationship_married_to", spousePlayerName));
                }
                return java.util.List.of(Component.translatable("slimpatch.dialogue.hover.relationship_married"));
            }
            float displayedGoldenRelationship = getDisplayedGoldenRelationship();
            if (canUseRomanticInteraction && displayedGoldenRelationship >= 5.0F) {
                return java.util.List.of(Component.translatable("slimpatch.dialogue.hover.relationship_ring_hint"));
            }
            if (canUseRomanticInteraction && displayedGoldenRelationship > 0.0F) {
                return java.util.List.of(Component.translatable("slimpatch.dialogue.hover.relationship_dating"));
            } else if (canUseRomanticInteraction && this.minecraft != null && this.minecraft.player != null
                    && VillagerFamilyData.canStartDating(familyVillager, this.villager, this.minecraft.player, getDisplayedRelationship())) {
                return java.util.List.of(Component.translatable("slimpatch.dialogue.hover.relationship_flower_hint"));
            }
        }
        return java.util.List.of(Component.translatable("slimpatch.dialogue.hover.relationship"));
    }

    private boolean canUseRomanticInteraction() {
        return this.minecraft != null
                && this.minecraft.player != null
                && VillagerFamilyData.canUseRomanticInteraction(this.villager, this.minecraft.player);
    }

    private boolean canShowFlirtOption() {
        return canUseRomanticInteraction();
    }

    private Component getNameRelationshipLabel() {
        boolean genderedLabels = Config.GENDERED_RELATIONSHIP_LABELS.get();
        boolean male = this.villager instanceof MaleVillagerEntity;
        boolean female = this.villager instanceof FemaleVillagerEntity;
        if (this.minecraft != null && this.minecraft.player != null && VillagerFamilyData.isFamilyChildOf(this.villager, this.minecraft.player)) {
            if (!genderedLabels) {
                return Component.translatable("slimpatch.relationship_label.your_child");
            }
            if (male) {
                return Component.translatable("slimpatch.relationship_label.your_son");
            }
            if (female) {
                return Component.translatable("slimpatch.relationship_label.your_daughter");
            }
            return Component.translatable("slimpatch.relationship_label.your_child");
        }
        if (this.villager instanceof FamilyVillager familyVillager) {
            UUID playerUuid = this.minecraft != null && this.minecraft.player != null ? this.minecraft.player.getUUID() : null;
            String playerName = this.minecraft != null && this.minecraft.player != null ? this.minecraft.player.getGameProfile().getName() : "";
            if (familyVillager.getRelationshipStage() == VillagerRelationshipStage.MARRIED) {
                boolean isSpouse = playerUuid != null && playerUuid.equals(familyVillager.getSpousePlayerUuid());
                if (!isSpouse && !playerName.isEmpty()) {
                    isSpouse = playerName.equals(familyVillager.getSpousePlayerName());
                }
                if (isSpouse) {
                    if (!genderedLabels) {
                        return Component.translatable("slimpatch.relationship_label.spouse");
                    }
                    if (male) {
                        return Component.translatable("slimpatch.relationship_label.husband");
                    }
                    if (female) {
                        return Component.translatable("slimpatch.relationship_label.wife");
                    }
                    return Component.translatable("slimpatch.relationship_label.spouse");
                }
                return getBaseRelationshipLabel();
            }
            boolean isFormerSpouse = playerUuid != null && playerUuid.equals(familyVillager.getFormerSpousePlayerUuid());
            if (!isFormerSpouse && !playerName.isEmpty()) {
                isFormerSpouse = playerName.equals(familyVillager.getFormerSpousePlayerName());
            }
            if (isFormerSpouse) {
                if (!genderedLabels) {
                    return Component.translatable("slimpatch.relationship_label.ex_spouse");
                }
                if (male) {
                    return Component.translatable("slimpatch.relationship_label.ex_husband");
                }
                if (female) {
                    return Component.translatable("slimpatch.relationship_label.ex_wife");
                }
                return Component.translatable("slimpatch.relationship_label.ex_spouse");
            }
            if (canUseRomanticInteraction() && getDisplayedGoldenRelationship() > 0.0F) {
                if (!genderedLabels) {
                    return Component.translatable("slimpatch.relationship_label.romantic_partner");
                }
                if (male) {
                    return Component.translatable("slimpatch.relationship_label.boyfriend");
                }
                if (female) {
                    return Component.translatable("slimpatch.relationship_label.girlfriend");
                }
                return Component.translatable("slimpatch.relationship_label.romantic_partner");
            }
        }

        return getBaseRelationshipLabel();
    }

    private Component getBaseRelationshipLabel() {
        float relationship = getDisplayedRelationship();
        if (relationship >= 4.0F) {
            return Component.translatable("slimpatch.relationship_label.close_friend");
        }
        if (relationship >= 3.0F) {
            return Component.translatable("slimpatch.relationship_label.friend");
        }
        if (relationship >= 1.0F) {
            return Component.translatable("slimpatch.relationship_label.acquaintance");
        }
        return Component.translatable("slimpatch.relationship_label.stranger");
    }	

    private String getCommandStatusText() {
        VillagerCommandState state = null;
        String ownerName = null;
        if (this.villager instanceof MaleVillagerEntity male) {
            state = male.getCommandState();
            ownerName = male.getCommandOwnerName();
        } else if (this.villager instanceof FemaleVillagerEntity female) {
            state = female.getCommandState();
            ownerName = female.getCommandOwnerName();
        }

        if (state == VillagerCommandState.FOLLOW) {
            if (shouldShowOwnerName(ownerName)) {
                return Component.translatable("slimpatch.dialogue.status.following_owner", ownerName).getString();
            }
            return Component.translatable("slimpatch.dialogue.status.following").getString();
        }
        if (state == VillagerCommandState.STAY) {
            if (shouldShowOwnerName(ownerName)) {
                return Component.translatable("slimpatch.dialogue.status.stay_owner", ownerName).getString();
            }
            return Component.translatable("slimpatch.dialogue.status.stay").getString();
        }
        return null;
    }

    private boolean canUseSensitiveAction() {
        if (this.minecraft == null || this.minecraft.player == null) {
            return false;
        }
        if (this.minecraft.getSingleplayerServer() != null && !this.minecraft.getSingleplayerServer().isPublished()) {
            return true;
        }
        if (this.villager instanceof com.javic.slimpatch.entity.CommandableVillager commandableVillager) {
            java.util.UUID ownerUuid = commandableVillager.getCommandOwnerUuid();
            return ownerUuid == null || ownerUuid.equals(this.minecraft.player.getUUID());
        }
        return true;
    }

    private void showOwnerDeniedMessage() {
        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }
        String ownerName = null;
        if (this.villager instanceof com.javic.slimpatch.entity.CommandableVillager commandableVillager) {
            ownerName = commandableVillager.getCommandOwnerName();
        }
        if (ownerName == null || ownerName.isEmpty()) {
            ownerName = Component.translatable("slimpatch.message.other_player").getString();
        }
        this.minecraft.player.displayClientMessage(Component.translatable("slimpatch.message.villager_controlled", ownerName), true);
    }

    private boolean shouldShowOwnerName(String ownerName) {
        if (ownerName == null || ownerName.isEmpty()) {
            return false;
        }
        return this.minecraft == null
                || this.minecraft.getSingleplayerServer() == null
                || this.minecraft.getSingleplayerServer().isPublished();
    }

    @Override
    public void onClose() {
        DialogueThirdPersonCameraHandler.reset();
        restoreDialogueCameraPreference();
        super.onClose();
        if (this.villager != null) {
            if (this.minecraft != null && this.minecraft.player != null) {
                net.neoforged.neoforge.network.PacketDistributor.sendToServer(new DialogueStatePacket(this.villager.getId(), false));
            }
            DialogueManager.endDialogue(this.villager);
        }
    }

    @Override
    public void removed() {
        DialogueThirdPersonCameraHandler.reset();
        restoreDialogueCameraPreference();
        super.removed();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBlurredBackground(float partialTick) {
        if (Config.DIALOGUE_SCREEN_BLUR.get()) {
            super.renderBlurredBackground(partialTick);
        }
    }

    private void onOptionClicked(String option) {
        if (this.minecraft != null && this.minecraft.player != null) {
            net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                    new RelationshipPacket(villager.getId(), option)
            );
        }
    }

    public void showSpouseCookingLine(int entityId, String lineKey) {
        if (this.villager.getId() != entityId) {
            return;
        }
        this.fullLine = Component.translatable(lineKey).getString();
        this.visibleChars = 0;
        this.lastCharTime = System.currentTimeMillis();
    }

    public void showFamilyLine(int entityId, String lineKey) {
        if (this.villager.getId() != entityId) {
            return;
        }
        this.birthReadyConfirmationPending = false;
        this.fullLine = Component.translatable(lineKey).getString();
        this.visibleChars = 0;
        this.lastCharTime = System.currentTimeMillis();
    }

    public void showBirthReadyPrompt(int entityId, String lineKey) {
        if (this.villager.getId() != entityId) {
            return;
        }
        this.birthReadyConfirmationPending = true;
        setMenuMode(MenuMode.FAMILY_CONFIRM);
        setDialogueLine(Component.translatable(lineKey).getString());
    }

    public void showDialogueResult(int entityId, String option, boolean success, String line) {
        if (this.villager.getId() != entityId) {
            return;
        }
        fullLine = line == null || line.isEmpty() ? "..." : line;
        visibleChars = 0;
        lastCharTime = System.currentTimeMillis();
    }

    private void applyDialogueCameraPreference() {
        if (!Config.FORCE_FIRST_PERSON_IN_DIALOGUE.get() || this.minecraft == null || this.minecraft.options == null || this.restoreCameraOnClose) {
            return;
        }
        this.previousCameraType = this.minecraft.options.getCameraType();
        if (this.previousCameraType != CameraType.FIRST_PERSON) {
            this.minecraft.options.setCameraType(CameraType.FIRST_PERSON);
            this.restoreCameraOnClose = true;
        }
    }

    private void restoreDialogueCameraPreference() {
        if (!this.restoreCameraOnClose || this.minecraft == null || this.minecraft.options == null || this.previousCameraType == null) {
            return;
        }
        this.minecraft.options.setCameraType(this.previousCameraType);
        this.restoreCameraOnClose = false;
        this.previousCameraType = null;
    }

    private class DialogueOption extends Button {
        private final String option;
        public DialogueOption(int x, int y, String option) {
            super(x, y, optionWidth, optionHeight, Component.literal(option),
                    b -> {}, DEFAULT_NARRATION);
            this.option = option;
        }
        @Override
        public void onPress() {
            if (this.option.equals("Flirt") && !VillagerDialogueScreen.this.canUseRomanticInteraction()) {
                return;
            }
            UUID uuid = villager.getUUID();
            Map<String, Integer> cooldowns = VillagerCooldownsStorage.getCooldowns(uuid, VillagerDialogueScreen.this.minecraft.player.getUUID());
            int remaining = cooldowns.getOrDefault(option, 0);
            if (remaining > 0) return;
            VillagerDialogueScreen.this.onOptionClicked(option);
        }
        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            UUID uuid = villager.getUUID();
            Map<String, Integer> cooldowns = VillagerCooldownsStorage.getCooldowns(uuid, VillagerDialogueScreen.this.minecraft.player.getUUID());
            int remaining = cooldowns.getOrDefault(option, 0);
            boolean onCooldown = remaining > 0;
            boolean romanticBlocked = this.option.equals("Flirt") && !VillagerDialogueScreen.this.canUseRomanticInteraction();
            int color = onCooldown || romanticBlocked ? 0x55333333 : (this.isHovered ? 0xAA444444 : 0x88000000);
            graphics.fill(getX(), getY(), getX() + this.width, getY() + this.height, color);
            graphics.drawCenteredString(VillagerDialogueScreen.this.font, getMessage(),
                    getX() + this.width / 2,
                    getY() + (this.height - 8) / 2,
                    romanticBlocked ? 0xAAAAAA : 0xFFFFFF);
            if (onCooldown && mouseX >= getX() && mouseX <= getX() + this.width &&
                mouseY >= getY() && mouseY <= getY() + this.height) {
                graphics.renderTooltip(VillagerDialogueScreen.this.font,
                        Component.literal(remaining + "s"),
                        mouseX, mouseY);
            }
        }
    }

    private class QuestOption extends Button {
        private final String action;
        public QuestOption(int x, int y, String action) {
            super(x, y, optionWidth, optionHeight, Component.literal(action),
                    b -> {}, DEFAULT_NARRATION);
            this.action = action;
        }
        @Override
        public void onPress() {
            onQuestAction(action);
        }
        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            int color = this.isHovered ? 0xAA444444 : 0x88000000;
            graphics.fill(getX(), getY(), getX() + this.width, getY() + this.height, color);
            graphics.drawCenteredString(VillagerDialogueScreen.this.font, getMessage(),
                    getX() + this.width / 2,
                    getY() + (this.height - 8) / 2,
                    0xFFFFFF);
        }
    }

    private class ActionOption extends Button {
        private final String action;
        public ActionOption(int x, int y, String action) {
            super(x, y, optionWidth, optionHeight, Component.literal(action),
                    b -> {}, DEFAULT_NARRATION);
            this.action = action;
        }

        @Override
        public void onPress() {
            VillagerDialogueScreen.this.onActionClicked(action);
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            int color = this.isHovered ? 0xAA444444 : 0x88000000;
            graphics.fill(getX(), getY(), getX() + this.width, getY() + this.height, color);
            graphics.drawCenteredString(VillagerDialogueScreen.this.font, getMessage(),
                    getX() + this.width / 2,
                    getY() + (this.height - 8) / 2,
                    0xFFFFFF);
        }
    }

    private class ActionsButton extends Button {
        public ActionsButton(int x, int y, int width, int height) {
            super(x, y, width, height, Component.literal(getActionsButtonLabel()),
                    b -> setMenuMode(menuMode == MenuMode.ACTIONS ? MenuMode.SOCIAL : MenuMode.ACTIONS),
                    DEFAULT_NARRATION);
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            int color = this.isHovered ? 0xE0E0E0 : 0xFFFFFF;
            graphics.fill(getX(), getY(), getX() + this.width, getY() + this.height,
                    this.isHovered ? 0xAA444444 : 0x88000000);

            graphics.drawCenteredString(VillagerDialogueScreen.this.font, getMessage(),
                    getX() + this.width / 2,
                    getY() + (this.height - 8) / 2,
                    color);
        }
    }

    private class FamilyButton extends Button {
        public FamilyButton(int x, int y, int width, int height) {
            super(x, y, width, height, Component.literal(getFamilyButtonLabel()),
                    b -> setMenuMode(menuMode == MenuMode.FAMILY || menuMode == MenuMode.FAMILY_CONFIRM ? MenuMode.SOCIAL : MenuMode.FAMILY),
                    DEFAULT_NARRATION);
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            int color = this.isHovered ? 0xE0E0E0 : 0xFFFFFF;
            graphics.fill(getX(), getY(), getX() + this.width, getY() + this.height,
                    this.isHovered ? 0xAA444444 : 0x88000000);

            graphics.drawCenteredString(VillagerDialogueScreen.this.font, getMessage(),
                    getX() + this.width / 2,
                    getY() + (this.height - 8) / 2,
                    color);
        }
    }

    private class EditIconButton extends Button {
        public EditIconButton(int x, int y, int width, int height) {
            super(x, y, width, height, Component.empty(), b -> openEditVillagerScreen(), DEFAULT_NARRATION);
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            if (this.isHovered) {
                int glowSize = this.width + 2;
                graphics.setColor(1.0F, 1.0F, 1.0F, 0.10F);
                graphics.blit(EDIT_BUTTON_TEXTURE, getX() - 1, getY() - 1, 0, 0, glowSize, glowSize, glowSize, glowSize);
                graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            }
            graphics.blit(EDIT_BUTTON_TEXTURE, getX(), getY(), 0, 0, this.width, this.height, this.width, this.height);
            if (this.isHovered) {
                graphics.renderTooltip(VillagerDialogueScreen.this.font,
                        Component.translatable("slimpatch.dialogue.edit.tooltip"),
                        mouseX, mouseY);
            }
        }
    }

    private class EquipmentIconButton extends Button {
        public EquipmentIconButton(int x, int y, int width, int height) {
            super(x, y, width, height, Component.empty(), b -> openVillagerEquipmentScreen(), DEFAULT_NARRATION);
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            if (this.isHovered) {
                int glowSize = this.width + 2;
                graphics.setColor(1.0F, 1.0F, 1.0F, 0.10F);
                graphics.blit(EQUIPMENT_BUTTON_TEXTURE, getX() - 1, getY() - 1, 0, 0, glowSize, glowSize, glowSize, glowSize);
                graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            }
            graphics.blit(EQUIPMENT_BUTTON_TEXTURE, getX(), getY(), 0, 0, this.width, this.height, this.width, this.height);
            if (this.isHovered) {
                graphics.renderTooltip(VillagerDialogueScreen.this.font,
                        Component.translatable("slimpatch.dialogue.equipment.tooltip"),
                        mouseX, mouseY);
            }
        }
    }
}
