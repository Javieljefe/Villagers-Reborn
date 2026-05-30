package com.javic.slimpatch.client.renderer;

import com.javic.slimpatch.entity.MaleVillagerEntity;
import com.javic.slimpatch.entity.FemaleVillagerEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class QuestIndicatorRenderer {

    private static final ResourceLocation ICON = ResourceLocation.fromNamespaceAndPath("slimpatch", "textures/gui/quest_marker.png");

    @SubscribeEvent
    public static <T extends LivingEntity, M extends EntityModel<T>> void onRenderVillager(RenderLivingEvent.Post<T, M> event) {
        LivingEntity entity = event.getEntity();
        boolean shouldRender = false;

        if (entity instanceof MaleVillagerEntity male) {
            shouldRender = male.hasQuest() && male.getQuestId() != null && !male.getQuestId().isEmpty();
        } else if (entity instanceof FemaleVillagerEntity female) {
            shouldRender = female.hasQuest() && female.getQuestId() != null && !female.getQuestId().isEmpty();
        }

        if (!shouldRender) return;

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource buffer = event.getMultiBufferSource();
        Minecraft mc = Minecraft.getInstance();

        poseStack.pushPose();

        double yOffset = entity.getBbHeight() + 0.6;
        poseStack.translate(0.0, yOffset, 0.0);

        float rotation = -mc.getEntityRenderDispatcher().camera.getYRot();
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        float time = (System.currentTimeMillis() % 1000L) / 1000.0f;
        float bounce = (float) Math.sin(time * Math.PI * 2.0f) * 0.05f;
        poseStack.translate(0.0, bounce, 0.0);

        float scale = 0.03f;
        poseStack.scale(scale, scale, scale);

        VertexConsumer vertex = buffer.getBuffer(RenderType.entityTranslucent(ICON));
        drawQuad(poseStack, vertex, 16, 16, event.getPackedLight());

        poseStack.popPose();
    }

    private static void drawQuad(PoseStack poseStack, VertexConsumer vertex, int width, int height, int light) {
        PoseStack.Pose last = poseStack.last();
        float halfW = width / 2f;
        float halfH = height / 2f;

        // bottom-left
        vertex.addVertex(last.pose(), -halfW, -halfH, 0.0f);
        vertex.setUv(0, 1);
        vertex.setColor(255, 255, 255, 255);
        vertex.setOverlay(OverlayTexture.NO_OVERLAY);
        vertex.setLight(light);
        vertex.setNormal(last, 0, 1, 0);

        // bottom-right
        vertex.addVertex(last.pose(), halfW, -halfH, 0.0f);
        vertex.setUv(1, 1);
        vertex.setColor(255, 255, 255, 255);
        vertex.setOverlay(OverlayTexture.NO_OVERLAY);
        vertex.setLight(light);
        vertex.setNormal(last, 0, 1, 0);

        // top-right
        vertex.addVertex(last.pose(), halfW, halfH, 0.0f);
        vertex.setUv(1, 0);
        vertex.setColor(255, 255, 255, 255);
        vertex.setOverlay(OverlayTexture.NO_OVERLAY);
        vertex.setLight(light);
        vertex.setNormal(last, 0, 1, 0);

        // top-left
        vertex.addVertex(last.pose(), -halfW, halfH, 0.0f);
        vertex.setUv(0, 0);
        vertex.setColor(255, 255, 255, 255);
        vertex.setOverlay(OverlayTexture.NO_OVERLAY);
        vertex.setLight(light);
        vertex.setNormal(last, 0, 1, 0);
    }
}