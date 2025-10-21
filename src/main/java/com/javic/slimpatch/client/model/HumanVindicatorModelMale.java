package com.javic.slimpatch.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.item.Items;

public class HumanVindicatorModelMale<T extends Vindicator> extends HumanoidModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(
                    ResourceLocation.fromNamespaceAndPath("slimpatch", "human_vindicator_male"),
                    "main"
            );

    public HumanVindicatorModelMale(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        return CustomVillagerModelMale.createBodyLayer();
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {

        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        if (entity.isAggressive() && entity.isHolding(Items.IRON_AXE)) {
            this.rightArm.xRot = (float) Math.toRadians(-110);
            this.rightArm.yRot = (float) Math.toRadians(10);
            this.leftArm.xRot = (float) Math.toRadians(-30);
            this.leftArm.yRot = (float) Math.toRadians(-10);
        }
    }
}