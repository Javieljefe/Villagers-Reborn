package com.javic.slimpatch.client.renderer;

import com.javic.slimpatch.client.model.MaleZombieVillagerModel;
import com.javic.slimpatch.client.model.FemaleZombieVillagerModel;
import com.javic.slimpatch.entity.HumanZombieVillagerEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.vertex.PoseStack;

public class HumanZombieVillagerRenderer extends MobRenderer<HumanZombieVillagerEntity, HumanoidModel<HumanZombieVillagerEntity>> {

    private final HumanoidModel<HumanZombieVillagerEntity> maleModel;
    private final HumanoidModel<HumanZombieVillagerEntity> femaleModel;

    public HumanZombieVillagerRenderer(EntityRendererProvider.Context context) {
        super(context, new MaleZombieVillagerModel<>(context.bakeLayer(MaleZombieVillagerModel.LAYER_LOCATION)), 0.5f);
        this.maleModel = new MaleZombieVillagerModel<>(context.bakeLayer(MaleZombieVillagerModel.LAYER_LOCATION));
        this.femaleModel = new FemaleZombieVillagerModel<>(context.bakeLayer(FemaleZombieVillagerModel.LAYER_LOCATION));
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(HumanZombieVillagerEntity entity) {
        String gender = entity.getGender();
        if (gender == null || gender.isEmpty()) {
            gender = entity.getRandom().nextBoolean() ? "male" : "female";
            entity.setGender(gender);
        }
        return com.javic.slimpatch.util.SkinPathHelper.getSkinForType("human_zombie_villager", gender, 1, entity.level());
    }

    @Override
    public void render(HumanZombieVillagerEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        boolean isFemale = entity.getGender().equalsIgnoreCase("female");

        if (isFemale) {
            this.model = femaleModel;
            poseStack.scale(0.95F, 0.95F, 0.95F);
        } else {
            this.model = maleModel;
            poseStack.scale(1.0F, 1.0F, 0.995F);
        }

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}