package com.javic.slimpatch.client.gui;

import com.javic.slimpatch.client.cutscene.WeddingMusicDuckingHandler;
import com.javic.slimpatch.network.ConfirmMarriageProposalPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class MarriageProposalScreen extends Screen {

    private final int villagerEntityId;

    public MarriageProposalScreen(int villagerEntityId, String villagerName) {
        super(Component.translatable("slimpatch.screen.marriage_proposal.title", villagerName));
        this.villagerEntityId = villagerEntityId;
    }

    @Override
    protected void init() {
        super.init();
        WeddingMusicDuckingHandler.startProposalDucking();
        this.addRenderableWidget(Button.builder(Component.translatable("slimpatch.screen.marriage_proposal.yes"), button -> confirmProposal())
                .bounds(this.width / 2 - 105, this.height / 2 + 12, 100, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.translatable("slimpatch.screen.marriage_proposal.no"), button -> onClose())
                .bounds(this.width / 2 + 5, this.height / 2 + 12, 100, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, 0x88000000);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 18, 0xFFFFFF);
    }

    private void confirmProposal() {
        WeddingMusicDuckingHandler.keepDuckedForCutscene();
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(new ConfirmMarriageProposalPacket(this.villagerEntityId));
        onClose();
    }

    @Override
    public void onClose() {
        if (!WeddingMusicDuckingHandler.isCutsceneDucking()) {
            WeddingMusicDuckingHandler.restore();
        }
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
