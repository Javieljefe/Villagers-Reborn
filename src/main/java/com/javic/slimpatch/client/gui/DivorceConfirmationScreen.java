package com.javic.slimpatch.client.gui;

import com.javic.slimpatch.network.ConfirmDivorcePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class DivorceConfirmationScreen extends Screen {

    private final int villagerEntityId;

    public DivorceConfirmationScreen(int villagerEntityId, String villagerName) {
        super(Component.translatable("slimpatch.screen.divorce.title", villagerName));
        this.villagerEntityId = villagerEntityId;
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(Button.builder(Component.translatable("slimpatch.screen.divorce.yes"), button -> confirmDivorce())
                .bounds(this.width / 2 - 105, this.height / 2 + 12, 100, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.translatable("slimpatch.screen.divorce.no"), button -> onClose())
                .bounds(this.width / 2 + 5, this.height / 2 + 12, 100, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, 0x88000000);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 18, 0xFFFFFF);
    }

    private void confirmDivorce() {
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(new ConfirmDivorcePacket(this.villagerEntityId));
        onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
