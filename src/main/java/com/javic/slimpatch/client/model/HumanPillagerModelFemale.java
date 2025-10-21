package com.javic.slimpatch.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.item.Items;

public class HumanPillagerModelFemale<T extends Pillager> extends HumanoidModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(
                    ResourceLocation.fromNamespaceAndPath("slimpatch", "human_pillager_female"),
                    "main"
            );

    public HumanPillagerModelFemale(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        return FemaleVillagerModel.createBodyLayer();
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
            this.rightArm.xRot = (float) Math.toRadians(-60);
            this.rightArm.yRot = 0.0F;
            this.rightArm.zRot = 0.0F;

            this.leftArm.xRot = (float) Math.toRadians(-30);
            this.leftArm.yRot = (float) Math.toRadians(-12);
            this.leftArm.zRot = (float) Math.toRadians(8);
        }

        else if (hasCrossbowRight && hasTarget) {
            this.rightArm.xRot = (float) Math.toRadians(-85);
            this.rightArm.yRot = 0.0F;
            this.leftArm.xRot = (float) Math.toRadians(-28);
            this.leftArm.yRot = (float) Math.toRadians(12);
        }

        else if (hasCrossbowLeft && hasTarget) {
            this.leftArm.xRot = (float) Math.toRadians(-85);
            this.leftArm.yRot = 0.0F;
            this.rightArm.xRot = (float) Math.toRadians(-28);
            this.rightArm.yRot = (float) Math.toRadians(-12);
        }

        else {
            this.rightArm.xRot = (float) Math.toRadians(-7);
            this.rightArm.yRot = 0.0F;
            this.leftArm.xRot = (float) Math.toRadians(-7);
            this.leftArm.yRot = 0.0F;
        }
    }
}