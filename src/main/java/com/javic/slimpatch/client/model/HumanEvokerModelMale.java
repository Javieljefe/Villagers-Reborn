package com.javic.slimpatch.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Evoker;

/**
 * Modelo masculino para Human Evoker.
 * 🔹 Basado en CustomVillagerModelMale.
 * 🔹 Añade animación de conjuro (brazos levantados al lanzar hechizo).
 */
public class HumanEvokerModelMale<T extends Evoker> extends HumanoidModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(
                    ResourceLocation.fromNamespaceAndPath("slimpatch", "human_evoker_male"),
                    "main"
            );

    public HumanEvokerModelMale(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        // Usa la misma geometría base del modelo masculino de aldeano
        return CustomVillagerModelMale.createBodyLayer();
    }

    // ============================================================
    // 🔮 Animaciones de conjuro (brazos levantados hacia el cielo)
    // ============================================================
    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {

        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        // Si está lanzando un hechizo
        if (entity.isCastingSpell()) {
            this.rightArm.xRot = (float) Math.toRadians(-130);
            this.rightArm.yRot = (float) Math.toRadians(20);
            this.leftArm.xRot = (float) Math.toRadians(-130);
            this.leftArm.yRot = (float) Math.toRadians(-20);
        }
    }
}