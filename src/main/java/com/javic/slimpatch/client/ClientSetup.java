package com.javic.slimpatch.client;

import com.javic.slimpatch.Config;
import com.javic.slimpatch.ModEntities;
import com.javic.slimpatch.ModMenus;
import com.javic.slimpatch.client.gui.SkinSelectionHandler;
import com.javic.slimpatch.client.gui.VillagerEquipmentScreen;
import com.javic.slimpatch.client.key.ModKeyBindings;
import com.javic.slimpatch.client.model.*;
import com.javic.slimpatch.client.renderer.*;
import com.javic.slimpatch.entity.FemaleVillagerEntity;
import com.javic.slimpatch.entity.MaleVillagerEntity;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EvokerRenderer;
import net.minecraft.client.renderer.entity.PillagerRenderer;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.client.renderer.entity.VindicatorRenderer;
import net.minecraft.client.renderer.entity.WitchRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import com.javic.slimpatch.item.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

public class ClientSetup {
    private static final ResourceLocation CROSSHAIR_LAYER = VanillaGuiLayers.CROSSHAIR;
    private static boolean suppressingCrosshairAttackIndicator;

    public static void init(IEventBus modEventBus) {
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(SkinSelectionHandler.class);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(ClientSetup::onScreenInit);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(ClientSetup::onRenderGui);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(ClientSetup::onRenderGuiLayerPre);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(ClientSetup::onRenderGuiLayerPost);
        modEventBus.addListener(ClientSetup::onClientSetup);
        modEventBus.addListener(ClientSetup::registerRenderers);
        modEventBus.addListener(ClientSetup::registerLayerDefinitions);
        modEventBus.addListener(ClientSetup::registerMenuScreens);
        modEventBus.addListener(ClientSetup::registerItemColors);
    }

    private static void onClientSetup(final FMLClientSetupEvent event) {
    }

    private static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof ConfigurationScreen)) return;
        if (!event.getScreen().getTitle().getString().equals(Component.translatable("slimpatch.configuration.title").getString())) return;

        String villagerSettings = Component.translatable("slimpatch.configuration.section.slimpatch.common.toml").getString();
        String globalSettings = Component.translatable("slimpatch.configuration.section.villagersreborn.toml").getString();

        for (var listener : event.getListenersList()) {
            if (listener instanceof AbstractWidget widget) {
                String message = widget.getMessage().getString();
                if (message.startsWith(villagerSettings) || message.startsWith(globalSettings)) {
                    widget.setY(widget.getY() + 14);
                }
            }
        }
    }

    private static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (!Config.PRESS_R_TO_TALK_INDICATOR.get()) return;
        LocalPlayer player = mc.player;
        if (player == null || mc.screen != null || mc.hitResult == null || mc.hitResult.getType() != HitResult.Type.ENTITY) return;

        Entity entity = ((EntityHitResult) mc.hitResult).getEntity();
        if (!(entity instanceof MaleVillagerEntity) && !(entity instanceof FemaleVillagerEntity)) return;
        if (player.distanceToSqr(entity) > 16.0D) return;

        GuiGraphics graphics = event.getGuiGraphics();
        int centerX = mc.getWindow().getGuiScaledWidth() / 2;
        int y = mc.getWindow().getGuiScaledHeight() / 2 + 10;
        float pulse = 0.88F + 0.12F * (float) ((Math.sin(System.currentTimeMillis() * (Math.PI * 2.0D) / 2000.0D) + 1.0D) * 0.5D);
        int textColor = (Math.min(255, Math.max(0, (int) (235 * pulse))) << 16)
                | (Math.min(255, Math.max(0, (int) (235 * pulse))) << 8)
                | Math.min(255, Math.max(0, (int) (235 * pulse)));
        int rColor = (Math.min(255, Math.max(0, (int) (255 * pulse))) << 16)
                | (Math.min(255, Math.max(0, (int) (255 * pulse))) << 8)
                | Math.min(255, Math.max(0, (int) (255 * pulse)));
        String keyName = ModKeyBindings.getOpenDialogueKeyMessage().getString();
        String text = Component.translatable("slimpatch.overlay.press_to_talk", keyName).getString();
        float rScale = 1.15F;
        int textWidth = mc.font.width(text);
        int keyIndex = text.indexOf(keyName);
        if (keyIndex < 0) {
            drawOutlinedString(graphics, mc, text, centerX - textWidth / 2, y, textColor);
            return;
        }

        String left = text.substring(0, keyIndex);
        String right = text.substring(keyIndex + keyName.length());
        int leftWidth = mc.font.width(left);
        int rWidth = Math.round(mc.font.width(keyName) * rScale);
        int rightWidth = mc.font.width(right);
        int startX = centerX - (leftWidth + rWidth + rightWidth) / 2;

        drawOutlinedString(graphics, mc, left, startX, y, textColor);
        drawScaledOutlinedString(graphics, mc, keyName, startX + leftWidth, y - 1, rScale, rColor);
        drawOutlinedString(graphics, mc, right, startX + leftWidth + rWidth, y, textColor);
    }

    private static void onRenderGuiLayerPre(RenderGuiLayerEvent.Pre event) {
        if (!CROSSHAIR_LAYER.equals(event.getName())) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.options.attackIndicator().get() != AttackIndicatorStatus.CROSSHAIR) return;
        if (!isCustomVillagerCrosshairTarget(mc)) return;

        mc.options.attackIndicator().set(AttackIndicatorStatus.OFF);
        suppressingCrosshairAttackIndicator = true;
    }

    private static void onRenderGuiLayerPost(RenderGuiLayerEvent.Post event) {
        if (!suppressingCrosshairAttackIndicator || !CROSSHAIR_LAYER.equals(event.getName())) return;

        Minecraft.getInstance().options.attackIndicator().set(AttackIndicatorStatus.CROSSHAIR);
        suppressingCrosshairAttackIndicator = false;
    }

    private static boolean isCustomVillagerCrosshairTarget(Minecraft mc) {
        if (mc.hitResult instanceof EntityHitResult entityHitResult) {
            Entity entity = entityHitResult.getEntity();
            if (entity instanceof MaleVillagerEntity || entity instanceof FemaleVillagerEntity) {
                return true;
            }
        }

        Entity entity = mc.crosshairPickEntity;
        return entity instanceof MaleVillagerEntity || entity instanceof FemaleVillagerEntity;
    }

    private static void drawOutlinedString(GuiGraphics graphics, Minecraft mc, String text, int x, int y, int color) {
        graphics.drawString(mc.font, text, x + 1, y, 0x000000);
        graphics.drawString(mc.font, text, x - 1, y, 0x000000);
        graphics.drawString(mc.font, text, x, y + 1, 0x000000);
        graphics.drawString(mc.font, text, x, y - 1, 0x000000);
        graphics.drawString(mc.font, text, x, y, color);
    }

    private static void drawScaledOutlinedString(GuiGraphics graphics, Minecraft mc, String text, int x, int y, float scale, int color) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        drawOutlinedString(graphics, mc, text, 0, 0, color);
        graphics.pose().popPose();
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.MALE_VILLAGER.get(), Config.CUSTOM_VILLAGER_SKINS.get() ? MaleVillagerRenderer::new : (EntityRendererProvider) (context -> new MountedVillagerRenderer(context, -0.42F, -0.32F)));
        event.registerEntityRenderer(ModEntities.FEMALE_VILLAGER.get(), Config.CUSTOM_VILLAGER_SKINS.get() ? FemaleVillagerRenderer::new : (EntityRendererProvider) (context -> new MountedVillagerRenderer(context, -0.40F, -0.30F)));
        event.registerEntityRenderer(ModEntities.HUMAN_WANDERING_TRADER.get(), HumanWanderingTraderRenderer::new);
        event.registerEntityRenderer(ModEntities.HUMAN_TRADER_NATURAL.get(), HumanWanderingTraderRenderer::new);
        event.registerEntityRenderer(ModEntities.HUMAN_PILLAGER.get(), Config.CUSTOM_ILLAGER_SKINS.get() && Config.CUSTOM_PILLAGER_MODEL.get() ? HumanPillagerRenderer::new : (EntityRendererProvider) PillagerRenderer::new);
        event.registerEntityRenderer(ModEntities.HUMAN_VINDICATOR.get(), Config.CUSTOM_ILLAGER_SKINS.get() && Config.CUSTOM_VINDICATOR_MODEL.get() ? HumanVindicatorRenderer::new : (EntityRendererProvider) VindicatorRenderer::new);
        event.registerEntityRenderer(ModEntities.HUMAN_EVOKER.get(), Config.CUSTOM_ILLAGER_SKINS.get() && Config.CUSTOM_EVOKER_MODEL.get() ? HumanEvokerRenderer::new : (EntityRendererProvider) EvokerRenderer::new);
        event.registerEntityRenderer(net.minecraft.world.entity.EntityType.WITCH, Config.CUSTOM_WITCH_SKIN.get() ? HumanWitchRenderer::new : WitchRenderer::new);
        event.registerEntityRenderer(ModEntities.HUMAN_ZOMBIE_VILLAGER.get(), HumanZombieVillagerRenderer::new);
    }

    private static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(FemaleVillagerModel.LAYER_LOCATION, FemaleVillagerModel::createBodyLayer);
        event.registerLayerDefinition(CustomVillagerModelMale.LAYER_LOCATION, CustomVillagerModelMale::createBodyLayer);
        event.registerLayerDefinition(HumanPillagerModelMale.LAYER_LOCATION, HumanPillagerModelMale::createBodyLayer);
        event.registerLayerDefinition(HumanPillagerModelFemale.LAYER_LOCATION, HumanPillagerModelFemale::createBodyLayer);
        event.registerLayerDefinition(HumanVindicatorModelMale.LAYER_LOCATION, HumanVindicatorModelMale::createBodyLayer);
        event.registerLayerDefinition(HumanVindicatorModelFemale.LAYER_LOCATION, HumanVindicatorModelFemale::createBodyLayer);
        event.registerLayerDefinition(HumanEvokerModelMale.LAYER_LOCATION, HumanEvokerModelMale::createBodyLayer);
        event.registerLayerDefinition(HumanEvokerModelFemale.LAYER_LOCATION, HumanEvokerModelFemale::createBodyLayer);
        event.registerLayerDefinition(HumanWanderingTraderModelMale.LAYER_LOCATION, HumanWanderingTraderModelMale::createBodyLayer);
        event.registerLayerDefinition(HumanWanderingTraderModelFemale.LAYER_LOCATION, HumanWanderingTraderModelFemale::createBodyLayer);
        event.registerLayerDefinition(MaleZombieVillagerModel.LAYER_LOCATION, MaleZombieVillagerModel::createBodyLayer);
        event.registerLayerDefinition(FemaleZombieVillagerModel.LAYER_LOCATION, FemaleZombieVillagerModel::createBodyLayer);
    }

    private static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, layer) -> -1, ModItems.MALE_VILLAGER_SPAWN_EGG.get());
        event.register((stack, layer) -> -1, ModItems.FEMALE_VILLAGER_SPAWN_EGG.get());
    }

    private static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.VILLAGER_EQUIPMENT.get(), VillagerEquipmentScreen::new);
    }
}
