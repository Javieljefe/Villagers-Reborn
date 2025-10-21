package com.javic.slimpatch.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.item.Items;

/**
 * Modelo femenino para Human Pillager.
 * 🔹 Basado en FemaleVillagerModel.
 * 🔹 Añade animaciones de apuntado, recarga y pose relajada.
 */
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
        // Usa la misma geometría base del modelo femenino de aldeano
        return FemaleVillagerModel.createBodyLayer();
    }

    // ============================================================
    // 🔹 Animaciones de apuntado, recarga y descanso
    // ============================================================
    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {

        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        boolean hasCrossbowRight = entity.getMainHandItem().is(Items.CROSSBOW);
        boolean hasCrossbowLeft  = entity.getOffhandItem().is(Items.CROSSBOW);
        boolean isCharging = entity.isChargingCrossbow();
        boolean hasTarget = entity.getTarget() != null;

        // ============================================================
        // 🏹 Lógica de animaciones realista (femenina)
        // ============================================================

        // Si está tensando la ballesta
        if (isCharging) {
            this.rightArm.xRot = (float) Math.toRadians(-60);
            this.rightArm.yRot = 0.0F;
            this.rightArm.zRot = 0.0F;

            this.leftArm.xRot = (float) Math.toRadians(-30);
            this.leftArm.yRot = (float) Math.toRadians(-12);
            this.leftArm.zRot = (float) Math.toRadians(8);
        }

        // Si tiene ballesta en mano derecha y está apuntando a un objetivo
        else if (hasCrossbowRight && hasTarget) {
            this.rightArm.xRot = (float) Math.toRadians(-85);  // Apuntar
            this.rightArm.yRot = 0.0F;
            this.leftArm.xRot = (float) Math.toRadians(-28);   // Soporte
            this.leftArm.yRot = (float) Math.toRadians(12);
        }

        // Si tiene ballesta en la mano izquierda (raro) y está atacando
        else if (hasCrossbowLeft && hasTarget) {
            this.leftArm.xRot = (float) Math.toRadians(-85);
            this.leftArm.yRot = 0.0F;
            this.rightArm.xRot = (float) Math.toRadians(-28);
            this.rightArm.yRot = (float) Math.toRadians(-12);
        }

        // En reposo → ambos brazos abajo
        else {
            this.rightArm.xRot = (float) Math.toRadians(-7);
            this.rightArm.yRot = 0.0F;
            this.leftArm.xRot = (float) Math.toRadians(-7);
            this.leftArm.yRot = 0.0F;
        }
    }
}