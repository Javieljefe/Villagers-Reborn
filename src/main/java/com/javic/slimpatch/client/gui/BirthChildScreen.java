package com.javic.slimpatch.client.gui;

import com.javic.slimpatch.client.ClientCustomSkinHelper;
import com.javic.slimpatch.ModEntities;
import com.javic.slimpatch.entity.BirthScreenData;
import com.javic.slimpatch.entity.FemaleVillagerEntity;
import com.javic.slimpatch.entity.MaleVillagerEntity;
import com.javic.slimpatch.entity.VillagerAgeStage;
import com.javic.slimpatch.entity.VillagerPersonality;
import com.javic.slimpatch.network.ConfirmBirthPacket;
import com.javic.slimpatch.util.MultiplayerSkinStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class BirthChildScreen extends Screen {

    private static final int SKIN_COLUMNS = 4;
    private static final int SKIN_BUTTON_SIZE = 40;
    private static final int SKIN_BUTTON_SPACING = 4;
    private static final int SKIN_VISIBLE_ROWS = 3;

    private final int spouseEntityId;
    private final String villagerName;
    private final String childGender;
    private final String skinType;
    private final List<Integer> skinIds;
    private final List<SkinOptionButton> skinButtons = new ArrayList<>();
    private final String pendingSkinKey;
    private EditBox nameField;
    private EditBox customSkinField;
    private Button confirmButton;
    private Button openSkinsFolderButton;
    private CustomSkinOptionButton customSkinButton;
    private LivingEntity previewEntity;
    private VillagerPersonality selectedPersonality;
    private int selectedSkinId;
    private boolean useCustomSkin;
    private String customSkinInput = "";
    private byte[] customSkinPngData = new byte[0];
    private String customSkinPreviewPath = "";
    private int skinScrollRow;

    public BirthChildScreen(int spouseEntityId, String villagerName, String childGender, String initialPersonality, String skinType, int initialSkinId) {
        super(Component.translatable("slimpatch.screen.birth.title"));
        this.spouseEntityId = spouseEntityId;
        this.villagerName = villagerName;
        this.childGender = BirthScreenData.normalizeGender(childGender);
        this.skinType = BirthScreenData.normalizeSkinType(skinType);
        this.skinIds = BirthScreenData.getValidSkinIds(this.childGender, this.skinType);
        this.pendingSkinKey = ClientCustomSkinHelper.getPendingBirthSkinKey(spouseEntityId);
        this.selectedPersonality = BirthScreenData.sanitizeBirthPersonality(parsePersonality(initialPersonality));
        this.selectedSkinId = BirthScreenData.isValidSkin(this.childGender, this.skinType, initialSkinId)
                ? initialSkinId
                : BirthScreenData.getDefaultSkinId(this.childGender, this.skinType);
    }

    @Override
    protected void init() {
        super.init();
        this.previewEntity = createPreviewEntity();

        int leftColumnX = this.width / 2 - 150;
        int rightColumnX = this.width / 2 + 16;
        int topY = 38;

        this.nameField = new EditBox(this.font, rightColumnX, topY + 202, 180, 20, Component.translatable("slimpatch.screen.birth.name"));
        this.nameField.setMaxLength(BirthScreenData.MAX_CHILD_NAME_LENGTH);
        this.nameField.setResponder(value -> updatePreviewName());
        this.addRenderableWidget(this.nameField);
        this.setInitialFocus(this.nameField);

        this.customSkinField = new EditBox(this.font, rightColumnX, topY + 300, 180, 20, Component.translatable("slimpatch.screen.birth.skindex_url"));
        this.customSkinField.setMaxLength(500);
        this.customSkinField.setResponder(value -> this.customSkinInput = value.trim());
        this.addRenderableWidget(this.customSkinField);

        this.skinButtons.clear();
        for (int i = 0; i < this.skinIds.size(); i++) {
            int skinId = this.skinIds.get(i);
            SkinOptionButton button = new SkinOptionButton(0, 0, skinId);
            this.skinButtons.add(button);
            this.addRenderableWidget(button);
        }

        this.customSkinButton = this.addRenderableWidget(new CustomSkinOptionButton(0, 0));
        updateSkinButtonPositions();

        this.addRenderableWidget(Button.builder(Component.literal("<"), button -> cyclePersonality(-1))
                .bounds(rightColumnX, topY + 252, 20, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.literal(">"), button -> cyclePersonality(1))
                .bounds(rightColumnX + 160, topY + 252, 20, 20)
                .build());

        this.openSkinsFolderButton = this.addRenderableWidget(Button.builder(Component.translatable("slimpatch.screen.birth.open_skins_folder"), button -> openSkinsFolder())
                .bounds(rightColumnX, topY + 326, 180, 20)
                .build());

        this.confirmButton = this.addRenderableWidget(Button.builder(Component.translatable("slimpatch.screen.birth.confirm"), button -> confirmBirth())
                .bounds(rightColumnX, topY + 352, 180, 20)
                .build());

        updatePreviewEntity();
        updateConfirmButton();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void onClose() {
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.minecraft != null && this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            return true;
        }
        if (keyCode == 256) {
            return true;
        }
        if ((keyCode == 257 || keyCode == 335) && this.customSkinField != null && this.customSkinField.isFocused()) {
            applyCustomSkinFromField();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int rightColumnX = this.width / 2 + 16;
        int topY = 38;
        int gridX1 = rightColumnX;
        int gridY1 = topY + 16;
        int gridX2 = gridX1 + SKIN_COLUMNS * SKIN_BUTTON_SIZE + (SKIN_COLUMNS - 1) * SKIN_BUTTON_SPACING;
        int gridY2 = gridY1 + SKIN_VISIBLE_ROWS * SKIN_BUTTON_SIZE + (SKIN_VISIBLE_ROWS - 1) * SKIN_BUTTON_SPACING;
        if (mouseX >= gridX1 && mouseX <= gridX2 + 8 && mouseY >= gridY1 && mouseY <= gridY2) {
            this.skinScrollRow = Math.max(0, Math.min(getMaxScrollRow(), this.skinScrollRow - (int) Math.signum(scrollY)));
            updateSkinButtonPositions();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0xC0101010);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        int leftColumnX = this.width / 2 - 150;
        int rightColumnX = this.width / 2 + 16;
        int topY = 38;

        graphics.fill(leftColumnX - 12, topY - 6, leftColumnX + 152, topY + 190, 0x66000000);
        graphics.fill(rightColumnX - 8, topY - 6, rightColumnX + 188, topY + 386, 0x66000000);

        graphics.drawCenteredString(this.font, this.title, this.width / 2, 16, 0xFFFFFF);
        graphics.drawString(this.font, Component.translatable("slimpatch.screen.birth.appearance"), rightColumnX, topY, 0xFFFFFF);
        graphics.drawString(this.font, Component.translatable("slimpatch.screen.birth.name"), rightColumnX, topY + 190, 0xFFFFFF);
        graphics.drawString(this.font, Component.translatable("slimpatch.screen.birth.personality"), rightColumnX, topY + 228, 0xFFFFFF);
        graphics.drawString(this.font, Component.translatable("slimpatch.screen.birth.skindex_url"), rightColumnX, topY + 288, 0xFFFFFF);
        graphics.drawCenteredString(this.font, BirthScreenData.getPersonalityName(this.selectedPersonality), rightColumnX + 90, topY + 262, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.translatable("female".equals(this.childGender) ? "slimpatch.dialogue.gender.female" : "slimpatch.dialogue.gender.male"), leftColumnX + 70, topY + 152, 0xAAAAAA);
        renderSkinScrollBar(graphics, rightColumnX, topY);

        if (this.previewEntity != null) {
            renderEntityPreview(graphics, leftColumnX, topY + 4, leftColumnX + 140, topY + 170, 60, this.previewEntity, leftColumnX + 70.0F, topY + 92.0F);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    public void handleConfirmResult(boolean success, String translationKey, String translationArgument) {
        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.displayClientMessage(translationArgument == null || translationArgument.isEmpty()
                    ? Component.translatable(translationKey)
                    : Component.translatable(translationKey, translationArgument), true);
        }
        if (success && this.minecraft != null) {
            this.minecraft.setScreen(null);
        }
    }

    private void confirmBirth() {
        if (this.minecraft == null || this.minecraft.player == null || !isSelectionValid()) {
            return;
        }
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(new ConfirmBirthPacket(
                this.spouseEntityId,
                this.nameField.getValue(),
                this.selectedSkinId,
                this.selectedPersonality.name(),
                this.childGender,
                this.useCustomSkin,
                this.customSkinInput,
                this.customSkinPngData
        ));
    }

    private void cyclePersonality(int direction) {
        this.selectedPersonality = BirthScreenData.cyclePersonality(this.selectedPersonality, direction);
        updatePreviewEntity();
        updateConfirmButton();
    }

    private void updatePreviewName() {
        updatePreviewEntity();
        updateConfirmButton();
    }

    private void updatePreviewEntity() {
        if (this.previewEntity instanceof MaleVillagerEntity male) {
            applyPreviewSkin(male);
            male.setAgeStage(VillagerAgeStage.TODDLER);
            male.setPersonality(this.selectedPersonality);
            if (BirthScreenData.isValidChildName(this.nameField != null ? this.nameField.getValue() : "")) {
                male.setCustomName(Component.literal(BirthScreenData.sanitizeChildName(this.nameField.getValue())));
            } else {
                male.setCustomName(Component.literal("?"));
            }
            return;
        }
        if (this.previewEntity instanceof FemaleVillagerEntity female) {
            applyPreviewSkin(female);
            female.setAgeStage(VillagerAgeStage.TODDLER);
            female.setPersonality(this.selectedPersonality);
            if (BirthScreenData.isValidChildName(this.nameField != null ? this.nameField.getValue() : "")) {
                female.setCustomName(Component.literal(BirthScreenData.sanitizeChildName(this.nameField.getValue())));
            } else {
                female.setCustomName(Component.literal("?"));
            }
        }
    }

    private void updateConfirmButton() {
        if (this.confirmButton != null) {
            this.confirmButton.active = isSelectionValid();
        }
    }

    private void updateSkinButtonPositions() {
        int rightColumnX = this.width / 2 + 16;
        int topY = 38;
        for (int i = 0; i < this.skinButtons.size(); i++) {
            SkinOptionButton button = this.skinButtons.get(i);
            int row = i / SKIN_COLUMNS;
            int column = i % SKIN_COLUMNS;
            int visibleRow = row - this.skinScrollRow;
            button.setX(rightColumnX + column * (SKIN_BUTTON_SIZE + SKIN_BUTTON_SPACING));
            button.setY(topY + 16 + visibleRow * (SKIN_BUTTON_SIZE + SKIN_BUTTON_SPACING));
            button.visible = visibleRow >= 0 && visibleRow < SKIN_VISIBLE_ROWS;
            button.active = button.visible;
        }
        updateCustomSkinButtonPosition(rightColumnX, topY);
    }

    private void updateCustomSkinButtonPosition(int rightColumnX, int topY) {
        if (this.customSkinButton == null) {
            return;
        }
        if (!this.useCustomSkin || MultiplayerSkinStorage.validatePng(this.customSkinPngData) != null) {
            this.customSkinButton.visible = false;
            this.customSkinButton.active = false;
            return;
        }
        int index = this.skinButtons.size();
        int row = index / SKIN_COLUMNS;
        int column = index % SKIN_COLUMNS;
        int visibleRow = row - this.skinScrollRow;
        this.customSkinButton.setX(rightColumnX + column * (SKIN_BUTTON_SIZE + SKIN_BUTTON_SPACING));
        this.customSkinButton.setY(topY + 16 + visibleRow * (SKIN_BUTTON_SIZE + SKIN_BUTTON_SPACING));
        this.customSkinButton.visible = visibleRow >= 0 && visibleRow < SKIN_VISIBLE_ROWS;
        this.customSkinButton.active = this.customSkinButton.visible;
    }

    private int getTotalSkinOptions() {
        return this.skinButtons.size() + (this.useCustomSkin && MultiplayerSkinStorage.validatePng(this.customSkinPngData) == null ? 1 : 0);
    }

    private int getMaxScrollRow() {
        return Math.max(0, (int) Math.ceil(getTotalSkinOptions() / (double) SKIN_COLUMNS) - SKIN_VISIBLE_ROWS);
    }

    private void scrollToCustomSkin() {
        this.skinScrollRow = getMaxScrollRow();
        updateSkinButtonPositions();
    }

    private void renderSkinScrollBar(GuiGraphics graphics, int rightColumnX, int topY) {
        int totalRows = (int) Math.ceil(getTotalSkinOptions() / (double) SKIN_COLUMNS);
        if (totalRows <= SKIN_VISIBLE_ROWS) {
            return;
        }
        int barX1 = rightColumnX + SKIN_COLUMNS * SKIN_BUTTON_SIZE + (SKIN_COLUMNS - 1) * SKIN_BUTTON_SPACING + 4;
        int barY1 = topY + 16;
        int barY2 = barY1 + SKIN_VISIBLE_ROWS * SKIN_BUTTON_SIZE + (SKIN_VISIBLE_ROWS - 1) * SKIN_BUTTON_SPACING;
        graphics.fill(barX1, barY1, barX1 + 4, barY2, 0x66444444);
        int thumbHeight = Math.max(12, (barY2 - barY1) * SKIN_VISIBLE_ROWS / totalRows);
        int maxScrollRow = totalRows - SKIN_VISIBLE_ROWS;
        int travel = (barY2 - barY1) - thumbHeight;
        int thumbY = barY1 + (maxScrollRow == 0 ? 0 : travel * this.skinScrollRow / maxScrollRow);
        graphics.fill(barX1, thumbY, barX1 + 4, thumbY + thumbHeight, 0xFFB0B0B0);
    }

    private boolean isSelectionValid() {
        return BirthScreenData.isValidChildName(this.nameField != null ? this.nameField.getValue() : "")
                && (this.useCustomSkin ? MultiplayerSkinStorage.validatePng(this.customSkinPngData) == null : BirthScreenData.isValidSkin(this.childGender, this.skinType, this.selectedSkinId))
                && BirthScreenData.isValidPersonality(this.selectedPersonality);
    }

    private void applyPreviewSkin(MaleVillagerEntity male) {
        if (this.useCustomSkin && MultiplayerSkinStorage.validatePng(this.customSkinPngData) == null) {
            male.setSavedSkinInput(this.customSkinInput);
            male.setCustomSkinPath(this.customSkinPreviewPath);
        } else {
            male.setSavedSkinInput("");
            String curatedSkinPath = BirthScreenData.getCuratedSkinResourcePath(this.childGender, this.skinType, this.selectedSkinId);
            male.setCustomSkinPath(curatedSkinPath == null ? "" : curatedSkinPath);
            male.setSkinIndex(this.selectedSkinId);
        }
    }

    private void applyPreviewSkin(FemaleVillagerEntity female) {
        if (this.useCustomSkin && MultiplayerSkinStorage.validatePng(this.customSkinPngData) == null) {
            female.setSavedSkinInput(this.customSkinInput);
            female.setCustomSkinPath(this.customSkinPreviewPath);
        } else {
            female.setSavedSkinInput("");
            String curatedSkinPath = BirthScreenData.getCuratedSkinResourcePath(this.childGender, this.skinType, this.selectedSkinId);
            female.setCustomSkinPath(curatedSkinPath == null ? "" : curatedSkinPath);
            female.setSkinIndex(this.selectedSkinId);
        }
    }

    private void applyCustomSkinFromField() {
        applyCustomSkinInput(this.customSkinField != null ? this.customSkinField.getValue() : "");
    }

    private void chooseCustomSkinFile() {
        openSkinsFolder();
    }

    private void applyCustomSkinInput(String skinInput) {
        String value = skinInput == null ? "" : skinInput.trim();
        if (value.isEmpty()) {
            this.useCustomSkin = false;
            this.customSkinInput = "";
            this.customSkinPngData = new byte[0];
            this.customSkinPreviewPath = "";
            updatePreviewEntity();
            updateSkinButtonPositions();
            updateConfirmButton();
            return;
        }
        if (!ClientCustomSkinHelper.allowLocalCustomSkins() && !ClientCustomSkinHelper.canUploadMultiplayerCustomSkin()) {
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.displayClientMessage(Component.translatable("slimpatch.message.custom_skin_disabled"), true);
            }
            return;
        }

        String previewPath = ClientCustomSkinHelper.resolvePendingBirthSkinPath(this.pendingSkinKey, value);
        byte[] pngData = readCustomSkinBytes(value);
        if (pngData == null || MultiplayerSkinStorage.validatePng(pngData) != null) {
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.displayClientMessage(Component.translatable("slimpatch.message.birth_invalid_skin"), true);
            }
            return;
        }

        this.useCustomSkin = true;
        this.customSkinInput = value;
        this.customSkinPngData = pngData;
        this.customSkinPreviewPath = previewPath;
        updatePreviewEntity();
        scrollToCustomSkin();
        updateConfirmButton();
    }

    private byte[] readCustomSkinBytes(String skinInput) {
        return ClientCustomSkinHelper.readPendingBirthSkinBytes(this.pendingSkinKey, skinInput);
    }

    private void openSkinsFolder() {
        ClientCustomSkinHelper.openPendingBirthSkinFolder(this.pendingSkinKey);
    }

    private LivingEntity createPreviewEntity() {
        Minecraft minecraft = Minecraft.getInstance();
        Level level = minecraft.level;
        if (level == null) {
            return null;
        }
        if ("female".equals(this.childGender)) {
            FemaleVillagerEntity villager = ModEntities.FEMALE_VILLAGER.get().create(level);
            if (villager != null) {
                villager.setNoAi(true);
                villager.setSilent(true);
            }
            return villager;
        }
        MaleVillagerEntity villager = ModEntities.MALE_VILLAGER.get().create(level);
        if (villager != null) {
            villager.setNoAi(true);
            villager.setSilent(true);
        }
        return villager;
    }

    private VillagerPersonality parsePersonality(String personalityName) {
        try {
            return VillagerPersonality.valueOf(personalityName);
        } catch (IllegalArgumentException e) {
            return VillagerPersonality.FRIENDLY;
        }
    }

    private ResourceLocation getSkinTexture(int skinId) {
        String path = "slimpatch:textures/entity/custom_villager/";
        if ("fantasy".equals(this.skinType)) {
            path += "fantasy/";
        }
        path += this.childGender + "/skin_" + skinId + ".png";
        ResourceLocation texture = ResourceLocation.tryParse(path);
        return texture != null ? texture : ResourceLocation.fromNamespaceAndPath("slimpatch", "textures/entity/custom_villager/" + this.childGender + "/skin_1.png");
    }

    private void renderEntityPreview(GuiGraphics graphics, int x1, int y1, int x2, int y2, int scale, LivingEntity entity, float lookX, float lookY) {
        if (entity == null) {
            return;
        }
        float bodyRot = entity.yBodyRot;
        float yRot = entity.getYRot();
        float xRot = entity.getXRot();
        float yHeadRotO = entity.yHeadRotO;
        float yHeadRot = entity.yHeadRot;
        entity.yBodyRot = 180.0F;
        entity.setYRot(180.0F);
        entity.setXRot(0.0F);
        entity.yHeadRot = entity.getYRot();
        entity.yHeadRotO = entity.getYRot();
        InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, x1, y1, x2, y2, scale, 0.0F, lookX, lookY, entity);
        entity.yBodyRot = bodyRot;
        entity.setYRot(yRot);
        entity.setXRot(xRot);
        entity.yHeadRotO = yHeadRotO;
        entity.yHeadRot = yHeadRot;
    }

    private class SkinOptionButton extends Button {
        private final int skinId;

        protected SkinOptionButton(int x, int y, int skinId) {
            super(x, y, 40, 40, Component.empty(), button -> {
                BirthChildScreen.this.useCustomSkin = false;
                BirthChildScreen.this.customSkinInput = BirthChildScreen.this.customSkinField != null ? BirthChildScreen.this.customSkinField.getValue().trim() : "";
                BirthChildScreen.this.customSkinPngData = new byte[0];
                BirthChildScreen.this.customSkinPreviewPath = "";
                BirthChildScreen.this.selectedSkinId = skinId;
                BirthChildScreen.this.updatePreviewEntity();
                BirthChildScreen.this.updateSkinButtonPositions();
                BirthChildScreen.this.updateConfirmButton();
            }, DEFAULT_NARRATION);
            this.skinId = skinId;
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            boolean selected = !BirthChildScreen.this.useCustomSkin && BirthChildScreen.this.selectedSkinId == this.skinId;
            graphics.fill(getX(), getY(), getX() + this.width, getY() + this.height, selected ? 0xFFB09040 : (this.isHovered ? 0xAA444444 : 0x88000000));
            LivingEntity preview = BirthChildScreen.this.createPreviewEntity();
            if (preview instanceof MaleVillagerEntity male) {
                String curatedSkinPath = BirthScreenData.getCuratedSkinResourcePath(BirthChildScreen.this.childGender, BirthChildScreen.this.skinType, this.skinId);
                male.setCustomSkinPath(curatedSkinPath == null ? "" : curatedSkinPath);
                male.setSkinIndex(this.skinId);
                male.setAgeStage(VillagerAgeStage.TODDLER);
                male.setPersonality(BirthChildScreen.this.selectedPersonality);
                renderEntityPreview(graphics, getX() + 2, getY() + 2, getX() + this.width - 2, getY() + this.height - 2, 18, male, getX() + this.width / 2.0F, getY() + 18.0F);
            } else if (preview instanceof FemaleVillagerEntity female) {
                String curatedSkinPath = BirthScreenData.getCuratedSkinResourcePath(BirthChildScreen.this.childGender, BirthChildScreen.this.skinType, this.skinId);
                female.setCustomSkinPath(curatedSkinPath == null ? "" : curatedSkinPath);
                female.setSkinIndex(this.skinId);
                female.setAgeStage(VillagerAgeStage.TODDLER);
                female.setPersonality(BirthChildScreen.this.selectedPersonality);
                renderEntityPreview(graphics, getX() + 2, getY() + 2, getX() + this.width - 2, getY() + this.height - 2, 18, female, getX() + this.width / 2.0F, getY() + 18.0F);
            }
            if (selected) {
                graphics.fill(getX(), getY(), getX() + this.width, getY() + 2, 0xFFFFFFFF);
                graphics.fill(getX(), getY() + this.height - 2, getX() + this.width, getY() + this.height, 0xFFFFFFFF);
                graphics.fill(getX(), getY(), getX() + 2, getY() + this.height, 0xFFFFFFFF);
                graphics.fill(getX() + this.width - 2, getY(), getX() + this.width, getY() + this.height, 0xFFFFFFFF);
            }
        }
    }

    private class CustomSkinOptionButton extends Button {

        protected CustomSkinOptionButton(int x, int y) {
            super(x, y, 40, 40, Component.empty(), button -> {
                BirthChildScreen.this.useCustomSkin = true;
                BirthChildScreen.this.updatePreviewEntity();
                BirthChildScreen.this.updateSkinButtonPositions();
                BirthChildScreen.this.updateConfirmButton();
            }, DEFAULT_NARRATION);
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.fill(getX(), getY(), getX() + this.width, getY() + this.height, this.isHovered ? 0xAA446688 : 0x88335577);
            LivingEntity preview = BirthChildScreen.this.createPreviewEntity();
            if (preview instanceof MaleVillagerEntity male) {
                male.setSavedSkinInput(BirthChildScreen.this.customSkinInput);
                male.setCustomSkinPath(BirthChildScreen.this.customSkinPreviewPath);
                male.setAgeStage(VillagerAgeStage.TODDLER);
                male.setPersonality(BirthChildScreen.this.selectedPersonality);
                renderEntityPreview(graphics, getX() + 2, getY() + 2, getX() + this.width - 2, getY() + this.height - 2, 18, male, getX() + this.width / 2.0F, getY() + 18.0F);
            } else if (preview instanceof FemaleVillagerEntity female) {
                female.setSavedSkinInput(BirthChildScreen.this.customSkinInput);
                female.setCustomSkinPath(BirthChildScreen.this.customSkinPreviewPath);
                female.setAgeStage(VillagerAgeStage.TODDLER);
                female.setPersonality(BirthChildScreen.this.selectedPersonality);
                renderEntityPreview(graphics, getX() + 2, getY() + 2, getX() + this.width - 2, getY() + this.height - 2, 18, female, getX() + this.width / 2.0F, getY() + 18.0F);
            }
            graphics.fill(getX(), getY(), getX() + this.width, getY() + 2, 0xFFB0D8FF);
            graphics.fill(getX(), getY() + this.height - 2, getX() + this.width, getY() + this.height, 0xFFB0D8FF);
            graphics.fill(getX(), getY(), getX() + 2, getY() + this.height, 0xFFB0D8FF);
            graphics.fill(getX() + this.width - 2, getY(), getX() + this.width, getY() + this.height, 0xFFB0D8FF);
        }
    }
}
