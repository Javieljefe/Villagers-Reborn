package com.javic.slimpatch.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Evoker;

/**
 * Modelo femenino para Human Evoker.
 * 🔹 Basado en FemaleVillagerModel.
 * 🔹 Añade animación de conjuro (brazos levantados al lanzar hechizo).
 */
public class HumanEvokerModelFemale<T extends Evoker> extends HumanoidModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(
                    ResourceLocation.fromNamespaceAndPath("slimpatch", "human_evoker_female"),
                    "main"
            );

    public HumanEvokerModelFemale(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        // Usa la misma geometría base del modelo femenino de aldeano
        return FemaleVillagerModel.createBodyLayer();
    }

    // ============================================================
    // 🔮 Animaciones de conjuro (más suaves pero elevadas)
    // ============================================================
    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {

        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        if (entity.isCastingSpell()) {
            this.rightArm.xRot = (float) Math.toRadians(-125);
            this.rightArm.yRot = (float) Math.toRadians(18);
            this.leftArm.xRot = (float) Math.toRadians(-125);
            this.leftArm.yRot = (float) Math.toRadians(-18);
        }
    }
}