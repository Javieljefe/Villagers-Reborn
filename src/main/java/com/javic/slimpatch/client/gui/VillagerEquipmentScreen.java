package com.javic.slimpatch.client.gui;

import com.javic.slimpatch.client.key.ModKeyBindings;
import com.javic.slimpatch.entity.CommandableVillager;
import com.javic.slimpatch.entity.VillagerCombatMode;
import com.javic.slimpatch.entity.VillagerFollowMode;
import com.javic.slimpatch.menu.VillagerEquipmentMenu;
import com.javic.slimpatch.network.VillagerArmorVisibilityPacket;
import com.javic.slimpatch.network.VillagerCombatModePacket;
import com.javic.slimpatch.network.VillagerFollowModePacket;
import com.javic.slimpatch.network.VillagerMutePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;

public class VillagerEquipmentScreen extends AbstractContainerScreen<VillagerEquipmentMenu> {

    private static final ResourceLocation CONTAINER_TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");
    private static final ResourceLocation INVENTORY_TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/inventory.png");
    private static final int LEFT_PANEL_WIDTH = 100;
    private static final int SLOT_TEXTURE_U = 7;
    private static final int SLOT_TEXTURE_V = 83;
    private static final int SLOT_SIZE = 18;
    private static final int COMBAT_BUTTON_X_OFFSET = 278;
    private static final int COMBAT_BUTTON_Y = 4;
    private static final int COMBAT_BUTTON_WIDTH = 14;
    private static final int COMBAT_BUTTON_HEIGHT = 14;
    private static final int COMBAT_BUTTON_SPACING = 15;
    private static final int FOLLOW_BUTTON_GROUP_GAP = 6;
    private static final int ARMOR_VISIBILITY_BUTTON_X_OFFSET = -16;
    private static final int ARMOR_VISIBILITY_BUTTON_Y = COMBAT_BUTTON_Y;
    private static final int MUTE_BUTTON_X_OFFSET = ARMOR_VISIBILITY_BUTTON_X_OFFSET;
    private static final int MUTE_BUTTON_Y = ARMOR_VISIBILITY_BUTTON_Y + COMBAT_BUTTON_SPACING;
    private static LivingEntity previewVillager;
    private final LivingEntity villagerPreview;

    public VillagerEquipmentScreen(VillagerEquipmentMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.villagerPreview = previewVillager;
        previewVillager = null;
        this.imageWidth = 276;
        this.imageHeight = 168;
        this.inventoryLabelX = 108;
        this.inventoryLabelY = 74;
        this.titleLabelX = 108;
        this.titleLabelY = 6;
    }

    public static void setPreviewVillager(LivingEntity villager) {
        previewVillager = villager;
    }

    @Override
    protected void init() {
        super.init();
        int buttonX = this.leftPos + COMBAT_BUTTON_X_OFFSET;
        this.addRenderableWidget(new CombatModeButton(buttonX, this.topPos + COMBAT_BUTTON_Y, VillagerCombatMode.AGGRESSIVE, "A", Component.translatable("slimpatch.screen.villager_equipment.combat.aggressive.tooltip")));
        this.addRenderableWidget(new CombatModeButton(buttonX, this.topPos + COMBAT_BUTTON_Y + COMBAT_BUTTON_SPACING, VillagerCombatMode.DEFENSIVE, "D", Component.translatable("slimpatch.screen.villager_equipment.combat.defensive.tooltip")));
        this.addRenderableWidget(new CombatModeButton(buttonX, this.topPos + COMBAT_BUTTON_Y + COMBAT_BUTTON_SPACING * 2, VillagerCombatMode.PASSIVE, "P", Component.translatable("slimpatch.screen.villager_equipment.combat.passive.tooltip")));
        int followButtonY = this.topPos + COMBAT_BUTTON_Y + COMBAT_BUTTON_SPACING * 3 + FOLLOW_BUTTON_GROUP_GAP;
        this.addRenderableWidget(new FollowModeButton(buttonX, followButtonY, VillagerFollowMode.CLOSE, "C", Component.translatable("slimpatch.screen.villager_equipment.follow.close.tooltip")));
        this.addRenderableWidget(new FollowModeButton(buttonX, followButtonY + COMBAT_BUTTON_SPACING, VillagerFollowMode.RELAXED, "R", Component.translatable("slimpatch.screen.villager_equipment.follow.relaxed.tooltip")));
        int armorVisibilityButtonX = this.leftPos + ARMOR_VISIBILITY_BUTTON_X_OFFSET;
        this.addRenderableWidget(new ArmorVisibilityButton(armorVisibilityButtonX, this.topPos + ARMOR_VISIBILITY_BUTTON_Y));
        this.addRenderableWidget(new MuteButton(this.leftPos + MUTE_BUTTON_X_OFFSET, this.topPos + MUTE_BUTTON_Y));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        graphics.blit(INVENTORY_TEXTURE, x, y, 0, 0, 97, 83, 256, 256);
        graphics.blit(CONTAINER_TEXTURE, x + LEFT_PANEL_WIDTH, y, 0, 0, 176, 71, 256, 256);
        graphics.blit(CONTAINER_TEXTURE, x + LEFT_PANEL_WIDTH, y + 71, 0, 126, 176, 96, 256, 256);
        this.renderEquipmentSlotBackground(graphics, x + VillagerEquipmentMenu.MAINHAND_SLOT_X, y + VillagerEquipmentMenu.MAINHAND_SLOT_Y);

        if (this.villagerPreview != null) {
            int renderX1 = x + 26;
            int renderY1 = y + 8;
            int renderX2 = x + 75;
            int renderY2 = y + 78;
            float centerX = (renderX1 + renderX2) / 2.0F;
            float centerY = (renderY1 + renderY2) / 2.0F;
            InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, renderX1, renderY1, renderX2, renderY2, 34, 0.0F, centerX, centerY, this.villagerPreview);
        }
    }

    @Override
    public void renderTransparentBackground(GuiGraphics graphics) {
        graphics.fill(0, 0, this.width, this.height, 0x78000000);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics, mouseX, mouseY, partialTicks);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
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
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);
        String hpText = this.getHealthText();
        graphics.drawString(this.font, hpText, LEFT_PANEL_WIDTH + 176 - this.font.width(hpText) - 20, 6, 0x404040, false);
    }

    private void renderEquipmentSlotBackground(GuiGraphics graphics, int x, int y) {
        graphics.blit(INVENTORY_TEXTURE, x, y, SLOT_TEXTURE_U, SLOT_TEXTURE_V, SLOT_SIZE, SLOT_SIZE, 256, 256);
    }

    private VillagerCombatMode getCombatMode() {
        if (this.villagerPreview instanceof CommandableVillager commandableVillager) {
            return commandableVillager.getCombatMode();
        }
        return VillagerCombatMode.DEFENSIVE;
    }

    private String getHealthText() {
        if (this.villagerPreview == null) {
            return Component.translatable("slimpatch.screen.villager_equipment.hp", 0, 0).getString();
        }
        return Component.translatable("slimpatch.screen.villager_equipment.hp", this.formatHealthValue(this.villagerPreview.getHealth()), this.formatHealthValue(this.villagerPreview.getMaxHealth())).getString();
    }

    private VillagerFollowMode getFollowMode() {
        if (this.villagerPreview instanceof CommandableVillager commandableVillager) {
            return commandableVillager.getFollowMode();
        }
        return VillagerFollowMode.CLOSE;
    }

    private String formatHealthValue(float value) {
        if (Math.abs(value - Math.round(value)) < 0.01F) {
            return Integer.toString(Math.round(value));
        }
        return String.format(java.util.Locale.ROOT, "%.1f", value);
    }

    private boolean isArmorHidden() {
        if (this.villagerPreview instanceof CommandableVillager commandableVillager) {
            return commandableVillager.isArmorHidden();
        }
        return false;
    }

    private boolean isMuted() {
        if (this.villagerPreview instanceof CommandableVillager commandableVillager) {
            return commandableVillager.isMuted();
        }
        return false;
    }

    private void sendCombatMode(VillagerCombatMode combatMode) {
        if (this.villagerPreview == null || this.minecraft == null || this.minecraft.player == null) {
            return;
        }
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(new VillagerCombatModePacket(this.villagerPreview.getId(), combatMode));
    }

    private void sendFollowMode(VillagerFollowMode followMode) {
        if (this.villagerPreview == null || this.minecraft == null || this.minecraft.player == null) {
            return;
        }
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(new VillagerFollowModePacket(this.villagerPreview.getId(), followMode));
    }

    private void sendArmorVisibility(boolean hidden) {
        if (this.villagerPreview == null || this.minecraft == null || this.minecraft.player == null) {
            return;
        }
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(new VillagerArmorVisibilityPacket(this.villagerPreview.getId(), hidden));
    }

    private void sendMute(boolean muted) {
        if (this.villagerPreview == null || this.minecraft == null || this.minecraft.player == null) {
            return;
        }
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(new VillagerMutePacket(this.villagerPreview.getId(), muted));
    }

    private class CombatModeButton extends Button {
        private final VillagerCombatMode combatMode;
        private final Component tooltip;

        private CombatModeButton(int x, int y, VillagerCombatMode combatMode, String label, Component tooltip) {
            super(x, y, COMBAT_BUTTON_WIDTH, COMBAT_BUTTON_HEIGHT, Component.literal(label), b -> sendCombatMode(combatMode), DEFAULT_NARRATION);
            this.combatMode = combatMode;
            this.tooltip = tooltip;
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            boolean activeMode = VillagerEquipmentScreen.this.getCombatMode() == this.combatMode;
            int background = activeMode ? 0xFF6B8E23 : (this.isHovered ? 0xFF686868 : 0xFF4A4A4A);
            int topBorder = activeMode ? 0xFFF0F0A0 : 0xFFB8B8B8;
            int bottomBorder = activeMode ? 0xFF2F4F12 : 0xFF202020;
            int textColor = activeMode ? 0xFFF8F8D8 : 0xFFFFFFFF;

            graphics.fill(getX(), getY(), getX() + this.width, getY() + this.height, background);
            graphics.fill(getX(), getY(), getX() + this.width, getY() + 1, topBorder);
            graphics.fill(getX(), getY(), getX() + 1, getY() + this.height, topBorder);
            graphics.fill(getX(), getY() + this.height - 1, getX() + this.width, getY() + this.height, bottomBorder);
            graphics.fill(getX() + this.width - 1, getY(), getX() + this.width, getY() + this.height, bottomBorder);
            graphics.drawCenteredString(VillagerEquipmentScreen.this.font, getMessage(), getX() + this.width / 2, getY() + 3, textColor);

            if (this.isHovered) {
                graphics.renderTooltip(VillagerEquipmentScreen.this.font, this.tooltip, mouseX, mouseY);
            }
        }
    }

    private class FollowModeButton extends Button {
        private final VillagerFollowMode followMode;
        private final Component tooltip;

        private FollowModeButton(int x, int y, VillagerFollowMode followMode, String label, Component tooltip) {
            super(x, y, COMBAT_BUTTON_WIDTH, COMBAT_BUTTON_HEIGHT, Component.literal(label), b -> sendFollowMode(followMode), DEFAULT_NARRATION);
            this.followMode = followMode;
            this.tooltip = tooltip;
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            boolean activeMode = VillagerEquipmentScreen.this.getFollowMode() == this.followMode;
            int background = activeMode ? 0xFF6B8E23 : (this.isHovered ? 0xFF686868 : 0xFF4A4A4A);
            int topBorder = activeMode ? 0xFFF0F0A0 : 0xFFB8B8B8;
            int bottomBorder = activeMode ? 0xFF2F4F12 : 0xFF202020;
            int textColor = activeMode ? 0xFFF8F8D8 : 0xFFFFFFFF;

            graphics.fill(getX(), getY(), getX() + this.width, getY() + this.height, background);
            graphics.fill(getX(), getY(), getX() + this.width, getY() + 1, topBorder);
            graphics.fill(getX(), getY(), getX() + 1, getY() + this.height, topBorder);
            graphics.fill(getX(), getY() + this.height - 1, getX() + this.width, getY() + this.height, bottomBorder);
            graphics.fill(getX() + this.width - 1, getY(), getX() + this.width, getY() + this.height, bottomBorder);
            graphics.drawCenteredString(VillagerEquipmentScreen.this.font, getMessage(), getX() + this.width / 2, getY() + 3, textColor);

            if (this.isHovered) {
                graphics.renderTooltip(VillagerEquipmentScreen.this.font, this.tooltip, mouseX, mouseY);
            }
        }
    }

    private class ArmorVisibilityButton extends Button {
        private ArmorVisibilityButton(int x, int y) {
            super(x, y, COMBAT_BUTTON_WIDTH, COMBAT_BUTTON_HEIGHT, Component.literal("H"), b -> sendArmorVisibility(!VillagerEquipmentScreen.this.isArmorHidden()), DEFAULT_NARRATION);
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            boolean armorHidden = VillagerEquipmentScreen.this.isArmorHidden();
            int background = armorHidden ? 0xFF6B8E23 : (this.isHovered ? 0xFF686868 : 0xFF4A4A4A);
            int topBorder = armorHidden ? 0xFFF0F0A0 : 0xFFB8B8B8;
            int bottomBorder = armorHidden ? 0xFF2F4F12 : 0xFF202020;
            int textColor = armorHidden ? 0xFFF8F8D8 : 0xFFFFFFFF;
            Component tooltip = Component.translatable(armorHidden
                    ? "slimpatch.screen.villager_equipment.armor.toggle.tooltip.hidden"
                    : "slimpatch.screen.villager_equipment.armor.toggle.tooltip.visible");

            graphics.fill(getX(), getY(), getX() + this.width, getY() + this.height, background);
            graphics.fill(getX(), getY(), getX() + this.width, getY() + 1, topBorder);
            graphics.fill(getX(), getY(), getX() + 1, getY() + this.height, topBorder);
            graphics.fill(getX(), getY() + this.height - 1, getX() + this.width, getY() + this.height, bottomBorder);
            graphics.fill(getX() + this.width - 1, getY(), getX() + this.width, getY() + this.height, bottomBorder);
            graphics.drawCenteredString(VillagerEquipmentScreen.this.font, getMessage(), getX() + this.width / 2, getY() + 3, textColor);

            if (this.isHovered) {
                graphics.renderTooltip(VillagerEquipmentScreen.this.font, tooltip, mouseX, mouseY);
            }
        }
    }

    private class MuteButton extends Button {
        private MuteButton(int x, int y) {
            super(x, y, COMBAT_BUTTON_WIDTH, COMBAT_BUTTON_HEIGHT, Component.literal("♪"), b -> sendMute(!VillagerEquipmentScreen.this.isMuted()), DEFAULT_NARRATION);
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            boolean muted = VillagerEquipmentScreen.this.isMuted();
            int background = muted ? (this.isHovered ? 0xFF686868 : 0xFF4A4A4A) : 0xFF6B8E23;
            int topBorder = muted ? 0xFFB8B8B8 : 0xFFF0F0A0;
            int bottomBorder = muted ? 0xFF202020 : 0xFF2F4F12;
            int textColor = muted ? 0xFFFFFFFF : 0xFFF8F8D8;
            Component tooltip = Component.translatable(muted
                    ? "slimpatch.screen.villager_equipment.mute.tooltip.muted"
                    : "slimpatch.screen.villager_equipment.mute.tooltip.enabled");

            graphics.fill(getX(), getY(), getX() + this.width, getY() + this.height, background);
            graphics.fill(getX(), getY(), getX() + this.width, getY() + 1, topBorder);
            graphics.fill(getX(), getY(), getX() + 1, getY() + this.height, topBorder);
            graphics.fill(getX(), getY() + this.height - 1, getX() + this.width, getY() + this.height, bottomBorder);
            graphics.fill(getX() + this.width - 1, getY(), getX() + this.width, getY() + this.height, bottomBorder);
            graphics.drawCenteredString(VillagerEquipmentScreen.this.font, getMessage(), getX() + this.width / 2, getY() + 3, textColor);

            if (this.isHovered) {
                graphics.renderTooltip(VillagerEquipmentScreen.this.font, tooltip, mouseX, mouseY);
            }
        }
    }
}
