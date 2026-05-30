package com.javic.slimpatch.client.renderer;

import com.javic.slimpatch.Config;
import com.javic.slimpatch.entity.CommandableVillager;
import com.javic.slimpatch.entity.FamilyVillager;
import com.javic.slimpatch.entity.VillagerAgeStage;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.horse.Horse;

import java.io.File;
import java.io.FileInputStream;

abstract class AbstractVillagerHumanoidRenderer<T extends Mob & CommandableVillager> extends MobRenderer<T, HumanoidModel<T>> {

    protected static final float TODDLER_MODEL_SCALE = 0.6F;
    protected static final float CHILD_MODEL_SCALE = 0.75F;
    protected static final float TEEN_MODEL_SCALE = 0.9F;

    protected AbstractVillagerHumanoidRenderer(EntityRendererProvider.Context context, HumanoidModel<T> model) {
        super(context, model, 0.5F);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
        this.addLayer(new HumanoidArmorLayer<>(
                this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()
        ) {
            @Override
            public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T livingEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
                if (livingEntity.isArmorHidden()) {
                    return;
                }
                super.render(poseStack, buffer, packedLight, livingEntity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        String path = this.getCustomSkinPath(entity);
        if (path != null && !path.isEmpty()) {
            try {
                File file = new File(path);
                if (file.exists()) {
                    Minecraft mc = Minecraft.getInstance();
                    DynamicTexture dynamic = new DynamicTexture(NativeImage.read(new FileInputStream(file)));
                    ResourceLocation location = ResourceLocation.fromNamespaceAndPath(
                            "slimpatch",
                            "custom/" + entity.getUUID().toString().toLowerCase() + "/" + file.getName().toLowerCase()
                    );
                    mc.getTextureManager().register(location, dynamic);
                    return location;
                }
            } catch (Exception e) {
            }
        }

        return this.getFallbackTextureLocation(entity);
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        if (entity.getVehicle() instanceof Horse) {
            poseStack.translate(0.0F, this.getHorseMountedOffset(entity), 0.0F);
        } else if (entity.getVehicle() instanceof Camel) {
            poseStack.translate(0.0F, this.getCamelMountedOffset(entity), 0.0F);
        }
        this.applyModelScale(entity, poseStack);
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    @Override
    protected boolean shouldShowName(T entity) {
        return Config.VILLAGER_NAME_TAG.get() && super.shouldShowName(entity);
    }

    protected abstract String getCustomSkinPath(T entity);

    protected abstract ResourceLocation getFallbackTextureLocation(T entity);

    protected abstract float getHorseMountedOffset(T entity);

    protected abstract float getCamelMountedOffset(T entity);

    protected abstract void applyModelScale(T entity, PoseStack poseStack);

    protected VillagerAgeStage getAgeStage(T entity) {
        if (entity instanceof FamilyVillager familyVillager) {
            return familyVillager.getAgeStage();
        }
        return VillagerAgeStage.ADULT;
    }

    protected float getAgeStageScale(T entity) {
        return switch (this.getAgeStage(entity)) {
            case TODDLER -> TODDLER_MODEL_SCALE;
            case CHILD -> CHILD_MODEL_SCALE;
            case TEEN -> TEEN_MODEL_SCALE;
            case ADULT -> 1.0F;
        };
    }
}
