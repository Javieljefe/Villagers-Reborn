package com.javic.slimpatch.client.renderer;

import com.javic.slimpatch.Config;
import com.javic.slimpatch.client.cutscene.FamilyCutsceneController;
import com.javic.slimpatch.client.model.FemaleVillagerModel;
import com.javic.slimpatch.entity.FemaleVillagerEntity;
import com.javic.slimpatch.entity.VillagerAgeStage;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;

public class FemaleVillagerRenderer extends AbstractVillagerHumanoidRenderer<FemaleVillagerEntity> {

    public FemaleVillagerRenderer(EntityRendererProvider.Context context) {
        super(context, new FemaleVillagerModel<>(context.bakeLayer(FemaleVillagerModel.LAYER_LOCATION)));
    }

    @Override
    protected String getCustomSkinPath(FemaleVillagerEntity entity) {
        return entity.getCustomSkinPath();
    }

    @Override
    protected ResourceLocation getFallbackTextureLocation(FemaleVillagerEntity entity) {
        return entity.getSkinTexture();
    }

    @Override
    protected float getHorseMountedOffset(FemaleVillagerEntity entity) {
        return -0.40F;
    }

    @Override
    protected float getCamelMountedOffset(FemaleVillagerEntity entity) {
        return -0.30F;
    }

    @Override
    protected void applyModelScale(FemaleVillagerEntity entity, PoseStack poseStack) {
        float widthScale = entity.getVisualWidth() / 100.0F;
        float heightScale = entity.getVisualHeight() / 100.0F;
        float ageStageScale = this.getAgeStageScale(entity);
        poseStack.scale(0.95F, 0.95F, 0.95F);
        poseStack.scale(0.93F, 1.0F, 0.93F);
        poseStack.scale(widthScale, heightScale, widthScale);
        poseStack.scale(ageStageScale, ageStageScale, ageStageScale);
    }

    @Override
    public void render(FemaleVillagerEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       net.minecraft.client.renderer.MultiBufferSource buffer, int packedLight) {
        if (this.model instanceof FemaleVillagerModel<FemaleVillagerEntity> femaleModel) {
            femaleModel.setPregnancyBellyVisible(Config.ENABLE_PREGNANCY_BELLY.get()
                    && entity.getAgeStage() == VillagerAgeStage.ADULT
                    && entity.isExpectingChild()
                    && (!FamilyCutsceneController.isActive() || FamilyCutsceneController.getVillagerEntityId() != entity.getId())
                    && entity.getItemBySlot(EquipmentSlot.CHEST).isEmpty());
        }
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}
