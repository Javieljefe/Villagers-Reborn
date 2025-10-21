package com.javic.slimpatch.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.item.Items;

public class HumanPillagerModelMale<T extends Pillager> extends HumanoidModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(
                    ResourceLocation.fromNamespaceAndPath("slimpatch", "human_pillager_male"),
                    "main"
            );

    public HumanPillagerModelMale(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        return CustomVillagerModelMale.createBodyLayer();
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {

        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        boolean hasCrossbowRight = entity.getMainHandItem().is(Items.CROSSBOW);
        boolean hasCrossbowLeft  = entity.getOffhandItem().is(Items.CROSSBOW);
        boolean isCharging = entity.isChargingCrossbow();
        boolean hasTarget = entity.getTarget() != null;

        if (isCharging) {
            this.rightArm.xRot = (float) Math.toRadians(-65);
            this.rightArm.yRot = (float) Math.toRadians(0);
            this.rightArm.zRot = 0.0F;

            this.leftArm.xRot = (float) Math.toRadians(-35);
            this.leftArm.yRot = (float) Math.toRadians(-15);
            this.leftArm.zRot = (float) Math.toRadians(10);
        }

        else if (hasCrossbowRight && hasTarget) {
            this.rightArm.xRot = (float) Math.toRadians(-90);
            this.rightArm.yRot = 0.0F;
            this.leftArm.xRot = (float) Math.toRadians(-30);
            this.leftArm.yRot = (float) Math.toRadians(15);
        }

        else if (hasCrossbowLeft && hasTarget) {
            this.leftArm.xRot = (float) Math.toRadians(-90);
            this.leftArm.yRot = (float) Math.toRadians(0);
            this.rightArm.xRot = (float) Math.toRadians(-30);
            this.rightArm.yRot = (float) Math.toRadians(-15);
        }

        else {
            this.rightArm.xRot = (float) Math.toRadians(-7);
            this.rightArm.yRot = 0.0F;
            this.leftArm.xRot = (float) Math.toRadians(-7);
            this.leftArm.yRot = 0.0F;
        }
    }
}