package com.javic.slimpatch.client.gui;

import com.javic.slimpatch.client.ClientSkinTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class SkinSelectionScreen extends Screen {

    public SkinSelectionScreen() {
        super(Component.translatable("slimpatch.screen.skin_selection.title"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.addRenderableWidget(Button.builder(Component.translatable("slimpatch.screen.skin_selection.modern"), btn -> chooseTheme("modern"))
                .pos(centerX - 100, centerY).size(90, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("slimpatch.screen.skin_selection.fantasy"), btn -> chooseTheme("fantasy"))
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
        graphics.drawCenteredString(this.font, Component.translatable("slimpatch.screen.skin_selection.heading"), scaledX, scaledY, 0xFFFF55);
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
