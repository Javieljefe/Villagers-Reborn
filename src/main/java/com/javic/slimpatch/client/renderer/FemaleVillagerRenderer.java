package com.javic.slimpatch.client.renderer;

import com.javic.slimpatch.client.model.FemaleVillagerModel;
import com.javic.slimpatch.entity.FemaleVillagerEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class FemaleVillagerRenderer extends MobRenderer<FemaleVillagerEntity, HumanoidModel<FemaleVillagerEntity>> {

    public FemaleVillagerRenderer(EntityRendererProvider.Context context) {
        super(context, new FemaleVillagerModel<>(context.bakeLayer(FemaleVillagerModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(FemaleVillagerEntity entity) {
        return entity.getSkinTexture();
    }

    @Override
    public void render(FemaleVillagerEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        poseStack.scale(0.95F, 0.95F, 0.95F);
        poseStack.scale(0.93F, 1.0F, 0.93F);

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}