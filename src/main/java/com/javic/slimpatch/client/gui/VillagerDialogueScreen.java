package com.javic.slimpatch.client.gui;

import com.javic.slimpatch.dialogue.DialogueManager;
import com.javic.slimpatch.entity.MaleVillagerEntity;
import com.javic.slimpatch.entity.FemaleVillagerEntity;
import com.javic.slimpatch.entity.VillagerPersonality;
import com.javic.slimpatch.client.gui.VillagerPersonalityIcons;
import com.javic.slimpatch.client.gui.VillagerProfessionIcons;
import com.javic.slimpatch.network.RelationshipPacket;
import com.javic.slimpatch.network.VillagerCooldownsStorage;
import com.javic.slimpatch.network.GiftPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VillagerDialogueScreen extends Screen {

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
    private static final ResourceLocation GIFT_ICON_COLOR = ResourceLocation.fromNamespaceAndPath("slimpatch", "textures/gui/gift_icon_color.png");
    private static final ResourceLocation GIFT_ICON_GRAY = ResourceLocation.fromNamespaceAndPath("slimpatch", "textures/gui/gift_icon_gray.png");
    private static final int HEART_SIZE = 16;

    public VillagerDialogueScreen(Villager villager) {
        super(Component.literal("Dialogue"));
        this.villager = villager;
    }

    @Override
    protected void init() {
        super.init();
        fullLine = DialogueManager.getRandomLine("Intro", villager);
        visibleChars = 0;
        lastCharTime = System.currentTimeMillis();
        UUID uuid = villager.getUUID();
        Map<String, Integer> existing = VillagerCooldownsStorage.getCooldowns(uuid);
        if (existing.isEmpty()) {
            Map<String, Integer> initial = new HashMap<>();
            initial.put("Friendly", 0);
            initial.put("Mean", 0);
            initial.put("Joke", 0);
            initial.put("Flirt", 0);
            VillagerCooldownsStorage.setCooldowns(uuid, initial);
        }
        int totalHeight = (optionHeight + optionSpacing) * 4;
        int startY = this.height - totalHeight - 40;
        int centerX = this.width / 2 - optionWidth / 2;
        this.addRenderableWidget(new DialogueOption(centerX, startY, "Friendly"));
        this.addRenderableWidget(new DialogueOption(centerX, startY + (optionHeight + optionSpacing), "Mean"));
        this.addRenderableWidget(new DialogueOption(centerX, startY + 2 * (optionHeight + optionSpacing), "Joke"));
        this.addRenderableWidget(new DialogueOption(centerX, startY + 3 * (optionHeight + optionSpacing), "Flirt"));
    }

    private void onOptionClicked(String option) {
        VillagerPersonality personality = null;
        if (villager instanceof MaleVillagerEntity male) {
            personality = male.getPersonality();
        } else if (villager instanceof FemaleVillagerEntity female) {
            personality = female.getPersonality();
        }
        boolean success = DialogueManager.calculateSuccess(personality, option);
        fullLine = DialogueManager.getRandomLine(option, villager, success);
        visibleChars = 0;
        lastCharTime = System.currentTimeMillis();
        if (this.minecraft != null && this.minecraft.player != null) {
            net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                    new RelationshipPacket(villager.getId(), option, success)
            );
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.fill(0, this.height - 150, this.width, this.height, 0x88000000);
        super.render(graphics, mouseX, mouseY, partialTicks);
        int heartX = 8;
        int topY = 10;
        float relationship = 0.0f;
        if (villager instanceof MaleVillagerEntity male) {
            relationship = male.getRelationship();
        } else if (villager instanceof FemaleVillagerEntity female) {
            relationship = female.getRelationship();
        }
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
        int leftX = 10;
        int textY = topY + HEART_SIZE + 8;
        String villagerName = this.villager.hasCustomName()
                ? this.villager.getCustomName().getString()
                : this.villager.getName().getString();
        drawTextWithIcon(graphics, villagerName, leftX, textY, 0xFFFFFF, null, mouseX, mouseY, null);
        int nameWidth = this.font.width(villagerName);
        int nameX1 = leftX;
        int nameX2 = leftX + nameWidth;
        int nameY1 = textY;
        int nameY2 = nameY1 + this.font.lineHeight;
        if (mouseX >= nameX1 && mouseX <= nameX2 && mouseY >= nameY1 && mouseY <= nameY2) {
            String gender = (this.villager instanceof MaleVillagerEntity) ? "Male" :
                            (this.villager instanceof FemaleVillagerEntity) ? "Female" : "Unknown";
            graphics.renderTooltip(this.font, Component.literal(gender), mouseX, mouseY);
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
            drawTextWithIcon(graphics, personalityName, leftX, textY, 0xFFFFFF, "Personality", mouseX, mouseY, personalityIcon);
        } else {
            drawTextWithIcon(graphics, "DEBUG_NULL", leftX, textY, 0xFF0000, "Personality", mouseX, mouseY, null);
        }
        textY += 20;
        VillagerProfession profession = this.villager.getVillagerData().getProfession();
        String professionName = VillagerProfessionIcons.getName(profession);
        ResourceLocation professionIcon = VillagerProfessionIcons.getIcon(profession);
        drawTextWithIcon(graphics, professionName, leftX, textY, 0xFFFFFF, "Job", mouseX, mouseY, professionIcon);

        textY += 20;
        boolean hasCooldown = VillagerCooldownsStorage.hasGiftCooldown(villager.getUUID());
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
            String tooltip = hasCooldown ? "This villager already gave you a gift today"
                    : canReceiveGift ? "Click to receive a gift"
                    : "Increase relationship to receive a gift";
            graphics.renderTooltip(this.font, Component.literal(tooltip), mouseX, mouseY);
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
            graphics.renderTooltip(this.font, Component.literal("Relationship"), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int leftX = 10;
        int topY = 10;
        int textY = topY + HEART_SIZE + 8 + 20 + 20 + 20;
        int iconX = leftX - 2;
        int iconY = textY;
        int iconSize = 18;

        boolean hasCooldown = VillagerCooldownsStorage.hasGiftCooldown(villager.getUUID());
        float relationship = 0.0f;
        if (villager instanceof MaleVillagerEntity male) relationship = male.getRelationship();
        else if (villager instanceof FemaleVillagerEntity female) relationship = female.getRelationship();
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
                                  String hover, int mouseX, int mouseY, ResourceLocation icon) {
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
                graphics.renderTooltip(this.font, Component.literal(hover), mouseX, mouseY);
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

    @Override
    public void onClose() {
        super.onClose();
        if (this.villager != null) {
            DialogueManager.endDialogue(this.villager);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
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
            UUID uuid = villager.getUUID();
            Map<String, Integer> cooldowns = VillagerCooldownsStorage.getCooldowns(uuid);
            int remaining = cooldowns.getOrDefault(option, 0);
            if (remaining > 0) return;
            onOptionClicked(option);
        }
        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            UUID uuid = villager.getUUID();
            Map<String, Integer> cooldowns = VillagerCooldownsStorage.getCooldowns(uuid);
            int remaining = cooldowns.getOrDefault(option, 0);
            boolean onCooldown = remaining > 0;
            int color = onCooldown ? 0x55333333 : (this.isHovered ? 0xAA444444 : 0x88000000);
            graphics.fill(getX(), getY(), getX() + this.width, getY() + this.height, color);
            graphics.drawCenteredString(VillagerDialogueScreen.this.font, getMessage(),
                    getX() + this.width / 2,
                    getY() + (this.height - 8) / 2,
                    0xFFFFFF);
            if (onCooldown && mouseX >= getX() && mouseX <= getX() + this.width &&
                mouseY >= getY() && mouseY <= getY() + this.height) {
                graphics.renderTooltip(VillagerDialogueScreen.this.font,
                        Component.literal(remaining + "s"),
                        mouseX, mouseY);
            }
        }
    }
}