package com.javic.slimpatch.client.renderer;

import com.javic.slimpatch.entity.HumanEvokerEntity;
import com.javic.slimpatch.client.model.HumanEvokerModelMale;
import com.javic.slimpatch.client.model.HumanEvokerModelFemale;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public class HumanEvokerRenderer extends MobRenderer<HumanEvokerEntity, HumanoidModel<HumanEvokerEntity>> {

    private final HumanoidModel<HumanEvokerEntity> maleModel;
    private final HumanoidModel<HumanEvokerEntity> femaleModel;

    public HumanEvokerRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanEvokerModelMale<>(context.bakeLayer(HumanEvokerModelMale.LAYER_LOCATION)), 0.5f);
        this.maleModel = new HumanEvokerModelMale<>(context.bakeLayer(HumanEvokerModelMale.LAYER_LOCATION));
        this.femaleModel = new HumanEvokerModelFemale<>(context.bakeLayer(HumanEvokerModelFemale.LAYER_LOCATION));
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(HumanEvokerEntity entity) {
        return entity.getSkinTexture();
    }

    @Override
    public void render(HumanEvokerEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        this.model = entity.isFemale() ? femaleModel : maleModel;
        poseStack.pushPose();
        poseStack.scale(entity.isFemale() ? 0.95F : 1.0F, entity.isFemale() ? 0.95F : 1.0F, entity.isFemale() ? 0.95F : 1.0F);
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}