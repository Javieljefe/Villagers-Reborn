package com.javic.slimpatch.client.gui;

import com.javic.slimpatch.client.ClientMultiplayerSkinSettings;
import com.javic.slimpatch.config.SlimPatchConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.Util;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class VillagerEditScreen extends Screen {

    private static final double MIN_SCALE = 0.50;
    private static final double MAX_SCALE = 1.50;

    private final Villager villager;
    private EditBox nameField;
    private EditBox skinField;
    private ScaleSlider heightSlider;
    private ScaleSlider widthSlider;
    private Component heightLabel;
    private Component widthLabel;

    public VillagerEditScreen(Villager villager) {
        super(Component.translatable("slimpatch.screen.villager_edit.title"));
        this.villager = villager;
    }

    @Override
    protected void init() {
        super.init();

        heightLabel = Component.translatable("slimpatch.screen.villager_edit.height");
        widthLabel = Component.translatable("slimpatch.screen.villager_edit.width");

        nameField = new EditBox(this.font, this.width / 2 - 100, this.height / 2 - 80, 200, 20, Component.literal(villager.getName().getString()));
        nameField.setValue(villager.getName().getString());
        skinField = new EditBox(this.font, this.width / 2 - 100, this.height / 2 - 20, 200, 20, Component.translatable("slimpatch.screen.villager_edit.skin_placeholder"));
        skinField.setMaxLength(500);
        skinField.setValue(getSavedSkinInput());

        double savedHeight = getSavedScale("Height");
        double savedWidth = getSavedScale("Width");

        heightSlider = new ScaleSlider(this.width / 2 - 100, this.height / 2 + 20, 200, 20, heightLabel, savedHeight);
        widthSlider = new ScaleSlider(this.width / 2 - 100, this.height / 2 + 60, 200, 20, widthLabel, savedWidth);

        this.addRenderableWidget(Button.builder(Component.translatable("slimpatch.screen.villager_edit.save"), b -> saveChanges())
                .bounds(this.width / 2 - 100, this.height / 2 + 100, 200, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("slimpatch.screen.villager_edit.open_skins_folder"), b -> openSkinsFolder())
                .bounds(this.width / 2 - 100, this.height / 2 + 130, 200, 20).build());

        this.addRenderableWidget(nameField);
        this.addRenderableWidget(skinField);
        this.addRenderableWidget(heightSlider);
        this.addRenderableWidget(widthSlider);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int labelWidth = 55;
        int labelHeight = 20;
        int labelX = this.width / 2 - 100 - labelWidth - 6;
        int color = 0xFFFFFF;

        renderLabel(guiGraphics, Component.translatable("slimpatch.screen.villager_edit.name"), labelX, this.height / 2 - 80, labelWidth, labelHeight, color);
        renderLabel(guiGraphics, Component.translatable("slimpatch.screen.villager_edit.skin"), labelX, this.height / 2 - 20, labelWidth, labelHeight, color);
    }

    private void renderLabel(GuiGraphics guiGraphics, Component text, int x, int y, int width, int height, int color) {
        guiGraphics.fill(x, y, x + width, y + height, 0xAA000000);
        guiGraphics.fill(x, y, x + width, y + 1, 0xFFFFFFFF);
        guiGraphics.fill(x, y, x + 1, y + height, 0xFFFFFFFF);
        guiGraphics.fill(x + width - 1, y, x + width, y + height, 0xFF555555);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, 0xFF555555);
        guiGraphics.drawCenteredString(this.font, text, x + width / 2, y + 6, color);
    }

    private void saveChanges() {
		boolean applyLocally = allowLocalCustomSkins();
		if (applyLocally) {
            villager.setCustomName(Component.literal(nameField.getValue()));
        }
		net.neoforged.neoforge.network.PacketDistributor.sendToServer(new com.javic.slimpatch.network.VillagerNamePacket(villager.getUUID(), nameField.getValue()));
        if (applyLocally) {
            villager.getPersistentData().putString("SavedName", nameField.getValue());
        }
        String skinName = skinField.getValue().trim();
        if (!skinName.isEmpty() && allowLocalCustomSkins()) {
            villager.getPersistentData().putString("SavedSkinInput", skinName);
            applySkin(skinName);
        } else if (canUploadMultiplayerCustomSkin()) {
            if (skinName.isEmpty()) {
                net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                        new com.javic.slimpatch.network.VillagerCustomSkinUploadPacket(villager.getId(), "", new byte[0], true)
                );
            } else {
                byte[] pngData = readSkinBytesForUpload(skinName);
                if (pngData == null) {
                    return;
                }
                net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                        new com.javic.slimpatch.network.VillagerCustomSkinUploadPacket(villager.getId(), skinName, pngData, false)
                );
            }
        } else if (!allowLocalCustomSkins()) {
            villager.getPersistentData().remove("SavedSkinInput");
            villager.getPersistentData().remove("CustomSkinPath");
        }
        int savedHeight = (int) Math.round(heightSlider.getScaleValue() * 100.0);
        int savedWidth = (int) Math.round(widthSlider.getScaleValue() * 100.0);
        if (applyLocally) {
            villager.getPersistentData().putInt("Height", savedHeight);
            villager.getPersistentData().putInt("Width", savedWidth);
        }
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(new com.javic.slimpatch.network.VillagerEditDataPacket(
                villager.getUUID(),
                villager.getPersistentData().getString("SavedSkinInput"),
                villager.getPersistentData().getString("CustomSkinPath"),
                savedHeight,
                savedWidth
        ));
        Minecraft.getInstance().setScreen(new VillagerDialogueScreen(villager));
    }

    private double getSavedScale(String key) {
        if (villager instanceof com.javic.slimpatch.entity.MaleVillagerEntity male) {
            return Math.max(MIN_SCALE, Math.min(MAX_SCALE, ("Height".equals(key) ? male.getVisualHeight() : male.getVisualWidth()) / 100.0));
        }
        if (villager instanceof com.javic.slimpatch.entity.FemaleVillagerEntity female) {
            return Math.max(MIN_SCALE, Math.min(MAX_SCALE, ("Height".equals(key) ? female.getVisualHeight() : female.getVisualWidth()) / 100.0));
        }
        if (!villager.getPersistentData().contains(key)) {
            return 1.0;
        }
        return Math.max(MIN_SCALE, Math.min(MAX_SCALE, villager.getPersistentData().getInt(key) / 100.0));
    }

    private String getSavedSkinInput() {
        if (villager instanceof com.javic.slimpatch.entity.MaleVillagerEntity male) {
            return male.getSavedSkinInput();
        }
        if (villager instanceof com.javic.slimpatch.entity.FemaleVillagerEntity female) {
            return female.getSavedSkinInput();
        }
        return villager.getPersistentData().getString("SavedSkinInput");
    }

    private void applySkin(String skinName) {
        if (!allowLocalCustomSkins()) {
            return;
        }
        try {
            String trimmedSkinName = skinName.trim();
            if (trimmedSkinName.isEmpty()) {
                return;
            }

            UUID uuid = villager.getUUID();
            File skinFolder = new File(Minecraft.getInstance().gameDirectory, "config/slimpatch/skins/" + uuid);
            if (!skinFolder.exists()) skinFolder.mkdirs();

            File skinFile;
            if (trimmedSkinName.startsWith("http://") || trimmedSkinName.startsWith("https://")) {
                skinFile = new File(skinFolder, uuid.toString() + ".png");
                try (InputStream inputStream = URI.create(trimmedSkinName).toURL().openStream()) {
                    Files.copy(inputStream, skinFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            } else {
                skinFile = new File(skinFolder, trimmedSkinName);
            }

            if (skinFile.exists() && skinFile.isFile()) {
                String safePath = skinFile.getAbsolutePath().replace("\\", "/");

                villager.getPersistentData().putString("CustomSkinPath", safePath);
                villager.getPersistentData().putString("SavedName", villager.getName().getString());

            }
        } catch (Exception e) {
        }
    }

    private void openSkinsFolder() {
        if (!allowLocalCustomSkins() && !canUploadMultiplayerCustomSkin()) {
            return;
        }
        try {
            UUID uuid = villager.getUUID();
            File folder = new File(Minecraft.getInstance().gameDirectory, "config/slimpatch/skins/" + uuid);
            if (!folder.exists()) folder.mkdirs();
            Util.getPlatform().openFile(folder);
        } catch (Exception e) {
        }
    }

    private boolean allowLocalCustomSkins() {
        Minecraft mc = Minecraft.getInstance();
        return mc.getSingleplayerServer() != null && !mc.getSingleplayerServer().isPublished();
    }

    private boolean canUploadMultiplayerCustomSkin() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getSingleplayerServer() != null && mc.getSingleplayerServer().isPublished()) {
            return SlimPatchConfig.SERVER.allowMultiplayerCustomSkins.get();
        }
        return ClientMultiplayerSkinSettings.isAllowed();
    }

    private byte[] readSkinBytesForUpload(String skinName) {
        try {
            String trimmedSkinName = skinName.trim();
            if (trimmedSkinName.isEmpty()) {
                return null;
            }

            int maxBytes = ClientMultiplayerSkinSettings.getMaxCustomSkinSizeKb() * 1024;
            if (trimmedSkinName.startsWith("http://") || trimmedSkinName.startsWith("https://")) {
                try (InputStream inputStream = URI.create(trimmedSkinName).toURL().openStream()) {
                    byte[] data = inputStream.readNBytes(maxBytes + 1);
                    if (data.length > maxBytes) {
                        return null;
                    }
                    return data;
                }
            }

            UUID uuid = villager.getUUID();
            Path skinFile = new File(Minecraft.getInstance().gameDirectory, "config/slimpatch/skins/" + uuid + "/" + trimmedSkinName).toPath();
            if (!Files.exists(skinFile) || !Files.isRegularFile(skinFile)) {
                return null;
            }
            if (Files.size(skinFile) > maxBytes) {
                return null;
            }
            return Files.readAllBytes(skinFile);
        } catch (Exception e) {
            return null;
        }
    }

    private static class ScaleSlider extends AbstractSliderButton {
        private final String translationKey;

        public ScaleSlider(int x, int y, int w, int h, Component msg, double val) {
            super(x, y, w, h, msg, toSliderValue(val));
            this.translationKey = msg.getString().equals(Component.translatable("slimpatch.screen.villager_edit.width").getString())
                    ? "slimpatch.screen.villager_edit.width"
                    : "slimpatch.screen.villager_edit.height";
            updateMessage();
        }
        @Override
        protected void updateMessage() {
            setMessage(Component.translatable(translationKey).append(" " + String.format("%.2f", getScaleValue())));
        }
        @Override
        protected void applyValue() {}
        public double getScaleValue() {
            return MIN_SCALE + (this.value * (MAX_SCALE - MIN_SCALE));
        }
        private static double toSliderValue(double scaleValue) {
            return (scaleValue - MIN_SCALE) / (MAX_SCALE - MIN_SCALE);
        }
    }
}
