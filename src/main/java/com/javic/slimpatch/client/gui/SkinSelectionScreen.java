package com.javic.slimpatch.client.gui;

import com.javic.slimpatch.client.ClientSkinTheme;
import com.javic.slimpatch.config.SlimPatchConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class SkinSelectionScreen extends Screen {

    public SkinSelectionScreen() {
        super(Component.literal("Villager Skin Selection"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.addRenderableWidget(Button.builder(Component.literal("Modern"), btn -> chooseTheme("modern"))
                .pos(centerX - 100, centerY).size(90, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Fantasy"), btn -> chooseTheme("fantasy"))
                .pos(centerX + 10, centerY).size(90, 20).build());
    }

    private void chooseTheme(String theme) {
        ClientSkinTheme.setTheme(theme);
        Minecraft mc = Minecraft.getInstance();

        if (mc.getSingleplayerServer() != null) {
            var serverLevel = mc.getSingleplayerServer().overworld();
            if (serverLevel != null) {
                var data = com.javic.slimpatch.data.WorldSkinData.get(serverLevel);
                data.setTheme(theme);
                data.setGuiShown(true);
                data.setDirty();
                SlimPatchConfig.SERVER.skinType.set(theme);
                SlimPatchConfig.SERVER_SPEC.save();
            }
        }

        this.onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics, mouseX, mouseY, partialTicks);
        super.render(graphics, mouseX, mouseY, partialTicks);
        graphics.pose().pushPose();
        graphics.pose().scale(1.7f, 1.7f, 1.7f);
        int scaledX = (int) (this.width / 2 / 1.7f);
        int scaledY = (int) ((this.height / 2 - 100) / 1.7f);
        graphics.drawCenteredString(this.font, "Select Villager Skins", scaledX, scaledY, 0xFFFF55);
        graphics.pose().popPose();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void onClose() {
        String theme = ClientSkinTheme.getTheme();
        if (theme != null && !theme.isEmpty()) super.onClose();
    }
}