package com.javic.slimpatch.client.renderer;

import com.javic.slimpatch.client.model.CustomVillagerModelMale;
import com.javic.slimpatch.entity.MaleVillagerEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class MaleVillagerRenderer extends AbstractVillagerHumanoidRenderer<MaleVillagerEntity> {

    public MaleVillagerRenderer(EntityRendererProvider.Context context) {
        super(context, new CustomVillagerModelMale<>(context.bakeLayer(CustomVillagerModelMale.LAYER_LOCATION)));
    }

    @Override
    protected String getCustomSkinPath(MaleVillagerEntity entity) {
        return entity.getCustomSkinPath();
    }

    @Override
    protected ResourceLocation getFallbackTextureLocation(MaleVillagerEntity entity) {
        return entity.getSkinTexture();
    }

    @Override
    protected float getHorseMountedOffset(MaleVillagerEntity entity) {
        return -0.42F;
    }

    @Override
    protected float getCamelMountedOffset(MaleVillagerEntity entity) {
        return -0.32F;
    }

    @Override
    protected void applyModelScale(MaleVillagerEntity entity, PoseStack poseStack) {
        float widthScale = entity.getVisualWidth() / 100.0F;
        float heightScale = entity.getVisualHeight() / 100.0F;
        float ageStageScale = this.getAgeStageScale(entity);
        poseStack.scale(1.0F, 1.0F, 0.995F);
        poseStack.scale(widthScale, heightScale, widthScale);
        poseStack.scale(ageStageScale, ageStageScale, ageStageScale);
    }
}
