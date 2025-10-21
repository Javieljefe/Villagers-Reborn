package com.javic.slimpatch.client.renderer;

import com.javic.slimpatch.entity.HumanVindicatorEntity;
import com.javic.slimpatch.client.model.HumanVindicatorModelMale;
import com.javic.slimpatch.client.model.HumanVindicatorModelFemale;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public class HumanVindicatorRenderer extends MobRenderer<HumanVindicatorEntity, HumanoidModel<HumanVindicatorEntity>> {

    private final HumanoidModel<HumanVindicatorEntity> maleModel;
    private final HumanoidModel<HumanVindicatorEntity> femaleModel;

    public HumanVindicatorRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanVindicatorModelMale<>(context.bakeLayer(HumanVindicatorModelMale.LAYER_LOCATION)), 0.5f);
        this.maleModel = new HumanVindicatorModelMale<>(context.bakeLayer(HumanVindicatorModelMale.LAYER_LOCATION));
        this.femaleModel = new HumanVindicatorModelFemale<>(context.bakeLayer(HumanVindicatorModelFemale.LAYER_LOCATION));
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(HumanVindicatorEntity entity) {
        return entity.getSkinTexture();
    }

    @Override
    public void render(HumanVindicatorEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        this.model = entity.isFemale() ? femaleModel : maleModel;
        poseStack.pushPose();
        poseStack.scale(entity.isFemale() ? 0.95F : 1.0F, entity.isFemale() ? 0.95F : 1.0F, entity.isFemale() ? 0.95F : 1.0F);
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}