package com.javic.slimpatch.client.gui;

import com.javic.slimpatch.client.cutscene.FamilyCutsceneController;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class FamilyCutsceneOverlayScreen extends Screen {

    public FamilyCutsceneOverlayScreen() {
        super(Component.empty());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int alpha = FamilyCutsceneController.getOverlayAlpha();
        if (alpha > 0) {
            guiGraphics.fill(0, 0, this.width, this.height, alpha << 24);
        }
        if (FamilyCutsceneController.shouldShowSkipHint()) {
            renderSkipHint(guiGraphics);
        }
    }

    private void renderSkipHint(GuiGraphics guiGraphics) {
        String text = Component.translatable("slimpatch.cutscene.family_skip_hint").getString();
        int x = this.width / 2;
        int y = 12;
        int color = 0xF8E3C0;
        int shadow = 0x000000;

        guiGraphics.drawCenteredString(this.font, text, x + 1, y, shadow);
        guiGraphics.drawCenteredString(this.font, text, x - 1, y, shadow);
        guiGraphics.drawCenteredString(this.font, text, x, y + 1, shadow);
        guiGraphics.drawCenteredString(this.font, text, x, y - 1, shadow);
        guiGraphics.drawCenteredString(this.font, text, x, y, color);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            FamilyCutsceneController.handleSkipRequest();
        }
        return true;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return true;
    }
}
