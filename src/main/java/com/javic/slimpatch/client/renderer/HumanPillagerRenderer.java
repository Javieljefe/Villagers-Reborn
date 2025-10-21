package com.javic.slimpatch.client.renderer;

import com.javic.slimpatch.entity.HumanPillagerEntity;
import com.javic.slimpatch.client.model.HumanPillagerModelMale;
import com.javic.slimpatch.client.model.HumanPillagerModelFemale;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public class HumanPillagerRenderer extends MobRenderer<HumanPillagerEntity, HumanoidModel<HumanPillagerEntity>> {

    private final HumanoidModel<HumanPillagerEntity> maleModel;
    private final HumanoidModel<HumanPillagerEntity> femaleModel;

    public HumanPillagerRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanPillagerModelMale<>(context.bakeLayer(HumanPillagerModelMale.LAYER_LOCATION)), 0.5f);
        this.maleModel = new HumanPillagerModelMale<>(context.bakeLayer(HumanPillagerModelMale.LAYER_LOCATION));
        this.femaleModel = new HumanPillagerModelFemale<>(context.bakeLayer(HumanPillagerModelFemale.LAYER_LOCATION));

        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(HumanPillagerEntity entity) {
        return entity.getSkinTexture();
    }

    @Override
    public void render(HumanPillagerEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        if (entity.isFemale()) {
            this.model = femaleModel;
        } else {
            this.model = maleModel;
        }

        poseStack.pushPose();

        if (entity.isFemale()) {
            poseStack.scale(0.95F, 0.95F, 0.95F);
        } else {
            poseStack.scale(1.0F, 1.0F, 0.995F);
        }

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}