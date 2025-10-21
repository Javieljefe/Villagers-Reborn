package com.javic.slimpatch.client.renderer;

import com.javic.slimpatch.client.model.CustomVillagerModelMale;
import com.javic.slimpatch.entity.MaleVillagerEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class MaleVillagerRenderer extends MobRenderer<MaleVillagerEntity, HumanoidModel<MaleVillagerEntity>> {

    public MaleVillagerRenderer(EntityRendererProvider.Context context) {
        super(context, new CustomVillagerModelMale<>(context.bakeLayer(CustomVillagerModelMale.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(MaleVillagerEntity entity) {
        return entity.getSkinTexture();
    }

    @Override
    public void render(MaleVillagerEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        poseStack.scale(1.0F, 1.0F, 0.995F);

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}